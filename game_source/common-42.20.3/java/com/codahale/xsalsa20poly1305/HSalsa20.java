/*
 * Decompiled with CFR 0.152.
 */
package com.codahale.xsalsa20poly1305;

import java.nio.charset.StandardCharsets;
import org.bouncycastle.crypto.engines.Salsa20Engine;
import org.bouncycastle.util.Pack;

class HSalsa20 {
    private static final byte[] SIGMA = "expand 32-byte k".getBytes(StandardCharsets.US_ASCII);
    private static final int SIGMA_0 = Pack.littleEndianToInt(SIGMA, 0);
    private static final int SIGMA_4 = Pack.littleEndianToInt(SIGMA, 4);
    private static final int SIGMA_8 = Pack.littleEndianToInt(SIGMA, 8);
    private static final int SIGMA_12 = Pack.littleEndianToInt(SIGMA, 12);

    private HSalsa20() {
    }

    static void hsalsa20(byte[] out, byte[] in, byte[] k) {
        int[] x = new int[16];
        int in0 = Pack.littleEndianToInt(in, 0);
        int in4 = Pack.littleEndianToInt(in, 4);
        int in8 = Pack.littleEndianToInt(in, 8);
        int in12 = Pack.littleEndianToInt(in, 12);
        x[0] = SIGMA_0;
        x[1] = Pack.littleEndianToInt(k, 0);
        x[2] = Pack.littleEndianToInt(k, 4);
        x[3] = Pack.littleEndianToInt(k, 8);
        x[4] = Pack.littleEndianToInt(k, 12);
        x[5] = SIGMA_4;
        x[6] = in0;
        x[7] = in4;
        x[8] = in8;
        x[9] = in12;
        x[10] = SIGMA_8;
        x[11] = Pack.littleEndianToInt(k, 16);
        x[12] = Pack.littleEndianToInt(k, 20);
        x[13] = Pack.littleEndianToInt(k, 24);
        x[14] = Pack.littleEndianToInt(k, 28);
        x[15] = SIGMA_12;
        Salsa20Engine.salsaCore(20, x, x);
        x[0] = x[0] - SIGMA_0;
        x[5] = x[5] - SIGMA_4;
        x[10] = x[10] - SIGMA_8;
        x[15] = x[15] - SIGMA_12;
        x[6] = x[6] - in0;
        x[7] = x[7] - in4;
        x[8] = x[8] - in8;
        x[9] = x[9] - in12;
        Pack.intToLittleEndian(x[0], out, 0);
        Pack.intToLittleEndian(x[5], out, 4);
        Pack.intToLittleEndian(x[10], out, 8);
        Pack.intToLittleEndian(x[15], out, 12);
        Pack.intToLittleEndian(x[6], out, 16);
        Pack.intToLittleEndian(x[7], out, 20);
        Pack.intToLittleEndian(x[8], out, 24);
        Pack.intToLittleEndian(x[9], out, 28);
    }
}

