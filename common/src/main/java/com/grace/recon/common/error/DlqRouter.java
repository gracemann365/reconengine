package com.grace.recon.common.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements logic for routing failed messages to Dead Letter Queues (DLQs). In a real-world
 * scenario, this would involve publishing the failed message to a dedicated DLQ topic in Kafka or
 * another messaging system.
 */
public class DlqRouter {

  // Made non-final to allow mocking in tests
  private static Logger log = LoggerFactory.getLogger(DlqRouter.class);

  /**
   * Routes a failed message to the Dead Letter Queue. This method simulates sending the message to
   * a DLQ.
   *
   * @param topic The original topic from which the message was consumed.
   * @param messageKey The key of the failed message.
   * @param messageValue The value (content) of the failed message.
   * @param errorMessage A description of why the message failed processing.
   */
  public static void routeToDlq(
      String topic, String messageKey, String messageValue, String errorMessage) {
    log.error(
        "Routing message to DLQ. Original Topic: {}, Key: {}, Error: {}. Message Content: {}",
        topic,
        messageKey,
        errorMessage,
        messageValue);
    // In a real application, this would involve:
    // 1. Constructing a DLQ record (e.g., Avro schema for DLQ messages)
    // 2. Publishing to a Kafka DLQ topic (e.g., using KafkaTemplate)
    // 3. Potentially adding headers with error details, timestamp, etc.
  }

  /**
   * Routes a failed message to the Dead Letter Queue with a generic error message.
   *
   * @param topic The original topic from which the message was consumed.
   * @param messageKey The key of the failed message.
   * @param messageValue The value (content) of the failed message.
   */
  public static void routeToDlq(String topic, String messageKey, String messageValue) {
    routeToDlq(topic, messageKey, messageValue, "Processing failed.");
  }
}