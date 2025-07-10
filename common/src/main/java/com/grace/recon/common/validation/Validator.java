package com.grace.recon.common.validation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Generic interface for defining custom data validation logic.
 * Implementations of this interface will provide specific validation rules
 * for different types of objects.
 * @param <T> The type of object to be validated.
 */
public interface Validator<T> {

    /**
     * Validates the given target object.
     * @param target The object to validate.
     * @return A {@link ValidationResult} indicating whether the validation passed or failed,
     *         along with any associated error messages.
     */
    ValidationResult validate(T target);

    /**
     * Represents the result of a validation operation.
     */
    class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        private ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
        }

        /**
         * Creates a successful validation result.
         * @return A ValidationResult indicating success.
         */
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        /**
         * Creates a failed validation result with a single error message.
         * @param error The error message.
         * @return A ValidationResult indicating failure.
         */
        public static ValidationResult failure(String error) {
            Objects.requireNonNull(error, "Error message cannot be null");
            return new ValidationResult(false, Collections.singletonList(error));
        }

        /**
         * Creates a failed validation result with multiple error messages.
         * @param errors A list of error messages.
         * @return A ValidationResult indicating failure.
         */
        public static ValidationResult failure(List<String> errors) {
            Objects.requireNonNull(errors, "Error list cannot be null");
            return new ValidationResult(false, errors);
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public boolean hasErrors() {
            return !valid;
        }

        @Override
        public String toString() {
            return "ValidationResult{" +
                   "valid=" + valid +
                   ", errors=" + errors +
                   '}' + (valid ? " (Success)" : " (Failure)");
        }
    }
}
