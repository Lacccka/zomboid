/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.core.LeafInfo;
import javax.xml.namespace.QName;

public interface BuiltinLeafInfo<T, C>
extends LeafInfo<T, C> {
    @Override
    public QName getTypeName();
}

