package ru.selena.util;

import org.apache.commons.lang.Validate;

/**
 * Date: 12/17/12
 * Time: 11:54 PM
 *
 * @author Artem Titov
 */
public class NumberUtils {

    private static final int INT_MASK = 0x000000ff;
    private static final long LONG_MASK = 0x0000000000000ff;
    private static final int BYTE_MASK = 0xff;
    private static final int INT_BYTE_SIZE = Integer.SIZE / Byte.SIZE;
    private static final int LONG_BYTE_SIZE = Long.SIZE / Byte.SIZE;

    /**
     * Convert specified byte array to int number.
     *
     * @param bytes byte array
     * @return int number was built from first 4 bytes of array
     * @throws IllegalArgumentException if array is null or contains less than 4 bytes
     */
    public static int toInt(final byte[] bytes) {
        return toInt(bytes, 0);
    }

    /**
     * Convert specified byte array to int number.
     *
     * @param bytes  byte array
     * @param offset position of the first byte in array
     * @return int number was built from {@link Integer#SIZE} / 8 bytes of array from offset position inclusively
     * @throws IllegalArgumentException if array is null or contains less than {@link Integer#SIZE} / 8 bytes after offset
     */
    public static int toInt(final byte[] bytes, final int offset) {
        Validate.notNull(bytes, "Byte array can't be null");
        Validate.isTrue(offset + INT_BYTE_SIZE - 1 < bytes.length,
                String.format("Start position must be in array and there must be at least %d bytes after offset, " +
                        "but array length is %d and offset is %d", INT_BYTE_SIZE, bytes.length, offset));

        int result = 0;
        for (int i = INT_BYTE_SIZE - 1, shift = 0; i >= 0; i--, shift += Byte.SIZE) {
            result += ((INT_MASK & bytes[offset + i]) << shift);
        }
        return result;
    }

    /**
     * Convert specified byte array to long number.
     *
     * @param bytes byte array
     * @return long number was built from first 8 bytes of array
     * @throws IllegalArgumentException if array is null or contains less than 8 bytes
     */
    public static long toLong(final byte[] bytes) {
        return toLong(bytes, 0);
    }

    /**
     * Convert specified byte array to long number.
     *
     * @param bytes  byte array
     * @param offset position of the first byte in array
     * @return long number was built from 8 bytes of array from offset position inclusively
     * @throws IllegalArgumentException if array is null or contains less than 8 bytes after offset
     */
    public static long toLong(final byte[] bytes, final int offset) {
        Validate.notNull(bytes, "Byte array can't be null");
        Validate.isTrue(offset + 7 < bytes.length,
                String.format("Start position must be in array and there must be at least 8 bytes after offset, " +
                        "but array length is %d and offset is %d", bytes.length, offset));

        long result = 0;
        for (int i = LONG_BYTE_SIZE - 1, shift = 0; i >= 0; i--, shift += Byte.SIZE) {
            result += ((LONG_MASK & bytes[offset + i]) << shift);
        }
        return result;
    }

    /**
     * Convert int number to byte array. Leading zeros are available
     *
     * @param number number
     * @return its byte array
     */
    public static byte[] toByteArray(final int number) {
        final byte[] buffer = new byte[INT_BYTE_SIZE];
        toByteArray(number, buffer, 0);
        return buffer;
    }

    /**
     * Convert int number to byte array. Leading zeros are available. Result will be store in buffer starting from
     * offset
     *
     * @param number number
     */
    public static void toByteArray(final int number, final byte[] buffer, final int offset) {
        for (int pos = 0, shift = Integer.SIZE - Byte.SIZE; pos < INT_BYTE_SIZE; pos++, shift -= Byte.SIZE) {
            buffer[offset + pos] = (byte) (number >>> shift & BYTE_MASK);
        }
    }

    /**
     * Convert long number to byte array. Leading zeros are available
     *
     * @param number number
     * @return its byte array
     */
    public static byte[] toByteArray(final long number) {
        final byte[] buffer = new byte[LONG_BYTE_SIZE];
        toByteArray(number, buffer, 0);
        return buffer;
    }

    /**
     * Convert long number to byte array. Leading zeros are available. Result will be store in buffer starting from
     * offset
     *
     * @param number number
     */
    public static void toByteArray(final long number, final byte[] buffer, final int offset) {
        for (int pos = 0, shift = Long.SIZE - Byte.SIZE; pos < LONG_BYTE_SIZE; pos++, shift -= Byte.SIZE) {
            buffer[offset + pos] = (byte) (number >>> shift & BYTE_MASK);
        }
    }

}
