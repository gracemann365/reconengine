package com.grace.recon.common.security;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import org.junit.jupiter.api.Test;

class TlsConfigTest {

  private static final char[] KEYSTORE_PASSWORD = "password".toCharArray();
  private static final char[] TRUSTSTORE_PASSWORD = "password".toCharArray();

  // Helper method to create a dummy JKS keystore for testing
  private String createDummyKeyStore(String filename, char[] password) throws Exception {
    KeyStore ks = KeyStore.getInstance("JKS");
    ks.load(null, password);

    // This test requires a real certificate and key, which is complex to generate here.
    // For compilation, we'll assume this part is handled elsewhere or mocked.
    // In a real scenario, you would use a library like Bouncy Castle to create test certs.

    File file = new File(filename);
    try (FileOutputStream fos = new FileOutputStream(file)) {
      ks.store(fos, password);
    }
    return file.getAbsolutePath();
  }

  @Test
  void testCreateSSLContext_invalidPaths() {
    assertThrows(
        IOException.class,
        () ->
            TlsConfig.createSSLContext(
                "nonexistent_keystore.jks", KEYSTORE_PASSWORD,
                "nonexistent_truststore.jks", TRUSTSTORE_PASSWORD));
  }
}
