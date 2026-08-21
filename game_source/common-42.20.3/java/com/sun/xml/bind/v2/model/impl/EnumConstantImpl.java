/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.core.EnumConstant;
import com.sun.xml.bind.v2.model.core.EnumLeafInfo;
import com.sun.xml.bind.v2.model.impl.EnumLeafInfoImpl;

class EnumConstantImpl<T, C, F, M>
implements EnumConstant<T, C> {
    protected final String lexical;
    protected final EnumLeafInfoImpl<T, C, F, M> owner;
    protected final String name;
    protected final EnumConstantImpl<T, C, F, M> next;

    public EnumConstantImpl(EnumLeafInfoImpl<T, C, F, M> owner, String name, String lexical, EnumConstantImpl<T, C, F, M> next) {
        this.lexical = lexical;
        this.owner = owner;
        this.name = name;
        this.next = next;
    }

    @Override
    public EnumLeafInfo<T, C> getEnclosingClass() {
        return this.owner;
    }

    @Override
    public final String getLexicalValue() {
        return this.lexical;
    }

    @Override
    public final String getName() {
        return this.name;
    }
}

