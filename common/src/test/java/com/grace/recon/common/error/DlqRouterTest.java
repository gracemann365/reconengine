package com.grace.recon.common.error;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DlqRouterTest {

  @Test
  void testRouteToDlq_fullArguments() {
    String topic = "test-topic";
    String key = "test-key";
    String value = "test-value";
    String errorMessage = "Simulated error";

    try (MockedStatic<LoggerFactory> mockedLoggerFactory =
        Mockito.mockStatic(LoggerFactory.class)) {
      Logger mockLogger = Mockito.mock(Logger.class);
      mockedLoggerFactory
          .when(() -> LoggerFactory.getLogger(DlqRouter.class))
          .thenReturn(mockLogger);

      DlqRouter.routeToDlq(topic, key, value, errorMessage);

      verify(mockLogger, times(1))
          .error(
              eq(
                  "Routing message to DLQ. Original Topic: {}, Key: {}, Error: {}. Message Content: {}"),
              eq(topic),
              eq(key),
              eq(errorMessage),
              eq(value));
    }
  }

  @Test
  void testRouteToDlq_defaultErrorMessage() {
    String topic = "another-topic";
    String key = "another-key";
    String value = "another-value";

    try (MockedStatic<LoggerFactory> mockedLoggerFactory =
        Mockito.mockStatic(LoggerFactory.class)) {
      Logger mockLogger = Mockito.mock(Logger.class);
      mockedLoggerFactory
          .when(() -> LoggerFactory.getLogger(DlqRouter.class))
          .thenReturn(mockLogger);

      DlqRouter.routeToDlq(topic, key, value);

      verify(mockLogger, times(1))
          .error(
              eq(
                  "Routing message to DLQ. Original Topic: {}, Key: {}, Error: {}. Message Content: {}"),
              eq(topic),
              eq(key),
              eq("Processing failed."),
              eq(value));
    }
  }
}
