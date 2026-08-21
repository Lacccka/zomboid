/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.runtime.AttributeAccessor;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Messages;
import com.sun.xml.bind.v2.runtime.NamespaceContext2;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.unmarshaller.DomLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiTypeLoader;
import java.io.IOException;
import javax.xml.bind.annotation.W3CDomHandler;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

final class AnyTypeBeanInfo
extends JaxBeanInfo<Object>
implements AttributeAccessor {
    private boolean nilIncluded = false;
    private static final W3CDomHandler domHandler = new W3CDomHandler();
    private static final DomLoader domLoader = new DomLoader<DOMResult>(domHandler);
    private final XsiTypeLoader substLoader = new XsiTypeLoader(this);

    public AnyTypeBeanInfo(JAXBContextImpl grammar, RuntimeTypeInfo anyTypeInfo) {
        super(grammar, anyTypeInfo, Object.class, new QName("http://www.w3.org/2001/XMLSchema", "anyType"), false, true, false);
    }

    @Override
    public String getElementNamespaceURI(Object element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getElementLocalName(Object element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object createInstance(UnmarshallingContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean reset(Object element, UnmarshallingContext context) {
        return false;
    }

    @Override
    public String getId(Object element, XMLSerializer target) {
        return null;
    }

    @Override
    public void serializeBody(Object element, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        NodeList childNodes = ((Element)element).getChildNodes();
        int len = childNodes.getLength();
        block4: for (int i = 0; i < len; ++i) {
            Node child = childNodes.item(i);
            switch (child.getNodeType()) {
                case 3: 
                case 4: {
                    target.text(child.getNodeValue(), null);
                    continue block4;
                }
                case 1: {
                    target.writeDom((Element)child, domHandler, null, null);
                }
            }
        }
    }

    @Override
    public void serializeAttributes(Object element, XMLSerializer target) throws SAXException {
        NamedNodeMap al = ((Element)element).getAttributes();
        int len = al.getLength();
        for (int i = 0; i < len; ++i) {
            Attr a = (Attr)al.item(i);
            String uri = a.getNamespaceURI();
            if (uri == null) {
                uri = "";
            }
            String local = a.getLocalName();
            String name = a.getName();
            if (local == null) {
                local = name;
            }
            if (uri.equals("http://www.w3.org/2001/XMLSchema-instance") && "nil".equals(local)) {
                this.isNilIncluded = true;
            }
            if (name.startsWith("xmlns")) continue;
            target.attribute(uri, local, a.getValue());
        }
    }

    @Override
    public void serializeRoot(Object element, XMLSerializer target) throws SAXException {
        target.reportError(new ValidationEventImpl(1, Messages.UNABLE_TO_MARSHAL_NON_ELEMENT.format(element.getClass().getName()), null, null));
    }

    @Override
    public void serializeURIs(Object element, XMLSerializer target) {
        NamedNodeMap al = ((Element)element).getAttributes();
        int len = al.getLength();
        NamespaceContext2 context = target.getNamespaceContext();
        for (int i = 0; i < len; ++i) {
            Attr a = (Attr)al.item(i);
            if ("xmlns".equals(a.getPrefix())) {
                context.force(a.getValue(), a.getLocalName());
                continue;
            }
            if ("xmlns".equals(a.getName())) {
                if (element instanceof Element) {
                    context.declareNamespace(a.getValue(), null, false);
                    continue;
                }
                context.force(a.getValue(), "");
                continue;
            }
            String nsUri = a.getNamespaceURI();
            if (nsUri == null || nsUri.length() <= 0) continue;
            context.declareNamespace(nsUri, a.getPrefix(), true);
        }
    }

    @Override
    public Transducer<Object> getTransducer() {
        return null;
    }

    @Override
    public Loader getLoader(JAXBContextImpl context, boolean typeSubstitutionCapable) {
        if (typeSubstitutionCapable) {
            return this.substLoader;
        }
        return domLoader;
    }
}

