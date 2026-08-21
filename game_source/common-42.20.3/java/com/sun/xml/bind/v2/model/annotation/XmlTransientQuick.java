/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.Quick;
import java.lang.annotation.Annotation;
import javax.xml.bind.annotation.XmlTransient;

final class XmlTransientQuick
extends Quick
implements XmlTransient {
    private final XmlTransient core;

    public XmlTransientQuick(Locatable upstream, XmlTransient core) {
        super(upstream);
        this.core = core;
    }

    @Override
    protected Annotation getAnnotation() {
        return this.core;
    }

    @Override
    protected Quick newInstance(Locatable upstream, Annotation core) {
        return new XmlTransientQuick(upstream, (XmlTransient)core);
    }

    public Class<XmlTransient> annotationType() {
        return XmlTransient.class;
    }
}

