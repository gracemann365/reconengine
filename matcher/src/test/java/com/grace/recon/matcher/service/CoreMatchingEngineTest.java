package com.grace.recon.matcher.service;

import com.grace.recon.common.dto.QuantPair;
import com.grace.recon.common.dto.output.MatchResult;
import com.grace.recon.matcher.kafka.ResultPublisher;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class CoreMatchingEngineTest {

    @Mock
    private QuantBufferService bufferService;
    @Mock
    private RuleService ruleService;
    @Mock
    private ResultPublisher resultPublisher;
    @Mock
    private MeterRegistry meterRegistry;

    private CoreMatchingEngine coreMatchingEngine;

    @BeforeEach
    void setUp() {
        when(meterRegistry.counter(anyString(), anyString(), anyString())).thenReturn(mock(Counter.class));
        coreMatchingEngine = new CoreMatchingEngine(bufferService, ruleService, resultPublisher, meterRegistry, 8);
    }

    @Test
    void processTransaction_shouldThrowException_whenRulesAreNull() {
        // Given: The RuleService is misconfigured and returns null
        when(ruleService.getRules()).thenReturn(null);
        QuantPair pair = new QuantPair(); // Assuming a default constructor or minimal setup

        // When & Then: Assert that our engine fails gracefully
        assertThrows(IllegalStateException.class, () -> {
            coreMatchingEngine.processTransaction(pair);
        });
    }

    // Other tests for exact, fuzzy, and URE paths can be added here
}
