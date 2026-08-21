/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.text.translate;

import org.apache.commons.lang3.text.translate.UnicodeEscaper;

@Deprecated
public class JavaUnicodeEscaper
extends UnicodeEscaper {
    public static JavaUnicodeEscaper above(int codePoint) {
        return JavaUnicodeEscaper.outsideOf(0, codePoint);
    }

    public static JavaUnicodeEscaper below(int codePoint) {
        return JavaUnicodeEscaper.outsideOf(codePoint, Integer.MAX_VALUE);
    }

    public static JavaUnicodeEscaper between(int codePointLow, int codePointHigh) {
        return new JavaUnicodeEscaper(codePointLow, codePointHigh, true);
    }

    public static JavaUnicodeEscaper outsideOf(int codePointLow, int codePointHigh) {
        return new JavaUnicodeEscaper(codePointLow, codePointHigh, false);
    }

    public JavaUnicodeEscaper(int below, int above, boolean between) {
        super(below, above, between);
    }

    @Override
    protected String toUtf16Escape(int codePoint) {
        char[] surrogatePair = Character.toChars(codePoint);
        return "\\u" + JavaUnicodeEscaper.hex(surrogatePair[0]) + "\\u" + JavaUnicodeEscaper.hex(surrogatePair[1]);
    }
}

