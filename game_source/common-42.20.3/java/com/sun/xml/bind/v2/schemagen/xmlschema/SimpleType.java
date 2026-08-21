/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.txw2.TypedXmlWriter
 *  com.sun.xml.txw2.annotation.XmlAttribute
 *  com.sun.xml.txw2.annotation.XmlElement
 */
package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.bind.v2.schemagen.xmlschema.Annotated;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleDerivation;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement(value="simpleType")
public interface SimpleType
extends Annotated,
SimpleDerivation,
TypedXmlWriter {
    @XmlAttribute(value="final")
    public SimpleType _final(String var1);

    @XmlAttribute(value="final")
    public SimpleType _final(String[] var1);

    @XmlAttribute
    public SimpleType name(String var1);
}

