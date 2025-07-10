package com.grace.recon.common.resilience;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Spring AOP aspects to apply Resilience4j annotations ( @CircuitBreaker, @Retry, @TimeLimiter)
 * to methods. This allows for declarative application of resilience patterns.
 */
@Component
@Aspect
public class ResilienceAspects {

    private static final Logger log = LoggerFactory.getLogger(ResilienceAspects.class);

    /**
     * Around advice for methods annotated with @CircuitBreaker.
     * Applies circuit breaking logic to the method execution.
     *
     * @param joinPoint The proceeding join point.
     * @return The result of the method execution.
     * @throws Throwable If the method execution throws an exception.
     */
    @Around("@annotation(io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker)")
    public Object circuitBreakerAroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("Applying CircuitBreaker to {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        return joinPoint.proceed();
    }

    /**
     * Around advice for methods annotated with @Retry.
     * Applies retry logic to the method execution.
     *
     * @param joinPoint The proceeding join point.
     * @return The result of the method execution.
     * @throws Throwable If the method execution throws an exception.
     */
    @Around("@annotation(io.github.resilience4j.retry.annotation.Retry)")
    public Object retryAroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("Applying Retry to {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        return joinPoint.proceed();
    }

    /**
     * Around advice for methods annotated with @TimeLimiter.
     * Applies time limiting logic to the method execution.
     *
     * @param joinPoint The proceeding joinPoint.
     * @return The result of the method execution.
     * @throws Throwable If the method execution throws an exception.
     */
    @Around("@annotation(io.github.resilience4j.timelimiter.annotation.TimeLimiter)")
    public Object timeLimiterAroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("Applying TimeLimiter to {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        return joinPoint.proceed();
    }
}