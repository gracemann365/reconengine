package com.grace.recon.common.validation;

import static org.junit.jupiter.api.Assertions.*;

import com.grace.recon.common.validation.Validator.ValidationResult;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidatorTest {

  // Example concrete implementation for testing purposes
  private static class LengthValidator implements Validator<String> {
    private final int minLength;

    public LengthValidator(int minLength) {
      this.minLength = minLength;
    }

    @Override
    public ValidationResult validate(String target) {
      if (target == null || target.length() < minLength) {
        return ValidationResult.failure(
            "String must be at least " + minLength + " characters long.");
      } else {
        return ValidationResult.success();
      }
    }
  }

  @Test
  void testValidationResultSuccess() {
    ValidationResult result = ValidationResult.success();
    assertTrue(result.isValid());
    assertTrue(result.getErrors().isEmpty());
    assertEquals("ValidationResult{valid=true, errors=[]} (Success)", result.toString());
  }

  @Test
  void testValidationResultFailureSingleError() {
    ValidationResult result = ValidationResult.failure("Error message 1");
    assertFalse(result.isValid());
    assertEquals(1, result.getErrors().size());
    assertEquals("Error message 1", result.getErrors().get(0));
    assertEquals(
        "ValidationResult{valid=false, errors=[Error message 1]} (Failure)", result.toString());
  }

  @Test
  void testValidationResultFailureMultipleErrors() {
    List<String> errors = Arrays.asList("Error message 1", "Error message 2");
    ValidationResult result = ValidationResult.failure(errors);
    assertFalse(result.isValid());
    assertEquals(2, result.getErrors().size());
    assertTrue(result.getErrors().contains("Error message 1"));
    assertTrue(result.getErrors().contains("Error message 2"));
    assertEquals(
        "ValidationResult{valid=false, errors=[Error message 1, Error message 2]} (Failure)",
        result.toString());
  }

  @Test
  void testLengthValidatorSuccess() {
    LengthValidator validator = new LengthValidator(5);
    ValidationResult result = validator.validate("hello");
    assertTrue(result.isValid());
  }

  @Test
  void testLengthValidatorFailure() {
    LengthValidator validator = new LengthValidator(5);
    ValidationResult result = validator.validate("hi");
    assertFalse(result.isValid());
    assertEquals(1, result.getErrors().size());
    assertEquals("String must be at least 5 characters long.", result.getErrors().get(0));
  }
}
