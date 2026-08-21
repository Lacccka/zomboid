/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.math.ec.rfc7748;

import org.bouncycastle.math.ec.rfc7748.X25519Field;

public abstract class X25519 {
    private static final int C_A = 486662;
    private static final int C_A24 = 121666;
    private static final int[] PsubS_x = new int[]{64258704, 46628941, 18905110, 42949224, 8920788, 10663709, 35115447, 21804323, 8973338, 4366948};
    private static int[] precompBase = null;

    private static int decode32(byte[] byArray, int n) {
        int n2 = byArray[n] & 0xFF;
        n2 |= (byArray[++n] & 0xFF) << 8;
        n2 |= (byArray[++n] & 0xFF) << 16;
        return n2 |= byArray[++n] << 24;
    }

    private static void decodeScalar(byte[] byArray, int n, int[] nArray) {
        for (int i = 0; i < 8; ++i) {
            nArray[i] = X25519.decode32(byArray, n + i * 4);
        }
        nArray[0] = nArray[0] & 0xFFFFFFF8;
        nArray[7] = nArray[7] & Integer.MAX_VALUE;
        nArray[7] = nArray[7] | 0x40000000;
    }

    private static void pointDouble(int[] nArray, int[] nArray2) {
        int[] nArray3 = X25519Field.create();
        int[] nArray4 = X25519Field.create();
        X25519Field.apm(nArray, nArray2, nArray3, nArray4);
        X25519Field.sqr(nArray3, nArray3);
        X25519Field.sqr(nArray4, nArray4);
        X25519Field.mul(nArray3, nArray4, nArray);
        X25519Field.sub(nArray3, nArray4, nArray3);
        X25519Field.mul(nArray3, 121666, nArray2);
        X25519Field.add(nArray2, nArray4, nArray2);
        X25519Field.mul(nArray2, nArray3, nArray2);
    }

    public static synchronized void precompute() {
        if (precompBase != null) {
            return;
        }
        int[] nArray = precompBase = new int[2520];
        int[] nArray2 = new int[2510];
        int[] nArray3 = X25519Field.create();
        nArray3[0] = 9;
        int[] nArray4 = X25519Field.create();
        nArray4[0] = 1;
        int[] nArray5 = X25519Field.create();
        int[] nArray6 = X25519Field.create();
        X25519Field.apm(nArray3, nArray4, nArray5, nArray6);
        int[] nArray7 = X25519Field.create();
        X25519Field.copy(nArray6, 0, nArray7, 0);
        int n = 0;
        while (true) {
            X25519Field.copy(nArray5, 0, nArray, n);
            if (n == 2510) break;
            X25519.pointDouble(nArray3, nArray4);
            X25519Field.apm(nArray3, nArray4, nArray5, nArray6);
            X25519Field.mul(nArray5, nArray7, nArray5);
            X25519Field.mul(nArray7, nArray6, nArray7);
            X25519Field.copy(nArray6, 0, nArray2, n);
            n += 10;
        }
        int[] nArray8 = X25519Field.create();
        X25519Field.inv(nArray7, nArray8);
        while (true) {
            X25519Field.copy(nArray, n, nArray3, 0);
            X25519Field.mul(nArray3, nArray8, nArray3);
            X25519Field.copy(nArray3, 0, precompBase, n);
            if (n == 0) break;
            X25519Field.copy(nArray2, n -= 10, nArray4, 0);
            X25519Field.mul(nArray8, nArray4, nArray8);
        }
    }

