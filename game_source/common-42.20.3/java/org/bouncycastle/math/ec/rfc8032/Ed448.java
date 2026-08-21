/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.math.ec.rfc8032;

import org.bouncycastle.crypto.digests.SHAKEDigest;
import org.bouncycastle.math.ec.rfc7748.X448Field;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

public abstract class Ed448 {
    private static final long M26L = 0x3FFFFFFL;
    private static final long M28L = 0xFFFFFFFL;
    private static final long M32L = 0xFFFFFFFFL;
    private static final int POINT_BYTES = 57;
    private static final int SCALAR_INTS = 14;
    private static final int SCALAR_BYTES = 57;
    public static final int PUBLIC_KEY_SIZE = 57;
    public static final int SECRET_KEY_SIZE = 57;
    public static final int SIGNATURE_SIZE = 114;
    private static final byte[] DOM4_PREFIX = Strings.toByteArray("SigEd448");
    private static final int[] P = new int[]{-1, -1, -1, -1, -1, -1, -1, -2, -1, -1, -1, -1, -1, -1};
    private static final int[] L = new int[]{-1420278541, 595116690, -1916432555, 560775794, -1361693040, -1001465015, 2093622249, -1, -1, -1, -1, -1, -1, 0x3FFFFFFF};
    private static final int L_0 = 78101261;
    private static final int L_1 = 141809365;
    private static final int L_2 = 175155932;
    private static final int L_3 = 64542499;
    private static final int L_4 = 158326419;
    private static final int L_5 = 191173276;
    private static final int L_6 = 104575268;
    private static final int L_7 = 137584065;
    private static final int L4_0 = 43969588;
    private static final int L4_1 = 30366549;
    private static final int L4_2 = 163752818;
    private static final int L4_3 = 258169998;
    private static final int L4_4 = 96434764;
    private static final int L4_5 = 227822194;
    private static final int L4_6 = 149865618;
    private static final int L4_7 = 550336261;
    private static final int[] B_x = new int[]{118276190, 40534716, 9670182, 135141552, 85017403, 259173222, 68333082, 171784774, 174973732, 15824510, 73756743, 57518561, 94773951, 248652241, 107736333, 82941708};
    private static final int[] B_y = new int[]{36764180, 8885695, 130592152, 20104429, 163904957, 30304195, 121295871, 5901357, 125344798, 171541512, 175338348, 209069246, 3626697, 38307682, 24032956, 110359655};
    private static final int C_d = -39081;
    private static final int WNAF_WIDTH_BASE = 7;
    private static final int PRECOMP_BLOCKS = 5;
    private static final int PRECOMP_TEETH = 5;
    private static final int PRECOMP_SPACING = 18;
    private static final int PRECOMP_POINTS = 16;
    private static final int PRECOMP_MASK = 15;
    private static PointExt[] precompBaseTable = null;
    private static int[] precompBase = null;

    private static byte[] calculateS(byte[] byArray, byte[] byArray2, byte[] byArray3) {
        int[] nArray = new int[28];
        Ed448.decodeScalar(byArray, 0, nArray);
        int[] nArray2 = new int[14];
        Ed448.decodeScalar(byArray2, 0, nArray2);
        int[] nArray3 = new int[14];
        Ed448.decodeScalar(byArray3, 0, nArray3);
        Nat.mulAddTo(14, nArray2, nArray3, nArray);
        byte[] byArray4 = new byte[114];
        for (int i = 0; i < nArray.length; ++i) {
            Ed448.encode32(nArray[i], byArray4, i * 4);
        }
        return Ed448.reduceScalar(byArray4);
    }

    private static boolean checkContextVar(byte[] byArray) {
        return byArray != null && byArray.length < 256;
    }

    private static boolean checkPointVar(byte[] byArray) {
        if ((byArray[56] & 0x7F) != 0) {
            return false;
        }
        int[] nArray = new int[14];
        Ed448.decode32(byArray, 0, nArray, 0, 14);
        return !Nat.gte(14, nArray, P);
    }

    private static boolean checkScalarVar(byte[] byArray) {
        if (byArray[56] != 0) {
            return false;
        }
        int[] nArray = new int[14];
        Ed448.decodeScalar(byArray, 0, nArray);
        return !Nat.gte(14, nArray, L);
    }

    private static int decode16(byte[] byArray, int n) {
        int n2 = byArray[n] & 0xFF;
        return n2 |= (byArray[++n] & 0xFF) << 8;
    }

    private static int decode24(byte[] byArray, int n) {
        int n2 = byArray[n] & 0xFF;
        n2 |= (byArray[++n] & 0xFF) << 8;
        return n2 |= (byArray[++n] & 0xFF) << 16;
    }

    private static int decode32(byte[] byArray, int n) {
        int n2 = byArray[n] & 0xFF;
        n2 |= (byArray[++n] & 0xFF) << 8;
        n2 |= (byArray[++n] & 0xFF) << 16;
        return n2 |= byArray[++n] << 24;
    }

    private static void decode32(byte[] byArray, int n, int[] nArray, int n2, int n3) {
        for (int i = 0; i < n3; ++i) {
            nArray[n2 + i] = Ed448.decode32(byArray, n + i * 4);
        }
    }

