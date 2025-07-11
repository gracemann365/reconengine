package com.grace.recon.common.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

  private static final String TEST_USERNAME = "testuser";

  @Test
  void testGenerateAndValidateToken() {
    String token = JwtUtil.generateToken(TEST_USERNAME);
    assertNotNull(token);
    assertTrue(JwtUtil.validateToken(token, TEST_USERNAME));
  }

  @Test
  void testExtractUsername() {
    String token = JwtUtil.generateToken(TEST_USERNAME);
    assertEquals(TEST_USERNAME, JwtUtil.extractUsername(token));
  }

  @Test
  void testTokenExpiration() throws InterruptedException {
    // Generate a token with a very short expiration time for testing
    Map<String, Object> claims = new HashMap<>();
    String shortLivedToken = JwtUtil.createToken(claims, TEST_USERNAME, 100); // 100 milliseconds
    Thread.sleep(150); // Wait for the token to expire
    assertFalse(JwtUtil.validateToken(shortLivedToken, TEST_USERNAME));
  }

  @Test
  void testInvalidToken() {
    String invalidToken = "invalid.jwt.token";
    assertThrows(
        io.jsonwebtoken.MalformedJwtException.class, () -> JwtUtil.extractUsername(invalidToken));
  }

  @Test
  void testValidateToken_wrongUsername() {
    String token = JwtUtil.generateToken(TEST_USERNAME);
    assertFalse(JwtUtil.validateToken(token, "wronguser"));
  }
}
