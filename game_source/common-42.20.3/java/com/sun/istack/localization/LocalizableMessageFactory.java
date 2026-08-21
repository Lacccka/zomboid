/*
 * Decompiled with CFR 0.152.
 */
package com.sun.istack.localization;

import com.sun.istack.localization.Localizable;
import com.sun.istack.localization.LocalizableMessage;
import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizableMessageFactory {
    private final String _bundlename;
    private final ResourceBundleSupplier _rbSupplier;

    @Deprecated
    public LocalizableMessageFactory(String bundlename) {
        this._bundlename = bundlename;
        this._rbSupplier = null;
    }

    public LocalizableMessageFactory(String bundlename, ResourceBundleSupplier rbSupplier) {
        this._bundlename = bundlename;
        this._rbSupplier = rbSupplier;
    }

    public Localizable getMessage(String key, Object ... args2) {
        return new LocalizableMessage(this._bundlename, this._rbSupplier, key, args2);
    }

    public static interface ResourceBundleSupplier {
        public ResourceBundle getResourceBundle(Locale var1);
    }
}

