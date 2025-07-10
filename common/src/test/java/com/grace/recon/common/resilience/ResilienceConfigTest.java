package com.grace.recon.common.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

 @SpringBootTest(classes = {CircuitBreakerConfiguration.class, RetryConfiguration.class, TimeLimiterConfiguration.class})
class ResilienceConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testExternalServiceCircuitBreakerConfigBean() {
        CircuitBreakerConfig config = applicationContext.getBean("externalServiceCircuitBreakerConfig", CircuitBreakerConfig.class);
        assertNotNull(config);
        assertEquals(50, config.getFailureRateThreshold());
        assertEquals(Duration.ofMillis(10000), config.getWaitIntervalFunctionInOpenState().apply(1));
        assertEquals(100, config.getSlidingWindowSize());
        assertEquals(10, config.getMinimumNumberOfCalls());
    }

    @Test
    void testDefaultCircuitBreakerConfigBean() {
        CircuitBreakerConfig config = applicationContext.getBean("defaultCircuitBreakerConfig", CircuitBreakerConfig.class);
        assertNotNull(config);
        assertEquals(CircuitBreakerConfig.ofDefaults().getFailureRateThreshold(), config.getFailureRateThreshold());
    }

    @Test
    void testTransientErrorRetryConfigBean() {
        RetryConfig config = applicationContext.getBean("transientErrorRetryConfig", RetryConfig.class);
        assertNotNull(config);
        assertEquals(3, config.getMaxAttempts());
        assertEquals(Duration.ofMillis(1000), config.getIntervalFunction().apply(1));
        assertTrue(config.getExceptionPredicate().test(new IOException()));
        assertTrue(config.getExceptionPredicate().test(new TimeoutException()));
    }

    @Test
    void testDefaultRetryConfigBean() {
        RetryConfig config = applicationContext.getBean("defaultRetryConfig", RetryConfig.class);
        assertNotNull(config);
        assertEquals(RetryConfig.ofDefaults().getMaxAttempts(), config.getMaxAttempts());
    }

    @Test
    void testLongRunningOperationTimeLimiterConfigBean() {
        TimeLimiterConfig config = applicationContext.getBean("longRunningOperationTimeLimiterConfig", TimeLimiterConfig.class);
        assertNotNull(config);
        assertEquals(Duration.ofSeconds(5), config.getTimeoutDuration());
        assertTrue(config.shouldCancelRunningFuture());
    }

    @Test
    void testDefaultTimeLimiterConfigBean() {
        TimeLimiterConfig config = applicationContext.getBean("defaultTimeLimiterConfig", TimeLimiterConfig.class);
        assertNotNull(config);
        assertEquals(TimeLimiterConfig.ofDefaults().getTimeoutDuration(), config.getTimeoutDuration());
    }
}