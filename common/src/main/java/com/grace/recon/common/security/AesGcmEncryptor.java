package com.grace.recon.common.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for AES-GCM encryption and decryption. AES-GCM is an authenticated
 * encryption algorithm that provides both confidentiality and integrity.
 */
public class AesGcmEncryptor {

    private static final int GCM_TAG_LENGTH = 16; // in bytes
    private static final int GCM_IV_LENGTH = 12;  // in bytes

    /**
     * Generates a new AES secret key.
     * @return A new AES secret key.
     * @throws NoSuchAlgorithmException If AES algorithm is not available.
     */
    public static SecretKey generateAesKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // 256-bit AES key
        return keyGen.generateKey();
    }

    /**
     * Encrypts data using AES/GCM/NoPadding.
     * @param plaintext The data to encrypt.
     * @param key The secret key for encryption.
     * @return The Base64 encoded string of IV + ciphertext + GCM tag.
     * @throws Exception If encryption fails.
     */
    public static String encrypt(byte[] plaintext, SecretKey key) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        (new SecureRandom()).nextBytes(iv); // Generate a random IV

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        byte[] ciphertext = cipher.doFinal(plaintext);

        byte[] encryptedData = new byte[GCM_IV_LENGTH + ciphertext.length];
        System.arraycopy(iv, 0, encryptedData, 0, GCM_IV_LENGTH);
        System.arraycopy(ciphertext, 0, encryptedData, GCM_IV_LENGTH, ciphertext.length);

        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * Decrypts data using AES/GCM/NoPadding.
     * @param encryptedText The Base64 encoded string of IV + ciphertext + GCM tag.
     * @param key The secret key for decryption.
     * @return The decrypted plaintext.
     * @throws Exception If decryption fails (e.g., tampering detected).
     */
    public static byte[] decrypt(String encryptedText, SecretKey key) throws Exception {
        byte[] decodedData = Base64.getDecoder().decode(encryptedText);

        if (decodedData.length < GCM_IV_LENGTH + GCM_TAG_LENGTH) {
            throw new IllegalArgumentException("Encrypted data is too short.");
        }

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(decodedData, 0, iv, 0, GCM_IV_LENGTH);

        byte[] ciphertextWithTag = new byte[decodedData.length - GCM_IV_LENGTH];
        System.arraycopy(decodedData, GCM_IV_LENGTH, ciphertextWithTag, 0, ciphertextWithTag.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        return cipher.doFinal(ciphertextWithTag);
    }

    /**
     * Converts a Base64 encoded key string back to a SecretKey object.
     * @param encodedKey The Base64 encoded key string.
     * @return The SecretKey object.
     */
    public static SecretKey decodeKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    /**
     * Converts a SecretKey object to a Base64 encoded string.
     * @param secretKey The SecretKey object.
     * @return The Base64 encoded key string.
     */
    public static String encodeKey(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
}
