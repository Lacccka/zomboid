/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.annotation.FieldLocatable;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.impl.EnumConstantImpl;
import com.sun.xml.bind.v2.model.impl.EnumLeafInfoImpl;
import com.sun.xml.bind.v2.model.impl.Messages;
import com.sun.xml.bind.v2.model.impl.RuntimeEnumConstantImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeModelBuilder;
import com.sun.xml.bind.v2.model.runtime.RuntimeEnumLeafInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

final class RuntimeEnumLeafInfoImpl<T extends Enum<T>, B>
extends EnumLeafInfoImpl<Type, Class, Field, Method>
implements RuntimeEnumLeafInfo,
Transducer<T> {
    private final Transducer<B> baseXducer;
    private final Map<B, T> parseMap = new HashMap<B, T>();
    private final Map<T, B> printMap;

    public Transducer<T> getTransducer() {
        return this;
    }

    RuntimeEnumLeafInfoImpl(RuntimeModelBuilder builder, Locatable upstream, Class<T> enumType) {
        super(builder, upstream, enumType, enumType);
        this.printMap = new EnumMap<T, B>(enumType);
        this.baseXducer = ((RuntimeNonElement)this.baseType).getTransducer();
    }

    public RuntimeEnumConstantImpl createEnumConstant(String name, String literal, Field constant, EnumConstantImpl<Type, Class, Field, Method> last) {
        Enum t;
        try {
            try {
                constant.setAccessible(true);
            }
            catch (SecurityException securityException) {
                // empty catch block
            }
            t = (Enum)constant.get(null);
        }
        catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
        Object b = null;
        try {
            b = this.baseXducer.parse(literal);
        }
        catch (Exception e) {
            this.builder.reportError(new IllegalAnnotationException(Messages.INVALID_XML_ENUM_VALUE.format(literal, ((Type)this.baseType.getType()).toString()), e, new FieldLocatable<Field>(this, constant, this.nav())));
        }
        this.parseMap.put(b, t);
        this.printMap.put(t, b);
        return new RuntimeEnumConstantImpl(this, name, literal, last);
    }

    @Override
    public QName[] getTypeNames() {
        return new QName[]{this.getTypeName()};
    }

    @Override
    public Class getClazz() {
        return (Class)this.clazz;
    }

    @Override
    public boolean useNamespace() {
        return this.baseXducer.useNamespace();
    }

    @Override
    public void declareNamespace(T t, XMLSerializer w) throws AccessorException {
        this.baseXducer.declareNamespace(this.printMap.get(t), w);
    }

    @Override
    public CharSequence print(T t) throws AccessorException {
        return this.baseXducer.print(this.printMap.get(t));
    }

    @Override
    public T parse(CharSequence lexical) throws AccessorException, SAXException {
        Object b = this.baseXducer.parse(lexical);
        if (this.tokenStringType) {
            b = ((String)b).trim();
        }
        return (T)((Enum)this.parseMap.get(b));
    }

    @Override
    public void writeText(XMLSerializer w, T t, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
        this.baseXducer.writeText(w, this.printMap.get(t), fieldName);
    }

    @Override
    public void writeLeafElement(XMLSerializer w, Name tagName, T o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
        this.baseXducer.writeLeafElement(w, tagName, this.printMap.get(o), fieldName);
    }

    @Override
    public QName getTypeName(T instance) {
        return null;
    }
}

