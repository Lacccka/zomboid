/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.codec.binary;

import java.io.InputStream;
import org.apache.commons.codec.CodecPolicy;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.BaseNCodecInputStream;

public class Base64InputStream
extends BaseNCodecInputStream {
    public Base64InputStream(InputStream inputStream2) {
        this(inputStream2, false);
    }

    public Base64InputStream(InputStream inputStream2, boolean doEncode) {
        super(inputStream2, new Base64(false), doEncode);
    }

    public Base64InputStream(InputStream inputStream2, boolean doEncode, int lineLength, byte[] lineSeparator) {
        super(inputStream2, new Base64(lineLength, lineSeparator), doEncode);
    }

    public Base64InputStream(InputStream inputStream2, boolean doEncode, int lineLength, byte[] lineSeparator, CodecPolicy decodingPolicy) {
        super(inputStream2, new Base64(lineLength, lineSeparator, false, decodingPolicy), doEncode);
    }
}

