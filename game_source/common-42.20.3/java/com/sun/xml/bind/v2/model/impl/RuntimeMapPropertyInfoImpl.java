/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.impl.MapPropertyInfoImpl;
import com.sun.xml.bind.v2.model.impl.PropertySeed;
import com.sun.xml.bind.v2.model.impl.RuntimeClassInfoImpl;
import com.sun.xml.bind.v2.model.runtime.RuntimeMapPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

class RuntimeMapPropertyInfoImpl
extends MapPropertyInfoImpl<Type, Class, Field, Method>
implements RuntimeMapPropertyInfo {
    private final Accessor acc;

    RuntimeMapPropertyInfoImpl(RuntimeClassInfoImpl classInfo, PropertySeed<Type, Class, Field, Method> seed) {
        super(classInfo, seed);
        this.acc = ((RuntimeClassInfoImpl.RuntimePropertySeed)seed).getAccessor();
    }

    @Override
    public Accessor getAccessor() {
        return this.acc;
    }

    @Override
    public boolean elementOnlyContent() {
        return true;
    }

    @Override
    public RuntimeNonElement getKeyType() {
        return (RuntimeNonElement)super.getKeyType();
    }

    @Override
    public RuntimeNonElement getValueType() {
        return (RuntimeNonElement)super.getValueType();
    }

    public List<? extends RuntimeTypeInfo> ref() {
        return (List)super.ref();
    }
}

