/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.txw2.TypedXmlWriter
 *  com.sun.xml.txw2.annotation.XmlAttribute
 *  com.sun.xml.txw2.annotation.XmlElement
 */
package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.bind.v2.schemagen.xmlschema.Annotation;
import com.sun.xml.bind.v2.schemagen.xmlschema.Import;
import com.sun.xml.bind.v2.schemagen.xmlschema.SchemaTop;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement(value="schema")
public interface Schema
extends SchemaTop,
TypedXmlWriter {
    @XmlElement
    public Annotation annotation();

    @XmlElement(value="import")
    public Import _import();

    @XmlAttribute
    public Schema targetNamespace(String var1);

    @XmlAttribute(ns="http://www.w3.org/XML/1998/namespace")
    public Schema lang(String var1);

    @XmlAttribute
    public Schema id(String var1);

    @XmlAttribute
    public Schema elementFormDefault(String var1);

    @XmlAttribute
    public Schema attributeFormDefault(String var1);

    @XmlAttribute
    public Schema blockDefault(String[] var1);

    @XmlAttribute
    public Schema blockDefault(String var1);

    @XmlAttribute
    public Schema finalDefault(String[] var1);

    @XmlAttribute
    public Schema finalDefault(String var1);

    @XmlAttribute
    public Schema version(String var1);
}

