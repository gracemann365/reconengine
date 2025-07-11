package com.grace.recon.common.pci;

import java.time.YearMonth;

/**
 * Utility class for validating credit card expiry dates. Ensures that the provided expiry month and
 * year are valid and not in the past.
 */
public class ExpiryValidator {

  /**
   * Validates if a given expiry month and year are valid and not in the past.
   *
   * @param expiryMonth The expiry month (1-12).
   * @param expiryYear The expiry year (e.g., 2025).
   * @return true if the expiry date is valid and in the future or current month, false otherwise.
   */
  public static boolean isValid(int expiryMonth, int expiryYear) {
    if (expiryMonth < 1 || expiryMonth > 12) {
      return false;
    }

    YearMonth expiryDate = YearMonth.of(expiryYear, expiryMonth);
    YearMonth currentDate = YearMonth.now();

    return !expiryDate.isBefore(currentDate);
  }
}
