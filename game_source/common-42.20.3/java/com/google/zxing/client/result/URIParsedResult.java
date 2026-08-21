/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.client.result.ResultParser;
import java.util.regex.Pattern;

public final class URIParsedResult
extends ParsedResult {
    private static final Pattern USER_IN_HOST = Pattern.compile(":/*([^/@]+)@[^/]+");
    private final String uri;
    private final String title;

    public URIParsedResult(String uri, String title) {
        super(ParsedResultType.URI);
        this.uri = URIParsedResult.massageURI(uri);
        this.title = title;
    }

    public String getURI() {
        return this.uri;
    }

    public String getTitle() {
        return this.title;
    }

    public boolean isPossiblyMaliciousURI() {
        return USER_IN_HOST.matcher(this.uri).find();
    }

    @Override
    public String getDisplayResult() {
        StringBuilder result = new StringBuilder(30);
        URIParsedResult.maybeAppend(this.title, result);
        URIParsedResult.maybeAppend(this.uri, result);
        return result.toString();
    }

    private static String massageURI(String uri) {
        int protocolEnd = (uri = uri.trim()).indexOf(58);
        if (protocolEnd < 0 || URIParsedResult.isColonFollowedByPortNumber(uri, protocolEnd)) {
            uri = "http://" + uri;
        }
        return uri;
    }

    private static boolean isColonFollowedByPortNumber(String uri, int protocolEnd) {
        int start = protocolEnd + 1;
        int nextSlash = uri.indexOf(47, start);
        if (nextSlash < 0) {
            nextSlash = uri.length();
        }
        return ResultParser.isSubstringOfDigits(uri, start, nextSlash - start);
    }
}

