/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.math.ec.rfc7748;

import org.bouncycastle.math.ec.rfc7748.X448Field;

public abstract class X448 {
    private static final int C_A = 156326;
    private static final int C_A24 = 39082;
    private static final int[] S_x = new int[]{0xFFFFFFE, 0xFFFFFFF, 0xFFFFFFF, 0xFFFFFFF, 0xFFFFFFF, 0xFFFFFFF, 0xFFFFFFF, 0xFFFFFFF, 0xFFFFFFE, 0xFFFFFFF, 0xFFFFFFF, 0xFFFFFFF, 0xFFFFFFF, 0xFFFFFFF, 0xFFFFFFF, 0xFFFFFFF};
    private static final int[] PsubS_x = new int[]{161294112, 185702364, 163248300, 54522310, 189866924, 105098465, 66174309, 139206530, 156517789, 136025714, 231801628, 246922668, 59251455, 69446896, 83964484, 252685170};
    private static int[] precompBase = null;

    private static int decode32(byte[] byArray, int n) {
        int n2 = byArray[n] & 0xFF;
        n2 |= (byArray[++n] & 0xFF) << 8;
        n2 |= (byArray[++n] & 0xFF) << 16;
        return n2 |= byArray[++n] << 24;
    }

    private static void decodeScalar(byte[] byArray, int n, int[] nArray) {
        for (int i = 0; i < 14; ++i) {
            nArray[i] = X448.decode32(byArray, n + i * 4);
        }
        nArray[0] = nArray[0] & 0xFFFFFFFC;
        nArray[13] = nArray[13] | Integer.MIN_VALUE;
    }

    private static void pointDouble(int[] nArray, int[] nArray2) {
        int[] nArray3 = X448Field.create();
        int[] nArray4 = X448Field.create();
        X448Field.add(nArray, nArray2, nArray3);
        X448Field.sub(nArray, nArray2, nArray4);
        X448Field.sqr(nArray3, nArray3);
        X448Field.sqr(nArray4, nArray4);
        X448Field.mul(nArray3, nArray4, nArray);
        X448Field.sub(nArray3, nArray4, nArray3);
        X448Field.mul(nArray3, 39082, nArray2);
        X448Field.add(nArray2, nArray4, nArray2);
        X448Field.mul(nArray2, nArray3, nArray2);
    }

    public static synchronized void precompute() {
        if (precompBase != null) {
            return;
        }
        int[] nArray = precompBase = new int[7136];
        int[] nArray2 = new int[7120];
        int[] nArray3 = X448Field.create();
        nArray3[0] = 5;
        int[] nArray4 = X448Field.create();
        nArray4[0] = 1;
        int[] nArray5 = X448Field.create();
        int[] nArray6 = X448Field.create();
        X448Field.add(nArray3, nArray4, nArray5);
        X448Field.sub(nArray3, nArray4, nArray6);
        int[] nArray7 = X448Field.create();
        X448Field.copy(nArray6, 0, nArray7, 0);
        int n = 0;
        while (true) {
            X448Field.copy(nArray5, 0, nArray, n);
            if (n == 7120) break;
            X448.pointDouble(nArray3, nArray4);
            X448Field.add(nArray3, nArray4, nArray5);
            X448Field.sub(nArray3, nArray4, nArray6);
            X448Field.mul(nArray5, nArray7, nArray5);
            X448Field.mul(nArray7, nArray6, nArray7);
            X448Field.copy(nArray6, 0, nArray2, n);
            n += 16;
        }
        int[] nArray8 = X448Field.create();
        X448Field.inv(nArray7, nArray8);
        while (true) {
            X448Field.copy(nArray, n, nArray3, 0);
            X448Field.mul(nArray3, nArray8, nArray3);
            X448Field.copy(nArray3, 0, precompBase, n);
            if (n == 0) break;
            X448Field.copy(nArray2, n -= 16, nArray4, 0);
            X448Field.mul(nArray8, nArray4, nArray8);
        }
    }

