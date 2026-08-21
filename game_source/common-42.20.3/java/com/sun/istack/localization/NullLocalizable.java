/*
 * Decompiled with CFR 0.152.
 */
package com.sun.istack.localization;

import com.sun.istack.localization.Localizable;
import java.util.Locale;
import java.util.ResourceBundle;

public final class NullLocalizable
implements Localizable {
    private final String msg;

    public NullLocalizable(String msg) {
        if (msg == null) {
            throw new IllegalArgumentException();
        }
        this.msg = msg;
    }

    @Override
    public String getKey() {
        return "\u0000";
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{this.msg};
    }

    @Override
    public String getResourceBundleName() {
        return "";
    }

    @Override
    public ResourceBundle getResourceBundle(Locale locale) {
        return null;
    }
}

