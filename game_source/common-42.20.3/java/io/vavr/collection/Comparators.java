/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.collection.NaturalComparator;
import java.util.Comparator;

final class Comparators {
    private Comparators() {
    }

    static <U> Comparator<U> naturalComparator() {
        return NaturalComparator.instance();
    }
}

