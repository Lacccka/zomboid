/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public final class AnyTypeAdapter
extends XmlAdapter<Object, Object> {
    @Override
    public Object unmarshal(Object v) {
        return v;
    }

    @Override
    public Object marshal(Object v) {
        return v;
    }
}

