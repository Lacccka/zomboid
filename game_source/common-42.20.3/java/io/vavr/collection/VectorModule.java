/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.collection.Vector;

interface VectorModule {

    public static final class Combinations {
        static <T> Vector<Vector<T>> apply(Vector<T> elements, int k) {
            return k == 0 ? Vector.of(Vector.empty()) : ((Vector)elements.zipWithIndex()).flatMap(t -> Combinations.apply(elements.drop((Integer)t._2 + 1), k - 1).map(c -> c.prepend(t._1)));
        }
    }
}

