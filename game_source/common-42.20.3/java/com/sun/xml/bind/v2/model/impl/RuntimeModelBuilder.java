/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.istack.Nullable;
import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.impl.ModelBuilder;
import com.sun.xml.bind.v2.model.impl.RegistryInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeArrayInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeBuiltinLeafInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeClassInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeElementInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeEnumLeafInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeTypeInfoSetImpl;
import com.sun.xml.bind.v2.model.impl.Utils;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElementRef;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfoSet;
import com.sun.xml.bind.v2.runtime.FilterTransducer;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.InlineBinaryTransducer;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.MimeTypedTransducer;
import com.sun.xml.bind.v2.runtime.SchemaTypeTransducer;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import javax.activation.MimeType;
import javax.xml.namespace.QName;
import org.xml.sax.SAXException;

public class RuntimeModelBuilder
extends ModelBuilder<Type, Class, Field, Method> {
    @Nullable
    public final JAXBContextImpl context;

    public RuntimeModelBuilder(JAXBContextImpl context, RuntimeAnnotationReader annotationReader, Map<Class, Class> subclassReplacements, String defaultNamespaceRemap) {
        super(annotationReader, Utils.REFLECTION_NAVIGATOR, subclassReplacements, defaultNamespaceRemap);
        this.context = context;
    }

    public RuntimeNonElement getClassInfo(Class clazz, Locatable upstream) {
        return (RuntimeNonElement)super.getClassInfo(clazz, upstream);
    }

    public RuntimeNonElement getClassInfo(Class clazz, boolean searchForSuperClass, Locatable upstream) {
        return (RuntimeNonElement)super.getClassInfo(clazz, searchForSuperClass, upstream);
    }

    protected RuntimeEnumLeafInfoImpl createEnumLeafInfo(Class clazz, Locatable upstream) {
        return new RuntimeEnumLeafInfoImpl(this, upstream, clazz);
    }

    protected RuntimeClassInfoImpl createClassInfo(Class clazz, Locatable upstream) {
        return new RuntimeClassInfoImpl(this, upstream, clazz);
    }

    public RuntimeElementInfoImpl createElementInfo(RegistryInfoImpl<Type, Class, Field, Method> registryInfo, Method method) throws IllegalAnnotationException {
        return new RuntimeElementInfoImpl(this, registryInfo, method);
    }

    public RuntimeArrayInfoImpl createArrayInfo(Locatable upstream, Type arrayType) {
        return new RuntimeArrayInfoImpl(this, upstream, (Class)arrayType);
    }

    protected RuntimeTypeInfoSetImpl createTypeInfoSet() {
        return new RuntimeTypeInfoSetImpl(this.reader);
    }

    public RuntimeTypeInfoSet link() {
        return (RuntimeTypeInfoSet)super.link();
    }

    public static Transducer createTransducer(RuntimeNonElementRef ref) {
        MimeType emt;
        Transducer t = ref.getTarget().getTransducer();
        RuntimePropertyInfo src = ref.getSource();
        ID id = src.id();
        if (id == ID.IDREF) {
            return RuntimeBuiltinLeafInfoImpl.STRING;
        }
        if (id == ID.ID) {
            t = new IDTransducerImpl(t);
        }
        if ((emt = src.getExpectedMimeType()) != null) {
            t = new MimeTypedTransducer(t, emt);
        }
        if (src.inlineBinaryData()) {
            t = new InlineBinaryTransducer(t);
        }
        if (src.getSchemaType() != null) {
            if (src.getSchemaType().equals(RuntimeModelBuilder.createXSSimpleType())) {
                return RuntimeBuiltinLeafInfoImpl.STRING;
            }
            t = new SchemaTypeTransducer(t, src.getSchemaType());
        }
        return t;
    }

    private static QName createXSSimpleType() {
        return new QName("http://www.w3.org/2001/XMLSchema", "anySimpleType");
    }

    private static final class IDTransducerImpl<ValueT>
    extends FilterTransducer<ValueT> {
        public IDTransducerImpl(Transducer<ValueT> core) {
            super(core);
        }

        @Override
        public ValueT parse(CharSequence lexical) throws AccessorException, SAXException {
            String value = WhiteSpaceProcessor.trim(lexical).toString();
            UnmarshallingContext.getInstance().addToIdTable(value);
            return this.core.parse(value);
        }
    }
}

