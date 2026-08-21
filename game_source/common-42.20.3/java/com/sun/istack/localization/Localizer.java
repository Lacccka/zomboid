/*
 * Decompiled with CFR 0.152.
 */
package com.sun.istack.localization;

import com.sun.istack.localization.Localizable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Localizer {
    private final Locale _locale;
    private final HashMap<String, ResourceBundle> _resourceBundles;

    public Localizer() {
        this(Locale.getDefault());
    }

    public Localizer(Locale l) {
        this._locale = l;
        this._resourceBundles = new HashMap();
    }

    public Locale getLocale() {
        return this._locale;
    }

    public String localize(Localizable l) {
        String key = l.getKey();
        if (key == "\u0000") {
            return (String)l.getArguments()[0];
        }
        String bundlename = l.getResourceBundleName();
        try {
            String msg;
            ResourceBundle bundle = this._resourceBundles.get(bundlename);
            if (bundle == null && (bundle = l.getResourceBundle(this._locale)) != null) {
                this._resourceBundles.put(bundlename, bundle);
            }
            if (bundle == null) {
                block16: {
                    try {
                        bundle = ResourceBundle.getBundle(bundlename, this._locale);
                    }
                    catch (MissingResourceException e) {
                        int i = bundlename.lastIndexOf(46);
                        if (i == -1) break block16;
                        String alternateBundleName = bundlename.substring(i + 1);
                        try {
                            bundle = ResourceBundle.getBundle(alternateBundleName, this._locale);
                        }
                        catch (MissingResourceException e2) {
                            try {
                                bundle = ResourceBundle.getBundle(bundlename, this._locale, Thread.currentThread().getContextClassLoader());
                            }
                            catch (MissingResourceException e3) {
                                return this.getDefaultMessage(l);
                            }
                        }
                    }
                }
                this._resourceBundles.put(bundlename, bundle);
            }
            if (bundle == null) {
                return this.getDefaultMessage(l);
            }
            if (key == null) {
                key = "undefined";
            }
            try {
                msg = bundle.getString(key);
            }
            catch (MissingResourceException e) {
                msg = bundle.getString("undefined");
            }
            Object[] args2 = l.getArguments();
            for (int i = 0; i < args2.length; ++i) {
                if (!(args2[i] instanceof Localizable)) continue;
                args2[i] = this.localize((Localizable)args2[i]);
            }
            String message = MessageFormat.format(msg, args2);
            return message;
        }
        catch (MissingResourceException e) {
            return this.getDefaultMessage(l);
        }
    }

    private String getDefaultMessage(Localizable l) {
        String key = l.getKey();
        Object[] args2 = l.getArguments();
        StringBuilder sb = new StringBuilder();
        sb.append("[failed to localize] ");
        sb.append(key);
        if (args2 != null) {
            sb.append('(');
            for (int i = 0; i < args2.length; ++i) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(String.valueOf(args2[i]));
            }
            sb.append(')');
        }
        return sb.toString();
    }
}

