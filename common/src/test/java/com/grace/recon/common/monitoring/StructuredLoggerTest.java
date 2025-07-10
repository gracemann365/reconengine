package com.grace.recon.common.monitoring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StructuredLoggerTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        MDC.clear(); // Clear MDC before each test
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        MDC.clear(); // Clear MDC after each test
    }

    @Test
    void testInfoWithStructuredData() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        Map<String, String> data = new HashMap<>();
        data.put("key1", "value1");
        data.put("key2", "value2");

        logger.info("Test message", data);

        String logOutput = outContent.toString();
        assertTrue(logOutput.contains("Test message"));
        assertTrue(logOutput.contains("key1=value1"));
        assertTrue(logOutput.contains("key2=value2"));

        // Verify MDC is cleared
        assertTrue(MDC.getCopyOfContextMap() == null || MDC.getCopyOfContextMap().isEmpty());
    }

    @Test
    void testErrorWithStructuredData() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        Map<String, String> data = new HashMap<>();
        data.put("errorCode", "123");

        logger.error("Error message", data);

        String logOutput = outContent.toString();
        assertTrue(logOutput.contains("Error message"));
        assertTrue(logOutput.contains("errorCode=123"));

        // Verify MDC is cleared
        assertTrue(MDC.getCopyOfContextMap() == null || MDC.getCopyOfContextMap().isEmpty());
    }

    @Test
    void testInfoWithoutStructuredData() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        logger.info("Simple info message");

        String logOutput = outContent.toString();
        assertTrue(logOutput.contains("Simple info message"));
        assertTrue(MDC.getCopyOfContextMap() == null || MDC.getCopyOfContextMap().isEmpty());
    }
}
