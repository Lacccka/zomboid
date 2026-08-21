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
import com.sun.xml.bind.v2.schemagen.xmlschema.AttributeType;
import com.sun.xml.bind.v2.schemagen.xmlschema.FixedOrDefault;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;
import javax.xml.namespace.QName;

@XmlElement(value="attribute")
public interface LocalAttribute
extends Annotated,
AttributeType,
FixedOrDefault,
TypedXmlWriter {
    @XmlAttribute
    public LocalAttribute form(String var1);

    @XmlAttribute
    public LocalAttribute name(String var1);

    @XmlAttribute
    public LocalAttribute ref(QName var1);

    @XmlAttribute
    public LocalAttribute use(String var1);
}

