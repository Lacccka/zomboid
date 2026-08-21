/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.txw2.TypedXmlWriter
 *  com.sun.xml.txw2.annotation.XmlElement
 */
package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.bind.v2.schemagen.xmlschema.LocalAttribute;
import com.sun.xml.bind.v2.schemagen.xmlschema.Wildcard;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlElement;

public interface AttrDecls
extends TypedXmlWriter {
    @XmlElement
    public LocalAttribute attribute();

    @XmlElement
    public Wildcard anyAttribute();
}

