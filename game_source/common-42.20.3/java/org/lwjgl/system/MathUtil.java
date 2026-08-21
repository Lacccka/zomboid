/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system;

public final class MathUtil {
    private MathUtil() {
    }

    public static boolean mathIsPoT(int value) {
        return Integer.bitCount(value) == 1;
    }

    public static int mathRoundPoT(int value) {
        return 1 << 32 - Integer.numberOfLeadingZeros(value - 1);
    }

    public static boolean mathHasZeroByte(int value) {
        return (value - 0x1010101 & ~value & 0x80808080) != 0;
    }

    public static boolean mathHasZeroByte(long value) {
        return (value - 0x101010101010101L & (value ^ 0xFFFFFFFFFFFFFFFFL) & 0x8080808080808080L) != 0L;
    }

    public static boolean mathHasZeroShort(int value) {
        return (value - 65537 & ~value & 0x80008000) != 0;
    }

    public static boolean mathHasZeroShort(long value) {
        return (value - 0x1000100010001L & (value ^ 0xFFFFFFFFFFFFFFFFL) & 0x8000800080008000L) != 0L;
    }
}

