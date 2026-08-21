/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.Location;
import javax.xml.namespace.QName;

class AnyTypeImpl<T, C>
implements NonElement<T, C> {
    private final T type;
    private final Navigator<T, C, ?, ?> nav;

    public AnyTypeImpl(Navigator<T, C, ?, ?> nav) {
        this.type = nav.ref(Object.class);
        this.nav = nav;
    }

    @Override
    public QName getTypeName() {
        return ANYTYPE_NAME;
    }

    @Override
    public T getType() {
        return this.type;
    }

    @Override
    public Locatable getUpstream() {
        return null;
    }

    @Override
    public boolean isSimpleType() {
        return false;
    }

    @Override
    public Location getLocation() {
        return this.nav.getClassLocation(this.nav.asDecl(Object.class));
    }

    @Override
    public final boolean canBeReferencedByIDREF() {
        return true;
    }
}

