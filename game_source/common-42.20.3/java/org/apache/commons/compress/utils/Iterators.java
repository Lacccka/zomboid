/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public class Iterators {
    public static <T> boolean addAll(Collection<T> collection, Iterator<? extends T> iterator2) {
        Objects.requireNonNull(collection);
        Objects.requireNonNull(iterator2);
        boolean wasModified = false;
        while (iterator2.hasNext()) {
            wasModified |= collection.add(iterator2.next());
        }
        return wasModified;
    }

    private Iterators() {
    }
}

