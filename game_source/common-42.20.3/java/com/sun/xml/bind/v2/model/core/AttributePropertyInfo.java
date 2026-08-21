/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.NonElementRef;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import javax.xml.namespace.QName;

public interface AttributePropertyInfo<T, C>
extends PropertyInfo<T, C>,
NonElementRef<T, C> {
    @Override
    public NonElement<T, C> getTarget();

    public boolean isRequired();

    public QName getXmlName();

    @Override
    public Adapter<T, C> getAdapter();
}

