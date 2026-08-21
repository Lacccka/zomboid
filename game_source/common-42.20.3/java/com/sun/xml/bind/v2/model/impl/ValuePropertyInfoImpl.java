/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.ValuePropertyInfo;
import com.sun.xml.bind.v2.model.impl.ClassInfoImpl;
import com.sun.xml.bind.v2.model.impl.PropertySeed;
import com.sun.xml.bind.v2.model.impl.SingleTypePropertyInfoImpl;

class ValuePropertyInfoImpl<TypeT, ClassDeclT, FieldT, MethodT>
extends SingleTypePropertyInfoImpl<TypeT, ClassDeclT, FieldT, MethodT>
implements ValuePropertyInfo<TypeT, ClassDeclT> {
    ValuePropertyInfoImpl(ClassInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> parent, PropertySeed<TypeT, ClassDeclT, FieldT, MethodT> seed) {
        super(parent, seed);
    }

    @Override
    public PropertyKind kind() {
        return PropertyKind.VALUE;
    }
}

