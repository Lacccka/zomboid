/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.Result;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.client.result.URIParsedResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class URIResultParser
extends ResultParser {
    private static final Pattern URL_WITH_PROTOCOL_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9+-.]+:");
    private static final Pattern URL_WITHOUT_PROTOCOL_PATTERN = Pattern.compile("([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}(:\\d{1,5})?(/|\\?|$)");

    @Override
    public URIParsedResult parse(Result result) {
        String rawText = URIResultParser.getMassagedText(result);
        if (rawText.startsWith("URL:") || rawText.startsWith("URI:")) {
            return new URIParsedResult(rawText.substring(4).trim(), null);
        }
        return URIResultParser.isBasicallyValidURI(rawText = rawText.trim()) ? new URIParsedResult(rawText, null) : null;
    }

    static boolean isBasicallyValidURI(String uri) {
        if (uri.contains(" ")) {
            return false;
        }
        Matcher m = URL_WITH_PROTOCOL_PATTERN.matcher(uri);
        if (m.find() && m.start() == 0) {
            return true;
        }
        m = URL_WITHOUT_PROTOCOL_PATTERN.matcher(uri);
        return m.find() && m.start() == 0;
    }
}

