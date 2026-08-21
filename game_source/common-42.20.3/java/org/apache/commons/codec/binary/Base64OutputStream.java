/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.codec.binary;

import java.io.OutputStream;
import org.apache.commons.codec.CodecPolicy;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.BaseNCodecOutputStream;

public class Base64OutputStream
extends BaseNCodecOutputStream {
    public Base64OutputStream(OutputStream outputStream2) {
        this(outputStream2, true);
    }

    public Base64OutputStream(OutputStream outputStream2, boolean doEncode) {
        super(outputStream2, new Base64(false), doEncode);
    }

    public Base64OutputStream(OutputStream outputStream2, boolean doEncode, int lineLength, byte[] lineSeparator) {
        super(outputStream2, new Base64(lineLength, lineSeparator), doEncode);
    }

    public Base64OutputStream(OutputStream outputStream2, boolean doEncode, int lineLength, byte[] lineSeparator, CodecPolicy decodingPolicy) {
        super(outputStream2, new Base64(lineLength, lineSeparator, false, decodingPolicy), doEncode);
    }
}

