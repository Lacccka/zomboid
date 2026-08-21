/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.binary;

import org.uncommons.maths.binary.BitString;

public final class BinaryUtils {
    private static final int BITWISE_BYTE_TO_INT = 255;
    private static final char[] HEX_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private BinaryUtils() {
    }

    public static String convertBytesToHexString(byte[] data) {
        StringBuilder buffer = new StringBuilder(data.length * 2);
        for (byte b : data) {
            buffer.append(HEX_CHARS[b >>> 4 & 0xF]);
            buffer.append(HEX_CHARS[b & 0xF]);
        }
        return buffer.toString();
    }

    public static byte[] convertHexStringToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even number of characters.");
        }
        byte[] seed = new byte[hex.length() / 2];
        for (int i = 0; i < seed.length; ++i) {
            int index = i * 2;
            seed[i] = (byte)Integer.parseInt(hex.substring(index, index + 2), 16);
        }
        return seed;
    }

    public static int convertBytesToInt(byte[] bytes, int offset) {
        return 0xFF & bytes[offset + 3] | (0xFF & bytes[offset + 2]) << 8 | (0xFF & bytes[offset + 1]) << 16 | (0xFF & bytes[offset]) << 24;
    }

    public static int[] convertBytesToInts(byte[] bytes) {
        if (bytes.length % 4 != 0) {
            throw new IllegalArgumentException("Number of input bytes must be a multiple of 4.");
        }
        int[] ints = new int[bytes.length / 4];
        for (int i = 0; i < ints.length; ++i) {
            ints[i] = BinaryUtils.convertBytesToInt(bytes, i * 4);
        }
        return ints;
    }

    public static long convertBytesToLong(byte[] bytes, int offset) {
        long value = 0L;
        for (int i = offset; i < offset + 8; ++i) {
            byte b = bytes[i];
            value <<= 8;
            value |= (long)b;
        }
        return value;
    }

    public static BitString convertDoubleToFixedPointBits(double value) {
        if (value < 0.0 || value >= 1.0) {
            throw new IllegalArgumentException("Value must be between 0 and 1.");
        }
        StringBuilder bits = new StringBuilder(64);
        double bitValue = 0.5;
        double d = value;
        while (d > 0.0) {
            if (d >= bitValue) {
                bits.append('1');
                d -= bitValue;
            } else {
                bits.append('0');
            }
            bitValue /= 2.0;
        }
        return new BitString(bits.toString());
    }
}

