/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.annotation;

import java.lang.annotation.Annotation;

public interface AnnotationSource {
    public <A extends Annotation> A readAnnotation(Class<A> var1);

    public boolean hasAnnotation(Class<? extends Annotation> var1);
}

