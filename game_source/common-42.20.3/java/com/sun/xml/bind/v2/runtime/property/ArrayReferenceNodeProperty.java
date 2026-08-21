/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.WildcardMode;
import com.sun.xml.bind.v2.model.runtime.RuntimeElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeReferencePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.property.ArrayERProperty;
import com.sun.xml.bind.v2.runtime.property.UnmarshallerChain;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Receiver;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.WildcardLoader;
import com.sun.xml.bind.v2.util.QNameMap;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

class ArrayReferenceNodeProperty<BeanT, ListT, ItemT>
extends ArrayERProperty<BeanT, ListT, ItemT> {
    private final QNameMap<JaxBeanInfo> expectedElements = new QNameMap();
    private final boolean isMixed;
    private final DomHandler domHandler;
    private final WildcardMode wcMode;

    public ArrayReferenceNodeProperty(JAXBContextImpl p, RuntimeReferencePropertyInfo prop) {
        super(p, prop, prop.getXmlName(), prop.isCollectionNillable());
        for (RuntimeElement runtimeElement : prop.getElements()) {
            JaxBeanInfo bi = p.getOrCreate(runtimeElement);
            this.expectedElements.put(runtimeElement.getElementName().getNamespaceURI(), runtimeElement.getElementName().getLocalPart(), bi);
        }
        this.isMixed = prop.isMixed();
        if (prop.getWildcard() != null) {
            this.domHandler = (DomHandler)ClassFactory.create((Class)prop.getDOMHandler());
            this.wcMode = prop.getWildcard();
        } else {
            this.domHandler = null;
            this.wcMode = null;
        }
    }

    @Override
    protected final void serializeListBody(BeanT o, XMLSerializer w, ListT list) throws IOException, XMLStreamException, SAXException {
        ListIterator itr = this.lister.iterator(list, w);
        while (itr.hasNext()) {
            try {
                Object item = itr.next();
                if (item == null) continue;
                if (this.isMixed && item.getClass() == String.class) {
                    w.text((String)item, null);
                    continue;
                }
                JaxBeanInfo bi = w.grammar.getBeanInfo(item, true);
                if (bi.jaxbType == Object.class && this.domHandler != null) {
                    w.writeDom(item, this.domHandler, o, this.fieldName);
                    continue;
                }
                bi.serializeRoot(item, w);
            }
            catch (JAXBException e) {
                w.reportError(this.fieldName, e);
            }
        }
    }

    @Override
    public void createBodyUnmarshaller(UnmarshallerChain chain, QNameMap<ChildLoader> loaders) {
        int offset = chain.allocateOffset();
        ArrayERProperty.ReceiverImpl recv = new ArrayERProperty.ReceiverImpl(offset);
        for (QNameMap.Entry<JaxBeanInfo> n : this.expectedElements.entrySet()) {
            JaxBeanInfo beanInfo = n.getValue();
            loaders.put(n.nsUri, n.localName, new ChildLoader(beanInfo.getLoader(chain.context, true), recv));
        }
        if (this.isMixed) {
            loaders.put(TEXT_HANDLER, new ChildLoader(new MixedTextLoader(recv), null));
        }
        if (this.domHandler != null) {
            loaders.put(CATCH_ALL, new ChildLoader(new WildcardLoader(this.domHandler, this.wcMode), recv));
        }
    }

    @Override
    public PropertyKind getKind() {
        return PropertyKind.REFERENCE;
    }

    @Override
    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        if (this.wrapperTagName != null ? this.wrapperTagName.equals(nsUri, localName) : this.expectedElements.containsKey(nsUri, localName)) {
            return this.acc;
        }
        return null;
    }

    private static final class MixedTextLoader
    extends Loader {
        private final Receiver recv;

        public MixedTextLoader(Receiver recv) {
            super(true);
            this.recv = recv;
        }

        @Override
        public void text(UnmarshallingContext.State state, CharSequence text) throws SAXException {
            if (text.length() != 0) {
                this.recv.receive(state, text.toString());
            }
        }
    }
}

