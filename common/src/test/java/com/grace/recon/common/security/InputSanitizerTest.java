package com.grace.recon.common.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class InputSanitizerTest {

  @Test
  void testSanitizeHtml() {
    assertEquals(
        "&lt;script&gt;alert(&#39;xss&#39;);&lt;/script&gt;",
        InputSanitizer.sanitizeHtml("<script>alert('xss');</script>"));
    assertEquals("Normal text", InputSanitizer.sanitizeHtml("Normal text"));
    assertNull(InputSanitizer.sanitizeHtml(null));
    assertEquals("", InputSanitizer.sanitizeHtml(""));
  }

  @Test
  void testSanitizeSql() {
    assertEquals(
        "SELECT name FROM users WHERE id = &#39;1 OR 1=1--&#39;",
        InputSanitizer.sanitizeSql("SELECT name FROM users WHERE id = '1 OR 1=1--'"));
    assertEquals("Normal text", InputSanitizer.sanitizeSql("Normal text"));
    assertNull(InputSanitizer.sanitizeSql(null));
    assertEquals("", InputSanitizer.sanitizeSql(""));
  }

  @Test
  void testSanitizeUrl() {
    assertEquals(
        "http%3A%2F%2Fexample.com%3Fparam%3Dvalue%26another%3Dtest",
        InputSanitizer.sanitizeUrl("http://example.com?param=value&another=test"));
    assertEquals("NormalText", InputSanitizer.sanitizeUrl("NormalText"));
    assertNull(InputSanitizer.sanitizeUrl(null));
    assertEquals("", InputSanitizer.sanitizeUrl(""));
  }

  @Test
  void testSanitizeFilePath() {
    // Note: ESAPI's encodeForOS behavior can be platform-dependent and complex.
    // These tests are basic and might need adjustment based on specific ESAPI configuration or OS.
    assertEquals("..%2F..%2Fetc%2Fpasswd", InputSanitizer.sanitizeFilePath("../../etc/passwd"));
    assertEquals(
        "C%3A%5CUsers%5Ctest%5Cfile.txt",
        InputSanitizer.sanitizeFilePath("C:\\Users\\test\\file.txt"));
    assertNull(InputSanitizer.sanitizeFilePath(null));
    assertEquals("", InputSanitizer.sanitizeFilePath(""));
  }
}
