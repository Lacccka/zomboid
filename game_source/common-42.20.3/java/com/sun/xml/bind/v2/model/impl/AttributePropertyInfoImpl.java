/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.bind.v2.model.core.AttributePropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.impl.ClassInfoImpl;
import com.sun.xml.bind.v2.model.impl.PropertySeed;
import com.sun.xml.bind.v2.model.impl.SingleTypePropertyInfoImpl;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;

class AttributePropertyInfoImpl<TypeT, ClassDeclT, FieldT, MethodT>
extends SingleTypePropertyInfoImpl<TypeT, ClassDeclT, FieldT, MethodT>
implements AttributePropertyInfo<TypeT, ClassDeclT> {
    private final QName xmlName;
    private final boolean isRequired;

    AttributePropertyInfoImpl(ClassInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> parent, PropertySeed<TypeT, ClassDeclT, FieldT, MethodT> seed) {
        super(parent, seed);
        XmlAttribute att = seed.readAnnotation(XmlAttribute.class);
        assert (att != null);
        this.isRequired = att.required() ? true : this.nav().isPrimitive(this.getIndividualType());
        this.xmlName = this.calcXmlName(att);
    }

    private QName calcXmlName(XmlAttribute att) {
        String uri = att.namespace();
        String local = att.name();
        if (local.equals("##default")) {
            local = NameConverter.standard.toVariableName(this.getName());
        }
        if (uri.equals("##default")) {
            XmlSchema xs = this.reader().getPackageAnnotation(XmlSchema.class, this.parent.getClazz(), this);
            if (xs != null) {
                switch (xs.attributeFormDefault()) {
                    case QUALIFIED: {
                        uri = this.parent.getTypeName().getNamespaceURI();
                        if (uri.length() != 0) break;
                        uri = this.parent.builder.defaultNsUri;
                        break;
                    }
                    case UNQUALIFIED: 
                    case UNSET: {
                        uri = "";
                    }
                }
            } else {
                uri = "";
            }
        }
        return new QName(uri.intern(), local.intern());
    }

    @Override
    public boolean isRequired() {
        return this.isRequired;
    }

    @Override
    public final QName getXmlName() {
        return this.xmlName;
    }

    @Override
    public final PropertyKind kind() {
        return PropertyKind.ATTRIBUTE;
    }
}

