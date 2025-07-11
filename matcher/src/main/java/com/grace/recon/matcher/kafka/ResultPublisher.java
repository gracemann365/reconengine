package com.grace.recon.matcher.kafka;

import com.grace.recon.common.dto.output.MatchResult;
import com.grace.recon.common.dto.output.ReconMeta;
import com.grace.recon.common.dto.output.UreQuant;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class ResultPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResultPublisher.class);

  @Value("${spring.kafka.topic.matching-output}")
  private String matchingOutputTopic;

  @Value("${spring.kafka.topic.escalation}")
  private String escalationTopic;

  @Value("${spring.kafka.topic.recon-metadata}")
  private String reconMetadataTopic;

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Autowired
  public ResultPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publishMatchResult(MatchResult result) {
    sendMessage(matchingOutputTopic, result.getQuantPair().getHusband().getTransactionId(), result);
  }

  public void publishUreQuant(UreQuant ure) {
    sendMessage(escalationTopic, ure.getQuantPair().getHusband().getTransactionId(), ure);
  }

  public void publishReconMeta(ReconMeta meta) {
    sendMessage(reconMetadataTopic, meta.getBatchId(), meta);
  }

  /**
   * Sends a message asynchronously. The calling thread is NOT blocked. Success or failure is
   * handled in a separate callback thread.
   */
  private void sendMessage(String topic, String key, Object payload) {
    CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, payload);
    future.whenComplete(
        (result, ex) -> {
          if (ex == null) {
            // This block executes on success
            LOGGER.debug("Successfully sent message to topic {}: key={}", topic, key);
          } else {
            // This block executes on failure
            LOGGER.error("Failed to send message to topic {}: key={}", topic, key, ex);
          }
        });
  }
}
