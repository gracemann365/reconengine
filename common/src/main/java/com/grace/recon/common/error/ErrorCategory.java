package com.grace.recon.common.error;

/**
 * Enumeration for categorizing different types of errors within the Reconciliation Engine. This
 * helps in standardizing error handling, reporting, and routing decisions.
 */
public enum ErrorCategory {
  /**
   * Errors that are temporary and might resolve themselves upon retry. Examples: network issues,
   * temporary service unavailability.
   */
  TRANSIENT,

  /**
   * Errors that are permanent and will not resolve upon retry. Examples: invalid input data,
   * missing configuration, business rule violations.
   */
  PERMANENT,

  /**
   * Errors related to business logic or data integrity. These often require manual intervention or
   * specific business process handling.
   */
  BUSINESS_LOGIC,

  /** Errors related to security concerns, such as authentication or authorization failures. */
  SECURITY,

  /** Unknown or uncategorized errors. */
  UNKNOWN
}
