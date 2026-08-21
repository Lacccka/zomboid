/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;

public final class WifiParsedResult
extends ParsedResult {
    private final String ssid;
    private final String networkEncryption;
    private final String password;
    private final boolean hidden;

    public WifiParsedResult(String networkEncryption, String ssid, String password) {
        this(networkEncryption, ssid, password, false);
    }

    public WifiParsedResult(String networkEncryption, String ssid, String password, boolean hidden) {
        super(ParsedResultType.WIFI);
        this.ssid = ssid;
        this.networkEncryption = networkEncryption;
        this.password = password;
        this.hidden = hidden;
    }

    public String getSsid() {
        return this.ssid;
    }

    public String getNetworkEncryption() {
        return this.networkEncryption;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    @Override
    public String getDisplayResult() {
        StringBuilder result = new StringBuilder(80);
        WifiParsedResult.maybeAppend(this.ssid, result);
        WifiParsedResult.maybeAppend(this.networkEncryption, result);
        WifiParsedResult.maybeAppend(this.password, result);
        WifiParsedResult.maybeAppend(Boolean.toString(this.hidden), result);
        return result.toString();
    }
}

