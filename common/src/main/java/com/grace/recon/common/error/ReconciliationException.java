package com.grace.recon.common.error;

/**
 * Base custom exception for all reconciliation-specific business errors. This exception serves as
 * the root of a hierarchy of custom exceptions that can be thrown within the Reconciliation Engine.
 * It extends {@link RuntimeException} to allow for unchecked exceptions in cases where recovery is
 * not expected or immediate.
 */
public class ReconciliationException extends RuntimeException {

  /**
   * Constructs a new reconciliation exception with {@code null} as its detail message. The cause is
   * not initialized, and may subsequently be initialized by a call to {@link
   * #initCause(Throwable)}.
   */
  public ReconciliationException() {
    super();
  }

  /**
   * Constructs a new reconciliation exception with the specified detail message. The cause is not
   * initialized, and may subsequently be initialized by a call to {@link #initCause(Throwable)}.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the
   *     {@link #getMessage()} method.
   */
  public ReconciliationException(String message) {
    super(message);
  }

  /**
   * Constructs a new reconciliation exception with the specified detail message and cause.
   *
   * <p>Note that the detail message associated with {@code cause} is <i>not</i> automatically
   * incorporated in this exception's detail message.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link
   *     #getMessage()} method).
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A {@code null} value is permitted, and indicates that the cause is nonexistent or
   *     unknown.)
   */
  public ReconciliationException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new reconciliation exception with the specified cause and a detail message of
   * {@code (cause==null ? null : cause.toString())} (which typically contains the class and detail
   * message of {@code cause}). This constructor is useful for exceptions that are little more than
   * wrappers for other throwables (for example, {@link java.security.PrivilegedActionException}).
   *
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A {@code null} value is permitted, and indicates that the cause is nonexistent or
   *     unknown.)
   */
  public ReconciliationException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new reconciliation exception with the specified detail message, cause, suppression
   * enabled or disabled, and writable stack trace enabled or disabled.
   *
   * @param message the detail message.
   * @param cause the cause. (A {@code null} value is permitted, and indicates that the cause is
   *     nonexistent or unknown.)
   * @param enableSuppression whether or not suppression is enabled or disabled
   * @param writableStackTrace whether or not the stack trace should be writable
   */
  protected ReconciliationException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
