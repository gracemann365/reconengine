package com.grace.recon.common.monitoring;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * A dedicated logger for critical audit events. This logger ensures that audit events are clearly
 * distinguishable from general application logs and can be routed to specific destinations for
 * security monitoring and compliance.
 */
public class AuditLogger {

  private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT");

  /**
   * Logs an audit event with a message and optional key-value pairs. The key-value pairs are added
   * to the MDC for structured logging.
   *
   * @param eventMessage The main message for the audit event.
   * @param properties Optional map of key-value pairs to include in the audit log.
   */
  public static void logEvent(String eventMessage, Map<String, String> properties) {
    try {
      if (properties != null) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
          MDC.put(entry.getKey(), entry.getValue());
        }
      }
      AUDIT_LOGGER.info(eventMessage);
    } finally {
      // Always clear MDC to prevent data leakage to subsequent log events
      if (properties != null) {
        for (String key : properties.keySet()) {
          MDC.remove(key);
        }
      }
    }
  }

  /**
   * Logs an audit event with only a message.
   *
   * @param eventMessage The main message for the audit event.
   */
  public static void logEvent(String eventMessage) {
    logEvent(eventMessage, null);
  }
}
