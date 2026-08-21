/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Collections;
import io.vavr.collection.Comparators;
import io.vavr.collection.Iterator;
import io.vavr.collection.Map;
import io.vavr.collection.Maps;
import io.vavr.collection.RedBlackTree;
import io.vavr.collection.Seq;
import io.vavr.collection.SortedMap;
import io.vavr.collection.SortedSet;
import io.vavr.collection.TreeSet;
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
import java.util.stream.Stream;

public final class TreeMap<K, V>
implements SortedMap<K, V>,
Serializable {
    private static final long serialVersionUID = 1L;
    private final RedBlackTree<Tuple2<K, V>> entries;

    private TreeMap(RedBlackTree<Tuple2<K, V>> entries) {
        this.entries = entries;
    }

    public static <K extends Comparable<? super K>, V> Collector<Tuple2<K, V>, ArrayList<Tuple2<K, V>>, TreeMap<K, V>> collector() {
        return TreeMap.createCollector(EntryComparator.natural());
    }

    public static <K, V> Collector<Tuple2<K, V>, ArrayList<Tuple2<K, V>>, TreeMap<K, V>> collector(Comparator<? super K> keyComparator) {
        return TreeMap.createCollector(EntryComparator.of(keyComparator));
    }

    public static <K extends Comparable<? super K>, V, T extends V> Collector<T, ArrayList<T>, TreeMap<K, V>> collector(Function<? super T, ? extends K> keyMapper) {
        Objects.requireNonNull(keyMapper, "key comparator is null");
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        return TreeMap.createCollector(EntryComparator.natural(), keyMapper, v -> v);
    }

    public static <K extends Comparable<? super K>, V, T> Collector<T, ArrayList<T>, TreeMap<K, V>> collector(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(keyMapper, "key comparator is null");
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return TreeMap.createCollector(EntryComparator.natural(), keyMapper, valueMapper);
    }

    public static <K, V, T extends V> Collector<T, ArrayList<T>, TreeMap<K, V>> collector(Comparator<? super K> keyComparator, Function<? super T, ? extends K> keyMapper) {
        Objects.requireNonNull(keyMapper, "key comparator is null");
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        return TreeMap.createCollector(EntryComparator.of(keyComparator), keyMapper, v -> v);
    }

    public static <K, V, T> Collector<T, ArrayList<T>, TreeMap<K, V>> collector(Comparator<? super K> keyComparator, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(keyMapper, "key comparator is null");
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return TreeMap.createCollector(EntryComparator.of(keyComparator), keyMapper, valueMapper);
    }

    public static <K extends Comparable<? super K>, V> TreeMap<K, V> empty() {
        return new TreeMap<K, V>(RedBlackTree.empty(EntryComparator.natural()));
    }

    public static <K, V> TreeMap<K, V> empty(Comparator<? super K> keyComparator) {
        return new TreeMap<K, V>(RedBlackTree.empty(EntryComparator.of(keyComparator)));
    }

    public static <K, V> TreeMap<K, V> narrow(TreeMap<? extends K, ? extends V> treeMap) {
        return treeMap;
    }

    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(Tuple2<? extends K, ? extends V> entry) {
        Objects.requireNonNull(entry, "entry is null");
        return TreeMap.createFromTuple(EntryComparator.natural(), entry);
    }

    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, Tuple2<? extends K, ? extends V> entry) {
        Objects.requireNonNull(entry, "entry is null");
        return TreeMap.createFromTuple(EntryComparator.of(keyComparator), entry);
    }

    public static <K extends Comparable<? super K>, V> TreeMap<K, V> ofAll(java.util.Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map is null");
        return TreeMap.createFromMap(EntryComparator.natural(), map);
    }

    public static <T, K extends Comparable<? super K>, V> TreeMap<K, V> ofAll(Stream<? extends T> stream, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return Maps.ofStream(TreeMap.empty(), stream, keyMapper, valueMapper);
    }

    public static <T, K, V> TreeMap<K, V> ofAll(Comparator<? super K> keyComparator, Stream<? extends T> stream, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return Maps.ofStream(TreeMap.empty(keyComparator), stream, keyMapper, valueMapper);
    }

    public static <T, K extends Comparable<? super K>, V> TreeMap<K, V> ofAll(Stream<? extends T> stream, Function<? super T, Tuple2<? extends K, ? extends V>> entryMapper) {
        return Maps.ofStream(TreeMap.empty(), stream, entryMapper);
    }

    public static <T, K, V> TreeMap<K, V> ofAll(Comparator<? super K> keyComparator, Stream<? extends T> stream, Function<? super T, Tuple2<? extends K, ? extends V>> entryMapper) {
        return Maps.ofStream(TreeMap.empty(keyComparator), stream, entryMapper);
    }

    public static <K, V> TreeMap<K, V> ofAll(Comparator<? super K> keyComparator, java.util.Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map is null");
        return TreeMap.createFromMap(EntryComparator.of(keyComparator), map);
    }

    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K key, V value) {
        return TreeMap.createFromPairs(EntryComparator.natural(), key, value);
    }

    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K k1, V v1, K k2, V v2) {
        return TreeMap.createFromPairs(EntryComparator.natural(), k1, v1, k2, v2);
    }

    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return TreeMap.createFromPairs(EntryComparator.natural(), k1, v1, k2, v2, k3, v3);
    }

    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return TreeMap.createFromPairs(EntryComparator.natural(), k1, v1, k2, v2, k3, v3, k4, v4);
    }

    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return TreeMap.createFromPairs(EntryComparator.natural(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }

    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        return TreeMap.createFromPairs(EntryComparator.natural(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
    }

    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        return TreeMap.createFromPairs(EntryComparator.natural(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
    }

    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        return TreeMap.createFromPairs(EntryComparator.natural(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
    }

    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        return TreeMap.createFromPairs(EntryComparator.natural(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
    }

    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        return TreeMap.createFromPairs(EntryComparator.natural(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
    }

    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K key, V value) {
        return TreeMap.createFromPairs(EntryComparator.of(keyComparator), key, value);
    }

    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K k1, V v1, K k2, V v2) {
        return TreeMap.createFromPairs(EntryComparator.of(keyComparator), k1, v1, k2, v2);
    }

    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K k1, V v1, K k2, V v2, K k3, V v3) {
        return TreeMap.createFromPairs(EntryComparator.of(keyComparator), k1, v1, k2, v2, k3, v3);
    }

    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return TreeMap.createFromPairs(EntryComparator.of(keyComparator), k1, v1, k2, v2, k3, v3, k4, v4);
    }

    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return TreeMap.createFromPairs(EntryComparator.of(keyComparator), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }

    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        return TreeMap.createFromPairs(EntryComparator.of(keyComparator), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
    }

    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        return TreeMap.createFromPairs(EntryComparator.of(keyComparator), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
    }

    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        return TreeMap.createFromPairs(EntryComparator.of(keyComparator), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
    }

    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        return TreeMap.createFromPairs(EntryComparator.of(keyComparator), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
    }

    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        return TreeMap.createFromPairs(EntryComparator.of(keyComparator), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
    }

    public static <K, V> TreeMap<K, V> tabulate(Comparator<? super K> keyComparator, int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V>> f) {
        Objects.requireNonNull(f, "f is null");
        return TreeMap.createTreeMap(EntryComparator.of(keyComparator), Collections.tabulate(n, f));
    }

    public static <K extends Comparable<? super K>, V> TreeMap<K, V> tabulate(int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V>> f) {
        Objects.requireNonNull(f, "f is null");
        return TreeMap.createTreeMap(EntryComparator.natural(), Collections.tabulate(n, f));
    }

    public static <K, V> TreeMap<K, V> fill(Comparator<? super K> keyComparator, int n, Supplier<? extends Tuple2<? extends K, ? extends V>> s) {
        Objects.requireNonNull(s, "s is null");
        return TreeMap.createTreeMap(EntryComparator.of(keyComparator), Collections.fill(n, s));
    }

    public static <K extends Comparable<? super K>, V> TreeMap<K, V> fill(int n, Supplier<? extends Tuple2<? extends K, ? extends V>> s) {
        Objects.requireNonNull(s, "s is null");
        return TreeMap.createTreeMap(EntryComparator.natural(), Collections.fill(n, s));
    }

    @SafeVarargs
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> ofEntries(Tuple2<? extends K, ? extends V> ... entries) {
        return TreeMap.createFromTuples(EntryComparator.natural(), entries);
    }

    @SafeVarargs
    public static <K, V> TreeMap<K, V> ofEntries(Comparator<? super K> keyComparator, Tuple2<? extends K, ? extends V> ... entries) {
        return TreeMap.createFromTuples(EntryComparator.of(keyComparator), entries);
    }

    @SafeVarargs
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> ofEntries(Map.Entry<? extends K, ? extends V> ... entries) {
        return TreeMap.createFromMapEntries(EntryComparator.natural(), entries);
    }

    @SafeVarargs
    public static <K, V> TreeMap<K, V> ofEntries(Comparator<? super K> keyComparator, Map.Entry<? extends K, ? extends V> ... entries) {
        return TreeMap.createFromMapEntries(EntryComparator.of(keyComparator), entries);
    }

    public static <K extends Comparable<? super K>, V> TreeMap<K, V> ofEntries(Iterable<? extends Tuple2<? extends K, ? extends V>> entries) {
        return TreeMap.createTreeMap(EntryComparator.natural(), entries);
    }

    public static <K, V> TreeMap<K, V> ofEntries(Comparator<? super K> keyComparator, Iterable<? extends Tuple2<? extends K, ? extends V>> entries) {
        return TreeMap.createTreeMap(EntryComparator.of(keyComparator), entries);
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> bimap(Function<? super K, ? extends K2> keyMapper, Function<? super V, ? extends V2> valueMapper) {
        return TreeMap.bimap(this, EntryComparator.natural(), keyMapper, valueMapper);
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> bimap(Comparator<? super K2> keyComparator, Function<? super K, ? extends K2> keyMapper, Function<? super V, ? extends V2> valueMapper) {
        return TreeMap.bimap(this, EntryComparator.of(keyComparator), keyMapper, valueMapper);
    }

    @Override
    public Tuple2<V, TreeMap<K, V>> computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return Maps.computeIfAbsent(this, key, mappingFunction);
    }

    @Override
    public Tuple2<Option<V>, TreeMap<K, V>> computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return Maps.computeIfPresent(this, key, remappingFunction);
    }

    @Override
    public boolean containsKey(K key) {
        return this.entries.contains(new Tuple2<K, Object>(key, null));
    }

    @Override
    public TreeMap<K, V> distinct() {
        return Maps.distinct(this);
    }

    @Override
    public TreeMap<K, V> distinctBy(Comparator<? super Tuple2<K, V>> comparator) {
        return Maps.distinctBy(this, this::createFromEntries, comparator);
    }

    @Override
    public <U> TreeMap<K, V> distinctBy(Function<? super Tuple2<K, V>, ? extends U> keyExtractor) {
        return Maps.distinctBy(this, this::createFromEntries, keyExtractor);
    }

    @Override
    public TreeMap<K, V> drop(int n) {
        return Maps.drop(this, this::createFromEntries, this::emptyInstance, n);
    }

    @Override
    public TreeMap<K, V> dropRight(int n) {
        return Maps.dropRight(this, this::createFromEntries, this::emptyInstance, n);
    }

    @Override
    public TreeMap<K, V> dropUntil(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.dropUntil(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> dropWhile(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.dropWhile(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> filter(BiPredicate<? super K, ? super V> predicate) {
        return Maps.filter(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> reject(BiPredicate<? super K, ? super V> predicate) {
        return Maps.reject(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> filter(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.filter(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> reject(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.reject(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> filterKeys(Predicate<? super K> predicate) {
        return Maps.filterKeys(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> rejectKeys(Predicate<? super K> predicate) {
        return Maps.rejectKeys(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> filterValues(Predicate<? super V> predicate) {
        return Maps.filterValues(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> rejectValues(Predicate<? super V> predicate) {
        return Maps.rejectValues(this, this::createFromEntries, predicate);
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        return TreeMap.flatMap(this, EntryComparator.natural(), mapper);
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> flatMap(Comparator<? super K2> keyComparator, BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        return TreeMap.flatMap(this, EntryComparator.of(keyComparator), mapper);
    }

    @Override
    public Option<V> get(K key) {
        Object ignored = null;
        return this.entries.find(new Tuple2<K, Object>(key, ignored)).map(Tuple2::_2);
    }

    @Override
    public V getOrElse(K key, V defaultValue) {
        return this.get(key).getOrElse(defaultValue);
    }

    @Override
    public <C> Map<C, TreeMap<K, V>> groupBy(Function<? super Tuple2<K, V>, ? extends C> classifier) {
        return Maps.groupBy(this, this::createFromEntries, classifier);
    }

    @Override
    public Iterator<TreeMap<K, V>> grouped(int size) {
        return Maps.grouped(this, this::createFromEntries, size);
    }

    @Override
    public Tuple2<K, V> head() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("head of empty TreeMap");
        }
        return this.entries.min().get();
    }

    @Override
    public Tuple2<K, V> last() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("last of empty TreeMap");
        }
        return this.entries.max().get();
    }

    @Override
    public TreeMap<K, V> init() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("init of empty TreeMap");
        }
        Tuple2<K, V> max = this.entries.max().get();
        return new TreeMap<K, V>(this.entries.delete(max));
    }

    @Override
    public Option<TreeMap<K, V>> initOption() {
        return Maps.initOption(this);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    @Override
    public boolean isLazy() {
        return false;
    }

    @Override
    public Iterator<Tuple2<K, V>> iterator() {
        return this.entries.iterator();
    }

    @Override
    public SortedSet<K> keySet() {
        return TreeSet.ofAll(this.comparator(), this.iterator().map(Tuple2::_1));
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> map(BiFunction<? super K, ? super V, Tuple2<K2, V2>> mapper) {
        return TreeMap.map(this, EntryComparator.natural(), mapper);
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> map(Comparator<? super K2> keyComparator, BiFunction<? super K, ? super V, Tuple2<K2, V2>> mapper) {
        Objects.requireNonNull(keyComparator, "keyComparator is null");
        return TreeMap.map(this, EntryComparator.of(keyComparator), mapper);
    }

    @Override
    public <K2> TreeMap<K2, V> mapKeys(Function<? super K, ? extends K2> keyMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        return this.map((T k, U v) -> Tuple.of(keyMapper.apply((Object)k), v));
    }

    @Override
    public <K2> TreeMap<K2, V> mapKeys(Function<? super K, ? extends K2> keyMapper, BiFunction<? super V, ? super V, ? extends V> valueMerge) {
        Comparator comparator = Comparators.naturalComparator();
        return Collections.mapKeys(this, TreeMap.empty(comparator), keyMapper, valueMerge);
    }

    @Override
    public <W> TreeMap<K, W> mapValues(Function<? super V, ? extends W> valueMapper) {
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return this.map(this.comparator(), (T k, U v) -> Tuple.of(k, valueMapper.apply((Object)v)));
    }

    @Override
    public TreeMap<K, V> merge(Map<? extends K, ? extends V> that) {
        return Maps.merge(this, this::createFromEntries, that);
    }

    @Override
    public <U extends V> TreeMap<K, V> merge(Map<? extends K, U> that, BiFunction<? super V, ? super U, ? extends V> collisionResolution) {
        return Maps.merge(this, this::createFromEntries, that, collisionResolution);
    }

    @Override
    public TreeMap<K, V> orElse(Iterable<? extends Tuple2<K, V>> other) {
        return this.isEmpty() ? TreeMap.ofEntries(this.comparator(), other) : this;
    }

    @Override
    public TreeMap<K, V> orElse(Supplier<? extends Iterable<? extends Tuple2<K, V>>> supplier) {
        return this.isEmpty() ? TreeMap.ofEntries(this.comparator(), supplier.get()) : this;
    }

    @Override
    public Tuple2<TreeMap<K, V>, TreeMap<K, V>> partition(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.partition(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> peek(Consumer<? super Tuple2<K, V>> action) {
        return Maps.peek(this, action);
    }

    @Override
    public <U extends V> TreeMap<K, V> put(K key, U value, BiFunction<? super V, ? super U, ? extends V> merge) {
        return Maps.put(this, key, value, merge);
    }

    @Override
    public TreeMap<K, V> put(K key, V value) {
        return new TreeMap<K, V>(this.entries.insert(new Tuple2<K, V>(key, value)));
    }

    @Override
    public TreeMap<K, V> put(Tuple2<? extends K, ? extends V> entry) {
        return Maps.put(this, entry);
    }

    @Override
    public <U extends V> TreeMap<K, V> put(Tuple2<? extends K, U> entry, BiFunction<? super V, ? super U, ? extends V> merge) {
        return Maps.put(this, entry, merge);
    }

    @Override
    public TreeMap<K, V> remove(K key) {
        Object ignored = null;
        Tuple2<K, Object> entry = new Tuple2<K, Object>(key, ignored);
        if (this.entries.contains(entry)) {
            return new TreeMap<K, V>(this.entries.delete(entry));
        }
        return this;
    }

    @Override
    @Deprecated
    public TreeMap<K, V> removeAll(BiPredicate<? super K, ? super V> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.reject((BiPredicate)predicate);
    }

    @Override
    public TreeMap<K, V> removeAll(Iterable<? extends K> keys2) {
        Object ignored = null;
        RedBlackTree<Tuple2<K, Object>> removed = this.entries;
        for (K key : keys2) {
            Tuple2<K, Object> entry = new Tuple2<K, Object>(key, ignored);
            if (!removed.contains(entry)) continue;
            removed = removed.delete(entry);
        }
        if (removed.size() == this.entries.size()) {
            return this;
        }
        return new TreeMap<K, V>(removed);
    }

    @Override
    @Deprecated
    public TreeMap<K, V> removeKeys(Predicate<? super K> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.rejectKeys((Predicate)predicate);
    }

    @Override
    @Deprecated
    public TreeMap<K, V> removeValues(Predicate<? super V> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.rejectValues((Predicate)predicate);
    }

    @Override
    public TreeMap<K, V> replace(Tuple2<K, V> currentElement, Tuple2<K, V> newElement) {
        return Maps.replace(this, currentElement, newElement);
    }

    @Override
    public TreeMap<K, V> replaceAll(Tuple2<K, V> currentElement, Tuple2<K, V> newElement) {
        return Maps.replaceAll(this, currentElement, newElement);
    }

    @Override
    public TreeMap<K, V> replaceValue(K key, V value) {
        return Maps.replaceValue(this, key, value);
    }

    @Override
    public TreeMap<K, V> replace(K key, V oldValue, V newValue) {
        return Maps.replace(this, key, oldValue, newValue);
    }

    @Override
    public TreeMap<K, V> replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        return Maps.replaceAll(this, function);
    }

    @Override
    public TreeMap<K, V> retainAll(Iterable<? extends Tuple2<K, V>> elements) {
        Objects.requireNonNull(elements, "elements is null");
        RedBlackTree<Tuple2<K, V>> tree = RedBlackTree.empty(this.entries.comparator());
        for (Tuple2<K, V> entry : elements) {
            if (!this.contains(entry)) continue;
            tree = tree.insert(entry);
        }
        return new TreeMap<K, V>(tree);
    }

    @Override
    public TreeMap<K, V> scan(Tuple2<K, V> zero, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K, V>, ? extends Tuple2<K, V>> operation) {
        return Maps.scan(this, zero, operation, this::createFromEntries);
    }

    @Override
    public int size() {
        return this.entries.size();
    }

    @Override
    public Iterator<TreeMap<K, V>> slideBy(Function<? super Tuple2<K, V>, ?> classifier) {
        return Maps.slideBy(this, this::createFromEntries, classifier);
    }

    @Override
    public Iterator<TreeMap<K, V>> sliding(int size) {
        return Maps.sliding(this, this::createFromEntries, size);
    }

    @Override
    public Iterator<TreeMap<K, V>> sliding(int size, int step) {
        return Maps.sliding(this, this::createFromEntries, size, step);
    }

    @Override
    public Tuple2<TreeMap<K, V>, TreeMap<K, V>> span(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.span(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> tail() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("tail of empty TreeMap");
        }
        Tuple2<K, V> min = this.entries.min().get();
        return new TreeMap<K, V>(this.entries.delete(min));
    }

    @Override
    public Option<TreeMap<K, V>> tailOption() {
        return Maps.tailOption(this);
    }

    @Override
    public TreeMap<K, V> take(int n) {
        return Maps.take(this, this::createFromEntries, n);
    }

    @Override
    public TreeMap<K, V> takeRight(int n) {
        return Maps.takeRight(this, this::createFromEntries, n);
    }

    @Override
    public TreeMap<K, V> takeUntil(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.takeUntil(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> takeWhile(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.takeWhile(this, this::createFromEntries, predicate);
    }

    @Override
    public java.util.TreeMap<K, V> toJavaMap() {
        return this.toJavaMap(() -> new java.util.TreeMap(this.comparator()), t -> t);
    }

    @Override
    public Seq<V> values() {
        return this.map(Tuple2::_2);
    }

    @Override
    public boolean equals(Object o) {
        return Collections.equals(this, o);
    }

    @Override
    public int hashCode() {
        return Collections.hashUnordered(this);
    }

    @Override
    public String stringPrefix() {
        return "TreeMap";
    }

    @Override
    public String toString() {
        return this.mkString(this.stringPrefix() + "(", ", ", ")");
    }

    private static <K, K2, V, V2> TreeMap<K2, V2> bimap(TreeMap<K, V> map, EntryComparator<K2, V2> entryComparator, Function<? super K, ? extends K2> keyMapper, Function<? super V, ? extends V2> valueMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return TreeMap.createTreeMap(entryComparator, map.entries, entry -> entry.map(keyMapper, valueMapper));
    }

    private static <K, V, K2, V2> TreeMap<K2, V2> flatMap(TreeMap<K, V> map, EntryComparator<K2, V2> entryComparator, BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return TreeMap.createTreeMap(entryComparator, map.entries.iterator().flatMap((T entry) -> (Iterable)mapper.apply((Object)entry._1, (Object)entry._2)));
    }

    private static <K, K2, V, V2> TreeMap<K2, V2> map(TreeMap<K, V> map, EntryComparator<K2, V2> entryComparator, BiFunction<? super K, ? super V, Tuple2<K2, V2>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return TreeMap.createTreeMap(entryComparator, map.entries, entry -> entry.map(mapper));
    }

    private static <K, V> Collector<Tuple2<K, V>, ArrayList<Tuple2<K, V>>, TreeMap<K, V>> createCollector(EntryComparator<K, V> entryComparator) {
        Supplier<ArrayList> supplier = ArrayList::new;
        BiConsumer<ArrayList, Tuple2> accumulator = ArrayList::add;
        BinaryOperator combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        Function<ArrayList, TreeMap> finisher = list -> TreeMap.createTreeMap(entryComparator, list);
        return Collector.of(supplier, accumulator, combiner, finisher, new Collector.Characteristics[0]);
    }

    private static <K, V, T> Collector<T, ArrayList<T>, TreeMap<K, V>> createCollector(EntryComparator<K, V> entryComparator, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        Supplier<ArrayList> supplier = ArrayList::new;
        BiConsumer<ArrayList, Object> accumulator = ArrayList::add;
        BinaryOperator combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        Function<ArrayList, TreeMap> finisher = arr -> TreeMap.createTreeMap(entryComparator, Iterator.ofAll(arr).map((T t) -> Tuple.of(keyMapper.apply(t), valueMapper.apply(t))));
        return Collector.of(supplier, accumulator, combiner, finisher, new Collector.Characteristics[0]);
    }

    private static <K, V> TreeMap<K, V> createTreeMap(EntryComparator<K, V> entryComparator, Iterable<? extends Tuple2<? extends K, ? extends V>> entries) {
        Objects.requireNonNull(entries, "entries is null");
        RedBlackTree<Tuple2<K, V>> tree = RedBlackTree.empty(entryComparator);
        for (Tuple2<K, V> tuple2 : entries) {
            tree = tree.insert(tuple2);
        }
        return new TreeMap<K, V>(tree);
    }

    private static <K, K2, V, V2> TreeMap<K2, V2> createTreeMap(EntryComparator<K2, V2> entryComparator, Iterable<Tuple2<K, V>> entries, Function<Tuple2<K, V>, Tuple2<K2, V2>> entryMapper) {
        RedBlackTree<Tuple2<Object, Object>> tree = RedBlackTree.empty(entryComparator);
        for (Tuple2<K, V> entry : entries) {
            tree = tree.insert(entryMapper.apply(entry));
        }
        return new TreeMap<K, V>(tree);
    }

    private static <K, V> TreeMap<K, V> createFromMap(EntryComparator<K, V> entryComparator, java.util.Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map is null");
        RedBlackTree<Tuple2<K, V>> tree = RedBlackTree.empty(entryComparator);
        for (Map.Entry<K, V> entry : map.entrySet()) {
            tree = tree.insert(Tuple.of(entry.getKey(), entry.getValue()));
        }
        return new TreeMap<K, V>(tree);
    }

    private static <K, V> TreeMap<K, V> createFromTuple(EntryComparator<K, V> entryComparator, Tuple2<? extends K, ? extends V> entry) {
        Objects.requireNonNull(entry, "entry is null");
        return new TreeMap<K, V>(RedBlackTree.of(entryComparator, entry));
    }

    private static <K, V> TreeMap<K, V> createFromTuples(EntryComparator<K, V> entryComparator, Tuple2<? extends K, ? extends V> ... entries) {
        Objects.requireNonNull(entries, "entries is null");
        RedBlackTree<Tuple2<Object, Object>> tree = RedBlackTree.empty(entryComparator);
        for (Tuple2<? extends K, ? extends V> entry : entries) {
            tree = tree.insert(entry);
        }
        return new TreeMap<K, V>(tree);
    }

    @SafeVarargs
    private static <K, V> TreeMap<K, V> createFromMapEntries(EntryComparator<K, V> entryComparator, Map.Entry<? extends K, ? extends V> ... entries) {
        Objects.requireNonNull(entries, "entries is null");
        RedBlackTree<Tuple2<K, V>> tree = RedBlackTree.empty(entryComparator);
        for (Map.Entry<K, V> entry : entries) {
            K key = entry.getKey();
            V value = entry.getValue();
            tree = tree.insert(Tuple.of(key, value));
        }
        return new TreeMap<K, V>(tree);
    }

    private static <K, V> TreeMap<K, V> createFromPairs(EntryComparator<K, V> entryComparator, Object ... pairs) {
        RedBlackTree<Tuple2<Object, Object>> tree = RedBlackTree.empty(entryComparator);
        for (int i = 0; i < pairs.length; i += 2) {
            Object key = pairs[i];
            Object value = pairs[i + 1];
            tree = tree.insert(Tuple.of(key, value));
        }
        return new TreeMap<K, V>(tree);
    }

    private TreeMap<K, V> createFromEntries(Iterable<Tuple2<K, V>> tuples) {
        return TreeMap.createTreeMap((EntryComparator)this.entries.comparator(), tuples);
    }

    private TreeMap<K, V> emptyInstance() {
        return this.isEmpty() ? this : new TreeMap<K, V>(this.entries.emptyInstance());
    }

    @Override
    public Comparator<K> comparator() {
        return ((EntryComparator)this.entries.comparator()).keyComparator();
    }

    private static interface EntryComparator<K, V>
    extends Comparator<Tuple2<K, V>>,
    Serializable {
        public static final long serialVersionUID = 1L;

        public static <K, V> EntryComparator<K, V> of(Comparator<? super K> keyComparator) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            return new Specific(keyComparator);
        }

        public static <K, V> EntryComparator<K, V> natural() {
            return Natural.instance();
        }

        public Comparator<K> keyComparator();

        public static final class Natural<K, V>
        implements EntryComparator<K, V> {
            private static final long serialVersionUID = 1L;
            private static final Natural<?, ?> INSTANCE = new Natural();

            private Natural() {
            }

            public static <K, V> Natural<K, V> instance() {
                return INSTANCE;
            }

            @Override
            public int compare(Tuple2<K, V> e1, Tuple2<K, V> e2) {
                Object key1 = e1._1;
                Object key2 = e2._1;
                return ((Comparable)key1).compareTo(key2);
            }

            @Override
            public Comparator<K> keyComparator() {
                return Comparators.naturalComparator();
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Natural;
            }

            public int hashCode() {
                return 1;
            }

            private Object readResolve() {
                return INSTANCE;
            }
        }

        public static final class Specific<K, V>
        implements EntryComparator<K, V> {
            private static final long serialVersionUID = 1L;
            private final Comparator<K> keyComparator;

            Specific(Comparator<? super K> keyComparator) {
                this.keyComparator = keyComparator;
            }

            @Override
            public int compare(Tuple2<K, V> e1, Tuple2<K, V> e2) {
                return this.keyComparator.compare(e1._1, e2._1);
            }

            @Override
            public Comparator<K> keyComparator() {
                return this.keyComparator;
            }
        }
    }
}

