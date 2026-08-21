/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Function1;
import io.vavr.PartialFunction;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.Array;
import io.vavr.collection.BitSetModule;
import io.vavr.collection.Collections;
import io.vavr.collection.Comparators;
import io.vavr.collection.HashSet;
import io.vavr.collection.Iterator;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.SortedSet;
import io.vavr.collection.TreeSet;
import io.vavr.control.Option;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public interface BitSet<T>
extends SortedSet<T> {
    public static final long serialVersionUID = 1L;

    public static <T> Builder<T> withRelations(Function1<Integer, T> fromInt, Function1<T, Integer> toInt) {
        return new Builder<T>(fromInt, toInt);
    }

    public static <T extends Enum<T>> Builder<T> withEnum(Class<T> enumClass) {
        return new Builder<Enum>(i -> ((Enum[])enumClass.getEnumConstants())[i], Enum::ordinal);
    }

    public static Builder<Character> withCharacters() {
        return new Builder<Character>(i -> Character.valueOf((char)i.intValue()), c -> c.charValue());
    }

    public static Builder<Byte> withBytes() {
        return new Builder<Byte>(Integer::byteValue, Byte::intValue);
    }

    public static Builder<Long> withLongs() {
        return new Builder<Long>(Integer::longValue, Long::intValue);
    }

    public static Builder<Short> withShorts() {
        return new Builder<Short>(Integer::shortValue, Short::intValue);
    }

    public static Collector<Integer, ArrayList<Integer>, BitSet<Integer>> collector() {
        return Builder.DEFAULT.collector();
    }

    public static BitSet<Integer> empty() {
        return Builder.DEFAULT.empty();
    }

    public static BitSet<Integer> of(Integer value) {
        return Builder.DEFAULT.of(value);
    }

    public static BitSet<Integer> of(Integer ... values2) {
        return Builder.DEFAULT.of((Integer[])values2);
    }

    public static BitSet<Integer> tabulate(int n, Function<Integer, Integer> f) {
        return Builder.DEFAULT.tabulate(n, f);
    }

    public static BitSet<Integer> fill(int n, Supplier<Integer> s) {
        return Builder.DEFAULT.fill(n, s);
    }

    public static BitSet<Integer> ofAll(Iterable<Integer> values2) {
        return Builder.DEFAULT.ofAll(values2);
    }

    public static BitSet<Integer> ofAll(Stream<Integer> javaStream) {
        return Builder.DEFAULT.ofAll(javaStream);
    }

    public static BitSet<Boolean> ofAll(boolean ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return BitSet.withRelations(i -> i != 0, b -> b != false ? 1 : 0).ofAll(Iterator.ofAll(elements));
    }

    public static BitSet<Byte> ofAll(byte ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return BitSet.withBytes().ofAll(Iterator.ofAll(elements));
    }

    public static BitSet<Character> ofAll(char ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return BitSet.withCharacters().ofAll(Iterator.ofAll(elements));
    }

    public static BitSet<Integer> ofAll(int ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return BitSet.ofAll(Iterator.ofAll(elements));
    }

    public static BitSet<Long> ofAll(long ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return BitSet.withLongs().ofAll(Iterator.ofAll(elements));
    }

    public static BitSet<Short> ofAll(short ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return BitSet.withShorts().ofAll(Iterator.ofAll(elements));
    }

    public static BitSet<Integer> range(int from, int toExclusive) {
        return BitSet.ofAll(Iterator.range(from, toExclusive));
    }

    public static BitSet<Character> range(char from, char toExclusive) {
        return BitSet.withCharacters().ofAll(Iterator.range(from, toExclusive));
    }

    public static BitSet<Long> range(long from, long toExclusive) {
        return BitSet.withLongs().ofAll(Iterator.range(from, toExclusive));
    }

    public static BitSet<Integer> rangeBy(int from, int toExclusive, int step) {
        return BitSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static BitSet<Character> rangeBy(char from, char toExclusive, int step) {
        return BitSet.withCharacters().ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static BitSet<Long> rangeBy(long from, long toExclusive, long step) {
        return BitSet.withLongs().ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static BitSet<Integer> rangeClosed(int from, int toInclusive) {
        return BitSet.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static BitSet<Character> rangeClosed(char from, char toInclusive) {
        return BitSet.withCharacters().ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static BitSet<Long> rangeClosed(long from, long toInclusive) {
        return BitSet.withLongs().ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static BitSet<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return BitSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static BitSet<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return BitSet.withCharacters().ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static BitSet<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return BitSet.withLongs().ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    @Override
    public BitSet<T> add(T var1);

    @Override
    public BitSet<T> addAll(Iterable<? extends T> var1);

    @Override
    default public <R> SortedSet<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        Objects.requireNonNull(partialFunction, "partialFunction is null");
        return TreeSet.ofAll(Comparators.naturalComparator(), this.iterator().collect(partialFunction));
    }

    @Override
    default public BitSet<T> diff(Set<? extends T> elements) {
        return this.removeAll(elements);
    }

    @Override
    default public BitSet<T> distinct() {
        return this;
    }

    @Override
    public BitSet<T> distinctBy(Comparator<? super T> var1);

    @Override
    public <U> BitSet<T> distinctBy(Function<? super T, ? extends U> var1);

    @Override
    public BitSet<T> drop(int var1);

    @Override
    public BitSet<T> dropRight(int var1);

    @Override
    default public BitSet<T> dropUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.dropWhile((Predicate)predicate.negate());
    }

    @Override
    public BitSet<T> dropWhile(Predicate<? super T> var1);

    @Override
    public BitSet<T> filter(Predicate<? super T> var1);

    @Override
    public BitSet<T> reject(Predicate<? super T> var1);

    @Override
    default public <U> SortedSet<U> flatMap(Comparator<? super U> comparator, Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return TreeSet.ofAll(comparator, this.iterator().flatMap(mapper));
    }

    @Override
    default public <U> SortedSet<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        return this.flatMap(Comparators.naturalComparator(), mapper);
    }

    @Override
    default public <U> U foldRight(U zero, BiFunction<? super T, ? super U, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return this.iterator().foldRight(zero, f);
    }

    @Override
    public <C> Map<C, BitSet<T>> groupBy(Function<? super T, ? extends C> var1);

    @Override
    default public Iterator<BitSet<T>> grouped(int size) {
        return this.sliding(size, size);
    }

    @Override
    default public boolean hasDefiniteSize() {
        return true;
    }

    @Override
    public BitSet<T> init();

    @Override
    default public Option<BitSet<T>> initOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.init());
    }

    @Override
    default public boolean isAsync() {
        return false;
    }

    @Override
    default public boolean isTraversableAgain() {
        return true;
    }

    @Override
    default public boolean isLazy() {
        return false;
    }

    @Override
    public BitSet<T> intersect(Set<? extends T> var1);

    @Override
    default public T last() {
        return Collections.last(this);
    }

    @Override
    public Tuple2<BitSet<T>, BitSet<T>> partition(Predicate<? super T> var1);

    @Override
    default public BitSet<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (!this.isEmpty()) {
            action.accept(this.head());
        }
        return this;
    }

    @Override
    default public String stringPrefix() {
        return "BitSet";
    }

    @Override
    default public <U> SortedSet<U> map(Comparator<? super U> comparator, Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return TreeSet.ofAll(comparator, this.iterator().map(mapper));
    }

    @Override
    default public <U> SortedSet<U> map(Function<? super T, ? extends U> mapper) {
        return this.map(Comparators.naturalComparator(), mapper);
    }

    @Override
    public BitSet<T> remove(T var1);

    @Override
    public BitSet<T> removeAll(Iterable<? extends T> var1);

    @Override
    default public BitSet<T> replace(T currentElement, T newElement) {
        if (this.contains(currentElement)) {
            return this.remove((Object)currentElement).add((Object)newElement);
        }
        return this;
    }

    @Override
    default public BitSet<T> replaceAll(T currentElement, T newElement) {
        return this.replace((Object)currentElement, (Object)newElement);
    }

    @Override
    default public BitSet<T> retainAll(Iterable<? extends T> elements) {
        return Collections.retainAll(this, elements);
    }

    @Override
    public BitSet<T> scan(T var1, BiFunction<? super T, ? super T, ? extends T> var2);

    @Override
    default public <U> Set<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        return Collections.scanLeft(this, zero, operation, HashSet::ofAll);
    }

    @Override
    default public <U> Set<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        return Collections.scanRight(this, zero, operation, HashSet::ofAll);
    }

    @Override
    public Iterator<BitSet<T>> slideBy(Function<? super T, ?> var1);

    @Override
    default public Iterator<BitSet<T>> sliding(int size) {
        return this.sliding(size, 1);
    }

    @Override
    public Iterator<BitSet<T>> sliding(int var1, int var2);

    @Override
    public Tuple2<BitSet<T>, BitSet<T>> span(Predicate<? super T> var1);

    @Override
    default public BitSet<T> tail() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("tail of empty BitSet");
        }
        return this.drop(1);
    }

    @Override
    default public Option<BitSet<T>> tailOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.tail());
    }

    @Override
    public BitSet<T> take(int var1);

    @Override
    public BitSet<T> takeRight(int var1);

    @Override
    default public BitSet<T> takeUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.takeWhile((Predicate)predicate.negate());
    }

    @Override
    public BitSet<T> takeWhile(Predicate<? super T> var1);

    @Override
    default public java.util.SortedSet<T> toJavaSet() {
        return this.toJavaSet(ignore -> new java.util.TreeSet(this.comparator()));
    }

    @Override
    default public BitSet<T> union(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        return elements.isEmpty() ? this : this.addAll(elements);
    }

    @Override
    default public <T1, T2> Tuple2<TreeSet<T1>, TreeSet<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        return this.iterator().unzip(unzipper).map((? super T1 i1) -> TreeSet.ofAll(Comparators.naturalComparator(), i1), (? super T2 i2) -> TreeSet.ofAll(Comparators.naturalComparator(), i2));
    }

    @Override
    default public <T1, T2, T3> Tuple3<TreeSet<T1>, TreeSet<T2>, TreeSet<T3>> unzip3(Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        return this.iterator().unzip3(unzipper).map(i1 -> TreeSet.ofAll(Comparators.naturalComparator(), i1), i2 -> TreeSet.ofAll(Comparators.naturalComparator(), i2), i3 -> TreeSet.ofAll(Comparators.naturalComparator(), i3));
    }

    @Override
    default public <U> TreeSet<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        Objects.requireNonNull(that, "that is null");
        Comparator tuple2Comparator = Tuple2.comparator(this.comparator(), Comparators.naturalComparator());
        return TreeSet.ofAll(tuple2Comparator, this.iterator().zip(that));
    }

    @Override
    default public <U, R> TreeSet<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return TreeSet.ofAll(Comparators.naturalComparator(), this.iterator().zipWith(that, mapper));
    }

    @Override
    default public <U> TreeSet<Tuple2<T, U>> zipAll(Iterable<? extends U> that, T thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        Comparator tuple2Comparator = Tuple2.comparator(this.comparator(), Comparators.naturalComparator());
        return TreeSet.ofAll(tuple2Comparator, this.iterator().zipAll(that, thisElem, thatElem));
    }

    @Override
    default public TreeSet<Tuple2<T, Integer>> zipWithIndex() {
        Comparator component1Comparator = this.comparator();
        Comparator tuple2Comparator = (t1, t2) -> component1Comparator.compare(t1._1, t2._1);
        return TreeSet.ofAll(tuple2Comparator, this.iterator().zipWithIndex());
    }

    @Override
    default public <U> TreeSet<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return TreeSet.ofAll(Comparators.naturalComparator(), this.iterator().zipWithIndex(mapper));
    }

    public static class Builder<T> {
        static final Builder<Integer> DEFAULT = new Builder<Integer>(i -> i, i -> i);
        final Function1<Integer, T> fromInt;
        final Function1<T, Integer> toInt;

        Builder(Function1<Integer, T> fromInt, Function1<T, Integer> toInt) {
            this.fromInt = fromInt;
            this.toInt = toInt;
        }

        public Collector<T, ArrayList<T>, BitSet<T>> collector() {
            BinaryOperator combiner = (left, right) -> {
                left.addAll(right);
                return left;
            };
            return Collector.of(ArrayList::new, ArrayList::add, combiner, this::ofAll, new Collector.Characteristics[0]);
        }

        public BitSet<T> empty() {
            return new BitSetModule.BitSet1<T>(this.fromInt, this.toInt, 0L);
        }

        public BitSet<T> of(T t) {
            int value = this.toInt.apply(t);
            if (value < 64) {
                return new BitSetModule.BitSet1<T>(this.fromInt, this.toInt, 1L << value);
            }
            if (value < 128) {
                return new BitSetModule.BitSet2<T>(this.fromInt, this.toInt, 0L, 1L << value);
            }
            return this.empty().add((Object)t);
        }

        @SafeVarargs
        public final BitSet<T> of(T ... values2) {
            return this.empty().addAll((Iterable)Array.wrap(values2));
        }

        public BitSet<T> ofAll(Iterable<? extends T> values2) {
            Objects.requireNonNull(values2, "values is null");
            return this.empty().addAll((Iterable)values2);
        }

        public BitSet<T> ofAll(Stream<? extends T> javaStream) {
            Objects.requireNonNull(javaStream, "javaStream is null");
            return this.empty().addAll((Iterable)Iterator.ofAll(javaStream.iterator()));
        }

        public BitSet<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
            Objects.requireNonNull(f, "f is null");
            return this.empty().addAll((Iterable)Collections.tabulate(n, f));
        }

        public BitSet<T> fill(int n, Supplier<? extends T> s) {
            Objects.requireNonNull(s, "s is null");
            return this.empty().addAll((Iterable)Collections.fill(n, s));
        }
    }
}

