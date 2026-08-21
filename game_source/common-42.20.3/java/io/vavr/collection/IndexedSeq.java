/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.PartialFunction;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.AbstractIterator;
import io.vavr.collection.GwtIncompatible;
import io.vavr.collection.IndexedSeqModule;
import io.vavr.collection.Iterator;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface IndexedSeq<T>
extends Seq<T> {
    public static final long serialVersionUID = 1L;

    public static <T> IndexedSeq<T> narrow(IndexedSeq<? extends T> indexedSeq) {
        return indexedSeq;
    }

    @Override
    public IndexedSeq<T> append(T var1);

    @Override
    public IndexedSeq<T> appendAll(Iterable<? extends T> var1);

    @Override
    @GwtIncompatible
    public IndexedSeq<T> asJava(Consumer<? super List<T>> var1);

    @Override
    @GwtIncompatible
    public IndexedSeq<T> asJavaMutable(Consumer<? super List<T>> var1);

    @Override
    default public PartialFunction<Integer, T> asPartialFunction() throws IndexOutOfBoundsException {
        return new PartialFunction<Integer, T>(){
            private static final long serialVersionUID = 1L;

            @Override
            public T apply(Integer index) {
                return IndexedSeq.this.get(index);
            }

            @Override
            public boolean isDefinedAt(Integer index) {
                return 0 <= index && index < IndexedSeq.this.length();
            }
        };
    }

    @Override
    public <R> IndexedSeq<R> collect(PartialFunction<? super T, ? extends R> var1);

    public IndexedSeq<? extends IndexedSeq<T>> combinations();

    public IndexedSeq<? extends IndexedSeq<T>> combinations(int var1);

    @Override
    public Iterator<? extends IndexedSeq<T>> crossProduct(int var1);

    @Override
    public IndexedSeq<T> distinct();

    @Override
    public IndexedSeq<T> distinctBy(Comparator<? super T> var1);

    @Override
    public <U> IndexedSeq<T> distinctBy(Function<? super T, ? extends U> var1);

    @Override
    public IndexedSeq<T> drop(int var1);

    @Override
    public IndexedSeq<T> dropUntil(Predicate<? super T> var1);

    @Override
    public IndexedSeq<T> dropWhile(Predicate<? super T> var1);

    @Override
    public IndexedSeq<T> dropRight(int var1);

    @Override
    public IndexedSeq<T> dropRightUntil(Predicate<? super T> var1);

    @Override
    public IndexedSeq<T> dropRightWhile(Predicate<? super T> var1);

    @Override
    default public boolean endsWith(Seq<? extends T> that) {
        Objects.requireNonNull(that, "that is null");
        if (that instanceof IndexedSeq) {
            int i = this.length() - 1;
            int j = that.length() - 1;
            if (j > i) {
                return false;
            }
            while (j >= 0) {
                if (!Objects.equals(this.get(i), that.get(j))) {
                    return false;
                }
                --i;
                --j;
            }
            return true;
        }
        return Seq.super.endsWith(that);
    }

    @Override
    public IndexedSeq<T> filter(Predicate<? super T> var1);

    @Override
    public IndexedSeq<T> reject(Predicate<? super T> var1);

    @Override
    public <U> IndexedSeq<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> var1);

    @Override
    public <C> Map<C, ? extends IndexedSeq<T>> groupBy(Function<? super T, ? extends C> var1);

    @Override
    default public int indexWhere(Predicate<? super T> predicate, int from) {
        Objects.requireNonNull(predicate, "predicate is null");
        int start = Math.max(from, 0);
        int n = start + this.segmentLength(predicate.negate(), start);
        return n >= this.length() ? -1 : n;
    }

    @Override
    public Iterator<? extends IndexedSeq<T>> grouped(int var1);

    @Override
    default public int indexOfSlice(Iterable<? extends T> that, int from) {
        Objects.requireNonNull(that, "that is null");
        return IndexedSeqModule.Slice.indexOfSlice(this, that, from);
    }

    @Override
    public IndexedSeq<T> init();

    @Override
    public Option<? extends IndexedSeq<T>> initOption();

    @Override
    public IndexedSeq<T> insert(int var1, T var2);

    @Override
    public IndexedSeq<T> insertAll(int var1, Iterable<? extends T> var2);

    @Override
    public IndexedSeq<T> intersperse(T var1);

    @Override
    @Deprecated
    default public boolean isDefinedAt(Integer index) {
        return 0 <= index && index < this.length();
    }

    @Override
    default public T last() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("last of empty IndexedSeq");
        }
        return this.get(this.length() - 1);
    }

    @Override
    default public int lastIndexOfSlice(Iterable<? extends T> that, int end) {
        Objects.requireNonNull(that, "that is null");
        return IndexedSeqModule.Slice.lastIndexOfSlice(this, that, end);
    }

    @Override
    default public int lastIndexWhere(Predicate<? super T> predicate, int end) {
        int i;
        Objects.requireNonNull(predicate, "predicate is null");
        for (i = Math.min(end, this.length() - 1); i >= 0 && !predicate.test(this.get(i)); --i) {
        }
        return i;
    }

    @Override
    public <U> IndexedSeq<U> map(Function<? super T, ? extends U> var1);

    @Override
    public IndexedSeq<T> orElse(Iterable<? extends T> var1);

    @Override
    public IndexedSeq<T> orElse(Supplier<? extends Iterable<? extends T>> var1);

    @Override
    public IndexedSeq<T> padTo(int var1, T var2);

    @Override
    public IndexedSeq<T> patch(int var1, Iterable<? extends T> var2, int var3);

    @Override
    public Tuple2<? extends IndexedSeq<T>, ? extends IndexedSeq<T>> partition(Predicate<? super T> var1);

    @Override
    public IndexedSeq<T> peek(Consumer<? super T> var1);

    public IndexedSeq<? extends IndexedSeq<T>> permutations();

    @Override
    public IndexedSeq<T> prepend(T var1);

    @Override
    public IndexedSeq<T> prependAll(Iterable<? extends T> var1);

    @Override
    public IndexedSeq<T> remove(T var1);

    @Override
    public IndexedSeq<T> removeFirst(Predicate<T> var1);

    @Override
    public IndexedSeq<T> removeLast(Predicate<T> var1);

    @Override
    public IndexedSeq<T> removeAt(int var1);

    @Override
    public IndexedSeq<T> removeAll(T var1);

    @Override
    public IndexedSeq<T> removeAll(Iterable<? extends T> var1);

    @Override
    @Deprecated
    public IndexedSeq<T> removeAll(Predicate<? super T> var1);

    @Override
    public IndexedSeq<T> replace(T var1, T var2);

    @Override
    public IndexedSeq<T> replaceAll(T var1, T var2);

    @Override
    public IndexedSeq<T> retainAll(Iterable<? extends T> var1);

    @Override
    public IndexedSeq<T> reverse();

    @Override
    default public Iterator<T> reverseIterator() {
        return new AbstractIterator<T>(){
            private int i;
            {
                this.i = IndexedSeq.this.length();
            }

            @Override
            public boolean hasNext() {
                return this.i > 0;
            }

            @Override
            public T getNext() {
                return IndexedSeq.this.get(--this.i);
            }
        };
    }

    @Override
    public IndexedSeq<T> rotateLeft(int var1);

    @Override
    public IndexedSeq<T> rotateRight(int var1);

    @Override
    public IndexedSeq<T> scan(T var1, BiFunction<? super T, ? super T, ? extends T> var2);

    @Override
    public <U> IndexedSeq<U> scanLeft(U var1, BiFunction<? super U, ? super T, ? extends U> var2);

    @Override
    public <U> IndexedSeq<U> scanRight(U var1, BiFunction<? super T, ? super U, ? extends U> var2);

    @Override
    default public int segmentLength(Predicate<? super T> predicate, int from) {
        int i;
        Objects.requireNonNull(predicate, "predicate is null");
        int len = this.length();
        for (i = from; i < len && predicate.test(this.get(i)); ++i) {
        }
        return i - from;
    }

    @Override
    public IndexedSeq<T> shuffle();

    @Override
    public IndexedSeq<T> slice(int var1, int var2);

    @Override
    public Iterator<? extends IndexedSeq<T>> slideBy(Function<? super T, ?> var1);

    @Override
    public Iterator<? extends IndexedSeq<T>> sliding(int var1);

    @Override
    public Iterator<? extends IndexedSeq<T>> sliding(int var1, int var2);

    @Override
    public IndexedSeq<T> sorted();

    @Override
    public IndexedSeq<T> sorted(Comparator<? super T> var1);

    @Override
    public <U extends Comparable<? super U>> IndexedSeq<T> sortBy(Function<? super T, ? extends U> var1);

    @Override
    public <U> IndexedSeq<T> sortBy(Comparator<? super U> var1, Function<? super T, ? extends U> var2);

    @Override
    public Tuple2<? extends IndexedSeq<T>, ? extends IndexedSeq<T>> span(Predicate<? super T> var1);

    @Override
    default public boolean startsWith(Iterable<? extends T> that, int offset) {
        Objects.requireNonNull(that, "that is null");
        if (offset < 0) {
            return false;
        }
        if (that instanceof IndexedSeq) {
            int j;
            IndexedSeq thatIndexedSeq = (IndexedSeq)that;
            int i = offset;
            int thisLen = this.length();
            int thatLen = thatIndexedSeq.length();
            for (j = 0; i < thisLen && j < thatLen && Objects.equals(this.get(i), thatIndexedSeq.get(j)); ++i, ++j) {
            }
            return j == thatLen;
        }
        int thisLen = this.length();
        java.util.Iterator<T> thatElems = that.iterator();
        for (int i = offset; i < thisLen && thatElems.hasNext(); ++i) {
            if (Objects.equals(this.get(i), thatElems.next())) continue;
            return false;
        }
        return !thatElems.hasNext();
    }

    @Override
    public IndexedSeq<T> subSequence(int var1);

    @Override
    public IndexedSeq<T> subSequence(int var1, int var2);

    @Override
    public IndexedSeq<T> tail();

    @Override
    public Option<? extends IndexedSeq<T>> tailOption();

    @Override
    public IndexedSeq<T> take(int var1);

    @Override
    public IndexedSeq<T> takeUntil(Predicate<? super T> var1);

    @Override
    public IndexedSeq<T> takeWhile(Predicate<? super T> var1);

    @Override
    public IndexedSeq<T> takeRight(int var1);

    @Override
    public IndexedSeq<T> takeRightUntil(Predicate<? super T> var1);

    @Override
    public IndexedSeq<T> takeRightWhile(Predicate<? super T> var1);

    @Override
    public <T1, T2> Tuple2<? extends IndexedSeq<T1>, ? extends IndexedSeq<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> var1);

    @Override
    public <T1, T2, T3> Tuple3<? extends IndexedSeq<T1>, ? extends IndexedSeq<T2>, ? extends IndexedSeq<T3>> unzip3(Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> var1);

    @Override
    public IndexedSeq<T> update(int var1, T var2);

    @Override
    public IndexedSeq<T> update(int var1, Function<? super T, ? extends T> var2);

    @Override
    public <U> IndexedSeq<Tuple2<T, U>> zip(Iterable<? extends U> var1);

    @Override
    public <U, R> IndexedSeq<R> zipWith(Iterable<? extends U> var1, BiFunction<? super T, ? super U, ? extends R> var2);

    @Override
    public <U> IndexedSeq<Tuple2<T, U>> zipAll(Iterable<? extends U> var1, T var2, U var3);

    @Override
    public IndexedSeq<Tuple2<T, Integer>> zipWithIndex();

    @Override
    public <U> IndexedSeq<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> var1);

    @Override
    default public int search(T element) {
        IntUnaryOperator comparison = midIndex -> {
            Comparable midVal = (Comparable)this.get(midIndex);
            return midVal.compareTo(element);
        };
        return IndexedSeqModule.Search.binarySearch(this, comparison);
    }

    @Override
    default public int search(T element, Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        IntUnaryOperator comparison = midIndex -> {
            Object midVal = this.get(midIndex);
            return comparator.compare((T)midVal, (T)element);
        };
        return IndexedSeqModule.Search.binarySearch(this, comparison);
    }
}

