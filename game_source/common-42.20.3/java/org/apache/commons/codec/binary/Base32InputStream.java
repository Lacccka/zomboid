/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.codec.binary;

import java.io.InputStream;
import org.apache.commons.codec.CodecPolicy;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.BaseNCodecInputStream;

public class Base32InputStream
extends BaseNCodecInputStream {
    public Base32InputStream(InputStream inputStream2) {
        this(inputStream2, false);
    }

    public Base32InputStream(InputStream inputStream2, boolean doEncode) {
        super(inputStream2, new Base32(false), doEncode);
    }

    public Base32InputStream(InputStream inputStream2, boolean doEncode, int lineLength, byte[] lineSeparator) {
        super(inputStream2, new Base32(lineLength, lineSeparator), doEncode);
    }

    public Base32InputStream(InputStream inputStream2, boolean doEncode, int lineLength, byte[] lineSeparator, CodecPolicy decodingPolicy) {
        super(inputStream2, new Base32(lineLength, lineSeparator, false, 61, decodingPolicy), doEncode);
    }
}

