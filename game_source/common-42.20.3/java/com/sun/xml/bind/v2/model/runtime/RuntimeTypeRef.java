/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.TypeRef;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElementRef;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import java.lang.reflect.Type;

public interface RuntimeTypeRef
extends TypeRef<Type, Class>,
RuntimeNonElementRef {
    @Override
    public RuntimeNonElement getTarget();

    @Override
    public RuntimePropertyInfo getSource();
}

