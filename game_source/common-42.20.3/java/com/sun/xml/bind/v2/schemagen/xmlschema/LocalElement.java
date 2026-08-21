/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.txw2.TypedXmlWriter
 *  com.sun.xml.txw2.annotation.XmlAttribute
 *  com.sun.xml.txw2.annotation.XmlElement
 */
package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.bind.v2.schemagen.xmlschema.Element;
import com.sun.xml.bind.v2.schemagen.xmlschema.Occurs;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;
import javax.xml.namespace.QName;

@XmlElement(value="element")
public interface LocalElement
extends Element,
Occurs,
TypedXmlWriter {
    @XmlAttribute
    public LocalElement form(String var1);

    @XmlAttribute
    public LocalElement name(String var1);

    @XmlAttribute
    public LocalElement ref(QName var1);
}

