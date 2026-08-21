/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.NonElementRef;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.Transducer;
import java.lang.reflect.Type;

public interface RuntimeNonElementRef
extends NonElementRef<Type, Class> {
    public RuntimeNonElement getTarget();

    public RuntimePropertyInfo getSource();

    public Transducer getTransducer();
}

