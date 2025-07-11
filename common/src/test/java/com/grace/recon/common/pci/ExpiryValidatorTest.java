package com.grace.recon.common.pci;

import static org.junit.jupiter.api.Assertions.*;

import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class ExpiryValidatorTest {

  @Test
  void testIsValid_futureDate() {
    // Test with a future date
    int futureMonth = YearMonth.now().getMonthValue();
    int futureYear = YearMonth.now().getYear() + 1;
    assertTrue(ExpiryValidator.isValid(futureMonth, futureYear));
  }

  @Test
  void testIsValid_currentMonthAndYear() {
    // Test with current month and year
    int currentMonth = YearMonth.now().getMonthValue();
    int currentYear = YearMonth.now().getYear();
    assertTrue(ExpiryValidator.isValid(currentMonth, currentYear));
  }

  @Test
  void testIsValid_pastMonthCurrentYear() {
    // Test with a past month in the current year
    int pastMonth = YearMonth.now().getMonthValue() - 1;
    if (pastMonth == 0) pastMonth = 12; // Handle January
    int currentYear = YearMonth.now().getYear();
    assertFalse(ExpiryValidator.isValid(pastMonth, currentYear));
  }

  @Test
  void testIsValid_pastYear() {
    // Test with a past year
    int currentMonth = YearMonth.now().getMonthValue();
    int pastYear = YearMonth.now().getYear() - 1;
    assertFalse(ExpiryValidator.isValid(currentMonth, pastYear));
  }

  @Test
  void testIsValid_invalidMonth() {
    assertFalse(ExpiryValidator.isValid(0, 2025));
    assertFalse(ExpiryValidator.isValid(13, 2025));
  }

  @Test
  void testIsValid_invalidYear() {
    // Assuming a reasonable range for years, e.g., not negative
    assertFalse(ExpiryValidator.isValid(1, -2025));
  }
}
