/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.impl.AnyTypeImpl;
import com.sun.xml.bind.v2.model.impl.Utils;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.runtime.Transducer;
import java.lang.reflect.Type;

final class RuntimeAnyTypeImpl
extends AnyTypeImpl<Type, Class>
implements RuntimeNonElement {
    static final RuntimeNonElement theInstance = new RuntimeAnyTypeImpl();

    private RuntimeAnyTypeImpl() {
        super(Utils.REFLECTION_NAVIGATOR);
    }

    @Override
    public <V> Transducer<V> getTransducer() {
        return null;
    }
}

