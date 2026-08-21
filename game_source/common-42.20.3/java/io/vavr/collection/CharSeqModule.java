/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.collection.CharSeq;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Vector;

interface CharSeqModule {

    public static interface Combinations {
        public static IndexedSeq<CharSeq> apply(CharSeq elements, int k) {
            if (k == 0) {
                return Vector.of(CharSeq.empty());
            }
            return elements.zipWithIndex().flatMap(t -> Combinations.apply(elements.drop((Integer)t._2 + 1), k - 1).map(c -> c.prepend((Character)t._1)));
        }
    }
}

