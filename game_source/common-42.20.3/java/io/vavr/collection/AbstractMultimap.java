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
import io.vavr.collection.Multimap;
import io.vavr.collection.Set;
import io.vavr.collection.Traversable;
import io.vavr.control.Option;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

abstract class AbstractMultimap<K, V, M extends Multimap<K, V>>
implements Multimap<K, V> {
    private static final long serialVersionUID = 1L;
    protected final Map<K, Traversable<V>> back;
    protected final SerializableSupplier<Traversable<?>> emptyContainer;
    private final Multimap.ContainerType containerType;

    AbstractMultimap(Map<K, Traversable<V>> back, Multimap.ContainerType containerType, SerializableSupplier<Traversable<?>> emptyContainer) {
        this.back = back;
        this.containerType = containerType;
        this.emptyContainer = emptyContainer;
    }

    protected abstract <K2, V2> Map<K2, V2> emptyMapSupplier();

    protected abstract <K2, V2> Multimap<K2, V2> emptyInstance();

    protected abstract <K2, V2> Multimap<K2, V2> createFromMap(Map<K2, Traversable<V2>> var1);

    private <K2, V2> Multimap<K2, V2> createFromEntries(Iterable<? extends Tuple2<? extends K2, ? extends V2>> entries) {
        Map<K2, Object> back = this.emptyMapSupplier();
        for (Tuple2<K2, V2> tuple2 : entries) {
            if (back.containsKey(tuple2._1)) {
                back = back.put(tuple2._1, this.containerType.add((Traversable)back.get(tuple2._1).get(), tuple2._2));
                continue;
            }
            back = back.put(tuple2._1, this.containerType.add((Traversable)this.emptyContainer.get(), tuple2._2));
        }
        return this.createFromMap(back);
    }

    @Override
    public Map<K, Traversable<V>> asMap() {
        return this.back;
    }

    @Override
    public <K2, V2> Multimap<K2, V2> bimap(Function<? super K, ? extends K2> keyMapper, Function<? super V, ? extends V2> valueMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        Traversable entries = this.iterator().map((T entry) -> Tuple.of(keyMapper.apply((Object)entry._1), valueMapper.apply((Object)entry._2)));
        return this.createFromEntries(entries);
    }

    @Override
    public boolean containsKey(K key) {
        return this.back.containsKey(key);
    }

    @Override
    public Multimap.ContainerType getContainerType() {
        return this.containerType;
    }

    @Override
    public <K2, V2> Multimap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.foldLeft(this.emptyInstance(), (acc, entry) -> {
            for (Tuple2 mappedEntry : (Iterable)mapper.apply((Object)entry._1, (Object)entry._2)) {
                acc = acc.put(mappedEntry);
            }
            return acc;
        });
    }

    @Override
    public Option<Traversable<V>> get(K key) {
        return this.back.get(key);
    }

    @Override
    public Traversable<V> getOrElse(K key, Traversable<? extends V> defaultValue) {
        return this.back.getOrElse(key, defaultValue);
    }

    @Override
    public Set<K> keySet() {
        return this.back.keySet();
    }

    @Override
    public <K2, V2> Multimap<K2, V2> map(BiFunction<? super K, ? super V, Tuple2<K2, V2>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.foldLeft(this.emptyInstance(), (acc, entry) -> acc.put((Tuple2)mapper.apply((Object)entry._1, (Object)entry._2)));
    }

    @Override
    public <V2> Multimap<K, V2> mapValues(Function<? super V, ? extends V2> valueMapper) {
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return this.map((? super K k, ? super V v) -> Tuple.of(k, valueMapper.apply((Object)v)));
    }

    public M put(K key, V value) {
        Traversable values2 = this.back.get(key).getOrElse((Traversable)this.emptyContainer.get());
        Traversable<V> newValues = this.containerType.add(values2, value);
        return (M)(newValues == values2 ? this : this.createFromMap(this.back.put(key, newValues)));
    }

    public M put(Tuple2<? extends K, ? extends V> entry) {
        Objects.requireNonNull(entry, "entry is null");
        return this.put(entry._1, entry._2);
    }

    public M remove(K key) {
        return (M)(this.back.containsKey(key) ? this.createFromMap(this.back.remove(key)) : this);
    }

    public M remove(K key, V value) {
        Traversable values2 = this.back.get(key).getOrElse((Traversable)this.emptyContainer.get());
        Traversable<V> newValues = this.containerType.remove(values2, value);
        if (newValues == values2) {
            return (M)this;
        }
        if (newValues.isEmpty()) {
            return (M)this.createFromMap(this.back.remove(key));
        }
        return (M)this.createFromMap(this.back.put(key, newValues));
    }

    public M removeAll(Iterable<? extends K> keys2) {
        Map<? extends K, Traversable<V>> result = this.back.removeAll(keys2);
        return (M)(result == this.back ? this : this.createFromMap(result));
    }

    @Override
    public int size() {
        return this.back.foldLeft(0, (s, t) -> s + ((Traversable)t._2).size());
    }

    @Override
    public Traversable<V> values() {
        return Iterator.concat(this.back.values()).toStream();
    }

    public M distinct() {
        return (M)(this.containerType == Multimap.ContainerType.SEQ ? this.createFromEntries(this.iterator().distinct()) : this);
    }

    public M distinctBy(Comparator<? super Tuple2<K, V>> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return (M)(this.isEmpty() ? this : this.createFromEntries(this.iterator().distinctBy(comparator)));
    }

    public <U> M distinctBy(Function<? super Tuple2<K, V>, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        return (M)(this.isEmpty() ? this : this.createFromEntries(this.iterator().distinctBy(keyExtractor)));
    }

    public M drop(int n) {
        if (n <= 0 || this.isEmpty()) {
            return (M)this;
        }
        if (n >= this.length()) {
            return (M)this.emptyInstance();
        }
        return (M)this.createFromEntries(this.iterator().drop(n));
    }

    public M dropRight(int n) {
        if (n <= 0 || this.isEmpty()) {
            return (M)this;
        }
        if (n >= this.length()) {
            return (M)this.emptyInstance();
        }
        return (M)this.createFromEntries(this.iterator().dropRight(n));
    }

    public M dropUntil(Predicate<? super Tuple2<K, V>> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return (M)this.dropWhile((Predicate)predicate.negate());
    }

    public M dropWhile(Predicate<? super Tuple2<K, V>> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return (M)(this.isEmpty() ? this : this.createFromEntries(this.iterator().dropWhile(predicate)));
    }

    public M filter(Predicate<? super Tuple2<K, V>> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (this.isEmpty()) {
            return (M)this;
        }
        return (M)this.createFromEntries(this.iterator().filter(predicate));
    }

    public M reject(Predicate<? super Tuple2<K, V>> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return (M)this.filter((Predicate)predicate.negate());
    }

    public M filter(BiPredicate<? super K, ? super V> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return (M)this.filter((T t) -> predicate.test((Object)t._1, (Object)t._2));
    }

    public M reject(BiPredicate<? super K, ? super V> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return (M)this.reject((T t) -> predicate.test((Object)t._1, (Object)t._2));
    }

    public M filterKeys(Predicate<? super K> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return (M)this.filter((T t) -> predicate.test((Object)t._1));
    }

    public M rejectKeys(Predicate<? super K> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return (M)this.reject((T t) -> predicate.test((Object)t._1));
    }

    public M filterValues(Predicate<? super V> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return (M)this.filter((T t) -> predicate.test((Object)t._2));
    }

    public M rejectValues(Predicate<? super V> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return (M)this.reject((T t) -> predicate.test((Object)t._2));
    }

    @Deprecated
    public M removeAll(BiPredicate<? super K, ? super V> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.reject(predicate);
    }

    @Deprecated
    public M removeKeys(Predicate<? super K> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.rejectKeys(predicate);
    }

    @Deprecated
    public M removeValues(Predicate<? super V> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.rejectValues(predicate);
    }

    @Override
    public <C> Map<C, M> groupBy(Function<? super Tuple2<K, V>, ? extends C> classifier) {
        return Collections.groupBy(this, classifier, this::createFromEntries);
    }

    @Override
    public Iterator<M> grouped(int size) {
        return this.sliding(size, size);
    }

    public M init() {
        if (this.back.isEmpty()) {
            throw new UnsupportedOperationException("init of empty HashMap");
        }
        Object last = this.last();
        return this.remove(((Tuple2)last)._1, ((Tuple2)last)._2);
    }

    @Override
    public Tuple2<K, V> head() {
        Tuple2 head = (Tuple2)this.back.head();
        return Tuple.of(head._1, ((Traversable)head._2).head());
    }

    @Override
    public Option<M> initOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.init());
    }

    @Override
    public boolean isAsync() {
        return this.back.isAsync();
    }

    @Override
    public boolean isEmpty() {
        return this.back.isEmpty();
    }

    @Override
    public boolean isLazy() {
        return this.back.isLazy();
    }

    @Override
    public Iterator<Tuple2<K, V>> iterator() {
        if (this.containerType == Multimap.ContainerType.SORTED_SET) {
            return this.back.iterator().flatMap((T t) -> ((Traversable)t._2).iterator().map((T v) -> Tuple.of(t._1, v)));
        }
        return this.back.iterator().flatMap((T t) -> ((Traversable)t._2).map((T v) -> Tuple.of(t._1, v)));
    }

    @Override
    public Tuple2<K, V> last() {
        Tuple2 last = (Tuple2)this.back.last();
        return Tuple.of(last._1, ((Traversable)last._2).last());
    }

    public M merge(Multimap<? extends K, ? extends V> that) {
        Objects.requireNonNull(that, "that is null");
        if (this.isEmpty()) {
            return (M)this.createFromEntries(that);
        }
        if (that.isEmpty()) {
            return (M)this;
        }
        return (M)that.foldLeft(this, (map, entry) -> map.put(entry));
    }

    public <K2 extends K, V2 extends V> M merge(Multimap<K2, V2> that, BiFunction<Traversable<V>, Traversable<V2>, Traversable<V>> collisionResolution) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(collisionResolution, "collisionResolution is null");
        if (this.isEmpty()) {
            return (M)this.createFromEntries(that);
        }
        if (that.isEmpty()) {
            return (M)this;
        }
        Map result = that.keySet().foldLeft(this.back, (map, key) -> {
            Traversable thisValues = map.get(key).getOrElse((Traversable)this.emptyContainer.get());
            Traversable thatValues = that.get(key).get();
            Traversable newValues = (Traversable)collisionResolution.apply(thisValues, thatValues);
            return map.put(key, newValues);
        });
        return (M)this.createFromMap(result);
    }

    public M orElse(Iterable<? extends Tuple2<K, V>> other) {
        return (M)(this.isEmpty() ? this.createFromEntries(other) : this);
    }

    public M orElse(Supplier<? extends Iterable<? extends Tuple2<K, V>>> supplier) {
        return (M)(this.isEmpty() ? this.createFromEntries(supplier.get()) : this);
    }

    @Override
    public Tuple2<M, M> partition(Predicate<? super Tuple2<K, V>> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        ArrayList<Tuple2> left = new ArrayList<Tuple2>();
        ArrayList right = new ArrayList();
        for (Tuple2 entry : this) {
            (predicate.test(entry) ? left : right).add(entry);
        }
        return Tuple.of(this.createFromEntries(left), this.createFromEntries(right));
    }

    public M peek(Consumer<? super Tuple2<K, V>> action) {
        Objects.requireNonNull(action, "action is null");
        if (!this.isEmpty()) {
            action.accept((Tuple2<K, V>)this.head());
        }
        return (M)this;
    }

    public M replace(Tuple2<K, V> currentElement, Tuple2<K, V> newElement) {
        Objects.requireNonNull(currentElement, "currentElement is null");
        Objects.requireNonNull(newElement, "newElement is null");
        return (M)(this.containsKey(currentElement._1) ? this.remove(currentElement._1, currentElement._2).put(newElement) : this);
    }

    public M replaceAll(Tuple2<K, V> currentElement, Tuple2<K, V> newElement) {
        return this.replace(currentElement, newElement);
    }

    public M replaceValue(K key, V value) {
        return (M)(this.containsKey(key) ? this.remove(key).put(key, value) : this);
    }

    public M replace(K key, V oldValue, V newValue) {
        return (M)(this.contains(API.Tuple(key, oldValue)) ? this.remove(key, oldValue).put(key, newValue) : this);
    }

    public M replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        return (M)this.map((? super K k, ? super V v) -> API.Tuple(k, function.apply((Object)k, (Object)v)));
    }

    public M retainAll(Iterable<? extends Tuple2<K, V>> elements) {
        Objects.requireNonNull(elements, "elements is null");
        return (M)this.createFromEntries(this.back.flatMap((T t) -> ((Traversable)t._2).map((T v) -> Tuple.of(t._1, v))).retainAll(elements));
    }

    public M scan(Tuple2<K, V> zero, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K, V>, ? extends Tuple2<K, V>> operation) {
        return (M)Collections.scanLeft(this, zero, operation, this::createFromEntries);
    }

    @Override
    public Iterator<M> slideBy(Function<? super Tuple2<K, V>, ?> classifier) {
        return this.iterator().slideBy(classifier).map(this::createFromEntries);
    }

    @Override
    public Iterator<M> sliding(int size) {
        return this.sliding(size, 1);
    }

    @Override
    public Iterator<M> sliding(int size, int step) {
        return this.iterator().sliding(size, step).map(this::createFromEntries);
    }

    @Override
    public Tuple2<M, M> span(Predicate<? super Tuple2<K, V>> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        Tuple2<Iterator<? super Tuple2<K, V>>, Iterator<? super Tuple2<K, V>>> t = this.iterator().span(predicate);
        return Tuple.of(this.createFromEntries((Iterable)t._1), this.createFromEntries((Iterable)t._2));
    }

    public M tail() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("tail of empty Multimap");
        }
        Object head = this.head();
        return this.remove(((Tuple2)head)._1, ((Tuple2)head)._2);
    }

    @Override
    public Option<M> tailOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.tail());
    }

    public M take(int n) {
        if (this.isEmpty() || n >= this.length()) {
            return (M)this;
        }
        if (n <= 0) {
            return (M)this.emptyInstance();
        }
        return (M)this.createFromEntries(this.iterator().take(n));
    }

    public M takeRight(int n) {
        if (this.isEmpty() || n >= this.length()) {
            return (M)this;
        }
        if (n <= 0) {
            return (M)this.emptyInstance();
        }
        return (M)this.createFromEntries(this.iterator().takeRight(n));
    }

    public M takeUntil(Predicate<? super Tuple2<K, V>> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return (M)this.takeWhile((Predicate)predicate.negate());
    }

    public M takeWhile(Predicate<? super Tuple2<K, V>> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        Multimap taken = this.createFromEntries(this.iterator().takeWhile(predicate));
        return (M)(taken.length() == this.length() ? this : taken);
    }

    @Override
    public boolean equals(Object o) {
        return Collections.equals(this, o);
    }

    @Override
    public int hashCode() {
        return this.back.hashCode();
    }

    @Override
    public String stringPrefix() {
        return this.getClass().getSimpleName() + "[" + ((Traversable)this.emptyContainer.get()).stringPrefix() + "]";
    }

    @Override
    public String toString() {
        return this.mkString(this.stringPrefix() + "(", ", ", ")");
    }

    @Override
    public java.util.Map<K, Collection<V>> toJavaMap() {
        return this.toJavaMap(new HashMap());
    }

    protected <JM extends java.util.Map<K, Collection<V>>> JM toJavaMap(JM javaMap) {
        for (Tuple2 t : this) {
            javaMap.computeIfAbsent(t._1, k -> this.containerType.instantiate()).add(t._2);
        }
        return javaMap;
    }

    static interface SerializableSupplier<T>
    extends Supplier<T>,
    Serializable {
    }
}

