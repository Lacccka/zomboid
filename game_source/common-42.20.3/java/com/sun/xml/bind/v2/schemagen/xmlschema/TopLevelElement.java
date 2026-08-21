/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.txw2.TypedXmlWriter
 *  com.sun.xml.txw2.annotation.XmlAttribute
 *  com.sun.xml.txw2.annotation.XmlElement
 */
package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.bind.v2.schemagen.xmlschema.Element;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;
import javax.xml.namespace.QName;

@XmlElement(value="element")
public interface TopLevelElement
extends Element,
TypedXmlWriter {
    @XmlAttribute(value="final")
    public TopLevelElement _final(String[] var1);

    @XmlAttribute(value="final")
    public TopLevelElement _final(String var1);

    @XmlAttribute(value="abstract")
    public TopLevelElement _abstract(boolean var1);

    @XmlAttribute
    public TopLevelElement substitutionGroup(QName var1);

    @XmlAttribute
    public TopLevelElement name(String var1);
}

