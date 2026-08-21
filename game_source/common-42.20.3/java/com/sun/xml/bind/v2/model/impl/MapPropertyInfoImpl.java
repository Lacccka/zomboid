/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.core.MapPropertyInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.impl.ClassInfoImpl;
import com.sun.xml.bind.v2.model.impl.PropertyInfoImpl;
import com.sun.xml.bind.v2.model.impl.PropertySeed;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.namespace.QName;

class MapPropertyInfoImpl<T, C, F, M>
extends PropertyInfoImpl<T, C, F, M>
implements MapPropertyInfo<T, C> {
    private final QName xmlName;
    private boolean nil;
    private final T keyType;
    private final T valueType;
    private NonElement<T, C> keyTypeInfo;
    private NonElement<T, C> valueTypeInfo;

    public MapPropertyInfoImpl(ClassInfoImpl<T, C, F, M> ci, PropertySeed<T, C, F, M> seed) {
        super(ci, seed);
        XmlElementWrapper xe = seed.readAnnotation(XmlElementWrapper.class);
        this.xmlName = this.calcXmlName(xe);
        this.nil = xe != null && xe.nillable();
        Object raw = this.getRawType();
        Object bt = this.nav().getBaseClass(raw, this.nav().asDecl(Map.class));
        assert (bt != null);
        if (this.nav().isParameterizedType(bt)) {
            this.keyType = this.nav().getTypeArgument(bt, 0);
            this.valueType = this.nav().getTypeArgument(bt, 1);
        } else {
            this.valueType = this.nav().ref(Object.class);
            this.keyType = this.valueType;
        }
    }

    @Override
    public Collection<? extends TypeInfo<T, C>> ref() {
        return Arrays.asList(this.getKeyType(), this.getValueType());
    }

    @Override
    public final PropertyKind kind() {
        return PropertyKind.MAP;
    }

    @Override
    public QName getXmlName() {
        return this.xmlName;
    }

    @Override
    public boolean isCollectionNillable() {
        return this.nil;
    }

    @Override
    public NonElement<T, C> getKeyType() {
        if (this.keyTypeInfo == null) {
            this.keyTypeInfo = this.getTarget(this.keyType);
        }
        return this.keyTypeInfo;
    }

    @Override
    public NonElement<T, C> getValueType() {
        if (this.valueTypeInfo == null) {
            this.valueTypeInfo = this.getTarget(this.valueType);
        }
        return this.valueTypeInfo;
    }

    public NonElement<T, C> getTarget(T type) {
        assert (this.parent.builder != null) : "this method must be called during the build stage";
        return this.parent.builder.getTypeInfo(type, this);
    }
}

