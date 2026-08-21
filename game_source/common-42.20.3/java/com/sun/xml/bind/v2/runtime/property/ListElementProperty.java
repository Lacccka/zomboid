/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.Transducer;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.property.ArrayProperty;
import com.sun.xml.bind.v2.runtime.property.UnmarshallerChain;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.DefaultValueLoaderDecorator;
import com.sun.xml.bind.v2.runtime.unmarshaller.LeafPropertyLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.util.QNameMap;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

final class ListElementProperty<BeanT, ListT, ItemT>
extends ArrayProperty<BeanT, ListT, ItemT> {
    private final Name tagName;
    private final String defaultValue;
    private final TransducedAccessor<BeanT> xacc;

    public ListElementProperty(JAXBContextImpl grammar, RuntimeElementPropertyInfo prop) {
        super(grammar, prop);
        assert (prop.isValueList());
        assert (prop.getTypes().size() == 1);
        RuntimeTypeRef ref = prop.getTypes().get(0);
        this.tagName = grammar.nameBuilder.createElementName(ref.getTagName());
        this.defaultValue = ref.getDefaultValue();
        Transducer xducer = ref.getTransducer();
        this.xacc = new ListTransducedAccessorImpl(xducer, this.acc, this.lister);
    }

    @Override
    public PropertyKind getKind() {
        return PropertyKind.ELEMENT;
    }

    @Override
    public void buildChildElementUnmarshallers(UnmarshallerChain chain, QNameMap<ChildLoader> handlers) {
        Loader l = new LeafPropertyLoader(this.xacc);
        l = new DefaultValueLoaderDecorator(l, this.defaultValue);
        handlers.put(this.tagName, new ChildLoader(l, null));
    }

    @Override
    public void serializeBody(BeanT o, XMLSerializer w, Object outerPeer) throws SAXException, AccessorException, IOException, XMLStreamException {
        Object list = this.acc.get(o);
        if (list != null) {
            if (this.xacc.useNamespace()) {
                w.startElement(this.tagName, null);
                this.xacc.declareNamespace(o, w);
                w.endNamespaceDecls(list);
                w.endAttributes();
                this.xacc.writeText(w, o, this.fieldName);
                w.endElement();
            } else {
                this.xacc.writeLeafElement(w, this.tagName, o, this.fieldName);
            }
        }
    }

    @Override
    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        if (this.tagName != null && this.tagName.equals(nsUri, localName)) {
            return this.acc;
        }
        return null;
    }
}

