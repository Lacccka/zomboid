/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import java.util.Comparator;

public interface Ordered<T> {
    public Comparator<T> comparator();
}

