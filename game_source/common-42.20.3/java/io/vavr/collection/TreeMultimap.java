/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Tuple2;
import io.vavr.collection.AbstractMultimap;
import io.vavr.collection.Collections;
import io.vavr.collection.Comparators;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Multimap;
import io.vavr.collection.Multimaps;
import io.vavr.collection.SortedMap;
import io.vavr.collection.SortedMultimap;
import io.vavr.collection.SortedSet;
import io.vavr.collection.Traversable;
import io.vavr.collection.TreeMap;
import io.vavr.collection.TreeSet;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public final class TreeMultimap<K, V>
extends AbstractMultimap<K, V, TreeMultimap<K, V>>
implements Serializable,
SortedMultimap<K, V> {
    private static final long serialVersionUID = 1L;

    public static <V> Builder<V> withSeq() {
        return new Builder(Multimap.ContainerType.SEQ, List::empty);
    }

    public static <V> Builder<V> withSet() {
        return new Builder(Multimap.ContainerType.SET, HashSet::empty);
    }

    public static <V extends Comparable<?>> Builder<V> withSortedSet() {
        return new Builder(Multimap.ContainerType.SORTED_SET, TreeSet::empty);
    }

    public static <V> Builder<V> withSortedSet(Comparator<? super V> comparator) {
        return new Builder(Multimap.ContainerType.SORTED_SET, () -> TreeSet.empty(comparator));
    }

    public static <K, V> TreeMultimap<K, V> narrow(TreeMultimap<? extends K, ? extends V> map) {
        return map;
    }

    private TreeMultimap(Map<K, Traversable<V>> back, Multimap.ContainerType containerType, AbstractMultimap.SerializableSupplier<Traversable<?>> emptyContainer) {
        super(back, containerType, emptyContainer);
    }

    @Override
    protected <K2, V2> Map<K2, V2> emptyMapSupplier() {
        return TreeMap.empty(Comparators.naturalComparator());
    }

    @Override
    protected <K2, V2> TreeMultimap<K2, V2> emptyInstance() {
        return new TreeMultimap<K2, V>(this.emptyMapSupplier(), this.getContainerType(), this.emptyContainer);
    }

    @Override
    protected <K2, V2> TreeMultimap<K2, V2> createFromMap(Map<K2, Traversable<V2>> back) {
        return new TreeMultimap<K2, V2>(back, this.getContainerType(), this.emptyContainer);
    }

    @Override
    public Comparator<K> comparator() {
        return ((SortedMap)this.back).comparator();
    }

    @Override
    public SortedSet<K> keySet() {
        return ((SortedMap)this.back).keySet();
    }

    @Override
    public java.util.SortedMap<K, Collection<V>> toJavaMap() {
        return this.toJavaMap(new java.util.TreeMap());
    }

    public static class Builder<V> {
        private final Multimap.ContainerType containerType;
        private final AbstractMultimap.SerializableSupplier<Traversable<?>> emptyContainer;

        private Builder(Multimap.ContainerType containerType, AbstractMultimap.SerializableSupplier<Traversable<?>> emptyContainer) {
            this.containerType = containerType;
            this.emptyContainer = emptyContainer;
        }

        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> empty() {
            return this.empty(Comparators.naturalComparator());
        }

        public <K, V2 extends V> TreeMultimap<K, V2> empty(Comparator<? super K> keyComparator) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            return new TreeMultimap(TreeMap.empty(keyComparator), this.containerType, this.emptyContainer);
        }

        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> ofEntries(Iterable<? extends Tuple2<? extends K, ? extends V2>> entries) {
            return this.ofEntries(Comparators.naturalComparator(), entries);
        }

        public <K, V2 extends V> TreeMultimap<K, V2> ofEntries(Comparator<? super K> keyComparator, Iterable<? extends Tuple2<? extends K, ? extends V2>> entries) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            Objects.requireNonNull(entries, "entries is null");
            TreeMultimap result = this.empty(keyComparator);
            for (Tuple2<K, V2> tuple2 : entries) {
                result = (TreeMultimap)result.put(tuple2._1, tuple2._2);
            }
            return result;
        }

        @SafeVarargs
        public final <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> ofEntries(Tuple2<? extends K, ? extends V2> ... entries) {
            return this.ofEntries(Comparators.naturalComparator(), entries);
        }

        @SafeVarargs
        public final <K, V2 extends V> TreeMultimap<K, V2> ofEntries(Comparator<? super K> keyComparator, Tuple2<? extends K, ? extends V2> ... entries) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            Objects.requireNonNull(entries, "entries is null");
            TreeMultimap result = this.empty(keyComparator);
            for (Tuple2<? extends K, ? extends V2> entry : entries) {
                result = (TreeMultimap)result.put(entry._1, entry._2);
            }
            return result;
        }

        @SafeVarargs
        public final <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> ofEntries(Map.Entry<? extends K, ? extends V2> ... entries) {
            return this.ofEntries(Comparators.naturalComparator(), entries);
        }

        @SafeVarargs
        public final <K, V2 extends V> TreeMultimap<K, V2> ofEntries(Comparator<? super K> keyComparator, Map.Entry<? extends K, ? extends V2> ... entries) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            Objects.requireNonNull(entries, "entries is null");
            TreeMultimap result = this.empty(keyComparator);
            for (Map.Entry<K, V2> entry : entries) {
                result = (TreeMultimap)result.put(entry.getKey(), entry.getValue());
            }
            return result;
        }

        public <K, V2 extends V> TreeMultimap<K, V2> ofAll(Comparator<? super K> keyComparator, java.util.Map<? extends K, ? extends V2> map) {
            return Multimaps.ofJavaMap(this.empty(keyComparator), map);
        }

        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> ofAll(java.util.Map<? extends K, ? extends V2> map) {
            return Multimaps.ofJavaMap(this.empty(), map);
        }

        public <T, K, V2 extends V> TreeMultimap<K, V2> ofAll(Comparator<? super K> keyComparator, Stream<? extends T> stream, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V2> valueMapper) {
            return Multimaps.ofStream(this.empty(keyComparator), stream, keyMapper, valueMapper);
        }

        public <T, K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> ofAll(Stream<? extends T> stream, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V2> valueMapper) {
            return Multimaps.ofStream(this.empty(), stream, keyMapper, valueMapper);
        }

        public <T, K, V2 extends V> TreeMultimap<K, V2> ofAll(Comparator<? super K> keyComparator, Stream<? extends T> stream, Function<? super T, Tuple2<? extends K, ? extends V2>> entryMapper) {
            return Multimaps.ofStream(this.empty(keyComparator), stream, entryMapper);
        }

        public <T, K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> ofAll(Stream<? extends T> stream, Function<? super T, Tuple2<? extends K, ? extends V2>> entryMapper) {
            return Multimaps.ofStream(this.empty(), stream, entryMapper);
        }

        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> tabulate(int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V2>> f) {
            return this.tabulate(Comparators.naturalComparator(), n, f);
        }

        public <K, V2 extends V> TreeMultimap<K, V2> tabulate(Comparator<? super K> keyComparator, int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V2>> f) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            Objects.requireNonNull(f, "f is null");
            return this.ofEntries(keyComparator, Collections.tabulate(n, f));
        }

        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> fill(int n, Supplier<? extends Tuple2<? extends K, ? extends V2>> s) {
            return this.fill(Comparators.naturalComparator(), n, s);
        }

        public <K, V2 extends V> TreeMultimap<K, V2> fill(Comparator<? super K> keyComparator, int n, Supplier<? extends Tuple2<? extends K, ? extends V2>> s) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            Objects.requireNonNull(s, "s is null");
            return this.ofEntries(keyComparator, Collections.fill(n, s));
        }

        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> fill(int n, Tuple2<? extends K, ? extends V2> element) {
            return this.fill(Comparators.naturalComparator(), n, element);
        }

        public <K, V2 extends V> TreeMultimap<K, V2> fill(Comparator<? super K> keyComparator, int n, Tuple2<? extends K, ? extends V2> element) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            return this.ofEntries(keyComparator, Collections.fillObject(n, element));
        }

        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K key, V2 value) {
            return this.of(Comparators.naturalComparator(), key, value);
        }

        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2) {
            return this.of(Comparators.naturalComparator(), k1, v1, k2, v2);
        }

        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3) {
            return this.of(Comparators.naturalComparator(), k1, v1, k2, v2, k3, v3);
        }

        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4) {
            return this.of(Comparators.naturalComparator(), k1, v1, k2, v2, k3, v3, k4, v4);
        }

        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5) {
            return this.of(Comparators.naturalComparator(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
        }

        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6) {
            return this.of(Comparators.naturalComparator(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
        }

        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7) {
            return this.of(Comparators.naturalComparator(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
        }

        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7, K k8, V2 v8) {
            return this.of(Comparators.naturalComparator(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
        }

        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7, K k8, V2 v8, K k9, V2 v9) {
            return this.of(Comparators.naturalComparator(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
        }

        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7, K k8, V2 v8, K k9, V2 v9, K k10, V2 v10) {
            return this.of(Comparators.naturalComparator(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
        }

        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(Tuple2<? extends K, ? extends V2> entry) {
            return this.of(Comparators.naturalComparator(), entry);
        }

        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K key, V2 value) {
            TreeMultimap<K, V2> e = this.empty(keyComparator);
            return (TreeMultimap)e.put(key, value);
        }

        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K k1, V2 v1, K k2, V2 v2) {
            return (TreeMultimap)this.of(keyComparator, k1, v1).put(k2, v2);
        }

        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K k1, V2 v1, K k2, V2 v2, K k3, V2 v3) {
            return (TreeMultimap)this.of(keyComparator, k1, v1, k2, v2).put(k3, v3);
        }

        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4) {
            return (TreeMultimap)this.of(keyComparator, k1, v1, k2, v2, k3, v3).put(k4, v4);
        }

        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5) {
            return (TreeMultimap)this.of(keyComparator, k1, v1, k2, v2, k3, v3, k4, v4).put(k5, v5);
        }

        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6) {
            return (TreeMultimap)this.of(keyComparator, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5).put(k6, v6);
        }

        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7) {
            return (TreeMultimap)this.of(keyComparator, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6).put(k7, v7);
        }

        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7, K k8, V2 v8) {
            return (TreeMultimap)this.of(keyComparator, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7).put(k8, v8);
        }

        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7, K k8, V2 v8, K k9, V2 v9) {
            return (TreeMultimap)this.of(keyComparator, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8).put(k9, v9);
        }

        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7, K k8, V2 v8, K k9, V2 v9, K k10, V2 v10) {
            return (TreeMultimap)this.of(keyComparator, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9).put(k10, v10);
        }

        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, Tuple2<? extends K, ? extends V2> entry) {
            TreeMultimap e = this.empty(keyComparator);
            return (TreeMultimap)e.put(entry._1, (V2)entry._2);
        }

        public <K extends Comparable<? super K>, V2 extends V> Collector<Tuple2<K, V2>, ArrayList<Tuple2<K, V2>>, TreeMultimap<K, V2>> collector() {
            return this.collector(Comparators.naturalComparator());
        }

        public <K, V2 extends V> Collector<Tuple2<K, V2>, ArrayList<Tuple2<K, V2>>, TreeMultimap<K, V2>> collector(Comparator<? super K> keyComparator) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            Supplier<ArrayList> supplier = ArrayList::new;
            BiConsumer<ArrayList, Tuple2> accumulator = ArrayList::add;
            BinaryOperator combiner = (left, right) -> {
                left.addAll(right);
                return left;
            };
            Function<ArrayList, TreeMultimap> finisher = list -> this.ofEntries(keyComparator, (Iterable)list);
            return Collector.of(supplier, accumulator, combiner, finisher, new Collector.Characteristics[0]);
        }
    }
}

