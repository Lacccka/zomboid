/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import javax.xml.namespace.QName;

public interface MapPropertyInfo<T, C>
extends PropertyInfo<T, C> {
    public QName getXmlName();

    public boolean isCollectionNillable();

    public NonElement<T, C> getKeyType();

    public NonElement<T, C> getValueType();
}

