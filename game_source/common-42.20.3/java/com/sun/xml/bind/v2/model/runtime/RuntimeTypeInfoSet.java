/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.runtime.RuntimeArrayInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeBuiltinLeafInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeEnumLeafInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import javax.xml.namespace.QName;

public interface RuntimeTypeInfoSet
extends TypeInfoSet<Type, Class, Field, Method> {
    @Override
    public Map<Class, ? extends RuntimeArrayInfo> arrays();

    @Override
    public Map<Class, ? extends RuntimeClassInfo> beans();

    @Override
    public Map<Type, ? extends RuntimeBuiltinLeafInfo> builtins();

    @Override
    public Map<Class, ? extends RuntimeEnumLeafInfo> enums();

    public RuntimeNonElement getTypeInfo(Type var1);

    public RuntimeNonElement getAnyTypeInfo();

    public RuntimeNonElement getClassInfo(Class var1);

    public RuntimeElementInfo getElementInfo(Class var1, QName var2);

    @Override
    public Map<QName, ? extends RuntimeElementInfo> getElementMappings(Class var1);

    @Override
    public Iterable<? extends RuntimeElementInfo> getAllElements();
}

