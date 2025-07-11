package com.grace.recon.matcher.kafka;

import com.grace.recon.common.dto.Quant;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import com.grace.recon.matcher.MatcherApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.backoff.FixedBackOff;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = MatcherApplication.class)
@EmbeddedKafka(partitions = 1, topics = { "Matching_Input_Topic", "Maas_DLQ_Topic" })
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.yml", properties = "spring.main.allow-bean-definition-overriding=true")
class MatcherKafkaIntegrationTest {

    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Value("${spring.kafka.topic.matcher-dlq}")
    private String dlqTopic;

    private Consumer<String, String> dlqConsumer;

    @TestConfiguration
    static class TestKafkaConfig {

        @Value("${spring.kafka.topic.matcher-dlq}")
        private String dlqTopic;

        @Autowired
        private EmbeddedKafkaBroker embeddedKafka;

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, Quant> kafkaListenerContainerFactory() {
            ConcurrentKafkaListenerContainerFactory<String, Quant> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory());

            // KafkaTemplate for DLQ producer
            Map<String, Object> dlqProducerProps = KafkaTestUtils.producerProps(embeddedKafka);
            dlqProducerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            dlqProducerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            ProducerFactory<String, String> dlqPf = new DefaultKafkaProducerFactory<>(dlqProducerProps);
            KafkaTemplate<String, String> dlqKafkaTemplate = new KafkaTemplate<>(dlqPf);

            // DeadLetterPublishingRecoverer with custom value conversion for DLQ (Spring
            // Kafka 3.2.x+)
            DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(dlqKafkaTemplate,
                    (r, e) -> new TopicPartition(dlqTopic, -1)) {
                @Override
                protected org.apache.kafka.clients.producer.ProducerRecord<Object, Object> createProducerRecord(
                        org.apache.kafka.clients.consumer.ConsumerRecord<?, ?> record,
                        org.apache.kafka.common.TopicPartition topicPartition,
                        org.apache.kafka.common.header.Headers headers,
                        byte[] key,
                        byte[] value) {
                    Object originalValue = record.value();
                    Object dlqValue;
                    if (originalValue instanceof byte[]) {
                        dlqValue = new String((byte[]) originalValue); // Assuming UTF-8
                    } else {
                        dlqValue = originalValue != null ? originalValue.toString() : null;
                    }
                    return new org.apache.kafka.clients.producer.ProducerRecord<>(
                            topicPartition.topic(),
                            topicPartition.partition() < 0 ? null : topicPartition.partition(),
                            record.key(),
                            dlqValue,
                            headers);
                }
            };
            DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(0L, 0L));
            factory.setCommonErrorHandler(errorHandler);
            return factory;
        }

        @Bean
        public ConsumerFactory<String, Quant> consumerFactory() {
            Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("matching-consumer-group", "false",
                    embeddedKafka);
            consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            return new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(),
                    new JsonDeserializer<>(Quant.class));
        }
    }

    @BeforeEach
    void setUp() {
        // KafkaTemplate for sending test messages
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
        kafkaTemplate = new KafkaTemplate<>(pf);

        // DLQ Consumer
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-dlq-consumer", "true", embeddedKafka);
        dlqConsumer = new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(),
                new StringDeserializer()).createConsumer();
        dlqConsumer.subscribe(java.util.Collections.singletonList(dlqTopic));

        // The kafkaListenerContainerFactory and its error handler are now configured
        // via TestKafkaConfig
    }

    @AfterEach
    void tearDown() {
        dlqConsumer.close();
    }

    @Test
    void testPoisonPillRoutesToDLQ() {
        String poisonPill = "this-is-not-valid-json";
        kafkaTemplate.send("Matching_Input_Topic", poisonPill);

        ConsumerRecord<String, String> dlqRecord = KafkaTestUtils.getSingleRecord(dlqConsumer, dlqTopic,
                Duration.ofSeconds(10));

        assertNotNull(dlqRecord, "Message should be in DLQ");
        String actualValue = dlqRecord.value().replaceAll("^\"|\"$", "");
        boolean matches = poisonPill.equals(actualValue);
        if (!matches) {
            try {
                byte[] decodedBytes = java.util.Base64.getDecoder().decode(actualValue);
                String decoded = new String(decodedBytes);
                matches = poisonPill.equals(decoded);
            } catch (IllegalArgumentException e) {
                // Not Base64 encoded, ignore
            }
        }
        if (!matches) {
            System.err.println("DLQ value was: " + actualValue);
        }
        assertEquals(true, matches, "DLQ message should match poison pill (raw or Base64)");
    }
}