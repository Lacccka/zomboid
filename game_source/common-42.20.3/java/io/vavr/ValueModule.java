/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.Tuple2;
import io.vavr.Value;
import io.vavr.collection.Iterator;
import io.vavr.collection.Map;
import io.vavr.collection.Traversable;
import java.util.Collection;
import java.util.function.Function;

interface ValueModule {
    public static <T, R extends Traversable<T>> R toTraversable(Value<T> value, R empty, Function<T, R> ofElement, Function<Iterable<T>, R> ofAll) {
        if (value.isEmpty()) {
            return empty;
        }
        if (value.isSingleValued()) {
            return (R)((Traversable)ofElement.apply(value.get()));
        }
        return (R)((Traversable)ofAll.apply(value));
    }

    public static <T, K, V, E extends Tuple2<? extends K, ? extends V>, R extends Map<K, V>> R toMap(Value<T> value, R empty, Function<E, R> ofElement, Function<Iterable<E>, R> ofAll, Function<? super T, ? extends E> f) {
        if (value.isEmpty()) {
            return empty;
        }
        if (value.isSingleValued()) {
            return (R)((Map)ofElement.apply(f.apply(value.get())));
        }
        return (R)((Map)ofAll.apply(Iterator.ofAll(value).map(f)));
    }

    public static <T, R extends Collection<T>> R toJavaCollection(Value<T> value, Function<Integer, R> containerSupplier) {
        return ValueModule.toJavaCollection(value, containerSupplier, 16);
    }

    public static <T, R extends Collection<T>> R toJavaCollection(Value<T> value, Function<Integer, R> containerSupplier, int defaultInitialCapacity) {
        int size = value instanceof Traversable && ((Traversable)value).isTraversableAgain() && !value.isLazy() ? ((Traversable)value).size() : defaultInitialCapacity;
        Collection container = (Collection)containerSupplier.apply(size);
        value.forEach(container::add);
        return (R)container;
    }
}

