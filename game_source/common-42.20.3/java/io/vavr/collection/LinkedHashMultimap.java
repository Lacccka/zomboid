/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Tuple2;
import io.vavr.collection.AbstractMultimap;
import io.vavr.collection.Collections;
import io.vavr.collection.HashSet;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Multimap;
import io.vavr.collection.Multimaps;
import io.vavr.collection.Traversable;
import io.vavr.collection.TreeSet;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public final class LinkedHashMultimap<K, V>
extends AbstractMultimap<K, V, LinkedHashMultimap<K, V>>
implements Serializable {
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

    public static <K, V> LinkedHashMultimap<K, V> narrow(LinkedHashMultimap<? extends K, ? extends V> map) {
        return map;
    }

    private LinkedHashMultimap(Map<K, Traversable<V>> back, Multimap.ContainerType containerType, AbstractMultimap.SerializableSupplier<Traversable<?>> emptyContainer) {
        super(back, containerType, emptyContainer);
    }

    @Override
    protected <K2, V2> Map<K2, V2> emptyMapSupplier() {
        return LinkedHashMap.empty();
    }

    @Override
    protected <K2, V2> LinkedHashMultimap<K2, V2> emptyInstance() {
        return new LinkedHashMultimap(LinkedHashMap.empty(), this.getContainerType(), this.emptyContainer);
    }

    @Override
    protected <K2, V2> LinkedHashMultimap<K2, V2> createFromMap(Map<K2, Traversable<V2>> back) {
        return new LinkedHashMultimap<K2, V2>(back, this.getContainerType(), this.emptyContainer);
    }

    @Override
    public boolean isSequential() {
        return true;
    }

    public static class Builder<V> {
        private final Multimap.ContainerType containerType;
        private final AbstractMultimap.SerializableSupplier<Traversable<?>> emptyContainer;

        private Builder(Multimap.ContainerType containerType, AbstractMultimap.SerializableSupplier<Traversable<?>> emptyContainer) {
            this.containerType = containerType;
            this.emptyContainer = emptyContainer;
        }

        public <K, V2 extends V> LinkedHashMultimap<K, V2> empty() {
            return new LinkedHashMultimap(LinkedHashMap.empty(), this.containerType, this.emptyContainer);
        }

        public <K, V2 extends V> LinkedHashMultimap<K, V2> ofEntries(Iterable<? extends Tuple2<? extends K, ? extends V2>> entries) {
            Objects.requireNonNull(entries, "entries is null");
            LinkedHashMultimap result = this.empty();
            for (Tuple2<K, V2> tuple2 : entries) {
                result = (LinkedHashMultimap)result.put(tuple2._1, tuple2._2);
            }
            return result;
        }

        @SafeVarargs
        public final <K, V2 extends V> LinkedHashMultimap<K, V2> ofEntries(Tuple2<? extends K, ? extends V2> ... entries) {
            Objects.requireNonNull(entries, "entries is null");
            LinkedHashMultimap result = this.empty();
            for (Tuple2<? extends K, ? extends V2> entry : entries) {
                result = (LinkedHashMultimap)result.put(entry._1, entry._2);
            }
            return result;
        }

        @SafeVarargs
        public final <K, V2 extends V> LinkedHashMultimap<K, V2> ofEntries(Map.Entry<? extends K, ? extends V2> ... entries) {
            Objects.requireNonNull(entries, "entries is null");
            LinkedHashMultimap result = this.empty();
            for (Map.Entry<K, V2> entry : entries) {
                result = (LinkedHashMultimap)result.put(entry.getKey(), entry.getValue());
            }
            return result;
        }

        public <K, V2 extends V> LinkedHashMultimap<K, V2> ofAll(java.util.Map<? extends K, ? extends V2> map) {
            return Multimaps.ofJavaMap(this.empty(), map);
        }

        public <T, K, V2 extends V> LinkedHashMultimap<K, V2> ofAll(Stream<? extends T> stream, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V2> valueMapper) {
            return Multimaps.ofStream(this.empty(), stream, keyMapper, valueMapper);
        }

        public <T, K, V2 extends V> LinkedHashMultimap<K, V2> ofAll(Stream<? extends T> stream, Function<? super T, Tuple2<? extends K, ? extends V2>> entryMapper) {
            return Multimaps.ofStream(this.empty(), stream, entryMapper);
        }

        public <K, V2 extends V> LinkedHashMultimap<K, V2> tabulate(int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V2>> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ofEntries(Collections.tabulate(n, f));
        }

        public <K, V2 extends V> LinkedHashMultimap<K, V2> fill(int n, Supplier<? extends Tuple2<? extends K, ? extends V2>> s) {
            Objects.requireNonNull(s, "s is null");
            return this.ofEntries(Collections.fill(n, s));
        }

        public <K, V2 extends V> LinkedHashMultimap<K, V2> fill(int n, Tuple2<? extends K, ? extends V2> element) {
            return this.ofEntries(Collections.fillObject(n, element));
        }

        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K key, V2 value) {
            LinkedHashMultimap<K, V2> e = this.empty();
            return (LinkedHashMultimap)e.put(key, value);
        }

        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2) {
            return (LinkedHashMultimap)this.of(k1, v1).put(k2, v2);
        }

        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3) {
            return (LinkedHashMultimap)this.of(k1, v1, k2, v2).put(k3, v3);
        }

        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4) {
            return (LinkedHashMultimap)this.of(k1, v1, k2, v2, k3, v3).put(k4, v4);
        }

        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5) {
            return (LinkedHashMultimap)this.of(k1, v1, k2, v2, k3, v3, k4, v4).put(k5, v5);
        }

        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6) {
            return (LinkedHashMultimap)this.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5).put(k6, v6);
        }

        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7) {
            return (LinkedHashMultimap)this.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6).put(k7, v7);
        }

        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7, K k8, V2 v8) {
            return (LinkedHashMultimap)this.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7).put(k8, v8);
        }

        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7, K k8, V2 v8, K k9, V2 v9) {
            return (LinkedHashMultimap)this.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8).put(k9, v9);
        }

        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7, K k8, V2 v8, K k9, V2 v9, K k10, V2 v10) {
            return (LinkedHashMultimap)this.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9).put(k10, v10);
        }

        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(Tuple2<? extends K, ? extends V2> entry) {
            LinkedHashMultimap e = this.empty();
            return (LinkedHashMultimap)e.put(entry._1, entry._2);
        }

        public <K, V2 extends V> Collector<Tuple2<K, V2>, ArrayList<Tuple2<K, V2>>, Multimap<K, V2>> collector() {
            Supplier<ArrayList> supplier = ArrayList::new;
            BiConsumer<ArrayList, Tuple2> accumulator = ArrayList::add;
            BinaryOperator combiner = (left, right) -> {
                left.addAll(right);
                return left;
            };
            return Collector.of(supplier, accumulator, combiner, this::ofEntries, new Collector.Characteristics[0]);
        }
    }
}

