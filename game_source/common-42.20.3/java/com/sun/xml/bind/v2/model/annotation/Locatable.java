/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.runtime.Location;

public interface Locatable {
    public Locatable getUpstream();

    public Location getLocation();
}

