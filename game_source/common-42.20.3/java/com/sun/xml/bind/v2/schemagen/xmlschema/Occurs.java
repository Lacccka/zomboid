/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.txw2.TypedXmlWriter
 *  com.sun.xml.txw2.annotation.XmlAttribute
 */
package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;

public interface Occurs
extends TypedXmlWriter {
    @XmlAttribute
    public Occurs minOccurs(int var1);

    @XmlAttribute
    public Occurs maxOccurs(String var1);

    @XmlAttribute
    public Occurs maxOccurs(int var1);
}

