/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.PartialFunction;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.Iterator;
import io.vavr.collection.Map;
import io.vavr.collection.Ordered;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface SortedSet<T>
extends Set<T>,
Ordered<T> {
    public static final long serialVersionUID = 1L;

    public static <T> SortedSet<T> narrow(SortedSet<? extends T> sortedSet) {
        return sortedSet;
    }

    public <U> SortedSet<U> flatMap(Comparator<? super U> var1, Function<? super T, ? extends Iterable<? extends U>> var2);

    public <U> SortedSet<U> map(Comparator<? super U> var1, Function<? super T, ? extends U> var2);

    @Override
    public SortedSet<T> add(T var1);

    @Override
    public SortedSet<T> addAll(Iterable<? extends T> var1);

    @Override
    public <R> SortedSet<R> collect(PartialFunction<? super T, ? extends R> var1);

    @Override
    public SortedSet<T> diff(Set<? extends T> var1);

    @Override
    public SortedSet<T> distinct();

    @Override
    public SortedSet<T> distinctBy(Comparator<? super T> var1);

    @Override
    public <U> SortedSet<T> distinctBy(Function<? super T, ? extends U> var1);

    @Override
    public SortedSet<T> drop(int var1);

    @Override
    public SortedSet<T> dropRight(int var1);

    @Override
    public SortedSet<T> dropUntil(Predicate<? super T> var1);

    @Override
    public SortedSet<T> dropWhile(Predicate<? super T> var1);

    @Override
    public SortedSet<T> filter(Predicate<? super T> var1);

    @Override
    public SortedSet<T> reject(Predicate<? super T> var1);

    @Override
    public <U> SortedSet<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> var1);

    @Override
    public <C> Map<C, ? extends SortedSet<T>> groupBy(Function<? super T, ? extends C> var1);

    @Override
    public Iterator<? extends SortedSet<T>> grouped(int var1);

    @Override
    public SortedSet<T> init();

    @Override
    public Option<? extends SortedSet<T>> initOption();

    @Override
    public SortedSet<T> intersect(Set<? extends T> var1);

    @Override
    default public boolean isOrdered() {
        return true;
    }

    @Override
    public <U> SortedSet<U> map(Function<? super T, ? extends U> var1);

    @Override
    public SortedSet<T> orElse(Iterable<? extends T> var1);

    @Override
    public SortedSet<T> orElse(Supplier<? extends Iterable<? extends T>> var1);

    @Override
    public Tuple2<? extends SortedSet<T>, ? extends SortedSet<T>> partition(Predicate<? super T> var1);

    @Override
    public SortedSet<T> peek(Consumer<? super T> var1);

    @Override
    public SortedSet<T> remove(T var1);

    @Override
    public SortedSet<T> removeAll(Iterable<? extends T> var1);

    @Override
    public SortedSet<T> replace(T var1, T var2);

    @Override
    public SortedSet<T> replaceAll(T var1, T var2);

    @Override
    public SortedSet<T> retainAll(Iterable<? extends T> var1);

    @Override
    public SortedSet<T> scan(T var1, BiFunction<? super T, ? super T, ? extends T> var2);

    @Override
    public <U> Set<U> scanLeft(U var1, BiFunction<? super U, ? super T, ? extends U> var2);

    @Override
    public <U> Set<U> scanRight(U var1, BiFunction<? super T, ? super U, ? extends U> var2);

    @Override
    public Iterator<? extends SortedSet<T>> slideBy(Function<? super T, ?> var1);

    @Override
    public Iterator<? extends SortedSet<T>> sliding(int var1);

    @Override
    public Iterator<? extends SortedSet<T>> sliding(int var1, int var2);

    @Override
    public Tuple2<? extends SortedSet<T>, ? extends SortedSet<T>> span(Predicate<? super T> var1);

    @Override
    public SortedSet<T> tail();

    @Override
    public Option<? extends SortedSet<T>> tailOption();

    @Override
    public SortedSet<T> take(int var1);

    @Override
    public SortedSet<T> takeRight(int var1);

    @Override
    public SortedSet<T> takeUntil(Predicate<? super T> var1);

    @Override
    public SortedSet<T> takeWhile(Predicate<? super T> var1);

    @Override
    public java.util.SortedSet<T> toJavaSet();

    @Override
    public SortedSet<T> union(Set<? extends T> var1);

    @Override
    public <T1, T2> Tuple2<? extends SortedSet<T1>, ? extends SortedSet<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> var1);

    @Override
    public <T1, T2, T3> Tuple3<? extends SortedSet<T1>, ? extends SortedSet<T2>, ? extends SortedSet<T3>> unzip3(Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> var1);

    @Override
    public <U> SortedSet<Tuple2<T, U>> zip(Iterable<? extends U> var1);

    @Override
    public <U, R> SortedSet<R> zipWith(Iterable<? extends U> var1, BiFunction<? super T, ? super U, ? extends R> var2);

    @Override
    public <U> SortedSet<Tuple2<T, U>> zipAll(Iterable<? extends U> var1, T var2, U var3);

    @Override
    public SortedSet<Tuple2<T, Integer>> zipWithIndex();

    @Override
    public <U> SortedSet<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> var1);
}

