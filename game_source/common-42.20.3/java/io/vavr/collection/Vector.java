/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.PartialFunction;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Value;
import io.vavr.collection.ArrayType;
import io.vavr.collection.BitMappedTrie;
import io.vavr.collection.Collections;
import io.vavr.collection.GwtIncompatible;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Iterator;
import io.vavr.collection.JavaConverters;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Traversable;
import io.vavr.collection.VectorModule;
import io.vavr.control.Option;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public final class Vector<T>
implements IndexedSeq<T>,
Serializable {
    private static final long serialVersionUID = 1L;
    private static final Vector<?> EMPTY = new Vector(BitMappedTrie.empty());
    final BitMappedTrie<T> trie;

    private Vector(BitMappedTrie<T> trie) {
        this.trie = trie;
    }

    private Vector<T> wrap(BitMappedTrie<T> trie) {
        return trie == this.trie ? this : Vector.ofAll(trie);
    }

    private static <T> Vector<T> ofAll(BitMappedTrie<T> trie) {
        return trie.length() == 0 ? Vector.empty() : new Vector<T>(trie);
    }

    public static <T> Vector<T> empty() {
        return EMPTY;
    }

    public static <T> Collector<T, ArrayList<T>, Vector<T>> collector() {
        Supplier<ArrayList> supplier = ArrayList::new;
        BiConsumer<ArrayList, Object> accumulator = ArrayList::add;
        BinaryOperator combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        Function<ArrayList, Vector> finisher = Vector::ofAll;
        return Collector.of(supplier, accumulator, combiner, finisher, new Collector.Characteristics[0]);
    }

    public static <T> Vector<T> narrow(Vector<? extends T> vector) {
        return vector;
    }

    public static <T> Vector<T> of(T element) {
        return Vector.ofAll(BitMappedTrie.ofAll(new Object[]{element}));
    }

    @SafeVarargs
    public static <T> Vector<T> of(T ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Vector.ofAll(BitMappedTrie.ofAll(elements));
    }

    public static <T> Vector<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return Collections.tabulate(n, f, Vector.empty(), Vector::of);
    }

    public static <T> Vector<T> fill(int n, Supplier<? extends T> s) {
        Objects.requireNonNull(s, "s is null");
        return Collections.fill(n, s, Vector.empty(), Vector::of);
    }

    public static <T> Vector<T> fill(int n, T element) {
        return Collections.fillObject(n, element, Vector.empty(), Vector::of);
    }

    public static <T> Vector<T> ofAll(Iterable<? extends T> iterable) {
        Objects.requireNonNull(iterable, "iterable is null");
        if (iterable instanceof Traversable && Collections.isEmpty(iterable)) {
            return Vector.empty();
        }
        if (iterable instanceof Vector) {
            return (Vector)iterable;
        }
        if (iterable instanceof JavaConverters.ListView && ((JavaConverters.ListView)iterable).getDelegate() instanceof Vector) {
            return (Vector)((JavaConverters.ListView)iterable).getDelegate();
        }
        Object[] values2 = Collections.withSize(iterable).toArray();
        return Vector.ofAll(BitMappedTrie.ofAll(values2));
    }

    public static <T> Vector<T> ofAll(Stream<? extends T> javaStream) {
        Objects.requireNonNull(javaStream, "javaStream is null");
        return Vector.ofAll(Iterator.ofAll(javaStream.iterator()));
    }

    public static Vector<Boolean> ofAll(boolean ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Vector.ofAll(BitMappedTrie.ofAll(elements));
    }

    public static Vector<Byte> ofAll(byte ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Vector.ofAll(BitMappedTrie.ofAll(elements));
    }

    public static Vector<Character> ofAll(char ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Vector.ofAll(BitMappedTrie.ofAll(elements));
    }

    public static Vector<Double> ofAll(double ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Vector.ofAll(BitMappedTrie.ofAll(elements));
    }

    public static Vector<Float> ofAll(float ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Vector.ofAll(BitMappedTrie.ofAll(elements));
    }

    public static Vector<Integer> ofAll(int ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Vector.ofAll(BitMappedTrie.ofAll(elements));
    }

    public static Vector<Long> ofAll(long ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Vector.ofAll(BitMappedTrie.ofAll(elements));
    }

    public static Vector<Short> ofAll(short ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Vector.ofAll(BitMappedTrie.ofAll(elements));
    }

    public static Vector<Character> range(char from, char toExclusive) {
        return Vector.ofAll((char[])ArrayType.asPrimitives(Character.TYPE, Iterator.range(from, toExclusive)));
    }

    public static Vector<Character> rangeBy(char from, char toExclusive, int step) {
        return Vector.ofAll((char[])ArrayType.asPrimitives(Character.TYPE, Iterator.rangeBy(from, toExclusive, step)));
    }

    @GwtIncompatible
    public static Vector<Double> rangeBy(double from, double toExclusive, double step) {
        return Vector.ofAll((double[])ArrayType.asPrimitives(Double.TYPE, Iterator.rangeBy(from, toExclusive, step)));
    }

    public static Vector<Integer> range(int from, int toExclusive) {
        return Vector.ofAll((int[])ArrayType.asPrimitives(Integer.TYPE, Iterator.range(from, toExclusive)));
    }

    public static Vector<Integer> rangeBy(int from, int toExclusive, int step) {
        return Vector.ofAll((int[])ArrayType.asPrimitives(Integer.TYPE, Iterator.rangeBy(from, toExclusive, step)));
    }

    public static Vector<Long> range(long from, long toExclusive) {
        return Vector.ofAll((long[])ArrayType.asPrimitives(Long.TYPE, Iterator.range(from, toExclusive)));
    }

    public static Vector<Long> rangeBy(long from, long toExclusive, long step) {
        return Vector.ofAll((long[])ArrayType.asPrimitives(Long.TYPE, Iterator.rangeBy(from, toExclusive, step)));
    }

    public static Vector<Character> rangeClosed(char from, char toInclusive) {
        return Vector.ofAll((char[])ArrayType.asPrimitives(Character.TYPE, Iterator.rangeClosed(from, toInclusive)));
    }

    public static Vector<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return Vector.ofAll((char[])ArrayType.asPrimitives(Character.TYPE, Iterator.rangeClosedBy(from, toInclusive, step)));
    }

    @GwtIncompatible
    public static Vector<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return Vector.ofAll((double[])ArrayType.asPrimitives(Double.TYPE, Iterator.rangeClosedBy(from, toInclusive, step)));
    }

    public static Vector<Integer> rangeClosed(int from, int toInclusive) {
        return Vector.ofAll((int[])ArrayType.asPrimitives(Integer.TYPE, Iterator.rangeClosed(from, toInclusive)));
    }

    public static Vector<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return Vector.ofAll((int[])ArrayType.asPrimitives(Integer.TYPE, Iterator.rangeClosedBy(from, toInclusive, step)));
    }

    public static Vector<Long> rangeClosed(long from, long toInclusive) {
        return Vector.ofAll((long[])ArrayType.asPrimitives(Long.TYPE, Iterator.rangeClosed(from, toInclusive)));
    }

    public static Vector<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return Vector.ofAll((long[])ArrayType.asPrimitives(Long.TYPE, Iterator.rangeClosedBy(from, toInclusive, step)));
    }

    public static <T> Vector<Vector<T>> transpose(Vector<Vector<T>> matrix) {
        return Collections.transpose(matrix, Vector::ofAll, Vector::of);
    }

    public static <T, U> Vector<U> unfoldRight(T seed, Function<? super T, Option<Tuple2<? extends U, ? extends T>>> f) {
        return Iterator.unfoldRight(seed, f).toVector();
    }

    public static <T, U> Vector<U> unfoldLeft(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends U>>> f) {
        return Iterator.unfoldLeft(seed, f).toVector();
    }

    public static <T> Vector<T> unfold(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends T>>> f) {
        return Iterator.unfold(seed, f).toVector();
    }

    @Override
    public Vector<T> append(T element) {
        return this.appendAll(List.of(element));
    }

    @Override
    public Vector<T> appendAll(Iterable<? extends T> iterable) {
        Objects.requireNonNull(iterable, "iterable is null");
        if (this.isEmpty()) {
            return Vector.ofAll(iterable);
        }
        if (Collections.isEmpty(iterable)) {
            return this;
        }
        return new Vector<T>(this.trie.appendAll(iterable));
    }

    @Override
    @GwtIncompatible
    public java.util.List<T> asJava() {
        return JavaConverters.asJava(this, JavaConverters.ChangePolicy.IMMUTABLE);
    }

    @Override
    @GwtIncompatible
    public Vector<T> asJava(Consumer<? super java.util.List<T>> action) {
        return Collections.asJava(this, action, JavaConverters.ChangePolicy.IMMUTABLE);
    }

    @Override
    @GwtIncompatible
    public java.util.List<T> asJavaMutable() {
        return JavaConverters.asJava(this, JavaConverters.ChangePolicy.MUTABLE);
    }

    @Override
    @GwtIncompatible
    public Vector<T> asJavaMutable(Consumer<? super java.util.List<T>> action) {
        return Collections.asJava(this, action, JavaConverters.ChangePolicy.MUTABLE);
    }

    @Override
    public <R> Vector<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        return Vector.ofAll(this.iterator().collect(partialFunction));
    }

    @Override
    public Vector<Vector<T>> combinations() {
        return ((Vector)Vector.rangeClosed(0, this.length()).map((T n) -> this.combinations((int)n))).flatMap(Function.identity());
    }

    @Override
    public Vector<Vector<T>> combinations(int k) {
        return VectorModule.Combinations.apply(this, Math.max(k, 0));
    }

    @Override
    public Iterator<Vector<T>> crossProduct(int power) {
        return Collections.crossProduct(Vector.empty(), this, power);
    }

    @Override
    public Vector<T> distinct() {
        return this.distinctBy(Function.identity());
    }

    @Override
    public Vector<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        TreeSet<? super T> seen = new TreeSet<T>(comparator);
        return this.filter(seen::add);
    }

    @Override
    public <U> Vector<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        HashSet seen = new HashSet(this.length());
        return this.filter((T t) -> seen.add(keyExtractor.apply(t)));
    }

    @Override
    public Vector<T> drop(int n) {
        return this.wrap(this.trie.drop(n));
    }

    @Override
    public Vector<T> dropUntil(Predicate<? super T> predicate) {
        return Collections.dropUntil(this, predicate);
    }

    @Override
    public Vector<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.dropUntil((Predicate)predicate.negate());
    }

    @Override
    public Vector<T> dropRight(int n) {
        return this.take(this.length() - n);
    }

    @Override
    public Vector<T> dropRightUntil(Predicate<? super T> predicate) {
        return Collections.dropRightUntil(this, predicate);
    }

    @Override
    public Vector<T> dropRightWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.dropRightUntil((Predicate)predicate.negate());
    }

    @Override
    public Vector<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.wrap(this.trie.filter(predicate));
    }

    @Override
    public Vector<T> reject(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Collections.reject(this, predicate);
    }

    @Override
    public <U> Vector<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        Traversable results = this.iterator().flatMap(mapper);
        return Vector.ofAll(results);
    }

    @Override
    public T get(int index) {
        if (this.isValid(index)) {
            return this.trie.get(index);
        }
        throw new IndexOutOfBoundsException("get(" + index + ")");
    }

    private boolean isValid(int index) {
        return index >= 0 && index < this.length();
    }

    @Override
    public T head() {
        if (this.nonEmpty()) {
            return this.get(0);
        }
        throw new NoSuchElementException("head of empty Vector");
    }

    @Override
    public <C> Map<C, Vector<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return Collections.groupBy(this, classifier, Vector::ofAll);
    }

    @Override
    public Iterator<Vector<T>> grouped(int size) {
        return this.sliding(size, size);
    }

    @Override
    public boolean hasDefiniteSize() {
        return true;
    }

    @Override
    public int indexOf(T element, int from) {
        for (int i = from; i < this.length(); ++i) {
            if (!Objects.equals(this.get(i), element)) continue;
            return i;
        }
        return -1;
    }

    @Override
    public Vector<T> init() {
        if (this.nonEmpty()) {
            return this.dropRight(1);
        }
        throw new UnsupportedOperationException("init of empty Vector");
    }

    @Override
    public Option<Vector<T>> initOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.init());
    }

    @Override
    public Vector<T> insert(int index, T element) {
        return this.insertAll(index, Iterator.of(element));
    }

    @Override
    public Vector<T> insertAll(int index, Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (index >= 0 && index <= this.length()) {
            IndexedSeq begin = ((Vector)this.take(index)).appendAll((Iterable)elements);
            IndexedSeq end = this.drop(index);
            return begin.size() > end.size() ? ((Vector)begin).appendAll((Iterable)end) : ((Vector)end).prependAll((Iterable)begin);
        }
        throw new IndexOutOfBoundsException("insert(" + index + ", e) on Vector of length " + this.length());
    }

    @Override
    public Vector<T> intersperse(T element) {
        return Vector.ofAll(this.iterator().intersperse(element));
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.length() == 0;
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
        return this.isEmpty() ? Iterator.empty() : this.trie.iterator();
    }

    @Override
    public int lastIndexOf(T element, int end) {
        for (int i = Math.min(end, this.length() - 1); i >= 0; --i) {
            if (!Objects.equals(this.get(i), element)) continue;
            return i;
        }
        return -1;
    }

    @Override
    public int length() {
        return this.trie.length();
    }

    @Override
    public <U> Vector<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return Vector.ofAll(this.trie.map(mapper));
    }

    @Override
    public Vector<T> orElse(Iterable<? extends T> other) {
        return this.isEmpty() ? Vector.ofAll(other) : this;
    }

    @Override
    public Vector<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return this.isEmpty() ? Vector.ofAll(supplier.get()) : this;
    }

    @Override
    public Vector<T> padTo(int length, T element) {
        int actualLength = this.length();
        return length <= actualLength ? this : this.appendAll((Iterable)Iterator.continually(element).take(length - actualLength));
    }

    @Override
    public Vector<T> leftPadTo(int length, T element) {
        if (length <= this.length()) {
            return this;
        }
        Traversable prefix = Iterator.continually(element).take(length - this.length());
        return this.prependAll((Iterable)prefix);
    }

    @Override
    public Vector<T> patch(int from, Iterable<? extends T> that, int replaced) {
        from = Math.max(from, 0);
        replaced = Math.max(replaced, 0);
        IndexedSeq result = ((Vector)this.take(from)).appendAll((Iterable)that);
        result = ((Vector)result).appendAll((Iterable)this.drop(from += replaced));
        return result;
    }

    @Override
    public Tuple2<Vector<T>, Vector<T>> partition(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        ArrayList<T> left = new ArrayList<T>();
        ArrayList right = new ArrayList();
        for (int i = 0; i < this.length(); ++i) {
            T t = this.get(i);
            (predicate.test(t) ? left : right).add(t);
        }
        return Tuple.of(Vector.ofAll(left), Vector.ofAll(right));
    }

    @Override
    public Vector<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (!this.isEmpty()) {
            action.accept(this.head());
        }
        return this;
    }

    @Override
    public Vector<Vector<T>> permutations() {
        if (this.isEmpty()) {
            return Vector.empty();
        }
        if (this.length() == 1) {
            return Vector.of(this);
        }
        IndexedSeq<Vector<T>> results = Vector.empty();
        for (Object t : this.distinct()) {
            for (Vector ts : ((Vector)this.remove(t)).permutations()) {
                results = results.append(Vector.of(t).appendAll((Iterable)ts));
            }
        }
        return results;
    }

    @Override
    public Vector<T> prepend(T element) {
        return this.prependAll(List.of(element));
    }

    @Override
    public Vector<T> prependAll(Iterable<? extends T> iterable) {
        Objects.requireNonNull(iterable, "iterable is null");
        if (this.isEmpty()) {
            return Vector.ofAll(iterable);
        }
        if (Collections.isEmpty(iterable)) {
            return this;
        }
        return new Vector<T>(this.trie.prependAll(iterable));
    }

    @Override
    public Vector<T> remove(T element) {
        for (int i = 0; i < this.length(); ++i) {
            if (!Objects.equals(this.get(i), element)) continue;
            return this.removeAt(i);
        }
        return this;
    }

    @Override
    public Vector<T> removeFirst(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        for (int i = 0; i < this.length(); ++i) {
            if (!predicate.test(this.get(i))) continue;
            return this.removeAt(i);
        }
        return this;
    }

    @Override
    public Vector<T> removeLast(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        for (int i = this.length() - 1; i >= 0; --i) {
            if (!predicate.test(this.get(i))) continue;
            return this.removeAt(i);
        }
        return this;
    }

    @Override
    public Vector<T> removeAt(int index) {
        if (this.isValid(index)) {
            IndexedSeq begin = this.take(index);
            IndexedSeq end = this.drop(index + 1);
            return begin.size() > end.size() ? ((Vector)begin).appendAll((Iterable)end) : ((Vector)end).prependAll((Iterable)begin);
        }
        throw new IndexOutOfBoundsException("removeAt(" + index + ")");
    }

    @Override
    public Vector<T> removeAll(T element) {
        return Collections.removeAll(this, element);
    }

    @Override
    public Vector<T> removeAll(Iterable<? extends T> elements) {
        return Collections.removeAll(this, elements);
    }

    @Override
    @Deprecated
    public Vector<T> removeAll(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.reject((Predicate)predicate);
    }

    @Override
    public Vector<T> replace(T currentElement, T newElement) {
        return this.indexOfOption(currentElement).map((T i) -> this.update((int)i, (Object)newElement)).getOrElse(this);
    }

    @Override
    public Vector<T> replaceAll(T currentElement, T newElement) {
        IndexedSeq<T> result = this;
        int index = 0;
        java.util.Iterator iterator2 = this.iterator().iterator();
        while (iterator2.hasNext()) {
            Object value = iterator2.next();
            if (Objects.equals(value, currentElement)) {
                result = result.update(index, (Object)newElement);
            }
            ++index;
        }
        return result;
    }

    @Override
    public Vector<T> retainAll(Iterable<? extends T> elements) {
        return Collections.retainAll(this, elements);
    }

    @Override
    public Vector<T> reverse() {
        return this.length() <= 1 ? this : Vector.ofAll(this.reverseIterator());
    }

    @Override
    public Vector<T> rotateLeft(int n) {
        return Collections.rotateLeft(this, n);
    }

    @Override
    public Vector<T> rotateRight(int n) {
        return Collections.rotateRight(this, n);
    }

    @Override
    public Vector<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        return this.scanLeft(zero, operation);
    }

    @Override
    public <U> Vector<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        return Collections.scanLeft(this, zero, operation, Value::toVector);
    }

    @Override
    public <U> Vector<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        return Collections.scanRight(this, zero, operation, Value::toVector);
    }

    @Override
    public Vector<T> shuffle() {
        return Collections.shuffle(this, Vector::ofAll);
    }

    @Override
    public Vector<T> slice(int beginIndex, int endIndex) {
        if (beginIndex >= endIndex || beginIndex >= this.size() || this.isEmpty()) {
            return Vector.empty();
        }
        if (beginIndex <= 0 && endIndex >= this.length()) {
            return this;
        }
        return ((Vector)this.take(endIndex)).drop(beginIndex);
    }

    @Override
    public Iterator<Vector<T>> slideBy(Function<? super T, ?> classifier) {
        return this.iterator().slideBy(classifier).map(Vector::ofAll);
    }

    @Override
    public Iterator<Vector<T>> sliding(int size) {
        return this.sliding(size, 1);
    }

    @Override
    public Iterator<Vector<T>> sliding(int size, int step) {
        return this.iterator().sliding(size, step).map(Vector::ofAll);
    }

    @Override
    public Vector<T> sorted() {
        if (this.isEmpty()) {
            return this;
        }
        Object[] list = this.toJavaArray();
        Arrays.sort(list);
        return Vector.of(list);
    }

    @Override
    public Vector<T> sorted(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return this.isEmpty() ? this : this.toJavaStream().sorted(comparator).collect(Vector.collector());
    }

    @Override
    public <U extends Comparable<? super U>> Vector<T> sortBy(Function<? super T, ? extends U> mapper) {
        return this.sortBy(Comparable::compareTo, (Function)mapper);
    }

    @Override
    public <U> Vector<T> sortBy(Comparator<? super U> comparator, Function<? super T, ? extends U> mapper) {
        return Collections.sortBy(this, comparator, mapper, Vector.collector());
    }

    @Override
    public Tuple2<Vector<T>, Vector<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Tuple.of(this.takeWhile((Predicate)predicate), this.dropWhile((Predicate)predicate));
    }

    @Override
    public Tuple2<Vector<T>, Vector<T>> splitAt(int n) {
        return Tuple.of(this.take(n), this.drop(n));
    }

    @Override
    public Tuple2<Vector<T>, Vector<T>> splitAt(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        IndexedSeq init = this.takeWhile((Predicate)predicate.negate());
        return Tuple.of(init, this.drop(init.size()));
    }

    @Override
    public Tuple2<Vector<T>, Vector<T>> splitAtInclusive(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        for (int i = 0; i < this.length(); ++i) {
            T value = this.get(i);
            if (!predicate.test(value)) continue;
            return i == this.length() - 1 ? Tuple.of(this, Vector.empty()) : Tuple.of(this.take(i + 1), this.drop(i + 1));
        }
        return Tuple.of(this, Vector.empty());
    }

    @Override
    public Vector<T> subSequence(int beginIndex) {
        if (beginIndex >= 0 && beginIndex <= this.length()) {
            return this.drop(beginIndex);
        }
        throw new IndexOutOfBoundsException("subSequence(" + beginIndex + ")");
    }

    @Override
    public Vector<T> subSequence(int beginIndex, int endIndex) {
        Collections.subSequenceRangeCheck(beginIndex, endIndex, this.length());
        return this.slice(beginIndex, endIndex);
    }

    @Override
    public Vector<T> tail() {
        if (this.nonEmpty()) {
            return this.drop(1);
        }
        throw new UnsupportedOperationException("tail of empty Vector");
    }

    @Override
    public Option<Vector<T>> tailOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.tail());
    }

    @Override
    public Vector<T> take(int n) {
        return this.wrap(this.trie.take(n));
    }

    @Override
    public Vector<T> takeUntil(Predicate<? super T> predicate) {
        return Collections.takeUntil(this, predicate);
    }

    @Override
    public Vector<T> takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.takeUntil((Predicate)predicate.negate());
    }

    @Override
    public Vector<T> takeRight(int n) {
        return this.drop(this.length() - n);
    }

    @Override
    public Vector<T> takeRightUntil(Predicate<? super T> predicate) {
        return Collections.takeRightUntil(this, predicate);
    }

    @Override
    public Vector<T> takeRightWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.takeRightUntil((Predicate)predicate.negate());
    }

    public <U> U transform(Function<? super Vector<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    public <T1, T2> Tuple2<Vector<T1>, Vector<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        IndexedSeq<T> xs = Vector.empty();
        IndexedSeq<T> ys = Vector.empty();
        for (int i = 0; i < this.length(); ++i) {
            Tuple2<? extends T1, ? extends T2> t = unzipper.apply(this.get(i));
            xs = xs.append(t._1);
            ys = ys.append(t._2);
        }
        return Tuple.of(xs, ys);
    }

    @Override
    public <T1, T2, T3> Tuple3<Vector<T1>, Vector<T2>, Vector<T3>> unzip3(Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        IndexedSeq<T> xs = Vector.empty();
        IndexedSeq<T> ys = Vector.empty();
        IndexedSeq<T> zs = Vector.empty();
        for (int i = 0; i < this.length(); ++i) {
            Tuple3<? extends T1, ? extends T2, ? extends T3> t = unzipper.apply(this.get(i));
            xs = xs.append(t._1);
            ys = ys.append(t._2);
            zs = zs.append(t._3);
        }
        return Tuple.of(xs, ys, zs);
    }

    @Override
    public Vector<T> update(int index, T element) {
        if (this.isValid(index)) {
            return this.wrap(this.trie.update(index, element));
        }
        throw new IndexOutOfBoundsException("update(" + index + ")");
    }

    @Override
    public Vector<T> update(int index, Function<? super T, ? extends T> updater) {
        Objects.requireNonNull(updater, "updater is null");
        return this.update(index, (Object)updater.apply(this.get(index)));
    }

    @Override
    public <U> Vector<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        return this.zipWith((Iterable)that, Tuple::of);
    }

    @Override
    public <U, R> Vector<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return Vector.ofAll(this.iterator().zipWith(that, mapper));
    }

    @Override
    public <U> Vector<Tuple2<T, U>> zipAll(Iterable<? extends U> that, T thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        return Vector.ofAll(this.iterator().zipAll(that, thisElem, thatElem));
    }

    @Override
    public Vector<Tuple2<T, Integer>> zipWithIndex() {
        return this.zipWithIndex(Tuple::of);
    }

    @Override
    public <U> Vector<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return Vector.ofAll(this.iterator().zipWithIndex(mapper));
    }

    private Object readResolve() {
        return this.isEmpty() ? EMPTY : this;
    }

    @Override
    public boolean equals(Object o) {
        return Collections.equals(this, o);
    }

    @Override
    public int hashCode() {
        return Collections.hashOrdered(this);
    }

    @Override
    public String stringPrefix() {
        return "Vector";
    }

    @Override
    public String toString() {
        return this.mkString(this.stringPrefix() + "(", ", ", ")");
    }
}

