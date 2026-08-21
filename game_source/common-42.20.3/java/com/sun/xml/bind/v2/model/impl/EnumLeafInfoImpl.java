/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.EnumLeafInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.impl.EnumConstantImpl;
import com.sun.xml.bind.v2.model.impl.ModelBuilder;
import com.sun.xml.bind.v2.model.impl.TypeInfoImpl;
import com.sun.xml.bind.v2.runtime.Location;
import java.util.Collection;
import java.util.Iterator;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.namespace.QName;

class EnumLeafInfoImpl<T, C, F, M>
extends TypeInfoImpl<T, C, F, M>
implements EnumLeafInfo<T, C>,
Element<T, C>,
Iterable<EnumConstantImpl<T, C, F, M>> {
    final C clazz;
    NonElement<T, C> baseType;
    private final T type;
    private final QName typeName;
    private EnumConstantImpl<T, C, F, M> firstConstant;
    private QName elementName;
    protected boolean tokenStringType;

    public EnumLeafInfoImpl(ModelBuilder<T, C, F, M> builder, Locatable upstream, C clazz, T type) {
        super(builder, upstream);
        this.clazz = clazz;
        this.type = type;
        this.elementName = this.parseElementName(clazz);
        this.typeName = this.parseTypeName(clazz);
        XmlEnum xe = builder.reader.getClassAnnotation(XmlEnum.class, clazz, this);
        if (xe != null) {
            Object base = builder.reader.getClassValue(xe, "value");
            this.baseType = builder.getTypeInfo(base, this);
        } else {
            this.baseType = builder.getTypeInfo(builder.nav.ref(String.class), this);
        }
    }

    protected void calcConstants() {
        EnumConstantImpl last = null;
        Collection fields = this.nav().getDeclaredFields(this.clazz);
        for (Object f : fields) {
            XmlSchemaType schemaTypeAnnotation;
            if (!this.nav().isSameType(this.nav().getFieldType(f), this.nav().ref(String.class)) || (schemaTypeAnnotation = this.builder.reader.getFieldAnnotation(XmlSchemaType.class, f, this)) == null || !"token".equals(schemaTypeAnnotation.name())) continue;
            this.tokenStringType = true;
            break;
        }
        FieldT[] constants = this.nav().getEnumConstants(this.clazz);
        for (int i = constants.length - 1; i >= 0; --i) {
            Object constant = constants[i];
            String name = this.nav().getFieldName(constant);
            XmlEnumValue xev = this.builder.reader.getFieldAnnotation(XmlEnumValue.class, constant, this);
            String literal = xev == null ? name : xev.value();
            last = this.createEnumConstant(name, literal, constant, last);
        }
        this.firstConstant = last;
    }

    protected EnumConstantImpl<T, C, F, M> createEnumConstant(String name, String literal, F constant, EnumConstantImpl<T, C, F, M> last) {
        return new EnumConstantImpl<T, C, F, M>(this, name, literal, last);
    }

    @Override
    public T getType() {
        return this.type;
    }

    public boolean isToken() {
        return this.tokenStringType;
    }

    @Override
    public final boolean canBeReferencedByIDREF() {
        return false;
    }

    @Override
    public QName getTypeName() {
        return this.typeName;
    }

    @Override
    public C getClazz() {
        return this.clazz;
    }

    @Override
    public NonElement<T, C> getBaseType() {
        return this.baseType;
    }

    @Override
    public boolean isSimpleType() {
        return true;
    }

    @Override
    public Location getLocation() {
        return this.nav().getClassLocation(this.clazz);
    }

    @Override
    public Iterable<? extends EnumConstantImpl<T, C, F, M>> getConstants() {
        if (this.firstConstant == null) {
            this.calcConstants();
        }
        return this;
    }

    @Override
    public void link() {
        this.getConstants();
        super.link();
    }

    @Override
    public Element<T, C> getSubstitutionHead() {
        return null;
    }

    @Override
    public QName getElementName() {
        return this.elementName;
    }

    @Override
    public boolean isElement() {
        return this.elementName != null;
    }

    @Override
    public Element<T, C> asElement() {
        if (this.isElement()) {
            return this;
        }
        return null;
    }

    @Override
    public ClassInfo<T, C> getScope() {
        return null;
    }

    @Override
    public Iterator<EnumConstantImpl<T, C, F, M>> iterator() {
        return new Iterator<EnumConstantImpl<T, C, F, M>>(){
            private EnumConstantImpl<T, C, F, M> next;
            {
                this.next = EnumLeafInfoImpl.this.firstConstant;
            }

            @Override
            public boolean hasNext() {
                return this.next != null;
            }

            @Override
            public EnumConstantImpl<T, C, F, M> next() {
                EnumConstantImpl r = this.next;
                this.next = this.next.next;
                return r;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}

