package com.grace.recon.matcher.service;

import com.grace.recon.common.dto.FuzzyMatchedQuant;
import com.grace.recon.common.dto.MatchedQuant;
import com.grace.recon.common.dto.Quant;
import com.grace.recon.common.dto.QuantPair;
import com.grace.recon.common.dto.UreQuant;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MatchingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MatchingService.class);

  private final QuantBufferService quantBufferService;
  private final MatchingRuleService matchingRuleService;
  private final ReferenceDataService referenceDataService;
  private final ConcurrentLinkedQueue<MatchedQuant> matchedQuantsOutputQueue =
      new ConcurrentLinkedQueue<>();
  private final ConcurrentLinkedQueue<FuzzyMatchedQuant> fuzzyMatchedQuantsOutputQueue =
      new ConcurrentLinkedQueue<>();
  private final ConcurrentLinkedQueue<UreQuant> ureQuantsOutputQueue =
      new ConcurrentLinkedQueue<>();

  private final ExecutorService executorService;

  public MatchingService(
      QuantBufferService quantBufferService,
      MatchingRuleService matchingRuleService,
      ReferenceDataService referenceDataService) {
    this.quantBufferService = quantBufferService;
    this.matchingRuleService = matchingRuleService;
    this.referenceDataService = referenceDataService;
    // Initialize a fixed thread pool for concurrent processing
    this.executorService = Executors.newFixedThreadPool(32); // As per memoryRECON.json
    startProcessing();
  }

  private void startProcessing() {
    // Continuously poll from the buffer and submit to the executor
    new Thread(
            () -> {
              while (!Thread.currentThread().isInterrupted()) {
                try {
                  QuantPair quantPair = quantBufferService.takeFromBuffer();
                  executorService.submit(() -> processQuantPair(quantPair));
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  LOGGER.warn("MatchingService processing thread interrupted.");
                } catch (Exception e) {
                  LOGGER.error("Error taking from buffer or submitting task", e);
                }
              }
            },
            "MatchingService-PollingThread")
        .start();
  }

  private void processQuantPair(QuantPair quantPair) {
    // 1. Exact Match Module (Soulmate)
    if (attemptExactMatch(quantPair)) {
      return;
    }

    // 2. Fuzzy Match Module
    if (attemptFuzzyMatch(quantPair)) {
      return;
    }

    // 3. Unmatched Exception Router Module
    routeUreQuant(quantPair);
  }

  private boolean attemptExactMatch(QuantPair quantPair) {
    LOGGER.debug(
        "Attempting exact match for QuantPair: {}", quantPair.getHusband().getTransactionId());
    boolean allRulesPass = true;
    for (MatchingRule rule : matchingRuleService.getExactMatchRules()) {
      Object husbandValue = getFieldValue(quantPair.getHusband(), rule.getField());
      Object wifeValue = getFieldValue(quantPair.getWife(), rule.getField());

      if (husbandValue == null || wifeValue == null || !husbandValue.equals(wifeValue)) {
        allRulesPass = false;
        break;
      }
    }

    if (allRulesPass) {
      matchedQuantsOutputQueue.add(new MatchedQuant(quantPair, "EXACT_MATCH"));
      LOGGER.info("Exact match found for QuantPair: {}", quantPair.getHusband().getTransactionId());
      return true;
    }
    return false;
  }

  private Object getFieldValue(Quant quant, String fieldName) {
    switch (fieldName) {
      case "transactionId":
        return quant.getTransactionId();
      case "amount":
        return quant.getAmount();
      case "currency":
        return quant.getCurrency();
      case "description":
        return quant.getDescription();
        // Add other fields as needed for matching
      default:
        return null;
    }
  }

  private boolean attemptFuzzyMatch(QuantPair quantPair) {
    LOGGER.debug(
        "Attempting fuzzy match for QuantPair: {}", quantPair.getHusband().getTransactionId());
    // In a real scenario, this would iterate through matchingRuleService.getFuzzyMatchRules()
    // and apply the rules dynamically using MatchingUtil.
    // For demonstration, a simplified fuzzy match based on description Levenshtein distance and
    // amount variance.

    // Example: Fuzzy match on description with Levenshtein distance <= 2
    int levenshteinDistance =
        MatchingUtil.calculateLevenshteinDistance(
            quantPair.getHusband().getDescription(), quantPair.getWife().getDescription());
    boolean descriptionFuzzyMatch = levenshteinDistance <= 2; // Tolerance from rules

    // Example: Fuzzy match on amount with variance <= 0.05 USD
    double amountDifference =
        MatchingUtil.calculateAbsoluteDifference(
            quantPair.getHusband().getAmount(), quantPair.getWife().getAmount());
    boolean amountFuzzyMatch = amountDifference <= 0.05; // Tolerance from rules

    // Example: Fuzzy match on amount with percentage variance <= 0.01%
    double percentageVariance =
        MatchingUtil.calculatePercentageVariance(
            quantPair.getHusband().getAmount(), quantPair.getWife().getAmount());
    boolean percentageFuzzyMatch = percentageVariance <= 0.01; // Tolerance from rules

    if (descriptionFuzzyMatch && (amountFuzzyMatch || percentageFuzzyMatch)) {
      fuzzyMatchedQuantsOutputQueue.add(
          new FuzzyMatchedQuant(quantPair, "FUZZY_MATCH", 0.8)); // Dummy confidence score
      LOGGER.info("Fuzzy match found for QuantPair: {}", quantPair.getHusband().getTransactionId());
      return true;
    }
    return false;
  }

  private void routeUreQuant(QuantPair quantPair) {
    LOGGER.info("Routing URE Quant for QuantPair: {}", quantPair.getHusband().getTransactionId());
    // Placeholder for URE routing logic
    // Create a UreQuant object with reason codes and add to URE output queue
    ureQuantsOutputQueue.add(new UreQuant(quantPair, "UNRECONCILED", "NO_MATCH_FOUND", null));
  }

  public ConcurrentLinkedQueue<MatchedQuant> getMatchedQuantsOutputQueue() {
    return matchedQuantsOutputQueue;
  }

  public ConcurrentLinkedQueue<FuzzyMatchedQuant> getFuzzyMatchedQuantsOutputQueue() {
    return fuzzyMatchedQuantsOutputQueue;
  }

  public ConcurrentLinkedQueue<UreQuant> getUreQuantsOutputQueue() {
    return ureQuantsOutputQueue;
  }

  public void shutdown() {
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}