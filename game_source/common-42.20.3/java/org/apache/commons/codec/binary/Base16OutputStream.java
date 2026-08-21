/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.codec.binary;

import java.io.OutputStream;
import org.apache.commons.codec.CodecPolicy;
import org.apache.commons.codec.binary.Base16;
import org.apache.commons.codec.binary.BaseNCodecOutputStream;

public class Base16OutputStream
extends BaseNCodecOutputStream {
    public Base16OutputStream(OutputStream outputStream2) {
        this(outputStream2, true);
    }

    public Base16OutputStream(OutputStream outputStream2, boolean doEncode) {
        this(outputStream2, doEncode, false);
    }

    public Base16OutputStream(OutputStream outputStream2, boolean doEncode, boolean lowerCase) {
        this(outputStream2, doEncode, lowerCase, CodecPolicy.LENIENT);
    }

    public Base16OutputStream(OutputStream outputStream2, boolean doEncode, boolean lowerCase, CodecPolicy decodingPolicy) {
        super(outputStream2, new Base16(lowerCase, decodingPolicy), doEncode);
    }
}

