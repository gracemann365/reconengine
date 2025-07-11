package com.grace.recon.matcher.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest @EmbeddedKafka(partitions = 1,
                   topics = { "${spring.kafka.topic.matching-input}", "${spring.kafka.topic.matcher-dlq}" })
class MatcherKafkaIntegrationTest {

        @Autowired
        private KafkaTemplate<String, Object> kafkaTemplate; // Autowire the main app template

        @Value("${spring.kafka.topic.matcher-dlq}")
        private String dlqTopic;

        private Consumer<String, String> dlqConsumer; // This is a test-only consumer

        @BeforeEach
        void setUp(EmbeddedKafkaBroker embeddedKafka) {
            // Programmatically create a consumer just for this test
            Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-dlq-consumer", "true", embeddedKafka);
            dlqConsumer = new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new StringDeserializer()).createConsumer();
            dlqConsumer.subscribe(java.util.Collections.singletonList(dlqTopic));
        }

        @AfterEach
        void tearDown() {
            dlqConsumer.close();
        }

        @Test
        void testPoisonPillRoutesToDLQ() {
            String poisonPill = "this-is-not-valid-json";
            
            // Send the bad message using the main application's template
            kafkaTemplate.send("${spring.kafka.topic.matching-input}", poisonPill);

            // Use our test consumer to verify the message lands on the DLQ
            ConsumerRecord<String, String> dlqRecord = KafkaTestUtils.getSingleRecord(dlqConsumer, dlqTopic, Duration.ofSeconds(10));

            assertNotNull(dlqRecord, "Message should be in DLQ");
            assertEquals(poisonPill, dlqRecord.value(), "DLQ message should match poison pill");
        }
    }