/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.MapPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import java.lang.reflect.Type;

public interface RuntimeMapPropertyInfo
extends RuntimePropertyInfo,
MapPropertyInfo<Type, Class> {
    public RuntimeNonElement getKeyType();

    public RuntimeNonElement getValueType();
}

