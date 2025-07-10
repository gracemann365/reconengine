package com.grace.recon.naas.service.EtlService;

import com.grace.recon.naas.dto.SwitchRecordDto;
import com.grace.recon.naas.dto.VisaRecordDto;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

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

@Service
public class EtlService {

    private static final Logger LOGGER = Logger.getLogger(EtlService.class.getName());

    public List<SwitchRecordDto> parseSwitchFile(String fileName) {
        LOGGER.info("Starting parsing for Switch file: " + fileName);
        List<SwitchRecordDto> records = new ArrayList<>();
        // Regex to find the VALUES (...) clauses in the INSERT statements
        Pattern pattern = Pattern.compile("\\(([^)]+)\\)");

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                LOGGER.severe("Switch file not found in resources: " + fileName);
                return records;
            }
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                // We have a match for a row of values, e.g., "'1', 'TXN123', ..."
                String[] values = matcher.group(1).split(",(?=')"); // Split by comma only if followed by a quote

                if (values.length >= 23) { // Check for minimum expected values
                    SwitchRecordDto dto = new SwitchRecordDto();
                    // Helper to trim and remove quotes
                    dto.setTransaction_id(cleanSqlValue(values[1]));
                    dto.setMerchant_name(cleanSqlValue(values[2]));
                    dto.setMerchant_id(cleanSqlValue(values[3]));
                    dto.setStan(cleanSqlValue(values[8]));
                    dto.setCard_number(cleanSqlValue(values[9]));
                    dto.setTransaction_type(cleanSqlValue(values[11]));
                    dto.setAuthorization_code(cleanSqlValue(values[12]));
                    dto.setRrn(cleanSqlValue(values[14]));
                    dto.setTerminal_id(cleanSqlValue(values[16]));
                    dto.setSettlement_amount(new BigDecimal(cleanSqlValue(values[19])));
                    dto.setCreated_at(LocalDateTime.parse(cleanSqlValue(values[22]), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    records.add(dto);
                }
            }
            LOGGER.info("Successfully parsed " + records.size() + " records from Switch file.");

        } catch (Exception e) {
            LOGGER.severe("Error processing Switch file: " + e.getMessage());
        }
        return records;
    }

    private String cleanSqlValue(String value) {
        return value.trim().replace("'", "");
    }

    public List<VisaRecordDto> parseVisaFile(String fileName) {
        LOGGER.info("Starting parsing for VISA file: " + fileName);
        List<VisaRecordDto> records = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");

        try (
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
            Reader reader = new InputStreamReader(inputStream);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())
        ) {
            if (inputStream == null) {
                LOGGER.severe("VISA file not found in resources: " + fileName);
                return records;
            }

            for (CSVRecord csvRecord : csvParser) {
                VisaRecordDto dto = new VisaRecordDto();
                dto.setTransactionType(csvRecord.get("transactionType"));
                dto.setTransactionId(csvRecord.get("transactionId"));
                dto.setCardNumber(csvRecord.get("cardNumber"));
                dto.setAmount(new BigDecimal(csvRecord.get("amount")));
                dto.setStan(csvRecord.get("Stan"));
                dto.setCurrencyCode(csvRecord.get("currencyCode"));
                dto.setTransactionDate(LocalDate.parse(csvRecord.get("transactionDate"), dateFormatter));
                dto.setTransactionTime(LocalTime.parse(csvRecord.get("transactionTime")));
                dto.setAuthorizationCode(csvRecord.get("authorizationCode"));
                dto.setMerchantId(csvRecord.get("merchantId"));
                dto.setRrn(csvRecord.get("rrn"));
                dto.setSettlementAmount(new BigDecimal(csvRecord.get("settlementAmount")));
                records.add(dto);
            }
            LOGGER.info("Successfully parsed " + records.size() + " records from VISA file.");

        } catch (Exception e) {
            LOGGER.severe("Error processing VISA file: " + e.getMessage());
        }
        return records;
    }
}
package com.grace.recon.naas.service.ValidationService;

