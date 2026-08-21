/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.PartialFunction;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Value;
import io.vavr.collection.Collections;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Traversable;
import io.vavr.collection.Vector;
import io.vavr.control.Option;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Multimap<K, V>
extends Traversable<Tuple2<K, V>>,
PartialFunction<K, Traversable<V>>,
Serializable {
    public static final long serialVersionUID = 1L;

    public static <K, V> Multimap<K, V> narrow(Multimap<? extends K, ? extends V> map) {
        return map;
    }

    @Override
    @Deprecated
    default public Traversable<V> apply(K key) {
        return this.get(key).getOrElseThrow(NoSuchElementException::new);
    }

    public Map<K, Traversable<V>> asMap();

    default public PartialFunction<K, Traversable<V>> asPartialFunction() throws IndexOutOfBoundsException {
        return new PartialFunction<K, Traversable<V>>(){
            private static final long serialVersionUID = 1L;

            @Override
            public Traversable<V> apply(K key) {
                return Multimap.this.get(key).getOrElseThrow(NoSuchElementException::new);
            }

            @Override
            public boolean isDefinedAt(K key) {
                return Multimap.this.containsKey(key);
            }
        };
    }

    public <K2, V2> Multimap<K2, V2> bimap(Function<? super K, ? extends K2> var1, Function<? super V, ? extends V2> var2);

    @Override
    default public <R> Seq<R> collect(PartialFunction<? super Tuple2<K, V>, ? extends R> partialFunction) {
        return Vector.ofAll(this.iterator().collect(partialFunction));
    }

    @Override
    default public boolean contains(Tuple2<K, V> element) {
        return this.get(element._1).map((T v) -> v.contains(element._2)).getOrElse(false);
    }

    public boolean containsKey(K var1);

    default public boolean containsValue(V value) {
        return this.iterator().map(Tuple2::_2).contains(value);
    }

    public Multimap<K, V> filter(BiPredicate<? super K, ? super V> var1);

    public Multimap<K, V> reject(BiPredicate<? super K, ? super V> var1);

    public Multimap<K, V> filterKeys(Predicate<? super K> var1);

    public Multimap<K, V> rejectKeys(Predicate<? super K> var1);

    public Multimap<K, V> filterValues(Predicate<? super V> var1);

    public Multimap<K, V> rejectValues(Predicate<? super V> var1);

    public <K2, V2> Multimap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> var1);

    default public void forEach(BiConsumer<K, V> action) {
        Objects.requireNonNull(action, "action is null");
        for (Tuple2 t : this) {
            action.accept(t._1, t._2);
        }
    }

    public Option<Traversable<V>> get(K var1);

    public Traversable<V> getOrElse(K var1, Traversable<? extends V> var2);

    public ContainerType getContainerType();

    @Override
    default public boolean hasDefiniteSize() {
        return true;
    }

    @Override
    @Deprecated
    default public boolean isDefinedAt(K key) {
        return this.containsKey(key);
    }

    @Override
    default public boolean isDistinct() {
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

    @Override
    default public int length() {
        return this.size();
    }

    public <K2, V2> Multimap<K2, V2> map(BiFunction<? super K, ? super V, Tuple2<K2, V2>> var1);

    @Override
    default public <U> Seq<U> map(Function<? super Tuple2<K, V>, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.iterator().map(mapper).toStream();
    }

    public <V2> Multimap<K, V2> mapValues(Function<? super V, ? extends V2> var1);

    public Multimap<K, V> merge(Multimap<? extends K, ? extends V> var1);

    public <K2 extends K, V2 extends V> Multimap<K, V> merge(Multimap<K2, V2> var1, BiFunction<Traversable<V>, Traversable<V2>, Traversable<V>> var2);

    public Multimap<K, V> put(K var1, V var2);

    public Multimap<K, V> put(Tuple2<? extends K, ? extends V> var1);

    public Multimap<K, V> remove(K var1);

    public Multimap<K, V> remove(K var1, V var2);

    @Deprecated
    public Multimap<K, V> removeAll(BiPredicate<? super K, ? super V> var1);

    public Multimap<K, V> removeAll(Iterable<? extends K> var1);

    @Deprecated
    public Multimap<K, V> removeKeys(Predicate<? super K> var1);

    @Deprecated
    public Multimap<K, V> removeValues(Predicate<? super V> var1);

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

    public java.util.Map<K, Collection<V>> toJavaMap();

    default public <U> U transform(Function<? super Multimap<K, V>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
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

    public Traversable<V> values();

    @Override
    default public <U> Seq<Tuple2<Tuple2<K, V>, U>> zip(Iterable<? extends U> that) {
        return this.zipWith((Iterable)that, Tuple::of);
    }

    @Override
    default public <U> Seq<Tuple2<Tuple2<K, V>, U>> zipAll(Iterable<? extends U> that, Tuple2<K, V> thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        return Stream.ofAll(this.iterator().zipAll(that, thisElem, thatElem));
    }

    @Override
    default public <U, R> Seq<R> zipWith(Iterable<? extends U> that, BiFunction<? super Tuple2<K, V>, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return Stream.ofAll(this.iterator().zipWith(that, mapper));
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

    public Multimap<K, V> distinct();

    public Multimap<K, V> distinctBy(Comparator<? super Tuple2<K, V>> var1);

    public <U> Multimap<K, V> distinctBy(Function<? super Tuple2<K, V>, ? extends U> var1);

    public Multimap<K, V> drop(int var1);

    public Multimap<K, V> dropRight(int var1);

    public Multimap<K, V> dropUntil(Predicate<? super Tuple2<K, V>> var1);

    public Multimap<K, V> dropWhile(Predicate<? super Tuple2<K, V>> var1);

    public Multimap<K, V> filter(Predicate<? super Tuple2<K, V>> var1);

    public Multimap<K, V> reject(Predicate<? super Tuple2<K, V>> var1);

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

    @Override
    public <C> Map<C, ? extends Multimap<K, V>> groupBy(Function<? super Tuple2<K, V>, ? extends C> var1);

    @Override
    public Iterator<? extends Multimap<K, V>> grouped(int var1);

    public Multimap<K, V> init();

    @Override
    public Option<? extends Multimap<K, V>> initOption();

    public Multimap<K, V> orElse(Iterable<? extends Tuple2<K, V>> var1);

    public Multimap<K, V> orElse(Supplier<? extends Iterable<? extends Tuple2<K, V>>> var1);

    @Override
    public Tuple2<? extends Multimap<K, V>, ? extends Multimap<K, V>> partition(Predicate<? super Tuple2<K, V>> var1);

    public Multimap<K, V> peek(Consumer<? super Tuple2<K, V>> var1);

    public Multimap<K, V> replace(Tuple2<K, V> var1, Tuple2<K, V> var2);

    public Multimap<K, V> replaceAll(Tuple2<K, V> var1, Tuple2<K, V> var2);

    public Multimap<K, V> replaceValue(K var1, V var2);

    public Multimap<K, V> replace(K var1, V var2, V var3);

    public Multimap<K, V> replaceAll(BiFunction<? super K, ? super V, ? extends V> var1);

    public Multimap<K, V> retainAll(Iterable<? extends Tuple2<K, V>> var1);

    public Multimap<K, V> scan(Tuple2<K, V> var1, BiFunction<? super Tuple2<K, V>, ? super Tuple2<K, V>, ? extends Tuple2<K, V>> var2);

    @Override
    public Iterator<? extends Multimap<K, V>> slideBy(Function<? super Tuple2<K, V>, ?> var1);

    @Override
    public Iterator<? extends Multimap<K, V>> sliding(int var1);

    @Override
    public Iterator<? extends Multimap<K, V>> sliding(int var1, int var2);

    @Override
    public Tuple2<? extends Multimap<K, V>, ? extends Multimap<K, V>> span(Predicate<? super Tuple2<K, V>> var1);

    public Multimap<K, V> tail();

    @Override
    public Option<? extends Multimap<K, V>> tailOption();

    public Multimap<K, V> take(int var1);

    public Multimap<K, V> takeRight(int var1);

    public Multimap<K, V> takeUntil(Predicate<? super Tuple2<K, V>> var1);

    public Multimap<K, V> takeWhile(Predicate<? super Tuple2<K, V>> var1);

    public static enum ContainerType {
        SET((set, elem) -> ((Set)set).add(elem), (set, elem) -> ((Set)set).remove(elem), HashSet::new),
        SORTED_SET((set, elem) -> ((Set)set).add(elem), (set, elem) -> ((Set)set).remove(elem), TreeSet::new),
        SEQ((seq, elem) -> ((List)seq).append(elem), (seq, elem) -> ((List)seq).remove(elem), ArrayList::new);

        private final BiFunction<Traversable<?>, Object, Traversable<?>> add;
        private final BiFunction<Traversable<?>, Object, Traversable<?>> remove;
        private final Supplier<Collection<?>> instantiate;

        private ContainerType(BiFunction<Traversable<?>, Object, Traversable<?>> add, BiFunction<Traversable<?>, Object, Traversable<?>> remove, Supplier<Collection<?>> instantiate) {
            this.add = add;
            this.remove = remove;
            this.instantiate = instantiate;
        }

        <T> Traversable<T> add(Traversable<?> container, T elem) {
            return this.add.apply(container, elem);
        }

        <T> Traversable<T> remove(Traversable<?> container, T elem) {
            return this.remove.apply(container, elem);
        }

        <T> Collection<T> instantiate() {
            return this.instantiate.get();
        }
    }
}

