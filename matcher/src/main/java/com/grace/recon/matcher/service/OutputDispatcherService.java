package com.grace.recon.matcher.service;

import com.grace.recon.common.dto.output.MatchResult;
import com.grace.recon.common.dto.output.ReconMeta;
import com.grace.recon.common.dto.output.UreQuant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OutputDispatcherService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OutputDispatcherService.class);

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Value("${spring.kafka.topic.matching-output:Matching_Output_Topic}")
  private String matchingOutputTopic;

  @Value("${spring.kafka.topic.escalation-output:Escalation_Topic}")
  private String escalationOutputTopic;

  @Value("${spring.kafka.topic.recon-meta-output:Recon_Metadata_Topic}")
  private String reconMetaOutputTopic;

  @Autowired
  public OutputDispatcherService(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void dispatchMatchResult(MatchResult matchResult) {
    LOGGER.info(
        "Dispatching MatchResult to {}: {}",
        matchingOutputTopic,
        matchResult.getQuantPair().getHusband().getTransactionId());
    kafkaTemplate.send(
        matchingOutputTopic,
        matchResult.getQuantPair().getHusband().getTransactionId(),
        matchResult);
  }

  public void dispatchUreQuant(UreQuant ureQuant) {
    LOGGER.info(
        "Dispatching UreQuant to {}: {}",
        escalationOutputTopic,
        ureQuant.getQuantPair().getHusband().getTransactionId());
    kafkaTemplate.send(
        escalationOutputTopic, ureQuant.getQuantPair().getHusband().getTransactionId(), ureQuant);
  }

  public void dispatchReconMeta(ReconMeta reconMeta) {
    LOGGER.info("Dispatching ReconMeta to {}: {}", reconMetaOutputTopic, reconMeta.getBatchId());
    kafkaTemplate.send(reconMetaOutputTopic, reconMeta.getBatchId(), reconMeta);
  }
}
