package com.grace.recon.orchestrator.kafka;

import com.grace.recon.orchestrator.model.IncompleteQuant;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EscalationProducerService {

    private static final Logger logger = LoggerFactory.getLogger(EscalationProducerService.class);
    private static final String ESCALATION_TOPIC = "Escalation_Topic";

    private final KafkaTemplate<String, IncompleteQuant> kafkaTemplate;
    private final Counter quantsEscalatedCounter;

    @Autowired
    public EscalationProducerService(KafkaTemplate<String, IncompleteQuant> kafkaTemplate, MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.quantsEscalatedCounter = Counter.builder("orchestrator.quants.escalated")
                .description("Number of Quants escalated due to timeout")
                .register(meterRegistry);
    }

    public void escalateIncompleteQuant(IncompleteQuant incompleteQuant) {
        if (incompleteQuant == null) {
            logger.warn("Attempted to escalate a null IncompleteQuant.");
            return;
        }

        String key = incompleteQuant.getQuant().getTransactionId();

        kafkaTemplate.send(ESCALATION_TOPIC, key, incompleteQuant)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        quantsEscalatedCounter.increment();
                        logger.info("Successfully escalated IncompleteQuant with transactionId {} to topic {}. Offset: {}",
                                incompleteQuant.getQuant().getTransactionId(), ESCALATION_TOPIC, result.getRecordMetadata().offset());
                    } else {
                        logger.error("Failed to escalate IncompleteQuant with transactionId {} to topic {}. Error: {}",
                                incompleteQuant.getQuant().getTransactionId(), ESCALATION_TOPIC, ex.getMessage(), ex);
                    }
                });
    }
}
