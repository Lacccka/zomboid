/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Function1;
import io.vavr.PartialFunction;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.Iterator;
import io.vavr.collection.Map;
import io.vavr.collection.Traversable;
import io.vavr.control.Option;
import java.io.Serializable;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Set<T>
extends Traversable<T>,
Function1<T, Boolean>,
Serializable {
    public static final long serialVersionUID = 1L;

    public static <T> Set<T> narrow(Set<? extends T> set) {
        return set;
    }

    public Set<T> add(T var1);

    public Set<T> addAll(Iterable<? extends T> var1);

    @Override
    @Deprecated
    default public Boolean apply(T element) {
        return this.contains(element);
    }

    public Set<T> diff(Set<? extends T> var1);

    public Set<T> intersect(Set<? extends T> var1);

    public Set<T> remove(T var1);

    public Set<T> removeAll(Iterable<? extends T> var1);

    @Override
    public java.util.Set<T> toJavaSet();

    public Set<T> union(Set<? extends T> var1);

    @Override
    public <R> Set<R> collect(PartialFunction<? super T, ? extends R> var1);

    @Override
    public boolean contains(T var1);

    @Override
    public Set<T> distinct();

    @Override
    public Set<T> distinctBy(Comparator<? super T> var1);

    @Override
    public <U> Set<T> distinctBy(Function<? super T, ? extends U> var1);

    @Override
    public Set<T> drop(int var1);

    @Override
    public Set<T> dropRight(int var1);

    @Override
    public Set<T> dropUntil(Predicate<? super T> var1);

    @Override
    public Set<T> dropWhile(Predicate<? super T> var1);

    @Override
    public Set<T> filter(Predicate<? super T> var1);

    @Override
    public Set<T> reject(Predicate<? super T> var1);

    @Override
    public <U> Set<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> var1);

    @Override
    public <C> Map<C, ? extends Set<T>> groupBy(Function<? super T, ? extends C> var1);

    @Override
    public Iterator<? extends Set<T>> grouped(int var1);

    @Override
    public Set<T> init();

    @Override
    public Option<? extends Set<T>> initOption();

    @Override
    default public boolean isDistinct() {
        return true;
    }

    @Override
    public Iterator<T> iterator();

    @Override
    public int length();

    @Override
    public <U> Set<U> map(Function<? super T, ? extends U> var1);

    @Override
    public Set<T> orElse(Iterable<? extends T> var1);

    @Override
    public Set<T> orElse(Supplier<? extends Iterable<? extends T>> var1);

    @Override
    public Tuple2<? extends Set<T>, ? extends Set<T>> partition(Predicate<? super T> var1);

    @Override
    public Set<T> peek(Consumer<? super T> var1);

    @Override
    public Set<T> replace(T var1, T var2);

    @Override
    public Set<T> replaceAll(T var1, T var2);

    @Override
    public Set<T> retainAll(Iterable<? extends T> var1);

    @Override
    public Set<T> scan(T var1, BiFunction<? super T, ? super T, ? extends T> var2);

    @Override
    public <U> Set<U> scanLeft(U var1, BiFunction<? super U, ? super T, ? extends U> var2);

    @Override
    public <U> Set<U> scanRight(U var1, BiFunction<? super T, ? super U, ? extends U> var2);

    @Override
    public Iterator<? extends Set<T>> slideBy(Function<? super T, ?> var1);

    @Override
    public Iterator<? extends Set<T>> sliding(int var1);

    @Override
    public Iterator<? extends Set<T>> sliding(int var1, int var2);

    @Override
    public Tuple2<? extends Set<T>, ? extends Set<T>> span(Predicate<? super T> var1);

    @Override
    public Set<T> tail();

    @Override
    public Option<? extends Set<T>> tailOption();

    @Override
    public Set<T> take(int var1);

    @Override
    public Set<T> takeRight(int var1);

    @Override
    public Set<T> takeUntil(Predicate<? super T> var1);

    @Override
    public Set<T> takeWhile(Predicate<? super T> var1);

    @Override
    public <T1, T2> Tuple2<? extends Set<T1>, ? extends Set<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> var1);

    @Override
    public <T1, T2, T3> Tuple3<? extends Set<T1>, ? extends Set<T2>, ? extends Set<T3>> unzip3(Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> var1);

    @Override
    public <U> Set<Tuple2<T, U>> zip(Iterable<? extends U> var1);

    @Override
    public <U, R> Set<R> zipWith(Iterable<? extends U> var1, BiFunction<? super T, ? super U, ? extends R> var2);

    @Override
    public <U> Set<Tuple2<T, U>> zipAll(Iterable<? extends U> var1, T var2, U var3);

    @Override
    public Set<Tuple2<T, Integer>> zipWithIndex();

    @Override
    public <U> Set<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> var1);
}

