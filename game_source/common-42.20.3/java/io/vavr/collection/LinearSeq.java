/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.PartialFunction;
import io.vavr.Tuple2;
import io.vavr.collection.GwtIncompatible;
import io.vavr.collection.Iterator;
import io.vavr.collection.LinearSeqModule;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public interface LinearSeq<T>
extends Seq<T> {
    public static final long serialVersionUID = 1L;

    public static <T> LinearSeq<T> narrow(LinearSeq<? extends T> linearSeq) {
        return linearSeq;
    }

    @Override
    public LinearSeq<T> append(T var1);

    @Override
    public LinearSeq<T> appendAll(Iterable<? extends T> var1);

    @Override
    @GwtIncompatible
    public LinearSeq<T> asJava(Consumer<? super List<T>> var1);

    @Override
    @GwtIncompatible
    public LinearSeq<T> asJavaMutable(Consumer<? super List<T>> var1);

    @Override
    default public PartialFunction<Integer, T> asPartialFunction() throws IndexOutOfBoundsException {
        return new PartialFunction<Integer, T>(){
            private static final long serialVersionUID = 1L;

            @Override
            public T apply(Integer index) {
                return LinearSeq.this.get(index);
            }

            @Override
            public boolean isDefinedAt(Integer index) {
                return 0 <= index && LinearSeq.this.drop(index).nonEmpty();
            }
        };
    }

    @Override
    public <R> LinearSeq<R> collect(PartialFunction<? super T, ? extends R> var1);

    public LinearSeq<? extends LinearSeq<T>> combinations();

    public LinearSeq<? extends LinearSeq<T>> combinations(int var1);

    @Override
    public Iterator<? extends LinearSeq<T>> crossProduct(int var1);

    @Override
    public LinearSeq<T> distinct();

    @Override
    public LinearSeq<T> distinctBy(Comparator<? super T> var1);

    @Override
    public <U> LinearSeq<T> distinctBy(Function<? super T, ? extends U> var1);

    @Override
    public LinearSeq<T> drop(int var1);

    @Override
    public LinearSeq<T> dropUntil(Predicate<? super T> var1);

    @Override
    public LinearSeq<T> dropWhile(Predicate<? super T> var1);

    @Override
    public LinearSeq<T> dropRight(int var1);

    @Override
    public LinearSeq<T> dropRightUntil(Predicate<? super T> var1);

    @Override
    public LinearSeq<T> dropRightWhile(Predicate<? super T> var1);

    @Override
    public LinearSeq<T> filter(Predicate<? super T> var1);

    @Override
    public LinearSeq<T> reject(Predicate<? super T> var1);

    @Override
    public <U> LinearSeq<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> var1);

    @Override
    public <C> Map<C, ? extends LinearSeq<T>> groupBy(Function<? super T, ? extends C> var1);

    @Override
    public Iterator<? extends LinearSeq<T>> grouped(int var1);

    @Override
    default public int indexOfSlice(Iterable<? extends T> that, int from) {
        Objects.requireNonNull(that, "that is null");
        return LinearSeqModule.Slice.indexOfSlice(this, that, from);
    }

    @Override
    default public int indexWhere(Predicate<? super T> predicate, int from) {
        Objects.requireNonNull(predicate, "predicate is null");
        int i = from;
        Seq these = this.drop(from);
        while (!these.isEmpty()) {
            if (predicate.test(these.head())) {
                return i;
            }
            ++i;
            these = these.tail();
        }
        return -1;
    }

    @Override
    public LinearSeq<T> init();

    @Override
    public Option<? extends LinearSeq<T>> initOption();

    @Override
    public LinearSeq<T> insert(int var1, T var2);

    @Override
    public LinearSeq<T> insertAll(int var1, Iterable<? extends T> var2);

    @Override
    public LinearSeq<T> intersperse(T var1);

    @Override
    @Deprecated
    default public boolean isDefinedAt(Integer index) {
        return 0 <= index && this.drop(index).nonEmpty();
    }

    @Override
    default public int lastIndexOfSlice(Iterable<? extends T> that, int end) {
        Objects.requireNonNull(that, "that is null");
        return LinearSeqModule.Slice.lastIndexOfSlice(this, that, end);
    }

    @Override
    default public int lastIndexWhere(Predicate<? super T> predicate, int end) {
        Objects.requireNonNull(predicate, "predicate is null");
        Seq<T> these = this;
        int last = -1;
        for (int i = 0; !these.isEmpty() && i <= end; ++i) {
            if (predicate.test(these.head())) {
                last = i;
            }
            these = these.tail();
        }
        return last;
    }

    @Override
    public <U> LinearSeq<U> map(Function<? super T, ? extends U> var1);

    @Override
    public LinearSeq<T> orElse(Iterable<? extends T> var1);

    @Override
    public LinearSeq<T> orElse(Supplier<? extends Iterable<? extends T>> var1);

    @Override
    public LinearSeq<T> padTo(int var1, T var2);

    @Override
    public LinearSeq<T> patch(int var1, Iterable<? extends T> var2, int var3);

    @Override
    public Tuple2<? extends LinearSeq<T>, ? extends LinearSeq<T>> partition(Predicate<? super T> var1);

    @Override
    public LinearSeq<T> peek(Consumer<? super T> var1);

    public LinearSeq<? extends LinearSeq<T>> permutations();

    @Override
    public LinearSeq<T> prepend(T var1);

    @Override
    public LinearSeq<T> prependAll(Iterable<? extends T> var1);

    @Override
    public LinearSeq<T> remove(T var1);

    @Override
    public LinearSeq<T> removeFirst(Predicate<T> var1);

    @Override
    public LinearSeq<T> removeLast(Predicate<T> var1);

    @Override
    public LinearSeq<T> removeAt(int var1);

    @Override
    public LinearSeq<T> removeAll(T var1);

    @Override
    public LinearSeq<T> removeAll(Iterable<? extends T> var1);

    @Override
    @Deprecated
    public LinearSeq<T> removeAll(Predicate<? super T> var1);

    @Override
    public LinearSeq<T> replace(T var1, T var2);

    @Override
    public LinearSeq<T> replaceAll(T var1, T var2);

    @Override
    public LinearSeq<T> retainAll(Iterable<? extends T> var1);

    @Override
    public LinearSeq<T> reverse();

    @Override
    default public Iterator<T> reverseIterator() {
        return this.reverse().iterator();
    }

    @Override
    public LinearSeq<T> rotateLeft(int var1);

    @Override
    public LinearSeq<T> rotateRight(int var1);

    @Override
    public LinearSeq<T> shuffle();

    @Override
    public LinearSeq<T> scan(T var1, BiFunction<? super T, ? super T, ? extends T> var2);

    @Override
    public <U> LinearSeq<U> scanLeft(U var1, BiFunction<? super U, ? super T, ? extends U> var2);

    @Override
    public <U> LinearSeq<U> scanRight(U var1, BiFunction<? super T, ? super U, ? extends U> var2);

    @Override
    default public int segmentLength(Predicate<? super T> predicate, int from) {
        Objects.requireNonNull(predicate, "predicate is null");
        int i = 0;
        Seq these = this.drop(from);
        while (!these.isEmpty() && predicate.test(these.head())) {
            ++i;
            these = these.tail();
        }
        return i;
    }

    @Override
    public LinearSeq<T> slice(int var1, int var2);

    @Override
    public Iterator<? extends LinearSeq<T>> slideBy(Function<? super T, ?> var1);

    @Override
    public Iterator<? extends LinearSeq<T>> sliding(int var1);

    @Override
    public Iterator<? extends LinearSeq<T>> sliding(int var1, int var2);

    @Override
    public LinearSeq<T> sorted();

    @Override
    public LinearSeq<T> sorted(Comparator<? super T> var1);

    @Override
    public <U extends Comparable<? super U>> LinearSeq<T> sortBy(Function<? super T, ? extends U> var1);

    @Override
    public <U> LinearSeq<T> sortBy(Comparator<? super U> var1, Function<? super T, ? extends U> var2);

    @Override
    public Tuple2<? extends LinearSeq<T>, ? extends LinearSeq<T>> span(Predicate<? super T> var1);

    @Override
    public LinearSeq<T> subSequence(int var1);

    @Override
    public LinearSeq<T> subSequence(int var1, int var2);

    @Override
    public LinearSeq<T> tail();

    @Override
    public Option<? extends LinearSeq<T>> tailOption();

    @Override
    public LinearSeq<T> take(int var1);

    @Override
    public LinearSeq<T> takeUntil(Predicate<? super T> var1);

    @Override
    public LinearSeq<T> takeWhile(Predicate<? super T> var1);

    @Override
    public LinearSeq<T> takeRight(int var1);

    @Override
    public LinearSeq<T> takeRightUntil(Predicate<? super T> var1);

    @Override
    public LinearSeq<T> takeRightWhile(Predicate<? super T> var1);

    @Override
    public <T1, T2> Tuple2<? extends LinearSeq<T1>, ? extends LinearSeq<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> var1);

    @Override
    public LinearSeq<T> update(int var1, T var2);

    @Override
    public LinearSeq<T> update(int var1, Function<? super T, ? extends T> var2);

    @Override
    public <U> LinearSeq<Tuple2<T, U>> zip(Iterable<? extends U> var1);

    @Override
    public <U, R> LinearSeq<R> zipWith(Iterable<? extends U> var1, BiFunction<? super T, ? super U, ? extends R> var2);

    @Override
    public <U> LinearSeq<Tuple2<T, U>> zipAll(Iterable<? extends U> var1, T var2, U var3);

    @Override
    public LinearSeq<Tuple2<T, Integer>> zipWithIndex();

    @Override
    public <U> LinearSeq<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> var1);

    @Override
    default public int search(T element) {
        ToIntFunction<Object> comparison = ((Comparable)element)::compareTo;
        return LinearSeqModule.Search.linearSearch(this, comparison);
    }

    @Override
    default public int search(T element, Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        ToIntFunction<Object> comparison = current -> comparator.compare(element, current);
        return LinearSeqModule.Search.linearSearch(this, comparison);
    }
}

