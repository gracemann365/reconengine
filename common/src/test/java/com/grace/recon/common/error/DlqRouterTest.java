package com.grace.recon.common.error;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.lang.reflect.Field;

class DlqRouterTest {

  private Logger originalLogger;

  private void setLogger(Logger logger) throws NoSuchFieldException, IllegalAccessException {
    Field logField = DlqRouter.class.getDeclaredField("log");
    logField.setAccessible(true);
    originalLogger = (Logger) logField.get(null);
    logField.set(null, logger);
  }

  private void restoreLogger() throws NoSuchFieldException, IllegalAccessException {
    Field logField = DlqRouter.class.getDeclaredField("log");
    logField.setAccessible(true);
    logField.set(null, originalLogger);
  }

  @Test
  void testRouteToDlq_fullArguments() throws NoSuchFieldException, IllegalAccessException {
    String topic = "test-topic";
    String key = "test-key";
    String value = "test-value";
    String errorMessage = "Simulated error";

    Logger mockLogger = Mockito.mock(Logger.class);
    setLogger(mockLogger);

    DlqRouter.routeToDlq(topic, key, value, errorMessage);

    verify(mockLogger, times(1))
          .error(
              eq("Routing message to DLQ. Original Topic: {}, Key: {}, Error: {}. Message Content: {}"),
              eq(topic),
              eq(key),
              eq(errorMessage),
              eq(value));

    restoreLogger();
  }

  @Test
  void testRouteToDlq_defaultErrorMessage() throws NoSuchFieldException, IllegalAccessException {
    String topic = "another-topic";
    String key = "another-key";
    String value = "another-value";

    Logger mockLogger = Mockito.mock(Logger.class);
    setLogger(mockLogger);

    DlqRouter.routeToDlq(topic, key, value);

    verify(mockLogger, times(1))
          .error(
              eq("Routing message to DLQ. Original Topic: {}, Key: {}, Error: {}. Message Content: {}"),
              eq(topic),
              eq(key),
              eq("Processing failed."),
              eq(value));

    restoreLogger();
  }
}