    private static boolean decodePointVar(byte[] byArray, int n, boolean bl, PointExt pointExt) {
        byte[] byArray2 = Arrays.copyOfRange(byArray, n, n + 57);
        if (!Ed448.checkPointVar(byArray2)) {
            return false;
        }
        int n2 = (byArray2[56] & 0x80) >>> 7;
        byArray2[56] = (byte)(byArray2[56] & 0x7F);
        X448Field.decode(byArray2, 0, pointExt.y);
        int[] nArray = X448Field.create();
        int[] nArray2 = X448Field.create();
        X448Field.sqr(pointExt.y, nArray);
        X448Field.mul(nArray, 39081, nArray2);
        X448Field.negate(nArray, nArray);
        X448Field.addOne(nArray);
        X448Field.addOne(nArray2);
        if (!X448Field.sqrtRatioVar(nArray, nArray2, pointExt.x)) {
            return false;
        }
        X448Field.normalize(pointExt.x);
        if (n2 == 1 && X448Field.isZeroVar(pointExt.x)) {
            return false;
        }
        if (bl ^ n2 != (pointExt.x[0] & 1)) {
            X448Field.negate(pointExt.x, pointExt.x);
        }
        Ed448.pointExtendXY(pointExt);
        return true;
    }

    private static void decodeScalar(byte[] byArray, int n, int[] nArray) {
        Ed448.decode32(byArray, n, nArray, 0, 14);
    }

    private static void dom4(SHAKEDigest sHAKEDigest, byte by, byte[] byArray) {
        sHAKEDigest.update(DOM4_PREFIX, 0, DOM4_PREFIX.length);
        sHAKEDigest.update(by);
        sHAKEDigest.update((byte)byArray.length);
        sHAKEDigest.update(byArray, 0, byArray.length);
    }

    private static void encode24(int n, byte[] byArray, int n2) {
        byArray[n2] = (byte)n;
        byArray[++n2] = (byte)(n >>> 8);
        byArray[++n2] = (byte)(n >>> 16);
    }

    private static void encode32(int n, byte[] byArray, int n2) {
        byArray[n2] = (byte)n;
        byArray[++n2] = (byte)(n >>> 8);
        byArray[++n2] = (byte)(n >>> 16);
        byArray[++n2] = (byte)(n >>> 24);
    }

    private static void encode56(long l, byte[] byArray, int n) {
        Ed448.encode32((int)l, byArray, n);
        Ed448.encode24((int)(l >>> 32), byArray, n + 4);
    }

    private static void encodePoint(PointExt pointExt, byte[] byArray, int n) {
        int[] nArray = X448Field.create();
        int[] nArray2 = X448Field.create();
        X448Field.inv(pointExt.z, nArray2);
        X448Field.mul(pointExt.x, nArray2, nArray);
        X448Field.mul(pointExt.y, nArray2, nArray2);
        X448Field.normalize(nArray);
        X448Field.normalize(nArray2);
        X448Field.encode(nArray2, byArray, n);
        byArray[n + 57 - 1] = (byte)((nArray[0] & 1) << 7);
    }

    public static void generatePublicKey(byte[] byArray, int n, byte[] byArray2, int n2) {
        SHAKEDigest sHAKEDigest = new SHAKEDigest(256);
        byte[] byArray3 = new byte[114];
        sHAKEDigest.update(byArray, n, 57);
        sHAKEDigest.doFinal(byArray3, 0, byArray3.length);
        byte[] byArray4 = new byte[57];
        Ed448.pruneScalar(byArray3, 0, byArray4);
        Ed448.scalarMultBaseEncodedVar(byArray4, byArray2, n2);
    }

    private static byte[] getWNAF(int[] nArray, int n) {
        int n2;
        int[] nArray2 = new int[28];
        int n3 = nArray2.length;
        int n4 = 0;
        int n5 = 14;
        while (--n5 >= 0) {
            n2 = nArray[n5];
            nArray2[--n3] = n2 >>> 16 | n4 << 16;
            nArray2[--n3] = n4 = n2;
        }
        byte[] byArray = new byte[448];
        n4 = 1 << n;
        n5 = n4 - 1;
        n2 = n4 >>> 1;
        int n6 = 0;
        int n7 = 0;
        int n8 = 0;
        while (n8 < nArray2.length) {
            int n9 = nArray2[n8];
            while (n6 < 16) {
                int n10 = n9 >>> n6;
                int n11 = n10 & 1;
                if (n11 == n7) {
                    ++n6;
                    continue;
                }
                int n12 = (n10 & n5) + n7;
                n7 = n12 & n2;
                n12 -= n7 << 1;
                n7 >>>= n - 1;
                byArray[(n8 << 4) + n6] = (byte)n12;
                n6 += n;
            }
            ++n8;
            n6 -= 16;
        }
        return byArray;
    }

