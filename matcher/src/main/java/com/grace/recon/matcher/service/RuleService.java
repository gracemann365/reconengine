package com.grace.recon.matcher.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.grace.recon.matcher.model.MatchingRules;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Service;

@Service
@Endpoint(id = "rules") // Exposes this as /actuator/rules
public class RuleService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RuleService.class);
  private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
  private MatchingRules rules;

  @PostConstruct
  public void loadAndValidateRules() {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream("rules.yml")) {
      if (is == null) {
        throw new IllegalStateException("Cannot find rules.yml on the classpath.");
      }
      this.rules = mapper.readValue(is, MatchingRules.class);
      this.rules.validateFields(); // Validate fields after loading
      LOGGER.info("Matching rules loaded and validated successfully.");
    } catch (Exception e) {
      LOGGER.error(
          "CRITICAL FAILURE: Could not load or validate matching rules. Shutting down.", e);
      // This ensures the application does not run in an indeterminate state.
      throw new RuntimeException("Failed to initialize matching rules", e);
    }
  }

  @WriteOperation // Allows reloading via POST /actuator/rules
  public synchronized void refreshRules() {
    LOGGER.info("Actuator request to refresh matching rules.");
    loadAndValidateRules();
  }

  public MatchingRules getRules() {
    return this.rules;
  }
}
