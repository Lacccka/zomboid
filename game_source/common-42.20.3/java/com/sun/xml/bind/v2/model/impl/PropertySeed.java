/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.AnnotationSource;
import com.sun.xml.bind.v2.model.annotation.Locatable;

interface PropertySeed<T, C, F, M>
extends Locatable,
AnnotationSource {
    public String getName();

    public T getRawType();
}

