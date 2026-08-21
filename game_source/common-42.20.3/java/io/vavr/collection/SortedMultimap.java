/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Tuple2;
import io.vavr.collection.Iterator;
import io.vavr.collection.Map;
import io.vavr.collection.Multimap;
import io.vavr.collection.Ordered;
import io.vavr.collection.SortedSet;
import io.vavr.collection.Traversable;
import io.vavr.control.Option;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface SortedMultimap<K, V>
extends Multimap<K, V>,
Ordered<K> {
    public static final long serialVersionUID = 1L;

    public static <K, V> SortedMultimap<K, V> narrow(SortedMultimap<? extends K, ? extends V> map) {
        return map;
    }

    @Override
    public SortedMultimap<K, V> filter(BiPredicate<? super K, ? super V> var1);

    @Override
    public SortedMultimap<K, V> reject(BiPredicate<? super K, ? super V> var1);

    @Override
    public SortedMultimap<K, V> filterKeys(Predicate<? super K> var1);

    @Override
    public SortedMultimap<K, V> rejectKeys(Predicate<? super K> var1);

    @Override
    public SortedMultimap<K, V> filterValues(Predicate<? super V> var1);

    @Override
    public SortedMultimap<K, V> rejectValues(Predicate<? super V> var1);

    @Override
    public SortedSet<K> keySet();

    @Override
    public SortedMultimap<K, V> merge(Multimap<? extends K, ? extends V> var1);

    @Override
    public <K2 extends K, V2 extends V> SortedMultimap<K, V> merge(Multimap<K2, V2> var1, BiFunction<Traversable<V>, Traversable<V2>, Traversable<V>> var2);

    @Override
    public SortedMultimap<K, V> put(K var1, V var2);

    @Override
    public SortedMultimap<K, V> put(Tuple2<? extends K, ? extends V> var1);

    @Override
    public SortedMultimap<K, V> remove(K var1);

    @Override
    public SortedMultimap<K, V> remove(K var1, V var2);

    @Override
    @Deprecated
    public SortedMultimap<K, V> removeAll(BiPredicate<? super K, ? super V> var1);

    @Override
    public SortedMultimap<K, V> removeAll(Iterable<? extends K> var1);

    @Override
    @Deprecated
    public SortedMultimap<K, V> removeKeys(Predicate<? super K> var1);

    @Override
    @Deprecated
    public SortedMultimap<K, V> removeValues(Predicate<? super V> var1);

    @Override
    public SortedMap<K, Collection<V>> toJavaMap();

    @Override
    public SortedMultimap<K, V> distinct();

    @Override
    public SortedMultimap<K, V> distinctBy(Comparator<? super Tuple2<K, V>> var1);

    @Override
    public <U> SortedMultimap<K, V> distinctBy(Function<? super Tuple2<K, V>, ? extends U> var1);

    @Override
    public SortedMultimap<K, V> drop(int var1);

    @Override
    public SortedMultimap<K, V> dropRight(int var1);

    @Override
    public SortedMultimap<K, V> dropUntil(Predicate<? super Tuple2<K, V>> var1);

    @Override
    public SortedMultimap<K, V> dropWhile(Predicate<? super Tuple2<K, V>> var1);

    @Override
    public SortedMultimap<K, V> filter(Predicate<? super Tuple2<K, V>> var1);

    @Override
    public SortedMultimap<K, V> reject(Predicate<? super Tuple2<K, V>> var1);

    @Override
    public <C> Map<C, ? extends SortedMultimap<K, V>> groupBy(Function<? super Tuple2<K, V>, ? extends C> var1);

    @Override
    public Iterator<? extends SortedMultimap<K, V>> grouped(int var1);

    @Override
    public SortedMultimap<K, V> init();

    @Override
    public Option<? extends SortedMultimap<K, V>> initOption();

    @Override
    public SortedMultimap<K, V> orElse(Iterable<? extends Tuple2<K, V>> var1);

    @Override
    public SortedMultimap<K, V> orElse(Supplier<? extends Iterable<? extends Tuple2<K, V>>> var1);

    @Override
    public Tuple2<? extends SortedMultimap<K, V>, ? extends SortedMultimap<K, V>> partition(Predicate<? super Tuple2<K, V>> var1);

    @Override
    public SortedMultimap<K, V> peek(Consumer<? super Tuple2<K, V>> var1);

    @Override
    public SortedMultimap<K, V> replace(Tuple2<K, V> var1, Tuple2<K, V> var2);

    @Override
    public SortedMultimap<K, V> replaceAll(Tuple2<K, V> var1, Tuple2<K, V> var2);

    @Override
    public SortedMultimap<K, V> replaceValue(K var1, V var2);

    @Override
    public SortedMultimap<K, V> replace(K var1, V var2, V var3);

    @Override
    public SortedMultimap<K, V> replaceAll(BiFunction<? super K, ? super V, ? extends V> var1);

    @Override
    public SortedMultimap<K, V> retainAll(Iterable<? extends Tuple2<K, V>> var1);

    @Override
    public SortedMultimap<K, V> scan(Tuple2<K, V> var1, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K, V>, ? extends Tuple2<K, V>> var2);

    @Override
    public Iterator<? extends SortedMultimap<K, V>> slideBy(Function<? super Tuple2<K, V>, ?> var1);

    @Override
    public Iterator<? extends SortedMultimap<K, V>> sliding(int var1);

    @Override
    public Iterator<? extends SortedMultimap<K, V>> sliding(int var1, int var2);

    @Override
    public Tuple2<? extends SortedMultimap<K, V>, ? extends SortedMultimap<K, V>> span(Predicate<? super Tuple2<K, V>> var1);

    @Override
    public SortedMultimap<K, V> tail();

    @Override
    public Option<? extends SortedMultimap<K, V>> tailOption();

    @Override
    public SortedMultimap<K, V> take(int var1);

    @Override
    public SortedMultimap<K, V> takeRight(int var1);

    @Override
    public SortedMultimap<K, V> takeUntil(Predicate<? super Tuple2<K, V>> var1);

    @Override
    public SortedMultimap<K, V> takeWhile(Predicate<? super Tuple2<K, V>> var1);
}