    public static void scalarMult(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, int n3) {
        int n4;
        int[] nArray = new int[14];
        X448.decodeScalar(byArray, n, nArray);
        int[] nArray2 = X448Field.create();
        X448Field.decode(byArray2, n2, nArray2);
        int[] nArray3 = X448Field.create();
        X448Field.copy(nArray2, 0, nArray3, 0);
        int[] nArray4 = X448Field.create();
        nArray4[0] = 1;
        int[] nArray5 = X448Field.create();
        nArray5[0] = 1;
        int[] nArray6 = X448Field.create();
        int[] nArray7 = X448Field.create();
        int[] nArray8 = X448Field.create();
        int n5 = 447;
        int n6 = 1;
        do {
            X448Field.add(nArray5, nArray6, nArray7);
            X448Field.sub(nArray5, nArray6, nArray5);
            X448Field.add(nArray3, nArray4, nArray6);
            X448Field.sub(nArray3, nArray4, nArray3);
            X448Field.mul(nArray7, nArray3, nArray7);
            X448Field.mul(nArray5, nArray6, nArray5);
            X448Field.sqr(nArray6, nArray6);
            X448Field.sqr(nArray3, nArray3);
            X448Field.sub(nArray6, nArray3, nArray8);
            X448Field.mul(nArray8, 39082, nArray4);
            X448Field.add(nArray4, nArray3, nArray4);
            X448Field.mul(nArray4, nArray8, nArray4);
            X448Field.mul(nArray3, nArray6, nArray3);
            X448Field.sub(nArray7, nArray5, nArray6);
            X448Field.add(nArray7, nArray5, nArray5);
            X448Field.sqr(nArray5, nArray5);
            X448Field.sqr(nArray6, nArray6);
            X448Field.mul(nArray6, nArray2, nArray6);
            n4 = --n5 >>> 5;
            int n7 = n5 & 0x1F;
            int n8 = nArray[n4] >>> n7 & 1;
            X448Field.cswap(n6 ^= n8, nArray3, nArray5);
            X448Field.cswap(n6, nArray4, nArray6);
            n6 = n8;
        } while (n5 >= 2);
        for (n4 = 0; n4 < 2; ++n4) {
            X448.pointDouble(nArray3, nArray4);
        }
        X448Field.inv(nArray4, nArray4);
        X448Field.mul(nArray3, nArray4, nArray3);
        X448Field.normalize(nArray3);
        X448Field.encode(nArray3, byArray3, n3);
    }

    public static void scalarMultBase(byte[] byArray, int n, byte[] byArray2, int n2) {
        int n3;
        X448.precompute();
        int[] nArray = new int[14];
        X448.decodeScalar(byArray, n, nArray);
        int[] nArray2 = X448Field.create();
        int[] nArray3 = X448Field.create();
        X448Field.copy(S_x, 0, nArray3, 0);
        int[] nArray4 = X448Field.create();
        nArray4[0] = 1;
        int[] nArray5 = X448Field.create();
        X448Field.copy(PsubS_x, 0, nArray5, 0);
        int[] nArray6 = X448Field.create();
        nArray6[0] = 1;
        int[] nArray7 = X448Field.create();
        int[] nArray8 = nArray4;
        int[] nArray9 = nArray2;
        int[] nArray10 = nArray3;
        int[] nArray11 = nArray8;
        int n4 = 0;
        int n5 = 2;
        int n6 = 1;
        do {
            X448Field.copy(precompBase, n4, nArray2, 0);
            n4 += 16;
            n3 = n5 >>> 5;
            int n7 = n5 & 0x1F;
            int n8 = nArray[n3] >>> n7 & 1;
            X448Field.cswap(n6 ^= n8, nArray3, nArray5);
            X448Field.cswap(n6, nArray4, nArray6);
            n6 = n8;
            X448Field.add(nArray3, nArray4, nArray7);
            X448Field.sub(nArray3, nArray4, nArray8);
            X448Field.mul(nArray2, nArray8, nArray9);
            X448Field.carry(nArray7);
            X448Field.add(nArray7, nArray9, nArray10);
            X448Field.sub(nArray7, nArray9, nArray11);
            X448Field.sqr(nArray10, nArray10);
            X448Field.sqr(nArray11, nArray11);
            X448Field.mul(nArray6, nArray10, nArray3);
            X448Field.mul(nArray5, nArray11, nArray4);
        } while (++n5 < 448);
        for (n3 = 0; n3 < 2; ++n3) {
            X448.pointDouble(nArray3, nArray4);
        }
        X448Field.inv(nArray4, nArray4);
        X448Field.mul(nArray3, nArray4, nArray3);
        X448Field.normalize(nArray3);
        X448Field.encode(nArray3, byArray2, n2);
    }
}

