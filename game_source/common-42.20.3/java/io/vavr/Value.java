/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.API;
import io.vavr.CheckedFunction0;
import io.vavr.GwtIncompatible;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.ValueModule;
import io.vavr.collection.Array;
import io.vavr.collection.CharSeq;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Iterator;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.LinkedHashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Ordered;
import io.vavr.collection.PriorityQueue;
import io.vavr.collection.Queue;
import io.vavr.collection.Set;
import io.vavr.collection.SortedMap;
import io.vavr.collection.SortedSet;
import io.vavr.collection.Stream;
import io.vavr.collection.Traversable;
import io.vavr.collection.Tree;
import io.vavr.collection.TreeMap;
import io.vavr.collection.TreeSet;
import io.vavr.collection.Vector;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.StreamSupport;

public interface Value<T>
extends Iterable<T> {
    public static <T> Value<T> narrow(Value<? extends T> value) {
        return value;
    }

    default public <R, A> R collect(Collector<? super T, A, R> collector) {
        return StreamSupport.stream(this.spliterator(), false).collect(collector);
    }

    default public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return StreamSupport.stream(this.spliterator(), false).collect(supplier, accumulator, combiner);
    }

    default public boolean contains(T element) {
        return this.exists(e -> Objects.equals(e, element));
    }

    default public <U> boolean corresponds(Iterable<U> that, BiPredicate<? super T, ? super U> predicate) {
        java.util.Iterator it1 = this.iterator();
        java.util.Iterator<U> it2 = that.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            if (predicate.test(it1.next(), it2.next())) continue;
            return false;
        }
        return !it1.hasNext() && !it2.hasNext();
    }

    default public boolean eq(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Value) {
            Value that = (Value)o;
            return this.iterator().corresponds((Iterable)((Object)that.iterator()), (BiPredicate)(o1, o2) -> {
                if (o1 instanceof Value) {
                    return ((Value)o1).eq(o2);
                }
                if (o2 instanceof Value) {
                    return ((Value)o2).eq(o1);
                }
                return Objects.equals(o1, o2);
            });
        }
        if (o instanceof Iterable) {
            Iterator that = Iterator.ofAll((Iterable)o);
            return this.eq(that);
        }
        return false;
    }

    default public boolean exists(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        for (Object t : this) {
            if (!predicate.test(t)) continue;
            return true;
        }
        return false;
    }

    default public boolean forAll(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return !this.exists(predicate.negate());
    }

    @Override
    default public void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        for (Object t : this) {
            action.accept(t);
        }
    }

    public T get();

    default public T getOrElse(T other) {
        return this.isEmpty() ? other : this.get();
    }

    default public T getOrElse(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return this.isEmpty() ? supplier.get() : this.get();
    }

    default public <X extends Throwable> T getOrElseThrow(Supplier<X> supplier) throws X {
        Objects.requireNonNull(supplier, "supplier is null");
        if (this.isEmpty()) {
            throw (Throwable)supplier.get();
        }
        return this.get();
    }

    default public T getOrElseTry(CheckedFunction0<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return this.isEmpty() ? Try.of(supplier).get() : this.get();
    }

    default public T getOrNull() {
        return this.isEmpty() ? null : (T)this.get();
    }

    public boolean isAsync();

    public boolean isEmpty();

    public boolean isLazy();

    public boolean isSingleValued();

    public <U> Value<U> map(Function<? super T, ? extends U> var1);

    public Value<T> peek(Consumer<? super T> var1);

    public String stringPrefix();

    @GwtIncompatible(value="java.io.PrintStream is not implemented")
    default public void out(PrintStream out) {
        for (Object t : this) {
            out.println(String.valueOf(t));
            if (!out.checkError()) continue;
            throw new IllegalStateException("Error writing to PrintStream");
        }
    }

    @GwtIncompatible(value="java.io.PrintWriter is not implemented")
    default public void out(PrintWriter writer) {
        for (Object t : this) {
            writer.println(String.valueOf(t));
            if (!writer.checkError()) continue;
            throw new IllegalStateException("Error writing to PrintWriter");
        }
    }

    @GwtIncompatible(value="java.io.PrintStream is not implemented")
    default public void stderr() {
        this.out(System.err);
    }

    @GwtIncompatible(value="java.io.PrintStream is not implemented")
    default public void stdout() {
        this.out(System.out);
    }

    @Override
    public Iterator<T> iterator();

    default public Array<T> toArray() {
        return ValueModule.toTraversable(this, Array.empty(), Array::of, Array::ofAll);
    }

    default public CharSeq toCharSeq() {
        if (this instanceof CharSeq) {
            return (CharSeq)this;
        }
        if (this.isEmpty()) {
            return CharSeq.empty();
        }
        return CharSeq.of(this.iterator().mkString());
    }

    @GwtIncompatible
    default public CompletableFuture<T> toCompletableFuture() {
        CompletableFuture completableFuture = new CompletableFuture();
        Try.of(this::get).onSuccess(completableFuture::complete).onFailure(completableFuture::completeExceptionally);
        return completableFuture;
    }

    @Deprecated
    default public <U> Validation<T, U> toInvalid(U value) {
        return this.isEmpty() ? Validation.valid(value) : Validation.invalid(this.get());
    }

    @Deprecated
    default public <U> Validation<T, U> toInvalid(Supplier<? extends U> valueSupplier) {
        Objects.requireNonNull(valueSupplier, "valueSupplier is null");
        return this.isEmpty() ? Validation.valid(valueSupplier.get()) : Validation.invalid(this.get());
    }

    default public Object[] toJavaArray() {
        if (this instanceof Traversable && ((Traversable)this).isTraversableAgain()) {
            Object[] results = new Object[((Traversable)this).size()];
            java.util.Iterator iter = this.iterator();
            Arrays.setAll(results, arg_0 -> Value.lambda$toJavaArray$2((Iterator)iter, arg_0));
            return results;
        }
        return this.toJavaList().toArray();
    }

    @Deprecated
    @GwtIncompatible(value="reflection is not supported")
    default public T[] toJavaArray(Class<T> componentType) {
        Objects.requireNonNull(componentType, "componentType is null");
        if (componentType.isPrimitive()) {
            Class<Boolean> boxedType = componentType == Boolean.TYPE ? Boolean.class : (componentType == Byte.TYPE ? Byte.class : (componentType == Character.TYPE ? Character.class : (componentType == Double.TYPE ? Double.class : (componentType == Float.TYPE ? Float.class : (componentType == Integer.TYPE ? Integer.class : (componentType == Long.TYPE ? Long.class : (componentType == Short.TYPE ? Short.class : (componentType == Void.TYPE ? Void.class : null))))))));
            componentType = boxedType;
        }
        java.util.List<Object> list = this.toJavaList();
        return list.toArray((Object[])java.lang.reflect.Array.newInstance(componentType, list.size()));
    }

    default public T[] toJavaArray(IntFunction<T[]> arrayFactory) {
        java.util.List<T> javaList = this.toJavaList();
        return javaList.toArray(arrayFactory.apply(javaList.size()));
    }

    default public <C extends Collection<T>> C toJavaCollection(Function<Integer, C> factory2) {
        return ValueModule.toJavaCollection(this, factory2);
    }

    default public java.util.List<T> toJavaList() {
        return ValueModule.toJavaCollection(this, ArrayList::new, 10);
    }

    default public <LIST extends java.util.List<T>> LIST toJavaList(Function<Integer, LIST> factory2) {
        return (LIST)((java.util.List)ValueModule.toJavaCollection(this, factory2));
    }

    default public <K, V> java.util.Map<K, V> toJavaMap(Function<? super T, ? extends Tuple2<? extends K, ? extends V>> f) {
        return this.toJavaMap(java.util.HashMap::new, f);
    }

    default public <K, V, MAP extends java.util.Map<K, V>> MAP toJavaMap(Supplier<MAP> factory2, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return this.toJavaMap(factory2, t -> Tuple.of(keyMapper.apply(t), valueMapper.apply(t)));
    }

    default public <K, V, MAP extends java.util.Map<K, V>> MAP toJavaMap(Supplier<MAP> factory2, Function<? super T, ? extends Tuple2<? extends K, ? extends V>> f) {
        Objects.requireNonNull(f, "f is null");
        java.util.Map map = (java.util.Map)factory2.get();
        if (!this.isEmpty()) {
            if (this.isSingleValued()) {
                Tuple2<? extends K, ? extends V> entry = f.apply(this.get());
                map.put(entry._1, entry._2);
            } else {
                for (Object a : this) {
                    Tuple2<? extends K, ? extends V> entry = f.apply(a);
                    map.put(entry._1, entry._2);
                }
            }
        }
        return (MAP)map;
    }

    default public Optional<T> toJavaOptional() {
        return this.isEmpty() ? Optional.empty() : Optional.ofNullable(this.get());
    }

    default public java.util.Set<T> toJavaSet() {
        return ValueModule.toJavaCollection(this, java.util.HashSet::new, 16);
    }

    default public <SET extends java.util.Set<T>> SET toJavaSet(Function<Integer, SET> factory2) {
        return (SET)((java.util.Set)ValueModule.toJavaCollection(this, factory2));
    }

    default public java.util.stream.Stream<T> toJavaStream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    default public java.util.stream.Stream<T> toJavaParallelStream() {
        return StreamSupport.stream(this.spliterator(), true);
    }

    @Deprecated
    default public <R> Either<T, R> toLeft(R right) {
        return this.isEmpty() ? Either.right(right) : Either.left(this.get());
    }

    @Deprecated
    default public <R> Either<T, R> toLeft(Supplier<? extends R> right) {
        Objects.requireNonNull(right, "right is null");
        return this.isEmpty() ? Either.right(right.get()) : Either.left(this.get());
    }

    default public List<T> toList() {
        return ValueModule.toTraversable(this, List.empty(), List::of, List::ofAll);
    }

    default public <K, V> Map<K, V> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return this.toMap(t -> Tuple.of(keyMapper.apply(t), valueMapper.apply(t)));
    }

    default public <K, V> Map<K, V> toMap(Function<? super T, ? extends Tuple2<? extends K, ? extends V>> f) {
        Objects.requireNonNull(f, "f is null");
        Function<Tuple2, Map> ofElement = HashMap::of;
        Function<Iterable, Map> ofAll = HashMap::ofEntries;
        return ValueModule.toMap(this, HashMap.empty(), ofElement, ofAll, f);
    }

    default public <K, V> Map<K, V> toLinkedMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return this.toLinkedMap(t -> Tuple.of(keyMapper.apply(t), valueMapper.apply(t)));
    }

    default public <K, V> Map<K, V> toLinkedMap(Function<? super T, ? extends Tuple2<? extends K, ? extends V>> f) {
        Objects.requireNonNull(f, "f is null");
        Function<Tuple2, Map> ofElement = LinkedHashMap::of;
        Function<Iterable, Map> ofAll = LinkedHashMap::ofEntries;
        return ValueModule.toMap(this, LinkedHashMap.empty(), ofElement, ofAll, f);
    }

    default public <K extends Comparable<? super K>, V> SortedMap<K, V> toSortedMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return this.toSortedMap(t -> Tuple.of(keyMapper.apply(t), valueMapper.apply(t)));
    }

    default public <K extends Comparable<? super K>, V> SortedMap<K, V> toSortedMap(Function<? super T, ? extends Tuple2<? extends K, ? extends V>> f) {
        Objects.requireNonNull(f, "f is null");
        return this.toSortedMap(Comparator.naturalOrder(), f);
    }

    default public <K, V> SortedMap<K, V> toSortedMap(Comparator<? super K> comparator, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return this.toSortedMap(comparator, (? super T t) -> Tuple.of(keyMapper.apply(t), valueMapper.apply(t)));
    }

    default public <K, V> SortedMap<K, V> toSortedMap(Comparator<? super K> comparator, Function<? super T, ? extends Tuple2<? extends K, ? extends V>> f) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(f, "f is null");
        Function<Tuple2, SortedMap> ofElement = t -> TreeMap.of(comparator, t);
        Function<Iterable, SortedMap> ofAll = t -> TreeMap.ofEntries(comparator, t);
        return ValueModule.toMap(this, TreeMap.empty(comparator), ofElement, ofAll, f);
    }

    default public Option<T> toOption() {
        if (this instanceof Option) {
            return (Option)this;
        }
        return this.isEmpty() ? Option.none() : Option.some(this.get());
    }

    default public <L> Either<L, T> toEither(L left) {
        if (this instanceof Either) {
            return ((Either)this).mapLeft(ignored -> left);
        }
        return this.isEmpty() ? API.Left(left) : API.Right(this.get());
    }

    default public <L> Either<L, T> toEither(Supplier<? extends L> leftSupplier) {
        Objects.requireNonNull(leftSupplier, "leftSupplier is null");
        if (this instanceof Either) {
            return ((Either)this).mapLeft(ignored -> leftSupplier.get());
        }
        return this.isEmpty() ? API.Left(leftSupplier.get()) : API.Right(this.get());
    }

    default public <E> Validation<E, T> toValidation(E invalid) {
        if (this instanceof Validation) {
            return ((Validation)this).mapError(ignored -> invalid);
        }
        return this.isEmpty() ? API.Invalid(invalid) : API.Valid(this.get());
    }

    default public <E> Validation<E, T> toValidation(Supplier<? extends E> invalidSupplier) {
        Objects.requireNonNull(invalidSupplier, "invalidSupplier is null");
        if (this instanceof Validation) {
            return ((Validation)this).mapError(ignored -> invalidSupplier.get());
        }
        return this.isEmpty() ? API.Invalid(invalidSupplier.get()) : API.Valid(this.get());
    }

    default public Queue<T> toQueue() {
        return ValueModule.toTraversable(this, Queue.empty(), Queue::of, Queue::ofAll);
    }

    default public PriorityQueue<T> toPriorityQueue() {
        if (this instanceof PriorityQueue) {
            return (PriorityQueue)this;
        }
        Comparator comparator = this instanceof Ordered ? ((Ordered)((Object)this)).comparator() : Comparator.naturalOrder();
        return this.toPriorityQueue(comparator);
    }

    default public PriorityQueue<T> toPriorityQueue(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        PriorityQueue<? super T> empty = PriorityQueue.empty(comparator);
        Function<Object, PriorityQueue> of = value -> PriorityQueue.of(comparator, value);
        Function<Iterable, PriorityQueue> ofAll = values2 -> PriorityQueue.ofAll(comparator, values2);
        return ValueModule.toTraversable(this, empty, of, ofAll);
    }

    @Deprecated
    default public <L> Either<L, T> toRight(L left) {
        return this.isEmpty() ? Either.left(left) : Either.right(this.get());
    }

    @Deprecated
    default public <L> Either<L, T> toRight(Supplier<? extends L> left) {
        Objects.requireNonNull(left, "left is null");
        return this.isEmpty() ? Either.left(left.get()) : Either.right(this.get());
    }

    default public Set<T> toSet() {
        return ValueModule.toTraversable(this, HashSet.empty(), HashSet::of, HashSet::ofAll);
    }

    default public Set<T> toLinkedSet() {
        return ValueModule.toTraversable(this, LinkedHashSet.empty(), LinkedHashSet::of, LinkedHashSet::ofAll);
    }

    default public SortedSet<T> toSortedSet() throws ClassCastException {
        if (this instanceof TreeSet) {
            return (TreeSet)this;
        }
        Comparator comparator = this instanceof Ordered ? ((Ordered)((Object)this)).comparator() : Comparator.naturalOrder();
        return this.toSortedSet(comparator);
    }

    default public SortedSet<T> toSortedSet(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return ValueModule.toTraversable(this, TreeSet.empty(comparator), value -> TreeSet.of(comparator, value), values2 -> TreeSet.ofAll(comparator, values2));
    }

    default public Stream<T> toStream() {
        return ValueModule.toTraversable(this, Stream.empty(), Stream::of, Stream::ofAll);
    }

    default public Try<T> toTry() {
        if (this instanceof Try) {
            return (Try)this;
        }
        return Try.of(this::get);
    }

    default public Try<T> toTry(Supplier<? extends Throwable> ifEmpty2) {
        Objects.requireNonNull(ifEmpty2, "ifEmpty is null");
        return this.isEmpty() ? Try.failure(ifEmpty2.get()) : this.toTry();
    }

    default public Tree<T> toTree() {
        return ValueModule.toTraversable(this, Tree.empty(), Tree::of, Tree::ofAll);
    }

    default public <ID> List<Tree.Node<T>> toTree(Function<? super T, ? extends ID> idMapper, Function<? super T, ? extends ID> parentMapper) {
        return Tree.build(this, idMapper, parentMapper);
    }

    @Deprecated
    default public <E> Validation<E, T> toValid(E error) {
        return this.isEmpty() ? Validation.invalid(error) : Validation.valid(this.get());
    }

    @Deprecated
    default public <E> Validation<E, T> toValid(Supplier<? extends E> errorSupplier) {
        Objects.requireNonNull(errorSupplier, "errorSupplier is null");
        return this.isEmpty() ? Validation.invalid(errorSupplier.get()) : Validation.valid(this.get());
    }

    default public Vector<T> toVector() {
        return ValueModule.toTraversable(this, Vector.empty(), Vector::of, Vector::ofAll);
    }

    @Override
    default public Spliterator<T> spliterator() {
        return Spliterators.spliterator(this.iterator(), this.isEmpty() ? 0L : 1L, 17488);
    }

    public boolean equals(Object var1);

    public int hashCode();

    public String toString();

    private static /* synthetic */ Object lambda$toJavaArray$2(Iterator iter, int i) {
        return iter.next();
    }
}

