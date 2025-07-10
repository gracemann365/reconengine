package com.grace.recon.common.pci;

import com.grace.recon.common.monitoring.AuditLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class PciAuditServiceTest {

    private MockedStatic<AuditLogger> mockedAuditLogger;

    @BeforeEach
    void setUp() {
        // Mock the static AuditLogger class before each test
        mockedAuditLogger = Mockito.mockStatic(AuditLogger.class);
    }

    @Test
    void testLogPciEvent_withDetails() {
        String eventType = "PAN_ACCESS";
        String userId = "user123";
        String panId = "maskedPan123";
        Map<String, String> details = new HashMap<>();
        details.put("reason", "debug");
        details.put("source", "application");

        PciAuditService.logPciEvent(eventType, userId, panId, details);

        // Verify that AuditLogger.logEvent was called with the correct arguments
        Map<String, String> expectedDetails = new HashMap<>();
        expectedDetails.put("eventType", eventType);
        expectedDetails.put("userId", userId);
        expectedDetails.put("panId", panId);
        expectedDetails.putAll(details);

        mockedAuditLogger.verify(() -> AuditLogger.logEvent(eq("PCI Audit Event"), eq(expectedDetails)), times(1));
    }

    @Test
    void testLogPciEvent_noDetails() {
        String eventType = "DATA_MASKING";
        String userId = "system456";
        String panId = "maskedPan456";

        PciAuditService.logPciEvent(eventType, userId, panId);

        // Verify that AuditLogger.logEvent was called with the correct arguments
        Map<String, String> expectedDetails = new HashMap<>();
        expectedDetails.put("eventType", eventType);
        expectedDetails.put("userId", userId);
        expectedDetails.put("panId", panId);

        mockedAuditLogger.verify(() -> AuditLogger.logEvent(eq("PCI Audit Event"), eq(expectedDetails)), times(1));
    }
}
