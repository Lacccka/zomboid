/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.api.CompositeStructure;
import com.sun.xml.bind.v2.runtime.InternalBridge;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Messages;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public class CompositeStructureBeanInfo
extends JaxBeanInfo<CompositeStructure> {
    public CompositeStructureBeanInfo(JAXBContextImpl context) {
        super(context, null, CompositeStructure.class, false, true, false);
    }

    @Override
    public String getElementNamespaceURI(CompositeStructure o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getElementLocalName(CompositeStructure o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompositeStructure createInstance(UnmarshallingContext context) throws IllegalAccessException, InvocationTargetException, InstantiationException, SAXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean reset(CompositeStructure o, UnmarshallingContext context) throws SAXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getId(CompositeStructure o, XMLSerializer target) throws SAXException {
        return null;
    }

    @Override
    public Loader getLoader(JAXBContextImpl context, boolean typeSubstitutionCapable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void serializeRoot(CompositeStructure o, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        target.reportError(new ValidationEventImpl(1, Messages.UNABLE_TO_MARSHAL_NON_ELEMENT.format(o.getClass().getName()), null, null));
    }

    @Override
    public void serializeURIs(CompositeStructure o, XMLSerializer target) throws SAXException {
    }

    @Override
    public void serializeAttributes(CompositeStructure o, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
    }

    @Override
    public void serializeBody(CompositeStructure o, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        int len = o.bridges.length;
        for (int i = 0; i < len; ++i) {
            Object value = o.values[i];
            InternalBridge bi = (InternalBridge)o.bridges[i];
            bi.marshal(value, target);
        }
    }

    @Override
    public Transducer<CompositeStructure> getTransducer() {
        return null;
    }
}

