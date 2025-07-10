package com.grace.recon.common.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YamlRuleEngineTest {

    private YamlRuleEngine ruleEngine;

    private static final String TEST_RULES_YAML = """
quant-validation-rules:
  rule-1:
    field: transactionId
    type: presence
  rule-2:
    field: amount
    type: presence
  rule-3:
    field: currency
    type: value
    expectedValue: USD
  rule-4:
    field: sourceSystem
    type: presence

ure-validation-rules:
  rule-a:
    field: ureId
    type: presence
  rule-b:
    field: transactionQuant
    type: presence
  rule-c:
    field: reasonCode
    type: presence
""";

    @BeforeEach
    void setUp() throws IOException {
        ruleEngine = new YamlRuleEngine();
        try (InputStream is = new ByteArrayInputStream(TEST_RULES_YAML.getBytes())) {
            ruleEngine.loadRules(is);
        }
    }

    @Test
    void testLoadRulesSuccess() {
        // Verify that the rulesCache is populated after loading rules
        assertNotNull(ruleEngine);
        // Assuming there are 2 rule sets in TEST_RULES_YAML
        // We can't directly access rulesCache, so we'll rely on validate method behavior
        // or add a public method to YamlRuleEngine to get rule set names for testing.
        // For now, we'll just ensure no exception is thrown when trying to validate existing rule sets.
        // A more robust test would involve validating a dummy object against the loaded rules.

        // Test that known rule sets exist by attempting to validate against them (expecting no ValidationException for rule set not found)
        assertDoesNotThrow(() -> ruleEngine.validate("quant-validation-rules", new HashMap<>()));
        assertDoesNotThrow(() -> ruleEngine.validate("ure-validation-rules", new HashMap<>()));

        // Test that a non-existent rule set throws ValidationException
        ValidationException thrown = assertThrows(ValidationException.class, () -> ruleEngine.validate("non-existent-rules", new HashMap<>()));
        assertTrue(thrown.getMessage().contains("Rule set 'non-existent-rules' not found."));
    }
}
