package com.grace.recon.matcher.kafka;

import com.grace.recon.common.dto.QuantPair;
import com.grace.recon.matcher.service.QuantBufferService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class MatchingInputConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MatchingInputConsumer.class);

  private final QuantBufferService quantBufferService;
  private final Counter messagesConsumedCounter;

  @Autowired
  public MatchingInputConsumer(QuantBufferService quantBufferService, MeterRegistry meterRegistry) {
    this.quantBufferService = quantBufferService;
    this.messagesConsumedCounter =
        meterRegistry.counter("matcher.kafka.consumer.messages.consumed");
  }

  @KafkaListener(
      topics = "${spring.kafka.topic.matching-input:Matching_Input_Topic}",
      groupId = "${spring.kafka.consumer.group-id}",
      containerFactory = "kafkaListenerContainerFactory",
      concurrency = "3")
  public void consumeMatchingInput(@Payload List<QuantPair> batch, Acknowledgment acknowledgment) {
    LOGGER.info("Received batch of {} QuantPairs from Orchestrator", batch.size());
    messagesConsumedCounter.increment(batch.size());

    try {
      for (QuantPair pair : batch) {
        quantBufferService.addToBuffer(pair);
      }
      acknowledgment.acknowledge();
      LOGGER.info(
          "Successfully processed and acknowledged batch of {} pairs. Current buffer size: {}",
          batch.size(),
          quantBufferService.getBufferSize());
    } catch (Exception e) {
      LOGGER.error("Error processing batch. Offsets will not be committed.", e);
    }
  }
}