    private static void implSignVar(SHAKEDigest sHAKEDigest, byte[] byArray, byte[] byArray2, byte[] byArray3, int n, byte[] byArray4, byte[] byArray5, int n2, int n3, byte[] byArray6, int n4) {
        byte by = 0;
        Ed448.dom4(sHAKEDigest, by, byArray4);
        sHAKEDigest.update(byArray, 57, 57);
        sHAKEDigest.update(byArray5, n2, n3);
        sHAKEDigest.doFinal(byArray, 0, byArray.length);
        byte[] byArray7 = Ed448.reduceScalar(byArray);
        byte[] byArray8 = new byte[57];
        Ed448.scalarMultBaseEncodedVar(byArray7, byArray8, 0);
        Ed448.dom4(sHAKEDigest, by, byArray4);
        sHAKEDigest.update(byArray8, 0, 57);
        sHAKEDigest.update(byArray3, n, 57);
        sHAKEDigest.update(byArray5, n2, n3);
        sHAKEDigest.doFinal(byArray, 0, byArray.length);
        byte[] byArray9 = Ed448.reduceScalar(byArray);
        byte[] byArray10 = Ed448.calculateS(byArray7, byArray9, byArray2);
        System.arraycopy(byArray8, 0, byArray6, n4, 57);
        System.arraycopy(byArray10, 0, byArray6, n4 + 57, 57);
    }

    private static void pointAddVar(boolean bl, PointExt pointExt, PointExt pointExt2) {
        int[] nArray;
        int[] nArray2;
        int[] nArray3;
        int[] nArray4;
        int[] nArray5 = X448Field.create();
        int[] nArray6 = X448Field.create();
        int[] nArray7 = X448Field.create();
        int[] nArray8 = X448Field.create();
        int[] nArray9 = X448Field.create();
        int[] nArray10 = X448Field.create();
        int[] nArray11 = X448Field.create();
        int[] nArray12 = X448Field.create();
        if (bl) {
            nArray4 = nArray9;
            nArray3 = nArray6;
            nArray2 = nArray11;
            nArray = nArray10;
            X448Field.sub(pointExt.y, pointExt.x, nArray12);
        } else {
            nArray4 = nArray6;
            nArray3 = nArray9;
            nArray2 = nArray10;
            nArray = nArray11;
            X448Field.add(pointExt.y, pointExt.x, nArray12);
        }
        X448Field.mul(pointExt.z, pointExt2.z, nArray5);
        X448Field.sqr(nArray5, nArray6);
        X448Field.mul(pointExt.x, pointExt2.x, nArray7);
        X448Field.mul(pointExt.y, pointExt2.y, nArray8);
        X448Field.mul(nArray7, nArray8, nArray9);
        X448Field.mul(nArray9, 39081, nArray9);
        X448Field.add(nArray6, nArray9, nArray2);
        X448Field.sub(nArray6, nArray9, nArray);
        X448Field.add(pointExt2.x, pointExt2.y, nArray9);
        X448Field.mul(nArray12, nArray9, nArray12);
        X448Field.add(nArray8, nArray7, nArray4);
        X448Field.sub(nArray8, nArray7, nArray3);
        X448Field.carry(nArray4);
        X448Field.sub(nArray12, nArray6, nArray12);
        X448Field.mul(nArray12, nArray5, nArray12);
        X448Field.mul(nArray9, nArray5, nArray9);
        X448Field.mul(nArray10, nArray12, pointExt2.x);
        X448Field.mul(nArray9, nArray11, pointExt2.y);
        X448Field.mul(nArray10, nArray11, pointExt2.z);
    }

    private static void pointAddPrecomp(PointPrecomp pointPrecomp, PointExt pointExt) {
        int[] nArray = X448Field.create();
        int[] nArray2 = X448Field.create();
        int[] nArray3 = X448Field.create();
        int[] nArray4 = X448Field.create();
        int[] nArray5 = X448Field.create();
        int[] nArray6 = X448Field.create();
        int[] nArray7 = X448Field.create();
        X448Field.sqr(pointExt.z, nArray);
        X448Field.mul(pointPrecomp.x, pointExt.x, nArray2);
        X448Field.mul(pointPrecomp.y, pointExt.y, nArray3);
        X448Field.mul(nArray2, nArray3, nArray4);
        X448Field.mul(nArray4, 39081, nArray4);
        X448Field.add(nArray, nArray4, nArray5);
        X448Field.sub(nArray, nArray4, nArray6);
        X448Field.add(pointPrecomp.x, pointPrecomp.y, nArray);
        X448Field.add(pointExt.x, pointExt.y, nArray4);
        X448Field.mul(nArray, nArray4, nArray7);
        X448Field.add(nArray3, nArray2, nArray);
        X448Field.sub(nArray3, nArray2, nArray4);
        X448Field.carry(nArray);
        X448Field.sub(nArray7, nArray, nArray7);
        X448Field.mul(nArray7, pointExt.z, nArray7);
        X448Field.mul(nArray4, pointExt.z, nArray4);
        X448Field.mul(nArray5, nArray7, pointExt.x);
        X448Field.mul(nArray4, nArray6, pointExt.y);
        X448Field.mul(nArray5, nArray6, pointExt.z);
    }

    private static PointExt pointCopy(PointExt pointExt) {
        PointExt pointExt2 = new PointExt();
        X448Field.copy(pointExt.x, 0, pointExt2.x, 0);
        X448Field.copy(pointExt.y, 0, pointExt2.y, 0);
        X448Field.copy(pointExt.z, 0, pointExt2.z, 0);
        return pointExt2;
    }

