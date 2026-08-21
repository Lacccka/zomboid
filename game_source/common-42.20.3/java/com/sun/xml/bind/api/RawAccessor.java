/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.api;

import com.sun.xml.bind.api.AccessorException;

public abstract class RawAccessor<B, V> {
    public abstract V get(B var1) throws AccessorException;

    public abstract void set(B var1, V var2) throws AccessorException;
}

