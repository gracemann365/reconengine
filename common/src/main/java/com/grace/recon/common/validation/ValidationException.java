package com.grace.recon.common.validation;

import com.grace.recon.common.error.ReconciliationException;

import java.util.Collections;
import java.util.List;

/**
 * Custom exception specifically for validation failures.
 * This exception is thrown when data fails to meet defined validation rules.
 * It extends {@link ReconciliationException} and can carry a list of specific
 * error messages from the validation process.
 */
public class ValidationException extends ReconciliationException {

    private final List<String> validationErrors;

    /**
     * Constructs a new validation exception with the specified detail message
     * and a list of specific validation errors.
     * @param message the detail message.
     * @param validationErrors a list of strings detailing the specific validation failures.
     */
    public ValidationException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors != null ? Collections.unmodifiableList(validationErrors) : Collections.emptyList();
    }

    /**
     * Constructs a new validation exception with the specified detail message.
     * @param message the detail message.
     */
    public ValidationException(String message) {
        this(message, Collections.emptyList());
    }

    /**
     * Constructs a new validation exception with the specified detail message and cause.
     * @param message the detail message.
     * @param cause the cause.
     * @param validationErrors a list of strings detailing the specific validation failures.
     */
    public ValidationException(String message, Throwable cause, List<String> validationErrors) {
        super(message, cause);
        this.validationErrors = validationErrors != null ? Collections.unmodifiableList(validationErrors) : Collections.emptyList();
    }

    /**
     * Constructs a new validation exception with the specified cause.
     * @param cause the cause.
     */
    public ValidationException(Throwable cause) {
        super(cause);
        this.validationErrors = Collections.emptyList();
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    @Override
    public String getMessage() {
        if (validationErrors.isEmpty()) {
            return super.getMessage();
        } else {
            return super.getMessage() + " Validation errors: " + String.join("; ", validationErrors);
        }
    }
}
