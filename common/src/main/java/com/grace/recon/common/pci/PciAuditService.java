package com.grace.recon.common.pci;

import com.grace.recon.common.monitoring.AuditLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for publishing PCI audit events. This ensures that all sensitive data
 * access and processing activities are logged for compliance and security monitoring.
 */
public class PciAuditService {

    /**
     * Logs a PCI-related audit event.
     *
     * @param eventType The type of PCI event (e.g., "PAN_ACCESS", "DATA_MASKING").
     * @param userId The ID of the user or system performing the action.
     * @param panId A masked or tokenized identifier for the PAN involved (not the actual PAN).
     * @param details Optional additional details about the event.
     */
    public static void logPciEvent(String eventType, String userId, String panId, Map<String, String> details) {
        Map<String, String> auditDetails = new HashMap<>();
        auditDetails.put("eventType", eventType);
        auditDetails.put("userId", userId);
        auditDetails.put("panId", panId);
        if (details != null) {
            auditDetails.putAll(details);
        }
        AuditLogger.logEvent("PCI Audit Event", auditDetails);
    }

    /**
     * Logs a PCI-related audit event without additional details.
     *
     * @param eventType The type of PCI event.
     * @param userId The ID of the user or system performing the action.
     * @param panId A masked or tokenized identifier for the PAN involved.
     */
    public static void logPciEvent(String eventType, String userId, String panId) {
        logPciEvent(eventType, userId, panId, null);
    }
}
