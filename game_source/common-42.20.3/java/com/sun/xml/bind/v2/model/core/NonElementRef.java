/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyInfo;

public interface NonElementRef<T, C> {
    public NonElement<T, C> getTarget();

    public PropertyInfo<T, C> getSource();
}

