/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import java.io.Serializable;
import java.util.Comparator;

final class NaturalComparator<T>
implements Comparator<T>,
Serializable {
    private static final long serialVersionUID = 1L;
    private static final NaturalComparator<?> INSTANCE = new NaturalComparator();

    private NaturalComparator() {
    }

    static <T> NaturalComparator<T> instance() {
        return INSTANCE;
    }

    @Override
    public int compare(T o1, T o2) {
        return ((Comparable)o1).compareTo(o2);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NaturalComparator;
    }

    public int hashCode() {
        return 1;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}

