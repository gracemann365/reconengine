package com.grace.recon.orchestrator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
