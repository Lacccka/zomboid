/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.result.ExpandedProductParsedResult;
import com.google.zxing.client.result.ResultParser;
import java.util.HashMap;

public final class ExpandedProductResultParser
extends ResultParser {
    @Override
    public ExpandedProductParsedResult parse(Result result) {
        BarcodeFormat format = result.getBarcodeFormat();
        if (format != BarcodeFormat.RSS_EXPANDED) {
            return null;
        }
        String rawText = ExpandedProductResultParser.getMassagedText(result);
        String productID = null;
        String sscc = null;
        String lotNumber = null;
        String productionDate = null;
        String packagingDate = null;
        String bestBeforeDate = null;
        String expirationDate = null;
        String weight = null;
        String weightType = null;
        String weightIncrement = null;
        String price = null;
        String priceIncrement = null;
        String priceCurrency = null;
        HashMap<String, String> uncommonAIs = new HashMap<String, String>();
        int i = 0;
        block50: while (i < rawText.length()) {
            String ai = ExpandedProductResultParser.findAIvalue(i, rawText);
            if (ai == null) {
                return null;
            }
            String value = ExpandedProductResultParser.findValue(i += ai.length() + 2, rawText);
            i += value.length();
            switch (ai) {
                case "00": {
                    sscc = value;
                    continue block50;
                }
                case "01": {
                    productID = value;
                    continue block50;
                }
                case "10": {
                    lotNumber = value;
                    continue block50;
                }
                case "11": {
                    productionDate = value;
                    continue block50;
                }
                case "13": {
                    packagingDate = value;
                    continue block50;
                }
                case "15": {
                    bestBeforeDate = value;
                    continue block50;
                }
                case "17": {
                    expirationDate = value;
                    continue block50;
                }
                case "3100": 
                case "3101": 
                case "3102": 
                case "3103": 
                case "3104": 
                case "3105": 
                case "3106": 
                case "3107": 
                case "3108": 
                case "3109": {
                    weight = value;
                    weightType = "KG";
                    weightIncrement = ai.substring(3);
                    continue block50;
                }
                case "3200": 
                case "3201": 
                case "3202": 
                case "3203": 
                case "3204": 
                case "3205": 
                case "3206": 
                case "3207": 
                case "3208": 
                case "3209": {
                    weight = value;
                    weightType = "LB";
                    weightIncrement = ai.substring(3);
                    continue block50;
                }
                case "3920": 
                case "3921": 
                case "3922": 
                case "3923": {
                    price = value;
                    priceIncrement = ai.substring(3);
                    continue block50;
                }
                case "3930": 
                case "3931": 
                case "3932": 
                case "3933": {
                    if (value.length() < 4) {
                        return null;
                    }
                    price = value.substring(3);
                    priceCurrency = value.substring(0, 3);
                    priceIncrement = ai.substring(3);
                    continue block50;
                }
            }
            uncommonAIs.put(ai, value);
        }
        return new ExpandedProductParsedResult(rawText, productID, sscc, lotNumber, productionDate, packagingDate, bestBeforeDate, expirationDate, weight, weightType, weightIncrement, price, priceIncrement, priceCurrency, uncommonAIs);
    }

    private static String findAIvalue(int i, String rawText) {
        char c = rawText.charAt(i);
        if (c != '(') {
            return null;
        }
        String rawTextAux = rawText.substring(i + 1);
        StringBuilder buf = new StringBuilder();
        for (int index = 0; index < rawTextAux.length(); ++index) {
            char currentChar = rawTextAux.charAt(index);
            if (currentChar == ')') {
                return buf.toString();
            }
            if (currentChar < '0' || currentChar > '9') {
                return null;
            }
            buf.append(currentChar);
        }
        return buf.toString();
    }

    private static String findValue(int i, String rawText) {
        StringBuilder buf = new StringBuilder();
        String rawTextAux = rawText.substring(i);
        for (int index = 0; index < rawTextAux.length(); ++index) {
            char c = rawTextAux.charAt(index);
            if (c == '(') {
                if (ExpandedProductResultParser.findAIvalue(index, rawTextAux) != null) break;
                buf.append('(');
                continue;
            }
            buf.append(c);
        }
        return buf.toString();
    }
}

