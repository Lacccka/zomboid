/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Tuple2;
import io.vavr.collection.Iterator;
import io.vavr.collection.Map;
import io.vavr.collection.Ordered;
import io.vavr.collection.SortedSet;
import io.vavr.control.Option;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface SortedMap<K, V>
extends Map<K, V>,
Ordered<K> {
    public static final long serialVersionUID = 1L;

    public static <K, V> SortedMap<K, V> narrow(SortedMap<? extends K, ? extends V> sortedMap) {
        return sortedMap;
    }

    public <K2, V2> SortedMap<K2, V2> bimap(Comparator<? super K2> var1, Function<? super K, ? extends K2> var2, Function<? super V, ? extends V2> var3);

    public <K2, V2> SortedMap<K2, V2> flatMap(Comparator<? super K2> var1, BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> var2);

    public <K2, V2> SortedMap<K2, V2> map(Comparator<? super K2> var1, BiFunction<? super K, ? super V, Tuple2<K2, V2>> var2);

    @Override
    public <K2, V2> SortedMap<K2, V2> bimap(Function<? super K, ? extends K2> var1, Function<? super V, ? extends V2> var2);

    @Override
    public Tuple2<V, ? extends SortedMap<K, V>> computeIfAbsent(K var1, Function<? super K, ? extends V> var2);

    @Override
    public Tuple2<Option<V>, ? extends SortedMap<K, V>> computeIfPresent(K var1, BiFunction<? super K, ? super V, ? extends V> var2);

    @Override
    public SortedMap<K, V> distinct();

    @Override
    public SortedMap<K, V> distinctBy(Comparator<? super Tuple2<K, V>> var1);

    @Override
    public <U> SortedMap<K, V> distinctBy(Function<? super Tuple2<K, V>, ? extends U> var1);

    @Override
    public SortedMap<K, V> drop(int var1);

    @Override
    public SortedMap<K, V> dropRight(int var1);

    @Override
    public SortedMap<K, V> dropUntil(Predicate<? super Tuple2<K, V>> var1);

    @Override
    public SortedMap<K, V> dropWhile(Predicate<? super Tuple2<K, V>> var1);

    @Override
    public SortedMap<K, V> filter(Predicate<? super Tuple2<K, V>> var1);

    @Override
    public SortedMap<K, V> reject(Predicate<? super Tuple2<K, V>> var1);

    @Override
    public SortedMap<K, V> filter(BiPredicate<? super K, ? super V> var1);

    @Override
    public SortedMap<K, V> reject(BiPredicate<? super K, ? super V> var1);

    @Override
    public SortedMap<K, V> filterKeys(Predicate<? super K> var1);

    @Override
    public SortedMap<K, V> rejectKeys(Predicate<? super K> var1);

    @Override
    public SortedMap<K, V> filterValues(Predicate<? super V> var1);

    @Override
    public SortedMap<K, V> rejectValues(Predicate<? super V> var1);

    @Override
    @Deprecated
    public SortedMap<K, V> removeAll(BiPredicate<? super K, ? super V> var1);

    @Override
    @Deprecated
    public SortedMap<K, V> removeKeys(Predicate<? super K> var1);

    @Override
    @Deprecated
    public SortedMap<K, V> removeValues(Predicate<? super V> var1);

    @Override
    public <K2, V2> SortedMap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> var1);

    @Override
    public <C> Map<C, ? extends SortedMap<K, V>> groupBy(Function<? super Tuple2<K, V>, ? extends C> var1);

    @Override
    public Iterator<? extends SortedMap<K, V>> grouped(int var1);

    @Override
    public SortedMap<K, V> init();

    @Override
    public Option<? extends SortedMap<K, V>> initOption();

    @Override
    default public boolean isOrdered() {
        return true;
    }

    @Override
    public SortedSet<K> keySet();

    @Override
    default public Tuple2<K, V> last() {
        return (Tuple2)this.max().getOrElseThrow(() -> new NoSuchElementException("last on empty SortedMap"));
    }

    @Override
    public <K2, V2> SortedMap<K2, V2> map(BiFunction<? super K, ? super V, Tuple2<K2, V2>> var1);

    @Override
    public <K2> SortedMap<K2, V> mapKeys(Function<? super K, ? extends K2> var1);

    @Override
    public <K2> SortedMap<K2, V> mapKeys(Function<? super K, ? extends K2> var1, BiFunction<? super V, ? super V, ? extends V> var2);

    @Override
    public <V2> SortedMap<K, V2> mapValues(Function<? super V, ? extends V2> var1);

    @Override
    public SortedMap<K, V> merge(Map<? extends K, ? extends V> var1);

    @Override
    public <U extends V> SortedMap<K, V> merge(Map<? extends K, U> var1, BiFunction<? super V, ? super U, ? extends V> var2);

    @Override
    public SortedMap<K, V> orElse(Iterable<? extends Tuple2<K, V>> var1);

    @Override
    public SortedMap<K, V> orElse(Supplier<? extends Iterable<? extends Tuple2<K, V>>> var1);

    @Override
    public Tuple2<? extends SortedMap<K, V>, ? extends SortedMap<K, V>> partition(Predicate<? super Tuple2<K, V>> var1);

    @Override
    public SortedMap<K, V> peek(Consumer<? super Tuple2<K, V>> var1);

    @Override
    public SortedMap<K, V> put(K var1, V var2);

    @Override
    public SortedMap<K, V> put(Tuple2<? extends K, ? extends V> var1);

    @Override
    public <U extends V> SortedMap<K, V> put(K var1, U var2, BiFunction<? super V, ? super U, ? extends V> var3);

    @Override
    public <U extends V> SortedMap<K, V> put(Tuple2<? extends K, U> var1, BiFunction<? super V, ? super U, ? extends V> var2);

    @Override
    public SortedMap<K, V> remove(K var1);

    @Override
    public SortedMap<K, V> removeAll(Iterable<? extends K> var1);

    @Override
    public SortedMap<K, V> replace(K var1, V var2, V var3);

    @Override
    public SortedMap<K, V> replace(Tuple2<K, V> var1, Tuple2<K, V> var2);

    @Override
    public SortedMap<K, V> replaceValue(K var1, V var2);

    @Override
    public SortedMap<K, V> replaceAll(BiFunction<? super K, ? super V, ? extends V> var1);

    @Override
    public SortedMap<K, V> replaceAll(Tuple2<K, V> var1, Tuple2<K, V> var2);

    @Override
    public SortedMap<K, V> retainAll(Iterable<? extends Tuple2<K, V>> var1);

    @Override
    public SortedMap<K, V> scan(Tuple2<K, V> var1, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K, V>, ? extends Tuple2<K, V>> var2);

    @Override
    public Iterator<? extends SortedMap<K, V>> slideBy(Function<? super Tuple2<K, V>, ?> var1);

    @Override
    public Iterator<? extends SortedMap<K, V>> sliding(int var1);

    @Override
    public Iterator<? extends SortedMap<K, V>> sliding(int var1, int var2);

    @Override
    public Tuple2<? extends SortedMap<K, V>, ? extends SortedMap<K, V>> span(Predicate<? super Tuple2<K, V>> var1);

    @Override
    public SortedMap<K, V> tail();

    @Override
    public Option<? extends SortedMap<K, V>> tailOption();

    @Override
    public SortedMap<K, V> take(int var1);

    @Override
    public SortedMap<K, V> takeRight(int var1);

    @Override
    public SortedMap<K, V> takeUntil(Predicate<? super Tuple2<K, V>> var1);

    @Override
    public SortedMap<K, V> takeWhile(Predicate<? super Tuple2<K, V>> var1);

    @Override
    public java.util.SortedMap<K, V> toJavaMap();
}

