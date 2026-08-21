/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.txw2.TypedXmlWriter
 *  com.sun.xml.txw2.annotation.XmlAttribute
 */
package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleTypeHost;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import javax.xml.namespace.QName;

public interface AttributeType
extends SimpleTypeHost,
TypedXmlWriter {
    @XmlAttribute
    public AttributeType type(QName var1);
}

