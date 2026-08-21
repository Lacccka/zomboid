/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.client.result.ResultParser;

abstract class AbstractDoCoMoResultParser
extends ResultParser {
    AbstractDoCoMoResultParser() {
    }

    static String[] matchDoCoMoPrefixedField(String prefix, String rawText, boolean trim) {
        return AbstractDoCoMoResultParser.matchPrefixedField(prefix, rawText, ';', trim);
    }

    static String matchSingleDoCoMoPrefixedField(String prefix, String rawText, boolean trim) {
        return AbstractDoCoMoResultParser.matchSinglePrefixedField(prefix, rawText, ';', trim);
    }
}

