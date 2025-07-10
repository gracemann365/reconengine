package com.grace.recon.common.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.grace.recon.common.dto.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core component that interprets and executes dynamic validation rules defined in YAML.
 * This engine loads rules from a YAML file and provides a mechanism to validate
 * data against these rules.
 */
public class YamlRuleEngine {

    private static final Logger log = LoggerFactory.getLogger(YamlRuleEngine.class);
    private final ObjectMapper yamlMapper;
    private final Map<String, Map<String, Object>> rulesCache;

    public YamlRuleEngine() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.rulesCache = new ConcurrentHashMap<>();
    }

    /**
     * Loads validation rules from a YAML input stream.
     * @param inputStream The InputStream containing the YAML rule definitions.
     * @throws IOException If an I/O error occurs during reading or parsing the YAML.
     */
    public void loadRules(InputStream inputStream) throws IOException {
        try {
            TypeReference<Map<String, Map<String, Object>>> typeRef = new TypeReference<>() {};
            Map<String, Map<String, Object>> loadedRules = yamlMapper.readValue(inputStream, typeRef);
            rulesCache.clear(); // Clear existing rules before loading new ones
            rulesCache.putAll(loadedRules);
            log.info("Successfully loaded {} validation rule sets.", rulesCache.size());
        } catch (IOException e) {
            log.error("Failed to load validation rules from input stream: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validates a target object against a specific rule set.
     * @param ruleSetName The name of the rule set to apply.
     * @param target The object to validate.
     * @param <T> The type of the object to validate.
     * @return A list of RuleResult indicating the outcome of each rule within the set.
     * @throws ValidationException If the rule set is not found or a critical error occurs during validation.
     */
    public <T> List<RuleResult> validate(String ruleSetName, T target) throws ValidationException {
        if (!rulesCache.containsKey(ruleSetName)) {
            throw new ValidationException("Rule set '" + ruleSetName + "' not found.", Collections.singletonList(ErrorCode.VALIDATION_FAILED.name()));
        }

        List<RuleResult> results = new ArrayList<>();
        Map<String, Object> ruleSet = rulesCache.get(ruleSetName);

        for (Map.Entry<String, Object> entry : ruleSet.entrySet()) {
            String ruleId = entry.getKey();
            Map<String, Object> ruleDefinition = (Map<String, Object>) entry.getValue();

            String field = (String) ruleDefinition.get("field");
            String expectedValue = (String) ruleDefinition.get("expectedValue");
            String type = (String) ruleDefinition.get("type"); // e.g., "presence", "value"

            boolean passed = true;
            String errorMessage = null;

            if ("presence".equals(type)) {
                // Basic check: does the target object have this field?
                // This is a simplified check and would need reflection for real implementation
                if (target instanceof Map) {
                    passed = ((Map<?, ?>) target).containsKey(field);
                    if (!passed) {
                        errorMessage = "Field '" + field + "' is missing.";
                    }
                } else {
                    // For non-Map objects, we'd need reflection or a more sophisticated approach
                    log.warn("Presence validation for non-Map object type is not fully implemented for rule '{}'.", ruleId);
                    passed = true; // Assume passed for now
                }
            } else if ("value".equals(type)) {
                if (target instanceof Map) {
                    Object actualValue = ((Map<?, ?>) target).get(field);
                    passed = (actualValue != null && actualValue.toString().equals(expectedValue));
                    if (!passed) {
                        errorMessage = "Field '" + field + "' has value '" + actualValue + "', expected '" + expectedValue + "'.";
                    }
                } else {
                    log.warn("Value validation for non-Map object type is not fully implemented for rule '{}'.", ruleId);
                    passed = true; // Assume passed for now
                }
            } else {
                log.warn("Unsupported rule type '{}' for rule '{}'. Skipping.", type, ruleId);
                passed = true; // Skip unsupported rules
            }

            if (passed) {
                results.add(RuleResult.passed(ruleId));
            } else {
                List<String> errors = Collections.singletonList(errorMessage);
                results.add(RuleResult.failed(ruleId, errors));
            }
        }
        return results;
    }
}