/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.api;

import com.sun.xml.bind.api.Messages;
import com.sun.xml.bind.api.Utils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import javax.xml.namespace.QName;

public final class TypeReference {
    public final QName tagName;
    public final Type type;
    public final Annotation[] annotations;

    public TypeReference(QName tagName, Type type, Annotation ... annotations) {
        if (tagName == null || type == null || annotations == null) {
            String nullArgs = "";
            if (tagName == null) {
                nullArgs = "tagName";
            }
            if (type == null) {
                nullArgs = nullArgs + (nullArgs.length() > 0 ? ", type" : "type");
            }
            if (annotations == null) {
                nullArgs = nullArgs + (nullArgs.length() > 0 ? ", annotations" : "annotations");
            }
            Messages.ARGUMENT_CANT_BE_NULL.format(nullArgs);
            throw new IllegalArgumentException(Messages.ARGUMENT_CANT_BE_NULL.format(nullArgs));
        }
        this.tagName = new QName(tagName.getNamespaceURI().intern(), tagName.getLocalPart().intern(), tagName.getPrefix());
        this.type = type;
        this.annotations = annotations;
    }

    public <A extends Annotation> A get(Class<A> annotationType) {
        for (Annotation a : this.annotations) {
            if (a.annotationType() != annotationType) continue;
            return (A)((Annotation)annotationType.cast(a));
        }
        return null;
    }

    public TypeReference toItemType() {
        Type base = Utils.REFLECTION_NAVIGATOR.getBaseClass(this.type, Collection.class);
        if (base == null) {
            return this;
        }
        return new TypeReference(this.tagName, Utils.REFLECTION_NAVIGATOR.getTypeArgument(base, 0), new Annotation[0]);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        TypeReference that = (TypeReference)o;
        if (!Arrays.equals(this.annotations, that.annotations)) {
            return false;
        }
        if (!this.tagName.equals(that.tagName)) {
            return false;
        }
        return this.type.equals(that.type);
    }

    public int hashCode() {
        int result = this.tagName.hashCode();
        result = 31 * result + this.type.hashCode();
        result = 31 * result + Arrays.hashCode(this.annotations);
        return result;
    }
}

