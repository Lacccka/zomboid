/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.impl.PropertySeed;
import com.sun.xml.bind.v2.model.impl.ReferencePropertyInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeClassInfoImpl;
import com.sun.xml.bind.v2.model.runtime.RuntimeElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeReferencePropertyInfo;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

class RuntimeReferencePropertyInfoImpl
extends ReferencePropertyInfoImpl<Type, Class, Field, Method>
implements RuntimeReferencePropertyInfo {
    private final Accessor acc;

    public RuntimeReferencePropertyInfoImpl(RuntimeClassInfoImpl classInfo, PropertySeed<Type, Class, Field, Method> seed) {
        super(classInfo, seed);
        Accessor rawAcc = ((RuntimeClassInfoImpl.RuntimePropertySeed)seed).getAccessor();
        if (this.getAdapter() != null && !this.isCollection()) {
            rawAcc = rawAcc.adapt(this.getAdapter());
        }
        this.acc = rawAcc;
    }

    @Override
    public Set<? extends RuntimeElement> getElements() {
        return super.getElements();
    }

    @Override
    public Set<? extends RuntimeElement> ref() {
        return super.ref();
    }

    @Override
    public Accessor getAccessor() {
        return this.acc;
    }

    @Override
    public boolean elementOnlyContent() {
        return !this.isMixed();
    }
}

