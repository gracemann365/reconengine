package com.grace.recon.common.monitoring;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;

/**
 * Service for managing and creating OpenTelemetry spans and traces.
 * This enables distributed tracing across microservices to monitor request flows
 * and identify performance bottlenecks.
 */
@Service
public class TraceService {

    private final Tracer tracer;

    public TraceService(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer("recon-engine-common", "1.0.0");
    }

    /**
     * Starts a new span with the given name.
     * @param spanName The name of the span.
     * @return The started Span object.
     */
    public Span startSpan(String spanName) {
        return tracer.spanBuilder(spanName).startSpan();
    }

    /**
     * Starts a new span and makes it the current span in the context.
     * The returned Scope must be closed to end the span's active state.
     * @param spanName The name of the span.
     * @return The Scope object, which must be closed.
     */
    public Scope startAndMakeCurrentSpan(String spanName) {
        Span span = startSpan(spanName);
        return span.makeCurrent();
    }

    /**
     * Ends the given span.
     * @param span The Span to end.
     */
    public void endSpan(Span span) {
        if (span != null) {
            span.end();
        }
    }

    /**
     * Records an exception on the given span.
     * @param span The Span to record the exception on.
     * @param throwable The Throwable to record.
     */
    public void recordException(Span span, Throwable throwable) {
        if (span != null) {
            span.recordException(throwable);
        }
    }

    /**
     * Returns the currently active span from the context.
     * @return The current Span, or Span.current() if no span is active.
     */
    public Span getCurrentSpan() {
        return Span.current();
    }
}
