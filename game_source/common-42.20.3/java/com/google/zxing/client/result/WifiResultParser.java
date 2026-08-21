/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.Result;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.client.result.WifiParsedResult;

public final class WifiResultParser
extends ResultParser {
    @Override
    public WifiParsedResult parse(Result result) {
        String rawText = WifiResultParser.getMassagedText(result);
        if (!rawText.startsWith("WIFI:")) {
            return null;
        }
        String ssid = WifiResultParser.matchSinglePrefixedField("S:", rawText, ';', false);
        if (ssid == null || ssid.isEmpty()) {
            return null;
        }
        String pass = WifiResultParser.matchSinglePrefixedField("P:", rawText, ';', false);
        String type = WifiResultParser.matchSinglePrefixedField("T:", rawText, ';', false);
        if (type == null) {
            type = "nopass";
        }
        boolean hidden = Boolean.parseBoolean(WifiResultParser.matchSinglePrefixedField("H:", rawText, ';', false));
        return new WifiParsedResult(type, ssid, pass, hidden);
    }
}

