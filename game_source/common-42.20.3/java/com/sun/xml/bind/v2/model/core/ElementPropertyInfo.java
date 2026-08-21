/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.TypeRef;
import java.util.List;
import javax.xml.namespace.QName;

public interface ElementPropertyInfo<T, C>
extends PropertyInfo<T, C> {
    public List<? extends TypeRef<T, C>> getTypes();

    public QName getXmlName();

    public boolean isCollectionRequired();

    public boolean isCollectionNillable();

    public boolean isValueList();

    public boolean isRequired();

    @Override
    public Adapter<T, C> getAdapter();
}

