/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Function1;
import io.vavr.PartialFunction;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Value;
import io.vavr.collection.Collections;
import io.vavr.collection.Iterator;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Traversable;
import io.vavr.collection.Vector;
import io.vavr.control.Option;
import java.io.Serializable;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Map<K, V>
extends Traversable<Tuple2<K, V>>,
PartialFunction<K, V>,
Serializable {
    public static final long serialVersionUID = 1L;

    public static <K, V> Map<K, V> narrow(Map<? extends K, ? extends V> map) {
        return map;
    }

    public static <K, V> Tuple2<K, V> entry(K key, V value) {
        return Tuple.of(key, value);
    }

    @Override
    @Deprecated
    default public V apply(K key) {
        return this.get(key).getOrElseThrow(() -> new NoSuchElementException(String.valueOf(key)));
    }

    default public PartialFunction<K, V> asPartialFunction() throws IndexOutOfBoundsException {
        return new PartialFunction<K, V>(){
            private static final long serialVersionUID = 1L;

            @Override
            public V apply(K key) {
                return Map.this.get(key).getOrElseThrow(() -> new NoSuchElementException(String.valueOf(key)));
            }

            @Override
            public boolean isDefinedAt(K key) {
                return Map.this.containsKey(key);
            }
        };
    }

    @Override
    default public <R> Seq<R> collect(PartialFunction<? super Tuple2<K, V>, ? extends R> partialFunction) {
        return Vector.ofAll(this.iterator().collect(partialFunction));
    }

    public <K2, V2> Map<K2, V2> bimap(Function<? super K, ? extends K2> var1, Function<? super V, ? extends V2> var2);

    @Override
    default public boolean contains(Tuple2<K, V> element) {
        return this.get(element._1).map((T v) -> Objects.equals(v, element._2)).getOrElse(false);
    }

    public Tuple2<V, ? extends Map<K, V>> computeIfAbsent(K var1, Function<? super K, ? extends V> var2);

    public Tuple2<Option<V>, ? extends Map<K, V>> computeIfPresent(K var1, BiFunction<? super K, ? super V, ? extends V> var2);

    public boolean containsKey(K var1);

    default public boolean containsValue(V value) {
        return this.iterator().map(Tuple2::_2).contains(value);
    }

    public Map<K, V> filter(BiPredicate<? super K, ? super V> var1);

    public Map<K, V> reject(BiPredicate<? super K, ? super V> var1);

    public Map<K, V> filterKeys(Predicate<? super K> var1);

    public Map<K, V> rejectKeys(Predicate<? super K> var1);

    public Map<K, V> filterValues(Predicate<? super V> var1);

    public Map<K, V> rejectValues(Predicate<? super V> var1);

    public <K2, V2> Map<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> var1);

    @Override
    default public <U> Seq<U> flatMap(Function<? super Tuple2<K, V>, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.iterator().flatMap(mapper).toStream();
    }

    @Override
    default public <U> U foldRight(U zero, BiFunction<? super Tuple2<K, V>, ? super U, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return this.iterator().foldRight(zero, f);
    }

    default public void forEach(BiConsumer<K, V> action) {
        Objects.requireNonNull(action, "action is null");
        for (Tuple2 t : this) {
            action.accept(t._1, t._2);
        }
    }

    public Option<V> get(K var1);

    public V getOrElse(K var1, V var2);

    @Override
    default public boolean hasDefiniteSize() {
        return true;
    }

    @Override
    default public boolean isTraversableAgain() {
        return true;
    }

    @Override
    public Iterator<Tuple2<K, V>> iterator();

    default public <U> Iterator<U> iterator(BiFunction<K, V, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.iterator().map((T t) -> mapper.apply(t._1, t._2));
    }

    public Set<K> keySet();

    default public Iterator<K> keysIterator() {
        return this.iterator().map(Tuple2::_1);
    }

    @Override
    default public int length() {
        return this.size();
    }

    @Override
    default public Function1<K, Option<V>> lift() {
        return this::get;
    }

    @Override
    default public <U> Seq<U> map(Function<? super Tuple2<K, V>, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.iterator().map(mapper).toStream();
    }

    public <K2, V2> Map<K2, V2> map(BiFunction<? super K, ? super V, Tuple2<K2, V2>> var1);

    public <K2> Map<K2, V> mapKeys(Function<? super K, ? extends K2> var1);

    public <K2> Map<K2, V> mapKeys(Function<? super K, ? extends K2> var1, BiFunction<? super V, ? super V, ? extends V> var2);

    public <V2> Map<K, V2> mapValues(Function<? super V, ? extends V2> var1);

    public Map<K, V> merge(Map<? extends K, ? extends V> var1);

    public <U extends V> Map<K, V> merge(Map<? extends K, U> var1, BiFunction<? super V, ? super U, ? extends V> var2);

    public Map<K, V> put(K var1, V var2);

    public Map<K, V> put(Tuple2<? extends K, ? extends V> var1);

    public <U extends V> Map<K, V> put(K var1, U var2, BiFunction<? super V, ? super U, ? extends V> var3);

    public <U extends V> Map<K, V> put(Tuple2<? extends K, U> var1, BiFunction<? super V, ? super U, ? extends V> var2);

    public Map<K, V> remove(K var1);

    @Deprecated
    public Map<K, V> removeAll(BiPredicate<? super K, ? super V> var1);

    public Map<K, V> removeAll(Iterable<? extends K> var1);

    @Deprecated
    public Map<K, V> removeKeys(Predicate<? super K> var1);

    @Deprecated
    public Map<K, V> removeValues(Predicate<? super V> var1);

    @Override
    default public <U> Seq<U> scanLeft(U zero, BiFunction<? super U, ? super Tuple2<K, V>, ? extends U> operation) {
        return Collections.scanLeft(this, zero, operation, Value::toVector);
    }

    @Override
    default public <U> Seq<U> scanRight(U zero, BiFunction<? super Tuple2<K, V>, ? super U, ? extends U> operation) {
        return Collections.scanRight(this, zero, operation, Value::toVector);
    }

    @Override
    public int size();

    public java.util.Map<K, V> toJavaMap();

    default public <U> U transform(Function<? super Map<K, V>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    default public Tuple2<Seq<K>, Seq<V>> unzip() {
        return this.unzip(Function.identity());
    }

    default public <T1, T2> Tuple2<Seq<T1>, Seq<T2>> unzip(BiFunction<? super K, ? super V, Tuple2<? extends T1, ? extends T2>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        return this.unzip((Function<? super Tuple2<K, V>, Tuple2<? extends T1, ? extends T2>>)((Function<Tuple2, Tuple2>)entry -> (Tuple2)unzipper.apply((Object)entry._1, (Object)entry._2)));
    }

    @Override
    default public <T1, T2> Tuple2<Seq<T1>, Seq<T2>> unzip(Function<? super Tuple2<K, V>, Tuple2<? extends T1, ? extends T2>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        return this.iterator().unzip(unzipper).map(Stream::ofAll, Stream::ofAll);
    }

    default public <T1, T2, T3> Tuple3<Seq<T1>, Seq<T2>, Seq<T3>> unzip3(BiFunction<? super K, ? super V, Tuple3<? extends T1, ? extends T2, ? extends T3>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        return this.unzip3((Function<? super Tuple2<K, V>, Tuple3<? extends T1, ? extends T2, ? extends T3>>)((Function<Tuple2, Tuple3>)entry -> (Tuple3)unzipper.apply((Object)entry._1, (Object)entry._2)));
    }

    @Override
    default public <T1, T2, T3> Tuple3<Seq<T1>, Seq<T2>, Seq<T3>> unzip3(Function<? super Tuple2<K, V>, Tuple3<? extends T1, ? extends T2, ? extends T3>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        return this.iterator().unzip3(unzipper).map(Stream::ofAll, Stream::ofAll, Stream::ofAll);
    }

    public Seq<V> values();

    default public Iterator<V> valuesIterator() {
        return this.iterator().map(Tuple2::_2);
    }

    @Deprecated
    default public Function1<K, V> withDefault(Function<? super K, ? extends V> defaultFunction) {
        return k -> this.get(k).getOrElse(() -> defaultFunction.apply((Object)k));
    }

    @Deprecated
    default public Function1<K, V> withDefaultValue(V defaultValue) {
        return k -> this.get(k).getOrElse(defaultValue);
    }

    @Override
    default public <U> Seq<Tuple2<Tuple2<K, V>, U>> zip(Iterable<? extends U> that) {
        return this.zipWith((Iterable)that, Tuple::of);
    }

    @Override
    default public <U, R> Seq<R> zipWith(Iterable<? extends U> that, BiFunction<? super Tuple2<K, V>, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return Stream.ofAll(this.iterator().zipWith(that, mapper));
    }

    @Override
    default public <U> Seq<Tuple2<Tuple2<K, V>, U>> zipAll(Iterable<? extends U> that, Tuple2<K, V> thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        return Stream.ofAll(this.iterator().zipAll(that, thisElem, thatElem));
    }

    @Override
    default public Seq<Tuple2<Tuple2<K, V>, Integer>> zipWithIndex() {
        return this.zipWithIndex(Tuple::of);
    }

    @Override
    default public <U> Seq<U> zipWithIndex(BiFunction<? super Tuple2<K, V>, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return Stream.ofAll(this.iterator().zipWithIndex(mapper));
    }

    public Map<K, V> distinct();

    public Map<K, V> distinctBy(Comparator<? super Tuple2<K, V>> var1);

    public <U> Map<K, V> distinctBy(Function<? super Tuple2<K, V>, ? extends U> var1);

    public Map<K, V> drop(int var1);

    public Map<K, V> dropRight(int var1);

    public Map<K, V> dropUntil(Predicate<? super Tuple2<K, V>> var1);

    public Map<K, V> dropWhile(Predicate<? super Tuple2<K, V>> var1);

    public Map<K, V> filter(Predicate<? super Tuple2<K, V>> var1);

    public Map<K, V> reject(Predicate<? super Tuple2<K, V>> var1);

    @Override
    public <C> Map<C, ? extends Map<K, V>> groupBy(Function<? super Tuple2<K, V>, ? extends C> var1);

    @Override
    public Iterator<? extends Map<K, V>> grouped(int var1);

    @Override
    @Deprecated
    default public boolean isDefinedAt(K key) {
        return this.containsKey(key);
    }

    @Override
    default public boolean isDistinct() {
        return true;
    }

    public Map<K, V> init();

    @Override
    public Option<? extends Map<K, V>> initOption();

    public Map<K, V> orElse(Iterable<? extends Tuple2<K, V>> var1);

    public Map<K, V> orElse(Supplier<? extends Iterable<? extends Tuple2<K, V>>> var1);

    @Override
    public Tuple2<? extends Map<K, V>, ? extends Map<K, V>> partition(Predicate<? super Tuple2<K, V>> var1);

    public Map<K, V> peek(Consumer<? super Tuple2<K, V>> var1);

    public Map<K, V> replace(Tuple2<K, V> var1, Tuple2<K, V> var2);

    public Map<K, V> replaceValue(K var1, V var2);

    public Map<K, V> replace(K var1, V var2, V var3);

    public Map<K, V> replaceAll(BiFunction<? super K, ? super V, ? extends V> var1);

    public Map<K, V> replaceAll(Tuple2<K, V> var1, Tuple2<K, V> var2);

    public Map<K, V> retainAll(Iterable<? extends Tuple2<K, V>> var1);

    public Map<K, V> scan(Tuple2<K, V> var1, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K, V>, ? extends Tuple2<K, V>> var2);

    @Override
    public Iterator<? extends Map<K, V>> slideBy(Function<? super Tuple2<K, V>, ?> var1);

    @Override
    public Iterator<? extends Map<K, V>> sliding(int var1);

    @Override
    public Iterator<? extends Map<K, V>> sliding(int var1, int var2);

    @Override
    public Tuple2<? extends Map<K, V>, ? extends Map<K, V>> span(Predicate<? super Tuple2<K, V>> var1);

    public Map<K, V> tail();

    @Override
    public Option<? extends Map<K, V>> tailOption();

    public Map<K, V> take(int var1);

    public Map<K, V> takeRight(int var1);

    public Map<K, V> takeUntil(Predicate<? super Tuple2<K, V>> var1);

    public Map<K, V> takeWhile(Predicate<? super Tuple2<K, V>> var1);
}

