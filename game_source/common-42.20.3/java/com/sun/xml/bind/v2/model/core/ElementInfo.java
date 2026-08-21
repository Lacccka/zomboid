/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import java.util.Collection;

public interface ElementInfo<T, C>
extends Element<T, C> {
    public ElementPropertyInfo<T, C> getProperty();

    public NonElement<T, C> getContentType();

    public T getContentInMemoryType();

    @Override
    public T getType();

    @Override
    public ElementInfo<T, C> getSubstitutionHead();

    public Collection<? extends ElementInfo<T, C>> getSubstitutionMembers();
}

