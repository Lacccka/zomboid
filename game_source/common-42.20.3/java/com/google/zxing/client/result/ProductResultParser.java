/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.result.ProductParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.oned.UPCEReader;

public final class ProductResultParser
extends ResultParser {
    @Override
    public ProductParsedResult parse(Result result) {
        BarcodeFormat format = result.getBarcodeFormat();
        if (format != BarcodeFormat.UPC_A && format != BarcodeFormat.UPC_E && format != BarcodeFormat.EAN_8 && format != BarcodeFormat.EAN_13) {
            return null;
        }
        String rawText = ProductResultParser.getMassagedText(result);
        if (!ProductResultParser.isStringOfDigits(rawText, rawText.length())) {
            return null;
        }
        String normalizedProductID = format == BarcodeFormat.UPC_E && rawText.length() == 8 ? UPCEReader.convertUPCEtoUPCA(rawText) : rawText;
        return new ProductParsedResult(rawText, normalizedProductID);
    }
}

