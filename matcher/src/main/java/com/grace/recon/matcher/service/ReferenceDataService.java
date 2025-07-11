package com.grace.recon.matcher.service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ReferenceDataService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceDataService.class);

  private Map<String, String> referenceDataCache;

  @PostConstruct
  public void loadReferenceData() {
    LOGGER.info("Loading reference data...");
    // Dummy reference data
    Map<String, String> data = new ConcurrentHashMap<>();
    data.put("merchantId_1", "Merchant A");
    data.put("merchantId_2", "Merchant B");
    referenceDataCache = Collections.unmodifiableMap(data);
    LOGGER.info("Reference data loaded successfully.");
  }

  public Map<String, String> getReferenceData() {
    return referenceDataCache;
  }

  // Placeholder for scheduled refresh (future implementation)
  public void refreshReferenceData() {
    LOGGER.info("Refreshing reference data...");
    loadReferenceData(); // Reload data
    LOGGER.info("Reference data refreshed.");
  }
}
