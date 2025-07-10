package com.grace.recon.orchestrator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class OrchestratorConfig {

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(8); // 8 threads for 8 processing units
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
