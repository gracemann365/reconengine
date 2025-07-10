package com.grace.recon.common.config;

import com.grace.recon.common.error.ReconciliationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecretManagerTest {

    private SecretManager secretManager = new SecretManager.PoCSecretManager();

    @Test
    void testGetDatabasePassword() {
        assertEquals("mockDbPass", secretManager.getSecret("database.password"));
    }

    @Test
    void testGetKafkaSaslPassword() {
        assertEquals("mockKafkaPass", secretManager.getSecret("kafka.sasl.password"));
    }

    @Test
    void testGetNonExistentSecretThrowsException() {
        assertThrows(ReconciliationException.class, () -> secretManager.getSecret("non.existent.secret"));
    }

    @Test
    void testGetSecretWithTypeCasting() {
        String dbPass = secretManager.getSecret("database.password", String.class);
        assertEquals("mockDbPass", dbPass);
    }

    @Test
    void testGetSecretWithTypeCastingFailure() {
        assertThrows(ReconciliationException.class, () -> secretManager.getSecret("database.password", Integer.class));
    }
}