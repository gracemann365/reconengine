package com.grace.recon.orchestrator.kafka;

import com.grace.recon.orchestrator.model.QuantPair;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchingProducerService {

    private static final Logger logger = LoggerFactory.getLogger(MatchingProducerService.class);
    private static final String MATCHING_INPUT_TOPIC = "Matching_Input_Topic";

    private final KafkaTemplate<String, List<QuantPair>> kafkaTemplate;
    private final Counter quantsSentForMatchingCounter;

    @Autowired
    public MatchingProducerService(KafkaTemplate<String, List<QuantPair>> kafkaTemplate, MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.quantsSentForMatchingCounter = Counter.builder("orchestrator.quants.sent.matching")
                .description("Number of QuantPairs sent to matching topic")
                .register(meterRegistry);
    }

    public void sendQuantPairs(List<QuantPair> quantPairs) {
        if (quantPairs == null || quantPairs.isEmpty()) {
            logger.warn("Attempted to send empty or null list of QuantPairs.");
            return;
        }

        // Use the transactionId of the first QuantPair as the Kafka message key
        // to ensure correct partitioning and ordered processing by the downstream Matcher service.
        String key = quantPairs.get(0).getQuant1().getTransactionId();

        kafkaTemplate.send(MATCHING_INPUT_TOPIC, key, quantPairs)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        quantsSentForMatchingCounter.increment();
                        logger.info("Successfully sent {} QuantPairs to topic {} with key {}. Offset: {}",
                                quantPairs.size(), MATCHING_INPUT_TOPIC, key, result.getRecordMetadata().offset());
                    } else {
                        logger.error("Failed to send {} QuantPairs to topic {} with key {}. Error: {}",
                                quantPairs.size(), MATCHING_INPUT_TOPIC, key, ex.getMessage(), ex);
                    }
                });
    }
}
