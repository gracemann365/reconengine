package com.grace.recon.matcher.model;

import com.grace.recon.common.dto.Quant;
import lombok.Data;
import java.util.List;

@Data
public class MatchingRules {
    private ExactMatch exactMatch;
    private FuzzyMatch fuzzyMatch;

    public void validateFields() {
        if (exactMatch != null && exactMatch.getKeys() != null) {
            exactMatch.getKeys().forEach(this::validateFieldExists);
        }
        if (fuzzyMatch != null && fuzzyMatch.getTolerances() != null) {
            fuzzyMatch.getTolerances().forEach(t -> validateFieldExists(t.getField()));
        }
    }

    private void validateFieldExists(String fieldName) {
        try {
            Quant.class.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Invalid rule: Field '" + fieldName + "' not found in Quant.class.");
        }
    }

    @Data
    public static class ExactMatch {
        private List<String> keys;
    }

    @Data
    public static class FuzzyMatch {
        private List<Tolerance> tolerances;
    }

    @Data
    public static class Tolerance {
        private String field;
        private String algorithm;
        private double value;
    }
}