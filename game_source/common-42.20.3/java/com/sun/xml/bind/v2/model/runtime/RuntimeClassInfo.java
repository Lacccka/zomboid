/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public interface RuntimeClassInfo
extends ClassInfo<Type, Class>,
RuntimeNonElement {
    public RuntimeClassInfo getBaseClass();

    @Override
    public List<? extends RuntimePropertyInfo> getProperties();

    public RuntimePropertyInfo getProperty(String var1);

    public Method getFactoryMethod();

    public <BeanT> Accessor<BeanT, Map<QName, String>> getAttributeWildcard();

    public <BeanT> Accessor<BeanT, Locator> getLocatorField();
}

