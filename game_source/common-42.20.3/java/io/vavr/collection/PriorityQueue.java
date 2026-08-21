/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.PartialFunction;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.AbstractQueue;
import io.vavr.collection.Collections;
import io.vavr.collection.Comparators;
import io.vavr.collection.GwtIncompatible;
import io.vavr.collection.Iterator;
import io.vavr.collection.LinearSeq;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Ordered;
import io.vavr.collection.PriorityQueueBase;
import io.vavr.collection.Seq;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public final class PriorityQueue<T>
extends AbstractQueue<T, PriorityQueue<T>>
implements Serializable,
Ordered<T> {
    private static final long serialVersionUID = 1L;
    private final Comparator<? super T> comparator;
    private final Seq<PriorityQueueBase.Node<T>> forest;
    private final int size;

    private PriorityQueue(Comparator<? super T> comparator, Seq<PriorityQueueBase.Node<T>> forest, int size) {
        this.comparator = comparator;
        this.forest = forest;
        this.size = size;
    }

    private PriorityQueue<T> with(Seq<PriorityQueueBase.Node<T>> forest, int size) {
        return new PriorityQueue<T>(this.comparator, forest, size);
    }

    @Override
    public <R> PriorityQueue<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        return PriorityQueue.ofAll(Comparators.naturalComparator(), this.iterator().collect(partialFunction));
    }

    @Override
    public PriorityQueue<T> enqueue(T element) {
        Seq<PriorityQueueBase.Node<T>> result = PriorityQueueBase.insert(this.comparator, element, this.forest);
        return this.with(result, this.size + 1);
    }

    @Override
    public PriorityQueue<T> enqueueAll(Iterable<? extends T> elements) {
        return this.merge(PriorityQueue.ofAll(this.comparator, elements));
    }

    @Override
    public T head() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("head of empty " + this.stringPrefix());
        }
        return PriorityQueueBase.findMin(this.comparator, this.forest).root;
    }

    @Override
    public PriorityQueue<T> tail() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("tail of empty " + this.stringPrefix());
        }
        return (PriorityQueue)this.dequeue()._2;
    }

    @Override
    public Tuple2<T, PriorityQueue<T>> dequeue() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("dequeue of empty " + this.stringPrefix());
        }
        Tuple2<? super T, Seq<PriorityQueueBase.Node<? super T>>> dequeue = PriorityQueueBase.deleteMin(this.comparator, this.forest);
        return Tuple.of(dequeue._1, this.with((Seq)dequeue._2, this.size - 1));
    }

    public PriorityQueue<T> merge(PriorityQueue<T> target) {
        Seq<PriorityQueueBase.Node<T>> meld = PriorityQueueBase.meld(this.comparator, this.forest, target.forest);
        return this.with(meld, this.size + target.size);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean isLazy() {
        return false;
    }

    @Override
    public boolean isOrdered() {
        return true;
    }

    public static <T extends Comparable<? super T>> PriorityQueue<T> empty() {
        return PriorityQueue.empty(Comparators.naturalComparator());
    }

    public static <T> PriorityQueue<T> empty(Comparator<? super T> comparator) {
        return new PriorityQueue<T>(comparator, List.empty(), 0);
    }

    public static <T> Collector<T, ArrayList<T>, PriorityQueue<T>> collector() {
        Supplier<ArrayList> supplier = ArrayList::new;
        BiConsumer<ArrayList, Object> accumulator = ArrayList::add;
        BinaryOperator combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        Function<ArrayList, PriorityQueue> finisher = values2 -> PriorityQueue.ofAll(Comparators.naturalComparator(), values2);
        return Collector.of(supplier, accumulator, combiner, finisher, new Collector.Characteristics[0]);
    }

    public static <T> PriorityQueue<T> narrow(PriorityQueue<? extends T> queue) {
        return queue;
    }

    public static <T extends Comparable<? super T>> PriorityQueue<T> of(T element) {
        return PriorityQueue.of(Comparators.naturalComparator(), element);
    }

    public static <T extends Comparable<? super T>> PriorityQueue<T> of(T ... elements) {
        return PriorityQueue.ofAll(Comparators.naturalComparator(), List.of(elements));
    }

    public static <T> PriorityQueue<T> of(Comparator<? super T> comparator, T element) {
        return PriorityQueue.ofAll(comparator, List.of(element));
    }

    public static <T> PriorityQueue<T> of(Comparator<? super T> comparator, T ... elements) {
        return PriorityQueue.ofAll(comparator, List.of(elements));
    }

    public static <T extends Comparable<? super T>> PriorityQueue<T> ofAll(Iterable<? extends T> elements) {
        return PriorityQueue.ofAll(Comparators.naturalComparator(), elements);
    }

    public static <T> PriorityQueue<T> ofAll(Comparator<? super T> comparator, Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (elements instanceof PriorityQueue && ((PriorityQueue)elements).comparator == comparator) {
            return (PriorityQueue)elements;
        }
        int size = 0;
        Seq<PriorityQueueBase.Node<Object>> forest = List.empty();
        for (T value : elements) {
            forest = PriorityQueueBase.insert(comparator, value, forest);
            ++size;
        }
        return new PriorityQueue<T>(comparator, forest, size);
    }

    public static <T extends Comparable<? super T>> PriorityQueue<T> ofAll(Stream<? extends T> javaStream) {
        return PriorityQueue.ofAll(Comparators.naturalComparator(), Iterator.ofAll(javaStream.iterator()));
    }

    public static <T> PriorityQueue<T> ofAll(Comparator<? super T> comparator, Stream<? extends T> javaStream) {
        return PriorityQueue.ofAll(comparator, Iterator.ofAll(javaStream.iterator()));
    }

    @GwtIncompatible
    public static <T> PriorityQueue<T> tabulate(int size, Function<? super Integer, ? extends T> function) {
        Objects.requireNonNull(function, "function is null");
        Comparator comparator = Comparators.naturalComparator();
        return Collections.tabulate(size, function, PriorityQueue.empty(comparator), values2 -> PriorityQueue.ofAll(comparator, List.of(values2)));
    }

    @GwtIncompatible
    public static <T> PriorityQueue<T> fill(int size, Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        Comparator comparator = Comparators.naturalComparator();
        return Collections.fill(size, supplier, PriorityQueue.empty(comparator), values2 -> PriorityQueue.ofAll(comparator, List.of(values2)));
    }

    @GwtIncompatible
    public static <T> PriorityQueue<T> fill(int size, T element) {
        Comparator comparator = Comparators.naturalComparator();
        return Collections.fillObject(size, element, PriorityQueue.empty(comparator), values2 -> PriorityQueue.ofAll(comparator, List.of(values2)));
    }

    @Override
    public List<T> toList() {
        LinearSeq results = List.empty();
        PriorityQueue queue = this;
        while (!queue.isEmpty()) {
            Tuple2<T, PriorityQueue<T>> dequeue = queue.dequeue();
            results = results.prepend(dequeue._1);
            queue = (PriorityQueue)dequeue._2;
        }
        return results.reverse();
    }

    @Override
    public PriorityQueue<T> distinct() {
        return this.distinctBy((Comparator)this.comparator);
    }

    @Override
    public PriorityQueue<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return this.isEmpty() ? this : PriorityQueue.ofAll(comparator, this.iterator().distinctBy(comparator));
    }

    @Override
    public <U> PriorityQueue<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        return this.isEmpty() ? this : PriorityQueue.ofAll(this.comparator, this.iterator().distinctBy(keyExtractor));
    }

    @Override
    public PriorityQueue<T> drop(int n) {
        if (n <= 0 || this.isEmpty()) {
            return this;
        }
        if (n >= this.length()) {
            return PriorityQueue.empty(this.comparator);
        }
        return PriorityQueue.ofAll(this.comparator, this.iterator().drop(n));
    }

    @Override
    public PriorityQueue<T> dropRight(int n) {
        if (n <= 0 || this.isEmpty()) {
            return this;
        }
        if (n >= this.length()) {
            return PriorityQueue.empty(this.comparator);
        }
        return PriorityQueue.ofAll(this.comparator, this.iterator().dropRight(n));
    }

    @Override
    public PriorityQueue<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        AbstractQueue result = this;
        while (!result.isEmpty() && predicate.test(result.head())) {
            result = result.tail();
        }
        return result;
    }

    @Override
    public PriorityQueue<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (this.isEmpty()) {
            return this;
        }
        return PriorityQueue.ofAll(this.comparator, this.iterator().filter(predicate));
    }

    @Override
    public <U> PriorityQueue<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        return this.flatMap(Comparators.naturalComparator(), mapper);
    }

    public <U> PriorityQueue<U> flatMap(Comparator<U> comparator, Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return PriorityQueue.ofAll(comparator, this.iterator().flatMap(mapper));
    }

    @Override
    public <U> U foldRight(U zero, BiFunction<? super T, ? super U, ? extends U> accumulator) {
        Objects.requireNonNull(zero, "zero is null");
        Objects.requireNonNull(accumulator, "accumulator is null");
        return this.toList().foldRight(zero, accumulator);
    }

    @Override
    public <C> Map<C, ? extends PriorityQueue<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return Collections.groupBy(this, classifier, elements -> PriorityQueue.ofAll(this.comparator, elements));
    }

    @Override
    public Iterator<? extends PriorityQueue<T>> grouped(int size) {
        return this.sliding(size, size);
    }

    @Override
    public boolean hasDefiniteSize() {
        return true;
    }

    @Override
    public PriorityQueue<T> init() {
        return PriorityQueue.ofAll(this.comparator, this.iterator().init());
    }

    @Override
    public boolean isTraversableAgain() {
        return true;
    }

    @Override
    public T last() {
        return Collections.last(this);
    }

    @Override
    public int length() {
        return this.size;
    }

    @Override
    public <U> PriorityQueue<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.map(Comparators.naturalComparator(), mapper);
    }

    public <U> PriorityQueue<U> map(Comparator<U> comparator, Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return PriorityQueue.ofAll(comparator, this.iterator().map(mapper));
    }

    @Override
    public PriorityQueue<T> orElse(Iterable<? extends T> other) {
        return this.isEmpty() ? PriorityQueue.ofAll(this.comparator, other) : this;
    }

    @Override
    public PriorityQueue<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return this.isEmpty() ? PriorityQueue.ofAll(this.comparator, supplier.get()) : this;
    }

    @Override
    public Tuple2<? extends PriorityQueue<T>, ? extends PriorityQueue<T>> partition(Predicate<? super T> predicate) {
        AbstractQueue left;
        Objects.requireNonNull(predicate, "predicate is null");
        AbstractQueue right = left = PriorityQueue.empty(this.comparator);
        for (Object t : this) {
            if (predicate.test(t)) {
                left = left.enqueue(t);
                continue;
            }
            right = right.enqueue(t);
        }
        return Tuple.of(left, right);
    }

    @Override
    public PriorityQueue<T> replace(T currentElement, T newElement) {
        Objects.requireNonNull(currentElement, "currentElement is null");
        Objects.requireNonNull(newElement, "newElement is null");
        return PriorityQueue.ofAll(this.comparator, this.iterator().replace(currentElement, newElement));
    }

    @Override
    public PriorityQueue<T> replaceAll(T currentElement, T newElement) {
        Objects.requireNonNull(currentElement, "currentElement is null");
        Objects.requireNonNull(newElement, "newElement is null");
        return PriorityQueue.ofAll(this.comparator, this.iterator().replaceAll(currentElement, newElement));
    }

    @Override
    public PriorityQueue<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        return Collections.scanLeft(this, zero, operation, it -> PriorityQueue.ofAll(this.comparator, it));
    }

    @Override
    public <U> PriorityQueue<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        return Collections.scanLeft(this, zero, operation, it -> PriorityQueue.ofAll(Comparators.naturalComparator(), it));
    }

    @Override
    public <U> PriorityQueue<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        return Collections.scanRight(this, zero, operation, it -> PriorityQueue.ofAll(Comparators.naturalComparator(), it));
    }

    @Override
    public Iterator<? extends PriorityQueue<T>> slideBy(Function<? super T, ?> classifier) {
        return this.iterator().slideBy(classifier).map((T v) -> PriorityQueue.ofAll(this.comparator, v));
    }

    @Override
    public Iterator<? extends PriorityQueue<T>> sliding(int size) {
        return this.iterator().sliding(size).map((T v) -> PriorityQueue.ofAll(this.comparator, v));
    }

    @Override
    public Iterator<? extends PriorityQueue<T>> sliding(int size, int step) {
        return this.iterator().sliding(size, step).map((T v) -> PriorityQueue.ofAll(this.comparator, v));
    }

    @Override
    public Tuple2<? extends PriorityQueue<T>, ? extends PriorityQueue<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Tuple.of(this.takeWhile((Predicate)predicate), this.dropWhile((Predicate)predicate));
    }

    @Override
    public PriorityQueue<T> take(int n) {
        if (n >= this.size() || this.isEmpty()) {
            return this;
        }
        if (n <= 0) {
            return PriorityQueue.empty(this.comparator);
        }
        return PriorityQueue.ofAll(this.comparator, this.iterator().take(n));
    }

    @Override
    public PriorityQueue<T> takeRight(int n) {
        if (n >= this.size() || this.isEmpty()) {
            return this;
        }
        if (n <= 0) {
            return PriorityQueue.empty(this.comparator);
        }
        return PriorityQueue.ofAll(this.comparator, this.toArray().takeRight(n));
    }

    @Override
    public PriorityQueue<T> takeUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.isEmpty() ? this : PriorityQueue.ofAll(this.comparator, this.iterator().takeUntil(predicate));
    }

    @Override
    public <T1, T2> Tuple2<? extends PriorityQueue<T1>, ? extends PriorityQueue<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        Tuple2<Iterator<? extends T1>, Iterator<? extends T2>> unzip = this.iterator().unzip(unzipper);
        return Tuple.of(PriorityQueue.ofAll(Comparators.naturalComparator(), (Iterable)unzip._1), PriorityQueue.ofAll(Comparators.naturalComparator(), (Iterable)unzip._2));
    }

    @Override
    public <T1, T2, T3> Tuple3<? extends PriorityQueue<T1>, ? extends PriorityQueue<T2>, ? extends PriorityQueue<T3>> unzip3(Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        Tuple3<Iterator<? extends T1>, Iterator<? extends T2>, Iterator<? extends T3>> unzip3 = this.iterator().unzip3(unzipper);
        return Tuple.of(PriorityQueue.ofAll(Comparators.naturalComparator(), (Iterable)unzip3._1), PriorityQueue.ofAll(Comparators.naturalComparator(), (Iterable)unzip3._2), PriorityQueue.ofAll(Comparators.naturalComparator(), (Iterable)unzip3._3));
    }

    @Override
    public <U> PriorityQueue<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        return this.zipWith((Iterable)that, Tuple::of);
    }

    @Override
    public <U, R> PriorityQueue<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return PriorityQueue.ofAll(Comparators.naturalComparator(), this.iterator().zipWith(that, mapper));
    }

    @Override
    public <U> PriorityQueue<Tuple2<T, U>> zipAll(Iterable<? extends U> that, T thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        return PriorityQueue.ofAll(this.iterator().zipAll(that, thisElem, thatElem));
    }

    @Override
    public PriorityQueue<Tuple2<T, Integer>> zipWithIndex() {
        return this.zipWithIndex(Tuple::of);
    }

    @Override
    public <U> PriorityQueue<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return PriorityQueue.ofAll(Comparators.naturalComparator(), this.iterator().zipWithIndex(mapper));
    }

    @Override
    public String stringPrefix() {
        return "PriorityQueue";
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof PriorityQueue && Collections.areEqual(this, (Iterable)o);
    }

    @Override
    public int hashCode() {
        return Collections.hashOrdered(this);
    }

    @Override
    public Comparator<T> comparator() {
        return this.comparator;
    }
}

