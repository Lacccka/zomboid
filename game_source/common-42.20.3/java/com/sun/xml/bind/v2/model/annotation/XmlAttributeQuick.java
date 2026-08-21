/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.Quick;
import java.lang.annotation.Annotation;
import javax.xml.bind.annotation.XmlAttribute;

final class XmlAttributeQuick
extends Quick
implements XmlAttribute {
    private final XmlAttribute core;

    public XmlAttributeQuick(Locatable upstream, XmlAttribute core) {
        super(upstream);
        this.core = core;
    }

    @Override
    protected Annotation getAnnotation() {
        return this.core;
    }

    @Override
    protected Quick newInstance(Locatable upstream, Annotation core) {
        return new XmlAttributeQuick(upstream, (XmlAttribute)core);
    }

    public Class<XmlAttribute> annotationType() {
        return XmlAttribute.class;
    }

    @Override
    public String name() {
        return this.core.name();
    }

    @Override
    public String namespace() {
        return this.core.namespace();
    }

    @Override
    public boolean required() {
        return this.core.required();
    }
}

