/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.runtime.Location;
import java.lang.annotation.Annotation;

public abstract class Quick
implements Annotation,
Locatable,
Location {
    private final Locatable upstream;

    protected Quick(Locatable upstream) {
        this.upstream = upstream;
    }

    protected abstract Annotation getAnnotation();

    protected abstract Quick newInstance(Locatable var1, Annotation var2);

    @Override
    public final Location getLocation() {
        return this;
    }

    @Override
    public final Locatable getUpstream() {
        return this.upstream;
    }

    @Override
    public final String toString() {
        return this.getAnnotation().toString();
    }
}

