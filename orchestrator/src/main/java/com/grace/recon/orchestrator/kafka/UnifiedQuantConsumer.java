package com.grace.recon.orchestrator.kafka;

import com.grace.recon.common.dto.Quant;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UnifiedQuantConsumer {

  private static final Logger logger = LoggerFactory.getLogger(UnifiedQuantConsumer.class);
  private final ConcurrentLinkedQueue<Quant> ingressBuffer = new ConcurrentLinkedQueue<>();
  private static final int MAX_BUFFER_CAPACITY = 10000; // Example capacity

  private final Counter quantsConsumedCounter;
  private final Counter quantsDroppedCounter;

  @Autowired
  public UnifiedQuantConsumer(MeterRegistry meterRegistry) {
    this.quantsConsumedCounter =
        Counter.builder("orchestrator.quants.consumed")
            .description("Number of Quants consumed from Kafka")
            .register(meterRegistry);
    this.quantsDroppedCounter =
        Counter.builder("orchestrator.quants.dropped")
            .description("Number of Quants dropped due to full buffer")
            .register(meterRegistry);
    Gauge.builder("orchestrator.ingress.buffer.size", ingressBuffer::size)
        .description("Current size of the ingress buffer")
        .register(meterRegistry);
    Gauge.builder("orchestrator.ingress.buffer.capacity", () -> MAX_BUFFER_CAPACITY)
        .description("Maximum capacity of the ingress buffer")
        .register(meterRegistry);
  }

  @KafkaListener(
      topics = "UnifiedDTOs_Input",
      groupId = "orchestrator-group",
      containerFactory = "kafkaListenerContainerFactory")
  public void listen(ConsumerRecord<String, Quant> record) {
    try {
      Quant quant = record.value();
      quantsConsumedCounter.increment();
      if (ingressBuffer.size() < MAX_BUFFER_CAPACITY) {
        ingressBuffer.offer(quant);
        logger.info(
            "Consumed Quant with transactionId: {} and added to buffer. Current buffer size: {}",
            quant.getTransactionId(),
            ingressBuffer.size());
      } else {
        quantsDroppedCounter.increment();
        logger.warn(
            "Ingress buffer is full. Dropping Quant with transactionId: {}. Consider increasing buffer capacity or processing speed.",
            quant.getTransactionId());
        // TODO: Implement more robust overflow handling, e.g., send to DLQ or apply backpressure
      }
    } catch (Exception e) {
      logger.error(
          "Error consuming Kafka message from UnifiedDTOs_Input. Record offset: {}, partition: {}. Error: {}",
          record.offset(),
          record.partition(),
          e.getMessage(),
          e);
      // Depending on the error, Spring Kafka's default error handler might re-attempt or move to
      // next record.
    }
  }

  public ConcurrentLinkedQueue<Quant> getIngressBuffer() {
    return ingressBuffer;
  }
}
