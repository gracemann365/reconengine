package com.grace.recon.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AppConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "kafka.bootstrap.servers=test-kafka:9092",
    "common.buffer.capacity=5000"
})
class AppConfigTest {

    @Autowired
    private AppConfig appConfig;

    @Test
    void testKafkaBootstrapServersOverriddenValue() {
        assertEquals("test-kafka:9092", appConfig.getKafkaBootstrapServers());
    }

    @Test
    void testCommonBufferCapacityOverriddenValue() {
        assertEquals(5000, appConfig.getCommonBufferCapacity());
    }
}
