/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.TypeRef;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.property.PropertyImpl;
import com.sun.xml.bind.v2.runtime.property.TagAndType;
import com.sun.xml.bind.v2.runtime.property.UnmarshallerChain;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.DefaultValueLoaderDecorator;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader;
import com.sun.xml.bind.v2.util.QNameMap;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

final class SingleElementNodeProperty<BeanT, ValueT>
extends PropertyImpl<BeanT> {
    private final Accessor<BeanT, ValueT> acc;
    private final boolean nillable;
    private final QName[] acceptedElements;
    private final Map<Class, TagAndType> typeNames = new HashMap<Class, TagAndType>();
    private RuntimeElementPropertyInfo prop;
    private final Name nullTagName;

    public SingleElementNodeProperty(JAXBContextImpl context, RuntimeElementPropertyInfo prop) {
        super(context, prop);
        this.acc = prop.getAccessor().optimize(context);
        this.prop = prop;
        QName nt = null;
        boolean nil = false;
        this.acceptedElements = new QName[prop.getTypes().size()];
        for (int i = 0; i < this.acceptedElements.length; ++i) {
            this.acceptedElements[i] = prop.getTypes().get(i).getTagName();
        }
        for (RuntimeTypeRef runtimeTypeRef : prop.getTypes()) {
            JaxBeanInfo beanInfo = context.getOrCreate(runtimeTypeRef.getTarget());
            if (nt == null) {
                nt = runtimeTypeRef.getTagName();
            }
            this.typeNames.put(beanInfo.jaxbType, new TagAndType(context.nameBuilder.createElementName(runtimeTypeRef.getTagName()), beanInfo));
            nil |= runtimeTypeRef.isNillable();
        }
        this.nullTagName = context.nameBuilder.createElementName(nt);
        this.nillable = nil;
    }

    @Override
    public void wrapUp() {
        super.wrapUp();
        this.prop = null;
    }

    @Override
    public void reset(BeanT bean) throws AccessorException {
        this.acc.set(bean, null);
    }

    @Override
    public String getIdValue(BeanT beanT) {
        return null;
    }

    @Override
    public void serializeBody(BeanT o, XMLSerializer w, Object outerPeer) throws SAXException, AccessorException, IOException, XMLStreamException {
        ValueT v = this.acc.get(o);
        if (v != null) {
            boolean addNilDecl;
            Class<?> vtype = v.getClass();
            TagAndType tt = this.typeNames.get(vtype);
            if (tt == null) {
                for (Map.Entry<Class, TagAndType> e : this.typeNames.entrySet()) {
                    if (!e.getKey().isAssignableFrom(vtype)) continue;
                    tt = e.getValue();
                    break;
                }
            }
            boolean bl = addNilDecl = o instanceof JAXBElement && ((JAXBElement)o).isNil();
            if (tt == null) {
                w.startElement(this.typeNames.values().iterator().next().tagName, null);
                w.childAsXsiType(v, this.fieldName, w.grammar.getBeanInfo(Object.class), addNilDecl && this.nillable);
            } else {
                w.startElement(tt.tagName, null);
                w.childAsXsiType(v, this.fieldName, tt.beanInfo, addNilDecl && this.nillable);
            }
            w.endElement();
        } else if (this.nillable) {
            w.startElement(this.nullTagName, null);
            w.writeXsiNilTrue();
            w.endElement();
        }
    }

    @Override
    public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<ChildLoader> handlers) {
        JAXBContextImpl context = chain.context;
        for (TypeRef typeRef : this.prop.getTypes()) {
            JaxBeanInfo bi = context.getOrCreate((RuntimeTypeInfo)((Object)typeRef.getTarget()));
            Loader l = bi.getLoader(context, !Modifier.isFinal(bi.jaxbType.getModifiers()));
            if (typeRef.getDefaultValue() != null) {
                l = new DefaultValueLoaderDecorator(l, typeRef.getDefaultValue());
            }
            if (this.nillable || chain.context.allNillable) {
                l = new XsiNilLoader.Single(l, this.acc);
            }
            handlers.put(typeRef.getTagName(), new ChildLoader(l, this.acc));
        }
    }

    @Override
    public PropertyKind getKind() {
        return PropertyKind.ELEMENT;
    }

    @Override
    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        for (QName n : this.acceptedElements) {
            if (!n.getNamespaceURI().equals(nsUri) || !n.getLocalPart().equals(localName)) continue;
            return this.acc;
        }
        return null;
    }
}

