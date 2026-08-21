/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.unmarshaller.InfosetScanner;
import com.sun.xml.bind.v2.runtime.AssociationMap;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.MarshallerImpl;
import com.sun.xml.bind.v2.runtime.Messages;
import com.sun.xml.bind.v2.runtime.output.DOMOutput;
import com.sun.xml.bind.v2.runtime.unmarshaller.InterningXmlVisitor;
import com.sun.xml.bind.v2.runtime.unmarshaller.SAXConnector;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;
import javax.xml.bind.Binder;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.QName;
import javax.xml.validation.Schema;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class BinderImpl<XmlNode>
extends Binder<XmlNode> {
    private final JAXBContextImpl context;
    private UnmarshallerImpl unmarshaller;
    private MarshallerImpl marshaller;
    private final InfosetScanner<XmlNode> scanner;
    private final AssociationMap<XmlNode> assoc = new AssociationMap();

    BinderImpl(JAXBContextImpl _context, InfosetScanner<XmlNode> scanner) {
        this.context = _context;
        this.scanner = scanner;
    }

    private UnmarshallerImpl getUnmarshaller() {
        if (this.unmarshaller == null) {
            this.unmarshaller = new UnmarshallerImpl(this.context, this.assoc);
        }
        return this.unmarshaller;
    }

    private MarshallerImpl getMarshaller() {
        if (this.marshaller == null) {
            this.marshaller = new MarshallerImpl(this.context, this.assoc);
        }
        return this.marshaller;
    }

    @Override
    public void marshal(Object jaxbObject, XmlNode xmlNode) throws JAXBException {
        if (xmlNode == null || jaxbObject == null) {
            throw new IllegalArgumentException();
        }
        this.getMarshaller().marshal(jaxbObject, this.createOutput(xmlNode));
    }

    private DOMOutput createOutput(XmlNode xmlNode) {
        return new DOMOutput((Node)xmlNode, this.assoc);
    }

    @Override
    public Object updateJAXB(XmlNode xmlNode) throws JAXBException {
        return this.associativeUnmarshal(xmlNode, true, null);
    }

    @Override
    public Object unmarshal(XmlNode xmlNode) throws JAXBException {
        return this.associativeUnmarshal(xmlNode, false, null);
    }

    @Override
    public <T> JAXBElement<T> unmarshal(XmlNode xmlNode, Class<T> expectedType) throws JAXBException {
        if (expectedType == null) {
            throw new IllegalArgumentException();
        }
        return (JAXBElement)this.associativeUnmarshal(xmlNode, true, expectedType);
    }

    @Override
    public void setSchema(Schema schema) {
        this.getMarshaller().setSchema(schema);
        this.getUnmarshaller().setSchema(schema);
    }

    @Override
    public Schema getSchema() {
        return this.getUnmarshaller().getSchema();
    }

    private Object associativeUnmarshal(XmlNode xmlNode, boolean inplace, Class expectedType) throws JAXBException {
        if (xmlNode == null) {
            throw new IllegalArgumentException();
        }
        JaxBeanInfo bi = null;
        if (expectedType != null) {
            bi = this.context.getBeanInfo(expectedType, true);
        }
        InterningXmlVisitor handler = new InterningXmlVisitor(this.getUnmarshaller().createUnmarshallerHandler(this.scanner, inplace, bi));
        this.scanner.setContentHandler(new SAXConnector(handler, this.scanner.getLocator()));
        try {
            this.scanner.scan(xmlNode);
        }
        catch (SAXException e) {
            throw this.unmarshaller.createUnmarshalException(e);
        }
        return handler.getContext().getResult();
    }

    @Override
    public XmlNode getXMLNode(Object jaxbObject) {
        if (jaxbObject == null) {
            throw new IllegalArgumentException();
        }
        AssociationMap.Entry<XmlNode> e = this.assoc.byPeer(jaxbObject);
        if (e == null) {
            return null;
        }
        return e.element();
    }

    @Override
    public Object getJAXBNode(XmlNode xmlNode) {
        if (xmlNode == null) {
            throw new IllegalArgumentException();
        }
        AssociationMap.Entry<XmlNode> e = this.assoc.byElement(xmlNode);
        if (e == null) {
            return null;
        }
        if (e.outer() != null) {
            return e.outer();
        }
        return e.inner();
    }

    @Override
    public XmlNode updateXML(Object jaxbObject) throws JAXBException {
        return this.updateXML(jaxbObject, this.getXMLNode(jaxbObject));
    }

    @Override
    public XmlNode updateXML(Object jaxbObject, XmlNode xmlNode) throws JAXBException {
        if (jaxbObject == null || xmlNode == null) {
            throw new IllegalArgumentException();
        }
        Element e = (Element)xmlNode;
        Node ns = e.getNextSibling();
        Node p = e.getParentNode();
        p.removeChild(e);
        JaxBeanInfo bi = this.context.getBeanInfo(jaxbObject, true);
        if (!bi.isElement()) {
            jaxbObject = new JAXBElement<Object>(new QName(e.getNamespaceURI(), e.getLocalName()), bi.jaxbType, jaxbObject);
        }
        this.getMarshaller().marshal((Object)jaxbObject, p);
        Node newNode = p.getLastChild();
        p.removeChild(newNode);
        p.insertBefore(newNode, ns);
        return (XmlNode)newNode;
    }

    @Override
    public void setEventHandler(ValidationEventHandler handler) throws JAXBException {
        this.getUnmarshaller().setEventHandler(handler);
        this.getMarshaller().setEventHandler(handler);
    }

    @Override
    public ValidationEventHandler getEventHandler() {
        return this.getUnmarshaller().getEventHandler();
    }

    @Override
    public Object getProperty(String name) throws PropertyException {
        if (name == null) {
            throw new IllegalArgumentException(Messages.NULL_PROPERTY_NAME.format(new Object[0]));
        }
        if (this.excludeProperty(name)) {
            throw new PropertyException(name);
        }
        Object prop = null;
        PropertyException pe = null;
        try {
            prop = this.getMarshaller().getProperty(name);
            return prop;
        }
        catch (PropertyException p) {
            pe = p;
            try {
                prop = this.getUnmarshaller().getProperty(name);
                return prop;
            }
            catch (PropertyException p2) {
                pe = p2;
                pe.setStackTrace(Thread.currentThread().getStackTrace());
                throw pe;
            }
        }
    }

    @Override
    public void setProperty(String name, Object value) throws PropertyException {
        if (name == null) {
            throw new IllegalArgumentException(Messages.NULL_PROPERTY_NAME.format(new Object[0]));
        }
        if (this.excludeProperty(name)) {
            throw new PropertyException(name, value);
        }
        PropertyException pe = null;
        try {
            this.getMarshaller().setProperty(name, value);
            return;
        }
        catch (PropertyException p) {
            pe = p;
            try {
                this.getUnmarshaller().setProperty(name, value);
                return;
            }
            catch (PropertyException p2) {
                pe = p2;
                pe.setStackTrace(Thread.currentThread().getStackTrace());
                throw pe;
            }
        }
    }

    private boolean excludeProperty(String name) {
        return name.equals("com.sun.xml.bind.characterEscapeHandler") || name.equals("com.sun.xml.bind.xmlDeclaration") || name.equals("com.sun.xml.bind.xmlHeaders");
    }
}

