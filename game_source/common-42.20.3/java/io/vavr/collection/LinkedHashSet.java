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
import io.vavr.collection.Iterator;
import io.vavr.collection.LinkedHashMap;
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

public final class LinkedHashSet<T>
implements Set<T>,
Serializable {
    private static final long serialVersionUID = 1L;
    private static final LinkedHashSet<?> EMPTY = new LinkedHashSet(LinkedHashMap.empty());
    private final LinkedHashMap<T, Object> map;

    private LinkedHashSet(LinkedHashMap<T, Object> map) {
        this.map = map;
    }

    public static <T> LinkedHashSet<T> empty() {
        return EMPTY;
    }

    static <T> LinkedHashSet<T> wrap(LinkedHashMap<T, Object> map) {
        return new LinkedHashSet<T>(map);
    }

    public static <T> Collector<T, ArrayList<T>, LinkedHashSet<T>> collector() {
        Supplier<ArrayList> supplier = ArrayList::new;
        BiConsumer<ArrayList, Object> accumulator = ArrayList::add;
        BinaryOperator combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        Function<ArrayList, LinkedHashSet> finisher = LinkedHashSet::ofAll;
        return Collector.of(supplier, accumulator, combiner, finisher, new Collector.Characteristics[0]);
    }

    public static <T> LinkedHashSet<T> narrow(LinkedHashSet<? extends T> linkedHashSet) {
        return linkedHashSet;
    }

    public static <T> LinkedHashSet<T> of(T element) {
        return LinkedHashSet.empty().add((Object)element);
    }

    @SafeVarargs
    public static <T> LinkedHashSet<T> of(T ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        Map map = LinkedHashMap.empty();
        for (T element : elements) {
            map = map.put(element, element);
        }
        return map.isEmpty() ? LinkedHashSet.empty() : new LinkedHashSet(map);
    }

    public static <T> LinkedHashSet<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return Collections.tabulate(n, f, LinkedHashSet.empty(), LinkedHashSet::of);
    }

    public static <T> LinkedHashSet<T> fill(int n, Supplier<? extends T> s) {
        Objects.requireNonNull(s, "s is null");
        return Collections.fill(n, s, LinkedHashSet.empty(), LinkedHashSet::of);
    }

    public static <T> LinkedHashSet<T> ofAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (elements instanceof LinkedHashSet) {
            return (LinkedHashSet)elements;
        }
        LinkedHashMap<T, Object> mao = LinkedHashSet.addAll(LinkedHashMap.empty(), elements);
        return mao.isEmpty() ? LinkedHashSet.empty() : new LinkedHashSet<T>(mao);
    }

    public static <T> LinkedHashSet<T> ofAll(Stream<? extends T> javaStream) {
        Objects.requireNonNull(javaStream, "javaStream is null");
        return LinkedHashSet.ofAll(Iterator.ofAll(javaStream.iterator()));
    }

    public static LinkedHashSet<Boolean> ofAll(boolean ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return LinkedHashSet.ofAll(Iterator.ofAll(elements));
    }

    public static LinkedHashSet<Byte> ofAll(byte ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return LinkedHashSet.ofAll(Iterator.ofAll(elements));
    }

    public static LinkedHashSet<Character> ofAll(char ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return LinkedHashSet.ofAll(Iterator.ofAll(elements));
    }

    public static LinkedHashSet<Double> ofAll(double ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return LinkedHashSet.ofAll(Iterator.ofAll(elements));
    }

    public static LinkedHashSet<Float> ofAll(float ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return LinkedHashSet.ofAll(Iterator.ofAll(elements));
    }

    public static LinkedHashSet<Integer> ofAll(int ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return LinkedHashSet.ofAll(Iterator.ofAll(elements));
    }

    public static LinkedHashSet<Long> ofAll(long ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return LinkedHashSet.ofAll(Iterator.ofAll(elements));
    }

    public static LinkedHashSet<Short> ofAll(short ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return LinkedHashSet.ofAll(Iterator.ofAll(elements));
    }

    public static LinkedHashSet<Integer> range(int from, int toExclusive) {
        return LinkedHashSet.ofAll(Iterator.range(from, toExclusive));
    }

    public static LinkedHashSet<Character> range(char from, char toExclusive) {
        return LinkedHashSet.ofAll(Iterator.range(from, toExclusive));
    }

    public static LinkedHashSet<Integer> rangeBy(int from, int toExclusive, int step) {
        return LinkedHashSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static LinkedHashSet<Character> rangeBy(char from, char toExclusive, int step) {
        return LinkedHashSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    @GwtIncompatible
    public static LinkedHashSet<Double> rangeBy(double from, double toExclusive, double step) {
        return LinkedHashSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static LinkedHashSet<Long> range(long from, long toExclusive) {
        return LinkedHashSet.ofAll(Iterator.range(from, toExclusive));
    }

    public static LinkedHashSet<Long> rangeBy(long from, long toExclusive, long step) {
        return LinkedHashSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static LinkedHashSet<Integer> rangeClosed(int from, int toInclusive) {
        return LinkedHashSet.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static LinkedHashSet<Character> rangeClosed(char from, char toInclusive) {
        return LinkedHashSet.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static LinkedHashSet<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return LinkedHashSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static LinkedHashSet<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return LinkedHashSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    @GwtIncompatible
    public static LinkedHashSet<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return LinkedHashSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static LinkedHashSet<Long> rangeClosed(long from, long toInclusive) {
        return LinkedHashSet.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static LinkedHashSet<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return LinkedHashSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    @Override
    public LinkedHashSet<T> add(T element) {
        return this.contains(element) ? this : new LinkedHashSet<T>(this.map.put((Object)element, (Object)element));
    }

    @Override
    public LinkedHashSet<T> addAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (this.isEmpty() && elements instanceof LinkedHashSet) {
            LinkedHashSet set = (LinkedHashSet)elements;
            return set;
        }
        LinkedHashMap<T, Object> that = LinkedHashSet.addAll(this.map, elements);
        if (that.size() == this.map.size()) {
            return this;
        }
        return new LinkedHashSet<T>(that);
    }

    @Override
    public <R> LinkedHashSet<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        return LinkedHashSet.ofAll(this.iterator().collect(partialFunction));
    }

    @Override
    public boolean contains(T element) {
        return this.map.get(element).isDefined();
    }

    @Override
    public LinkedHashSet<T> diff(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (this.isEmpty() || elements.isEmpty()) {
            return this;
        }
        return this.removeAll(elements);
    }

    @Override
    public LinkedHashSet<T> distinct() {
        return this;
    }

    @Override
    public LinkedHashSet<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return LinkedHashSet.ofAll(this.iterator().distinctBy(comparator));
    }

    @Override
    public <U> LinkedHashSet<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        return LinkedHashSet.ofAll(this.iterator().distinctBy(keyExtractor));
    }

    @Override
    public LinkedHashSet<T> drop(int n) {
        if (n <= 0) {
            return this;
        }
        return LinkedHashSet.ofAll(this.iterator().drop(n));
    }

    @Override
    public LinkedHashSet<T> dropRight(int n) {
        if (n <= 0) {
            return this;
        }
        return LinkedHashSet.ofAll(this.iterator().dropRight(n));
    }

    @Override
    public LinkedHashSet<T> dropUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.dropWhile((Predicate)predicate.negate());
    }

    @Override
    public LinkedHashSet<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        LinkedHashSet<T> dropped = LinkedHashSet.ofAll(this.iterator().dropWhile(predicate));
        return dropped.length() == this.length() ? this : dropped;
    }

    @Override
    public LinkedHashSet<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        LinkedHashSet<T> filtered = LinkedHashSet.ofAll(this.iterator().filter(predicate));
        return filtered.length() == this.length() ? this : filtered;
    }

    @Override
    public LinkedHashSet<T> reject(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.filter((Predicate)predicate.negate());
    }

    @Override
    public <U> LinkedHashSet<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (this.isEmpty()) {
            return LinkedHashSet.empty();
        }
        LinkedHashMap that = this.foldLeft(LinkedHashMap.empty(), (tree, t) -> LinkedHashSet.addAll(tree, (Iterable)mapper.apply(t)));
        return new LinkedHashSet<T>(that);
    }

    @Override
    public <U> U foldRight(U zero, BiFunction<? super T, ? super U, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return this.iterator().foldRight(zero, f);
    }

    @Override
    public <C> Map<C, LinkedHashSet<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return Collections.groupBy(this, classifier, LinkedHashSet::ofAll);
    }

    @Override
    public Iterator<LinkedHashSet<T>> grouped(int size) {
        return this.sliding(size, size);
    }

    @Override
    public boolean hasDefiniteSize() {
        return true;
    }

    @Override
    public T head() {
        if (this.map.isEmpty()) {
            throw new NoSuchElementException("head of empty set");
        }
        return (T)this.iterator().next();
    }

    @Override
    public Option<T> headOption() {
        return this.iterator().headOption();
    }

    @Override
    public LinkedHashSet<T> init() {
        if (this.map.isEmpty()) {
            throw new UnsupportedOperationException("tail of empty set");
        }
        return new LinkedHashSet<T>(this.map.init());
    }

    @Override
    public Option<LinkedHashSet<T>> initOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.init());
    }

    @Override
    public LinkedHashSet<T> intersect(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (this.isEmpty() || elements.isEmpty()) {
            return LinkedHashSet.empty();
        }
        return this.retainAll(elements);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
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
    public boolean isSequential() {
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return this.map.iterator().map((T t) -> t._1);
    }

    @Override
    public T last() {
        return (T)((Tuple2)this.map.last())._1;
    }

    @Override
    public int length() {
        return this.map.size();
    }

    @Override
    public <U> LinkedHashSet<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (this.isEmpty()) {
            return LinkedHashSet.empty();
        }
        LinkedHashMap that = this.foldLeft(LinkedHashMap.empty(), (tree, t) -> {
            Object u = mapper.apply(t);
            return tree.put(u, u);
        });
        return new LinkedHashSet<T>(that);
    }

    @Override
    public String mkString(CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
        return this.iterator().mkString(prefix, delimiter, suffix);
    }

    @Override
    public LinkedHashSet<T> orElse(Iterable<? extends T> other) {
        return this.isEmpty() ? LinkedHashSet.ofAll(other) : this;
    }

    @Override
    public LinkedHashSet<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return this.isEmpty() ? LinkedHashSet.ofAll(supplier.get()) : this;
    }

    @Override
    public Tuple2<LinkedHashSet<T>, LinkedHashSet<T>> partition(Predicate<? super T> predicate) {
        return Collections.partition(this, LinkedHashSet::ofAll, predicate);
    }

    @Override
    public LinkedHashSet<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (!this.isEmpty()) {
            action.accept(this.iterator().head());
        }
        return this;
    }

    @Override
    public LinkedHashSet<T> remove(T element) {
        Map newMap = this.map.remove((Object)element);
        return newMap == this.map ? this : new LinkedHashSet<T>(newMap);
    }

    @Override
    public LinkedHashSet<T> removeAll(Iterable<? extends T> elements) {
        return Collections.removeAll(this, elements);
    }

    @Override
    public LinkedHashSet<T> replace(T currentElement, T newElement) {
        if (!Objects.equals(currentElement, newElement) && this.contains(currentElement)) {
            Tuple2<T, T> currentPair = Tuple.of(currentElement, currentElement);
            Tuple2<T, T> newPair = Tuple.of(newElement, newElement);
            Map newMap = this.map.replace(currentPair, newPair);
            return new LinkedHashSet<T>(newMap);
        }
        return this;
    }

    @Override
    public LinkedHashSet<T> replaceAll(T currentElement, T newElement) {
        return this.replace((Object)currentElement, (Object)newElement);
    }

    @Override
    public LinkedHashSet<T> retainAll(Iterable<? extends T> elements) {
        return Collections.retainAll(this, elements);
    }

    @Override
    public LinkedHashSet<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        return this.scanLeft(zero, operation);
    }

    @Override
    public <U> LinkedHashSet<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        return Collections.scanLeft(this, zero, operation, LinkedHashSet::ofAll);
    }

    @Override
    public <U> LinkedHashSet<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        return Collections.scanRight(this, zero, operation, LinkedHashSet::ofAll);
    }

    @Override
    public Iterator<LinkedHashSet<T>> slideBy(Function<? super T, ?> classifier) {
        return this.iterator().slideBy(classifier).map(LinkedHashSet::ofAll);
    }

    @Override
    public Iterator<LinkedHashSet<T>> sliding(int size) {
        return this.sliding(size, 1);
    }

    @Override
    public Iterator<LinkedHashSet<T>> sliding(int size, int step) {
        return this.iterator().sliding(size, step).map(LinkedHashSet::ofAll);
    }

    @Override
    public Tuple2<LinkedHashSet<T>, LinkedHashSet<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        Tuple2<Iterator<? super T>, Iterator<? super T>> t = this.iterator().span(predicate);
        return Tuple.of(LinkedHashSet.ofAll((Iterable)t._1), LinkedHashSet.ofAll((Iterable)t._2));
    }

    @Override
    public LinkedHashSet<T> tail() {
        if (this.map.isEmpty()) {
            throw new UnsupportedOperationException("tail of empty set");
        }
        return this.remove((Object)this.head());
    }

    @Override
    public Option<LinkedHashSet<T>> tailOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.tail());
    }

    @Override
    public LinkedHashSet<T> take(int n) {
        if (this.map.size() <= n) {
            return this;
        }
        return LinkedHashSet.ofAll(() -> this.iterator().take(n));
    }

    @Override
    public LinkedHashSet<T> takeRight(int n) {
        if (this.map.size() <= n) {
            return this;
        }
        return LinkedHashSet.ofAll(() -> this.iterator().takeRight(n));
    }

    @Override
    public LinkedHashSet<T> takeUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.takeWhile((Predicate)predicate.negate());
    }

    @Override
    public LinkedHashSet<T> takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        LinkedHashSet<T> taken = LinkedHashSet.ofAll(this.iterator().takeWhile(predicate));
        return taken.length() == this.length() ? this : taken;
    }

    public <U> U transform(Function<? super LinkedHashSet<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    public java.util.LinkedHashSet<T> toJavaSet() {
        return this.toJavaSet(java.util.LinkedHashSet::new);
    }

    @Override
    public LinkedHashSet<T> union(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (this.isEmpty()) {
            if (elements instanceof LinkedHashSet) {
                return (LinkedHashSet)elements;
            }
            return LinkedHashSet.ofAll(elements);
        }
        if (elements.isEmpty()) {
            return this;
        }
        LinkedHashMap<T, Object> that = LinkedHashSet.addAll(this.map, elements);
        if (that.size() == this.map.size()) {
            return this;
        }
        return new LinkedHashSet<T>(that);
    }

    @Override
    public <T1, T2> Tuple2<LinkedHashSet<T1>, LinkedHashSet<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        Tuple2<Iterator<? extends T1>, Iterator<? extends T2>> t = this.iterator().unzip(unzipper);
        return Tuple.of(LinkedHashSet.ofAll((Iterable)t._1), LinkedHashSet.ofAll((Iterable)t._2));
    }

    @Override
    public <T1, T2, T3> Tuple3<LinkedHashSet<T1>, LinkedHashSet<T2>, LinkedHashSet<T3>> unzip3(Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        Tuple3<Iterator<? extends T1>, Iterator<? extends T2>, Iterator<? extends T3>> t = this.iterator().unzip3(unzipper);
        return Tuple.of(LinkedHashSet.ofAll((Iterable)t._1), LinkedHashSet.ofAll((Iterable)t._2), LinkedHashSet.ofAll((Iterable)t._3));
    }

    @Override
    public <U> LinkedHashSet<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        return this.zipWith((Iterable)that, Tuple::of);
    }

    @Override
    public <U, R> LinkedHashSet<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return LinkedHashSet.ofAll(this.iterator().zipWith(that, mapper));
    }

    @Override
    public <U> LinkedHashSet<Tuple2<T, U>> zipAll(Iterable<? extends U> that, T thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        return LinkedHashSet.ofAll(this.iterator().zipAll(that, thisElem, thatElem));
    }

    @Override
    public LinkedHashSet<Tuple2<T, Integer>> zipWithIndex() {
        return this.zipWithIndex(Tuple::of);
    }

    @Override
    public <U> LinkedHashSet<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return LinkedHashSet.ofAll(this.iterator().zipWithIndex(mapper));
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
        return "LinkedHashSet";
    }

    @Override
    public String toString() {
        return this.mkString(this.stringPrefix() + "(", ", ", ")");
    }

    private static <T> LinkedHashMap<T, Object> addAll(LinkedHashMap<T, Object> initial, Iterable<? extends T> additional) {
        Map<Object, Object> that = initial;
        for (T t : additional) {
            that = that.put((Object)t, (Object)t);
        }
        return that;
    }

    @GwtIncompatible(value="The Java serialization protocol is explicitly not supported")
    private Object writeReplace() {
        return new SerializationProxy<T>(this.map);
    }

    @GwtIncompatible(value="The Java serialization protocol is explicitly not supported")
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    @GwtIncompatible(value="The Java serialization protocol is explicitly not supported")
    private static final class SerializationProxy<T>
    implements Serializable {
        private static final long serialVersionUID = 1L;
        private transient LinkedHashMap<T, Object> map;

        SerializationProxy(LinkedHashMap<T, Object> map) {
            this.map = map;
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            s.writeInt(this.map.size());
            for (Tuple2 tuple2 : this.map) {
                s.writeObject(tuple2._1);
            }
        }

        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            s.defaultReadObject();
            int size = s.readInt();
            if (size < 0) {
                throw new InvalidObjectException("No elements");
            }
            Map temp = LinkedHashMap.empty();
            for (int i = 0; i < size; ++i) {
                Object element = s.readObject();
                temp = temp.put(element, element);
            }
            this.map = temp;
        }

        private Object readResolve() {
            return this.map.isEmpty() ? LinkedHashSet.empty() : new LinkedHashSet(this.map);
        }
    }
}

