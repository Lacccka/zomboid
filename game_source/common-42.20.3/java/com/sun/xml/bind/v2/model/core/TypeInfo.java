/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.annotation.Locatable;

public interface TypeInfo<T, C>
extends Locatable {
    public T getType();

    public boolean canBeReferencedByIDREF();
}

