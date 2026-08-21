/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.LeafInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.runtime.Transducer;
import java.lang.reflect.Type;
import javax.xml.namespace.QName;

public interface RuntimeLeafInfo
extends LeafInfo<Type, Class>,
RuntimeNonElement {
    @Override
    public <V> Transducer<V> getTransducer();

    public Class getClazz();

    public QName[] getTypeNames();
}

