/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.impl.ElementInfoImpl;
import com.sun.xml.bind.v2.model.impl.RegistryInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeModelBuilder;
import com.sun.xml.bind.v2.model.impl.Utils;
import com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

final class RuntimeElementInfoImpl
extends ElementInfoImpl<Type, Class, Field, Method>
implements RuntimeElementInfo {
    private final Class<? extends XmlAdapter> adapterType;

    public RuntimeElementInfoImpl(RuntimeModelBuilder modelBuilder, RegistryInfoImpl registry, Method method) throws IllegalAnnotationException {
        super(modelBuilder, registry, method);
        Adapter a = this.getProperty().getAdapter();
        this.adapterType = a != null ? (Class)a.adapterType : null;
    }

    @Override
    protected ElementInfoImpl.PropertyImpl createPropertyImpl() {
        return new RuntimePropertyImpl();
    }

    @Override
    public RuntimeElementPropertyInfo getProperty() {
        return (RuntimeElementPropertyInfo)super.getProperty();
    }

    @Override
    public Class<? extends JAXBElement> getType() {
        return (Class)Utils.REFLECTION_NAVIGATOR.erasure((Type)super.getType());
    }

    @Override
    public RuntimeClassInfo getScope() {
        return (RuntimeClassInfo)super.getScope();
    }

    @Override
    public RuntimeNonElement getContentType() {
        return (RuntimeNonElement)super.getContentType();
    }

    class RuntimePropertyImpl
    extends ElementInfoImpl.PropertyImpl
    implements RuntimeElementPropertyInfo,
    RuntimeTypeRef {
        RuntimePropertyImpl() {
        }

        @Override
        public Accessor getAccessor() {
            if (RuntimeElementInfoImpl.this.adapterType == null) {
                return Accessor.JAXB_ELEMENT_VALUE;
            }
            return Accessor.JAXB_ELEMENT_VALUE.adapt((Class)this.getAdapter().defaultType, RuntimeElementInfoImpl.this.adapterType);
        }

        @Override
        public Type getRawType() {
            return Collection.class;
        }

        @Override
        public Type getIndividualType() {
            return (Type)RuntimeElementInfoImpl.this.getContentType().getType();
        }

        @Override
        public boolean elementOnlyContent() {
            return false;
        }

        @Override
        public List<? extends RuntimeTypeRef> getTypes() {
            return Collections.singletonList(this);
        }

        @Override
        public List<? extends RuntimeNonElement> ref() {
            return super.ref();
        }

        @Override
        public RuntimeNonElement getTarget() {
            return (RuntimeNonElement)super.getTarget();
        }

        @Override
        public RuntimePropertyInfo getSource() {
            return this;
        }

        @Override
        public Transducer getTransducer() {
            return RuntimeModelBuilder.createTransducer(this);
        }
    }
}

