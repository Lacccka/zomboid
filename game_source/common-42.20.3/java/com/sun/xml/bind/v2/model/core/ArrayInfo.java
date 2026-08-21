/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.core.NonElement;

public interface ArrayInfo<T, C>
extends NonElement<T, C> {
    public NonElement<T, C> getItemType();
}

