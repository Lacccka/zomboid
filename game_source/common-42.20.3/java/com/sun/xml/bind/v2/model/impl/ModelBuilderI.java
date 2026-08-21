/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.nav.Navigator;

public interface ModelBuilderI<T, C, F, M> {
    public Navigator<T, C, F, M> getNavigator();

    public AnnotationReader<T, C, F, M> getReader();
}

