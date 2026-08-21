/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.NonElement;
import javax.xml.namespace.QName;

public interface MaybeElement<T, C>
extends NonElement<T, C> {
    public boolean isElement();

    public QName getElementName();

    public Element<T, C> asElement();
}

