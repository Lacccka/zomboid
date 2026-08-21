/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.impl.AttributePropertyInfoImpl;
import com.sun.xml.bind.v2.model.impl.PropertySeed;
import com.sun.xml.bind.v2.model.impl.RuntimeClassInfoImpl;
import com.sun.xml.bind.v2.model.runtime.RuntimeAttributePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

class RuntimeAttributePropertyInfoImpl
extends AttributePropertyInfoImpl<Type, Class, Field, Method>
implements RuntimeAttributePropertyInfo {
    RuntimeAttributePropertyInfoImpl(RuntimeClassInfoImpl classInfo, PropertySeed<Type, Class, Field, Method> seed) {
        super(classInfo, seed);
    }

    @Override
    public boolean elementOnlyContent() {
        return true;
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
    public RuntimePropertyInfo getSource() {
        return this;
    }

    @Override
    public void link() {
        this.getTransducer();
        super.link();
    }
}