    private static void pointDouble(PointExt pointExt) {
        int[] nArray = X448Field.create();
        int[] nArray2 = X448Field.create();
        int[] nArray3 = X448Field.create();
        int[] nArray4 = X448Field.create();
        int[] nArray5 = X448Field.create();
        int[] nArray6 = X448Field.create();
        X448Field.add(pointExt.x, pointExt.y, nArray);
        X448Field.sqr(nArray, nArray);
        X448Field.sqr(pointExt.x, nArray2);
        X448Field.sqr(pointExt.y, nArray3);
        X448Field.add(nArray2, nArray3, nArray4);
        X448Field.carry(nArray4);
        X448Field.sqr(pointExt.z, nArray5);
        X448Field.add(nArray5, nArray5, nArray5);
        X448Field.carry(nArray5);
        X448Field.sub(nArray4, nArray5, nArray6);
        X448Field.sub(nArray, nArray4, nArray);
        X448Field.sub(nArray2, nArray3, nArray2);
        X448Field.mul(nArray, nArray6, pointExt.x);
        X448Field.mul(nArray4, nArray2, pointExt.y);
        X448Field.mul(nArray4, nArray6, pointExt.z);
    }

    private static void pointExtendXY(PointExt pointExt) {
        X448Field.one(pointExt.z);
    }

    private static void pointLookup(int n, int n2, PointPrecomp pointPrecomp) {
        int n3 = n * 16 * 2 * 16;
        for (int i = 0; i < 16; ++i) {
            int n4 = (i ^ n2) - 1 >> 31;
            Nat.cmov(16, n4, precompBase, n3, pointPrecomp.x, 0);
            Nat.cmov(16, n4, precompBase, n3 += 16, pointPrecomp.y, 0);
            n3 += 16;
        }
    }

    private static PointExt[] pointPrecompVar(PointExt pointExt, int n) {
        PointExt pointExt2 = Ed448.pointCopy(pointExt);
        Ed448.pointDouble(pointExt2);
        PointExt[] pointExtArray = new PointExt[n];
        pointExtArray[0] = Ed448.pointCopy(pointExt);
        for (int i = 1; i < n; ++i) {
            pointExtArray[i] = Ed448.pointCopy(pointExtArray[i - 1]);
            Ed448.pointAddVar(false, pointExt2, pointExtArray[i]);
        }
        return pointExtArray;
    }

    private static void pointSetNeutral(PointExt pointExt) {
        X448Field.zero(pointExt.x);
        X448Field.one(pointExt.y);
        X448Field.one(pointExt.z);
    }

    public static synchronized void precompute() {
        if (precompBase != null) {
            return;
        }
        PointExt pointExt = new PointExt();
        X448Field.copy(B_x, 0, pointExt.x, 0);
        X448Field.copy(B_y, 0, pointExt.y, 0);
        Ed448.pointExtendXY(pointExt);
        precompBaseTable = Ed448.pointPrecompVar(pointExt, 32);
        precompBase = new int[2560];
        int n = 0;
        for (int i = 0; i < 5; ++i) {
            int n2;
            int n3;
            PointExt[] pointExtArray = new PointExt[5];
            PointExt pointExt2 = new PointExt();
            Ed448.pointSetNeutral(pointExt2);
            for (int j = 0; j < 5; ++j) {
                Ed448.pointAddVar(true, pointExt, pointExt2);
                Ed448.pointDouble(pointExt);
                pointExtArray[j] = Ed448.pointCopy(pointExt);
                for (n3 = 1; n3 < 18; ++n3) {
                    Ed448.pointDouble(pointExt);
                }
            }
            PointExt[] pointExtArray2 = new PointExt[16];
            n3 = 0;
            pointExtArray2[n3++] = pointExt2;
            for (n2 = 0; n2 < 4; ++n2) {
                int n4 = 1 << n2;
                for (int j = 0; j < n4; ++j) {
                    pointExtArray2[n3] = Ed448.pointCopy(pointExtArray2[n3 - n4]);
                    Ed448.pointAddVar(false, pointExtArray[n2], pointExtArray2[n3++]);
                }
            }
            for (n2 = 0; n2 < 16; ++n2) {
                PointExt pointExt3 = pointExtArray2[n2];
                X448Field.inv(pointExt3.z, pointExt3.z);
                X448Field.mul(pointExt3.x, pointExt3.z, pointExt3.x);
                X448Field.mul(pointExt3.y, pointExt3.z, pointExt3.y);
                X448Field.copy(pointExt3.x, 0, precompBase, n);
                X448Field.copy(pointExt3.y, 0, precompBase, n += 16);
                n += 16;
            }
        }
    }

    private static void pruneScalar(byte[] byArray, int n, byte[] byArray2) {
        System.arraycopy(byArray, n, byArray2, 0, 57);
        byArray2[0] = (byte)(byArray2[0] & 0xFC);
        byArray2[55] = (byte)(byArray2[55] | 0x80);
        byArray2[56] = (byte)(byArray2[56] & 0);
    }

