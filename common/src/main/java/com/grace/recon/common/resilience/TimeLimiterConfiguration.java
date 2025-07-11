package com.grace.recon.common.resilience;

import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Resilience4j TimeLimiter patterns. Defines default and custom time
 * limiter configurations.
 */
@Configuration
public class TimeLimiterConfiguration {

  /**
   * Defines a custom TimeLimiterConfig for long-running operations. This configuration can be
   * applied to specific time limiter instances.
   *
   * @return A custom TimeLimiterConfig instance.
   */
  @Bean
  public TimeLimiterConfig longRunningOperationTimeLimiterConfig() {
    return TimeLimiterConfig.custom()
        .timeoutDuration(Duration.ofSeconds(5)) // Maximum duration for the operation
        .cancelRunningFuture(true) // Whether to cancel the running future if timeout occurs
        .build();
  }

  /**
   * Defines a default TimeLimiterConfig. This will be used if no specific configuration is provided
   * for a time limiter instance.
   *
   * @return A default TimeLimiterConfig instance.
   */
  @Bean
  public TimeLimiterConfig defaultTimeLimiterConfig() {
    return TimeLimiterConfig.ofDefaults();
  }
}
