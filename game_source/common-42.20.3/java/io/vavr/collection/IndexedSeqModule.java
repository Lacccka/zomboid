/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.collection.Collections;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Vector;
import java.util.function.IntUnaryOperator;

interface IndexedSeqModule {

    public static interface Search {
        public static <T> int binarySearch(IndexedSeq<T> seq, IntUnaryOperator comparison) {
            int low = 0;
            int high = seq.size() - 1;
            while (low <= high) {
                int mid = low + high >>> 1;
                int cmp = comparison.applyAsInt(mid);
                if (cmp < 0) {
                    low = mid + 1;
                    continue;
                }
                if (cmp > 0) {
                    high = mid - 1;
                    continue;
                }
                return mid;
            }
            return -(low + 1);
        }
    }

    public static class Slice {
        static <T> int indexOfSlice(IndexedSeq<T> source2, Iterable<? extends T> slice, int from) {
            if (source2.isEmpty()) {
                return from == 0 && Collections.isEmpty(slice) ? 0 : -1;
            }
            IndexedSeq<T> _slice = Slice.toIndexedSeq(slice);
            int maxIndex = source2.length() - _slice.length();
            return Slice.findSlice(source2, _slice, Math.max(from, 0), maxIndex);
        }

        static <T> int lastIndexOfSlice(IndexedSeq<T> source2, Iterable<? extends T> slice, int end) {
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
            IndexedSeq<T> _slice = Slice.toIndexedSeq(slice);
            int maxIndex = source2.length() - _slice.length();
            while (index <= maxIndex) {
                int indexOfSlice = Slice.findSlice(source2, _slice, index, maxIndex);
                if (indexOfSlice < 0) {
                    return result;
                }
                if (indexOfSlice <= end) {
                    result = indexOfSlice;
                    index = indexOfSlice + 1;
                    continue;
                }
                return result;
            }
            return result;
        }

        private static <T> int findSlice(IndexedSeq<T> source2, IndexedSeq<T> slice, int index, int maxIndex) {
            while (index <= maxIndex) {
                if (source2.startsWith(slice, index)) {
                    return index;
                }
                ++index;
            }
            return -1;
        }

        private static <T> IndexedSeq<T> toIndexedSeq(Iterable<? extends T> iterable) {
            return iterable instanceof IndexedSeq ? (Vector<? extends T>)iterable : Vector.ofAll(iterable);
        }
    }
}

