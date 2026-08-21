/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.LinearSeq;
import io.vavr.collection.List;
import java.util.Objects;
import java.util.function.Predicate;

interface ListModule {

    public static interface SplitAt {
        public static <T> Tuple2<List<T>, List<T>> splitByPredicateReversed(List<T> source2, Predicate<? super T> predicate) {
            Objects.requireNonNull(predicate, "predicate is null");
            LinearSeq init = List.Nil.instance();
            LinearSeq<T> tail = source2;
            while (!tail.isEmpty() && !predicate.test(tail.head())) {
                init = init.prepend(tail.head());
                tail = tail.tail();
            }
            return Tuple.of(init, tail);
        }
    }

    public static interface Combinations {
        public static <T> List<List<T>> apply(List<T> elements, int k) {
            if (k == 0) {
                return List.of(List.empty());
            }
            return elements.zipWithIndex().flatMap(t -> Combinations.apply(elements.drop((Integer)t._2 + 1), k - 1).map(c -> c.prepend(t._1)));
        }
    }
}

