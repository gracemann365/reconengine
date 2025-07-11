package com.grace.recon.common.validation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Encapsulates the outcome of a single validation rule execution. This class provides a structured
 * way to report whether a rule passed or failed, along with any associated error messages and the
 * name of the rule.
 */
public class RuleResult {
  private final String ruleName;
  private final boolean passed;
  private final List<String> errorMessages;

  private RuleResult(String ruleName, boolean passed, List<String> errorMessages) {
    this.ruleName = Objects.requireNonNull(ruleName, "Rule name cannot be null");
    this.passed = passed;
    this.errorMessages =
        errorMessages != null
            ? Collections.unmodifiableList(errorMessages)
            : Collections.emptyList();
  }

  /**
   * Creates a successful rule result.
   *
   * @param ruleName The name of the rule that passed.
   * @return A RuleResult indicating success.
   */
  public static RuleResult passed(String ruleName) {
    return new RuleResult(ruleName, true, null);
  }

  /**
   * Creates a failed rule result with a single error message.
   *
   * @param ruleName The name of the rule that failed.
   * @param errorMessage The error message.
   * @return A RuleResult indicating failure.
   */
  public static RuleResult failed(String ruleName, List<String> errorMessages) {
    Objects.requireNonNull(errorMessages, "Error messages list cannot be null");
    return new RuleResult(ruleName, false, errorMessages);
  }

  public String getRuleName() {
    return ruleName;
  }

  public boolean isPassed() {
    return passed;
  }

  public boolean isFailed() {
    return !passed;
  }

  public List<String> getErrorMessages() {
    return errorMessages;
  }

  @Override
  public String toString() {
    return "RuleResult{"
        + "ruleName='"
        + ruleName
        + '\''
        + ", passed="
        + passed
        + ", errorMessages="
        + errorMessages
        + '}';
  }
}
