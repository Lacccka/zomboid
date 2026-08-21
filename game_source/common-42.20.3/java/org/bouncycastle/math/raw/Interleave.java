/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.math.raw;

public class Interleave {
    private static final long M32 = 0x55555555L;
    private static final long M64 = 0x5555555555555555L;
    private static final long M64R = -6148914691236517206L;

    public static int expand8to16(int n) {
        n &= 0xFF;
        n = (n | n << 4) & 0xF0F;
        n = (n | n << 2) & 0x3333;
        n = (n | n << 1) & 0x5555;
        return n;
    }

    public static int expand16to32(int n) {
        n &= 0xFFFF;
        n = (n | n << 8) & 0xFF00FF;
        n = (n | n << 4) & 0xF0F0F0F;
        n = (n | n << 2) & 0x33333333;
        n = (n | n << 1) & 0x55555555;
        return n;
    }

    public static long expand32to64(int n) {
        int n2 = (n ^ n >>> 8) & 0xFF00;
        n ^= n2 ^ n2 << 8;
        n2 = (n ^ n >>> 4) & 0xF000F0;
        n ^= n2 ^ n2 << 4;
        n2 = (n ^ n >>> 2) & 0xC0C0C0C;
        n ^= n2 ^ n2 << 2;
        n2 = (n ^ n >>> 1) & 0x22222222;
        return ((long)((n ^= n2 ^ n2 << 1) >>> 1) & 0x55555555L) << 32 | (long)n & 0x55555555L;
    }

    public static void expand64To128(long l, long[] lArray, int n) {
        long l2 = (l ^ l >>> 16) & 0xFFFF0000L;
        l ^= l2 ^ l2 << 16;
        l2 = (l ^ l >>> 8) & 0xFF000000FF00L;
        l ^= l2 ^ l2 << 8;
        l2 = (l ^ l >>> 4) & 0xF000F000F000F0L;
        l ^= l2 ^ l2 << 4;
        l2 = (l ^ l >>> 2) & 0xC0C0C0C0C0C0C0CL;
        l ^= l2 ^ l2 << 2;
        l2 = (l ^ l >>> 1) & 0x2222222222222222L;
        lArray[n] = (l ^= l2 ^ l2 << 1) & 0x5555555555555555L;
        lArray[n + 1] = l >>> 1 & 0x5555555555555555L;
    }

    public static void expand64To128Rev(long l, long[] lArray, int n) {
        long l2 = (l ^ l >>> 16) & 0xFFFF0000L;
        l ^= l2 ^ l2 << 16;
        l2 = (l ^ l >>> 8) & 0xFF000000FF00L;
        l ^= l2 ^ l2 << 8;
        l2 = (l ^ l >>> 4) & 0xF000F000F000F0L;
        l ^= l2 ^ l2 << 4;
        l2 = (l ^ l >>> 2) & 0xC0C0C0C0C0C0C0CL;
        l ^= l2 ^ l2 << 2;
        l2 = (l ^ l >>> 1) & 0x2222222222222222L;
        lArray[n] = (l ^= l2 ^ l2 << 1) & 0xAAAAAAAAAAAAAAAAL;
        lArray[n + 1] = l << 1 & 0xAAAAAAAAAAAAAAAAL;
    }

    public static int shuffle(int n) {
        int n2 = (n ^ n >>> 8) & 0xFF00;
        n ^= n2 ^ n2 << 8;
        n2 = (n ^ n >>> 4) & 0xF000F0;
        n ^= n2 ^ n2 << 4;
        n2 = (n ^ n >>> 2) & 0xC0C0C0C;
        n ^= n2 ^ n2 << 2;
        n2 = (n ^ n >>> 1) & 0x22222222;
        return n ^= n2 ^ n2 << 1;
    }

    public static long shuffle(long l) {
        long l2 = (l ^ l >>> 16) & 0xFFFF0000L;
        l ^= l2 ^ l2 << 16;
        l2 = (l ^ l >>> 8) & 0xFF000000FF00L;
        l ^= l2 ^ l2 << 8;
        l2 = (l ^ l >>> 4) & 0xF000F000F000F0L;
        l ^= l2 ^ l2 << 4;
        l2 = (l ^ l >>> 2) & 0xC0C0C0C0C0C0C0CL;
        l ^= l2 ^ l2 << 2;
        l2 = (l ^ l >>> 1) & 0x2222222222222222L;
        return l ^= l2 ^ l2 << 1;
    }

    public static int shuffle2(int n) {
        int n2 = (n ^ n >>> 7) & 0xAA00AA;
        n ^= n2 ^ n2 << 7;
        n2 = (n ^ n >>> 14) & 0xCCCC;
        n ^= n2 ^ n2 << 14;
        n2 = (n ^ n >>> 4) & 0xF000F0;
        n ^= n2 ^ n2 << 4;
        n2 = (n ^ n >>> 8) & 0xFF00;
        return n ^= n2 ^ n2 << 8;
    }

    public static int unshuffle(int n) {
        int n2 = (n ^ n >>> 1) & 0x22222222;
        n ^= n2 ^ n2 << 1;
        n2 = (n ^ n >>> 2) & 0xC0C0C0C;
        n ^= n2 ^ n2 << 2;
        n2 = (n ^ n >>> 4) & 0xF000F0;
        n ^= n2 ^ n2 << 4;
        n2 = (n ^ n >>> 8) & 0xFF00;
        return n ^= n2 ^ n2 << 8;
    }

    public static long unshuffle(long l) {
        long l2 = (l ^ l >>> 1) & 0x2222222222222222L;
        l ^= l2 ^ l2 << 1;
        l2 = (l ^ l >>> 2) & 0xC0C0C0C0C0C0C0CL;
        l ^= l2 ^ l2 << 2;
        l2 = (l ^ l >>> 4) & 0xF000F000F000F0L;
        l ^= l2 ^ l2 << 4;
        l2 = (l ^ l >>> 8) & 0xFF000000FF00L;
        l ^= l2 ^ l2 << 8;
        l2 = (l ^ l >>> 16) & 0xFFFF0000L;
        return l ^= l2 ^ l2 << 16;
    }

    public static int unshuffle2(int n) {
        int n2 = (n ^ n >>> 8) & 0xFF00;
        n ^= n2 ^ n2 << 8;
        n2 = (n ^ n >>> 4) & 0xF000F0;
        n ^= n2 ^ n2 << 4;
        n2 = (n ^ n >>> 14) & 0xCCCC;
        n ^= n2 ^ n2 << 14;
        n2 = (n ^ n >>> 7) & 0xAA00AA;
        return n ^= n2 ^ n2 << 7;
    }
}

