/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.Location;

public class MethodLocatable<M>
implements Locatable {
    private final Locatable upstream;
    private final M method;
    private final Navigator<?, ?, ?, M> nav;

    public MethodLocatable(Locatable upstream, M method, Navigator<?, ?, ?, M> nav) {
        this.upstream = upstream;
        this.method = method;
        this.nav = nav;
    }

    @Override
    public Locatable getUpstream() {
        return this.upstream;
    }

    @Override
    public Location getLocation() {
        return this.nav.getMethodLocation(this.method);
    }
}

