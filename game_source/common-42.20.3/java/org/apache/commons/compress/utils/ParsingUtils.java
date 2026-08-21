/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.utils;

import java.io.IOException;

public final class ParsingUtils {
    public static int parseIntValue(String value) throws IOException {
        return ParsingUtils.parseIntValue(value, 10);
    }

    public static int parseIntValue(String value, int radix) throws IOException {
        try {
            return Integer.parseInt(value, radix);
        }
        catch (NumberFormatException exp) {
            throw new IOException("Unable to parse int from string value: " + value);
        }
    }

    public static long parseLongValue(String value) throws IOException {
        return ParsingUtils.parseLongValue(value, 10);
    }

    public static long parseLongValue(String value, int radix) throws IOException {
        try {
            return Long.parseLong(value, radix);
        }
        catch (NumberFormatException exp) {
            throw new IOException("Unable to parse long from string value: " + value);
        }
    }

    private ParsingUtils() {
    }
}

