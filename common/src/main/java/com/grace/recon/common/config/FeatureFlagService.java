package com.grace.recon.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FeatureFlagService {

  @Value("${features.new-matching-algorithm.enabled:false}")
  private boolean newMatchingAlgorithmEnabled;

  @Value("${features.detailed-audit-logging.enabled:false}")
  private boolean detailedAuditLoggingEnabled;

  public boolean isNewMatchingAlgorithmEnabled() {
    return newMatchingAlgorithmEnabled;
  }

  public boolean isDetailedAuditLoggingEnabled() {
    return detailedAuditLoggingEnabled;
  }
}