    public static void scalarMult(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, int n3) {
        int n4;
        int[] nArray = new int[8];
        X25519.decodeScalar(byArray, n, nArray);
        int[] nArray2 = X25519Field.create();
        X25519Field.decode(byArray2, n2, nArray2);
        int[] nArray3 = X25519Field.create();
        X25519Field.copy(nArray2, 0, nArray3, 0);
        int[] nArray4 = X25519Field.create();
        nArray4[0] = 1;
        int[] nArray5 = X25519Field.create();
        nArray5[0] = 1;
        int[] nArray6 = X25519Field.create();
        int[] nArray7 = X25519Field.create();
        int[] nArray8 = X25519Field.create();
        int n5 = 254;
        int n6 = 1;
        do {
            X25519Field.apm(nArray5, nArray6, nArray7, nArray5);
            X25519Field.apm(nArray3, nArray4, nArray6, nArray3);
            X25519Field.mul(nArray7, nArray3, nArray7);
            X25519Field.mul(nArray5, nArray6, nArray5);
            X25519Field.sqr(nArray6, nArray6);
            X25519Field.sqr(nArray3, nArray3);
            X25519Field.sub(nArray6, nArray3, nArray8);
            X25519Field.mul(nArray8, 121666, nArray4);
            X25519Field.add(nArray4, nArray3, nArray4);
            X25519Field.mul(nArray4, nArray8, nArray4);
            X25519Field.mul(nArray3, nArray6, nArray3);
            X25519Field.apm(nArray7, nArray5, nArray5, nArray6);
            X25519Field.sqr(nArray5, nArray5);
            X25519Field.sqr(nArray6, nArray6);
            X25519Field.mul(nArray6, nArray2, nArray6);
            n4 = --n5 >>> 5;
            int n7 = n5 & 0x1F;
            int n8 = nArray[n4] >>> n7 & 1;
            X25519Field.cswap(n6 ^= n8, nArray3, nArray5);
            X25519Field.cswap(n6, nArray4, nArray6);
            n6 = n8;
        } while (n5 >= 3);
        for (n4 = 0; n4 < 3; ++n4) {
            X25519.pointDouble(nArray3, nArray4);
        }
        X25519Field.inv(nArray4, nArray4);
        X25519Field.mul(nArray3, nArray4, nArray3);
        X25519Field.normalize(nArray3);
        X25519Field.encode(nArray3, byArray3, n3);
    }

    public static void scalarMultBase(byte[] byArray, int n, byte[] byArray2, int n2) {
        int n3;
        X25519.precompute();
        int[] nArray = new int[8];
        X25519.decodeScalar(byArray, n, nArray);
        int[] nArray2 = X25519Field.create();
        int[] nArray3 = X25519Field.create();
        nArray3[0] = 1;
        int[] nArray4 = X25519Field.create();
        nArray4[0] = 1;
        int[] nArray5 = X25519Field.create();
        X25519Field.copy(PsubS_x, 0, nArray5, 0);
        int[] nArray6 = X25519Field.create();
        nArray6[0] = 1;
        int[] nArray7 = nArray3;
        int[] nArray8 = nArray4;
        int[] nArray9 = nArray2;
        int[] nArray10 = nArray7;
        int[] nArray11 = nArray8;
        int n4 = 0;
        int n5 = 3;
        int n6 = 1;
        do {
            X25519Field.copy(precompBase, n4, nArray2, 0);
            n4 += 10;
            n3 = n5 >>> 5;
            int n7 = n5 & 0x1F;
            int n8 = nArray[n3] >>> n7 & 1;
            X25519Field.cswap(n6 ^= n8, nArray3, nArray5);
            X25519Field.cswap(n6, nArray4, nArray6);
            n6 = n8;
            X25519Field.apm(nArray3, nArray4, nArray7, nArray8);
            X25519Field.mul(nArray2, nArray8, nArray9);
            X25519Field.carry(nArray7);
            X25519Field.apm(nArray7, nArray9, nArray10, nArray11);
            X25519Field.sqr(nArray10, nArray10);
            X25519Field.sqr(nArray11, nArray11);
            X25519Field.mul(nArray6, nArray10, nArray3);
            X25519Field.mul(nArray5, nArray11, nArray4);
        } while (++n5 < 255);
        for (n3 = 0; n3 < 3; ++n3) {
            X25519.pointDouble(nArray3, nArray4);
        }
        X25519Field.inv(nArray4, nArray4);
        X25519Field.mul(nArray3, nArray4, nArray3);
        X25519Field.normalize(nArray3);
        X25519Field.encode(nArray3, byArray2, n2);
    }
}

