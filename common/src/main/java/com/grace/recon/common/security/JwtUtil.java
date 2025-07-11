package com.grace.recon.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for handling JSON Web Tokens (JWT). Provides methods for generating, parsing, and
 * validating JWTs.
 */
public class JwtUtil {

  // This key should be loaded securely from a configuration or environment variable
  // For PoC, a simple static key is used. In production, use a strong, securely managed key.
  private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

  /**
   * Extracts the username from a JWT.
   *
   * @param token The JWT from which to extract the username.
   * @return The username.
   */
  public static String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extracts the expiration date from a JWT.
   *
   * @param token The JWT from which to extract the expiration date.
   * @return The expiration date.
   */
  public static Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Extracts a specific claim from a JWT.
   *
   * @param token The JWT from which to extract the claim.
   * @param claimsResolver A function to resolve the desired claim from the Claims object.
   * @param <T> The type of the claim.
   * @return The extracted claim.
   */
  public static <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private static Claims extractAllClaims(String token) {
    return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
  }

  /**
   * Checks if a JWT is expired.
   *
   * @param token The JWT to check.
   * @return true if the token is expired, false otherwise.
   */
  private static Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Generates a JWT for a given username.
   *
   * @param username The username for which to generate the token.
   * @return The generated JWT.
   */
  public static String generateToken(String username) {
    Map<String, Object> claims = new HashMap<>();
    return createToken(claims, username);
  }

  /**
   * Creates a JWT with specified claims and subject, and default expiration.
   *
   * @param claims Additional claims to include in the token.
   * @param subject The subject of the token (typically the username).
   * @return The created JWT.
   */
  public static String createToken(Map<String, Object> claims, String subject) {
    return createToken(claims, subject, 1000 * 60 * 60 * 10); // Default to 10 hours validity
  }

  /**
   * Creates a JWT with specified claims, subject, and expiration time.
   *
   * @param claims Additional claims to include in the token.
   * @param subject The subject of the token (typically the username).
   * @param expirationMillis The expiration time in milliseconds from now.
   * @return The created JWT.
   */
  public static String createToken(
      Map<String, Object> claims, String subject, long expirationMillis) {
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
        .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * Validates a JWT.
   *
   * @param token The JWT to validate.
   * @param username The expected username.
   * @return true if the token is valid for the given username, false otherwise.
   */
  public static Boolean validateToken(String token, String username) {
    final String extractedUsername = extractUsername(token);
    return (extractedUsername.equals(username) && !isTokenExpired(token));
  }
}
