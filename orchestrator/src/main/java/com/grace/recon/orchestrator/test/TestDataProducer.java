package com.grace.recon.orchestrator.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grace.recon.common.dto.Quant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;
import java.util.List;

@Component
public class TestDataProducer implements CommandLineRunner {

    private static final String UNIFIED_DTOS_INPUT_TOPIC = "UnifiedDTOs_Input";
    private static final String TEST_DATA_FILE = "testdata.json";

    private final KafkaTemplate<String, Quant> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    @Autowired
    public TestDataProducer(KafkaTemplate<String, Quant> kafkaTemplate, ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting TestDataProducer to send Quants to Kafka...");

        try {
            // Try to load from classpath first, then filesystem
            Resource resource = resourceLoader.getResource("classpath:" + TEST_DATA_FILE);
            if (!resource.exists()) {
                resource = resourceLoader.getResource("file:" + TEST_DATA_FILE);
            }

            if (!resource.exists()) {
                System.err.println("Error: " + TEST_DATA_FILE + " not found in classpath or project root.");
                return;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                List<Quant> quants = objectMapper.readValue(inputStream, new TypeReference<List<Quant>>() {});

                for (Quant quant : quants) {
                    // Use transactionId as key for partitioning
                    kafkaTemplate.send(UNIFIED_DTOS_INPUT_TOPIC, quant.getTransactionId(), quant);
                    System.out.println("Sent Quant: " + quant.getTransactionId() + " from " + quant.getSourceSystem());
                }
                System.out.println("Finished sending " + quants.size() + " Quants to " + UNIFIED_DTOS_INPUT_TOPIC);
            }
        } catch (Exception e) {
            System.err.println("Error sending test data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
