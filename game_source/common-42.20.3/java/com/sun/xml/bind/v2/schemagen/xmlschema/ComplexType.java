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
import com.sun.xml.bind.v2.schemagen.xmlschema.ComplexTypeModel;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement(value="complexType")
public interface ComplexType
extends Annotated,
ComplexTypeModel,
TypedXmlWriter {
    @XmlAttribute(value="final")
    public ComplexType _final(String[] var1);

    @XmlAttribute(value="final")
    public ComplexType _final(String var1);

    @XmlAttribute
    public ComplexType block(String[] var1);

    @XmlAttribute
    public ComplexType block(String var1);

    @XmlAttribute(value="abstract")
    public ComplexType _abstract(boolean var1);

    @XmlAttribute
    public ComplexType name(String var1);
}

