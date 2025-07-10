package com.grace.recon.common.error;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Utility to inject and manage distributed trace IDs within the application's logging context.
 * This helps in correlating log messages across different services in a distributed system.
 */
public class TraceIdInjector {

    public static final String TRACE_ID_KEY = "traceId";

    /**
     * Generates a new unique trace ID and injects it into the MDC (Mapped Diagnostic Context).
     * If a trace ID already exists in the MDC, it will be overwritten.
     */
    public static void injectNewTraceId() {
        String traceId = UUID.randomUUID().toString();
        MDC.put(TRACE_ID_KEY, traceId);
    }

    /**
     * Injects a given trace ID into the MDC.
     * @param traceId The trace ID to inject.
     */
    public static void injectTraceId(String traceId) {
        if (traceId != null && !traceId.isEmpty()) {
            MDC.put(TRACE_ID_KEY, traceId);
        } else {
            injectNewTraceId(); // Generate a new one if the provided is null or empty
        }
    }

    /**
     * Retrieves the current trace ID from the MDC.
     * @return The current trace ID, or null if not present.
     */
    public static String getCurrentTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * Removes the trace ID from the MDC.
     * This should be called at the end of a request or process to clean up the MDC.
     */
    public static void clearTraceId() {
        MDC.remove(TRACE_ID_KEY);
    }
}
