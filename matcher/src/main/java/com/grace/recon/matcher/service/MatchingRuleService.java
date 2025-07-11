package com.grace.recon.matcher.service;

import java.util.Arrays;
import java.util.List;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MatchingRuleService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MatchingRuleService.class);

  private List<MatchingRule> exactMatchRules;
  private List<MatchingRule> fuzzyMatchRules;

  @PostConstruct
  public void loadRules() {
    LOGGER.info("Loading matching rules...");
    // Dummy exact match rules
    exactMatchRules =
        Arrays.asList(
            new MatchingRule("transactionId", "EXACT", null, null, 0.0),
            new MatchingRule("amount", "EXACT", null, null, 0.0),
            new MatchingRule("currency", "EXACT", null, null, 0.0));

    // Dummy fuzzy match rules
    fuzzyMatchRules =
        Arrays.asList(
            new MatchingRule("description", "LEVENSHTEIN", "2", "<=", 0.0),
            new MatchingRule("amount", "VARIANCE_USD", "0.05", "<=", 0.0),
            new MatchingRule("amount", "PERCENTAGE_VARIANCE", "0.01", "<=", 0.0));
    LOGGER.info("Matching rules loaded successfully.");
  }

  public List<MatchingRule> getExactMatchRules() {
    return exactMatchRules;
  }

  public List<MatchingRule> getFuzzyMatchRules() {
    return fuzzyMatchRules;
  }

  // Placeholder for rule refresh via Actuator (future implementation)
  public void refreshRules() {
    LOGGER.info("Refreshing matching rules...");
    loadRules(); // Reload rules
    LOGGER.info("Matching rules refreshed.");
  }
}
