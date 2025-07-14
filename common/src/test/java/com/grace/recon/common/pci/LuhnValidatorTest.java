package com.grace.recon.common.pci;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LuhnValidatorTest {

  @Test
  void testIsValid_validNumbers() {
    assertTrue(LuhnValidator.isValid("79927398713")); // Example valid Luhn number
    assertTrue(LuhnValidator.isValid("49927398716")); // Another valid credit card like number
    assertTrue(LuhnValidator.isValid("49927398717")); // Another valid credit card like number
  }

  @Test
  void testIsValid_invalidNumbers() {
    assertFalse(LuhnValidator.isValid("79927398714")); // Invalid Luhn number
    assertFalse(LuhnValidator.isValid("1234567890123456")); // Typically invalid
  }

  @Test
  void testIsValid_emptyString() {
    assertFalse(LuhnValidator.isValid(""));
  }

  @Test
  void testIsValid_nullString() {
    assertFalse(LuhnValidator.isValid(null));
  }

  @Test
  void testIsValid_nonDigitCharacters() {
    assertFalse(LuhnValidator.isValid("123A456"));
  }

  @Test
  void testIsValid_singleDigit() {
    assertFalse(LuhnValidator.isValid("7"));
  }
}
