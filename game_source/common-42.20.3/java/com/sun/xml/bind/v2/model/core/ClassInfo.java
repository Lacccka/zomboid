/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.core.MaybeElement;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import java.util.List;

public interface ClassInfo<T, C>
extends MaybeElement<T, C> {
    public ClassInfo<T, C> getBaseClass();

    public C getClazz();

    public String getName();

    public List<? extends PropertyInfo<T, C>> getProperties();

    public boolean hasValueProperty();

    public PropertyInfo<T, C> getProperty(String var1);

    public boolean hasProperties();

    public boolean isAbstract();

    public boolean isOrdered();

    public boolean isFinal();

    public boolean hasSubClasses();

    public boolean hasAttributeWildcard();

    public boolean inheritsAttributeWildcard();

    public boolean declaresAttributeWildcard();
}

