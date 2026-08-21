/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.core.TypeInfo;
import javax.xml.namespace.QName;

public interface NonElement<T, C>
extends TypeInfo<T, C> {
    public static final QName ANYTYPE_NAME = new QName("http://www.w3.org/2001/XMLSchema", "anyType");

    public QName getTypeName();

    public boolean isSimpleType();
}

