package com.grace.recon.common.resilience;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Resilience4j Retry patterns. Defines default and custom retry
 * configurations.
 */
@Configuration
public class RetryConfiguration {

  /**
   * Defines a custom RetryConfig for transient errors. This configuration can be applied to
   * specific retry instances.
   *
   * @return A custom RetryConfig instance.
   */
  @Bean
  public RetryConfig transientErrorRetryConfig() {
    return RetryConfig.custom()
        .maxAttempts(3) // Maximum number of retry attempts
        .intervalFunction(
            IntervalFunction.of(
                Duration.ofMillis(1000))) // Time to wait before the next retry attempt
        .retryOnException(
            e ->
                e instanceof IOException
                    || e instanceof TimeoutException) // Exceptions that trigger a retry
        .build();
  }

  /**
   * Defines a default RetryConfig. This will be used if no specific configuration is provided for a
   * retry instance.
   *
   * @return A default RetryConfig instance.
   */
  @Bean
  public RetryConfig defaultRetryConfig() {
    return RetryConfig.ofDefaults();
  }
}
