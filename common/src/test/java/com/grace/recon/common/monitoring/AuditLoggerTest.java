package com.grace.recon.common.monitoring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AuditLoggerTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    private MockedStatic<LoggerFactory> mockedLoggerFactory;
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));

        // Mock LoggerFactory and Logger
        mockedLoggerFactory = Mockito.mockStatic(LoggerFactory.class);
        mockLogger = Mockito.mock(Logger.class);
        mockedLoggerFactory.when(() -> LoggerFactory.getLogger("AUDIT")).thenReturn(mockLogger);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        mockedLoggerFactory.close(); // Close the mocked static
    }

    @Test
    void testLogEvent_withDetails() {
        Map<String, String> details = new HashMap<>();
        details.put("user", "testUser");
        details.put("action", "login");

        AuditLogger.logEvent("User Login", details);

        // Verify that the logger's info method was called
        verify(mockLogger, times(1)).info(eq("User Login"));

        // Verify MDC interactions (though direct MDC verification is harder with static mocks)
        // We rely on the implementation detail that MDC.put/remove are called.
    }

    @Test
    void testLogEvent_noDetails() {
        AuditLogger.logEvent("System Startup");

        // Verify that the logger's info method was called
        verify(mockLogger, times(1)).info(eq("System Startup"));
    }
}
