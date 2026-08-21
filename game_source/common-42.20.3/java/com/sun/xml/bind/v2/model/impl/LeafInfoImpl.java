/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.LeafInfo;
import com.sun.xml.bind.v2.runtime.Location;
import javax.xml.namespace.QName;

abstract class LeafInfoImpl<TypeT, ClassDeclT>
implements LeafInfo<TypeT, ClassDeclT>,
Location {
    private final TypeT type;
    private final QName typeName;

    protected LeafInfoImpl(TypeT type, QName typeName) {
        assert (type != null);
        this.type = type;
        this.typeName = typeName;
    }

    @Override
    public TypeT getType() {
        return this.type;
    }

    @Override
    public final boolean canBeReferencedByIDREF() {
        return false;
    }

    @Override
    public QName getTypeName() {
        return this.typeName;
    }

    @Override
    public Locatable getUpstream() {
        return null;
    }

    @Override
    public Location getLocation() {
        return this;
    }

    @Override
    public boolean isSimpleType() {
        return true;
    }

    @Override
    public String toString() {
        return this.type.toString();
    }
}

