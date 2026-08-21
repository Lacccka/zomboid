/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.txw2.TypedXmlWriter
 *  com.sun.xml.txw2.annotation.XmlAttribute
 *  com.sun.xml.txw2.annotation.XmlElement
 */
package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.bind.v2.schemagen.xmlschema.NoFixedFacet;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleTypeHost;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;
import javax.xml.namespace.QName;

public interface SimpleRestrictionModel
extends SimpleTypeHost,
TypedXmlWriter {
    @XmlAttribute
    public SimpleRestrictionModel base(QName var1);

    @XmlElement
    public NoFixedFacet enumeration();
}

