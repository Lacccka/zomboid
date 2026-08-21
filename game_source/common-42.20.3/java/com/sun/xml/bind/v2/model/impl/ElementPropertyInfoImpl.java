/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.istack.FinalArrayList;
import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.impl.ClassInfoImpl;
import com.sun.xml.bind.v2.model.impl.ERPropertyInfoImpl;
import com.sun.xml.bind.v2.model.impl.Messages;
import com.sun.xml.bind.v2.model.impl.PropertySeed;
import com.sun.xml.bind.v2.model.impl.TypeRefImpl;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import java.lang.annotation.Annotation;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlList;
import javax.xml.namespace.QName;

class ElementPropertyInfoImpl<TypeT, ClassDeclT, FieldT, MethodT>
extends ERPropertyInfoImpl<TypeT, ClassDeclT, FieldT, MethodT>
implements ElementPropertyInfo<TypeT, ClassDeclT> {
    private List<TypeRefImpl<TypeT, ClassDeclT>> types;
    private final List<TypeInfo<TypeT, ClassDeclT>> ref = new AbstractList<TypeInfo<TypeT, ClassDeclT>>(){

        @Override
        public TypeInfo<TypeT, ClassDeclT> get(int index) {
            return ElementPropertyInfoImpl.this.getTypes().get(index).getTarget();
        }

        @Override
        public int size() {
            return ElementPropertyInfoImpl.this.getTypes().size();
        }
    };
    private Boolean isRequired;
    private final boolean isValueList = this.seed.hasAnnotation(XmlList.class);

    ElementPropertyInfoImpl(ClassInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> parent, PropertySeed<TypeT, ClassDeclT, FieldT, MethodT> propertySeed) {
        super(parent, propertySeed);
    }

    @Override
    public List<? extends TypeRefImpl<TypeT, ClassDeclT>> getTypes() {
        if (this.types == null) {
            this.types = new FinalArrayList<TypeRefImpl<TypeT, ClassDeclT>>();
            XmlElement[] ann = null;
            XmlElement xe = this.seed.readAnnotation(XmlElement.class);
            XmlElements xes = this.seed.readAnnotation(XmlElements.class);
            if (xe != null && xes != null) {
                this.parent.builder.reportError(new IllegalAnnotationException(Messages.MUTUALLY_EXCLUSIVE_ANNOTATIONS.format(this.nav().getClassName(this.parent.getClazz()) + '#' + this.seed.getName(), xe.annotationType().getName(), xes.annotationType().getName()), (Annotation)xe, xes));
            }
            this.isRequired = true;
            if (xe != null) {
                ann = new XmlElement[]{xe};
            } else if (xes != null) {
                ann = xes.value();
            }
            if (ann == null) {
                Object t = this.getIndividualType();
                if (!this.nav().isPrimitive(t) || this.isCollection()) {
                    this.isRequired = false;
                }
                this.types.add(this.createTypeRef(this.calcXmlName((XmlElement)null), t, this.isCollection(), null));
            } else {
                for (XmlElement item : ann) {
                    QName name = this.calcXmlName(item);
                    Object type = this.reader().getClassValue(item, "type");
                    if (this.nav().isSameType(type, this.nav().ref(XmlElement.DEFAULT.class))) {
                        type = this.getIndividualType();
                    }
                    if (!(this.nav().isPrimitive(type) && !this.isCollection() || item.required())) {
                        this.isRequired = false;
                    }
                    this.types.add(this.createTypeRef(name, type, item.nillable(), this.getDefaultValue(item.defaultValue())));
                }
            }
            this.types = Collections.unmodifiableList(this.types);
            assert (!this.types.contains(null));
        }
        return this.types;
    }

    private String getDefaultValue(String value) {
        if (value.equals("\u0000")) {
            return null;
        }
        return value;
    }

    protected TypeRefImpl<TypeT, ClassDeclT> createTypeRef(QName name, TypeT type, boolean isNillable, String defaultValue) {
        return new TypeRefImpl(this, name, type, isNillable, defaultValue);
    }

    @Override
    public boolean isValueList() {
        return this.isValueList;
    }

    @Override
    public boolean isRequired() {
        if (this.isRequired == null) {
            this.getTypes();
        }
        return this.isRequired;
    }

    public List<? extends TypeInfo<TypeT, ClassDeclT>> ref() {
        return this.ref;
    }

    @Override
    public final PropertyKind kind() {
        return PropertyKind.ELEMENT;
    }

    @Override
    protected void link() {
        super.link();
        for (TypeRefImpl<TypeT, ClassDeclT> ref : this.getTypes()) {
            ref.link();
        }
        if (this.isValueList()) {
            if (this.id() != ID.IDREF) {
                for (TypeRefImpl<TypeT, ClassDeclT> ref : this.types) {
                    if (ref.getTarget().isSimpleType()) continue;
                    this.parent.builder.reportError(new IllegalAnnotationException(Messages.XMLLIST_NEEDS_SIMPLETYPE.format(this.nav().getTypeName(ref.getTarget().getType())), this));
                    break;
                }
            }
            if (!this.isCollection()) {
                this.parent.builder.reportError(new IllegalAnnotationException(Messages.XMLLIST_ON_SINGLE_PROPERTY.format(new Object[0]), this));
            }
        }
    }
}

