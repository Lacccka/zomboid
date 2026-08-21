/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.annotation;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.Quick;
import java.lang.annotation.Annotation;
import javax.xml.bind.annotation.XmlRootElement;

final class XmlRootElementQuick
extends Quick
implements XmlRootElement {
    private final XmlRootElement core;

    public XmlRootElementQuick(Locatable upstream, XmlRootElement core) {
        super(upstream);
        this.core = core;
    }

    @Override
    protected Annotation getAnnotation() {
        return this.core;
    }

    @Override
    protected Quick newInstance(Locatable upstream, Annotation core) {
        return new XmlRootElementQuick(upstream, (XmlRootElement)core);
    }

    public Class<XmlRootElement> annotationType() {
        return XmlRootElement.class;
    }

    @Override
    public String name() {
        return this.core.name();
    }

    @Override
    public String namespace() {
        return this.core.namespace();
    }
}