    private static byte[] reduceScalar(byte[] byArray) {
        long l = (long)Ed448.decode32(byArray, 0) & 0xFFFFFFFFL;
        long l2 = (long)(Ed448.decode24(byArray, 4) << 4) & 0xFFFFFFFFL;
        long l3 = (long)Ed448.decode32(byArray, 7) & 0xFFFFFFFFL;
        long l4 = (long)(Ed448.decode24(byArray, 11) << 4) & 0xFFFFFFFFL;
        long l5 = (long)Ed448.decode32(byArray, 14) & 0xFFFFFFFFL;
        long l6 = (long)(Ed448.decode24(byArray, 18) << 4) & 0xFFFFFFFFL;
        long l7 = (long)Ed448.decode32(byArray, 21) & 0xFFFFFFFFL;
        long l8 = (long)(Ed448.decode24(byArray, 25) << 4) & 0xFFFFFFFFL;
        long l9 = (long)Ed448.decode32(byArray, 28) & 0xFFFFFFFFL;
        long l10 = (long)(Ed448.decode24(byArray, 32) << 4) & 0xFFFFFFFFL;
        long l11 = (long)Ed448.decode32(byArray, 35) & 0xFFFFFFFFL;
        long l12 = (long)(Ed448.decode24(byArray, 39) << 4) & 0xFFFFFFFFL;
        long l13 = (long)Ed448.decode32(byArray, 42) & 0xFFFFFFFFL;
        long l14 = (long)(Ed448.decode24(byArray, 46) << 4) & 0xFFFFFFFFL;
        long l15 = (long)Ed448.decode32(byArray, 49) & 0xFFFFFFFFL;
        long l16 = (long)(Ed448.decode24(byArray, 53) << 4) & 0xFFFFFFFFL;
        long l17 = (long)Ed448.decode32(byArray, 56) & 0xFFFFFFFFL;
        long l18 = (long)(Ed448.decode24(byArray, 60) << 4) & 0xFFFFFFFFL;
        long l19 = (long)Ed448.decode32(byArray, 63) & 0xFFFFFFFFL;
        long l20 = (long)(Ed448.decode24(byArray, 67) << 4) & 0xFFFFFFFFL;
        long l21 = (long)Ed448.decode32(byArray, 70) & 0xFFFFFFFFL;
        long l22 = (long)(Ed448.decode24(byArray, 74) << 4) & 0xFFFFFFFFL;
        long l23 = (long)Ed448.decode32(byArray, 77) & 0xFFFFFFFFL;
        long l24 = (long)(Ed448.decode24(byArray, 81) << 4) & 0xFFFFFFFFL;
        long l25 = (long)Ed448.decode32(byArray, 84) & 0xFFFFFFFFL;
        long l26 = (long)(Ed448.decode24(byArray, 88) << 4) & 0xFFFFFFFFL;
        long l27 = (long)Ed448.decode32(byArray, 91) & 0xFFFFFFFFL;
        long l28 = (long)(Ed448.decode24(byArray, 95) << 4) & 0xFFFFFFFFL;
        long l29 = (long)Ed448.decode32(byArray, 98) & 0xFFFFFFFFL;
        long l30 = (long)(Ed448.decode24(byArray, 102) << 4) & 0xFFFFFFFFL;
        long l31 = (long)Ed448.decode32(byArray, 105) & 0xFFFFFFFFL;
        long l32 = (long)(Ed448.decode24(byArray, 109) << 4) & 0xFFFFFFFFL;
        long l33 = (long)Ed448.decode16(byArray, 112) & 0xFFFFFFFFL;
        l17 += l33 * 43969588L;
        l18 += l33 * 30366549L;
        l19 += l33 * 163752818L;
        l20 += l33 * 258169998L;
        l21 += l33 * 96434764L;
        l22 += l33 * 227822194L;
        l23 += l33 * 149865618L;
        l24 += l33 * 550336261L;
        l32 += l31 >>> 28;
        l31 &= 0xFFFFFFFL;
        l16 += l32 * 43969588L;
        l17 += l32 * 30366549L;
        l18 += l32 * 163752818L;
        l19 += l32 * 258169998L;
        l20 += l32 * 96434764L;
        l21 += l32 * 227822194L;
        l22 += l32 * 149865618L;
        l23 += l32 * 550336261L;
        l15 += l31 * 43969588L;
        l16 += l31 * 30366549L;
        l17 += l31 * 163752818L;
        l18 += l31 * 258169998L;
        l19 += l31 * 96434764L;
        l20 += l31 * 227822194L;
        l21 += l31 * 149865618L;
        l22 += l31 * 550336261L;
        l30 += l29 >>> 28;
        l29 &= 0xFFFFFFFL;
        l14 += l30 * 43969588L;
        l15 += l30 * 30366549L;
        l16 += l30 * 163752818L;
        l17 += l30 * 258169998L;
        l18 += l30 * 96434764L;
        l19 += l30 * 227822194L;
        l20 += l30 * 149865618L;
        l21 += l30 * 550336261L;
        l13 += l29 * 43969588L;
        l14 += l29 * 30366549L;
        l15 += l29 * 163752818L;
        l16 += l29 * 258169998L;
        l17 += l29 * 96434764L;
        l18 += l29 * 227822194L;
        l19 += l29 * 149865618L;
        l20 += l29 * 550336261L;
        l28 += l27 >>> 28;
        l27 &= 0xFFFFFFFL;
        l12 += l28 * 43969588L;
        l13 += l28 * 30366549L;
        l14 += l28 * 163752818L;
        l15 += l28 * 258169998L;
        l16 += l28 * 96434764L;
        l17 += l28 * 227822194L;
        l18 += l28 * 149865618L;
        l19 += l28 * 550336261L;
        l11 += l27 * 43969588L;
        l12 += l27 * 30366549L;
        l13 += l27 * 163752818L;
        l14 += l27 * 258169998L;
        l15 += l27 * 96434764L;
        l16 += l27 * 227822194L;
        l17 += l27 * 149865618L;
        l18 += l27 * 550336261L;
        l26 += l25 >>> 28;
        l25 &= 0xFFFFFFFL;
        l10 += l26 * 43969588L;
        l11 += l26 * 30366549L;
        l12 += l26 * 163752818L;
        l13 += l26 * 258169998L;
        l14 += l26 * 96434764L;
        l15 += l26 * 227822194L;
        l16 += l26 * 149865618L;
        l17 += l26 * 550336261L;
        l22 += l21 >>> 28;
        l21 &= 0xFFFFFFFL;
        l23 += l22 >>> 28;
        l22 &= 0xFFFFFFFL;
        l24 += l23 >>> 28;
        l23 &= 0xFFFFFFFL;
        l25 += l24 >>> 28;
        l24 &= 0xFFFFFFFL;
        l9 += l25 * 43969588L;
        l10 += l25 * 30366549L;
        l11 += l25 * 163752818L;
        l12 += l25 * 258169998L;
        l13 += l25 * 96434764L;
        l14 += l25 * 227822194L;
        l15 += l25 * 149865618L;
        l16 += l25 * 550336261L;
        l8 += l24 * 43969588L;
        l9 += l24 * 30366549L;
        l10 += l24 * 163752818L;
        l11 += l24 * 258169998L;
        l12 += l24 * 96434764L;
        l13 += l24 * 227822194L;
        l14 += l24 * 149865618L;
        l15 += l24 * 550336261L;
        l7 += l23 * 43969588L;
        l8 += l23 * 30366549L;
        l9 += l23 * 163752818L;
        l10 += l23 * 258169998L;
        l11 += l23 * 96434764L;
        l12 += l23 * 227822194L;
        l13 += l23 * 149865618L;
        l14 += l23 * 550336261L;
        l19 += l18 >>> 28;
        l18 &= 0xFFFFFFFL;
        l20 += l19 >>> 28;
        l19 &= 0xFFFFFFFL;
        l21 += l20 >>> 28;
        l20 &= 0xFFFFFFFL;
        l22 += l21 >>> 28;
        l21 &= 0xFFFFFFFL;
        l6 += l22 * 43969588L;
        l7 += l22 * 30366549L;
        l8 += l22 * 163752818L;
        l9 += l22 * 258169998L;
        l10 += l22 * 96434764L;
        l11 += l22 * 227822194L;
        l12 += l22 * 149865618L;
        l13 += l22 * 550336261L;
        l5 += l21 * 43969588L;
        l6 += l21 * 30366549L;
        l7 += l21 * 163752818L;
        l8 += l21 * 258169998L;
        l9 += l21 * 96434764L;
        l10 += l21 * 227822194L;
        l11 += l21 * 149865618L;
        l12 += l21 * 550336261L;
        l4 += l20 * 43969588L;
        l5 += l20 * 30366549L;
        l6 += l20 * 163752818L;
        l7 += l20 * 258169998L;
        l8 += l20 * 96434764L;
        l9 += l20 * 227822194L;
        l10 += l20 * 149865618L;
        l11 += l20 * 550336261L;
        l16 += l15 >>> 28;
        l15 &= 0xFFFFFFFL;
        l17 += l16 >>> 28;
        l16 &= 0xFFFFFFFL;
        l18 += l17 >>> 28;
        l17 &= 0xFFFFFFFL;
        l19 += l18 >>> 28;
        l18 &= 0xFFFFFFFL;
        l3 += l19 * 43969588L;
        l4 += l19 * 30366549L;
        l5 += l19 * 163752818L;
        l6 += l19 * 258169998L;
        l7 += l19 * 96434764L;
        l8 += l19 * 227822194L;
        l9 += l19 * 149865618L;
        l10 += l19 * 550336261L;
        l2 += l18 * 43969588L;
        l3 += l18 * 30366549L;
        l4 += l18 * 163752818L;
        l5 += l18 * 258169998L;
        l6 += l18 * 96434764L;
        l7 += l18 * 227822194L;
        l8 += l18 * 149865618L;
        l9 += l18 * 550336261L;
        l17 *= 4L;
        l17 += l16 >>> 26;
        l16 &= 0x3FFFFFFL;
        l += ++l17 * 78101261L;
        l2 += l17 * 141809365L;
        l3 += l17 * 175155932L;
        l4 += l17 * 64542499L;
        l5 += l17 * 158326419L;
        l6 += l17 * 191173276L;
        l7 += l17 * 104575268L;
        l8 += l17 * 137584065L;
        l2 += l >>> 28;
        l &= 0xFFFFFFFL;
        l3 += l2 >>> 28;
        l2 &= 0xFFFFFFFL;
        l4 += l3 >>> 28;
        l3 &= 0xFFFFFFFL;
        l5 += l4 >>> 28;
        l4 &= 0xFFFFFFFL;
        l6 += l5 >>> 28;
        l5 &= 0xFFFFFFFL;
        l7 += l6 >>> 28;
        l6 &= 0xFFFFFFFL;
        l8 += l7 >>> 28;
        l7 &= 0xFFFFFFFL;
        l9 += l8 >>> 28;
        l8 &= 0xFFFFFFFL;
        l10 += l9 >>> 28;
        l9 &= 0xFFFFFFFL;
        l11 += l10 >>> 28;
        l10 &= 0xFFFFFFFL;
        l12 += l11 >>> 28;
        l11 &= 0xFFFFFFFL;
        l13 += l12 >>> 28;
        l12 &= 0xFFFFFFFL;
        l14 += l13 >>> 28;
        l13 &= 0xFFFFFFFL;
        l15 += l14 >>> 28;
        l14 &= 0xFFFFFFFL;
        l16 += l15 >>> 28;
        l15 &= 0xFFFFFFFL;
        l17 = l16 >>> 26;
        l16 &= 0x3FFFFFFL;
        l -= --l17 & 0x4A7BB0DL;
        l2 -= l17 & 0x873D6D5L;
        l3 -= l17 & 0xA70AADCL;
        l4 -= l17 & 0x3D8D723L;
        l5 -= l17 & 0x96FDE93L;
        l6 -= l17 & 0xB65129CL;
        l7 -= l17 & 0x63BB124L;
        l8 -= l17 & 0x8335DC1L;
        l2 += l >> 28;
        l &= 0xFFFFFFFL;
        l3 += l2 >> 28;
        l2 &= 0xFFFFFFFL;
        l4 += l3 >> 28;
        l3 &= 0xFFFFFFFL;
        l5 += l4 >> 28;
        l4 &= 0xFFFFFFFL;
        l6 += l5 >> 28;
        l5 &= 0xFFFFFFFL;
        l7 += l6 >> 28;
        l6 &= 0xFFFFFFFL;
        l8 += l7 >> 28;
        l7 &= 0xFFFFFFFL;
        l9 += l8 >> 28;
        l8 &= 0xFFFFFFFL;
        l10 += l9 >> 28;
        l9 &= 0xFFFFFFFL;
        l11 += l10 >> 28;
        l10 &= 0xFFFFFFFL;
        l12 += l11 >> 28;
        l11 &= 0xFFFFFFFL;
        l13 += l12 >> 28;
        l12 &= 0xFFFFFFFL;
        l14 += l13 >> 28;
        l13 &= 0xFFFFFFFL;
        l15 += l14 >> 28;
        l14 &= 0xFFFFFFFL;
        l16 += l15 >> 28;
        l15 &= 0xFFFFFFFL;
        byte[] byArray2 = new byte[57];
        Ed448.encode56(l | l2 << 28, byArray2, 0);
        Ed448.encode56(l3 | l4 << 28, byArray2, 7);
        Ed448.encode56(l5 | l6 << 28, byArray2, 14);
        Ed448.encode56(l7 | l8 << 28, byArray2, 21);
        Ed448.encode56(l9 | l10 << 28, byArray2, 28);
        Ed448.encode56(l11 | l12 << 28, byArray2, 35);
        Ed448.encode56(l13 | l14 << 28, byArray2, 42);
        Ed448.encode56(l15 | l16 << 28, byArray2, 49);
        return byArray2;
    }

