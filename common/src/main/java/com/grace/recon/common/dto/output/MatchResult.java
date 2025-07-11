package com.grace.recon.common.dto.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.grace.recon.common.dto.QuantPair;
import java.util.Map;

/** Represents the outcome of a successful matching attempt. Sent to the Matching_Output_Topic. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchResult {

  /** Defines the type of match. Tightly coupled to MatchResult, hence it's a nested enum. */
  public enum MatchType {
    EXACT,
    FUZZY
  }

  private QuantPair quantPair;
  private MatchType matchType;
  private double confidenceScore;
  private Map<String, String> fuzzyMatchDetails;

  // No-arg constructor for Jackson deserialization
  public MatchResult() {}

  // All-args constructor for programmatic creation
  public MatchResult(
      QuantPair quantPair,
      MatchType matchType,
      double confidenceScore,
      Map<String, String> fuzzyMatchDetails) {
    this.quantPair = quantPair;
    this.matchType = matchType;
    this.confidenceScore = confidenceScore;
    this.fuzzyMatchDetails = fuzzyMatchDetails;
  }

  // Standard Getters and Setters...
  public QuantPair getQuantPair() {
    return quantPair;
  }

  public void setQuantPair(QuantPair quantPair) {
    this.quantPair = quantPair;
  }

  public MatchType getMatchType() {
    return matchType;
  }

  public void setMatchType(MatchType matchType) {
    this.matchType = matchType;
  }

  public double getConfidenceScore() {
    return confidenceScore;
  }

  public void setConfidenceScore(double confidenceScore) {
    this.confidenceScore = confidenceScore;
  }

  public Map<String, String> getFuzzyMatchDetails() {
    return fuzzyMatchDetails;
  }

  public void setFuzzyMatchDetails(Map<String, String> fuzzyMatchDetails) {
    this.fuzzyMatchDetails = fuzzyMatchDetails;
  }
}
