/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.txw2.TypedXmlWriter
 *  com.sun.xml.txw2.annotation.XmlAttribute
 *  com.sun.xml.txw2.annotation.XmlElement
 */
package com.sun.xml.bind.v2.schemagen.episode;

import com.sun.xml.bind.v2.schemagen.episode.Klass;
import com.sun.xml.bind.v2.schemagen.episode.SchemaBindings;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement(value="bindings")
public interface Bindings
extends TypedXmlWriter {
    @XmlElement
    public Bindings bindings();

    @XmlElement(value="class")
    public Klass klass();

    public Klass typesafeEnumClass();

    @XmlElement
    public SchemaBindings schemaBindings();

    @XmlAttribute
    public void scd(String var1);

    @XmlAttribute
    public void version(String var1);
}

