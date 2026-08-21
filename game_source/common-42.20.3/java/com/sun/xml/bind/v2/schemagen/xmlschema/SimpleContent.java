/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.txw2.TypedXmlWriter
 *  com.sun.xml.txw2.annotation.XmlElement
 */
package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.bind.v2.schemagen.xmlschema.Annotated;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleExtension;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleRestriction;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement(value="simpleContent")
public interface SimpleContent
extends Annotated,
TypedXmlWriter {
    @XmlElement
    public SimpleExtension extension();

    @XmlElement
    public SimpleRestriction restriction();
}

