package com.grace.recon.matcher.service;

import com.grace.recon.common.dto.Quant;
import com.grace.recon.common.dto.QuantPair;
import com.grace.recon.common.dto.output.MatchResult;
import com.grace.recon.common.dto.output.UreQuant;
import com.grace.recon.matcher.kafka.ResultPublisher;
import com.grace.recon.matcher.model.MatchingRules;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

@Service
public class CoreMatchingEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreMatchingEngine.class);

    private final QuantBufferService bufferService;
    private final RuleService ruleService;
    private final ResultPublisher resultPublisher;
    private final ExecutorService workerPool;

    private final Map<String, Field> fieldCache = new ConcurrentHashMap<>();
    private final Counter exactMatchCounter;
    private final Counter fuzzyMatchCounter;
    private final Counter ureCounter;

    @Autowired
    public CoreMatchingEngine(QuantBufferService bufferService, RuleService ruleService, ResultPublisher resultPublisher,
                              MeterRegistry meterRegistry, @Value("${matcher.engine.threads:8}") int threadCount) {
        this.bufferService = bufferService;
        this.ruleService = ruleService;
        this.resultPublisher = resultPublisher;
        this.workerPool = Executors.newFixedThreadPool(threadCount);
        this.exactMatchCounter = meterRegistry.counter("matcher.engine.matches.total", "type", "exact");
        this.fuzzyMatchCounter = meterRegistry.counter("matcher.engine.matches.total", "type", "fuzzy");
        this.ureCounter = meterRegistry.counter("matcher.engine.matches.total", "type", "ure");
    }

    @PostConstruct
    public void startEngine() {
        LOGGER.info("Starting Core Matching Engine...");
        workerPool.submit(this::processingLoop);
    }

    private void processingLoop() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                QuantPair pair = bufferService.takeFromBuffer();
                processTransaction(pair);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("CME processing loop was interrupted.");
        }
    }
    
    // This method contains the main waterfall logic
    public void processTransaction(QuantPair pair) {
        if (isExactMatch(pair)) {
            exactMatchCounter.increment();
            MatchResult match = new MatchResult(pair, MatchResult.MatchType.EXACT, 1.0, null);
            resultPublisher.publishMatchResult(match);
        } else if (isFuzzyMatch(pair)) {
            fuzzyMatchCounter.increment();
            MatchResult match = new MatchResult(pair, MatchResult.MatchType.FUZZY, 0.85, null);
            resultPublisher.publishMatchResult(match);
        } else {
            ureCounter.increment();
            UreQuant ure = new UreQuant(pair, "NO_MATCH_FOUND", "All matching rules failed.", System.currentTimeMillis());
            resultPublisher.publishUreQuant(ure);
        }
    }

    private boolean isExactMatch(QuantPair pair) {
        MatchingRules rules = ruleService.getRules();
        if (rules == null || rules.getExactMatch() == null || rules.getExactMatch().getKeys() == null) {
            return false;
        }
        return rules.getExactMatch().getKeys().stream().allMatch(key -> {
            try {
                Object val1 = getFieldValue(pair.getHusband(), key);
                Object val2 = getFieldValue(pair.getWife(), key);
                return Objects.equals(val1, val2);
            } catch (Exception e) {
                return false;
            }
        });
    }

    private boolean isFuzzyMatch(QuantPair pair) {
        MatchingRules rules = ruleService.getRules();
        if (rules == null || rules.getFuzzyMatch() == null || rules.getFuzzyMatch().getTolerances() == null) {
            throw new IllegalStateException("Fuzzy match rules are not loaded or configured.");
        }
        return rules.getFuzzyMatch().getTolerances().stream().allMatch(tolerance -> {
            try {
                Object val1 = getFieldValue(pair.getHusband(), tolerance.getField());
                Object val2 = getFieldValue(pair.getWife(), tolerance.getField());
                return checkTolerance(val1, val2, tolerance);
            } catch (Exception e) {
                return false;
            }
        });
    }

    private boolean checkTolerance(Object val1, Object val2, MatchingRules.Tolerance tolerance) {
        if (val1 == null || val2 == null) return false;
        return switch (tolerance.getAlgorithm()) {
            case "levenshtein" -> LevenshteinDistance.getDefaultInstance().apply(val1.toString(), val2.toString()) <= tolerance.getValue();
            case "absolute_variance" -> {
                BigDecimal amount1 = new BigDecimal(val1.toString());
                BigDecimal amount2 = new BigDecimal(val2.toString());
                yield amount1.subtract(amount2).abs().compareTo(BigDecimal.valueOf(tolerance.getValue())) <= 0;
            }
            default -> false;
        };
    }

    private Object getFieldValue(Quant quant, String fieldName) throws Exception {
        Field field = fieldCache.computeIfAbsent(fieldName, key -> {
            try {
                Field f = Quant.class.getDeclaredField(key);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        });
        return field.get(quant);
    }

    @PreDestroy
    public void stopEngine() {
        LOGGER.info("Stopping Core Matching Engine.");
        if (workerPool != null) {
            workerPool.shutdownNow();
        }
    }
}