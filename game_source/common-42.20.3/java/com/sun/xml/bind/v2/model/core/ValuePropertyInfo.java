/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.NonElementRef;
import com.sun.xml.bind.v2.model.core.PropertyInfo;

public interface ValuePropertyInfo<T, C>
extends PropertyInfo<T, C>,
NonElementRef<T, C> {
    @Override
    public Adapter<T, C> getAdapter();
}