    private static void scalarMultBase(byte[] byArray, PointExt pointExt) {
        Ed448.precompute();
        Ed448.pointSetNeutral(pointExt);
        int[] nArray = new int[15];
        Ed448.decodeScalar(byArray, 0, nArray);
        nArray[14] = 4 + Nat.cadd(14, ~nArray[0] & 1, nArray, L, nArray);
        Nat.shiftDownBit(nArray.length, nArray, 0);
        PointPrecomp pointPrecomp = new PointPrecomp();
        int n = 17;
        while (true) {
            int n2 = n;
            for (int i = 0; i < 5; ++i) {
                int n3;
                int n4;
                int n5 = 0;
                for (n4 = 0; n4 < 5; ++n4) {
                    n3 = nArray[n2 >>> 5] >>> (n2 & 0x1F) & 1;
                    n5 |= n3 << n4;
                    n2 += 18;
                }
                n4 = n5 >>> 4 & 1;
                n3 = (n5 ^ -n4) & 0xF;
                Ed448.pointLookup(i, n3, pointPrecomp);
                X448Field.cnegate(n4, pointPrecomp.x);
                Ed448.pointAddPrecomp(pointPrecomp, pointExt);
            }
            if (--n < 0) break;
            Ed448.pointDouble(pointExt);
        }
    }

