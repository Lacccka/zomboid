/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.property.PropertyImpl;
import com.sun.xml.bind.v2.runtime.property.UnmarshallerChain;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.DefaultValueLoaderDecorator;
import com.sun.xml.bind.v2.runtime.unmarshaller.LeafPropertyLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.LeafPropertyXsiLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader;
import com.sun.xml.bind.v2.util.QNameMap;
import java.io.IOException;
import java.lang.reflect.Modifier;
import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

final class SingleElementLeafProperty<BeanT>
extends PropertyImpl<BeanT> {
    private final Name tagName;
    private final boolean nillable;
    private final Accessor acc;
    private final String defaultValue;
    private final TransducedAccessor<BeanT> xacc;
    private final boolean improvedXsiTypeHandling;
    private final boolean idRef;

    public SingleElementLeafProperty(JAXBContextImpl context, RuntimeElementPropertyInfo prop) {
        super(context, prop);
        RuntimeTypeRef ref = prop.getTypes().get(0);
        this.tagName = context.nameBuilder.createElementName(ref.getTagName());
        assert (this.tagName != null);
        this.nillable = ref.isNillable();
        this.defaultValue = ref.getDefaultValue();
        this.acc = prop.getAccessor().optimize(context);
        this.xacc = TransducedAccessor.get(context, ref);
        assert (this.xacc != null);
        this.improvedXsiTypeHandling = context.improvedXsiTypeHandling;
        this.idRef = ref.getSource().id() == ID.IDREF;
    }

    @Override
    public void reset(BeanT o) throws AccessorException {
        this.acc.set(o, null);
    }

    @Override
    public String getIdValue(BeanT bean) throws AccessorException, SAXException {
        return this.xacc.print(bean).toString();
    }

    @Override
    public void serializeBody(BeanT o, XMLSerializer w, Object outerPeer) throws SAXException, AccessorException, IOException, XMLStreamException {
        boolean hasValue = this.xacc.hasValue(o);
        Object obj = null;
        try {
            obj = this.acc.getUnadapted(o);
        }
        catch (AccessorException accessorException) {
            // empty catch block
        }
        Class valueType = this.acc.getValueType();
        if (this.xsiTypeNeeded(o, w, obj, valueType)) {
            w.startElement(this.tagName, outerPeer);
            w.childAsXsiType(obj, this.fieldName, w.grammar.getBeanInfo(valueType), false);
            w.endElement();
        } else if (hasValue) {
            this.xacc.writeLeafElement(w, this.tagName, o, this.fieldName);
        } else if (this.nillable) {
            w.startElement(this.tagName, null);
            w.writeXsiNilTrue();
            w.endElement();
        }
    }

    private boolean xsiTypeNeeded(BeanT bean, XMLSerializer w, Object value, Class valueTypeClass) {
        if (!this.improvedXsiTypeHandling) {
            return false;
        }
        if (this.acc.isAdapted()) {
            return false;
        }
        if (value == null) {
            return false;
        }
        if (value.getClass().equals(valueTypeClass)) {
            return false;
        }
        if (this.idRef) {
            return false;
        }
        if (valueTypeClass.isPrimitive()) {
            return false;
        }
        return this.acc.isValueTypeAbstractable() || this.isNillableAbstract(bean, w.grammar, value, valueTypeClass);
    }

    private boolean isNillableAbstract(BeanT bean, JAXBContextImpl context, Object value, Class valueTypeClass) {
        if (!this.nillable) {
            return false;
        }
        if (valueTypeClass != Object.class) {
            return false;
        }
        if (bean.getClass() != JAXBElement.class) {
            return false;
        }
        JAXBElement jaxbElement = (JAXBElement)bean;
        Class<?> valueClass = value.getClass();
        Class declaredTypeClass = jaxbElement.getDeclaredType();
        if (declaredTypeClass.equals(valueClass)) {
            return false;
        }
        if (!declaredTypeClass.isAssignableFrom(valueClass)) {
            return false;
        }
        if (!Modifier.isAbstract(declaredTypeClass.getModifiers())) {
            return false;
        }
        return this.acc.isAbstractable(declaredTypeClass);
    }

    @Override
    public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<ChildLoader> handlers) {
        Loader l = new LeafPropertyLoader(this.xacc);
        if (this.defaultValue != null) {
            l = new DefaultValueLoaderDecorator(l, this.defaultValue);
        }
        if (this.nillable || chain.context.allNillable) {
            l = new XsiNilLoader.Single(l, this.acc);
        }
        if (this.improvedXsiTypeHandling) {
            l = new LeafPropertyXsiLoader(l, this.xacc, this.acc);
        }
        handlers.put(this.tagName, new ChildLoader(l, null));
    }

    @Override
    public PropertyKind getKind() {
        return PropertyKind.ELEMENT;
    }

    @Override
    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        if (this.tagName.equals(nsUri, localName)) {
            return this.acc;
        }
        return null;
    }
}

