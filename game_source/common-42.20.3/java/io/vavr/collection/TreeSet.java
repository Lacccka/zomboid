/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.PartialFunction;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.Collections;
import io.vavr.collection.Comparators;
import io.vavr.collection.GwtIncompatible;
import io.vavr.collection.HashSet;
import io.vavr.collection.Iterator;
import io.vavr.collection.Map;
import io.vavr.collection.RedBlackTree;
import io.vavr.collection.Set;
import io.vavr.collection.SortedSet;
import io.vavr.control.Option;
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

public final class TreeSet<T>
implements SortedSet<T>,
Serializable {
    private static final long serialVersionUID = 1L;
    private final RedBlackTree<T> tree;

    TreeSet(RedBlackTree<T> tree) {
        this.tree = tree;
    }

    public static <T extends Comparable<? super T>> Collector<T, ArrayList<T>, TreeSet<T>> collector() {
        return TreeSet.collector(Comparators.naturalComparator());
    }

    public static <T> Collector<T, ArrayList<T>, TreeSet<T>> collector(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        Supplier<ArrayList> supplier = ArrayList::new;
        BiConsumer<ArrayList, Object> accumulator = ArrayList::add;
        BinaryOperator combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        Function<ArrayList, TreeSet> finisher = list -> TreeSet.ofAll(comparator, list);
        return Collector.of(supplier, accumulator, combiner, finisher, new Collector.Characteristics[0]);
    }

    public static <T extends Comparable<? super T>> TreeSet<T> empty() {
        return TreeSet.empty(Comparators.naturalComparator());
    }

    public static <T> TreeSet<T> empty(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return new TreeSet<T>(RedBlackTree.empty(comparator));
    }

    public static <T> TreeSet<T> narrow(TreeSet<? extends T> treeSet) {
        return treeSet;
    }

    public static <T extends Comparable<? super T>> TreeSet<T> of(T value) {
        return TreeSet.of(Comparators.naturalComparator(), value);
    }

    public static <T> TreeSet<T> of(Comparator<? super T> comparator, T value) {
        Objects.requireNonNull(comparator, "comparator is null");
        return new TreeSet<T>(RedBlackTree.of(comparator, value));
    }

    @SafeVarargs
    public static <T extends Comparable<? super T>> TreeSet<T> of(T ... values2) {
        return TreeSet.of(Comparators.naturalComparator(), values2);
    }

    @SafeVarargs
    public static <T> TreeSet<T> of(Comparator<? super T> comparator, T ... values2) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(values2, "values is null");
        return new TreeSet<T>(RedBlackTree.of(comparator, values2));
    }

    public static <T> TreeSet<T> tabulate(Comparator<? super T> comparator, int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(f, "f is null");
        return Collections.tabulate(n, f, TreeSet.empty(comparator), values2 -> TreeSet.of(comparator, values2));
    }

    public static <T extends Comparable<? super T>> TreeSet<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return TreeSet.tabulate(Comparators.naturalComparator(), n, f);
    }

    public static <T> TreeSet<T> fill(Comparator<? super T> comparator, int n, Supplier<? extends T> s) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(s, "s is null");
        return Collections.fill(n, s, TreeSet.empty(comparator), values2 -> TreeSet.of(comparator, values2));
    }

    public static <T extends Comparable<? super T>> TreeSet<T> fill(int n, Supplier<? extends T> s) {
        Objects.requireNonNull(s, "s is null");
        return TreeSet.fill(Comparators.naturalComparator(), n, s);
    }

    public static <T extends Comparable<? super T>> TreeSet<T> ofAll(Iterable<? extends T> values2) {
        return TreeSet.ofAll(Comparators.naturalComparator(), values2);
    }

    public static <T> TreeSet<T> ofAll(Comparator<? super T> comparator, Iterable<? extends T> values2) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(values2, "values is null");
        if (values2 instanceof TreeSet && ((TreeSet)values2).comparator() == comparator) {
            return (TreeSet)values2;
        }
        return values2.iterator().hasNext() ? new TreeSet<T>(RedBlackTree.ofAll(comparator, values2)) : TreeSet.empty(comparator);
    }

    public static <T extends Comparable<? super T>> TreeSet<T> ofAll(Stream<? extends T> javaStream) {
        Objects.requireNonNull(javaStream, "javaStream is null");
        return TreeSet.ofAll(Iterator.ofAll(javaStream.iterator()));
    }

    public static <T> TreeSet<T> ofAll(Comparator<? super T> comparator, Stream<? extends T> javaStream) {
        Objects.requireNonNull(javaStream, "javaStream is null");
        return TreeSet.ofAll(comparator, Iterator.ofAll(javaStream.iterator()));
    }

    public static TreeSet<Boolean> ofAll(boolean ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return TreeSet.ofAll(Iterator.ofAll(elements));
    }

    public static TreeSet<Byte> ofAll(byte ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return TreeSet.ofAll(Iterator.ofAll(elements));
    }

    public static TreeSet<Character> ofAll(char ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return TreeSet.ofAll(Iterator.ofAll(elements));
    }

    public static TreeSet<Double> ofAll(double ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return TreeSet.ofAll(Iterator.ofAll(elements));
    }

    public static TreeSet<Float> ofAll(float ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return TreeSet.ofAll(Iterator.ofAll(elements));
    }

    public static TreeSet<Integer> ofAll(int ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return TreeSet.ofAll(Iterator.ofAll(elements));
    }

    public static TreeSet<Long> ofAll(long ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return TreeSet.ofAll(Iterator.ofAll(elements));
    }

    public static TreeSet<Short> ofAll(short ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return TreeSet.ofAll(Iterator.ofAll(elements));
    }

    public static TreeSet<Integer> range(int from, int toExclusive) {
        return TreeSet.ofAll(Iterator.range(from, toExclusive));
    }

    public static TreeSet<Character> range(char from, char toExclusive) {
        return TreeSet.ofAll(Iterator.range(from, toExclusive));
    }

    public static TreeSet<Integer> rangeBy(int from, int toExclusive, int step) {
        return TreeSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static TreeSet<Character> rangeBy(char from, char toExclusive, int step) {
        return TreeSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    @GwtIncompatible
    public static TreeSet<Double> rangeBy(double from, double toExclusive, double step) {
        return TreeSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static TreeSet<Long> range(long from, long toExclusive) {
        return TreeSet.ofAll(Iterator.range(from, toExclusive));
    }

    public static TreeSet<Long> rangeBy(long from, long toExclusive, long step) {
        return TreeSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static TreeSet<Integer> rangeClosed(int from, int toInclusive) {
        return TreeSet.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static TreeSet<Character> rangeClosed(char from, char toInclusive) {
        return TreeSet.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static TreeSet<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return TreeSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static TreeSet<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return TreeSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    @GwtIncompatible
    public static TreeSet<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return TreeSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static TreeSet<Long> rangeClosed(long from, long toInclusive) {
        return TreeSet.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static TreeSet<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return TreeSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    @Override
    public TreeSet<T> add(T element) {
        return this.contains(element) ? this : new TreeSet<T>(this.tree.insert(element));
    }

    @Override
    public TreeSet<T> addAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        RedBlackTree<T> that = this.tree;
        for (T element : elements) {
            if (that.contains(element)) continue;
            that = that.insert(element);
        }
        if (this.tree == that) {
            return this;
        }
        return new TreeSet<T>(that);
    }

    @Override
    public <R> TreeSet<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        return TreeSet.ofAll(Comparators.naturalComparator(), this.iterator().collect(partialFunction));
    }

    @Override
    public Comparator<T> comparator() {
        return this.tree.comparator();
    }

    @Override
    public TreeSet<T> diff(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (this.isEmpty()) {
            return this;
        }
        if (elements instanceof TreeSet) {
            TreeSet that = (TreeSet)elements;
            return that.isEmpty() ? this : new TreeSet<T>(this.tree.difference(that.tree));
        }
        return this.removeAll((Iterable)elements);
    }

    @Override
    public boolean contains(T element) {
        return this.tree.contains(element);
    }

    @Override
    public TreeSet<T> distinct() {
        return this;
    }

    @Override
    public TreeSet<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return this.isEmpty() ? this : TreeSet.ofAll(this.tree.comparator(), this.iterator().distinctBy(comparator));
    }

    @Override
    public <U> TreeSet<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        return this.isEmpty() ? this : TreeSet.ofAll(this.tree.comparator(), this.iterator().distinctBy(keyExtractor));
    }

    @Override
    public TreeSet<T> drop(int n) {
        if (n <= 0 || this.isEmpty()) {
            return this;
        }
        if (n >= this.length()) {
            return TreeSet.empty(this.tree.comparator());
        }
        return TreeSet.ofAll(this.tree.comparator(), this.iterator().drop(n));
    }

    @Override
    public TreeSet<T> dropRight(int n) {
        if (n <= 0 || this.isEmpty()) {
            return this;
        }
        if (n >= this.length()) {
            return TreeSet.empty(this.tree.comparator());
        }
        return TreeSet.ofAll(this.tree.comparator(), this.iterator().dropRight(n));
    }

    @Override
    public TreeSet<T> dropUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.dropWhile((Predicate)predicate.negate());
    }

    @Override
    public TreeSet<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        TreeSet<T> treeSet = TreeSet.ofAll(this.tree.comparator(), this.iterator().dropWhile(predicate));
        return treeSet.length() == this.length() ? this : treeSet;
    }

    @Override
    public TreeSet<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        TreeSet<T> treeSet = TreeSet.ofAll(this.tree.comparator(), this.iterator().filter(predicate));
        return treeSet.length() == this.length() ? this : treeSet;
    }

    @Override
    public TreeSet<T> reject(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.filter((Predicate)predicate.negate());
    }

    @Override
    public <U> TreeSet<U> flatMap(Comparator<? super U> comparator, Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return TreeSet.ofAll(comparator, this.iterator().flatMap(mapper));
    }

    @Override
    public <U> TreeSet<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        return this.flatMap(Comparators.naturalComparator(), (Function)mapper);
    }

    @Override
    public <U> U foldRight(U zero, BiFunction<? super T, ? super U, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return this.iterator().foldRight(zero, f);
    }

    @Override
    public <C> Map<C, TreeSet<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return Collections.groupBy(this, classifier, elements -> TreeSet.ofAll(this.comparator(), elements));
    }

    @Override
    public Iterator<TreeSet<T>> grouped(int size) {
        return this.sliding(size, size);
    }

    @Override
    public boolean hasDefiniteSize() {
        return true;
    }

    @Override
    public T head() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("head of empty TreeSet");
        }
        return this.tree.min().get();
    }

    @Override
    public Option<T> headOption() {
        return this.tree.min();
    }

    @Override
    public TreeSet<T> init() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("init of empty TreeSet");
        }
        return new TreeSet<T>(this.tree.delete(this.tree.max().get()));
    }

    @Override
    public Option<TreeSet<T>> initOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.init());
    }

    @Override
    public TreeSet<T> intersect(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (this.isEmpty()) {
            return this;
        }
        if (elements instanceof TreeSet) {
            TreeSet that = (TreeSet)elements;
            return new TreeSet<T>(this.tree.intersection(that.tree));
        }
        return this.retainAll(elements);
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
        return this.tree.iterator();
    }

    @Override
    public T last() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("last of empty TreeSet");
        }
        return this.tree.max().get();
    }

    @Override
    public int length() {
        return this.tree.size();
    }

    @Override
    public <U> TreeSet<U> map(Comparator<? super U> comparator, Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return TreeSet.ofAll(comparator, this.iterator().map(mapper));
    }

    @Override
    public <U> TreeSet<U> map(Function<? super T, ? extends U> mapper) {
        return this.map(Comparators.naturalComparator(), (Function)mapper);
    }

    @Override
    public TreeSet<T> orElse(Iterable<? extends T> other) {
        return this.isEmpty() ? TreeSet.ofAll(this.tree.comparator(), other) : this;
    }

    @Override
    public TreeSet<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return this.isEmpty() ? TreeSet.ofAll(this.tree.comparator(), supplier.get()) : this;
    }

    @Override
    public Tuple2<TreeSet<T>, TreeSet<T>> partition(Predicate<? super T> predicate) {
        return Collections.partition(this, values2 -> TreeSet.ofAll(this.tree.comparator(), values2), predicate);
    }

    @Override
    public TreeSet<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (!this.isEmpty()) {
            action.accept(this.head());
        }
        return this;
    }

    @Override
    public TreeSet<T> remove(T element) {
        return new TreeSet<T>(this.tree.delete(element));
    }

    @Override
    public TreeSet<T> removeAll(Iterable<? extends T> elements) {
        return Collections.removeAll(this, elements);
    }

    @Override
    public TreeSet<T> replace(T currentElement, T newElement) {
        if (this.tree.contains(currentElement)) {
            return new TreeSet<T>(this.tree.delete(currentElement).insert(newElement));
        }
        return this;
    }

    @Override
    public TreeSet<T> replaceAll(T currentElement, T newElement) {
        return this.replace((Object)currentElement, (Object)newElement);
    }

    @Override
    public TreeSet<T> retainAll(Iterable<? extends T> elements) {
        return Collections.retainAll(this, elements);
    }

    @Override
    public TreeSet<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        return Collections.scanLeft(this, zero, operation, iter -> TreeSet.ofAll(this.comparator(), iter));
    }

    @Override
    public <U> Set<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        if (zero instanceof Comparable) {
            Comparator comparator = Comparators.naturalComparator();
            return Collections.scanLeft(this, zero, operation, iter -> TreeSet.ofAll(comparator, iter));
        }
        return Collections.scanLeft(this, zero, operation, HashSet::ofAll);
    }

    @Override
    public <U> Set<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        if (zero instanceof Comparable) {
            Comparator comparator = Comparators.naturalComparator();
            return Collections.scanRight(this, zero, operation, iter -> TreeSet.ofAll(comparator, iter));
        }
        return Collections.scanRight(this, zero, operation, HashSet::ofAll);
    }

    @Override
    public Iterator<TreeSet<T>> slideBy(Function<? super T, ?> classifier) {
        return this.iterator().slideBy(classifier).map((T seq) -> TreeSet.ofAll(this.tree.comparator(), seq));
    }

    @Override
    public Iterator<TreeSet<T>> sliding(int size) {
        return this.sliding(size, 1);
    }

    @Override
    public Iterator<TreeSet<T>> sliding(int size, int step) {
        return this.iterator().sliding(size, step).map((T seq) -> TreeSet.ofAll(this.tree.comparator(), seq));
    }

    @Override
    public Tuple2<TreeSet<T>, TreeSet<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.iterator().span(predicate).map((? super T1 i1) -> TreeSet.ofAll(this.tree.comparator(), i1), (? super T2 i2) -> TreeSet.ofAll(this.tree.comparator(), i2));
    }

    @Override
    public TreeSet<T> tail() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("tail of empty TreeSet");
        }
        return new TreeSet<T>(this.tree.delete(this.tree.min().get()));
    }

    @Override
    public Option<TreeSet<T>> tailOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.tail());
    }

    @Override
    public TreeSet<T> take(int n) {
        if (n <= 0) {
            return TreeSet.empty(this.tree.comparator());
        }
        if (n >= this.length()) {
            return this;
        }
        return TreeSet.ofAll(this.tree.comparator(), this.iterator().take(n));
    }

    @Override
    public TreeSet<T> takeRight(int n) {
        if (n <= 0) {
            return TreeSet.empty(this.tree.comparator());
        }
        if (n >= this.length()) {
            return this;
        }
        return TreeSet.ofAll(this.tree.comparator(), this.iterator().takeRight(n));
    }

    @Override
    public TreeSet<T> takeUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        SortedSet treeSet = this.takeWhile((Predicate)predicate.negate());
        return ((TreeSet)treeSet).length() == this.length() ? this : treeSet;
    }

    @Override
    public TreeSet<T> takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        TreeSet<T> treeSet = TreeSet.ofAll(this.tree.comparator(), this.iterator().takeWhile(predicate));
        return treeSet.length() == this.length() ? this : treeSet;
    }

    public <U> U transform(Function<? super TreeSet<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    public java.util.TreeSet<T> toJavaSet() {
        return this.toJavaSet(ignore -> new java.util.TreeSet<T>(this.comparator()));
    }

    @Override
    public TreeSet<T> union(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (elements instanceof TreeSet) {
            TreeSet that = (TreeSet)elements;
            return that.isEmpty() ? this : new TreeSet<T>(this.tree.union(that.tree));
        }
        return this.addAll((Iterable)elements);
    }

    @Override
    public <T1, T2> Tuple2<TreeSet<T1>, TreeSet<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        return this.iterator().unzip(unzipper).map((? super T1 i1) -> TreeSet.ofAll(Comparators.naturalComparator(), i1), (? super T2 i2) -> TreeSet.ofAll(Comparators.naturalComparator(), i2));
    }

    @Override
    public <T1, T2, T3> Tuple3<TreeSet<T1>, TreeSet<T2>, TreeSet<T3>> unzip3(Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        return this.iterator().unzip3(unzipper).map(i1 -> TreeSet.ofAll(Comparators.naturalComparator(), i1), i2 -> TreeSet.ofAll(Comparators.naturalComparator(), i2), i3 -> TreeSet.ofAll(Comparators.naturalComparator(), i3));
    }

    @Override
    public <U> TreeSet<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        return this.zipWith((Iterable)that, Tuple::of);
    }

    @Override
    public <U, R> TreeSet<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return TreeSet.ofAll(Comparators.naturalComparator(), this.iterator().zipWith(that, mapper));
    }

    @Override
    public <U> TreeSet<Tuple2<T, U>> zipAll(Iterable<? extends U> that, T thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        Comparator tuple2Comparator = Tuple2.comparator(this.tree.comparator(), Comparators.naturalComparator());
        return TreeSet.ofAll(tuple2Comparator, this.iterator().zipAll(that, thisElem, thatElem));
    }

    @Override
    public TreeSet<Tuple2<T, Integer>> zipWithIndex() {
        Comparator component1Comparator = this.tree.comparator();
        Comparator tuple2Comparator = (t1, t2) -> component1Comparator.compare(t1._1, t2._1);
        return TreeSet.ofAll(tuple2Comparator, this.iterator().zipWithIndex());
    }

    @Override
    public <U> SortedSet<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        return TreeSet.ofAll(Comparators.naturalComparator(), this.iterator().zipWithIndex(mapper));
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
        return "TreeSet";
    }

    @Override
    public String toString() {
        return this.mkString(this.stringPrefix() + "(", ", ", ")");
    }
}

