package com.grace.recon.orchestrator.service;

import com.grace.recon.common.dto.Quant;
import com.grace.recon.orchestrator.kafka.EscalationProducerService;
import com.grace.recon.orchestrator.kafka.MatchingProducerService;
import com.grace.recon.orchestrator.kafka.UnifiedQuantConsumer;
import com.grace.recon.orchestrator.model.QuantPair;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BatchPreparationService {

  private static final Logger logger = LoggerFactory.getLogger(BatchPreparationService.class);

  private final UnifiedQuantConsumer unifiedQuantConsumer;
  private final ScheduledExecutorService scheduledExecutorService;
  private final EscalationProducerService escalationProducerService;
  private final MatchingProducerService matchingProducerService;
  private final MeterRegistry meterRegistry;

  private final int NUM_PROCESSING_UNITS = 8;
  private final ProcessingUnit[] processingUnits;

  @Autowired
  public BatchPreparationService(
      UnifiedQuantConsumer unifiedQuantConsumer,
      ScheduledExecutorService scheduledExecutorService,
      EscalationProducerService escalationProducerService,
      MatchingProducerService matchingProducerService,
      MeterRegistry meterRegistry) {
    this.unifiedQuantConsumer = unifiedQuantConsumer;
    this.scheduledExecutorService = scheduledExecutorService;
    this.escalationProducerService = escalationProducerService;
    this.matchingProducerService = matchingProducerService;
    this.meterRegistry = meterRegistry;
    this.processingUnits = new ProcessingUnit[NUM_PROCESSING_UNITS];
    for (int i = 0; i < NUM_PROCESSING_UNITS; i++) {
      processingUnits[i] =
          new ProcessingUnit(i, escalationProducerService, matchingProducerService, meterRegistry);
    }
  }

  @PostConstruct
  public void init() {
    // Schedule a task to continuously poll from the ingress buffer
    scheduledExecutorService.scheduleAtFixedRate(
        this::pollAndRouteQuants, 0, 10, TimeUnit.MILLISECONDS);
    logger.info("BatchPreparationService initialized and polling scheduled.");
  }

  private void pollAndRouteQuants() {
    Quant quant = unifiedQuantConsumer.getIngressBuffer().poll();
    if (quant != null) {
      try {
        int unitIndex = Math.abs(quant.getTransactionId().hashCode()) % NUM_PROCESSING_UNITS;
        processingUnits[unitIndex].processQuant(quant);
        logger.info("Routed Quant {} to ProcessingUnit {}", quant.getTransactionId(), unitIndex);
      } catch (Exception e) {
        logger.error(
            "Error routing Quant with transactionId {}: {}",
            quant.getTransactionId(),
            e.getMessage(),
            e);
        // TODO: Consider sending to a DLQ or applying backpressure if routing consistently fails
      }
    }
  }

  // Inner class for ProcessingUnit
  private class ProcessingUnit implements Runnable {
    private final int id;
    private final Map<String, TimedQuant> pairingArena = new ConcurrentHashMap<>();

    // Inner class to hold Quant and its entry time
    private class TimedQuant {
      private final Quant quant;
      private final long entryTime;

      public TimedQuant(Quant quant) {
        this.quant = quant;
        this.entryTime = System.currentTimeMillis();
      }

      public Quant getQuant() {
        return quant;
      }

      public long getEntryTime() {
        return entryTime;
      }
    }

    private final BlockingQueue<QuantPair> pairedQuantsBuffer = new LinkedBlockingQueue<>();
    private final EscalationProducerService escalationProducerService;
    private final MatchingProducerService matchingProducerService;
    private final ScheduledFuture<?> flushTask;
    private final AtomicInteger batchSize = new AtomicInteger(0);
    private long lastFlushTime = System.currentTimeMillis();

    private final Counter quantsPairedCounter;
    private final Timer pairingLatencyTimer;

    private static final long FLUSH_INTERVAL_MS = 5000; // 5 seconds
    private static final int BATCH_SIZE_THRESHOLD = 100; // 100 QuantPairs
    private static final long INCOMPLETE_PAIR_TIMEOUT_MS = 30000; // 30 seconds

    public ProcessingUnit(
        int id,
        EscalationProducerService escalationProducerService,
        MatchingProducerService matchingProducerService,
        MeterRegistry meterRegistry) {
      this.id = id;
      this.escalationProducerService = escalationProducerService;
      this.matchingProducerService = matchingProducerService;
      this.quantsPairedCounter =
          Counter.builder("orchestrator.quants.paired")
              .description("Number of Quants paired")
              .tag("unit", String.valueOf(id))
              .register(meterRegistry);
      this.pairingLatencyTimer =
          Timer.builder("orchestrator.pairing.latency")
              .description("Latency of Quant pairing operations")
              .tag("unit", String.valueOf(id))
              .register(meterRegistry);
      // Schedule the flush task for this processing unit
      this.flushTask =
          scheduledExecutorService.scheduleAtFixedRate(
              this, FLUSH_INTERVAL_MS, FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS);
      logger.info("ProcessingUnit {} initialized.", id);
    }

    public void processQuant(Quant quant) {
      try {
        // Simple pairing logic: if a quant with the same transactionId exists, pair them
        // This is a simplified example; real pairing logic would be more complex
        String transactionId = quant.getTransactionId();
        if (pairingArena.containsKey(transactionId)) {
          TimedQuant existingTimedQuant = pairingArena.remove(transactionId);
          Quant existingQuant = existingTimedQuant.getQuant();
          QuantPair quantPair = new QuantPair(existingQuant, quant);
          pairedQuantsBuffer.offer(quantPair);
          batchSize.incrementAndGet();
          quantsPairedCounter.increment();
          pairingLatencyTimer.record(
              System.currentTimeMillis() - existingTimedQuant.getEntryTime(),
              TimeUnit.MILLISECONDS);
          logger.info(
              "ProcessingUnit {}: Paired Quants for transactionId {}. Paired buffer size: {}",
              id,
              transactionId,
              pairedQuantsBuffer.size());
          checkAndFlush();
        } else {
          pairingArena.put(transactionId, new TimedQuant(quant));
          logger.info(
              "ProcessingUnit {}: Added Quant for transactionId {} to pairing arena.",
              id,
              transactionId);
        }
      } catch (Exception e) {
        logger.error(
            "ProcessingUnit {}: Error processing Quant with transactionId {}: {}",
            id,
            quant.getTransactionId(),
            e.getMessage(),
            e);
      }
    }

    private void checkAndFlush() {
      try {
        long currentTime = System.currentTimeMillis();
        if (batchSize.get() >= BATCH_SIZE_THRESHOLD
            || (currentTime - lastFlushTime >= FLUSH_INTERVAL_MS && batchSize.get() > 0)) {
          flushPairedQuants();
        }
      } catch (Exception e) {
        logger.error("ProcessingUnit {}: Error in checkAndFlush: {}", id, e.getMessage(), e);
      }
    }

    private void flushPairedQuants() {
      try {
        if (pairedQuantsBuffer.isEmpty()) {
          return;
        }
        List<QuantPair> batch = new java.util.ArrayList<>();
        pairedQuantsBuffer.drainTo(batch);
        if (!batch.isEmpty()) {
          matchingProducerService.sendQuantPairs(batch);
          batchSize.set(0);
          lastFlushTime = System.currentTimeMillis();
          logger.info("ProcessingUnit {}: Flushed {} QuantPairs.", id, batch.size());
        }
      } catch (Exception e) {
        logger.error("ProcessingUnit {}: Error flushing paired Quants: {}", id, e.getMessage(), e);
      }
    }

    @Override
    public void run() {
      try {
        // This run method is for the scheduled flush task
        checkAndFlush();

        long currentTime = System.currentTimeMillis();
        pairingArena
            .entrySet()
            .removeIf(
                entry -> {
                  TimedQuant timedQuant = entry.getValue();
                  if (currentTime - timedQuant.getEntryTime() >= INCOMPLETE_PAIR_TIMEOUT_MS) {
                    logger.warn(
                        "ProcessingUnit {}: Quant with transactionId {} timed out. Escalating.",
                        id,
                        timedQuant.getQuant().getTransactionId());
                    escalationProducerService.escalateIncompleteQuant(
                        new com.grace.recon.orchestrator.model.IncompleteQuant(
                            timedQuant.getQuant(), timedQuant.getEntryTime(), "Pairing timeout"));
                    return true; // Remove from map
                  }
                  return false; // Keep in map
                });
      } catch (Exception e) {
        logger.error("ProcessingUnit {}: Error in scheduled run task: {}", id, e.getMessage(), e);
      }
    }
  }
}
