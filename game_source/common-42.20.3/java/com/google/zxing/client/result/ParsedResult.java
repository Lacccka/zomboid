/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.client.result.ParsedResultType;

public abstract class ParsedResult {
    private final ParsedResultType type;

    protected ParsedResult(ParsedResultType type) {
        this.type = type;
    }

    public final ParsedResultType getType() {
        return this.type;
    }

    public abstract String getDisplayResult();

    public final String toString() {
        return this.getDisplayResult();
    }

    public static void maybeAppend(String value, StringBuilder result) {
        if (value != null && !value.isEmpty()) {
            if (result.length() > 0) {
                result.append('\n');
            }
            result.append(value);
        }
    }

    public static void maybeAppend(String[] values2, StringBuilder result) {
        if (values2 != null) {
            for (String value : values2) {
                ParsedResult.maybeAppend(value, result);
            }
        }
    }
}

