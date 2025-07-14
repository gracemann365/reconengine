package com.grace.recon.common.security;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.codecs.MySQLCodec;
import org.owasp.esapi.codecs.WindowsCodec;
import org.owasp.esapi.errors.EncodingException;

public class InputSanitizer {

  public static String sanitizeHtml(String input) {
    if (input == null) {
      return null;
    }
    return ESAPI.encoder().encodeForHTML(input);
  }

  public static String sanitizeSql(String input) {
    if (input == null) {
      return null;
    }
    return ESAPI.encoder().encodeForSQL(new MySQLCodec(MySQLCodec.Mode.STANDARD), input);
  }

  public static String sanitizeUrl(String input) {
    if (input == null) {
      return null;
    }
    try {
      return ESAPI.encoder().encodeForURL(input);
    } catch (EncodingException e) {
      // Wrap the checked exception in an unchecked exception
      throw new RuntimeException("Error encoding URL", e);
    }
  }

  public static String sanitizeFilePath(String input) {
    if (input == null) {
      return null;
    }
    return ESAPI.encoder().encodeForOS(new WindowsCodec(), input);
  }
}
