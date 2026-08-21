/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.Messages;
import com.sun.xml.bind.v2.model.core.ErrorHandler;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import java.lang.annotation.Annotation;

public abstract class AbstractInlineAnnotationReaderImpl<T, C, F, M>
implements AnnotationReader<T, C, F, M> {
    private ErrorHandler errorHandler;

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        if (errorHandler == null) {
            throw new IllegalArgumentException();
        }
        this.errorHandler = errorHandler;
    }

    public final ErrorHandler getErrorHandler() {
        assert (this.errorHandler != null) : "error handler must be set before use";
        return this.errorHandler;
    }

    @Override
    public final <A extends Annotation> A getMethodAnnotation(Class<A> annotation, M getter, M setter, Locatable srcPos) {
        Annotation a2;
        Annotation a1 = getter == null ? null : (Annotation)this.getMethodAnnotation(annotation, getter, srcPos);
        Annotation annotation2 = a2 = setter == null ? null : (Annotation)this.getMethodAnnotation(annotation, setter, srcPos);
        if (a1 == null) {
            if (a2 == null) {
                return null;
            }
            return (A)a2;
        }
        if (a2 == null) {
            return (A)a1;
        }
        this.getErrorHandler().error(new IllegalAnnotationException(Messages.DUPLICATE_ANNOTATIONS.format(annotation.getName(), this.fullName(getter), this.fullName(setter)), a1, a2));
        return (A)a1;
    }

    @Override
    public boolean hasMethodAnnotation(Class<? extends Annotation> annotation, String propertyName, M getter, M setter, Locatable srcPos) {
        boolean y;
        boolean x = getter != null && this.hasMethodAnnotation(annotation, getter);
        boolean bl = y = setter != null && this.hasMethodAnnotation(annotation, setter);
        if (x && y) {
            this.getMethodAnnotation(annotation, getter, setter, srcPos);
        }
        return x || y;
    }

    protected abstract String fullName(M var1);
}

