package com.grace.recon.common.pci;

/**
 * Implements the Luhn algorithm for basic validation of credit card numbers and other identification numbers.
 * The Luhn algorithm is a simple checksum formula used to validate a variety of identification numbers,
 * such as credit card numbers, IMEI numbers, and Canadian Social Insurance Numbers.
 */
public class LuhnValidator {

    /**
     * Validates a given number string using the Luhn algorithm.
     *
     * @param number The number string to validate (e.g., credit card number).
     * @return true if the number is valid according to the Luhn algorithm, false otherwise.
     */
    public static boolean isValid(String number) {
        if (number == null || number.isEmpty()) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }
}
