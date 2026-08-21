/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.impl.RuntimeElementPropertyInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeModelBuilder;
import com.sun.xml.bind.v2.model.impl.TypeRefImpl;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef;
import com.sun.xml.bind.v2.runtime.Transducer;
import java.lang.reflect.Type;
import javax.xml.namespace.QName;

final class RuntimeTypeRefImpl
extends TypeRefImpl<Type, Class>
implements RuntimeTypeRef {
    public RuntimeTypeRefImpl(RuntimeElementPropertyInfoImpl elementPropertyInfo, QName elementName, Type type, boolean isNillable, String defaultValue) {
        super(elementPropertyInfo, elementName, type, isNillable, defaultValue);
    }

    @Override
    public RuntimeNonElement getTarget() {
        return (RuntimeNonElement)super.getTarget();
    }

    @Override
    public Transducer getTransducer() {
        return RuntimeModelBuilder.createTransducer(this);
    }

    @Override
    public RuntimePropertyInfo getSource() {
        return (RuntimePropertyInfo)((Object)this.owner);
    }
}

