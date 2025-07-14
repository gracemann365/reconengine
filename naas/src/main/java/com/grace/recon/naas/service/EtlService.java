package com.grace.recon.naas.service;

import com.grace.recon.common.monitoring.MetricService;
import com.grace.recon.naas.dto.SwitchRecordDto;
import com.grace.recon.naas.dto.VisaRecordDto;
import com.grace.recon.naas.kafka.KafkaPublisherService;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EtlService {

  private static final Logger LOGGER = Logger.getLogger(EtlService.class.getName());
  private static final String ETL_DLQ_TOPIC = "naas-etl-dlq";
  private static final int EXPECTED_FIELD_COUNT = 55;

  private final KafkaPublisherService kafkaPublisherService;
  private final MetricService metricService;

  @Autowired
  public EtlService(KafkaPublisherService kafkaPublisherService, MetricService metricService) {
    this.kafkaPublisherService = kafkaPublisherService;
    this.metricService = metricService;
  }

  public List<SwitchRecordDto> parseSwitchFile(String fileName) {
    LOGGER.info("Starting parsing for Switch file: " + fileName);
    List<SwitchRecordDto> records = new ArrayList<>();
    Pattern pattern =
        Pattern.compile("VALUES\\s*\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
      if (inputStream == null) {
        LOGGER.severe("Switch file not found in resources: " + fileName);
        return records;
      }
      String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      Matcher matcher = pattern.matcher(content);

      while (matcher.find()) {
        String rawRecord = matcher.group(1);
        try {
          String[] values = rawRecord.split(",(?=(?:[^']*'[^']*')*[^']*$)", -1);
          for (int i = 0; i < values.length; i++) {
            values[i] = cleanSqlValue(values[i]);
          }

          if (values.length != EXPECTED_FIELD_COUNT) {
            LOGGER.severe(
                "Parsed value count mismatch: got "
                    + values.length
                    + ", expected "
                    + EXPECTED_FIELD_COUNT);
            for (int i = 0; i < values.length; i++) {
              LOGGER.severe("values[" + i + "]: '" + values[i] + "'");
            }
            throw new IllegalArgumentException("Invalid field count");
          }

          SwitchRecordDto dto = new SwitchRecordDto();
          dto.setTransaction_id(values[1]);
          dto.setMerchant_name(values[2]);
          dto.setMerchant_id(values[3]);
          dto.setStan(values[8]);
          dto.setCard_number(values[9]);
          dto.setTransaction_type(values[11]);
          dto.setAuthorization_code(values[12]);
          dto.setRrn(values[14]);
          dto.setTerminal_id(values[16]);
          try {
            dto.setSettlement_amount(new BigDecimal(values[19]));
          } catch (NumberFormatException nfe) {
            LOGGER.severe(
                "Failed to parse settlement_amount for Switch record. Value: "
                    + values[19]
                    + ". Error: "
                    + nfe.getMessage());
            throw nfe;
          }
          dto.setCreated_at(
              LocalDateTime.parse(values[22], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
          records.add(dto);
          metricService.incrementCounter("naas.etl.records.parsed", "source", "switch");

        } catch (Exception e) {
          LOGGER.severe("Failed to parse Switch record. Sending to DLQ. Error: " + e.getMessage());
          kafkaPublisherService.sendToDlq(ETL_DLQ_TOPIC, rawRecord);
          metricService.incrementCounter("naas.etl.records.failed", "source", "switch");
        }
      }
      LOGGER.info("Successfully parsed " + records.size() + " records from Switch file.");

    } catch (Exception e) {
      LOGGER.severe("Error processing Switch file: " + e.getMessage());
    }
    return records;
  }

  private String cleanSqlValue(String value) {
    if (value == null) return null;
    value = value.trim();
    if ("NULL".equalsIgnoreCase(value)) return null;
    if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
      value = value.substring(1, value.length() - 1);
    }
    return value;
  }

  public List<VisaRecordDto> parseVisaFile(String fileName) {
    LOGGER.info("Starting parsing for VISA file: " + fileName);
    List<VisaRecordDto> records = new ArrayList<>();
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");

    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
      if (inputStream == null) {
        LOGGER.severe("VISA file not found in resources or InputStream is null for: " + fileName);
        return records;
      }

      try (Reader reader = new InputStreamReader(inputStream);
          CSVParser csvParser =
              new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {

        for (CSVRecord csvRecord : csvParser) {
          try {
            VisaRecordDto dto = new VisaRecordDto();
            dto.setTransactionType(csvRecord.get("transactionType"));
            dto.setTransactionId(csvRecord.get("transactionId"));
            dto.setCardNumber(csvRecord.get("cardNumber"));
            dto.setAmount(new BigDecimal(csvRecord.get("amount")));
            dto.setStan(csvRecord.get("Stan"));
            dto.setCurrencyCode(csvRecord.get("currencyCode"));
            dto.setTransactionDate(
                LocalDate.parse(csvRecord.get("transactionDate"), dateFormatter));
            dto.setTransactionTime(LocalTime.parse(csvRecord.get("transactionTime")));
            dto.setAuthorizationCode(csvRecord.get("authorizationCode"));
            dto.setMerchantId(csvRecord.get("merchantId"));
            dto.setRrn(csvRecord.get("rrn"));
            dto.setSettlementAmount(new BigDecimal(csvRecord.get("settlementAmount")));
            records.add(dto);
            metricService.incrementCounter("naas.etl.records.parsed", "source", "visa");
          } catch (Exception e) {
            LOGGER.severe("Failed to parse VISA record. Sending to DLQ. Error: " + e.getMessage());
            kafkaPublisherService.sendToDlq(ETL_DLQ_TOPIC, csvRecord.toString());
            metricService.incrementCounter("naas.etl.records.failed", "source", "visa");
          }
        }
        LOGGER.info("Successfully parsed " + records.size() + " records from VISA file.");
      }
    } catch (Exception e) {
      LOGGER.severe("Error processing VISA file: " + e.getMessage());
      e.printStackTrace();
    }
    return records;
  }
}
