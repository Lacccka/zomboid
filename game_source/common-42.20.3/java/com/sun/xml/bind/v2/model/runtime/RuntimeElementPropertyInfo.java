/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public interface RuntimeElementPropertyInfo
extends ElementPropertyInfo<Type, Class>,
RuntimePropertyInfo {
    @Override
    public Collection<? extends RuntimeTypeInfo> ref();

    @Override
    public List<? extends RuntimeTypeRef> getTypes();
}

