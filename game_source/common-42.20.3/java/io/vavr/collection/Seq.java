/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Function1;
import io.vavr.PartialFunction;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.Collections;
import io.vavr.collection.GwtIncompatible;
import io.vavr.collection.Iterator;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;
import io.vavr.collection.Traversable;
import io.vavr.control.Option;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Seq<T>
extends Traversable<T>,
PartialFunction<Integer, T>,
Serializable {
    public static final long serialVersionUID = 1L;

    public static <T> Seq<T> narrow(Seq<? extends T> seq) {
        return seq;
    }

    public Seq<T> append(T var1);

    public Seq<T> appendAll(Iterable<? extends T> var1);

    @Override
    @Deprecated
    default public T apply(Integer index) {
        return this.get(index);
    }

    @GwtIncompatible
    public List<T> asJava();

    @GwtIncompatible
    public Seq<T> asJava(Consumer<? super List<T>> var1);

    @GwtIncompatible
    public List<T> asJavaMutable();

    @GwtIncompatible
    public Seq<T> asJavaMutable(Consumer<? super List<T>> var1);

    public PartialFunction<Integer, T> asPartialFunction() throws IndexOutOfBoundsException;

    @Override
    public <R> Seq<R> collect(PartialFunction<? super T, ? extends R> var1);

    public Seq<? extends Seq<T>> combinations();

    public Seq<? extends Seq<T>> combinations(int var1);

    default public boolean containsSlice(Iterable<? extends T> that) {
        Objects.requireNonNull(that, "that is null");
        return this.indexOfSlice(that) >= 0;
    }

    default public Iterator<Tuple2<T, T>> crossProduct() {
        return this.crossProduct(this);
    }

    public Iterator<? extends Seq<T>> crossProduct(int var1);

    default public <U> Iterator<Tuple2<T, U>> crossProduct(Iterable<? extends U> that) {
        Objects.requireNonNull(that, "that is null");
        Stream other = Stream.ofAll(that);
        return Iterator.ofAll(this).flatMap((T a) -> other.map((T b) -> Tuple.of(a, b)));
    }

    default public boolean endsWith(Seq<? extends T> that) {
        Objects.requireNonNull(that, "that is null");
        Traversable i = this.iterator().drop(this.length() - that.length());
        java.util.Iterator j = that.iterator();
        while (i.hasNext() && j.hasNext()) {
            if (Objects.equals(i.next(), j.next())) continue;
            return false;
        }
        return !j.hasNext();
    }

    public T get(int var1);

    default public int indexOf(T element) {
        return this.indexOf(element, 0);
    }

    default public Option<Integer> indexOfOption(T element) {
        return Collections.indexOption(this.indexOf(element));
    }

    public int indexOf(T var1, int var2);

    default public Option<Integer> indexOfOption(T element, int from) {
        return Collections.indexOption(this.indexOf(element, from));
    }

    default public int indexOfSlice(Iterable<? extends T> that) {
        return this.indexOfSlice(that, 0);
    }

    default public Option<Integer> indexOfSliceOption(Iterable<? extends T> that) {
        return Collections.indexOption(this.indexOfSlice(that));
    }

    public int indexOfSlice(Iterable<? extends T> var1, int var2);

    default public Option<Integer> indexOfSliceOption(Iterable<? extends T> that, int from) {
        return Collections.indexOption(this.indexOfSlice(that, from));
    }

    default public int indexWhere(Predicate<? super T> predicate) {
        return this.indexWhere(predicate, 0);
    }

    default public Option<Integer> indexWhereOption(Predicate<? super T> predicate) {
        return Collections.indexOption(this.indexWhere(predicate));
    }

    public int indexWhere(Predicate<? super T> var1, int var2);

    default public Option<Integer> indexWhereOption(Predicate<? super T> predicate, int from) {
        return Collections.indexOption(this.indexWhere(predicate, from));
    }

    public Seq<T> insert(int var1, T var2);

    public Seq<T> insertAll(int var1, Iterable<? extends T> var2);

    public Seq<T> intersperse(T var1);

    default public Iterator<T> iterator(int index) {
        return this.subSequence(index).iterator();
    }

    default public int lastIndexOf(T element) {
        return this.lastIndexOf(element, Integer.MAX_VALUE);
    }

    default public Option<Integer> lastIndexOfOption(T element) {
        return Collections.indexOption(this.lastIndexOf(element));
    }

    default public int lastIndexWhere(Predicate<? super T> predicate) {
        return this.lastIndexWhere(predicate, this.length() - 1);
    }

    default public Option<Integer> lastIndexWhereOption(Predicate<? super T> predicate) {
        return Collections.indexOption(this.lastIndexWhere(predicate));
    }

    public int lastIndexWhere(Predicate<? super T> var1, int var2);

    default public Option<Integer> lastIndexWhereOption(Predicate<? super T> predicate, int end) {
        return Collections.indexOption(this.lastIndexWhere(predicate, end));
    }

    @Override
    @Deprecated
    default public Function1<Integer, Option<T>> lift() {
        return i -> i >= 0 && i < this.length() ? Option.some(this.apply((Integer)i)) : Option.none();
    }

    public int lastIndexOf(T var1, int var2);

    default public Option<Integer> lastIndexOfOption(T element, int end) {
        return Collections.indexOption(this.lastIndexOf(element, end));
    }

    default public int lastIndexOfSlice(Iterable<? extends T> that) {
        return this.lastIndexOfSlice(that, Integer.MAX_VALUE);
    }

    default public Option<Integer> lastIndexOfSliceOption(Iterable<? extends T> that) {
        return Collections.indexOption(this.lastIndexOfSlice(that));
    }

    public int lastIndexOfSlice(Iterable<? extends T> var1, int var2);

    default public Option<Integer> lastIndexOfSliceOption(Iterable<? extends T> that, int end) {
        return Collections.indexOption(this.lastIndexOfSlice(that, end));
    }

    public Seq<T> padTo(int var1, T var2);

    public Seq<T> leftPadTo(int var1, T var2);

    public Seq<T> patch(int var1, Iterable<? extends T> var2, int var3);

    public Seq<? extends Seq<T>> permutations();

    default public int prefixLength(Predicate<? super T> predicate) {
        return this.segmentLength(predicate, 0);
    }

    public Seq<T> prepend(T var1);

    public Seq<T> prependAll(Iterable<? extends T> var1);

    public Seq<T> remove(T var1);

    public Seq<T> removeAll(T var1);

    public Seq<T> removeAll(Iterable<? extends T> var1);

    @Deprecated
    public Seq<T> removeAll(Predicate<? super T> var1);

    public Seq<T> removeAt(int var1);

    public Seq<T> removeFirst(Predicate<T> var1);

    public Seq<T> removeLast(Predicate<T> var1);

    public Seq<T> reverse();

    public Iterator<T> reverseIterator();

    public Seq<T> rotateLeft(int var1);

    public Seq<T> rotateRight(int var1);

    public int segmentLength(Predicate<? super T> var1, int var2);

    public Seq<T> shuffle();

    public Seq<T> slice(int var1, int var2);

    public Seq<T> sorted();

    public Seq<T> sorted(Comparator<? super T> var1);

    public <U extends Comparable<? super U>> Seq<T> sortBy(Function<? super T, ? extends U> var1);

    public <U> Seq<T> sortBy(Comparator<? super U> var1, Function<? super T, ? extends U> var2);

    public Tuple2<? extends Seq<T>, ? extends Seq<T>> splitAt(int var1);

    public Tuple2<? extends Seq<T>, ? extends Seq<T>> splitAt(Predicate<? super T> var1);

    public Tuple2<? extends Seq<T>, ? extends Seq<T>> splitAtInclusive(Predicate<? super T> var1);

    default public boolean startsWith(Iterable<? extends T> that) {
        return this.startsWith(that, 0);
    }

    default public boolean startsWith(Iterable<? extends T> that, int offset) {
        Objects.requireNonNull(that, "that is null");
        if (offset < 0) {
            return false;
        }
        Traversable i = this.iterator().drop(offset);
        java.util.Iterator<T> j = that.iterator();
        while (i.hasNext() && j.hasNext()) {
            if (Objects.equals(i.next(), j.next())) continue;
            return false;
        }
        return !j.hasNext();
    }

    public Seq<T> subSequence(int var1);

    public Seq<T> subSequence(int var1, int var2);

    public Seq<T> update(int var1, T var2);

    public Seq<T> update(int var1, Function<? super T, ? extends T> var2);

    public int search(T var1);

    public int search(T var1, Comparator<? super T> var2);

    @Override
    public Seq<T> distinct();

    @Override
    public Seq<T> distinctBy(Comparator<? super T> var1);

    @Override
    public <U> Seq<T> distinctBy(Function<? super T, ? extends U> var1);

    @Override
    public Seq<T> drop(int var1);

    @Override
    public Seq<T> dropUntil(Predicate<? super T> var1);

    @Override
    public Seq<T> dropWhile(Predicate<? super T> var1);

    @Override
    public Seq<T> dropRight(int var1);

    public Seq<T> dropRightUntil(Predicate<? super T> var1);

    public Seq<T> dropRightWhile(Predicate<? super T> var1);

    @Override
    public Seq<T> filter(Predicate<? super T> var1);

    @Override
    public Seq<T> reject(Predicate<? super T> var1);

    @Override
    public <U> Seq<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> var1);

    @Override
    default public <U> U foldRight(U zero, BiFunction<? super T, ? super U, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return (U)this.reverse().foldLeft(zero, (xs, x) -> f.apply(x, xs));
    }

    @Override
    public <C> Map<C, ? extends Seq<T>> groupBy(Function<? super T, ? extends C> var1);

    @Override
    public Iterator<? extends Seq<T>> grouped(int var1);

    @Override
    public Seq<T> init();

    @Override
    public Option<? extends Seq<T>> initOption();

    @Override
    public <U> Seq<U> map(Function<? super T, ? extends U> var1);

    @Override
    public Seq<T> orElse(Iterable<? extends T> var1);

    @Override
    public Seq<T> orElse(Supplier<? extends Iterable<? extends T>> var1);

    @Override
    public Tuple2<? extends Seq<T>, ? extends Seq<T>> partition(Predicate<? super T> var1);

    @Override
    public Seq<T> peek(Consumer<? super T> var1);

    @Override
    public Seq<T> replace(T var1, T var2);

    @Override
    public Seq<T> replaceAll(T var1, T var2);

    @Override
    public Seq<T> retainAll(Iterable<? extends T> var1);

    @Override
    public Seq<T> scan(T var1, BiFunction<? super T, ? super T, ? extends T> var2);

    @Override
    public <U> Seq<U> scanLeft(U var1, BiFunction<? super U, ? super T, ? extends U> var2);

    @Override
    public <U> Seq<U> scanRight(U var1, BiFunction<? super T, ? super U, ? extends U> var2);

    @Override
    public Iterator<? extends Seq<T>> slideBy(Function<? super T, ?> var1);

    @Override
    public Iterator<? extends Seq<T>> sliding(int var1);

    @Override
    public Iterator<? extends Seq<T>> sliding(int var1, int var2);

    @Override
    public Tuple2<? extends Seq<T>, ? extends Seq<T>> span(Predicate<? super T> var1);

    @Override
    public Seq<T> tail();

    @Override
    public Option<? extends Seq<T>> tailOption();

    @Override
    public Seq<T> take(int var1);

    @Override
    public Seq<T> takeUntil(Predicate<? super T> var1);

    @Override
    public Seq<T> takeWhile(Predicate<? super T> var1);

    @Override
    public Seq<T> takeRight(int var1);

    public Seq<T> takeRightUntil(Predicate<? super T> var1);

    public Seq<T> takeRightWhile(Predicate<? super T> var1);

    @Override
    public <T1, T2> Tuple2<? extends Seq<T1>, ? extends Seq<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> var1);

    @Override
    public <T1, T2, T3> Tuple3<? extends Seq<T1>, ? extends Seq<T2>, ? extends Seq<T3>> unzip3(Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> var1);

    @Override
    public <U> Seq<Tuple2<T, U>> zip(Iterable<? extends U> var1);

    @Override
    public <U, R> Seq<R> zipWith(Iterable<? extends U> var1, BiFunction<? super T, ? super U, ? extends R> var2);

    @Override
    public <U> Seq<Tuple2<T, U>> zipAll(Iterable<? extends U> var1, T var2, U var3);

    @Override
    public Seq<Tuple2<T, Integer>> zipWithIndex();

    @Override
    public <U> Seq<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> var1);

    @Deprecated
    default public Function1<Integer, T> withDefaultValue(T defaultValue) {
        return i -> i >= 0 && i < this.length() ? this.apply((Integer)i) : defaultValue;
    }

    @Deprecated
    default public Function1<Integer, T> withDefault(Function<? super Integer, ? extends T> defaultFunction) {
        return i -> i >= 0 && i < this.length() ? this.apply((Integer)i) : defaultFunction.apply((Integer)i);
    }

    @Override
    default public boolean isSequential() {
        return true;
    }
}

