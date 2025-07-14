package com.grace.recon.common.monitoring;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * A wrapper around SLF4J that facilitates structured logging. This class allows logging messages
 * with associated key-value pairs, which can be easily parsed by log aggregation systems (e.g., ELK
 * stack).
 */
public class StructuredLogger {

  private final Logger logger;

  private StructuredLogger(Logger logger) {
    this.logger = logger;
  }

  /**
   * Returns a StructuredLogger instance for the given class.
   *
   * @param clazz The class for which to get the logger.
   * @return A StructuredLogger instance.
   */
  public static StructuredLogger getLogger(Class<?> clazz) {
    return new StructuredLogger(LoggerFactory.getLogger(clazz));
  }

  /**
   * Logs an INFO level message with structured data.
   *
   * @param message The log message.
   * @param data A map of key-value pairs to include in the structured log.
   */
  public void info(String message, Map<String, String> data) {
    try {
      data.forEach(MDC::put);
      logger.info(message);
    } finally {
      data.keySet().forEach(MDC::remove);
    }
  }

  /**
   * Logs a WARN level message with structured data.
   *
   * @param message The log message.
   * @param data A map of key-value pairs to include in the structured log.
   */
  public void warn(String message, Map<String, String> data) {
    try {
      data.forEach(MDC::put);
      logger.warn(message);
    } finally {
      data.keySet().forEach(MDC::remove);
    }
  }

  /**
   * Logs an ERROR level message with structured data.
   *
   * @param message The log message.
   * @param data A map of key-value pairs to include in the structured log.
   */
  public void error(String message, Map<String, String> data) {
    try {
      data.forEach(MDC::put);
      logger.error(message);
    } finally {
      data.keySet().forEach(MDC::remove);
    }
  }

  /**
   * Logs a DEBUG level message with structured data.
   *
   * @param message The log message.
   * @param data A map of key-value pairs to include in the structured log.
   */
  public void debug(String message, Map<String, String> data) {
    try {
      data.forEach(MDC::put);
      logger.debug(message);
    } finally {
      data.keySet().forEach(MDC::remove);
    }
  }

  /**
   * Logs a TRACE level message with structured data.
   *
   * @param message The log message.
   * @param data A map of key-value pairs to include in the structured log.
   */
  public void trace(String message, Map<String, String> data) {
    try {
      data.forEach(MDC::put);
      logger.trace(message);
    } finally {
      data.keySet().forEach(MDC::remove);
    }
  }

  // Standard logging methods without structured data

  public void info(String message) {
    logger.info(message);
  }

  public void warn(String message) {
    logger.warn(message);
  }

  public void error(String message) {
    logger.error(message);
  }

  public void debug(String message) {
    logger.debug(message);
  }

  public void trace(String message) {
    logger.trace(message);
  }

  public void error(String message, Throwable t) {
    logger.error(message, t);
  }
}
