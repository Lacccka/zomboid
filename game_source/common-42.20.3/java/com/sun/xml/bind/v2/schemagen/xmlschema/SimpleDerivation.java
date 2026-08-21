/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.txw2.TypedXmlWriter
 *  com.sun.xml.txw2.annotation.XmlElement
 */
package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.bind.v2.schemagen.xmlschema.List;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleRestriction;
import com.sun.xml.bind.v2.schemagen.xmlschema.Union;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlElement;

public interface SimpleDerivation
extends TypedXmlWriter {
    @XmlElement
    public SimpleRestriction restriction();

    @XmlElement
    public Union union();

    @XmlElement
    public List list();
}

