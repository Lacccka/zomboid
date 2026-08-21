/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Collections;
import io.vavr.collection.HashArrayMappedTrie;
import io.vavr.collection.HashSet;
import io.vavr.collection.Iterator;
import io.vavr.collection.Map;
import io.vavr.collection.Maps;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Traversable;
import io.vavr.control.Option;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
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

public final class HashMap<K, V>
implements Map<K, V>,
Serializable {
    private static final long serialVersionUID = 1L;
    private static final HashMap<?, ?> EMPTY = new HashMap(HashArrayMappedTrie.empty());
    private final HashArrayMappedTrie<K, V> trie;

    private HashMap(HashArrayMappedTrie<K, V> trie) {
        this.trie = trie;
    }

    public static <K, V> Collector<Tuple2<K, V>, ArrayList<Tuple2<K, V>>, HashMap<K, V>> collector() {
        Supplier<ArrayList> supplier = ArrayList::new;
        BiConsumer<ArrayList, Tuple2> accumulator = ArrayList::add;
        BinaryOperator combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        Function<ArrayList, HashMap> finisher = HashMap::ofEntries;
        return Collector.of(supplier, accumulator, combiner, finisher, new Collector.Characteristics[0]);
    }

    public static <K, V, T extends V> Collector<T, ArrayList<T>, HashMap<K, V>> collector(Function<? super T, ? extends K> keyMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        return HashMap.collector(keyMapper, v -> v);
    }

    public static <K, V, T> Collector<T, ArrayList<T>, HashMap<K, V>> collector(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        Supplier<ArrayList> supplier = ArrayList::new;
        BiConsumer<ArrayList, Object> accumulator = ArrayList::add;
        BinaryOperator combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        Function<ArrayList, HashMap> finisher = arr -> HashMap.ofEntries(Iterator.ofAll(arr).map((T t) -> Tuple.of(keyMapper.apply(t), valueMapper.apply(t))));
        return Collector.of(supplier, accumulator, combiner, finisher, new Collector.Characteristics[0]);
    }

    public static <K, V> HashMap<K, V> empty() {
        return EMPTY;
    }

    public static <K, V> HashMap<K, V> narrow(HashMap<? extends K, ? extends V> hashMap) {
        return hashMap;
    }

    public static <K, V> HashMap<K, V> of(Tuple2<? extends K, ? extends V> entry) {
        return new HashMap(HashArrayMappedTrie.empty().put(entry._1, entry._2));
    }

    public static <K, V> HashMap<K, V> ofAll(java.util.Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map is null");
        HashArrayMappedTrie<K, V> tree = HashArrayMappedTrie.empty();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            tree = tree.put(entry.getKey(), entry.getValue());
        }
        return HashMap.wrap(tree);
    }

    public static <T, K, V> HashMap<K, V> ofAll(java.util.stream.Stream<? extends T> stream, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return Maps.ofStream(HashMap.empty(), stream, keyMapper, valueMapper);
    }

    public static <T, K, V> HashMap<K, V> ofAll(java.util.stream.Stream<? extends T> stream, Function<? super T, Tuple2<? extends K, ? extends V>> entryMapper) {
        return Maps.ofStream(HashMap.empty(), stream, entryMapper);
    }

    public static <K, V> HashMap<K, V> of(K key, V value) {
        return new HashMap(HashArrayMappedTrie.empty().put(key, value));
    }

    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2) {
        return HashMap.of(k1, v1).put((Object)k2, (Object)v2);
    }

    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return HashMap.of(k1, v1, k2, v2).put((Object)k3, (Object)v3);
    }

    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return HashMap.of(k1, v1, k2, v2, k3, v3).put((Object)k4, (Object)v4);
    }

    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4).put((Object)k5, (Object)v5);
    }

    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        return HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5).put((Object)k6, (Object)v6);
    }

    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        return HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6).put((Object)k7, (Object)v7);
    }

    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        return HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7).put((Object)k8, (Object)v8);
    }

    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        return HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8).put((Object)k9, (Object)v9);
    }

    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        return HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9).put((Object)k10, (Object)v10);
    }

    public static <K, V> HashMap<K, V> tabulate(int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V>> f) {
        Objects.requireNonNull(f, "f is null");
        return HashMap.ofEntries(Collections.tabulate(n, f));
    }

    public static <K, V> HashMap<K, V> fill(int n, Supplier<? extends Tuple2<? extends K, ? extends V>> s) {
        Objects.requireNonNull(s, "s is null");
        return HashMap.ofEntries(Collections.fill(n, s));
    }

    @SafeVarargs
    public static <K, V> HashMap<K, V> ofEntries(Map.Entry<? extends K, ? extends V> ... entries) {
        Objects.requireNonNull(entries, "entries is null");
        HashArrayMappedTrie<K, V> trie = HashArrayMappedTrie.empty();
        for (Map.Entry<K, V> entry : entries) {
            trie = trie.put(entry.getKey(), entry.getValue());
        }
        return HashMap.wrap(trie);
    }

    @SafeVarargs
    public static <K, V> HashMap<K, V> ofEntries(Tuple2<? extends K, ? extends V> ... entries) {
        Objects.requireNonNull(entries, "entries is null");
        HashArrayMappedTrie trie = HashArrayMappedTrie.empty();
        for (Tuple2<? extends K, ? extends V> entry : entries) {
            trie = trie.put(entry._1, entry._2);
        }
        return HashMap.wrap(trie);
    }

    public static <K, V> HashMap<K, V> ofEntries(Iterable<? extends Tuple2<? extends K, ? extends V>> entries) {
        Objects.requireNonNull(entries, "entries is null");
        if (entries instanceof HashMap) {
            return (HashMap)entries;
        }
        HashArrayMappedTrie trie = HashArrayMappedTrie.empty();
        for (Tuple2<K, V> tuple2 : entries) {
            trie = trie.put(tuple2._1, tuple2._2);
        }
        return trie.isEmpty() ? HashMap.empty() : HashMap.wrap(trie);
    }

    @Override
    public <K2, V2> HashMap<K2, V2> bimap(Function<? super K, ? extends K2> keyMapper, Function<? super V, ? extends V2> valueMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        Traversable entries = this.iterator().map((T entry) -> Tuple.of(keyMapper.apply((Object)entry._1), valueMapper.apply((Object)entry._2)));
        return HashMap.ofEntries(entries);
    }

    @Override
    public Tuple2<V, HashMap<K, V>> computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return Maps.computeIfAbsent(this, key, mappingFunction);
    }

    @Override
    public Tuple2<Option<V>, HashMap<K, V>> computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return Maps.computeIfPresent(this, key, remappingFunction);
    }

    @Override
    public boolean containsKey(K key) {
        return this.trie.containsKey(key);
    }

    @Override
    public HashMap<K, V> distinct() {
        return Maps.distinct(this);
    }

    @Override
    public HashMap<K, V> distinctBy(Comparator<? super Tuple2<K, V>> comparator) {
        return Maps.distinctBy(this, this::createFromEntries, comparator);
    }

    @Override
    public <U> HashMap<K, V> distinctBy(Function<? super Tuple2<K, V>, ? extends U> keyExtractor) {
        return Maps.distinctBy(this, this::createFromEntries, keyExtractor);
    }

    @Override
    public HashMap<K, V> drop(int n) {
        return Maps.drop(this, this::createFromEntries, HashMap::empty, n);
    }

    @Override
    public HashMap<K, V> dropRight(int n) {
        return Maps.dropRight(this, this::createFromEntries, HashMap::empty, n);
    }

    @Override
    public HashMap<K, V> dropUntil(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.dropUntil(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> dropWhile(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.dropWhile(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> filter(BiPredicate<? super K, ? super V> predicate) {
        return Maps.filter(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> reject(BiPredicate<? super K, ? super V> predicate) {
        return Maps.reject(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> filter(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.filter(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> reject(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.reject(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> filterKeys(Predicate<? super K> predicate) {
        return Maps.filterKeys(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> rejectKeys(Predicate<? super K> predicate) {
        return Maps.rejectKeys(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> filterValues(Predicate<? super V> predicate) {
        return Maps.filterValues(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> rejectValues(Predicate<? super V> predicate) {
        return Maps.rejectValues(this, this::createFromEntries, predicate);
    }

    @Override
    public <K2, V2> HashMap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.foldLeft(HashMap.empty(), (acc, entry) -> {
            for (Tuple2 mappedEntry : (Iterable)mapper.apply((Object)entry._1, (Object)entry._2)) {
                acc = acc.put(mappedEntry);
            }
            return acc;
        });
    }

    @Override
    public Option<V> get(K key) {
        return this.trie.get(key);
    }

    @Override
    public V getOrElse(K key, V defaultValue) {
        return this.trie.getOrElse(key, defaultValue);
    }

    @Override
    public <C> Map<C, HashMap<K, V>> groupBy(Function<? super Tuple2<K, V>, ? extends C> classifier) {
        return Maps.groupBy(this, this::createFromEntries, classifier);
    }

    @Override
    public Iterator<HashMap<K, V>> grouped(int size) {
        return Maps.grouped(this, this::createFromEntries, size);
    }

    @Override
    public Tuple2<K, V> head() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("head of empty HashMap");
        }
        return (Tuple2)this.iterator().next();
    }

    @Override
    public HashMap<K, V> init() {
        if (this.trie.isEmpty()) {
            throw new UnsupportedOperationException("init of empty HashMap");
        }
        return this.remove(((Tuple2)this.last())._1);
    }

    @Override
    public Option<HashMap<K, V>> initOption() {
        return Maps.initOption(this);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.trie.isEmpty();
    }

    @Override
    public boolean isLazy() {
        return false;
    }

    @Override
    public Iterator<Tuple2<K, V>> iterator() {
        return this.trie.iterator();
    }

    @Override
    public Set<K> keySet() {
        return HashSet.ofAll(this.iterator().map(Tuple2::_1));
    }

    @Override
    public Iterator<K> keysIterator() {
        return this.trie.keysIterator();
    }

    @Override
    public Tuple2<K, V> last() {
        return (Tuple2)Collections.last(this);
    }

    @Override
    public <K2, V2> HashMap<K2, V2> map(BiFunction<? super K, ? super V, Tuple2<K2, V2>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.foldLeft(HashMap.empty(), (acc, entry) -> acc.put(entry.map(mapper)));
    }

    @Override
    public <K2> HashMap<K2, V> mapKeys(Function<? super K, ? extends K2> keyMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        return this.map((T k, U v) -> Tuple.of(keyMapper.apply((Object)k), v));
    }

    @Override
    public <K2> HashMap<K2, V> mapKeys(Function<? super K, ? extends K2> keyMapper, BiFunction<? super V, ? super V, ? extends V> valueMerge) {
        return Collections.mapKeys(this, HashMap.empty(), keyMapper, valueMerge);
    }

    @Override
    public <V2> HashMap<K, V2> mapValues(Function<? super V, ? extends V2> valueMapper) {
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return this.map((T k, U v) -> Tuple.of(k, valueMapper.apply((Object)v)));
    }

    @Override
    public HashMap<K, V> merge(Map<? extends K, ? extends V> that) {
        return Maps.merge(this, this::createFromEntries, that);
    }

    @Override
    public <U extends V> HashMap<K, V> merge(Map<? extends K, U> that, BiFunction<? super V, ? super U, ? extends V> collisionResolution) {
        return Maps.merge(this, this::createFromEntries, that, collisionResolution);
    }

    @Override
    public HashMap<K, V> orElse(Iterable<? extends Tuple2<K, V>> other) {
        return this.isEmpty() ? HashMap.ofEntries(other) : this;
    }

    @Override
    public HashMap<K, V> orElse(Supplier<? extends Iterable<? extends Tuple2<K, V>>> supplier) {
        return this.isEmpty() ? HashMap.ofEntries(supplier.get()) : this;
    }

    @Override
    public Tuple2<HashMap<K, V>, HashMap<K, V>> partition(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.partition(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> peek(Consumer<? super Tuple2<K, V>> action) {
        return Maps.peek(this, action);
    }

    @Override
    public <U extends V> HashMap<K, V> put(K key, U value, BiFunction<? super V, ? super U, ? extends V> merge) {
        return Maps.put(this, key, value, merge);
    }

    @Override
    public HashMap<K, V> put(K key, V value) {
        return new HashMap<K, V>(this.trie.put(key, value));
    }

    @Override
    public HashMap<K, V> put(Tuple2<? extends K, ? extends V> entry) {
        return Maps.put(this, entry);
    }

    @Override
    public <U extends V> HashMap<K, V> put(Tuple2<? extends K, U> entry, BiFunction<? super V, ? super U, ? extends V> merge) {
        return Maps.put(this, entry, merge);
    }

    @Override
    public HashMap<K, V> remove(K key) {
        HashArrayMappedTrie<K, V> result = this.trie.remove(key);
        return result.size() == this.trie.size() ? this : HashMap.wrap(result);
    }

    @Override
    @Deprecated
    public HashMap<K, V> removeAll(BiPredicate<? super K, ? super V> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.reject((BiPredicate)predicate);
    }

    @Override
    public HashMap<K, V> removeAll(Iterable<? extends K> keys2) {
        Objects.requireNonNull(keys2, "keys is null");
        HashArrayMappedTrie<K, V> result = this.trie;
        for (K key : keys2) {
            result = result.remove(key);
        }
        if (result.isEmpty()) {
            return HashMap.empty();
        }
        if (result.size() == this.trie.size()) {
            return this;
        }
        return HashMap.wrap(result);
    }

    @Override
    @Deprecated
    public HashMap<K, V> removeKeys(Predicate<? super K> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.rejectKeys((Predicate)predicate);
    }

    @Override
    @Deprecated
    public HashMap<K, V> removeValues(Predicate<? super V> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.rejectValues((Predicate)predicate);
    }

    @Override
    public HashMap<K, V> replace(Tuple2<K, V> currentElement, Tuple2<K, V> newElement) {
        return Maps.replace(this, currentElement, newElement);
    }

    @Override
    public HashMap<K, V> replaceAll(Tuple2<K, V> currentElement, Tuple2<K, V> newElement) {
        return Maps.replaceAll(this, currentElement, newElement);
    }

    @Override
    public HashMap<K, V> replaceValue(K key, V value) {
        return Maps.replaceValue(this, key, value);
    }

    @Override
    public HashMap<K, V> replace(K key, V oldValue, V newValue) {
        return Maps.replace(this, key, oldValue, newValue);
    }

    @Override
    public HashMap<K, V> replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        return Maps.replaceAll(this, function);
    }

    @Override
    public HashMap<K, V> retainAll(Iterable<? extends Tuple2<K, V>> elements) {
        Objects.requireNonNull(elements, "elements is null");
        HashArrayMappedTrie tree = HashArrayMappedTrie.empty();
        for (Tuple2<K, V> entry : elements) {
            if (!this.contains(entry)) continue;
            tree = tree.put(entry._1, entry._2);
        }
        return HashMap.wrap(tree);
    }

    @Override
    public HashMap<K, V> scan(Tuple2<K, V> zero, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K, V>, ? extends Tuple2<K, V>> operation) {
        return Maps.scan(this, zero, operation, this::createFromEntries);
    }

    @Override
    public int size() {
        return this.trie.size();
    }

    @Override
    public Iterator<HashMap<K, V>> slideBy(Function<? super Tuple2<K, V>, ?> classifier) {
        return Maps.slideBy(this, this::createFromEntries, classifier);
    }

    @Override
    public Iterator<HashMap<K, V>> sliding(int size) {
        return Maps.sliding(this, this::createFromEntries, size);
    }

    @Override
    public Iterator<HashMap<K, V>> sliding(int size, int step) {
        return Maps.sliding(this, this::createFromEntries, size, step);
    }

    @Override
    public Tuple2<HashMap<K, V>, HashMap<K, V>> span(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.span(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> tail() {
        if (this.trie.isEmpty()) {
            throw new UnsupportedOperationException("tail of empty HashMap");
        }
        return this.remove(((Tuple2)this.head())._1);
    }

    @Override
    public Option<HashMap<K, V>> tailOption() {
        return Maps.tailOption(this);
    }

    @Override
    public HashMap<K, V> take(int n) {
        return Maps.take(this, this::createFromEntries, n);
    }

    @Override
    public HashMap<K, V> takeRight(int n) {
        return Maps.takeRight(this, this::createFromEntries, n);
    }

    @Override
    public HashMap<K, V> takeUntil(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.takeUntil(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> takeWhile(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.takeWhile(this, this::createFromEntries, predicate);
    }

    @Override
    public java.util.HashMap<K, V> toJavaMap() {
        return this.toJavaMap(java.util.HashMap::new, t -> t);
    }

    @Override
    public Stream<V> values() {
        return this.trie.valuesIterator().toStream();
    }

    @Override
    public Iterator<V> valuesIterator() {
        return this.trie.valuesIterator();
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
        return "HashMap";
    }

    @Override
    public String toString() {
        return this.mkString(this.stringPrefix() + "(", ", ", ")");
    }

    private static <K, V> HashMap<K, V> wrap(HashArrayMappedTrie<K, V> trie) {
        return trie.isEmpty() ? HashMap.empty() : new HashMap<K, V>(trie);
    }

    private HashMap<K, V> createFromEntries(Iterable<Tuple2<K, V>> tuples) {
        return HashMap.ofEntries(tuples);
    }
}

