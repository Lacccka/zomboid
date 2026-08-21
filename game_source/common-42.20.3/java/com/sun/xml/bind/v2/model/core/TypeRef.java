/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.core.NonElementRef;
import javax.xml.namespace.QName;

public interface TypeRef<T, C>
extends NonElementRef<T, C> {
    public QName getTagName();

    public boolean isNillable();

    public String getDefaultValue();
}

