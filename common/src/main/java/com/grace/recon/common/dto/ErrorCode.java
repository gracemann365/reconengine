package com.grace.recon.common.dto;

/**
 * An enumeration of standardized error codes across the entire system.
 * These codes provide a consistent way to categorize and identify
 * different types of errors that can occur during reconciliation processing.
 */
public enum ErrorCode {
    // General Errors
    UNKNOWN_ERROR("GEN-001", "An unknown error occurred."),
    INTERNAL_SYSTEM_ERROR("GEN-002", "An internal system error prevented processing."),

    // Validation Errors
    VALIDATION_FAILED("VAL-001", "Data validation failed."),
    INVALID_FIELD_VALUE("VAL-002", "A field contains an invalid value."),
    MISSING_REQUIRED_FIELD("VAL-003", "A required field is missing."),

    // Matching Errors
    NO_EXACT_MATCH("MAT-001", "No exact match found for the transaction."),
    FUZZY_TOLERANCE_EXCEEDED("MAT-002", "Fuzzy match tolerance exceeded."),
    MISSING_COUNTERPARTY("MAT-003", "Counterparty transaction not found."),
    RULE_ENGINE_FAILURE("MAT-004", "Matching rule engine encountered an error."),

    // Data Format/Parsing Errors
    MALFORMED_DATA("DAT-001", "Data is malformed or unparseable."),
    SCHEMA_MISMATCH("DAT-002", "Data schema does not match expected schema."),

    // External System Integration Errors
    EXTERNAL_SERVICE_UNAVAILABLE("EXT-001", "External service is unavailable."),
    EXTERNAL_SERVICE_TIMEOUT("EXT-002", "External service call timed out."),
    EXTERNAL_SERVICE_ERROR("EXT-003", "External service returned an error."),

    // Business Logic Errors
    DUPLICATE_TRANSACTION("BUS-001", "Duplicate transaction detected."),
    UNSUPPORTED_OPERATION("BUS-002", "Operation is not supported."),

    // PCI Compliance Errors
    PCI_MASKING_FAILED("PCI-001", "Sensitive data masking failed."),
    PCI_AUDIT_FAILED("PCI-002", "PCI audit event generation failed."),

    // Resilience Errors
    CIRCUIT_BREAKER_OPEN("RES-001", "Circuit breaker is open, preventing calls."),
    RATE_LIMIT_EXCEEDED("RES-002", "Rate limit for external calls exceeded."),

    // Other/Miscellaneous
    OTHER_ISSUE("OTH-001", "An unspecified issue occurred.");

    private final String code;
    private final String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Looks up an ErrorCode by its string code.
     * @param code The string code of the error (e.g., "VAL-001").
     * @return The corresponding ErrorCode, or null if not found.
     */
    public static ErrorCode fromCode(String code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return null;
    }
}
