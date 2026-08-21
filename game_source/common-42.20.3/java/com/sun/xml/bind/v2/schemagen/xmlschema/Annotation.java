/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.txw2.TypedXmlWriter
 *  com.sun.xml.txw2.annotation.XmlAttribute
 *  com.sun.xml.txw2.annotation.XmlElement
 */
package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.bind.v2.schemagen.xmlschema.Appinfo;
import com.sun.xml.bind.v2.schemagen.xmlschema.Documentation;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement(value="annotation")
public interface Annotation
extends TypedXmlWriter {
    @XmlElement
    public Appinfo appinfo();

    @XmlElement
    public Documentation documentation();

    @XmlAttribute
    public Annotation id(String var1);
}

