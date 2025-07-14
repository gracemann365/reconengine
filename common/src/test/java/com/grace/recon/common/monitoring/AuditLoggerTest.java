package com.grace.recon.common.monitoring;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.MDC;

class AuditLoggerTest {

  private Logger mockLogger;
  private Logger originalLogger;

  private void setLogger(Logger logger) throws NoSuchFieldException, IllegalAccessException {
    Field logField = AuditLogger.class.getDeclaredField("AUDIT_LOGGER");
    logField.setAccessible(true);
    originalLogger = (Logger) logField.get(null);
    logField.set(null, logger);
  }

  private void restoreLogger() throws NoSuchFieldException, IllegalAccessException {
    if (originalLogger != null) {
      Field logField = AuditLogger.class.getDeclaredField("AUDIT_LOGGER");
      logField.setAccessible(true);
      logField.set(null, originalLogger);
    }
  }

  @BeforeEach
  void setUp() throws NoSuchFieldException, IllegalAccessException {
    // Clear any existing MDC context
    MDC.clear();
    
    // Create mock logger and set it
    mockLogger = Mockito.mock(Logger.class);
    setLogger(mockLogger);
  }

  @AfterEach
  void tearDown() throws NoSuchFieldException, IllegalAccessException {
    restoreLogger();
    MDC.clear();
  }

  @Test
  void testLogEvent_withDetails() {
    // Prepare test data
    Map<String, String> details = new HashMap<>();
    details.put("user", "testUser");
    details.put("action", "login");

    // Execute the test
    AuditLogger.logEvent("User Login", details);

    // Verify logger interactions
    verify(mockLogger, times(1)).info("User Login");

    // Verify MDC is cleared after logging
    assertNull(MDC.get("user"));
    assertNull(MDC.get("action"));
  }

  @Test
  void testLogEvent_noDetails() {
    // Execute the test
    AuditLogger.logEvent("System Startup");

    // Verify logger interactions
    verify(mockLogger, times(1)).info("System Startup");

    // Verify MDC is empty
    assertNull(MDC.getCopyOfContextMap());
  }
}