    private static void scalarMultBaseEncodedVar(byte[] byArray, byte[] byArray2, int n) {
        PointExt pointExt = new PointExt();
        Ed448.scalarMultBase(byArray, pointExt);
        Ed448.encodePoint(pointExt, byArray2, n);
    }

    private static void scalarMultStraussVar(int[] nArray, int[] nArray2, PointExt pointExt, PointExt pointExt2) {
        int n;
        Ed448.precompute();
        byte[] byArray = Ed448.getWNAF(nArray, 7);
        byte[] byArray2 = Ed448.getWNAF(nArray2, 5);
        PointExt[] pointExtArray = Ed448.pointPrecompVar(pointExt, 8);
        Ed448.pointSetNeutral(pointExt2);
        for (n = 447; n > 0 && (byArray[n] | byArray2[n]) == 0; --n) {
        }
        while (true) {
            int n2;
            int n3;
            byte by;
            if ((by = byArray[n]) != 0) {
                n3 = by >> 31;
                n2 = (by ^ n3) >>> 1;
                Ed448.pointAddVar(n3 != 0, precompBaseTable[n2], pointExt2);
            }
            if ((n3 = byArray2[n]) != 0) {
                n2 = n3 >> 31;
                int n4 = (n3 ^ n2) >>> 1;
                Ed448.pointAddVar(n2 != 0, pointExtArray[n4], pointExt2);
            }
            if (--n < 0) break;
            Ed448.pointDouble(pointExt2);
        }
    }

