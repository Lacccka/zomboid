/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.property.Property;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

abstract class PropertyImpl<BeanT>
implements Property<BeanT> {
    protected final String fieldName;
    private RuntimePropertyInfo propertyInfo = null;
    private boolean hiddenByOverride = false;

    public PropertyImpl(JAXBContextImpl context, RuntimePropertyInfo prop) {
        this.fieldName = prop.getName();
        if (context.retainPropertyInfo) {
            this.propertyInfo = prop;
        }
    }

    @Override
    public RuntimePropertyInfo getInfo() {
        return this.propertyInfo;
    }

    @Override
    public void serializeBody(BeanT o, XMLSerializer w, Object outerPeer) throws SAXException, AccessorException, IOException, XMLStreamException {
    }

    @Override
    public void serializeURIs(BeanT o, XMLSerializer w) throws SAXException, AccessorException {
    }

    @Override
    public boolean hasSerializeURIAction() {
        return false;
    }

    @Override
    public Accessor getElementPropertyAccessor(String nsUri, String localName) {
        return null;
    }

    @Override
    public void wrapUp() {
    }

    @Override
    public boolean isHiddenByOverride() {
        return this.hiddenByOverride;
    }

    @Override
    public void setHiddenByOverride(boolean hidden) {
        this.hiddenByOverride = hidden;
    }

    @Override
    public String getFieldName() {
        return this.fieldName;
    }
}

