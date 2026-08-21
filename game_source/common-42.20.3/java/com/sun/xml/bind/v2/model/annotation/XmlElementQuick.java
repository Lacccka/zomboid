/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.Quick;
import java.lang.annotation.Annotation;
import javax.xml.bind.annotation.XmlElement;

final class XmlElementQuick
extends Quick
implements XmlElement {
    private final XmlElement core;

    public XmlElementQuick(Locatable upstream, XmlElement core) {
        super(upstream);
        this.core = core;
    }

    @Override
    protected Annotation getAnnotation() {
        return this.core;
    }

    @Override
    protected Quick newInstance(Locatable upstream, Annotation core) {
        return new XmlElementQuick(upstream, (XmlElement)core);
    }

    public Class<XmlElement> annotationType() {
        return XmlElement.class;
    }

    @Override
    public String name() {
        return this.core.name();
    }

    @Override
    public Class type() {
        return this.core.type();
    }

    @Override
    public String namespace() {
        return this.core.namespace();
    }

    @Override
    public String defaultValue() {
        return this.core.defaultValue();
    }

    @Override
    public boolean required() {
        return this.core.required();
    }

    @Override
    public boolean nillable() {
        return this.core.nillable();
    }
}

