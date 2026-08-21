/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.Quick;
import java.lang.annotation.Annotation;
import javax.xml.bind.annotation.XmlSchemaType;

final class XmlSchemaTypeQuick
extends Quick
implements XmlSchemaType {
    private final XmlSchemaType core;

    public XmlSchemaTypeQuick(Locatable upstream, XmlSchemaType core) {
        super(upstream);
        this.core = core;
    }

    @Override
    protected Annotation getAnnotation() {
        return this.core;
    }

    @Override
    protected Quick newInstance(Locatable upstream, Annotation core) {
        return new XmlSchemaTypeQuick(upstream, (XmlSchemaType)core);
    }

    public Class<XmlSchemaType> annotationType() {
        return XmlSchemaType.class;
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
}

