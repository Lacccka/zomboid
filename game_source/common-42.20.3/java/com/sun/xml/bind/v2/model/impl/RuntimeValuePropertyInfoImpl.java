/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.impl.PropertySeed;
import com.sun.xml.bind.v2.model.impl.RuntimeClassInfoImpl;
import com.sun.xml.bind.v2.model.impl.ValuePropertyInfoImpl;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeValuePropertyInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

final class RuntimeValuePropertyInfoImpl
extends ValuePropertyInfoImpl<Type, Class, Field, Method>
implements RuntimeValuePropertyInfo {
    RuntimeValuePropertyInfoImpl(RuntimeClassInfoImpl classInfo, PropertySeed<Type, Class, Field, Method> seed) {
        super(classInfo, seed);
    }

    @Override
    public boolean elementOnlyContent() {
        return false;
    }

    @Override
    public RuntimePropertyInfo getSource() {
        return (RuntimePropertyInfo)super.getSource();
    }

    @Override
    public RuntimeNonElement getTarget() {
        return (RuntimeNonElement)super.getTarget();
    }

    @Override
    public List<? extends RuntimeNonElement> ref() {
        return super.ref();
    }

    @Override
    public void link() {
        this.getTransducer();
        super.link();
    }
}

