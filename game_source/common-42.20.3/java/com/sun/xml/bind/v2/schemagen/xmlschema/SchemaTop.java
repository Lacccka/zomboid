/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.txw2.TypedXmlWriter
 *  com.sun.xml.txw2.annotation.XmlElement
 */
package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.bind.v2.schemagen.xmlschema.Redefinable;
import com.sun.xml.bind.v2.schemagen.xmlschema.TopLevelAttribute;
import com.sun.xml.bind.v2.schemagen.xmlschema.TopLevelElement;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlElement;

public interface SchemaTop
extends Redefinable,
TypedXmlWriter {
    @XmlElement
    public TopLevelAttribute attribute();

    @XmlElement
    public TopLevelElement element();
}

