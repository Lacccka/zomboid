/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Collections;
import io.vavr.collection.LinearSeq;
import io.vavr.collection.List;
import io.vavr.collection.Traversable;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

interface LinearSeqModule {

    public static interface Search {
        public static <T> int linearSearch(LinearSeq<T> seq, ToIntFunction<T> comparison) {
            int idx = 0;
            for (Object current : seq) {
                int cmp = comparison.applyAsInt(current);
                if (cmp == 0) {
                    return idx;
                }
                if (cmp < 0) {
                    return -(idx + 1);
                }
                ++idx;
            }
            return -(idx + 1);
        }
    }

    public static class Slice {
        static <T> int indexOfSlice(LinearSeq<T> source2, Iterable<? extends T> slice, int from) {
            if (source2.isEmpty()) {
                return from == 0 && Collections.isEmpty(slice) ? 0 : -1;
            }
            LinearSeq<? extends T> _slice = Slice.toLinearSeq(slice);
            return Slice.findFirstSlice(source2, _slice, Math.max(from, 0));
        }

        static <T> int lastIndexOfSlice(LinearSeq<T> source2, Iterable<? extends T> slice, int end) {
            if (end < 0) {
                return -1;
            }
            if (source2.isEmpty()) {
                return Collections.isEmpty(slice) ? 0 : -1;
            }
            if (Collections.isEmpty(slice)) {
                int len = source2.length();
                return len < end ? len : end;
            }
            int index = 0;
            int result = -1;
            LinearSeq<T> _slice = Slice.toLinearSeq(slice);
            while (source2.length() >= _slice.length()) {
                Tuple2<LinearSeq<? extends T>, Integer> r = Slice.findNextSlice(source2, _slice);
                if (r == null) {
                    return result;
                }
                if (index + (Integer)r._2 <= end) {
                    result = index + (Integer)r._2;
                    index += (Integer)r._2 + 1;
                    source2 = ((LinearSeq)r._1).tail();
                    continue;
                }
                return result;
            }
            return result;
        }

        private static <T> int findFirstSlice(LinearSeq<T> source2, LinearSeq<T> slice, int from) {
            Predicate<LinearSeq> hasMore;
            int index = 0;
            int sliceLength = slice.length();
            Predicate<LinearSeq> predicate = hasMore = source2.isLazy() ? Traversable::nonEmpty : seq -> seq.length() >= sliceLength;
            while (hasMore.test((LinearSeq)source2)) {
                if (index >= from && source2.startsWith(slice)) {
                    return index;
                }
                ++index;
                source2 = source2.tail();
            }
            return -1;
        }

        private static <T> Tuple2<LinearSeq<T>, Integer> findNextSlice(LinearSeq<T> source2, LinearSeq<T> slice) {
            int index = 0;
            while (source2.length() >= slice.length()) {
                if (source2.startsWith(slice)) {
                    return Tuple.of(source2, index);
                }
                ++index;
                source2 = source2.tail();
            }
            return null;
        }

        private static <T> LinearSeq<T> toLinearSeq(Iterable<? extends T> iterable) {
            return iterable instanceof LinearSeq ? (List<? extends T>)iterable : List.ofAll(iterable);
        }
    }
}

