/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.Result;
import com.google.zxing.client.result.AbstractDoCoMoResultParser;
import com.google.zxing.client.result.URIParsedResult;
import com.google.zxing.client.result.URIResultParser;

public final class BookmarkDoCoMoResultParser
extends AbstractDoCoMoResultParser {
    @Override
    public URIParsedResult parse(Result result) {
        String rawText = result.getText();
        if (!rawText.startsWith("MEBKM:")) {
            return null;
        }
        String title = BookmarkDoCoMoResultParser.matchSingleDoCoMoPrefixedField("TITLE:", rawText, true);
        String[] rawUri = BookmarkDoCoMoResultParser.matchDoCoMoPrefixedField("URL:", rawText, true);
        if (rawUri == null) {
            return null;
        }
        String uri = rawUri[0];
        return URIResultParser.isBasicallyValidURI(uri) ? new URIParsedResult(uri, title) : null;
    }
}

