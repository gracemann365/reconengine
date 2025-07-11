package com.grace.recon.common.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Utility class for configuring TLS/SSL contexts. This class provides methods to create SSLContext
 * instances from keystores and truststores, enabling secure communication over TLS.
 */
public class TlsConfig {

  /**
   * Creates an SSLContext initialized with the given keystore and truststore.
   *
   * @param keyStorePath The path to the JKS keystore file.
   * @param keyStorePassword The password for the keystore.
   * @param trustStorePath The path to the JKS truststore file.
   * @param trustStorePassword The password for the truststore.
   * @return An initialized SSLContext.
   * @throws IOException If there's an issue reading the keystore/truststore files.
   * @throws KeyStoreException If there's an issue with the keystore/truststore.
   * @throws NoSuchAlgorithmException If the specified algorithm is not available.
   * @throws CertificateException If there's an issue with certificates.
   * @throws UnrecoverableKeyException If the key cannot be recovered.
   * @throws KeyManagementException If the SSLContext initialization fails.
   */
  public static SSLContext createSSLContext(
      String keyStorePath,
      char[] keyStorePassword,
      String trustStorePath,
      char[] trustStorePassword)
      throws IOException,
          KeyStoreException,
          NoSuchAlgorithmException,
          CertificateException,
          UnrecoverableKeyException,
          KeyManagementException {

    // Load KeyStore
    KeyStore keyStore = KeyStore.getInstance("JKS");
    try (FileInputStream fis = new FileInputStream(keyStorePath)) {
      keyStore.load(fis, keyStorePassword);
    }

    // Load TrustStore
    KeyStore trustStore = KeyStore.getInstance("JKS");
    try (FileInputStream fis = new FileInputStream(trustStorePath)) {
      trustStore.load(fis, trustStorePassword);
    }

    // Initialize KeyManagerFactory
    KeyManagerFactory keyManagerFactory =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyManagerFactory.init(keyStore, keyStorePassword);

    // Initialize TrustManagerFactory
    TrustManagerFactory trustManagerFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(trustStore);

    // Initialize SSLContext
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(
        keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

    return sslContext;
  }
}
