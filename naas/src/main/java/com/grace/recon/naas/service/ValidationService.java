package com.grace.recon.naas.service;

import com.grace.recon.common.monitoring.MetricService;
import com.grace.recon.naas.dto.SwitchRecordDto;
import com.grace.recon.naas.dto.VisaRecordDto;
import com.grace.recon.naas.kafka.KafkaPublisherService;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ValidationService {

  private static final Logger LOGGER = Logger.getLogger(ValidationService.class.getName());
  private static final String VALIDATION_DLQ_TOPIC = "naas-validation-dlq";

  private final KafkaPublisherService kafkaPublisherService;
  private final MetricService metricService;

  @Autowired
  public ValidationService(
      KafkaPublisherService kafkaPublisherService, MetricService metricService) {
    this.kafkaPublisherService = kafkaPublisherService;
    this.metricService = metricService;
  }

  public List<SwitchRecordDto> validateSwitchRecords(List<SwitchRecordDto> records) {
    LOGGER.info("Validating " + records.size() + " Switch records...");
    final List<SwitchRecordDto> validRecords = new ArrayList<>();

    for (SwitchRecordDto record : records) {
      if (isSwitchRecordValid(record)) {
        validRecords.add(record);
      } else {
        LOGGER.warning(
            "Invalid Switch Record found. Sending to DLQ: " + record.getTransaction_id());
        kafkaPublisherService.sendToDlq(VALIDATION_DLQ_TOPIC, record);
        metricService.incrementCounter("naas.validation.records.failed", "source", "switch");
      }
    }

    long invalidCount = records.size() - validRecords.size();
    LOGGER.info(
        "Validation complete. Valid records: "
            + validRecords.size()
            + ", Invalid records: "
            + invalidCount);
    return validRecords;
  }

  public List<VisaRecordDto> validateVisaRecords(List<VisaRecordDto> records) {
    LOGGER.info("Validating " + records.size() + " VISA records...");
    final List<VisaRecordDto> validRecords = new ArrayList<>();

    for (VisaRecordDto record : records) {
      if (isVisaRecordValid(record)) {
        validRecords.add(record);
      } else {
        LOGGER.warning("Invalid VISA Record found. Sending to DLQ: " + record.getTransactionId());
        kafkaPublisherService.sendToDlq(VALIDATION_DLQ_TOPIC, record);
        metricService.incrementCounter("naas.validation.records.failed", "source", "visa");
      }
    }

    long invalidCount = records.size() - validRecords.size();
    LOGGER.info(
        "Validation complete. Valid records: "
            + validRecords.size()
            + ", Invalid records: "
            + invalidCount);
    return validRecords;
  }

  private boolean isSwitchRecordValid(SwitchRecordDto dto) {
    if (!StringUtils.hasText(dto.getTransaction_id())) {
      LOGGER.warning("Invalid Switch Record: " + dto + " - Missing transaction_id");
      return false;
    }
    return true;
  }

  private boolean isVisaRecordValid(VisaRecordDto dto) {
    if (!StringUtils.hasText(dto.getTransactionId())) {
      LOGGER.warning("Invalid VISA Record: " + dto + " - Missing transactionId");
      return false;
    }
    if (dto.getAmount() == null) {
      LOGGER.warning("Invalid VISA Record: " + dto + " - Missing amount");
      return false;
    }
    return true;
  }
}
