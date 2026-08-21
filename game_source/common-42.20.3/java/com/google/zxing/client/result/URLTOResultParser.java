/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.Result;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.client.result.URIParsedResult;

public final class URLTOResultParser
extends ResultParser {
    @Override
    public URIParsedResult parse(Result result) {
        String rawText = URLTOResultParser.getMassagedText(result);
        if (!rawText.startsWith("urlto:") && !rawText.startsWith("URLTO:")) {
            return null;
        }
        int titleEnd = rawText.indexOf(58, 6);
        if (titleEnd < 0) {
            return null;
        }
        String title = titleEnd <= 6 ? null : rawText.substring(6, titleEnd);
        String uri = rawText.substring(titleEnd + 1);
        return new URIParsedResult(uri, title);
    }
}

