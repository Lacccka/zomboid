/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.core.WildcardMode;
import java.util.Collection;
import java.util.Set;
import javax.xml.namespace.QName;

public interface ReferencePropertyInfo<T, C>
extends PropertyInfo<T, C> {
    public Set<? extends Element<T, C>> getElements();

    @Override
    public Collection<? extends TypeInfo<T, C>> ref();

    public QName getXmlName();

    public boolean isCollectionNillable();

    public boolean isCollectionRequired();

    public boolean isMixed();

    public WildcardMode getWildcard();

    public C getDOMHandler();

    public boolean isRequired();

    @Override
    public Adapter<T, C> getAdapter();
}

