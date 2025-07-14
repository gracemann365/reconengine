package com.grace.recon.matcher.service;

public class MatchingUtil {

  /**
   * Calculates the Levenshtein distance between two strings. This is a simplified implementation
   * for demonstration purposes. A more robust solution might use an external library or a more
   * optimized algorithm.
   *
   * @param s1 the first string
   * @param s2 the second string
   * @return the Levenshtein distance
   */
  public static int calculateLevenshteinDistance(String s1, String s2) {
    s1 = s1.toLowerCase();
    s2 = s2.toLowerCase();

    int[] costs = new int[s2.length() + 1];
    for (int i = 0; i <= s1.length(); i++) {
      int lastValue = i;
      for (int j = 0; j <= s2.length(); j++) {
        if (i == 0) {
          costs[j] = j;
        } else {
          if (j > 0) {
            int newValue = costs[j - 1];
            if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
              newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
            }
            costs[j - 1] = lastValue;
            lastValue = newValue;
          }
        }
      }
      if (i > 0) {
        costs[s2.length()] = lastValue;
      }
    }
    return costs[s2.length()];
  }

  /**
   * Calculates the absolute difference between two double values.
   *
   * @param d1 the first double
   * @param d2 the second double
   * @return the absolute difference
   */
  public static double calculateAbsoluteDifference(double d1, double d2) {
    return Math.abs(d1 - d2);
  }

  /**
   * Calculates the percentage variance between two double values.
   *
   * @param d1 the first double
   * @param d2 the second double
   * @return the percentage variance
   */
  public static double calculatePercentageVariance(double d1, double d2) {
    if (d1 == 0 && d2 == 0) return 0.0;
    if (d1 == 0 || d2 == 0) return Double.MAX_VALUE; // Avoid division by zero or infinite variance
    return Math.abs((d1 - d2) / ((d1 + d2) / 2.0)) * 100; // Symmetric percentage difference
  }
}
