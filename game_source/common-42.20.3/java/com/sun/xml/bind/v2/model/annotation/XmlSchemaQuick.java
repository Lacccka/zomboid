/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.Quick;
import java.lang.annotation.Annotation;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;

final class XmlSchemaQuick
extends Quick
implements XmlSchema {
    private final XmlSchema core;

    public XmlSchemaQuick(Locatable upstream, XmlSchema core) {
        super(upstream);
        this.core = core;
    }

    @Override
    protected Annotation getAnnotation() {
        return this.core;
    }

    @Override
    protected Quick newInstance(Locatable upstream, Annotation core) {
        return new XmlSchemaQuick(upstream, (XmlSchema)core);
    }

    public Class<XmlSchema> annotationType() {
        return XmlSchema.class;
    }

    @Override
    public String location() {
        return this.core.location();
    }

    @Override
    public String namespace() {
        return this.core.namespace();
    }

    @Override
    public XmlNs[] xmlns() {
        return this.core.xmlns();
    }

    @Override
    public XmlNsForm elementFormDefault() {
        return this.core.elementFormDefault();
    }

    @Override
    public XmlNsForm attributeFormDefault() {
        return this.core.attributeFormDefault();
    }
}

