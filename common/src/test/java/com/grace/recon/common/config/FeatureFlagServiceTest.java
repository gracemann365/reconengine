package com.grace.recon.common.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {FeatureFlagService.class, AppConfig.class})
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      "features.new-matching-algorithm.enabled=true",
      "features.detailed-audit-logging.enabled=false"
    })
class FeatureFlagServiceTest {

  @Autowired private FeatureFlagService featureFlagService;

  @Test
  void testNewMatchingAlgorithmEnabled() {
    assertTrue(featureFlagService.isNewMatchingAlgorithmEnabled());
  }

  @Test
  void testDetailedAuditLoggingEnabled() {
    assertFalse(featureFlagService.isDetailedAuditLoggingEnabled());
  }
}
