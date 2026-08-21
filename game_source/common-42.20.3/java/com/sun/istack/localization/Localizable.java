/*
 * Decompiled with CFR 0.152.
 */
package com.sun.istack.localization;

import java.util.Locale;
import java.util.ResourceBundle;

public interface Localizable {
    public static final String NOT_LOCALIZABLE = "\u0000";

    public String getKey();

    public Object[] getArguments();

    public String getResourceBundleName();

    public ResourceBundle getResourceBundle(Locale var1);
}

