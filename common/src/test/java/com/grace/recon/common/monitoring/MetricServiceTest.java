package com.grace.recon.common.monitoring;

import static org.junit.jupiter.api.Assertions.*;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetricServiceTest {

  private MeterRegistry meterRegistry;
  private MetricService metricService;

  @BeforeEach
  void setUp() {
    meterRegistry = new SimpleMeterRegistry();
    metricService = new MetricService(meterRegistry);
  }

  @Test
  void testIncrementCounter() {
    String counterName = "test.counter";
    metricService.incrementCounter(counterName, "tag1", "value1");
    assertEquals(1.0, meterRegistry.counter(counterName, "tag1", "value1").count());
    metricService.incrementCounter(counterName, "tag1", "value1");
    assertEquals(2.0, meterRegistry.counter(counterName, "tag1", "value1").count());
  }

  @Test
  void testRegisterGauge() {
    String gaugeName = "test.gauge";
    // Test with a simple object and function
    class MyObject {
      double value = 10.0;
    }
    MyObject obj = new MyObject();
    metricService.registerGauge(gaugeName, obj, o -> o.value, "tag2", "value2");
    assertEquals(10.0, meterRegistry.get(gaugeName).tag("tag2", "value2").gauge().value());

    obj.value = 20.0;
    assertEquals(20.0, meterRegistry.get(gaugeName).tag("tag2", "value2").gauge().value());
  }

  @Test
  void testRecordTimerCallable() throws Exception {
    String timerName = "test.timer.callable";
    Callable<String> task =
        () -> {
          Thread.sleep(100);
          return "done";
        };
    String result = metricService.recordTimer(timerName, task, "task", "callable");
    assertEquals("done", result);
    Awaitility.await().atMost(Duration.ofSeconds(1)).until(() -> meterRegistry.get(timerName).tag("task", "callable").timer().count() > 0);
    Awaitility.await().atMost(Duration.ofSeconds(1)).until(() -> meterRegistry.get(timerName).tag("task", "callable").timer().totalTime(TimeUnit.MILLISECONDS) >= 100);
  }

  @Test
  void testRecordTimerRunnable() {
    String timerName = "test.timer.runnable";
    Runnable task =
        () -> {
          try {
            Thread.sleep(50);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        };
    metricService.recordTimer(timerName, task, "task", "runnable");
    Awaitility.await().atMost(Duration.ofSeconds(1)).until(() -> meterRegistry.get(timerName).tag("task", "runnable").timer().count() > 0);
    Awaitility.await().atMost(Duration.ofSeconds(1)).until(() -> meterRegistry.get(timerName).tag("task", "runnable").timer().totalTime(TimeUnit.MILLISECONDS) >= 50);
  }
}
