/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.impl.ArrayInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeModelBuilder;
import com.sun.xml.bind.v2.model.runtime.RuntimeArrayInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.runtime.Transducer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class RuntimeArrayInfoImpl
extends ArrayInfoImpl<Type, Class, Field, Method>
implements RuntimeArrayInfo {
    RuntimeArrayInfoImpl(RuntimeModelBuilder builder, Locatable upstream, Class arrayType) {
        super(builder, upstream, arrayType);
    }

    @Override
    public Class getType() {
        return (Class)super.getType();
    }

    @Override
    public RuntimeNonElement getItemType() {
        return (RuntimeNonElement)super.getItemType();
    }

    @Override
    public <V> Transducer<V> getTransducer() {
        return null;
    }
}

