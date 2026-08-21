/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import javax.xml.namespace.QName;

public interface Element<T, C>
extends TypeInfo<T, C> {
    public QName getElementName();

    public Element<T, C> getSubstitutionHead();

    public ClassInfo<T, C> getScope();
}

