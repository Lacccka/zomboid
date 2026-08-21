/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.txw2.TypedXmlWriter
 *  com.sun.xml.txw2.annotation.XmlAttribute
 */
package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.bind.v2.schemagen.xmlschema.Annotated;
import com.sun.xml.bind.v2.schemagen.xmlschema.ComplexTypeHost;
import com.sun.xml.bind.v2.schemagen.xmlschema.FixedOrDefault;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleTypeHost;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import javax.xml.namespace.QName;

public interface Element
extends Annotated,
ComplexTypeHost,
FixedOrDefault,
SimpleTypeHost,
TypedXmlWriter {
    @XmlAttribute
    public Element type(QName var1);

    @XmlAttribute
    public Element block(String[] var1);

    @XmlAttribute
    public Element block(String var1);

    @XmlAttribute
    public Element nillable(boolean var1);
}