import com.grace.recon.naas.dto.SwitchRecordDto;
import com.grace.recon.naas.dto.VisaRecordDto;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class ValidationService {

    private static final Logger LOGGER = Logger.getLogger(ValidationService.class.getName());

    public List<SwitchRecordDto> validateSwitchRecords(List<SwitchRecordDto> records) {
        LOGGER.info("Validating " + records.size() + " Switch records...");
        // For the PoC, we'll implement a few simple, hardcoded rules.
        List<SwitchRecordDto> validRecords = records.stream()
                .filter(this::isSwitchRecordValid)
                .collect(Collectors.toList());

        long invalidCount = records.size() - validRecords.size();
        LOGGER.info("Validation complete. Valid records: " + validRecords.size() + ", Invalid records: " + invalidCount);
        return validRecords;
    }

    public List<VisaRecordDto> validateVisaRecords(List<VisaRecordDto> records) {
        LOGGER.info("Validating " + records.size() + " VISA records...");
        List<VisaRecordDto> validRecords = records.stream()
                .filter(this::isVisaRecordValid)
                .collect(Collectors.toList());

        long invalidCount = records.size() - validRecords.size();
        LOGGER.info("Validation complete. Valid records: " + validRecords.size() + ", Invalid records: " + invalidCount);
        return validRecords;
    }

    private boolean isSwitchRecordValid(SwitchRecordDto dto) {
        // Example Rule: Transaction ID must not be null or empty.
        if (!StringUtils.hasText(dto.getTransaction_id())) {
            LOGGER.warning("Invalid Switch Record: " + dto + " - Missing transaction_id");
            return false;
        }
        return true;
    }

    private boolean isVisaRecordValid(VisaRecordDto dto) {
        // Example Rule: Transaction ID must not be null or empty.
        if (!StringUtils.hasText(dto.getTransactionId())) {
            LOGGER.warning("Invalid VISA Record: " + dto + " - Missing transactionId");
            return false;
        }
        // Example Rule: Amount must not be null.
        if (dto.getAmount() == null) {
            LOGGER.warning("Invalid VISA Record: " + dto + " - Missing amount");
            return false;
        }
        return true;
    }
}
package com.grace.recon.naas.service.NormalizationService;

import com.grace.recon.common.dto.Quant;
import com.grace.recon.naas.dto.SwitchRecordDto;
import com.grace.recon.naas.dto.VisaRecordDto;
import com.grace.recon.naas.kafka.KafkaPublisherService;
import com.grace.recon.naas.queue.NaasDataQueue;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class NormalizationService {

    private static final Logger LOGGER = Logger.getLogger(NormalizationService.class.getName());

    private final NaasDataQueue dataQueue;
    private final KafkaPublisherService kafkaPublisherService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Autowired
    public NormalizationService(NaasDataQueue dataQueue, KafkaPublisherService kafkaPublisherService) {
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
                if (record instanceof SwitchRecordDto switchRecord) {
                    Quant quant = normalizeSwitchRecord(switchRecord);
                    kafkaPublisherService.sendQuant(quant); // Send to Kafka
                } else if (record instanceof VisaRecordDto visaRecord) {
                    Quant quant = normalizeVisaRecord(visaRecord);
                    kafkaPublisherService.sendQuant(quant); // Send to Kafka
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
        quant.setAmount(switchRecord.getSettlement_amount());
        quant.setPan(switchRecord.getCard_number());
        quant.setStan(switchRecord.getStan());
        quant.setRrn(switchRecord.getRrn());
        quant.setSourceSystem("SWITCH");
        return quant;
    }

    private Quant normalizeVisaRecord(VisaRecordDto visaRecord) {
        Quant quant = new Quant();
        quant.setTransactionId(visaRecord.getTransactionId());
        quant.setAmount(visaRecord.getAmount());
        quant.setPan(visaRecord.getCardNumber());
        quant.setStan(visaRecord.getStan());
        quant.setRrn(visaRecord.getRrn());
        quant.setSourceSystem("VISA");
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
package com.grace.recon.naas.kafka.KafkaPublisherService;

import com.grace.recon.common.dto.Quant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class KafkaPublisherService {

    private static final Logger LOGGER = Logger.getLogger(KafkaPublisherService.class.getName());

    private static final String TOPIC = "UnifiedDTOs_Input";

    // Changed to KafkaTemplate<String, Object> to handle various DTO types for DLQs
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public KafkaPublisherService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendQuant(Quant quant) {
        try {
            String key = quant.getTransactionId();
            kafkaTemplate.send(TOPIC, key, quant);
            LOGGER.info("Sent Quant to Kafka topic " + TOPIC + " with key " + key);
        } catch (Exception e) {
            LOGGER.severe("Failed to send Quant to Kafka: " + e.getMessage());
            // In a real system, a more robust fallback would be needed here.
        }
    }

    /**
     * Sends a failed record to a specified Dead Letter Queue (DLQ) topic.
     * @param dlqTopic The DLQ topic to send the record to.
     * @param failedRecord The DTO of the record that failed processing.
     */
    public void sendToDlq(String dlqTopic, Object failedRecord) {
        try {
            // We don't have a guaranteed key here, so we send it without one.
            kafkaTemplate.send(dlqTopic, failedRecord);
            LOGGER.info("Sent failed record to DLQ topic: " + dlqTopic);
        } catch (Exception e) {
            LOGGER.severe("Failed to send record to DLQ " + dlqTopic + ": " + e.getMessage());
        }
    }
}