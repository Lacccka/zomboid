/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;

public final class ISBNParsedResult
extends ParsedResult {
    private final String isbn;

    ISBNParsedResult(String isbn) {
        super(ParsedResultType.ISBN);
        this.isbn = isbn;
    }

    public String getISBN() {
        return this.isbn;
    }

    @Override
    public String getDisplayResult() {
        return this.isbn;
    }
}

