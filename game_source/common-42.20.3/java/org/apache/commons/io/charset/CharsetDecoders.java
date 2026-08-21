/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.charset;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public final class CharsetDecoders {
    public static CharsetDecoder toCharsetDecoder(CharsetDecoder charsetDecoder) {
        return charsetDecoder != null ? charsetDecoder : Charset.defaultCharset().newDecoder();
    }

    private CharsetDecoders() {
    }
}

