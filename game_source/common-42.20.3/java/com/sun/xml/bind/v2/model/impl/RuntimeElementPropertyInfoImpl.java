/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.impl.ElementPropertyInfoImpl;
import com.sun.xml.bind.v2.model.impl.PropertySeed;
import com.sun.xml.bind.v2.model.impl.RuntimeClassInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeTypeRefImpl;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import javax.xml.namespace.QName;

class RuntimeElementPropertyInfoImpl
extends ElementPropertyInfoImpl<Type, Class, Field, Method>
implements RuntimeElementPropertyInfo {
    private final Accessor acc;

    RuntimeElementPropertyInfoImpl(RuntimeClassInfoImpl classInfo, PropertySeed<Type, Class, Field, Method> seed) {
        super(classInfo, seed);
        Accessor rawAcc = ((RuntimeClassInfoImpl.RuntimePropertySeed)seed).getAccessor();
        if (this.getAdapter() != null && !this.isCollection()) {
            rawAcc = rawAcc.adapt(this.getAdapter());
        }
        this.acc = rawAcc;
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
    public List<? extends RuntimeTypeInfo> ref() {
        return super.ref();
    }

    protected RuntimeTypeRefImpl createTypeRef(QName name, Type type, boolean isNillable, String defaultValue) {
        return new RuntimeTypeRefImpl(this, name, type, isNillable, defaultValue);
    }

    @Override
    public List<RuntimeTypeRefImpl> getTypes() {
        return super.getTypes();
    }
}

