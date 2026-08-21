/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.impl.ClassInfoImpl;
import com.sun.xml.bind.v2.model.impl.PropertySeed;
import com.sun.xml.bind.v2.runtime.Location;
import java.beans.Introspector;
import java.lang.annotation.Annotation;

class GetterSetterPropertySeed<TypeT, ClassDeclT, FieldT, MethodT>
implements PropertySeed<TypeT, ClassDeclT, FieldT, MethodT> {
    protected final MethodT getter;
    protected final MethodT setter;
    private ClassInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> parent;

    GetterSetterPropertySeed(ClassInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> parent, MethodT getter, MethodT setter) {
        this.parent = parent;
        this.getter = getter;
        this.setter = setter;
        if (getter == null && setter == null) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public TypeT getRawType() {
        if (this.getter != null) {
            return this.parent.nav().getReturnType(this.getter);
        }
        return this.parent.nav().getMethodParameters(this.setter)[0];
    }

    @Override
    public <A extends Annotation> A readAnnotation(Class<A> annotation) {
        return this.parent.reader().getMethodAnnotation(annotation, this.getter, this.setter, this);
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return this.parent.reader().hasMethodAnnotation(annotationType, this.getName(), this.getter, this.setter, this);
    }

    @Override
    public String getName() {
        if (this.getter != null) {
            return this.getName(this.getter);
        }
        return this.getName(this.setter);
    }

    private String getName(MethodT m) {
        String seed = this.parent.nav().getMethodName(m);
        String lseed = seed.toLowerCase();
        if (lseed.startsWith("get") || lseed.startsWith("set")) {
            return GetterSetterPropertySeed.camelize(seed.substring(3));
        }
        if (lseed.startsWith("is")) {
            return GetterSetterPropertySeed.camelize(seed.substring(2));
        }
        return seed;
    }

    private static String camelize(String s) {
        return Introspector.decapitalize(s);
    }

    @Override
    public Locatable getUpstream() {
        return this.parent;
    }

    @Override
    public Location getLocation() {
        if (this.getter != null) {
            return this.parent.nav().getMethodLocation(this.getter);
        }
        return this.parent.nav().getMethodLocation(this.setter);
    }
}

