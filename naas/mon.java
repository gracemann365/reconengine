package com.grace.recon.naas.queue;

import com.grace.recon.naas.dto.SwitchRecordDto;
import com.grace.recon.naas.dto.VisaRecordDto;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class NaasDataQueue {

    // A thread-safe queue to hold records that have passed validation
    // and are waiting for normalization.
    public final Queue<Object> validationToNormalizationQueue = new ConcurrentLinkedQueue<>();

}
package com.grace.recon.naas.kafka;

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
package com.grace.recon.naas.service;

import com.grace.recon.naas.dto.SwitchRecordDto;
import com.grace.recon.naas.dto.VisaRecordDto;
import com.grace.recon.naas.kafka.KafkaPublisherService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final String ETL_DLQ_TOPIC = "naas-etl-dlq";

    private final KafkaPublisherService kafkaPublisherService;

    @Autowired
    public EtlService(KafkaPublisherService kafkaPublisherService) {
        this.kafkaPublisherService = kafkaPublisherService;
    }

    public List<SwitchRecordDto> parseSwitchFile(String fileName) {
        LOGGER.info("Starting parsing for Switch file: " + fileName);
        List<SwitchRecordDto> records = new ArrayList<>();
        Pattern pattern = Pattern.compile("\(([^)]+)\)");

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
                    String[] values = rawRecord.split(",(?=')");

                    if (values.length >= 23) {
                        SwitchRecordDto dto = new SwitchRecordDto();
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
                } catch (Exception e) {
                    LOGGER.severe("Failed to parse Switch record. Sending to DLQ. Error: " + e.getMessage());
                    kafkaPublisherService.sendToDlq(ETL_DLQ_TOPIC, rawRecord);
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
                try {
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
                } catch (Exception e) {
                    LOGGER.severe("Failed to parse VISA record. Sending to DLQ. Error: " + e.getMessage());
                    kafkaPublisherService.sendToDlq(ETL_DLQ_TOPIC, csvRecord.toString());
                }
            }
            LOGGER.info("Successfully parsed " + records.size() + " records from VISA file.");

        } catch (Exception e) {
            LOGGER.severe("Error processing VISA file: " + e.getMessage());
        }
        return records;
    }
}
package com.grace.recon.naas.service;

import com.grace.recon.naas.dto.SwitchRecordDto;
import com.grace.recon.naas.dto.VisaRecordDto;
import com.grace.recon.naas.kafka.KafkaPublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ValidationService {

    private static final Logger LOGGER = Logger.getLogger(ValidationService.class.getName());
    private static final String VALIDATION_DLQ_TOPIC = "naas-validation-dlq";

    private final KafkaPublisherService kafkaPublisherService;

    @Autowired
    public ValidationService(KafkaPublisherService kafkaPublisherService) {
        this.kafkaPublisherService = kafkaPublisherService;
    }

    public List<SwitchRecordDto> validateSwitchRecords(List<SwitchRecordDto> records) {
        LOGGER.info("Validating " + records.size() + " Switch records...");
        final List<SwitchRecordDto> validRecords = new ArrayList<>();

        for (SwitchRecordDto record : records) {
            if (isSwitchRecordValid(record)) {
                validRecords.add(record);
            } else {
                LOGGER.warning("Invalid Switch Record found. Sending to DLQ: " + record.getTransaction_id());
                kafkaPublisherService.sendToDlq(VALIDATION_DLQ_TOPIC, record);
            }
        }

        long invalidCount = records.size() - validRecords.size();
        LOGGER.info("Validation complete. Valid records: " + validRecords.size() + ", Invalid records: " + invalidCount);
        return validRecords;
    }

    public List<VisaRecordDto> validateVisaRecords(List<VisaRecordDto> records) {
        LOGGER.info("Validating " + records.size() + " VISA records...");
        final List<VisaRecordDto> validRecords = new ArrayList<>();

        for (VisaRecordDto record : records) {
            if (isVisaRecordValid(record)) {
                validRecords.add(record);
            }
            else {
                LOGGER.warning("Invalid VISA Record found. Sending to DLQ: " + record.getTransactionId());
                kafkaPublisherService.sendToDlq(VALIDATION_DLQ_TOPIC, record);
            }
        }

        long invalidCount = records.size() - validRecords.size();
        LOGGER.info("Validation complete. Valid records: " + validRecords.size() + ", Invalid records: " + invalidCount);
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
package com.grace.recon.common.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.function.ToDoubleFunction;

/**
 * Service for publishing custom metrics using Micrometer. This allows for
 * instrumenting the application with various metric types like counters, gauges,
 * and timers, which can then be exported to monitoring systems.
 */
@Service
public class MetricService {

    private final MeterRegistry meterRegistry;

    public MetricService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Increments a counter metric.
     * @param name The name of the counter.
     * @param tags Optional tags (key-value pairs) to associate with the metric.
     */
    public void incrementCounter(String name, String... tags) {
        Counter.builder(name)
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Registers a gauge metric.
     * @param name The name of the gauge.
     * @param obj The object to be gauged.
     * @param valueFunction The function to extract the double value from the object.
     * @param tags Optional tags (key-value pairs) to associate with the metric.
     * @param <T> The type of the object being gauged.
     */
    public <T> void registerGauge(String name, T obj, ToDoubleFunction<T> valueFunction, String... tags) {
        Gauge.builder(name, obj, valueFunction)
                .tags(tags)
                .register(meterRegistry);
    }

    /**
     * Records the time taken by an operation.
     * @param name The name of the timer.
     * @param callable The operation to time.
     * @param tags Optional tags (key-value pairs) to associate with the metric.
     * @param <T> The return type of the callable.
     * @return The result of the callable.
     * @throws Exception If the callable throws an exception.
     */
    public <T> T recordTimer(String name, Callable<T> callable, String... tags) throws Exception {
        return Timer.builder(name)
                .tags(tags)
                .register(meterRegistry)
                .recordCallable(callable);
    }

    /**
     * Records the time taken by a runnable operation.
     * @param name The name of the timer.
     * @param runnable The operation to time.
     * @param tags Optional tags (key-value pairs) to associate with the metric.
     */
    public void recordTimer(String name, Runnable runnable, String... tags) {
        Timer.builder(name)
                .tags(tags)
                .register(meterRegistry)
                .record(runnable);
    }
}