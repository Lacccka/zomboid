/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Tuple2;
import io.vavr.collection.Multimap;
import io.vavr.collection.Stream;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

class Multimaps {
    Multimaps() {
    }

    static <K, V, M extends Multimap<K, V>> M ofJavaMap(M source2, Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map is null");
        return (M)Stream.ofAll(map.entrySet()).foldLeft(source2, (m, el) -> m.put(el.getKey(), el.getValue()));
    }

    static <T, K, V, M extends Multimap<K, V>> M ofStream(M source2, java.util.stream.Stream<? extends T> stream, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(stream, "stream is null");
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return (M)Stream.ofAll(stream).foldLeft(source2, (m, el) -> m.put(keyMapper.apply(el), valueMapper.apply(el)));
    }

    static <T, K, V, M extends Multimap<K, V>> M ofStream(M source2, java.util.stream.Stream<? extends T> stream, Function<? super T, Tuple2<? extends K, ? extends V>> entryMapper) {
        Objects.requireNonNull(stream, "stream is null");
        Objects.requireNonNull(entryMapper, "entryMapper is null");
        return (M)Stream.ofAll(stream).foldLeft(source2, (m, el) -> m.put((Tuple2)entryMapper.apply(el)));
    }
}

