/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.txw2.TypedXmlWriter
 *  com.sun.xml.txw2.annotation.XmlAttribute
 *  com.sun.xml.txw2.annotation.XmlElement
 */
package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.bind.v2.schemagen.xmlschema.AttrDecls;
import com.sun.xml.bind.v2.schemagen.xmlschema.ComplexContent;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleContent;
import com.sun.xml.bind.v2.schemagen.xmlschema.TypeDefParticle;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

public interface ComplexTypeModel
extends AttrDecls,
TypeDefParticle,
TypedXmlWriter {
    @XmlElement
    public SimpleContent simpleContent();

    @XmlElement
    public ComplexContent complexContent();

    @XmlAttribute
    public ComplexTypeModel mixed(boolean var1);
}

