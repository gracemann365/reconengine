package com.grace.recon.orchestrator.kafka;

import com.grace.recon.common.dto.output.UreQuant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

public class EscalationProducerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EscalationProducerService.class);

    @Value("${spring.kafka.topic.escalation}")
    private String escalationTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EscalationProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUreQuant(UreQuant ure) {
        try {
            String key = ure.getQuantPair().getHusband().getTransactionId();
            kafkaTemplate.send(escalationTopic, key, ure);
            LOGGER.warn("Published URE for transactionId {} to topic {}", key, escalationTopic);
        } catch (Exception e) {
            LOGGER.error("Failed to publish UreQuant to topic {}", escalationTopic, e);
        }
    }
}