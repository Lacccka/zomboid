/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import org.xml.sax.Attributes;

public interface AttributesEx
extends Attributes {
    public CharSequence getData(int var1);

    public CharSequence getData(String var1, String var2);
}

