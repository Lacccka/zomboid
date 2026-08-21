/*
 * Decompiled with CFR 0.152.
 */
package com.sun.istack.localization;

import com.sun.istack.localization.Localizable;
import com.sun.istack.localization.LocalizableMessageFactory;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public final class LocalizableMessage
implements Localizable {
    private final String _bundlename;
    private final LocalizableMessageFactory.ResourceBundleSupplier _rbSupplier;
    private final String _key;
    private final Object[] _args;

    @Deprecated
    public LocalizableMessage(String bundlename, String key, Object ... args2) {
        this(bundlename, null, key, args2);
    }

    public LocalizableMessage(String bundlename, LocalizableMessageFactory.ResourceBundleSupplier rbSupplier, String key, Object ... args2) {
        this._bundlename = bundlename;
        this._rbSupplier = rbSupplier;
        this._key = key;
        if (args2 == null) {
            args2 = new Object[]{};
        }
        this._args = args2;
    }

    @Override
    public String getKey() {
        return this._key;
    }

    @Override
    public Object[] getArguments() {
        return Arrays.copyOf(this._args, this._args.length);
    }

    @Override
    public String getResourceBundleName() {
        return this._bundlename;
    }

    @Override
    public ResourceBundle getResourceBundle(Locale locale) {
        if (this._rbSupplier == null) {
            return null;
        }
        return this._rbSupplier.getResourceBundle(locale);
    }
}

