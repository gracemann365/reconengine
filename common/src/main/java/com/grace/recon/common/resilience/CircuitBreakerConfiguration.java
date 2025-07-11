package com.grace.recon.common.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Resilience4j Circuit Breaker patterns. Defines default and custom circuit
 * breaker configurations.
 */
@Configuration
public class CircuitBreakerConfiguration {

  /**
   * Defines a custom CircuitBreakerConfig for external service calls. This configuration can be
   * applied to specific circuit breaker instances.
   *
   * @return A custom CircuitBreakerConfig instance.
   */
  @Bean
  public CircuitBreakerConfig externalServiceCircuitBreakerConfig() {
    return CircuitBreakerConfig.custom()
        .failureRateThreshold(
            50) // Percentage of failures above which the circuit breaker should open
        .waitDurationInOpenState(
            Duration.ofMillis(10000)) // Duration the circuit breaker stays open
        .slidingWindowType(
            CircuitBreakerConfig.SlidingWindowType.COUNT_BASED) // Type of sliding window
        .slidingWindowSize(100) // Size of the sliding window
        .minimumNumberOfCalls(
            10) // Minimum number of calls in a sliding window to calculate failure rate
        .build();
  }

  /**
   * Defines a default CircuitBreakerConfig. This will be used if no specific configuration is
   * provided for a circuit breaker instance.
   *
   * @return A default CircuitBreakerConfig instance.
   */
  @Bean
  public CircuitBreakerConfig defaultCircuitBreakerConfig() {
    return CircuitBreakerConfig.ofDefaults();
  }
}
