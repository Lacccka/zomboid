/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.property.StructureLoaderBuilder;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public interface Property<BeanT>
extends StructureLoaderBuilder {
    public void reset(BeanT var1) throws AccessorException;

    public void serializeBody(BeanT var1, XMLSerializer var2, Object var3) throws SAXException, AccessorException, IOException, XMLStreamException;

    public void serializeURIs(BeanT var1, XMLSerializer var2) throws SAXException, AccessorException;

    public boolean hasSerializeURIAction();

    public String getIdValue(BeanT var1) throws AccessorException, SAXException;

    public PropertyKind getKind();

    public Accessor getElementPropertyAccessor(String var1, String var2);

    public void wrapUp();

    public RuntimePropertyInfo getInfo();

    public boolean isHiddenByOverride();

    public void setHiddenByOverride(boolean var1);

    public String getFieldName();
}

