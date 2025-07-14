package com.grace.recon.naas.kafka;

import com.grace.recon.common.dto.Quant;
import com.grace.recon.common.monitoring.MetricService;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaPublisherService {

  private static final Logger LOGGER = Logger.getLogger(KafkaPublisherService.class.getName());

  private static final String TOPIC = "UnifiedDTOs_Input";

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final MetricService metricService;

  @Autowired
  public KafkaPublisherService(
      KafkaTemplate<String, Object> kafkaTemplate, MetricService metricService) {
    this.kafkaTemplate = kafkaTemplate;
    this.metricService = metricService;
  }

  public void sendQuant(Quant quant) {
    try {
      // Explicitly cast to String to resolve incompatible types error
      String key = (String) quant.getTransactionId();
      kafkaTemplate.send(TOPIC, key, quant);
      LOGGER.info("Sent Quant to Kafka topic " + TOPIC + " with key " + key);
    } catch (Exception e) {
      LOGGER.severe("Failed to send Quant to Kafka: " + e.getMessage());
      // In a real system, a more robust fallback would be needed here.
    }
  }

  public void sendToDlq(String dlqTopic, Object failedRecord) {
    try {
      metricService.incrementCounter("naas.dlq.messages.sent", "topic", dlqTopic);
      kafkaTemplate.send(dlqTopic, failedRecord);
      LOGGER.info("Sent failed record to DLQ topic: " + dlqTopic);
    } catch (Exception e) {
      LOGGER.severe("Failed to send record to DLQ " + dlqTopic + ": " + e.getMessage());
    }
  }
}
