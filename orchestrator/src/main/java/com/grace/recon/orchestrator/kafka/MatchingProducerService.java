package com.grace.recon.orchestrator.kafka;

import com.grace.recon.common.dto.QuantPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

public class MatchingProducerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchingProducerService.class);

    @Value("${spring.kafka.topic.matching-input}")
    private String topic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public MatchingProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Corrected method name and parameter type
    public void sendBatch(List<QuantPair> batch) {
        if (batch == null || batch.isEmpty()) {
            return;
        }
        try {
            String key = batch.get(0).getHusband().getTransactionId();
            kafkaTemplate.send(topic, key, batch);
            LOGGER.info("Sent batch of {} pairs to topic {}", batch.size(), topic);
        } catch (Exception e) {
            LOGGER.error("Failed to send batch to topic {}", topic, e);
        }
    }
}