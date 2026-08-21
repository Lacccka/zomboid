/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;

public final class ProductParsedResult
extends ParsedResult {
    private final String productID;
    private final String normalizedProductID;

    ProductParsedResult(String productID) {
        this(productID, productID);
    }

    ProductParsedResult(String productID, String normalizedProductID) {
        super(ParsedResultType.PRODUCT);
        this.productID = productID;
        this.normalizedProductID = normalizedProductID;
    }

    public String getProductID() {
        return this.productID;
    }

    public String getNormalizedProductID() {
        return this.normalizedProductID;
    }

    @Override
    public String getDisplayResult() {
        return this.productID;
    }
}

