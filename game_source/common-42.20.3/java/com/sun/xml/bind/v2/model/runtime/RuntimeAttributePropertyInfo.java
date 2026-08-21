/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.AttributePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElementRef;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import java.lang.reflect.Type;

public interface RuntimeAttributePropertyInfo
extends AttributePropertyInfo<Type, Class>,
RuntimePropertyInfo,
RuntimeNonElementRef {
    @Override
    public RuntimeNonElement getTarget();
}

