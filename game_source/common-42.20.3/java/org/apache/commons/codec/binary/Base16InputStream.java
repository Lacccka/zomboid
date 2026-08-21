/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.codec.binary;

import java.io.InputStream;
import org.apache.commons.codec.CodecPolicy;
import org.apache.commons.codec.binary.Base16;
import org.apache.commons.codec.binary.BaseNCodecInputStream;

public class Base16InputStream
extends BaseNCodecInputStream {
    public Base16InputStream(InputStream inputStream2) {
        this(inputStream2, false);
    }

    public Base16InputStream(InputStream inputStream2, boolean doEncode) {
        this(inputStream2, doEncode, false);
    }

    public Base16InputStream(InputStream inputStream2, boolean doEncode, boolean lowerCase) {
        this(inputStream2, doEncode, lowerCase, CodecPolicy.LENIENT);
    }

    public Base16InputStream(InputStream inputStream2, boolean doEncode, boolean lowerCase, CodecPolicy decodingPolicy) {
        super(inputStream2, new Base16(lowerCase, decodingPolicy), doEncode);
    }
}

