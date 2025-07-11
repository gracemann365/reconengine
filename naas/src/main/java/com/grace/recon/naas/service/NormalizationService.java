package com.grace.recon.naas.service;

import com.grace.recon.common.dto.Quant;
import com.grace.recon.naas.dto.SwitchRecordDto;
import com.grace.recon.naas.dto.VisaRecordDto;
import com.grace.recon.naas.kafka.KafkaPublisherService;
import com.grace.recon.naas.queue.NaasDataQueue;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NormalizationService {

  private static final Logger LOGGER = Logger.getLogger(NormalizationService.class.getName());
  private static final String NORMALIZATION_DLQ_TOPIC = "naas-normalization-dlq";

  private final NaasDataQueue dataQueue;
  private final KafkaPublisherService kafkaPublisherService;
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  @Autowired
  public NormalizationService(
      NaasDataQueue dataQueue, KafkaPublisherService kafkaPublisherService) {
    this.dataQueue = dataQueue;
    this.kafkaPublisherService = kafkaPublisherService;
  }

  @PostConstruct
  private void init() {
    executorService.submit(this::processQueue);
    LOGGER.info("NormalizationService started and listening to the queue.");
  }

  private void processQueue() {
    while (!Thread.currentThread().isInterrupted()) {
      Object record = dataQueue.validationToNormalizationQueue.poll();
      if (record != null) {
        try {
          if (record instanceof SwitchRecordDto switchRecord) {
            Quant quant = normalizeSwitchRecord(switchRecord);
            kafkaPublisherService.sendQuant(quant);
          } else if (record instanceof VisaRecordDto visaRecord) {
            Quant quant = normalizeVisaRecord(visaRecord);
            kafkaPublisherService.sendQuant(quant);
          }
        } catch (Exception e) {
          LOGGER.severe("Failed to normalize record. Sending to DLQ. Error: " + e.getMessage());
          kafkaPublisherService.sendToDlq(NORMALIZATION_DLQ_TOPIC, record);
        }
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        LOGGER.warning("NormalizationService interrupted.");
      }
    }
  }

  private Quant normalizeSwitchRecord(SwitchRecordDto switchRecord) {
    Quant quant = new Quant();
    quant.setTransactionId(switchRecord.getTransaction_id());
    quant.setAmount(switchRecord.getSettlement_amount().doubleValue());
    quant.setSourceSystem("SWITCH");
    quant.setCurrency("USD"); // Assuming default USD for Switch records if not explicitly available
    quant.setTransactionDate(switchRecord.getCreated_at().toInstant(ZoneOffset.UTC).toEpochMilli());
    quant.setTransactionType(switchRecord.getTransaction_type());
    quant.setAuthorizationCode(switchRecord.getAuthorization_code());
    quant.setSourceReferenceId(switchRecord.getRrn()); // Using RRN as sourceReferenceId
    quant.setDescription(switchRecord.getMerchant_name()); // Mapping merchant_name to description
    // accountId and additionalMetadata are not directly available in SwitchRecordDto
    return quant;
  }

  private Quant normalizeVisaRecord(VisaRecordDto visaRecord) {
    Quant quant = new Quant();
    quant.setTransactionId(visaRecord.getTransactionId());
    quant.setAmount(visaRecord.getSettlementAmount().doubleValue());
    quant.setSourceSystem("VISA");
    quant.setCurrency(visaRecord.getCurrencyCode());
    // Combine LocalDate and LocalTime to LocalDateTime, then convert to epoch millis
    quant.setTransactionDate(
        visaRecord
            .getTransactionDate()
            .atTime(visaRecord.getTransactionTime())
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli());
    quant.setTransactionType(visaRecord.getTransactionType());
    quant.setAuthorizationCode(visaRecord.getAuthorizationCode());
    quant.setSourceReferenceId(visaRecord.getRrn()); // Using RRN as sourceReferenceId
    quant.setDescription(visaRecord.getMerchantId()); // Mapping merchantId to description
    // accountId and additionalMetadata are not directly available in VisaRecordDto
    return quant;
  }

  @PreDestroy
  private void shutdown() {
    executorService.shutdownNow();
    try {
      if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
        LOGGER.severe("NormalizationService did not terminate gracefully.");
      }
    } catch (InterruptedException e) {
      LOGGER.severe("Shutdown interrupted.");
    }
  }
}
