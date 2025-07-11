package com.grace.recon.orchestrator.service;

import com.grace.recon.common.dto.Quant;
import com.grace.recon.common.dto.QuantPair;
import com.grace.recon.common.dto.output.UreQuant;
import com.grace.recon.orchestrator.kafka.EscalationProducerService;
import com.grace.recon.orchestrator.kafka.MatchingProducerService;
import com.grace.recon.orchestrator.kafka.UnifiedQuantConsumer;
import com.grace.recon.orchestrator.model.TimedQuant;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class BatchPreparationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchPreparationService.class);
    private final CopyOnWriteArrayList<ProcessingUnit> processingUnits = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduledExecutor;

    public BatchPreparationService(UnifiedQuantConsumer quantConsumer, MeterRegistry meterRegistry,
                                 MatchingProducerService matchingProducerService, EscalationProducerService escalationProducerService) {
        this.scheduledExecutor = Executors.newScheduledThreadPool(8); // For timeout checks
        for (int i = 0; i < 8; i++) {
            ProcessingUnit unit = new ProcessingUnit(i, matchingProducerService, escalationProducerService);
            processingUnits.add(unit);
            // Schedule the timeout check to run periodically for each unit
            scheduledExecutor.scheduleAtFixedRate(unit, 10, 10, TimeUnit.SECONDS);
        }
        // Start consuming from the buffer and routing to processing units
        new Thread(() -> quantConsumer.getIngressBuffer().forEach(this::routeQuant)).start();
    }

    private void routeQuant(Quant quant) {
        if (quant != null && quant.getTransactionId() != null) {
            int unitIndex = Math.abs(quant.getTransactionId().hashCode() % processingUnits.size());
            processingUnits.get(unitIndex).addQuant(quant);
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduledExecutor.shutdownNow();
    }

    private static class ProcessingUnit implements Runnable {
        private final int id;
        private final Map<String, TimedQuant> pairingArena = new ConcurrentHashMap<>();
        private final BlockingQueue<QuantPair> pairedQuantsBuffer = new LinkedBlockingQueue<>();
        private final EscalationProducerService escalationProducerService;
        private final MatchingProducerService matchingProducerService;
        
        private static final long PAIRING_TIMEOUT_MS = 30000;
        private static final int BATCH_SIZE_THRESHOLD = 100;

        public ProcessingUnit(int id, MatchingProducerService matchingProducerService, EscalationProducerService escalationProducerService) {
            this.id = id;
            this.matchingProducerService = matchingProducerService;
            this.escalationProducerService = escalationProducerService;
        }

        public void addQuant(Quant quant) {
            pairingArena.compute(quant.getTransactionId(), (key, existing) -> {
                if (existing == null) {
                    return new TimedQuant(quant);
                } else {
                    pairedQuantsBuffer.add(new QuantPair(existing.getQuant(), quant));
                    return null; // Remove from arena
                }
            });
            flushPairedQuants();
        }

        private void flushPairedQuants() {
            if (pairedQuantsBuffer.size() >= BATCH_SIZE_THRESHOLD) {
                List<QuantPair> batch = new ArrayList<>();
                pairedQuantsBuffer.drainTo(batch, BATCH_SIZE_THRESHOLD);
                if (!batch.isEmpty()) {
                    matchingProducerService.sendBatch(batch);
                }
            }
        }
        
        @Override
        public void run() {
            // This 'run' method is for the scheduled timeout check
            pairingArena.values().removeIf(this::handleTimeout);
        }

        private boolean handleTimeout(TimedQuant timedQuant) {
            if (System.currentTimeMillis() - timedQuant.getEntryTime() > PAIRING_TIMEOUT_MS) {
                UreQuant ureDto = new UreQuant(
                    new QuantPair(timedQuant.getQuant(), null),
                    "PAIRING_TIMEOUT",
                    "Quant did not find its partner within the defined window.",
                    System.currentTimeMillis()
                );
                escalationProducerService.publishUreQuant(ureDto);
                return true; // Remove from map
            }
            return false; // Keep in map
        }
    }
}
