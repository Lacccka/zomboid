/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.core.EnumLeafInfo;

public interface EnumConstant<T, C> {
    public EnumLeafInfo<T, C> getEnclosingClass();

    public String getLexicalValue();

    public String getName();
}

