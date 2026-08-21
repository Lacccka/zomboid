/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.annotation;

import com.sun.istack.Nullable;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.ErrorHandler;
import java.lang.annotation.Annotation;

public interface AnnotationReader<T, C, F, M> {
    public void setErrorHandler(ErrorHandler var1);

    public <A extends Annotation> A getFieldAnnotation(Class<A> var1, F var2, Locatable var3);

    public boolean hasFieldAnnotation(Class<? extends Annotation> var1, F var2);

    public boolean hasClassAnnotation(C var1, Class<? extends Annotation> var2);

    public Annotation[] getAllFieldAnnotations(F var1, Locatable var2);

    public <A extends Annotation> A getMethodAnnotation(Class<A> var1, M var2, M var3, Locatable var4);

    public boolean hasMethodAnnotation(Class<? extends Annotation> var1, String var2, M var3, M var4, Locatable var5);

    public Annotation[] getAllMethodAnnotations(M var1, Locatable var2);

    public <A extends Annotation> A getMethodAnnotation(Class<A> var1, M var2, Locatable var3);

    public boolean hasMethodAnnotation(Class<? extends Annotation> var1, M var2);

    @Nullable
    public <A extends Annotation> A getMethodParameterAnnotation(Class<A> var1, M var2, int var3, Locatable var4);

    @Nullable
    public <A extends Annotation> A getClassAnnotation(Class<A> var1, C var2, Locatable var3);

    @Nullable
    public <A extends Annotation> A getPackageAnnotation(Class<A> var1, C var2, Locatable var3);

    public T getClassValue(Annotation var1, String var2);

    public T[] getClassArrayValue(Annotation var1, String var2);
}

