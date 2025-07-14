package com.grace.recon.common.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.Callable;
import java.util.function.ToDoubleFunction;
import org.springframework.stereotype.Service;

/**
 * Service for publishing custom metrics using Micrometer. This allows for instrumenting the
 * application with various metric types like counters, gauges, and timers, which can then be
 * exported to monitoring systems.
 */
@Service
public class MetricService {

  private final MeterRegistry meterRegistry;

  public MetricService(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  /**
   * Increments a counter metric.
   *
   * @param name The name of the counter.
   * @param tags Optional tags (key-value pairs) to associate with the metric.
   */
  public void incrementCounter(String name, String... tags) {
    Counter.builder(name).tags(tags).register(meterRegistry).increment();
  }

  /**
   * Registers a gauge metric.
   *
   * @param name The name of the gauge.
   * @param obj The object to be gauged.
   * @param valueFunction The function to extract the double value from the object.
   * @param tags Optional tags (key-value pairs) to associate with the metric.
   * @param <T> The type of the object being gauged.
   */
  public <T> void registerGauge(
      String name, T obj, ToDoubleFunction<T> valueFunction, String... tags) {
    Gauge.builder(name, obj, valueFunction).tags(tags).register(meterRegistry);
  }

  /**
   * Records the time taken by an operation.
   *
   * @param name The name of the timer.
   * @param callable The operation to time.
   * @param tags Optional tags (key-value pairs) to associate with the metric.
   * @param <T> The return type of the callable.
   * @return The result of the callable.
   * @throws Exception If the callable throws an exception.
   */
  public <T> T recordTimer(String name, Callable<T> callable, String... tags) throws Exception {
    return Timer.builder(name).tags(tags).register(meterRegistry).recordCallable(callable);
  }

  /**
   * Records the time taken by a runnable operation.
   *
   * @param name The name of the timer.
   * @param runnable The operation to time.
   * @param tags Optional tags (key-value pairs) to associate with the metric.
   */
  public void recordTimer(String name, Runnable runnable, String... tags) {
    Timer.builder(name).tags(tags).register(meterRegistry).record(runnable);
  }
}
