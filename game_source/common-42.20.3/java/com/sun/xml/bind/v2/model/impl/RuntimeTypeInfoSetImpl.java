/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.impl.RuntimeAnyTypeImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeArrayInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeBuiltinLeafInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeClassInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeElementInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeEnumLeafInfoImpl;
import com.sun.xml.bind.v2.model.impl.TypeInfoSetImpl;
import com.sun.xml.bind.v2.model.impl.Utils;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfoSet;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import javax.xml.namespace.QName;

final class RuntimeTypeInfoSetImpl
extends TypeInfoSetImpl<Type, Class, Field, Method>
implements RuntimeTypeInfoSet {
    public RuntimeTypeInfoSetImpl(AnnotationReader<Type, Class, Field, Method> reader) {
        super(Utils.REFLECTION_NAVIGATOR, reader, RuntimeBuiltinLeafInfoImpl.LEAVES);
    }

    protected RuntimeNonElement createAnyType() {
        return RuntimeAnyTypeImpl.theInstance;
    }

    @Override
    public RuntimeNonElement getTypeInfo(Type type) {
        return (RuntimeNonElement)super.getTypeInfo(type);
    }

    @Override
    public RuntimeNonElement getAnyTypeInfo() {
        return (RuntimeNonElement)super.getAnyTypeInfo();
    }

    @Override
    public RuntimeNonElement getClassInfo(Class clazz) {
        return (RuntimeNonElement)super.getClassInfo(clazz);
    }

    @Override
    public Map<Class, RuntimeClassInfoImpl> beans() {
        return super.beans();
    }

    @Override
    public Map<Type, RuntimeBuiltinLeafInfoImpl<?>> builtins() {
        return super.builtins();
    }

    @Override
    public Map<Class, RuntimeEnumLeafInfoImpl<?, ?>> enums() {
        return super.enums();
    }

    @Override
    public Map<Class, RuntimeArrayInfoImpl> arrays() {
        return super.arrays();
    }

    @Override
    public RuntimeElementInfoImpl getElementInfo(Class scope, QName name) {
        return (RuntimeElementInfoImpl)super.getElementInfo(scope, name);
    }

    @Override
    public Map<QName, RuntimeElementInfoImpl> getElementMappings(Class scope) {
        return super.getElementMappings(scope);
    }

    @Override
    public Iterable<RuntimeElementInfoImpl> getAllElements() {
        return super.getAllElements();
    }
}

