/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import java.lang.reflect.Type;
import java.util.Collection;

public interface RuntimePropertyInfo
extends PropertyInfo<Type, Class> {
    @Override
    public Collection<? extends RuntimeTypeInfo> ref();

    public Accessor getAccessor();

    public boolean elementOnlyContent();

    public Type getRawType();

    public Type getIndividualType();
}

