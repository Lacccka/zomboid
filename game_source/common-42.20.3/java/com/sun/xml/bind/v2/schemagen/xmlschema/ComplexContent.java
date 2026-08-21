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
import com.sun.xml.bind.v2.schemagen.xmlschema.ComplexExtension;
import com.sun.xml.bind.v2.schemagen.xmlschema.ComplexRestriction;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement(value="complexContent")
public interface ComplexContent
extends Annotated,
TypedXmlWriter {
    @XmlElement
    public ComplexExtension extension();

    @XmlElement
    public ComplexRestriction restriction();

    @XmlAttribute
    public ComplexContent mixed(boolean var1);
}

