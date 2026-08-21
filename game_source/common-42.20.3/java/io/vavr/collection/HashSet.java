/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.PartialFunction;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.Collections;
import io.vavr.collection.GwtIncompatible;
import io.vavr.collection.HashArrayMappedTrie;
import io.vavr.collection.Iterator;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public final class HashSet<T>
implements Set<T>,
Serializable {
    private static final long serialVersionUID = 1L;
    private static final HashSet<?> EMPTY = new HashSet(HashArrayMappedTrie.empty());
    private final HashArrayMappedTrie<T, T> tree;

    private HashSet(HashArrayMappedTrie<T, T> tree) {
        this.tree = tree;
    }

    public static <T> HashSet<T> empty() {
        return EMPTY;
    }

    public static <T> Collector<T, ArrayList<T>, HashSet<T>> collector() {
        Supplier<ArrayList> supplier = ArrayList::new;
        BiConsumer<ArrayList, Object> accumulator = ArrayList::add;
        BinaryOperator combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        Function<ArrayList, HashSet> finisher = HashSet::ofAll;
        return Collector.of(supplier, accumulator, combiner, finisher, new Collector.Characteristics[0]);
    }

    public static <T> HashSet<T> narrow(HashSet<? extends T> hashSet) {
        return hashSet;
    }

    public static <T> HashSet<T> of(T element) {
        return HashSet.empty().add((Object)element);
    }

    @SafeVarargs
    public static <T> HashSet<T> of(T ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        HashArrayMappedTrie<T, T> tree = HashArrayMappedTrie.empty();
        for (T element : elements) {
            tree = tree.put(element, element);
        }
        return tree.isEmpty() ? HashSet.empty() : new HashSet(tree);
    }

    public static <T> HashSet<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return Collections.tabulate(n, f, HashSet.empty(), HashSet::of);
    }

    public static <T> HashSet<T> fill(int n, Supplier<? extends T> s) {
        Objects.requireNonNull(s, "s is null");
        return Collections.fill(n, s, HashSet.empty(), HashSet::of);
    }

    public static <T> HashSet<T> ofAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (elements instanceof HashSet) {
            return (HashSet)elements;
        }
        HashArrayMappedTrie<T, T> tree = HashSet.addAll(HashArrayMappedTrie.empty(), elements);
        return tree.isEmpty() ? HashSet.empty() : new HashSet<T>(tree);
    }

    public static <T> HashSet<T> ofAll(Stream<? extends T> javaStream) {
        Objects.requireNonNull(javaStream, "javaStream is null");
        return HashSet.ofAll(Iterator.ofAll(javaStream.iterator()));
    }

    public static HashSet<Boolean> ofAll(boolean ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return HashSet.ofAll(Iterator.ofAll(elements));
    }

    public static HashSet<Byte> ofAll(byte ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return HashSet.ofAll(Iterator.ofAll(elements));
    }

    public static HashSet<Character> ofAll(char ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return HashSet.ofAll(Iterator.ofAll(elements));
    }

    public static HashSet<Double> ofAll(double ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return HashSet.ofAll(Iterator.ofAll(elements));
    }

    public static HashSet<Float> ofAll(float ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return HashSet.ofAll(Iterator.ofAll(elements));
    }

    public static HashSet<Integer> ofAll(int ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return HashSet.ofAll(Iterator.ofAll(elements));
    }

    public static HashSet<Long> ofAll(long ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return HashSet.ofAll(Iterator.ofAll(elements));
    }

    public static HashSet<Short> ofAll(short ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return HashSet.ofAll(Iterator.ofAll(elements));
    }

    public static HashSet<Integer> range(int from, int toExclusive) {
        return HashSet.ofAll(Iterator.range(from, toExclusive));
    }

    public static HashSet<Character> range(char from, char toExclusive) {
        return HashSet.ofAll(Iterator.range(from, toExclusive));
    }

    public static HashSet<Integer> rangeBy(int from, int toExclusive, int step) {
        return HashSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static HashSet<Character> rangeBy(char from, char toExclusive, int step) {
        return HashSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    @GwtIncompatible
    public static HashSet<Double> rangeBy(double from, double toExclusive, double step) {
        return HashSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static HashSet<Long> range(long from, long toExclusive) {
        return HashSet.ofAll(Iterator.range(from, toExclusive));
    }

    public static HashSet<Long> rangeBy(long from, long toExclusive, long step) {
        return HashSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static HashSet<Integer> rangeClosed(int from, int toInclusive) {
        return HashSet.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static HashSet<Character> rangeClosed(char from, char toInclusive) {
        return HashSet.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static HashSet<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return HashSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static HashSet<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return HashSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    @GwtIncompatible
    public static HashSet<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return HashSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static HashSet<Long> rangeClosed(long from, long toInclusive) {
        return HashSet.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static HashSet<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return HashSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    @Override
    public HashSet<T> add(T element) {
        return this.contains(element) ? this : new HashSet<T>(this.tree.put(element, element));
    }

    @Override
    public HashSet<T> addAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (this.isEmpty() && elements instanceof HashSet) {
            HashSet set = (HashSet)elements;
            return set;
        }
        HashArrayMappedTrie<T, T> that = HashSet.addAll(this.tree, elements);
        if (that.size() == this.tree.size()) {
            return this;
        }
        return new HashSet<T>(that);
    }

    @Override
    public <R> HashSet<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        return HashSet.ofAll(this.iterator().collect(partialFunction));
    }

    @Override
    public boolean contains(T element) {
        return this.tree.get(element).isDefined();
    }

    @Override
    public HashSet<T> diff(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (this.isEmpty() || elements.isEmpty()) {
            return this;
        }
        return this.removeAll(elements);
    }

    @Override
    public HashSet<T> distinct() {
        return this;
    }

    @Override
    public HashSet<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return HashSet.ofAll(this.iterator().distinctBy(comparator));
    }

    @Override
    public <U> HashSet<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        return HashSet.ofAll(this.iterator().distinctBy(keyExtractor));
    }

    @Override
    public HashSet<T> drop(int n) {
        if (n <= 0) {
            return this;
        }
        return HashSet.ofAll(this.iterator().drop(n));
    }

    @Override
    public HashSet<T> dropRight(int n) {
        return this.drop(n);
    }

    @Override
    public HashSet<T> dropUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.dropWhile((Predicate)predicate.negate());
    }

    @Override
    public HashSet<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        HashSet<T> dropped = HashSet.ofAll(this.iterator().dropWhile(predicate));
        return dropped.length() == this.length() ? this : dropped;
    }

    @Override
    public HashSet<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        HashSet<T> filtered = HashSet.ofAll(this.iterator().filter(predicate));
        if (filtered.isEmpty()) {
            return HashSet.empty();
        }
        if (filtered.length() == this.length()) {
            return this;
        }
        return filtered;
    }

    @Override
    public HashSet<T> reject(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.filter((Predicate)predicate.negate());
    }

    @Override
    public <U> HashSet<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (this.isEmpty()) {
            return HashSet.empty();
        }
        HashArrayMappedTrie that = this.foldLeft(HashArrayMappedTrie.empty(), (tree, t) -> HashSet.addAll(tree, (Iterable)mapper.apply(t)));
        return new HashSet<T>(that);
    }

    @Override
    public <U> U foldRight(U zero, BiFunction<? super T, ? super U, ? extends U> f) {
        return (U)this.foldLeft(zero, (u, t) -> f.apply(t, u));
    }

    @Override
    public <C> Map<C, HashSet<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return Collections.groupBy(this, classifier, HashSet::ofAll);
    }

    @Override
    public Iterator<HashSet<T>> grouped(int size) {
        return this.sliding(size, size);
    }

    @Override
    public boolean hasDefiniteSize() {
        return true;
    }

    @Override
    public T head() {
        if (this.tree.isEmpty()) {
            throw new NoSuchElementException("head of empty set");
        }
        return (T)this.iterator().next();
    }

    @Override
    public Option<T> headOption() {
        return this.iterator().headOption();
    }

    @Override
    public HashSet<T> init() {
        return this.tail();
    }

    @Override
    public Option<HashSet<T>> initOption() {
        return this.tailOption();
    }

    @Override
    public HashSet<T> intersect(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (this.isEmpty() || elements.isEmpty()) {
            return HashSet.empty();
        }
        int size = this.size();
        if (size <= elements.size()) {
            return this.retainAll(elements);
        }
        Set results = HashSet.ofAll(elements).retainAll((Iterable)this);
        return size == results.size() ? this : results;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.tree.isEmpty();
    }

    @Override
    public boolean isLazy() {
        return false;
    }

    @Override
    public boolean isTraversableAgain() {
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return this.tree.keysIterator();
    }

    @Override
    public T last() {
        return Collections.last(this);
    }

    @Override
    public int length() {
        return this.tree.size();
    }

    @Override
    public <U> HashSet<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (this.isEmpty()) {
            return HashSet.empty();
        }
        HashArrayMappedTrie that = this.foldLeft(HashArrayMappedTrie.empty(), (tree, t) -> {
            Object u = mapper.apply(t);
            return tree.put(u, u);
        });
        return new HashSet<T>(that);
    }

    @Override
    public String mkString(CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
        return this.iterator().mkString(prefix, delimiter, suffix);
    }

    @Override
    public HashSet<T> orElse(Iterable<? extends T> other) {
        return this.isEmpty() ? HashSet.ofAll(other) : this;
    }

    @Override
    public HashSet<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return this.isEmpty() ? HashSet.ofAll(supplier.get()) : this;
    }

    @Override
    public Tuple2<HashSet<T>, HashSet<T>> partition(Predicate<? super T> predicate) {
        return Collections.partition(this, HashSet::ofAll, predicate);
    }

    @Override
    public HashSet<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (!this.isEmpty()) {
            action.accept(this.iterator().head());
        }
        return this;
    }

    @Override
    public HashSet<T> remove(T element) {
        HashArrayMappedTrie<T, T> newTree = this.tree.remove(element);
        return newTree == this.tree ? this : new HashSet<T>(newTree);
    }

    @Override
    public HashSet<T> removeAll(Iterable<? extends T> elements) {
        return Collections.removeAll(this, elements);
    }

    @Override
    public HashSet<T> replace(T currentElement, T newElement) {
        if (this.tree.containsKey(currentElement)) {
            return ((HashSet)this.remove((Object)currentElement)).add((Object)newElement);
        }
        return this;
    }

    @Override
    public HashSet<T> replaceAll(T currentElement, T newElement) {
        return this.replace((Object)currentElement, (Object)newElement);
    }

    @Override
    public HashSet<T> retainAll(Iterable<? extends T> elements) {
        return Collections.retainAll(this, elements);
    }

    @Override
    public HashSet<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        return this.scanLeft(zero, operation);
    }

    @Override
    public <U> HashSet<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        return Collections.scanLeft(this, zero, operation, HashSet::ofAll);
    }

    @Override
    public <U> HashSet<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        return Collections.scanRight(this, zero, operation, HashSet::ofAll);
    }

    @Override
    public Iterator<HashSet<T>> slideBy(Function<? super T, ?> classifier) {
        return this.iterator().slideBy(classifier).map(HashSet::ofAll);
    }

    @Override
    public Iterator<HashSet<T>> sliding(int size) {
        return this.sliding(size, 1);
    }

    @Override
    public Iterator<HashSet<T>> sliding(int size, int step) {
        return this.iterator().sliding(size, step).map(HashSet::ofAll);
    }

    @Override
    public Tuple2<HashSet<T>, HashSet<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        Tuple2<Iterator<? super T>, Iterator<? super T>> t = this.iterator().span(predicate);
        return Tuple.of(HashSet.ofAll((Iterable)t._1), HashSet.ofAll((Iterable)t._2));
    }

    @Override
    public HashSet<T> tail() {
        if (this.tree.isEmpty()) {
            throw new UnsupportedOperationException("tail of empty set");
        }
        return this.remove((Object)this.head());
    }

    @Override
    public Option<HashSet<T>> tailOption() {
        if (this.tree.isEmpty()) {
            return Option.none();
        }
        return Option.some(this.tail());
    }

    @Override
    public HashSet<T> take(int n) {
        if (n >= this.size() || this.isEmpty()) {
            return this;
        }
        if (n <= 0) {
            return HashSet.empty();
        }
        return HashSet.ofAll(() -> this.iterator().take(n));
    }

    @Override
    public HashSet<T> takeRight(int n) {
        return this.take(n);
    }

    @Override
    public HashSet<T> takeUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.takeWhile((Predicate)predicate.negate());
    }

    @Override
    public HashSet<T> takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        HashSet<T> taken = HashSet.ofAll(this.iterator().takeWhile(predicate));
        return taken.length() == this.length() ? this : taken;
    }

    public <U> U transform(Function<? super HashSet<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    public java.util.HashSet<T> toJavaSet() {
        return this.toJavaSet(java.util.HashSet::new);
    }

    @Override
    public HashSet<T> union(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (this.isEmpty()) {
            if (elements instanceof HashSet) {
                return (HashSet)elements;
            }
            return HashSet.ofAll(elements);
        }
        if (elements.isEmpty()) {
            return this;
        }
        HashArrayMappedTrie<T, T> that = HashSet.addAll(this.tree, elements);
        if (that.size() == this.tree.size()) {
            return this;
        }
        return new HashSet<T>(that);
    }

    @Override
    public <T1, T2> Tuple2<HashSet<T1>, HashSet<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        Tuple2<Iterator<? extends T1>, Iterator<? extends T2>> t = this.iterator().unzip(unzipper);
        return Tuple.of(HashSet.ofAll((Iterable)t._1), HashSet.ofAll((Iterable)t._2));
    }

    @Override
    public <T1, T2, T3> Tuple3<HashSet<T1>, HashSet<T2>, HashSet<T3>> unzip3(Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        Tuple3<Iterator<? extends T1>, Iterator<? extends T2>, Iterator<? extends T3>> t = this.iterator().unzip3(unzipper);
        return Tuple.of(HashSet.ofAll((Iterable)t._1), HashSet.ofAll((Iterable)t._2), HashSet.ofAll((Iterable)t._3));
    }

    @Override
    public <U> HashSet<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        return this.zipWith((Iterable)that, Tuple::of);
    }

    @Override
    public <U, R> HashSet<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return HashSet.ofAll(this.iterator().zipWith(that, mapper));
    }

    @Override
    public <U> HashSet<Tuple2<T, U>> zipAll(Iterable<? extends U> that, T thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        return HashSet.ofAll(this.iterator().zipAll(that, thisElem, thatElem));
    }

    @Override
    public HashSet<Tuple2<T, Integer>> zipWithIndex() {
        return this.zipWithIndex(Tuple::of);
    }

    @Override
    public <U> HashSet<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return HashSet.ofAll(this.iterator().zipWithIndex(mapper));
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
        return "HashSet";
    }

    @Override
    public String toString() {
        return this.mkString(this.stringPrefix() + "(", ", ", ")");
    }

    private static <T> HashArrayMappedTrie<T, T> addAll(HashArrayMappedTrie<T, T> initial, Iterable<? extends T> additional) {
        HashArrayMappedTrie<T, T> that = initial;
        for (T t : additional) {
            that = that.put(t, t);
        }
        return that;
    }

    @GwtIncompatible(value="The Java serialization protocol is explicitly not supported")
    private Object writeReplace() {
        return new SerializationProxy<T>(this.tree);
    }

    @GwtIncompatible(value="The Java serialization protocol is explicitly not supported")
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    @GwtIncompatible(value="The Java serialization protocol is explicitly not supported")
    private static final class SerializationProxy<T>
    implements Serializable {
        private static final long serialVersionUID = 1L;
        private transient HashArrayMappedTrie<T, T> tree;

        SerializationProxy(HashArrayMappedTrie<T, T> tree) {
            this.tree = tree;
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            s.writeInt(this.tree.size());
            for (Tuple2 tuple2 : this.tree) {
                s.writeObject(tuple2._1);
            }
        }

        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            s.defaultReadObject();
            int size = s.readInt();
            if (size < 0) {
                throw new InvalidObjectException("No elements");
            }
            HashArrayMappedTrie<Object, Object> temp = HashArrayMappedTrie.empty();
            for (int i = 0; i < size; ++i) {
                Object element = s.readObject();
                temp = temp.put(element, element);
            }
            this.tree = temp;
        }

        private Object readResolve() {
            return this.tree.isEmpty() ? HashSet.empty() : new HashSet(this.tree);
        }
    }
}

