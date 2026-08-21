/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity;

import java.net.MalformedURLException;
import java.net.URL;
import org.javacord.api.entity.VanityUrlCode;

public class VanityUrlCodeImpl
implements VanityUrlCode {
    private final String code;

    public VanityUrlCodeImpl(String vanityUrlCode) {
        this.code = vanityUrlCode;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public URL getUrl() {
        try {
            return new URL("https://discord.com/invite/" + this.code);
        }
        catch (MalformedURLException e) {
            throw new AssertionError("Unexpected malformed vanity url", e);
        }
    }
}

