package com.grace.recon.common.pci;

/**
 * Utility class for masking sensitive Primary Account Numbers (PANs).
 * This helps in complying with PCI DSS requirements by not exposing full PANs in logs or non-secure environments.
 */
public class PanMasker {

    /**
     * Masks a Primary Account Number (PAN) to show only the last four digits.
     * The format will be `************1234` for a 16-digit PAN.
     * If the PAN is null or shorter than 4 characters, it returns the original value.
     *
     * @param pan The Primary Account Number to mask.
     * @return The masked PAN, or the original PAN if it's too short or null.
     */
    public static String maskPan(String pan) {
        if (pan == null || pan.length() <= 4) {
            return pan;
        }
        // Mask all but the last 4 digits with asterisks
        return "************" + pan.substring(pan.length() - 4);
    }

    /**
     * Masks a Primary Account Number (PAN) to show the first six and last four digits,
     * with asterisks in between. This is a common masking format for display purposes.
     * The format will be `123456******1234` for a 16-digit PAN.
     * If the PAN is null or shorter than 10 characters (6 + 4), it returns the original value.
     *
     * @param pan The Primary Account Number to mask.
     * @return The masked PAN, or the original PAN if it's too short or null.
     */
    public static String maskPanShowFirstSixLastFour(String pan) {
        if (pan == null || pan.length() <= 10) { // 6 (first) + 4 (last) = 10
            return pan;
        }
        String firstSix = pan.substring(0, 6);
        String lastFour = pan.substring(pan.length() - 4);
        // Calculate number of asterisks needed
        int asterisksCount = pan.length() - 10;
        StringBuilder maskedPan = new StringBuilder(firstSix);
        for (int i = 0; i < asterisksCount; i++) {
            maskedPan.append('*');
        }
        maskedPan.append(lastFour);
        return maskedPan.toString();
    }
}
