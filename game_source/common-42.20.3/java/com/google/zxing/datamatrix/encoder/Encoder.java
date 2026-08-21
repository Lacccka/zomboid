/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.datamatrix.encoder;

import com.google.zxing.datamatrix.encoder.EncoderContext;

interface Encoder {
    public int getEncodingMode();

    public void encode(EncoderContext var1);
}

