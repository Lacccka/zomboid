/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.core.EnumConstant;
import com.sun.xml.bind.v2.model.core.LeafInfo;
import com.sun.xml.bind.v2.model.core.NonElement;

public interface EnumLeafInfo<T, C>
extends LeafInfo<T, C> {
    public C getClazz();

    public NonElement<T, C> getBaseType();

    public Iterable<? extends EnumConstant> getConstants();
}

