package com.grace.recon.common.security;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

class AesGcmEncryptorTest {

  @Test
  void testGenerateAesKey() throws Exception {
    SecretKey key = AesGcmEncryptor.generateAesKey();
    assertNotNull(key);
    assertEquals("AES", key.getAlgorithm());
    assertEquals(256 / 8, key.getEncoded().length); // 256-bit key
  }

  @Test
  void testEncryptAndDecrypt() throws Exception {
    SecretKey key = AesGcmEncryptor.generateAesKey();
    String plaintext = "This is a test message for AES-GCM encryption.";
    byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);

    String encryptedText = AesGcmEncryptor.encrypt(plaintextBytes, key);
    assertNotNull(encryptedText);
    assertNotEquals(plaintext, encryptedText); // Encrypted text should be different

    byte[] decryptedBytes = AesGcmEncryptor.decrypt(encryptedText, key);
    String decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);

    assertEquals(plaintext, decryptedText);
  }

  @Test
  void testEncryptAndDecrypt_emptyString() throws Exception {
    SecretKey key = AesGcmEncryptor.generateAesKey();
    String plaintext = "";
    byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);

    String encryptedText = AesGcmEncryptor.encrypt(plaintextBytes, key);
    assertNotNull(encryptedText);

    byte[] decryptedBytes = AesGcmEncryptor.decrypt(encryptedText, key);
    String decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);

    assertEquals(plaintext, decryptedText);
  }

  @Test
  void testDecrypt_tamperedData() throws Exception {
    SecretKey key = AesGcmEncryptor.generateAesKey();
    String plaintext = "Original message.";
    byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);

    String encryptedText = AesGcmEncryptor.encrypt(plaintextBytes, key);

    // Tamper with the encrypted text (e.g., change a character)
    String tamperedEncryptedText = encryptedText.substring(0, encryptedText.length() - 5) + "ABCDE";

    assertThrows(
        javax.crypto.AEADBadTagException.class,
        () -> AesGcmEncryptor.decrypt(tamperedEncryptedText, key));
  }

  @Test
  void testEncodeAndDecodeKey() throws Exception {
    SecretKey originalKey = AesGcmEncryptor.generateAesKey();
    String encodedKey = AesGcmEncryptor.encodeKey(originalKey);
    assertNotNull(encodedKey);

    SecretKey decodedKey = AesGcmEncryptor.decodeKey(encodedKey);
    assertNotNull(decodedKey);
    assertEquals(originalKey.getAlgorithm(), decodedKey.getAlgorithm());
    assertArrayEquals(originalKey.getEncoded(), decodedKey.getEncoded());
  }
}
