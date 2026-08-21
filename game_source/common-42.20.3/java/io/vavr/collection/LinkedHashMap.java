/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Collections;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Iterator;
import io.vavr.collection.LinearSeq;
import io.vavr.collection.LinkedHashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Maps;
import io.vavr.collection.Queue;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.Traversable;
import io.vavr.control.Option;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public final class LinkedHashMap<K, V>
implements Map<K, V>,
Serializable {
    private static final long serialVersionUID = 1L;
    private static final LinkedHashMap<?, ?> EMPTY = new LinkedHashMap(Queue.empty(), HashMap.empty());
    private final Queue<Tuple2<K, V>> list;
    private final HashMap<K, V> map;

    private LinkedHashMap(Queue<Tuple2<K, V>> list, HashMap<K, V> map) {
        this.list = list;
        this.map = map;
    }

    public static <K, V> Collector<Tuple2<K, V>, ArrayList<Tuple2<K, V>>, LinkedHashMap<K, V>> collector() {
        Supplier<ArrayList> supplier = ArrayList::new;
        BiConsumer<ArrayList, Tuple2> accumulator = ArrayList::add;
        BinaryOperator combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        Function<ArrayList, LinkedHashMap> finisher = LinkedHashMap::ofEntries;
        return Collector.of(supplier, accumulator, combiner, finisher, new Collector.Characteristics[0]);
    }

    public static <K, V, T extends V> Collector<T, ArrayList<T>, LinkedHashMap<K, V>> collector(Function<? super T, ? extends K> keyMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        return LinkedHashMap.collector(keyMapper, v -> v);
    }

    public static <K, V, T> Collector<T, ArrayList<T>, LinkedHashMap<K, V>> collector(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        Supplier<ArrayList> supplier = ArrayList::new;
        BiConsumer<ArrayList, Object> accumulator = ArrayList::add;
        BinaryOperator combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        Function<ArrayList, LinkedHashMap> finisher = arr -> LinkedHashMap.ofEntries(Iterator.ofAll(arr).map((T t) -> Tuple.of(keyMapper.apply(t), valueMapper.apply(t))));
        return Collector.of(supplier, accumulator, combiner, finisher, new Collector.Characteristics[0]);
    }

    public static <K, V> LinkedHashMap<K, V> empty() {
        return EMPTY;
    }

    public static <K, V> LinkedHashMap<K, V> narrow(LinkedHashMap<? extends K, ? extends V> linkedHashMap) {
        return linkedHashMap;
    }

    public static <K, V> LinkedHashMap<K, V> of(Tuple2<? extends K, ? extends V> entry) {
        HashMap<? extends K, ? extends V> map = HashMap.of(entry);
        Queue<Tuple2<K, V>> list = Queue.of(entry);
        return LinkedHashMap.wrap(list, map);
    }

    public static <K, V> LinkedHashMap<K, V> ofAll(java.util.Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map is null");
        Map<K, V> result = LinkedHashMap.empty();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            result = result.put((Object)entry.getKey(), (Object)entry.getValue());
        }
        return result;
    }

    public static <T, K, V> LinkedHashMap<K, V> ofAll(Stream<? extends T> stream, Function<? super T, Tuple2<? extends K, ? extends V>> entryMapper) {
        return Maps.ofStream(LinkedHashMap.empty(), stream, entryMapper);
    }

    public static <T, K, V> LinkedHashMap<K, V> ofAll(Stream<? extends T> stream, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return Maps.ofStream(LinkedHashMap.empty(), stream, keyMapper, valueMapper);
    }

    public static <K, V> LinkedHashMap<K, V> of(K key, V value) {
        HashMap<K, V> map = HashMap.of(key, value);
        Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(key, value));
        return LinkedHashMap.wrap(list, map);
    }

    public static <K, V> LinkedHashMap<K, V> of(K k1, V v1, K k2, V v2) {
        HashMap<K, V> map = HashMap.of(k1, v1, k2, v2);
        Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(k1, v1), Tuple.of(k2, v2));
        return LinkedHashMap.wrapNonUnique(list, map);
    }

    public static <K, V> LinkedHashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        HashMap<K, V> map = HashMap.of(k1, v1, k2, v2, k3, v3);
        Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(k1, v1), Tuple.of(k2, v2), Tuple.of(k3, v3));
        return LinkedHashMap.wrapNonUnique(list, map);
    }

    public static <K, V> LinkedHashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        HashMap<K, V> map = HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4);
        Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(k1, v1), Tuple.of(k2, v2), Tuple.of(k3, v3), Tuple.of(k4, v4));
        return LinkedHashMap.wrapNonUnique(list, map);
    }

    public static <K, V> LinkedHashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        HashMap<K, V> map = HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
        Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(k1, v1), Tuple.of(k2, v2), Tuple.of(k3, v3), Tuple.of(k4, v4), Tuple.of(k5, v5));
        return LinkedHashMap.wrapNonUnique(list, map);
    }

    public static <K, V> LinkedHashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        HashMap<K, V> map = HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
        Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(k1, v1), Tuple.of(k2, v2), Tuple.of(k3, v3), Tuple.of(k4, v4), Tuple.of(k5, v5), Tuple.of(k6, v6));
        return LinkedHashMap.wrapNonUnique(list, map);
    }

    public static <K, V> LinkedHashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        HashMap<K, V> map = HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
        Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(k1, v1), Tuple.of(k2, v2), Tuple.of(k3, v3), Tuple.of(k4, v4), Tuple.of(k5, v5), Tuple.of(k6, v6), Tuple.of(k7, v7));
        return LinkedHashMap.wrapNonUnique(list, map);
    }

    public static <K, V> LinkedHashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        HashMap<K, V> map = HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
        Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(k1, v1), Tuple.of(k2, v2), Tuple.of(k3, v3), Tuple.of(k4, v4), Tuple.of(k5, v5), Tuple.of(k6, v6), Tuple.of(k7, v7), Tuple.of(k8, v8));
        return LinkedHashMap.wrapNonUnique(list, map);
    }

    public static <K, V> LinkedHashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        HashMap<K, V> map = HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
        Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(k1, v1), Tuple.of(k2, v2), Tuple.of(k3, v3), Tuple.of(k4, v4), Tuple.of(k5, v5), Tuple.of(k6, v6), Tuple.of(k7, v7), Tuple.of(k8, v8), Tuple.of(k9, v9));
        return LinkedHashMap.wrapNonUnique(list, map);
    }

    public static <K, V> LinkedHashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        HashMap<K, V> map = HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
        Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(k1, v1), Tuple.of(k2, v2), Tuple.of(k3, v3), Tuple.of(k4, v4), Tuple.of(k5, v5), Tuple.of(k6, v6), Tuple.of(k7, v7), Tuple.of(k8, v8), Tuple.of(k9, v9), Tuple.of(k10, v10));
        return LinkedHashMap.wrapNonUnique(list, map);
    }

    public static <K, V> LinkedHashMap<K, V> tabulate(int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V>> f) {
        Objects.requireNonNull(f, "f is null");
        return LinkedHashMap.ofEntries(Collections.tabulate(n, f));
    }

    public static <K, V> LinkedHashMap<K, V> fill(int n, Supplier<? extends Tuple2<? extends K, ? extends V>> s) {
        Objects.requireNonNull(s, "s is null");
        return LinkedHashMap.ofEntries(Collections.fill(n, s));
    }

    public static <K, V> LinkedHashMap<K, V> ofEntries(Map.Entry<? extends K, ? extends V> ... entries) {
        Map map = HashMap.empty();
        LinearSeq<Tuple2<Object, Object>> list = Queue.empty();
        for (Map.Entry<K, V> entry : entries) {
            Tuple2<K, V> tuple = Tuple.of(entry.getKey(), entry.getValue());
            map = map.put(tuple);
            list = list.append(tuple);
        }
        return LinkedHashMap.wrapNonUnique(list, map);
    }

    public static <K, V> LinkedHashMap<K, V> ofEntries(Tuple2<? extends K, ? extends V> ... entries) {
        HashMap<? extends K, ? extends V> map = HashMap.ofEntries(entries);
        Queue<Tuple2<K, V>> list = Queue.of(entries);
        return LinkedHashMap.wrapNonUnique(list, map);
    }

    public static <K, V> LinkedHashMap<K, V> ofEntries(Iterable<? extends Tuple2<? extends K, ? extends V>> entries) {
        Objects.requireNonNull(entries, "entries is null");
        if (entries instanceof LinkedHashMap) {
            return (LinkedHashMap)entries;
        }
        Map map = HashMap.empty();
        LinearSeq<Tuple2<Object, Object>> list = Queue.empty();
        for (Tuple2<K, V> tuple2 : entries) {
            map = map.put(tuple2);
            list = list.append(tuple2);
        }
        return LinkedHashMap.wrapNonUnique(list, map);
    }

    @Override
    public <K2, V2> LinkedHashMap<K2, V2> bimap(Function<? super K, ? extends K2> keyMapper, Function<? super V, ? extends V2> valueMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        Traversable entries = this.iterator().map((T entry) -> Tuple.of(keyMapper.apply((Object)entry._1), valueMapper.apply((Object)entry._2)));
        return LinkedHashMap.ofEntries(entries);
    }

    @Override
    public Tuple2<V, LinkedHashMap<K, V>> computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return Maps.computeIfAbsent(this, key, mappingFunction);
    }

    @Override
    public Tuple2<Option<V>, LinkedHashMap<K, V>> computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return Maps.computeIfPresent(this, key, remappingFunction);
    }

    @Override
    public boolean containsKey(K key) {
        return this.map.containsKey(key);
    }

    @Override
    public LinkedHashMap<K, V> distinct() {
        return Maps.distinct(this);
    }

    @Override
    public LinkedHashMap<K, V> distinctBy(Comparator<? super Tuple2<K, V>> comparator) {
        return Maps.distinctBy(this, this::createFromEntries, comparator);
    }

    @Override
    public <U> LinkedHashMap<K, V> distinctBy(Function<? super Tuple2<K, V>, ? extends U> keyExtractor) {
        return Maps.distinctBy(this, this::createFromEntries, keyExtractor);
    }

    @Override
    public LinkedHashMap<K, V> drop(int n) {
        return Maps.drop(this, this::createFromEntries, LinkedHashMap::empty, n);
    }

    @Override
    public LinkedHashMap<K, V> dropRight(int n) {
        return Maps.dropRight(this, this::createFromEntries, LinkedHashMap::empty, n);
    }

    @Override
    public LinkedHashMap<K, V> dropUntil(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.dropUntil(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> dropWhile(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.dropWhile(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> filter(BiPredicate<? super K, ? super V> predicate) {
        return Maps.filter(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> reject(BiPredicate<? super K, ? super V> predicate) {
        return Maps.reject(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> filter(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.filter(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> reject(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.reject(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> filterKeys(Predicate<? super K> predicate) {
        return Maps.filterKeys(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> rejectKeys(Predicate<? super K> predicate) {
        return Maps.rejectKeys(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> filterValues(Predicate<? super V> predicate) {
        return Maps.filterValues(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> rejectValues(Predicate<? super V> predicate) {
        return Maps.rejectValues(this, this::createFromEntries, predicate);
    }

    @Override
    public <K2, V2> LinkedHashMap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.foldLeft(LinkedHashMap.empty(), (acc, entry) -> {
            for (Tuple2 mappedEntry : (Iterable)mapper.apply((Object)entry._1, (Object)entry._2)) {
                acc = acc.put(mappedEntry);
            }
            return acc;
        });
    }

    @Override
    public Option<V> get(K key) {
        return this.map.get(key);
    }

    @Override
    public V getOrElse(K key, V defaultValue) {
        return this.map.getOrElse(key, defaultValue);
    }

    @Override
    public <C> Map<C, LinkedHashMap<K, V>> groupBy(Function<? super Tuple2<K, V>, ? extends C> classifier) {
        return Maps.groupBy(this, this::createFromEntries, classifier);
    }

    @Override
    public Iterator<LinkedHashMap<K, V>> grouped(int size) {
        return Maps.grouped(this, this::createFromEntries, size);
    }

    @Override
    public Tuple2<K, V> head() {
        return this.list.head();
    }

    @Override
    public LinkedHashMap<K, V> init() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("init of empty LinkedHashMap");
        }
        return LinkedHashMap.ofEntries(this.list.init());
    }

    @Override
    public Option<LinkedHashMap<K, V>> initOption() {
        return Maps.initOption(this);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean isLazy() {
        return false;
    }

    @Override
    public boolean isSequential() {
        return true;
    }

    @Override
    public Iterator<Tuple2<K, V>> iterator() {
        return this.list.iterator();
    }

    @Override
    public Set<K> keySet() {
        return LinkedHashSet.wrap(this);
    }

    @Override
    public Tuple2<K, V> last() {
        return this.list.last();
    }

    @Override
    public <K2, V2> LinkedHashMap<K2, V2> map(BiFunction<? super K, ? super V, Tuple2<K2, V2>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.foldLeft(LinkedHashMap.empty(), (acc, entry) -> acc.put(entry.map(mapper)));
    }

    @Override
    public <K2> LinkedHashMap<K2, V> mapKeys(Function<? super K, ? extends K2> keyMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        return this.map((T k, U v) -> Tuple.of(keyMapper.apply((Object)k), v));
    }

    @Override
    public <K2> LinkedHashMap<K2, V> mapKeys(Function<? super K, ? extends K2> keyMapper, BiFunction<? super V, ? super V, ? extends V> valueMerge) {
        return Collections.mapKeys(this, LinkedHashMap.empty(), keyMapper, valueMerge);
    }

    @Override
    public <W> LinkedHashMap<K, W> mapValues(Function<? super V, ? extends W> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.map((T k, U v) -> Tuple.of(k, mapper.apply((Object)v)));
    }

    @Override
    public LinkedHashMap<K, V> merge(Map<? extends K, ? extends V> that) {
        return Maps.merge(this, this::createFromEntries, that);
    }

    @Override
    public <U extends V> LinkedHashMap<K, V> merge(Map<? extends K, U> that, BiFunction<? super V, ? super U, ? extends V> collisionResolution) {
        return Maps.merge(this, this::createFromEntries, that, collisionResolution);
    }

    @Override
    public LinkedHashMap<K, V> orElse(Iterable<? extends Tuple2<K, V>> other) {
        return this.isEmpty() ? LinkedHashMap.ofEntries(other) : this;
    }

    @Override
    public LinkedHashMap<K, V> orElse(Supplier<? extends Iterable<? extends Tuple2<K, V>>> supplier) {
        return this.isEmpty() ? LinkedHashMap.ofEntries(supplier.get()) : this;
    }

    @Override
    public Tuple2<LinkedHashMap<K, V>, LinkedHashMap<K, V>> partition(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.partition(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> peek(Consumer<? super Tuple2<K, V>> action) {
        return Maps.peek(this, action);
    }

    @Override
    public <U extends V> LinkedHashMap<K, V> put(K key, U value, BiFunction<? super V, ? super U, ? extends V> merge) {
        return Maps.put(this, key, value, merge);
    }

    @Override
    public LinkedHashMap<K, V> put(K key, V value) {
        Option<V> currentEntry = this.get(key);
        Traversable newList = currentEntry.isDefined() ? this.list.replace((Object)Tuple.of(key, currentEntry.get()), (Object)Tuple.of(key, value)) : this.list.append((Object)Tuple.of(key, value));
        Map newMap = this.map.put((Object)key, (Object)value);
        return LinkedHashMap.wrap(newList, newMap);
    }

    @Override
    public LinkedHashMap<K, V> put(Tuple2<? extends K, ? extends V> entry) {
        return Maps.put(this, entry);
    }

    @Override
    public <U extends V> LinkedHashMap<K, V> put(Tuple2<? extends K, U> entry, BiFunction<? super V, ? super U, ? extends V> merge) {
        return Maps.put(this, entry, merge);
    }

    @Override
    public LinkedHashMap<K, V> remove(K key) {
        if (this.containsKey(key)) {
            LinearSeq newList = this.list.removeFirst(t -> Objects.equals(t._1, key));
            Map newMap = this.map.remove((Object)key);
            return LinkedHashMap.wrap(newList, newMap);
        }
        return this;
    }

    @Override
    @Deprecated
    public LinkedHashMap<K, V> removeAll(BiPredicate<? super K, ? super V> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.reject((BiPredicate)predicate);
    }

    @Override
    public LinkedHashMap<K, V> removeAll(Iterable<? extends K> keys2) {
        Objects.requireNonNull(keys2, "keys is null");
        HashSet toRemove = HashSet.ofAll(keys2);
        Traversable newList = this.list.filter((T t) -> !toRemove.contains((Object)t._1));
        Map newMap = this.map.filter(t -> !toRemove.contains((Object)t._1));
        return newList.size() == this.size() ? this : LinkedHashMap.wrap(newList, newMap);
    }

    @Override
    @Deprecated
    public LinkedHashMap<K, V> removeKeys(Predicate<? super K> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.rejectKeys((Predicate)predicate);
    }

    @Override
    @Deprecated
    public LinkedHashMap<K, V> removeValues(Predicate<? super V> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.rejectValues((Predicate)predicate);
    }

    @Override
    public LinkedHashMap<K, V> replace(Tuple2<K, V> currentElement, Tuple2<K, V> newElement) {
        Objects.requireNonNull(currentElement, "currentElement is null");
        Objects.requireNonNull(newElement, "newElement is null");
        if (!Objects.equals(currentElement, newElement) && this.contains(currentElement)) {
            Option<V> value;
            Traversable<Tuple2<Object, Object>> newList = this.list;
            Map<Object, V> newMap = this.map;
            Object currentKey = currentElement._1;
            Object newKey = newElement._1;
            if (!Objects.equals(currentKey, newKey) && (value = newMap.get(newKey)).isDefined()) {
                newList = ((Queue)newList).remove(Tuple.of(newKey, value.get()));
            }
            newList = ((Queue)newList).replace(currentElement, newElement);
            newMap = ((HashMap)newMap.remove(currentKey)).put(newElement);
            return LinkedHashMap.wrap(newList, newMap);
        }
        return this;
    }

    @Override
    public LinkedHashMap<K, V> replaceAll(Tuple2<K, V> currentElement, Tuple2<K, V> newElement) {
        return Maps.replaceAll(this, currentElement, newElement);
    }

    @Override
    public LinkedHashMap<K, V> replaceValue(K key, V value) {
        return Maps.replaceValue(this, key, value);
    }

    @Override
    public LinkedHashMap<K, V> replace(K key, V oldValue, V newValue) {
        return Maps.replace(this, key, oldValue, newValue);
    }

    @Override
    public LinkedHashMap<K, V> replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        return Maps.replaceAll(this, function);
    }

    @Override
    public LinkedHashMap<K, V> retainAll(Iterable<? extends Tuple2<K, V>> elements) {
        Objects.requireNonNull(elements, "elements is null");
        Map<K, V> result = LinkedHashMap.empty();
        for (Tuple2<K, V> entry : elements) {
            if (!this.contains(entry)) continue;
            result = result.put(entry._1, entry._2);
        }
        return result;
    }

    @Override
    public LinkedHashMap<K, V> scan(Tuple2<K, V> zero, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K, V>, ? extends Tuple2<K, V>> operation) {
        return Maps.scan(this, zero, operation, this::createFromEntries);
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public Iterator<LinkedHashMap<K, V>> slideBy(Function<? super Tuple2<K, V>, ?> classifier) {
        return Maps.slideBy(this, this::createFromEntries, classifier);
    }

    @Override
    public Iterator<LinkedHashMap<K, V>> sliding(int size) {
        return Maps.sliding(this, this::createFromEntries, size);
    }

    @Override
    public Iterator<LinkedHashMap<K, V>> sliding(int size, int step) {
        return Maps.sliding(this, this::createFromEntries, size, step);
    }

    @Override
    public Tuple2<LinkedHashMap<K, V>, LinkedHashMap<K, V>> span(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.span(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> tail() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("tail of empty LinkedHashMap");
        }
        return LinkedHashMap.ofEntries(this.list.tail());
    }

    @Override
    public Option<LinkedHashMap<K, V>> tailOption() {
        return Maps.tailOption(this);
    }

    @Override
    public LinkedHashMap<K, V> take(int n) {
        return Maps.take(this, this::createFromEntries, n);
    }

    @Override
    public LinkedHashMap<K, V> takeRight(int n) {
        return Maps.takeRight(this, this::createFromEntries, n);
    }

    @Override
    public LinkedHashMap<K, V> takeUntil(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.takeUntil(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> takeWhile(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.takeWhile(this, this::createFromEntries, predicate);
    }

    @Override
    public java.util.LinkedHashMap<K, V> toJavaMap() {
        return this.toJavaMap(java.util.LinkedHashMap::new, t -> t);
    }

    @Override
    public Seq<V> values() {
        return this.map((T t) -> t._2);
    }

    @Override
    public boolean equals(Object o) {
        return Collections.equals(this, o);
    }

    @Override
    public int hashCode() {
        return Collections.hashUnordered(this);
    }

    private Object readResolve() {
        return this.isEmpty() ? EMPTY : this;
    }

    @Override
    public String stringPrefix() {
        return "LinkedHashMap";
    }

    @Override
    public String toString() {
        return this.mkString(this.stringPrefix() + "(", ", ", ")");
    }

    private static <K, V> LinkedHashMap<K, V> wrap(Queue<Tuple2<K, V>> list, HashMap<K, V> map) {
        return list.isEmpty() ? LinkedHashMap.empty() : new LinkedHashMap<K, V>(list, map);
    }

    private static <K, V> LinkedHashMap<K, V> wrapNonUnique(Queue<Tuple2<K, V>> list, HashMap<K, V> map) {
        return list.isEmpty() ? LinkedHashMap.empty() : new LinkedHashMap<K, V>(((Queue)((Queue)list.reverse()).distinctBy(Tuple2::_1)).reverse().toQueue(), map);
    }

    private LinkedHashMap<K, V> createFromEntries(Iterable<Tuple2<K, V>> tuples) {
        return LinkedHashMap.ofEntries(tuples);
    }
}

