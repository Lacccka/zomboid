/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.runtime.Transducer;
import java.lang.reflect.Type;

public interface RuntimeNonElement
extends NonElement<Type, Class>,
RuntimeTypeInfo {
    public <V> Transducer<V> getTransducer();
}