    public static void sign(byte[] byArray, int n, byte[] byArray2, byte[] byArray3, int n2, int n3, byte[] byArray4, int n4) {
        if (!Ed448.checkContextVar(byArray2)) {
            throw new IllegalArgumentException("ctx");
        }
        SHAKEDigest sHAKEDigest = new SHAKEDigest(256);
        byte[] byArray5 = new byte[114];
        sHAKEDigest.update(byArray, n, 57);
        sHAKEDigest.doFinal(byArray5, 0, byArray5.length);
        byte[] byArray6 = new byte[57];
        Ed448.pruneScalar(byArray5, 0, byArray6);
        byte[] byArray7 = new byte[57];
        Ed448.scalarMultBaseEncodedVar(byArray6, byArray7, 0);
        Ed448.implSignVar(sHAKEDigest, byArray5, byArray6, byArray7, 0, byArray2, byArray3, n2, n3, byArray4, n4);
    }

    public static void sign(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, byte[] byArray4, int n3, int n4, byte[] byArray5, int n5) {
        if (!Ed448.checkContextVar(byArray3)) {
            throw new IllegalArgumentException("ctx");
        }
        SHAKEDigest sHAKEDigest = new SHAKEDigest(256);
        byte[] byArray6 = new byte[114];
        sHAKEDigest.update(byArray, n, 57);
        sHAKEDigest.doFinal(byArray6, 0, byArray6.length);
        byte[] byArray7 = new byte[57];
        Ed448.pruneScalar(byArray6, 0, byArray7);
        Ed448.implSignVar(sHAKEDigest, byArray6, byArray7, byArray2, n2, byArray3, byArray4, n3, n4, byArray5, n5);
    }

    public static boolean verify(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, byte[] byArray4, int n3, int n4) {
        if (!Ed448.checkContextVar(byArray3)) {
            throw new IllegalArgumentException("ctx");
        }
        byte[] byArray5 = Arrays.copyOfRange(byArray, n, n + 57);
        byte[] byArray6 = Arrays.copyOfRange(byArray, n + 57, n + 114);
        if (!Ed448.checkPointVar(byArray5)) {
            return false;
        }
        if (!Ed448.checkScalarVar(byArray6)) {
            return false;
        }
        PointExt pointExt = new PointExt();
        if (!Ed448.decodePointVar(byArray2, n2, true, pointExt)) {
            return false;
        }
        byte by = 0;
        SHAKEDigest sHAKEDigest = new SHAKEDigest(256);
        byte[] byArray7 = new byte[114];
        Ed448.dom4(sHAKEDigest, by, byArray3);
        sHAKEDigest.update(byArray5, 0, 57);
        sHAKEDigest.update(byArray2, n2, 57);
        sHAKEDigest.update(byArray4, n3, n4);
        sHAKEDigest.doFinal(byArray7, 0, byArray7.length);
        byte[] byArray8 = Ed448.reduceScalar(byArray7);
        int[] nArray = new int[14];
        Ed448.decodeScalar(byArray6, 0, nArray);
        int[] nArray2 = new int[14];
        Ed448.decodeScalar(byArray8, 0, nArray2);
        PointExt pointExt2 = new PointExt();
        Ed448.scalarMultStraussVar(nArray, nArray2, pointExt, pointExt2);
        byte[] byArray9 = new byte[57];
        Ed448.encodePoint(pointExt2, byArray9, 0);
        return Arrays.areEqual(byArray9, byArray5);
    }

    private static class PointExt {
        int[] x = X448Field.create();
        int[] y = X448Field.create();
        int[] z = X448Field.create();

        private PointExt() {
        }
    }

    private static class PointPrecomp {
        int[] x = X448Field.create();
        int[] y = X448Field.create();

        private PointPrecomp() {
        }
    }
}

