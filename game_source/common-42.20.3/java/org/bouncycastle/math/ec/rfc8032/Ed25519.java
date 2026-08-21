/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.math.ec.rfc8032;

import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.math.ec.rfc7748.X25519Field;
import org.bouncycastle.math.raw.Interleave;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.math.raw.Nat256;
import org.bouncycastle.util.Arrays;

public abstract class Ed25519 {
    private static final long M28L = 0xFFFFFFFL;
    private static final long M32L = 0xFFFFFFFFL;
    private static final int POINT_BYTES = 32;
    private static final int SCALAR_INTS = 8;
    private static final int SCALAR_BYTES = 32;
    public static final int PUBLIC_KEY_SIZE = 32;
    public static final int SECRET_KEY_SIZE = 32;
    public static final int SIGNATURE_SIZE = 64;
    private static final int[] P = new int[]{-19, -1, -1, -1, -1, -1, -1, Integer.MAX_VALUE};
    private static final int[] L = new int[]{1559614445, 1477600026, -1560830762, 350157278, 0, 0, 0, 0x10000000};
    private static final int L0 = -50998291;
    private static final int L1 = 19280294;
    private static final int L2 = 127719000;
    private static final int L3 = -6428113;
    private static final int L4 = 5343;
    private static final int[] B_x = new int[]{52811034, 25909283, 8072341, 50637101, 13785486, 30858332, 20483199, 20966410, 43936626, 4379245};
    private static final int[] B_y = new int[]{40265304, 0x1999999, 0x666666, 0x3333333, 0xCCCCCC, 0x2666666, 0x1999999, 0x666666, 0x3333333, 0xCCCCCC};
    private static final int[] C_d = new int[]{56195235, 47411844, 25868126, 40503822, 57364, 58321048, 30416477, 31930572, 57760639, 10749657};
    private static final int[] C_d2 = new int[]{45281625, 27714825, 18181821, 0xD4141D, 114729, 49533232, 60832955, 30306712, 48412415, 4722099};
    private static final int[] C_d4 = new int[]{23454386, 55429651, 2809210, 27797563, 229458, 31957600, 54557047, 27058993, 29715967, 9444199};
    private static final int WNAF_WIDTH_BASE = 7;
    private static final int PRECOMP_BLOCKS = 8;
    private static final int PRECOMP_TEETH = 4;
    private static final int PRECOMP_SPACING = 8;
    private static final int PRECOMP_POINTS = 8;
    private static final int PRECOMP_MASK = 7;
    private static PointExt[] precompBaseTable = null;
    private static int[] precompBase = null;

    private static byte[] calculateS(byte[] byArray, byte[] byArray2, byte[] byArray3) {
        int[] nArray = new int[16];
        Ed25519.decodeScalar(byArray, 0, nArray);
        int[] nArray2 = new int[8];
        Ed25519.decodeScalar(byArray2, 0, nArray2);
        int[] nArray3 = new int[8];
        Ed25519.decodeScalar(byArray3, 0, nArray3);
        Nat256.mulAddTo(nArray2, nArray3, nArray);
        byte[] byArray4 = new byte[64];
        for (int i = 0; i < nArray.length; ++i) {
            Ed25519.encode32(nArray[i], byArray4, i * 4);
        }
        return Ed25519.reduceScalar(byArray4);
    }

    private static boolean checkPointVar(byte[] byArray) {
        int[] nArray = new int[8];
        Ed25519.decode32(byArray, 0, nArray, 0, 8);
        nArray[7] = nArray[7] & Integer.MAX_VALUE;
        return !Nat256.gte(nArray, P);
    }

