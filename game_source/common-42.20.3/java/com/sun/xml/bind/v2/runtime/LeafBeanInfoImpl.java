/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.runtime.RuntimeLeafInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.Messages;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.TextLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiTypeLoader;
import java.io.IOException;
import javax.xml.bind.helpers.ValidationEventImpl;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

final class LeafBeanInfoImpl<BeanT>
extends JaxBeanInfo<BeanT> {
    private final Loader loader;
    private final Loader loaderWithSubst;
    private final Transducer<BeanT> xducer;
    private final Name tagName;

    public LeafBeanInfoImpl(JAXBContextImpl grammar, RuntimeLeafInfo li) {
        super(grammar, (RuntimeTypeInfo)li, li.getClazz(), li.getTypeNames(), li.isElement(), true, false);
        this.xducer = li.getTransducer();
        this.loader = new TextLoader(this.xducer);
        this.loaderWithSubst = new XsiTypeLoader(this);
        this.tagName = this.isElement() ? grammar.nameBuilder.createElementName(li.getElementName()) : null;
    }

    @Override
    public QName getTypeName(BeanT instance) {
        QName tn = this.xducer.getTypeName(instance);
        if (tn != null) {
            return tn;
        }
        return super.getTypeName(instance);
    }

    @Override
    public final String getElementNamespaceURI(BeanT t) {
        return this.tagName.nsUri;
    }

    @Override
    public final String getElementLocalName(BeanT t) {
        return this.tagName.localName;
    }

    @Override
    public BeanT createInstance(UnmarshallingContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean reset(BeanT bean, UnmarshallingContext context) {
        return false;
    }

    @Override
    public final String getId(BeanT bean, XMLSerializer target) {
        return null;
    }

    @Override
    public final void serializeBody(BeanT bean, XMLSerializer w) throws SAXException, IOException, XMLStreamException {
        try {
            this.xducer.writeText(w, bean, null);
        }
        catch (AccessorException e) {
            w.reportError(null, e);
        }
    }

    @Override
    public final void serializeAttributes(BeanT bean, XMLSerializer target) {
    }

    @Override
    public final void serializeRoot(BeanT bean, XMLSerializer target) throws SAXException, IOException, XMLStreamException {
        if (this.tagName == null) {
            target.reportError(new ValidationEventImpl(1, Messages.UNABLE_TO_MARSHAL_NON_ELEMENT.format(bean.getClass().getName()), null, null));
        } else {
            target.startElement(this.tagName, bean);
            target.childAsSoleContent(bean, null);
            target.endElement();
        }
    }

    @Override
    public final void serializeURIs(BeanT bean, XMLSerializer target) throws SAXException {
        if (this.xducer.useNamespace()) {
            try {
                this.xducer.declareNamespace(bean, target);
            }
            catch (AccessorException e) {
                target.reportError(null, e);
            }
        }
    }

    @Override
    public final Loader getLoader(JAXBContextImpl context, boolean typeSubstitutionCapable) {
        if (typeSubstitutionCapable) {
            return this.loaderWithSubst;
        }
        return this.loader;
    }

    @Override
    public Transducer<BeanT> getTransducer() {
        return this.xducer;
    }
}

