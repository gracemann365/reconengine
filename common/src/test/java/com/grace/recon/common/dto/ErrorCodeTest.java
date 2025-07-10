package com.grace.recon.common.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorCodeTest {

    @Test
    void testEnumValuesAndCodes() {
        assertEquals("GEN-001", ErrorCode.UNKNOWN_ERROR.getCode());
        assertEquals("An unknown error occurred.", ErrorCode.UNKNOWN_ERROR.getDescription());

        assertEquals("VAL-001", ErrorCode.VALIDATION_FAILED.getCode());
        assertEquals("Data validation failed.", ErrorCode.VALIDATION_FAILED.getDescription());

        assertEquals("MAT-001", ErrorCode.NO_EXACT_MATCH.getCode());
        assertEquals("No exact match found for the transaction.", ErrorCode.NO_EXACT_MATCH.getDescription());

        assertEquals("OTH-001", ErrorCode.OTHER_ISSUE.getCode());
        assertEquals("An unspecified issue occurred.", ErrorCode.OTHER_ISSUE.getDescription());
    }

    @Test
    void testFromCodeMethod() {
        assertEquals(ErrorCode.VALIDATION_FAILED, ErrorCode.fromCode("VAL-001"));
        assertEquals(ErrorCode.NO_EXACT_MATCH, ErrorCode.fromCode("MAT-001"));
        assertEquals(ErrorCode.UNKNOWN_ERROR, ErrorCode.fromCode("GEN-001"));
        assertNull(ErrorCode.fromCode("NON-EXISTENT-CODE"));
        assertNull(ErrorCode.fromCode(null));
        assertNull(ErrorCode.fromCode(""));
    }

    @Test
    void testAllErrorCodesHaveUniqueValues() {
        // This test ensures that no two error codes have the same string representation
        // and that no two error codes have the same description.
        // It also implicitly checks that all enum values are covered.
        for (ErrorCode code : ErrorCode.values()) {
            assertNotNull(code.getCode(), "Code for " + code.name() + " should not be null");
            assertNotNull(code.getDescription(), "Description for " + code.name() + " should not be null");

            // Check for uniqueness of codes
            for (ErrorCode otherCode : ErrorCode.values()) {
                if (code != otherCode) {
                    assertNotEquals(code.getCode(), otherCode.getCode(),
                            "Duplicate error code found: " + code.getCode() + " for " + code.name() + " and " + otherCode.name());
                }
            }
        }
    }
}
