package com.grace.recon.common.monitoring;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class TraceServiceTest {

  @Mock private OpenTelemetry mockOpenTelemetry;
  @Mock private io.opentelemetry.api.trace.Tracer mockTracer;
  @Mock private SpanBuilder mockSpanBuilder;
  @Mock private Span mockSpan;
  @Mock private Scope mockScope;

  private TraceService traceService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    
    // Setup the mock chain
    when(mockOpenTelemetry.getTracer(anyString(), anyString())).thenReturn(mockTracer);
    when(mockTracer.spanBuilder(anyString())).thenReturn(mockSpanBuilder);
    when(mockSpanBuilder.startSpan()).thenReturn(mockSpan);
    when(mockSpan.makeCurrent()).thenReturn(mockScope);

    traceService = new TraceService(mockOpenTelemetry);
  }

  @Test
  void testStartSpan() {
    String spanName = "testSpan";
    Span span = traceService.startSpan(spanName);
    
    assertNotNull(span);
    verify(mockTracer).spanBuilder(spanName);
    verify(mockSpanBuilder).startSpan();
  }

  @Test
  void testStartAndMakeCurrentSpan() {
    String spanName = "testCurrentSpan";
    Scope scope = traceService.startAndMakeCurrentSpan(spanName);
    
    assertNotNull(scope);
    verify(mockTracer).spanBuilder(spanName);
    verify(mockSpanBuilder).startSpan();
    verify(mockSpan).makeCurrent();
  }

  @Test
  void testEndSpan() {
    traceService.endSpan(mockSpan);
    verify(mockSpan).end();
  }

  @Test
  void testEndSpan_nullSpan() {
    traceService.endSpan(null);
    // No exception should be thrown, and no interaction with mockSpan
    verifyNoInteractions(mockSpan);
  }

  @Test
  void testRecordException() {
    RuntimeException exception = new RuntimeException("Test Exception");
    traceService.recordException(mockSpan, exception);
    verify(mockSpan).recordException(exception);
  }

  @Test
  void testRecordException_nullSpan() {
    RuntimeException exception = new RuntimeException("Test Exception");
    traceService.recordException(null, exception);
    // No exception should be thrown, and no interaction with mockSpan
    verifyNoInteractions(mockSpan);
  }

  @Test
  void testGetCurrentSpan() {
    // Mock Span.current() behavior
    try (MockedStatic<Span> mockedSpan = Mockito.mockStatic(Span.class)) {
      mockedSpan.when(Span::current).thenReturn(mockSpan);
      Span currentSpan = traceService.getCurrentSpan();
      assertNotNull(currentSpan);
      assertEquals(mockSpan, currentSpan);
    }
  }
}
