/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.Location;

public class ClassLocatable<C>
implements Locatable {
    private final Locatable upstream;
    private final C clazz;
    private final Navigator<?, C, ?, ?> nav;

    public ClassLocatable(Locatable upstream, C clazz, Navigator<?, C, ?, ?> nav) {
        this.upstream = upstream;
        this.clazz = clazz;
        this.nav = nav;
    }

    @Override
    public Locatable getUpstream() {
        return this.upstream;
    }

    @Override
    public Location getLocation() {
        return this.nav.getClassLocation(this.clazz);
    }
}

