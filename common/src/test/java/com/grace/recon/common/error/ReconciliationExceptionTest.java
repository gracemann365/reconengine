package com.grace.recon.common.error;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReconciliationExceptionTest {

    @Test
    void testDefaultConstructor() {
        ReconciliationException exception = new ReconciliationException();
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testMessageConstructor() {
        String message = "Test message";
        ReconciliationException exception = new ReconciliationException(message);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testMessageAndCauseConstructor() {
        String message = "Test message";
        Throwable cause = new RuntimeException("Original cause");
        ReconciliationException exception = new ReconciliationException(message, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testCauseConstructor() {
        Throwable cause = new RuntimeException("Original cause");
        ReconciliationException exception = new ReconciliationException(cause);
        assertEquals("java.lang.RuntimeException: Original cause", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testFullConstructor() {
        String message = "Test message";
        Throwable cause = new RuntimeException("Original cause");
        ReconciliationException exception = new ReconciliationException(message, cause, true, true);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testDataFormatException() {
        DataFormatException exception = new DataFormatException("Invalid data");
        assertTrue(exception instanceof ReconciliationException);
        assertEquals("Invalid data", exception.getMessage());
    }

    @Test
    void testKafkaProducerException() {
        KafkaProducerException exception = new KafkaProducerException("Kafka error");
        assertTrue(exception instanceof ReconciliationException);
        assertEquals("Kafka error", exception.getMessage());
    }
}
