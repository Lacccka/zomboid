/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.Quick;
import java.lang.annotation.Annotation;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;

final class XmlElementRefsQuick
extends Quick
implements XmlElementRefs {
    private final XmlElementRefs core;

    public XmlElementRefsQuick(Locatable upstream, XmlElementRefs core) {
        super(upstream);
        this.core = core;
    }

    @Override
    protected Annotation getAnnotation() {
        return this.core;
    }

    @Override
    protected Quick newInstance(Locatable upstream, Annotation core) {
        return new XmlElementRefsQuick(upstream, (XmlElementRefs)core);
    }

    public Class<XmlElementRefs> annotationType() {
        return XmlElementRefs.class;
    }

    @Override
    public XmlElementRef[] value() {
        return this.core.value();
    }
}

