package com.grace.recon.common.resilience;

import static org.junit.jupiter.api.Assertions.*;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;
import org.awaitility.Awaitility;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@SpringBootTest(
    classes = {
      ResilienceAspects.class,
      CircuitBreakerConfiguration.class,
      RetryConfiguration.class,
      TimeLimiterConfiguration.class,
      ResilienceAspectsIntegrationTest.TestService.class
    })
@EnableAspectJAutoProxy(proxyTargetClass = true) // Enable AspectJ auto-proxying
class ResilienceAspectsIntegrationTest {

  @Autowired private TestService testService;

  // A simple service to apply resilience annotations to
  @Service
  static class TestService {

    private int circuitBreakerCallCount = 0;
    private int retryCallCount = 0;

    @CircuitBreaker(name = "externalService", fallbackMethod = "circuitBreakerFallback")
    public String circuitBreakerMethod(boolean shouldFail) {
      circuitBreakerCallCount++;
      if (shouldFail) {
        throw new RuntimeException("Simulated failure for Circuit Breaker");
      }
      return "Success";
    }

    private String circuitBreakerFallback(boolean shouldFail, Throwable t) {
      return "Fallback for Circuit Breaker: " + t.getMessage();
    }

    @Retry(name = "transientError", fallbackMethod = "retryFallback")
    public String retryMethod(boolean shouldFail) throws IOException {
      retryCallCount++;
      if (shouldFail) { // This will now always fail if shouldFail is true
        throw new IOException("Simulated failure for Retry");
      }
      return "Success after " + retryCallCount + " attempts";
    }

    private String retryFallback(boolean shouldFail, Throwable t) {
      return "Fallback for Retry: " + t.getMessage();
    }

    @TimeLimiter(name = "longRunningOperation", fallbackMethod = "timeLimiterFallback")
    public CompletableFuture<String> timeLimiterMethod(long delayMillis) {
      return CompletableFuture.supplyAsync(
          () -> {
            try {
              Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              throw new CompletionException(e);
            }
            return "Operation completed in " + delayMillis + "ms";
          });
    }

    private CompletableFuture<String> timeLimiterFallback(long delayMillis, Throwable t) {
      return CompletableFuture.completedFuture(
          "Fallback for TimeLimiter: " + t.getClass().getSimpleName());
    }
  }

  @Test
  void testRetrySuccess() throws IOException {
    testService.retryCallCount = 0; // Reset call count for this test
    String result = testService.retryMethod(false);
    Awaitility.await().atMost(Duration.ofSeconds(1)).until(() -> testService.retryCallCount == 1);
    assertEquals("Success after 1 attempts", result);
  }

  @Test
  void testRetryFailure() throws IOException {
    testService.retryCallCount = 0; // Reset call count for this test
    String result = testService.retryMethod(true);
    Awaitility.await().atMost(Duration.ofSeconds(1)).until(() -> testService.retryCallCount == 3);
    assertTrue(result.startsWith("Fallback for Retry"));
  }

  @Test
  void testTimeLimiterSuccess() throws Exception {
    CompletableFuture<String> future = testService.timeLimiterMethod(100);
    assertEquals("Operation completed in 100ms", future.get());
  }

  @Test
  void testTimeLimiterTimeout() {
    CompletableFuture<String> future = testService.timeLimiterMethod(6000); // Longer than 5 seconds timeout
    Awaitility.await().atMost(Duration.ofSeconds(10)).until(future::isDone);
    String result = future.join();
    assertEquals("Fallback for TimeLimiter: TimeoutException", result);
  }
}
