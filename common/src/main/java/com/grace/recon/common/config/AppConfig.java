package com.grace.recon.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

  @Value("${kafka.bootstrap.servers:localhost:9092}")
  private String kafkaBootstrapServers;

  @Value("${common.buffer.capacity:10000}")
  private int commonBufferCapacity;

  public String getKafkaBootstrapServers() {
    return kafkaBootstrapServers;
  }

  public int getCommonBufferCapacity() {
    return commonBufferCapacity;
  }
}
