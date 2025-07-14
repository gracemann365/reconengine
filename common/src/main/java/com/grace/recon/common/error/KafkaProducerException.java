package com.grace.recon.common.error;

/**
 * Exception thrown when an error occurs during Kafka producer operations. This typically indicates
 * issues with sending messages to Kafka topics, such as network problems, serialization failures,
 * or Kafka broker issues.
 */
public class KafkaProducerException extends ReconciliationException {

  public KafkaProducerException() {
    super();
  }

  public KafkaProducerException(String message) {
    super(message);
  }

  public KafkaProducerException(String message, Throwable cause) {
    super(message, cause);
  }

  public KafkaProducerException(Throwable cause) {
    super(cause);
  }

  protected KafkaProducerException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
