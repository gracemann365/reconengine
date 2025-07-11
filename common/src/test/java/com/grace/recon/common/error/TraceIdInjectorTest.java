package com.grace.recon.common.error;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class TraceIdInjectorTest {

  @Test
  void testInjectNewTraceId() {
    TraceIdInjector.clearTraceId(); // Ensure clean state
    assertNull(MDC.get(TraceIdInjector.TRACE_ID_KEY));

    TraceIdInjector.injectNewTraceId();
    String traceId = MDC.get(TraceIdInjector.TRACE_ID_KEY);
    assertNotNull(traceId);
    assertFalse(traceId.isEmpty());
    // Basic UUID format check (length, hyphens)
    assertTrue(traceId.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
  }

  @Test
  void testInjectTraceId_withProvidedId() {
    TraceIdInjector.clearTraceId();
    String customTraceId = "my-custom-trace-id-123";
    TraceIdInjector.injectTraceId(customTraceId);
    assertEquals(customTraceId, MDC.get(TraceIdInjector.TRACE_ID_KEY));
  }

  @Test
  void testInjectTraceId_withNullProvidedId() {
    TraceIdInjector.clearTraceId();
    TraceIdInjector.injectTraceId(null);
    assertNotNull(MDC.get(TraceIdInjector.TRACE_ID_KEY)); // Should generate a new one
  }

  @Test
  void testInjectTraceId_withEmptyProvidedId() {
    TraceIdInjector.clearTraceId();
    TraceIdInjector.injectTraceId("");
    assertNotNull(MDC.get(TraceIdInjector.TRACE_ID_KEY)); // Should generate a new one
  }

  @Test
  void testGetCurrentTraceId() {
    TraceIdInjector.clearTraceId();
    assertNull(TraceIdInjector.getCurrentTraceId());

    TraceIdInjector.injectNewTraceId();
    String traceId = TraceIdInjector.getCurrentTraceId();
    assertNotNull(traceId);
    assertFalse(traceId.isEmpty());
  }

  @Test
  void testClearTraceId() {
    TraceIdInjector.injectNewTraceId();
    assertNotNull(MDC.get(TraceIdInjector.TRACE_ID_KEY));

    TraceIdInjector.clearTraceId();
    assertNull(MDC.get(TraceIdInjector.TRACE_ID_KEY));
  }
}
