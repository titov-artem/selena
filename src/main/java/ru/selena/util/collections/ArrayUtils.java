package ru.selena.util.collections;

import org.apache.commons.lang.Validate;

/**
 * Date: 12/17/12
 * Time: 10:23 AM
 *
 * @author Artem Titov
 */
public class ArrayUtils {
    private static final char[] HEXADECIMAL_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private ArrayUtils() {
    }

    /**
     * Return hexadecimal string representing this byte array. Leading zeros are available
     *
     * @param a byte array
     * @return hexadecimal string
     * @throws IllegalArgumentException if array is null
     */
    public static String toHexString(final byte[] a) {
        Validate.notNull(a, "Array can't be null");

        final char[] hexChars = new char[a.length * 2];
        int v;
        for (int i = 0, aLength = a.length; i < aLength; i++) {
            v = a[i] & 0xff;
            hexChars[i * 2] = HEXADECIMAL_DIGITS[v >>> 4];
            hexChars[i * 2 + 1] = HEXADECIMAL_DIGITS[v & 0x0f];
        }
        return new String(hexChars);
    }

    /**
     * Return a byte array corresponding to hexadecimal string
     *
     * @param hexadecimalString hexadecimal string
     * @return byte array
     * @throws IllegalArgumentException if hexadecimalString is null or its length not even
     */
    public static byte[] toByteArray(final String hexadecimalString) {
        Validate.notNull(hexadecimalString, "Hexadecimal string can't be null");
        Validate.isTrue(hexadecimalString.length() % 2 == 0, "Length must be even");

        final byte[] a = new byte[hexadecimalString.length() / 2];
        for (int i = 0; i < a.length; i++) {
            a[i] = (byte) ((Character.digit(hexadecimalString.charAt(2 * i), 16) << 4)
                    + Character.digit(hexadecimalString.charAt(2 * i + 1), 16));
        }
        return a;
    }
}
