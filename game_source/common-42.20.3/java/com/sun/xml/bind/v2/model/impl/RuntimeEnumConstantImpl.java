/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.impl.EnumConstantImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeEnumLeafInfoImpl;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class RuntimeEnumConstantImpl
extends EnumConstantImpl<Type, Class, Field, Method> {
    public RuntimeEnumConstantImpl(RuntimeEnumLeafInfoImpl owner, String name, String lexical, EnumConstantImpl<Type, Class, Field, Method> next) {
        super(owner, name, lexical, next);
    }
}

