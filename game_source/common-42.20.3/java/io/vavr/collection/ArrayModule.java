/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.collection.Array;

interface ArrayModule {

    public static final class Combinations {
        static <T> Array<Array<T>> apply(Array<T> elements, int k) {
            if (k == 0) {
                return Array.of(Array.empty());
            }
            return ((Array)elements.zipWithIndex()).flatMap(t -> Combinations.apply(elements.drop((Integer)t._2 + 1), k - 1).map(c -> c.prepend(t._1)));
        }
    }
}

