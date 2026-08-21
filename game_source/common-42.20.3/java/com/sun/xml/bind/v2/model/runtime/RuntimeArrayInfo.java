/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.ArrayInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import java.lang.reflect.Type;

public interface RuntimeArrayInfo
extends ArrayInfo<Type, Class>,
RuntimeNonElement {
    @Override
    public Class getType();

    public RuntimeNonElement getItemType();
}