    private static boolean checkScalarVar(byte[] byArray) {
        int[] nArray = new int[8];
        Ed25519.decodeScalar(byArray, 0, nArray);
        return !Nat256.gte(nArray, L);
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
            nArray[n2 + i] = Ed25519.decode32(byArray, n + i * 4);
        }
    }

    private static boolean decodePointVar(byte[] byArray, int n, boolean bl, PointExt pointExt) {
        byte[] byArray2 = Arrays.copyOfRange(byArray, n, n + 32);
        if (!Ed25519.checkPointVar(byArray2)) {
            return false;
        }
        int n2 = (byArray2[31] & 0x80) >>> 7;
        byArray2[31] = (byte)(byArray2[31] & 0x7F);
        X25519Field.decode(byArray2, 0, pointExt.y);
        int[] nArray = X25519Field.create();
        int[] nArray2 = X25519Field.create();
        X25519Field.sqr(pointExt.y, nArray);
        X25519Field.mul(C_d, nArray, nArray2);
        X25519Field.subOne(nArray);
        X25519Field.addOne(nArray2);
        if (!X25519Field.sqrtRatioVar(nArray, nArray2, pointExt.x)) {
            return false;
        }
        X25519Field.normalize(pointExt.x);
        if (n2 == 1 && X25519Field.isZeroVar(pointExt.x)) {
            return false;
        }
        if (bl ^ n2 != (pointExt.x[0] & 1)) {
            X25519Field.negate(pointExt.x, pointExt.x);
        }
        Ed25519.pointExtendXY(pointExt);
        return true;
    }

    private static void decodeScalar(byte[] byArray, int n, int[] nArray) {
        Ed25519.decode32(byArray, n, nArray, 0, 8);
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
        Ed25519.encode32((int)l, byArray, n);
        Ed25519.encode24((int)(l >>> 32), byArray, n + 4);
    }

    private static void encodePoint(PointExt pointExt, byte[] byArray, int n) {
        int[] nArray = X25519Field.create();
        int[] nArray2 = X25519Field.create();
        X25519Field.inv(pointExt.z, nArray2);
        X25519Field.mul(pointExt.x, nArray2, nArray);
        X25519Field.mul(pointExt.y, nArray2, nArray2);
        X25519Field.normalize(nArray);
        X25519Field.normalize(nArray2);
        X25519Field.encode(nArray2, byArray, n);
        int n2 = n + 32 - 1;
        byArray[n2] = (byte)(byArray[n2] | (nArray[0] & 1) << 7);
    }

    public static void generatePublicKey(byte[] byArray, int n, byte[] byArray2, int n2) {
        SHA512Digest sHA512Digest = new SHA512Digest();
        byte[] byArray3 = new byte[sHA512Digest.getDigestSize()];
        sHA512Digest.update(byArray, n, 32);
        sHA512Digest.doFinal(byArray3, 0);
        byte[] byArray4 = new byte[32];
        Ed25519.pruneScalar(byArray3, 0, byArray4);
        Ed25519.scalarMultBaseEncoded(byArray4, byArray2, n2);
    }

    private static byte[] getWNAF(int[] nArray, int n) {
        int n2;
        int[] nArray2 = new int[16];
        int n3 = nArray2.length;
        int n4 = 0;
        int n5 = 8;
        while (--n5 >= 0) {
            n2 = nArray[n5];
            nArray2[--n3] = n2 >>> 16 | n4 << 16;
            nArray2[--n3] = n4 = n2;
        }
        byte[] byArray = new byte[256];
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

    private static void implSign(SHA512Digest sHA512Digest, byte[] byArray, byte[] byArray2, byte[] byArray3, int n, byte[] byArray4, int n2, int n3, byte[] byArray5, int n4) {
        sHA512Digest.update(byArray, 32, 32);
        sHA512Digest.update(byArray4, n2, n3);
        sHA512Digest.doFinal(byArray, 0);
        byte[] byArray6 = Ed25519.reduceScalar(byArray);
        byte[] byArray7 = new byte[32];
        Ed25519.scalarMultBaseEncoded(byArray6, byArray7, 0);
        sHA512Digest.update(byArray7, 0, 32);
        sHA512Digest.update(byArray3, 0, 32);
        sHA512Digest.update(byArray4, n2, n3);
        sHA512Digest.doFinal(byArray, 0);
        byte[] byArray8 = Ed25519.reduceScalar(byArray);
        byte[] byArray9 = Ed25519.calculateS(byArray6, byArray8, byArray2);
        System.arraycopy(byArray7, 0, byArray5, n4, 32);
        System.arraycopy(byArray9, 0, byArray5, n4 + 32, 32);
    }

    private static void pointAddVar(boolean bl, PointExt pointExt, PointExt pointExt2) {
        int[] nArray;
        int[] nArray2;
        int[] nArray3;
        int[] nArray4;
        int[] nArray5 = X25519Field.create();
        int[] nArray6 = X25519Field.create();
        int[] nArray7 = X25519Field.create();
        int[] nArray8 = X25519Field.create();
        int[] nArray9 = X25519Field.create();
        int[] nArray10 = X25519Field.create();
        int[] nArray11 = X25519Field.create();
        int[] nArray12 = X25519Field.create();
        if (bl) {
            nArray4 = nArray8;
            nArray3 = nArray7;
            nArray2 = nArray11;
            nArray = nArray10;
        } else {
            nArray4 = nArray7;
            nArray3 = nArray8;
            nArray2 = nArray10;
            nArray = nArray11;
        }
        X25519Field.apm(pointExt2.y, pointExt2.x, nArray6, nArray5);
        X25519Field.apm(pointExt.y, pointExt.x, nArray3, nArray4);
        X25519Field.mul(nArray5, nArray7, nArray5);
        X25519Field.mul(nArray6, nArray8, nArray6);
        X25519Field.mul(pointExt2.t, pointExt.t, nArray7);
        X25519Field.mul(nArray7, C_d2, nArray7);
        X25519Field.mul(pointExt2.z, pointExt.z, nArray8);
        X25519Field.add(nArray8, nArray8, nArray8);
        X25519Field.apm(nArray6, nArray5, nArray12, nArray9);
        X25519Field.apm(nArray8, nArray7, nArray, nArray2);
        X25519Field.carry(nArray);
        X25519Field.mul(nArray9, nArray10, pointExt2.x);
        X25519Field.mul(nArray11, nArray12, pointExt2.y);
        X25519Field.mul(nArray10, nArray11, pointExt2.z);
        X25519Field.mul(nArray9, nArray12, pointExt2.t);
    }

    private static void pointAddPrecomp(PointPrecomp pointPrecomp, PointExt pointExt) {
        int[] nArray = X25519Field.create();
        int[] nArray2 = X25519Field.create();
        int[] nArray3 = X25519Field.create();
        int[] nArray4 = X25519Field.create();
        int[] nArray5 = X25519Field.create();
        int[] nArray6 = X25519Field.create();
        int[] nArray7 = X25519Field.create();
        X25519Field.apm(pointExt.y, pointExt.x, nArray2, nArray);
        X25519Field.mul(nArray, pointPrecomp.ymx_h, nArray);
        X25519Field.mul(nArray2, pointPrecomp.ypx_h, nArray2);
        X25519Field.mul(pointExt.t, pointPrecomp.xyd, nArray3);
        X25519Field.apm(nArray2, nArray, nArray7, nArray4);
        X25519Field.apm(pointExt.z, nArray3, nArray6, nArray5);
        X25519Field.carry(nArray6);
        X25519Field.mul(nArray4, nArray5, pointExt.x);
        X25519Field.mul(nArray6, nArray7, pointExt.y);
        X25519Field.mul(nArray5, nArray6, pointExt.z);
        X25519Field.mul(nArray4, nArray7, pointExt.t);
    }

    private static PointExt pointCopy(PointExt pointExt) {
        PointExt pointExt2 = new PointExt();
        X25519Field.copy(pointExt.x, 0, pointExt2.x, 0);
        X25519Field.copy(pointExt.y, 0, pointExt2.y, 0);
        X25519Field.copy(pointExt.z, 0, pointExt2.z, 0);
        X25519Field.copy(pointExt.t, 0, pointExt2.t, 0);
        return pointExt2;
    }

    private static void pointDouble(PointExt pointExt) {
        int[] nArray = X25519Field.create();
        int[] nArray2 = X25519Field.create();
        int[] nArray3 = X25519Field.create();
        int[] nArray4 = X25519Field.create();
        int[] nArray5 = X25519Field.create();
        int[] nArray6 = X25519Field.create();
        int[] nArray7 = X25519Field.create();
        X25519Field.sqr(pointExt.x, nArray);
        X25519Field.sqr(pointExt.y, nArray2);
        X25519Field.sqr(pointExt.z, nArray3);
        X25519Field.add(nArray3, nArray3, nArray3);
        X25519Field.apm(nArray, nArray2, nArray7, nArray6);
        X25519Field.add(pointExt.x, pointExt.y, nArray4);
        X25519Field.sqr(nArray4, nArray4);
        X25519Field.sub(nArray7, nArray4, nArray4);
        X25519Field.add(nArray3, nArray6, nArray5);
        X25519Field.carry(nArray5);
        X25519Field.mul(nArray4, nArray5, pointExt.x);
        X25519Field.mul(nArray6, nArray7, pointExt.y);
        X25519Field.mul(nArray5, nArray6, pointExt.z);
        X25519Field.mul(nArray4, nArray7, pointExt.t);
    }

    private static void pointExtendXY(PointExt pointExt) {
        X25519Field.one(pointExt.z);
        X25519Field.mul(pointExt.x, pointExt.y, pointExt.t);
    }

    private static void pointLookup(int n, int n2, PointPrecomp pointPrecomp) {
        int n3 = n * 8 * 3 * 10;
        for (int i = 0; i < 8; ++i) {
            int n4 = (i ^ n2) - 1 >> 31;
            Nat.cmov(10, n4, precompBase, n3, pointPrecomp.ypx_h, 0);
            Nat.cmov(10, n4, precompBase, n3 += 10, pointPrecomp.ymx_h, 0);
            Nat.cmov(10, n4, precompBase, n3 += 10, pointPrecomp.xyd, 0);
            n3 += 10;
        }
    }

    private static PointExt[] pointPrecompVar(PointExt pointExt, int n) {
        PointExt pointExt2 = Ed25519.pointCopy(pointExt);
        Ed25519.pointDouble(pointExt2);
        PointExt[] pointExtArray = new PointExt[n];
        pointExtArray[0] = Ed25519.pointCopy(pointExt);
        for (int i = 1; i < n; ++i) {
            pointExtArray[i] = Ed25519.pointCopy(pointExtArray[i - 1]);
            Ed25519.pointAddVar(false, pointExt2, pointExtArray[i]);
        }
        return pointExtArray;
    }

    private static void pointSetNeutral(PointExt pointExt) {
        X25519Field.zero(pointExt.x);
        X25519Field.one(pointExt.y);
        X25519Field.one(pointExt.z);
        X25519Field.zero(pointExt.t);
    }

    public static synchronized void precompute() {
        if (precompBase != null) {
            return;
        }
        PointExt pointExt = new PointExt();
        X25519Field.copy(B_x, 0, pointExt.x, 0);
        X25519Field.copy(B_y, 0, pointExt.y, 0);
        Ed25519.pointExtendXY(pointExt);
        precompBaseTable = Ed25519.pointPrecompVar(pointExt, 32);
        precompBase = new int[1920];
        int n = 0;
        for (int i = 0; i < 8; ++i) {
            int n2;
            int n3;
            PointExt[] pointExtArray = new PointExt[4];
            PointExt pointExt2 = new PointExt();
            Ed25519.pointSetNeutral(pointExt2);
            for (int j = 0; j < 4; ++j) {
                Ed25519.pointAddVar(true, pointExt, pointExt2);
                Ed25519.pointDouble(pointExt);
                pointExtArray[j] = Ed25519.pointCopy(pointExt);
                for (n3 = 1; n3 < 8; ++n3) {
                    Ed25519.pointDouble(pointExt);
                }
            }
            PointExt[] pointExtArray2 = new PointExt[8];
            n3 = 0;
            pointExtArray2[n3++] = pointExt2;
            for (n2 = 0; n2 < 3; ++n2) {
                int n4 = 1 << n2;
                for (int j = 0; j < n4; ++j) {
                    pointExtArray2[n3] = Ed25519.pointCopy(pointExtArray2[n3 - n4]);
                    Ed25519.pointAddVar(false, pointExtArray[n2], pointExtArray2[n3++]);
                }
            }
            for (n2 = 0; n2 < 8; ++n2) {
                PointExt pointExt3 = pointExtArray2[n2];
                int[] nArray = X25519Field.create();
                int[] nArray2 = X25519Field.create();
                X25519Field.add(pointExt3.z, pointExt3.z, nArray);
                X25519Field.inv(nArray, nArray2);
                X25519Field.mul(pointExt3.x, nArray2, nArray);
                X25519Field.mul(pointExt3.y, nArray2, nArray2);
                PointPrecomp pointPrecomp = new PointPrecomp();
                X25519Field.apm(nArray2, nArray, pointPrecomp.ypx_h, pointPrecomp.ymx_h);
                X25519Field.mul(nArray, nArray2, pointPrecomp.xyd);
                X25519Field.mul(pointPrecomp.xyd, C_d4, pointPrecomp.xyd);
                X25519Field.normalize(pointPrecomp.ypx_h);
                X25519Field.normalize(pointPrecomp.ymx_h);
                X25519Field.copy(pointPrecomp.ypx_h, 0, precompBase, n);
                X25519Field.copy(pointPrecomp.ymx_h, 0, precompBase, n += 10);
                X25519Field.copy(pointPrecomp.xyd, 0, precompBase, n += 10);
                n += 10;
            }
        }
    }

    private static void pruneScalar(byte[] byArray, int n, byte[] byArray2) {
        System.arraycopy(byArray, n, byArray2, 0, 32);
        byArray2[0] = (byte)(byArray2[0] & 0xF8);
        byArray2[31] = (byte)(byArray2[31] & 0x7F);
        byArray2[31] = (byte)(byArray2[31] | 0x40);
    }

    private static byte[] reduceScalar(byte[] byArray) {
        long l = (long)Ed25519.decode32(byArray, 0) & 0xFFFFFFFFL;
        long l2 = (long)(Ed25519.decode24(byArray, 4) << 4) & 0xFFFFFFFFL;
        long l3 = (long)Ed25519.decode32(byArray, 7) & 0xFFFFFFFFL;
        long l4 = (long)(Ed25519.decode24(byArray, 11) << 4) & 0xFFFFFFFFL;
        long l5 = (long)Ed25519.decode32(byArray, 14) & 0xFFFFFFFFL;
        long l6 = (long)(Ed25519.decode24(byArray, 18) << 4) & 0xFFFFFFFFL;
        long l7 = (long)Ed25519.decode32(byArray, 21) & 0xFFFFFFFFL;
        long l8 = (long)(Ed25519.decode24(byArray, 25) << 4) & 0xFFFFFFFFL;
        long l9 = (long)Ed25519.decode32(byArray, 28) & 0xFFFFFFFFL;
        long l10 = (long)(Ed25519.decode24(byArray, 32) << 4) & 0xFFFFFFFFL;
        long l11 = (long)Ed25519.decode32(byArray, 35) & 0xFFFFFFFFL;
        long l12 = (long)(Ed25519.decode24(byArray, 39) << 4) & 0xFFFFFFFFL;
        long l13 = (long)Ed25519.decode32(byArray, 42) & 0xFFFFFFFFL;
        long l14 = (long)(Ed25519.decode24(byArray, 46) << 4) & 0xFFFFFFFFL;
        long l15 = (long)Ed25519.decode32(byArray, 49) & 0xFFFFFFFFL;
        long l16 = (long)(Ed25519.decode24(byArray, 53) << 4) & 0xFFFFFFFFL;
        long l17 = (long)Ed25519.decode32(byArray, 56) & 0xFFFFFFFFL;
        long l18 = (long)(Ed25519.decode24(byArray, 60) << 4) & 0xFFFFFFFFL;
        long l19 = (long)byArray[63] & 0xFFL;
        l10 -= l19 * -50998291L;
        l11 -= l19 * 19280294L;
        l12 -= l19 * 127719000L;
        l13 -= l19 * -6428113L;
        l14 -= l19 * 5343L;
        l18 += l17 >> 28;
        l17 &= 0xFFFFFFFL;
        l9 -= l18 * -50998291L;
        l10 -= l18 * 19280294L;
        l11 -= l18 * 127719000L;
        l12 -= l18 * -6428113L;
        l13 -= l18 * 5343L;
        l8 -= l17 * -50998291L;
        l9 -= l17 * 19280294L;
        l10 -= l17 * 127719000L;
        l11 -= l17 * -6428113L;
        l12 -= l17 * 5343L;
        l16 += l15 >> 28;
        l15 &= 0xFFFFFFFL;
        l7 -= l16 * -50998291L;
        l8 -= l16 * 19280294L;
        l9 -= l16 * 127719000L;
        l10 -= l16 * -6428113L;
        l11 -= l16 * 5343L;
        l6 -= l15 * -50998291L;
        l7 -= l15 * 19280294L;
        l8 -= l15 * 127719000L;
        l9 -= l15 * -6428113L;
        l10 -= l15 * 5343L;
        l14 += l13 >> 28;
        l13 &= 0xFFFFFFFL;
        l5 -= l14 * -50998291L;
        l6 -= l14 * 19280294L;
        l7 -= l14 * 127719000L;
        l8 -= l14 * -6428113L;
        l9 -= l14 * 5343L;
        l13 += l12 >> 28;
        l12 &= 0xFFFFFFFL;
        l4 -= l13 * -50998291L;
        l5 -= l13 * 19280294L;
        l6 -= l13 * 127719000L;
        l7 -= l13 * -6428113L;
        l8 -= l13 * 5343L;
        l12 += l11 >> 28;
        l11 &= 0xFFFFFFFL;
        l3 -= l12 * -50998291L;
        l4 -= l12 * 19280294L;
        l5 -= l12 * 127719000L;
        l6 -= l12 * -6428113L;
        l7 -= l12 * 5343L;
        l11 += l10 >> 28;
        l10 &= 0xFFFFFFFL;
        l2 -= l11 * -50998291L;
        l3 -= l11 * 19280294L;
        l4 -= l11 * 127719000L;
        l5 -= l11 * -6428113L;
        l6 -= l11 * 5343L;
        l9 += l8 >> 28;
        l8 &= 0xFFFFFFFL;
        l10 += l9 >> 28;
        long l20 = (l9 &= 0xFFFFFFFL) >>> 27;
        l2 -= l10 * 19280294L;
        l3 -= l10 * 127719000L;
        l4 -= l10 * -6428113L;
        l5 -= l10 * 5343L;
        l &= 0xFFFFFFFL;
        l2 &= 0xFFFFFFFL;
        l3 &= 0xFFFFFFFL;
        l4 &= 0xFFFFFFFL;
        l5 &= 0xFFFFFFFL;
        l6 &= 0xFFFFFFFL;
        l7 &= 0xFFFFFFFL;
        l8 &= 0xFFFFFFFL;
        l10 = (l9 += (l8 += (l7 += (l6 += (l5 += (l4 += (l3 += (l2 += (l -= (l10 += l20) * -50998291L) >> 28) >> 28) >> 28) >> 28) >> 28) >> 28) >> 28) >> 28) >> 28;
        l9 &= 0xFFFFFFFL;
        l2 += l10 & 0x12631A6L;
        l3 += l10 & 0x79CD658L;
        l4 += l10 & 0xFFFFFFFFFF9DEA2FL;
        l5 += l10 & 0x14DFL;
        l &= 0xFFFFFFFL;
        l2 &= 0xFFFFFFFL;
        l3 &= 0xFFFFFFFL;
        l4 &= 0xFFFFFFFL;
        l5 &= 0xFFFFFFFL;
        l6 &= 0xFFFFFFFL;
        l7 &= 0xFFFFFFFL;
        l9 += (l8 += (l7 += (l6 += (l5 += (l4 += (l3 += (l2 += (l += (l10 -= l20) & 0xFFFFFFFFFCF5D3EDL) >> 28) >> 28) >> 28) >> 28) >> 28) >> 28) >> 28) >> 28;
        byte[] byArray2 = new byte[32];
        Ed25519.encode56(l | l2 << 28, byArray2, 0);
        Ed25519.encode56(l3 | l4 << 28, byArray2, 7);
        Ed25519.encode56(l5 | l6 << 28, byArray2, 14);
        Ed25519.encode56(l7 | (l8 &= 0xFFFFFFFL) << 28, byArray2, 21);
        Ed25519.encode32((int)l9, byArray2, 28);
        return byArray2;
    }

    private static void scalarMultBase(byte[] byArray, PointExt pointExt) {
        Ed25519.precompute();
        Ed25519.pointSetNeutral(pointExt);
        int[] nArray = new int[8];
        Ed25519.decodeScalar(byArray, 0, nArray);
        Nat.cadd(8, ~nArray[0] & 1, nArray, L, nArray);
        Nat.shiftDownBit(8, nArray, 1);
        for (int i = 0; i < 8; ++i) {
            nArray[i] = Interleave.shuffle2(nArray[i]);
        }
        PointPrecomp pointPrecomp = new PointPrecomp();
        int n = 28;
        while (true) {
            for (int i = 0; i < 8; ++i) {
                int n2 = nArray[i] >>> n;
                int n3 = n2 >>> 3 & 1;
                int n4 = (n2 ^ -n3) & 7;
                Ed25519.pointLookup(i, n4, pointPrecomp);
                X25519Field.cswap(n3, pointPrecomp.ypx_h, pointPrecomp.ymx_h);
                X25519Field.cnegate(n3, pointPrecomp.xyd);
                Ed25519.pointAddPrecomp(pointPrecomp, pointExt);
            }
            if ((n -= 4) < 0) break;
            Ed25519.pointDouble(pointExt);
        }
    }

    private static void scalarMultBaseEncoded(byte[] byArray, byte[] byArray2, int n) {
        PointExt pointExt = new PointExt();
        Ed25519.scalarMultBase(byArray, pointExt);
        Ed25519.encodePoint(pointExt, byArray2, n);
    }

    private static void scalarMultStraussVar(int[] nArray, int[] nArray2, PointExt pointExt, PointExt pointExt2) {
        int n;
        Ed25519.precompute();
        byte[] byArray = Ed25519.getWNAF(nArray, 7);
        byte[] byArray2 = Ed25519.getWNAF(nArray2, 5);
        PointExt[] pointExtArray = Ed25519.pointPrecompVar(pointExt, 8);
        Ed25519.pointSetNeutral(pointExt2);
        for (n = 255; n > 0 && (byArray[n] | byArray2[n]) == 0; --n) {
        }
        while (true) {
            int n2;
            int n3;
            byte by;
            if ((by = byArray[n]) != 0) {
                n3 = by >> 31;
                n2 = (by ^ n3) >>> 1;
                Ed25519.pointAddVar(n3 != 0, precompBaseTable[n2], pointExt2);
            }
            if ((n3 = byArray2[n]) != 0) {
                n2 = n3 >> 31;
                int n4 = (n3 ^ n2) >>> 1;
                Ed25519.pointAddVar(n2 != 0, pointExtArray[n4], pointExt2);
            }
            if (--n < 0) break;
            Ed25519.pointDouble(pointExt2);
        }
    }

    public static void sign(byte[] byArray, int n, byte[] byArray2, int n2, int n3, byte[] byArray3, int n4) {
        SHA512Digest sHA512Digest = new SHA512Digest();
        byte[] byArray4 = new byte[sHA512Digest.getDigestSize()];
        sHA512Digest.update(byArray, n, 32);
        sHA512Digest.doFinal(byArray4, 0);
        byte[] byArray5 = new byte[32];
        Ed25519.pruneScalar(byArray4, 0, byArray5);
        byte[] byArray6 = new byte[32];
        Ed25519.scalarMultBaseEncoded(byArray5, byArray6, 0);
        Ed25519.implSign(sHA512Digest, byArray4, byArray5, byArray6, 0, byArray2, n2, n3, byArray3, n4);
    }

    public static void sign(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, int n3, int n4, byte[] byArray4, int n5) {
        SHA512Digest sHA512Digest = new SHA512Digest();
        byte[] byArray5 = new byte[sHA512Digest.getDigestSize()];
        sHA512Digest.update(byArray, n, 32);
        sHA512Digest.doFinal(byArray5, 0);
        byte[] byArray6 = new byte[32];
        Ed25519.pruneScalar(byArray5, 0, byArray6);
        Ed25519.implSign(sHA512Digest, byArray5, byArray6, byArray2, n2, byArray3, n3, n4, byArray4, n5);
    }

    public static boolean verify(byte[] byArray, int n, byte[] byArray2, int n2, byte[] byArray3, int n3, int n4) {
        byte[] byArray4 = Arrays.copyOfRange(byArray, n, n + 32);
        byte[] byArray5 = Arrays.copyOfRange(byArray, n + 32, n + 64);
        if (!Ed25519.checkPointVar(byArray4)) {
            return false;
        }
        if (!Ed25519.checkScalarVar(byArray5)) {
            return false;
        }
        PointExt pointExt = new PointExt();
        if (!Ed25519.decodePointVar(byArray2, n2, true, pointExt)) {
            return false;
        }
        SHA512Digest sHA512Digest = new SHA512Digest();
        byte[] byArray6 = new byte[sHA512Digest.getDigestSize()];
        sHA512Digest.update(byArray4, 0, 32);
        sHA512Digest.update(byArray2, n2, 32);
        sHA512Digest.update(byArray3, n3, n4);
        sHA512Digest.doFinal(byArray6, 0);
        byte[] byArray7 = Ed25519.reduceScalar(byArray6);
        int[] nArray = new int[8];
        Ed25519.decodeScalar(byArray5, 0, nArray);
        int[] nArray2 = new int[8];
        Ed25519.decodeScalar(byArray7, 0, nArray2);
        PointExt pointExt2 = new PointExt();
        Ed25519.scalarMultStraussVar(nArray, nArray2, pointExt, pointExt2);
        byte[] byArray8 = new byte[32];
        Ed25519.encodePoint(pointExt2, byArray8, 0);
        return Arrays.areEqual(byArray8, byArray4);
    }

    private static class PointExt {
        int[] x = X25519Field.create();
        int[] y = X25519Field.create();
        int[] z = X25519Field.create();
        int[] t = X25519Field.create();

        private PointExt() {
        }
    }

    private static class PointPrecomp {
        int[] ypx_h = X25519Field.create();
        int[] ymx_h = X25519Field.create();
        int[] xyd = X25519Field.create();

        private PointPrecomp() {
        }
    }
}

