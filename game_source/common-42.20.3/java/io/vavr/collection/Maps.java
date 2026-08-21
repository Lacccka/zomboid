/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.API;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Collections;
import io.vavr.collection.Iterator;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;
import io.vavr.collection.Traversable;
import io.vavr.control.Option;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class Maps {
    private Maps() {
    }

    static <K, V, M extends Map<K, V>> Tuple2<V, M> computeIfAbsent(M map, K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction, "mappingFunction is null");
        Option<V> value = map.get(key);
        if (value.isDefined()) {
            return Tuple.of(value.get(), map);
        }
        V newValue = mappingFunction.apply(key);
        Map<K, V> newMap = map.put(key, newValue);
        return Tuple.of(newValue, newMap);
    }

    static <K, V, M extends Map<K, V>> Tuple2<Option<V>, M> computeIfPresent(M map, K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Option<V> value = map.get(key);
        if (value.isDefined()) {
            V newValue = remappingFunction.apply(key, value.get());
            Map<K, V> newMap = map.put(key, newValue);
            return Tuple.of(Option.of(newValue), newMap);
        }
        return Tuple.of(Option.none(), map);
    }

    static <K, V, M extends Map<K, V>> M distinct(M map) {
        return map;
    }

    static <K, V, M extends Map<K, V>> M distinctBy(M map, OfEntries<K, V, M> ofEntries, Comparator<? super Tuple2<K, V>> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return (M)((Map)ofEntries.apply(map.iterator().distinctBy(comparator)));
    }

    static <K, V, U, M extends Map<K, V>> M distinctBy(M map, OfEntries<K, V, M> ofEntries, Function<? super Tuple2<K, V>, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        return (M)((Map)ofEntries.apply(map.iterator().distinctBy(keyExtractor)));
    }

    static <K, V, M extends Map<K, V>> M drop(M map, OfEntries<K, V, M> ofEntries, Supplier<M> emptySupplier, int n) {
        if (n <= 0) {
            return map;
        }
        if (n >= map.size()) {
            return (M)((Map)emptySupplier.get());
        }
        return (M)((Map)ofEntries.apply(map.iterator().drop(n)));
    }

    static <K, V, M extends Map<K, V>> M dropRight(M map, OfEntries<K, V, M> ofEntries, Supplier<M> emptySupplier, int n) {
        if (n <= 0) {
            return map;
        }
        if (n >= map.size()) {
            return (M)((Map)emptySupplier.get());
        }
        return (M)((Map)ofEntries.apply(map.iterator().dropRight(n)));
    }

    static <K, V, M extends Map<K, V>> M dropUntil(M map, OfEntries<K, V, M> ofEntries, Predicate<? super Tuple2<K, V>> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Maps.dropWhile(map, ofEntries, predicate.negate());
    }

    static <K, V, M extends Map<K, V>> M dropWhile(M map, OfEntries<K, V, M> ofEntries, Predicate<? super Tuple2<K, V>> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return (M)((Map)ofEntries.apply(map.iterator().dropWhile(predicate)));
    }

    static <K, V, M extends Map<K, V>> M filter(M map, OfEntries<K, V, M> ofEntries, BiPredicate<? super K, ? super V> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Maps.filter(map, ofEntries, (? super Tuple2<K, V> t) -> predicate.test((Object)t._1, (Object)t._2));
    }

    static <K, V, M extends Map<K, V>> M filter(M map, OfEntries<K, V, M> ofEntries, Predicate<? super Tuple2<K, V>> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return (M)((Map)ofEntries.apply(map.iterator().filter(predicate)));
    }

    static <K, V, M extends Map<K, V>> M filterKeys(M map, OfEntries<K, V, M> ofEntries, Predicate<? super K> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Maps.filter(map, ofEntries, (? super Tuple2<K, V> t) -> predicate.test((Object)t._1));
    }

    static <K, V, M extends Map<K, V>> M filterValues(M map, OfEntries<K, V, M> ofEntries, Predicate<? super V> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Maps.filter(map, ofEntries, (? super Tuple2<K, V> t) -> predicate.test((Object)t._2));
    }

    static <K, V, C, M extends Map<K, V>> Map<C, M> groupBy(M map, OfEntries<K, V, M> ofEntries, Function<? super Tuple2<K, V>, ? extends C> classifier) {
        return Collections.groupBy(map, classifier, ofEntries);
    }

    static <K, V, M extends Map<K, V>> Iterator<M> grouped(M map, OfEntries<K, V, M> ofEntries, int size) {
        return Maps.sliding(map, ofEntries, size, size);
    }

    static <K, V, M extends Map<K, V>> Option<M> initOption(M map) {
        return map.isEmpty() ? Option.none() : Option.some(map.init());
    }

    static <K, V, M extends Map<K, V>> M merge(M map, OfEntries<K, V, M> ofEntries, Map<? extends K, ? extends V> that) {
        Objects.requireNonNull(that, "that is null");
        if (map.isEmpty()) {
            return (M)((Map)ofEntries.apply(Map.narrow(that)));
        }
        if (that.isEmpty()) {
            return map;
        }
        return (M)that.foldLeft(map, (result, entry) -> !result.containsKey(entry._1) ? Maps.put(result, entry) : result);
    }

    static <K, V, U extends V, M extends Map<K, V>> M merge(M map, OfEntries<K, V, M> ofEntries, Map<? extends K, U> that, BiFunction<? super V, ? super U, ? extends V> collisionResolution) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(collisionResolution, "collisionResolution is null");
        if (map.isEmpty()) {
            return (M)((Map)ofEntries.apply(Map.narrow(that)));
        }
        if (that.isEmpty()) {
            return map;
        }
        return (M)that.foldLeft(map, (result, entry) -> {
            Object key = entry._1;
            Object value = entry._2;
            Object newValue = result.get(key).map(v -> collisionResolution.apply((Object)v, (Object)value)).getOrElse(value);
            return result.put(key, newValue);
        });
    }

    static <T, K, V, M extends Map<K, V>> M ofStream(M map, java.util.stream.Stream<? extends T> stream, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(stream, "stream is null");
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return (M)Stream.ofAll(stream).foldLeft(map, (m, el) -> m.put(keyMapper.apply(el), valueMapper.apply(el)));
    }

    static <T, K, V, M extends Map<K, V>> M ofStream(M map, java.util.stream.Stream<? extends T> stream, Function<? super T, Tuple2<? extends K, ? extends V>> entryMapper) {
        Objects.requireNonNull(stream, "stream is null");
        Objects.requireNonNull(entryMapper, "entryMapper is null");
        return (M)Stream.ofAll(stream).foldLeft(map, (m, el) -> m.put((Tuple2)entryMapper.apply(el)));
    }

    static <K, V, M extends Map<K, V>> Tuple2<M, M> partition(M map, OfEntries<K, V, M> ofEntries, Predicate<? super Tuple2<K, V>> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        ArrayList<Tuple2> left = new ArrayList<Tuple2>();
        ArrayList right = new ArrayList();
        for (Tuple2 entry : map) {
            (predicate.test(entry) ? left : right).add(entry);
        }
        return Tuple.of(ofEntries.apply(left), ofEntries.apply(right));
    }

    static <K, V, M extends Map<K, V>> M peek(M map, Consumer<? super Tuple2<K, V>> action) {
        Objects.requireNonNull(action, "action is null");
        if (!map.isEmpty()) {
            action.accept((Tuple2<K, V>)map.head());
        }
        return map;
    }

    static <K, V, U extends V, M extends Map<K, V>> M put(M map, K key, U value, BiFunction<? super V, ? super U, ? extends V> merge) {
        Objects.requireNonNull(merge, "the merge function is null");
        Option<V> currentValue = map.get(key);
        if (currentValue.isEmpty()) {
            return (M)map.put(key, value);
        }
        return (M)map.put(key, merge.apply(currentValue.get(), value));
    }

    static <K, V, M extends Map<K, V>> M put(M map, Tuple2<? extends K, ? extends V> entry) {
        Objects.requireNonNull(entry, "entry is null");
        return (M)map.put(entry._1, entry._2);
    }

    static <K, V, U extends V, M extends Map<K, V>> M put(M map, Tuple2<? extends K, U> entry, BiFunction<? super V, ? super U, ? extends V> merge) {
        Objects.requireNonNull(merge, "the merge function is null");
        Option currentValue = map.get(entry._1);
        if (currentValue.isEmpty()) {
            return Maps.put(map, entry);
        }
        return Maps.put(map, entry.map2(value -> merge.apply((Object)currentValue.get(), (Object)value)));
    }

    static <K, V, M extends Map<K, V>> M reject(M map, OfEntries<K, V, M> ofEntries, Predicate<? super Tuple2<K, V>> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Maps.filter(map, ofEntries, predicate.negate());
    }

    static <K, V, M extends Map<K, V>> M reject(M map, OfEntries<K, V, M> ofEntries, BiPredicate<? super K, ? super V> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Maps.filter(map, ofEntries, predicate.negate());
    }

    static <K, V, M extends Map<K, V>> M rejectKeys(M map, OfEntries<K, V, M> ofEntries, Predicate<? super K> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Maps.filterKeys(map, ofEntries, predicate.negate());
    }

    static <K, V, M extends Map<K, V>> M rejectValues(M map, OfEntries<K, V, M> ofEntries, Predicate<? super V> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Maps.filterValues(map, ofEntries, predicate.negate());
    }

    static <K, V, M extends Map<K, V>> M replace(M map, K key, V oldValue, V newValue) {
        return (M)(map.contains(API.Tuple(key, oldValue)) ? map.put(key, newValue) : map);
    }

    static <K, V, M extends Map<K, V>> M replace(M map, Tuple2<K, V> currentElement, Tuple2<K, V> newElement) {
        Objects.requireNonNull(currentElement, "currentElement is null");
        Objects.requireNonNull(newElement, "newElement is null");
        return (M)(map.containsKey(currentElement._1) ? map.remove(currentElement._1).put(newElement) : map);
    }

    static <K, V, M extends Map<K, V>> M replaceAll(M map, BiFunction<? super K, ? super V, ? extends V> function) {
        return (M)map.map((k, v) -> API.Tuple(k, function.apply((Object)k, (Object)v)));
    }

    static <K, V, M extends Map<K, V>> M replaceAll(M map, Tuple2<K, V> currentElement, Tuple2<K, V> newElement) {
        return Maps.replace(map, currentElement, newElement);
    }

    static <K, V, M extends Map<K, V>> M replaceValue(M map, K key, V value) {
        return (M)(map.containsKey(key) ? map.put(key, value) : map);
    }

    static <K, V, M extends Map<K, V>> M scan(M map, Tuple2<K, V> zero, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K, V>, ? extends Tuple2<K, V>> operation, Function<Iterator<Tuple2<K, V>>, Traversable<Tuple2<K, V>>> finisher) {
        return (M)((Map)Collections.scanLeft(map, zero, operation, finisher));
    }

    static <K, V, M extends Map<K, V>> Iterator<M> slideBy(M map, OfEntries<K, V, M> ofEntries, Function<? super Tuple2<K, V>, ?> classifier) {
        return map.iterator().slideBy(classifier).map(ofEntries);
    }

    static <K, V, M extends Map<K, V>> Iterator<M> sliding(M map, OfEntries<K, V, M> ofEntries, int size) {
        return Maps.sliding(map, ofEntries, size, 1);
    }

    static <K, V, M extends Map<K, V>> Iterator<M> sliding(M map, OfEntries<K, V, M> ofEntries, int size, int step) {
        return map.iterator().sliding(size, step).map(ofEntries);
    }

    static <K, V, M extends Map<K, V>> Tuple2<M, M> span(M map, OfEntries<K, V, M> ofEntries, Predicate<? super Tuple2<K, V>> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        Tuple2<Iterator<? super Tuple2<K, V>>, Iterator<? super Tuple2<K, V>>> t = map.iterator().span(predicate);
        return Tuple.of(ofEntries.apply(t._1), ofEntries.apply(t._2));
    }

    static <K, V, M extends Map<K, V>> Option<M> tailOption(M map) {
        return map.isEmpty() ? Option.none() : Option.some(map.tail());
    }

    static <K, V, M extends Map<K, V>> M take(M map, OfEntries<K, V, M> ofEntries, int n) {
        if (n >= map.size()) {
            return map;
        }
        return (M)((Map)ofEntries.apply(map.iterator().take(n)));
    }

    static <K, V, M extends Map<K, V>> M takeRight(M map, OfEntries<K, V, M> ofEntries, int n) {
        if (n >= map.size()) {
            return map;
        }
        return (M)((Map)ofEntries.apply(map.iterator().takeRight(n)));
    }

    static <K, V, M extends Map<K, V>> M takeUntil(M map, OfEntries<K, V, M> ofEntries, Predicate<? super Tuple2<K, V>> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Maps.takeWhile(map, ofEntries, predicate.negate());
    }

    static <K, V, M extends Map<K, V>> M takeWhile(M map, OfEntries<K, V, M> ofEntries, Predicate<? super Tuple2<K, V>> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        Map taken = (Map)ofEntries.apply(map.iterator().takeWhile(predicate));
        return (M)(taken.size() == map.size() ? map : taken);
    }

    @FunctionalInterface
    static interface OfEntries<K, V, M extends Map<K, V>>
    extends Function<Iterable<Tuple2<K, V>>, M> {
    }
}

