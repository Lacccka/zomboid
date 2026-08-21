/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.core.TypeInfo;
import java.util.Set;

public interface RegistryInfo<T, C> {
    public Set<TypeInfo<T, C>> getReferences();

    public C getClazz();
}

