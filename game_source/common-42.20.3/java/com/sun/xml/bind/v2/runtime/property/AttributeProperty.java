/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimeAttributePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.property.PropertyImpl;
import com.sun.xml.bind.v2.runtime.property.UnmarshallerChain;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.util.QNameMap;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public final class AttributeProperty<BeanT>
extends PropertyImpl<BeanT>
implements Comparable<AttributeProperty> {
    public final Name attName;
    public final TransducedAccessor<BeanT> xacc;
    private final Accessor acc;

    public AttributeProperty(JAXBContextImpl context, RuntimeAttributePropertyInfo prop) {
        super(context, prop);
        this.attName = context.nameBuilder.createAttributeName(prop.getXmlName());
        this.xacc = TransducedAccessor.get(context, prop);
        this.acc = prop.getAccessor();
    }

    public void serializeAttributes(BeanT o, XMLSerializer w) throws SAXException, AccessorException, IOException, XMLStreamException {
        CharSequence value = this.xacc.print(o);
        if (value != null) {
            w.attribute(this.attName, value.toString());
        }
    }

    @Override
    public void serializeURIs(BeanT o, XMLSerializer w) throws AccessorException, SAXException {
        this.xacc.declareNamespace(o, w);
    }

    @Override
    public boolean hasSerializeURIAction() {
        return this.xacc.useNamespace();
    }

    @Override
    public void buildChildElementUnmarshallers(UnmarshallerChain chainElem, QNameMap<ChildLoader> handlers) {
        throw new IllegalStateException();
    }

    @Override
    public PropertyKind getKind() {
        return PropertyKind.ATTRIBUTE;
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
    public int compareTo(AttributeProperty that) {
        return this.attName.compareTo(that.attName);
    }
}

