package com.grace.recon.common.error;

/**
 * Exception thrown when data is found to be in an unexpected or malformed format.
 * This indicates an issue with the structure or content of the data that prevents
 * it from being processed correctly.
 */
public class DataFormatException extends ReconciliationException {

    public DataFormatException() {
        super();
    }

    public DataFormatException(String message) {
        super(message);
    }

    public DataFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataFormatException(Throwable cause) {
        super(cause);
    }

    protected DataFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}