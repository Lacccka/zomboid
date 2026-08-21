/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.nav;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

abstract class TypeVisitor<T, P> {
    TypeVisitor() {
    }

    public final T visit(Type t, P param) {
        assert (t != null);
        if (t instanceof Class) {
            return this.onClass((Class)t, param);
        }
        if (t instanceof ParameterizedType) {
            return this.onParameterizdType((ParameterizedType)t, param);
        }
        if (t instanceof GenericArrayType) {
            return this.onGenericArray((GenericArrayType)t, param);
        }
        if (t instanceof WildcardType) {
            return this.onWildcard((WildcardType)t, param);
        }
        if (t instanceof TypeVariable) {
            return this.onVariable((TypeVariable)t, param);
        }
        assert (false);
        throw new IllegalArgumentException();
    }

    protected abstract T onClass(Class var1, P var2);

    protected abstract T onParameterizdType(ParameterizedType var1, P var2);

    protected abstract T onGenericArray(GenericArrayType var1, P var2);

    protected abstract T onVariable(TypeVariable var1, P var2);

    protected abstract T onWildcard(WildcardType var1, P var2);
}

