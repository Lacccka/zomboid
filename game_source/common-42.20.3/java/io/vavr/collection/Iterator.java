/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.PartialFunction;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.AbstractIterator;
import io.vavr.collection.Collections;
import io.vavr.collection.GwtIncompatible;
import io.vavr.collection.HashSet;
import io.vavr.collection.IteratorModule;
import io.vavr.collection.LinearSeq;
import io.vavr.collection.Map;
import io.vavr.collection.Queue;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.collection.Traversable;
import io.vavr.collection.TreeSet;
import io.vavr.control.Option;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Iterator<T>
extends java.util.Iterator<T>,
Traversable<T> {
    @SafeVarargs
    public static <T> Iterator<T> concat(Iterable<? extends T> ... iterables) {
        Objects.requireNonNull(iterables, "iterables is null");
        if (iterables.length == 0) {
            return Iterator.empty();
        }
        IteratorModule.ConcatIterator<? extends T> res = new IteratorModule.ConcatIterator<T>();
        for (Iterable<T> iterable : iterables) {
            res.append(iterable.iterator());
        }
        return res;
    }

    public static <T> Iterator<T> concat(Iterable<? extends Iterable<? extends T>> iterables) {
        Objects.requireNonNull(iterables, "iterables is null");
        if (!iterables.iterator().hasNext()) {
            return Iterator.empty();
        }
        IteratorModule.ConcatIterator<? extends T> res = new IteratorModule.ConcatIterator<T>();
        for (Iterable<T> iterable : iterables) {
            res.append(iterable.iterator());
        }
        return res;
    }

    public static <T> Iterator<T> empty() {
        return IteratorModule.EmptyIterator.INSTANCE;
    }

    public static <T> Iterator<T> narrow(Iterator<? extends T> iterator2) {
        return iterator2;
    }

    public static <T> Iterator<T> of(final T element) {
        return new AbstractIterator<T>(){
            boolean hasNext = true;

            @Override
            public boolean hasNext() {
                return this.hasNext;
            }

            @Override
            public T getNext() {
                this.hasNext = false;
                return element;
            }
        };
    }

    @SafeVarargs
    public static <T> Iterator<T> of(final T ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (elements.length == 0) {
            return Iterator.empty();
        }
        return new AbstractIterator<T>(){
            int index = 0;

            @Override
            public boolean hasNext() {
                return this.index < elements.length;
            }

            @Override
            public T getNext() {
                return elements[this.index++];
            }
        };
    }

    public static <T> Iterator<T> ofAll(Iterable<? extends T> iterable) {
        Objects.requireNonNull(iterable, "iterable is null");
        if (iterable instanceof Iterator) {
            return (Iterator)iterable;
        }
        return Iterator.ofAll(iterable.iterator());
    }

    public static <T> Iterator<T> ofAll(final java.util.Iterator<? extends T> iterator2) {
        Objects.requireNonNull(iterator2, "iterator is null");
        if (iterator2 instanceof Iterator) {
            return (Iterator)iterator2;
        }
        return new AbstractIterator<T>(){

            @Override
            public boolean hasNext() {
                return iterator2.hasNext();
            }

            @Override
            public T getNext() {
                return iterator2.next();
            }
        };
    }

    public static Iterator<Boolean> ofAll(final boolean ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new AbstractIterator<Boolean>(){
            int i = 0;

            @Override
            public boolean hasNext() {
                return this.i < elements.length;
            }

            @Override
            public Boolean getNext() {
                return elements[this.i++];
            }
        };
    }

    public static Iterator<Byte> ofAll(final byte ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new AbstractIterator<Byte>(){
            int i = 0;

            @Override
            public boolean hasNext() {
                return this.i < elements.length;
            }

            @Override
            public Byte getNext() {
                return elements[this.i++];
            }
        };
    }

    public static Iterator<Character> ofAll(final char ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new AbstractIterator<Character>(){
            int i = 0;

            @Override
            public boolean hasNext() {
                return this.i < elements.length;
            }

            @Override
            public Character getNext() {
                return Character.valueOf(elements[this.i++]);
            }
        };
    }

    public static Iterator<Double> ofAll(final double ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new AbstractIterator<Double>(){
            int i = 0;

            @Override
            public boolean hasNext() {
                return this.i < elements.length;
            }

            @Override
            public Double getNext() {
                return elements[this.i++];
            }
        };
    }

    public static Iterator<Float> ofAll(final float ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new AbstractIterator<Float>(){
            int i = 0;

            @Override
            public boolean hasNext() {
                return this.i < elements.length;
            }

            @Override
            public Float getNext() {
                return Float.valueOf(elements[this.i++]);
            }
        };
    }

    public static Iterator<Integer> ofAll(final int ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new AbstractIterator<Integer>(){
            int i = 0;

            @Override
            public boolean hasNext() {
                return this.i < elements.length;
            }

            @Override
            public Integer getNext() {
                return elements[this.i++];
            }
        };
    }

    public static Iterator<Long> ofAll(final long ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new AbstractIterator<Long>(){
            int i = 0;

            @Override
            public boolean hasNext() {
                return this.i < elements.length;
            }

            @Override
            public Long getNext() {
                return elements[this.i++];
            }
        };
    }

    public static Iterator<Short> ofAll(final short ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new AbstractIterator<Short>(){
            int i = 0;

            @Override
            public boolean hasNext() {
                return this.i < elements.length;
            }

            @Override
            public Short getNext() {
                return elements[this.i++];
            }
        };
    }

    public static <T> Iterator<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return Collections.tabulate(n, f);
    }

    public static <T> Iterator<T> fill(int n, Supplier<? extends T> s) {
        Objects.requireNonNull(s, "s is null");
        return Collections.fill(n, s);
    }

    public static <T> Iterator<T> fill(int n, T element) {
        return Collections.fillObject(n, element);
    }

    public static Iterator<Character> range(char from, char toExclusive) {
        return Iterator.rangeBy(from, toExclusive, 1);
    }

    public static Iterator<Character> rangeBy(char from, char toExclusive, int step) {
        return Iterator.rangeBy((int)from, (int)toExclusive, step).map((T i) -> Character.valueOf((char)i.shortValue()));
    }

    @GwtIncompatible(value="BigDecimalHelper is GwtIncompatible")
    public static Iterator<Double> rangeBy(double from, double toExclusive, double step) {
        BigDecimal fromDecimal = IteratorModule.BigDecimalHelper.asDecimal(from);
        BigDecimal toDecimal = IteratorModule.BigDecimalHelper.asDecimal(toExclusive);
        BigDecimal stepDecimal = IteratorModule.BigDecimalHelper.asDecimal(step);
        return Iterator.rangeBy(fromDecimal, toDecimal, stepDecimal).map(BigDecimal::doubleValue);
    }

    public static Iterator<BigDecimal> rangeBy(final BigDecimal from, final BigDecimal toExclusive, final BigDecimal step) {
        if (step.signum() == 0) {
            throw new IllegalArgumentException("step cannot be 0");
        }
        if (IteratorModule.BigDecimalHelper.areEqual(from, toExclusive) || step.signum() == from.subtract(toExclusive).signum()) {
            return Iterator.empty();
        }
        if (step.signum() > 0) {
            return new AbstractIterator<BigDecimal>(){
                BigDecimal i;
                {
                    this.i = from;
                }

                @Override
                public boolean hasNext() {
                    return this.i.compareTo(toExclusive) < 0;
                }

                @Override
                public BigDecimal getNext() {
                    BigDecimal next = this.i;
                    this.i = next.add(step);
                    return next;
                }
            };
        }
        return new AbstractIterator<BigDecimal>(){
            BigDecimal i;
            {
                this.i = from;
            }

            @Override
            public boolean hasNext() {
                return this.i.compareTo(toExclusive) > 0;
            }

            @Override
            public BigDecimal getNext() {
                BigDecimal next = this.i;
                this.i = next.add(step);
                return next;
            }
        };
    }

    public static Iterator<Integer> range(int from, int toExclusive) {
        return Iterator.rangeBy(from, toExclusive, 1);
    }

    public static Iterator<Integer> rangeBy(int from, int toExclusive, int step) {
        int toInclusive = toExclusive - (step > 0 ? 1 : -1);
        return Iterator.rangeClosedBy(from, toInclusive, step);
    }

    public static Iterator<Long> range(long from, long toExclusive) {
        return Iterator.rangeBy(from, toExclusive, 1L);
    }

    public static Iterator<Long> rangeBy(long from, long toExclusive, long step) {
        long toInclusive = toExclusive - (long)(step > 0L ? 1 : -1);
        return Iterator.rangeClosedBy(from, toInclusive, step);
    }

    public static Iterator<Character> rangeClosed(char from, char toInclusive) {
        return Iterator.rangeClosedBy(from, toInclusive, 1);
    }

    public static Iterator<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return Iterator.rangeClosedBy((int)from, (int)toInclusive, step).map((T i) -> Character.valueOf((char)i.shortValue()));
    }

    @GwtIncompatible
    public static Iterator<Double> rangeClosedBy(double from, double toInclusive, double step) {
        if (from == toInclusive) {
            return Iterator.of(Double.valueOf(from));
        }
        double toExclusive = step > 0.0 ? Math.nextUp(toInclusive) : Math.nextDown(toInclusive);
        return Iterator.rangeBy(from, toExclusive, step);
    }

    public static Iterator<Integer> rangeClosed(int from, int toInclusive) {
        return Iterator.rangeClosedBy(from, toInclusive, 1);
    }

    public static Iterator<Integer> rangeClosedBy(final int from, int toInclusive, final int step) {
        if (step == 0) {
            throw new IllegalArgumentException("step cannot be 0");
        }
        if (from == toInclusive) {
            return Iterator.of(Integer.valueOf(from));
        }
        if (Integer.signum(step) == Integer.signum(from - toInclusive)) {
            return Iterator.empty();
        }
        final int end = toInclusive - step;
        if (step > 0) {
            return new AbstractIterator<Integer>(){
                int i;
                {
                    this.i = from - step;
                }

                @Override
                public boolean hasNext() {
                    return this.i <= end;
                }

                @Override
                public Integer getNext() {
                    return this.i += step;
                }
            };
        }
        return new AbstractIterator<Integer>(){
            int i;
            {
                this.i = from - step;
            }

            @Override
            public boolean hasNext() {
                return this.i >= end;
            }

            @Override
            public Integer getNext() {
                return this.i += step;
            }
        };
    }

    public static Iterator<Long> rangeClosed(long from, long toInclusive) {
        return Iterator.rangeClosedBy(from, toInclusive, 1L);
    }

    public static Iterator<Long> rangeClosedBy(final long from, long toInclusive, final long step) {
        if (step == 0L) {
            throw new IllegalArgumentException("step cannot be 0");
        }
        if (from == toInclusive) {
            return Iterator.of(Long.valueOf(from));
        }
        if (Long.signum(step) == Long.signum(from - toInclusive)) {
            return Iterator.empty();
        }
        final long end = toInclusive - step;
        if (step > 0L) {
            return new AbstractIterator<Long>(){
                long i;
                {
                    this.i = from - step;
                }

                @Override
                public boolean hasNext() {
                    return this.i <= end;
                }

                @Override
                public Long getNext() {
                    return this.i += step;
                }
            };
        }
        return new AbstractIterator<Long>(){
            long i;
            {
                this.i = from - step;
            }

            @Override
            public boolean hasNext() {
                return this.i >= end;
            }

            @Override
            public Long getNext() {
                return this.i += step;
            }
        };
    }

    public static Iterator<Integer> from(final int value) {
        return new AbstractIterator<Integer>(){
            private int next;
            {
                this.next = value;
            }

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer getNext() {
                return this.next++;
            }
        };
    }

    public static Iterator<Integer> from(final int value, final int step) {
        return new AbstractIterator<Integer>(){
            private int next;
            {
                this.next = value;
            }

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer getNext() {
                int result = this.next;
                this.next += step;
                return result;
            }
        };
    }

    public static Iterator<Long> from(final long value) {
        return new AbstractIterator<Long>(){
            private long next;
            {
                this.next = value;
            }

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Long getNext() {
                return this.next++;
            }
        };
    }

    public static Iterator<Long> from(final long value, final long step) {
        return new AbstractIterator<Long>(){
            private long next;
            {
                this.next = value;
            }

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Long getNext() {
                long result = this.next;
                this.next += step;
                return result;
            }
        };
    }

    public static <T> Iterator<T> continually(final Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return new AbstractIterator<T>(){

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T getNext() {
                return supplier.get();
            }
        };
    }

    public static <T> Iterator<T> iterate(final Supplier<? extends Option<? extends T>> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return new AbstractIterator<T>(){
            Option<? extends T> nextOption;

            @Override
            public boolean hasNext() {
                if (this.nextOption == null) {
                    this.nextOption = (Option)supplier.get();
                }
                return this.nextOption.isDefined();
            }

            @Override
            public T getNext() {
                Object next = this.nextOption.get();
                this.nextOption = null;
                return next;
            }
        };
    }

    public static <T> Iterator<T> iterate(final T seed, final Function<? super T, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return new AbstractIterator<T>(){
            Function<? super T, ? extends T> nextFunc = s -> {
                this.nextFunc = f;
                return seed;
            };
            T current = null;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T getNext() {
                this.current = this.nextFunc.apply(this.current);
                return this.current;
            }
        };
    }

    public static <T> Iterator<T> continually(final T t) {
        return new AbstractIterator<T>(){

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T getNext() {
                return t;
            }
        };
    }

    @Override
    default public <R> Iterator<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        Objects.requireNonNull(partialFunction, "partialFunction is null");
        return this.filter(partialFunction::isDefinedAt).map(partialFunction::apply);
    }

    default public Iterator<T> concat(java.util.Iterator<? extends T> that) {
        Objects.requireNonNull(that, "that is null");
        if (!that.hasNext()) {
            return this;
        }
        if (!this.hasNext()) {
            return Iterator.ofAll(that);
        }
        return Iterator.concat(this, Iterator.ofAll(that));
    }

    default public Iterator<T> intersperse(final T element) {
        if (!this.hasNext()) {
            return Iterator.empty();
        }
        final Iterator that = this;
        return new AbstractIterator<T>(){
            boolean insertElement = false;

            @Override
            public boolean hasNext() {
                return that.hasNext();
            }

            @Override
            public T getNext() {
                if (this.insertElement) {
                    this.insertElement = false;
                    return element;
                }
                this.insertElement = true;
                return that.next();
            }
        };
    }

    default public <U> U transform(Function<? super Iterator<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    default public <U> Iterator<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        return this.zipWith((Iterable)that, Tuple::of);
    }

    @Override
    default public <U, R> Iterator<R> zipWith(Iterable<? extends U> that, final BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        if (this.isEmpty()) {
            return Iterator.empty();
        }
        final Iterator it1 = this;
        final java.util.Iterator<? extends U> it2 = that.iterator();
        return new AbstractIterator<R>(){

            @Override
            public boolean hasNext() {
                return it1.hasNext() && it2.hasNext();
            }

            @Override
            public R getNext() {
                return mapper.apply(it1.next(), it2.next());
            }
        };
    }

    @Override
    default public <U> Iterator<Tuple2<T, U>> zipAll(Iterable<? extends U> that, final T thisElem, final U thatElem) {
        Objects.requireNonNull(that, "that is null");
        final java.util.Iterator<U> thatIt = that.iterator();
        if (this.isEmpty() && !thatIt.hasNext()) {
            return Iterator.empty();
        }
        final Iterator thisIt = this;
        return new AbstractIterator<Tuple2<T, U>>(){

            @Override
            public boolean hasNext() {
                return thisIt.hasNext() || thatIt.hasNext();
            }

            @Override
            public Tuple2<T, U> getNext() {
                Object v1 = thisIt.hasNext() ? thisIt.next() : thisElem;
                Object v2 = thatIt.hasNext() ? thatIt.next() : thatElem;
                return Tuple.of(v1, v2);
            }
        };
    }

    @Override
    default public Iterator<Tuple2<T, Integer>> zipWithIndex() {
        return this.zipWithIndex(Tuple::of);
    }

    @Override
    default public <U> Iterator<U> zipWithIndex(final BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (this.isEmpty()) {
            return Iterator.empty();
        }
        final Iterator it1 = this;
        return new AbstractIterator<U>(){
            private int index = 0;

            @Override
            public boolean hasNext() {
                return it1.hasNext();
            }

            @Override
            public U getNext() {
                return mapper.apply(it1.next(), this.index++);
            }
        };
    }

    @Override
    default public <T1, T2> Tuple2<Iterator<T1>, Iterator<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        if (!this.hasNext()) {
            return Tuple.of(Iterator.empty(), Iterator.empty());
        }
        Stream source2 = Stream.ofAll(this.map(unzipper));
        return Tuple.of(source2.map((T t) -> t._1).iterator(), source2.map((T t) -> t._2).iterator());
    }

    @Override
    default public <T1, T2, T3> Tuple3<Iterator<T1>, Iterator<T2>, Iterator<T3>> unzip3(Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        if (!this.hasNext()) {
            return Tuple.of(Iterator.empty(), Iterator.empty(), Iterator.empty());
        }
        Stream source2 = Stream.ofAll(this.map(unzipper));
        return Tuple.of(source2.map((T t) -> t._1).iterator(), source2.map((T t) -> t._2).iterator(), source2.map((T t) -> t._3).iterator());
    }

    public static <T> Iterator<T> unfold(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends T>>> f) {
        return Iterator.unfoldLeft(seed, f);
    }

    public static <T, U> Iterator<U> unfoldLeft(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends U>>> f) {
        Objects.requireNonNull(f, "f is null");
        return Stream.ofAll(Iterator.unfoldRight(seed, f.andThen(tupleOpt -> tupleOpt.map((T t) -> Tuple.of(t._2, t._1))))).reverse().iterator();
    }

    public static <T, U> Iterator<U> unfoldRight(final T seed, final Function<? super T, Option<Tuple2<? extends U, ? extends T>>> f) {
        Objects.requireNonNull(f, "the unfold iterating function is null");
        return new AbstractIterator<U>(){
            private Option<Tuple2<? extends U, ? extends T>> nextVal;
            {
                this.nextVal = (Option)f.apply(seed);
            }

            @Override
            public boolean hasNext() {
                return this.nextVal.isDefined();
            }

            @Override
            public U getNext() {
                Object result = this.nextVal.get()._1;
                this.nextVal = (Option)f.apply(this.nextVal.get()._2);
                return result;
            }
        };
    }

    @Override
    default public Iterator<T> distinct() {
        if (!this.hasNext()) {
            return Iterator.empty();
        }
        return new IteratorModule.DistinctIterator(this, HashSet.empty(), Function.identity());
    }

    @Override
    default public Iterator<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        if (!this.hasNext()) {
            return Iterator.empty();
        }
        return new IteratorModule.DistinctIterator(this, TreeSet.empty(comparator), Function.identity());
    }

    @Override
    default public <U> Iterator<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        if (!this.hasNext()) {
            return Iterator.empty();
        }
        return new IteratorModule.DistinctIterator<T, U>(this, HashSet.empty(), keyExtractor);
    }

    @Override
    default public Iterator<T> drop(final int n) {
        if (n <= 0) {
            return this;
        }
        if (!this.hasNext()) {
            return Iterator.empty();
        }
        final Iterator that = this;
        return new AbstractIterator<T>(){
            long count;
            {
                this.count = n;
            }

            @Override
            public boolean hasNext() {
                while (this.count > 0L && that.hasNext()) {
                    that.next();
                    --this.count;
                }
                return that.hasNext();
            }

            @Override
            public T getNext() {
                return that.next();
            }
        };
    }

    @Override
    default public Iterator<T> dropRight(final int n) {
        if (n <= 0) {
            return this;
        }
        if (!this.hasNext()) {
            return Iterator.empty();
        }
        final Iterator that = this;
        return new AbstractIterator<T>(){
            private Queue<T> queue = Queue.empty();

            @Override
            public boolean hasNext() {
                while (this.queue.length() < n && that.hasNext()) {
                    this.queue = this.queue.append(that.next());
                }
                return this.queue.length() == n && that.hasNext();
            }

            @Override
            public T getNext() {
                Tuple2 t = ((Queue)this.queue.append(that.next())).dequeue();
                this.queue = (Queue)t._2;
                return t._1;
            }
        };
    }

    @Override
    default public Iterator<T> dropUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.dropWhile((Predicate)predicate.negate());
    }

    @Override
    default public Iterator<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (!this.hasNext()) {
            return Iterator.empty();
        }
        IteratorModule.CachedIterator that = new IteratorModule.CachedIterator(this);
        while (that.hasNext() && predicate.test(that.touch())) {
            that.next();
        }
        return that;
    }

    @Override
    default public Iterator<T> filter(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (!this.hasNext()) {
            return Iterator.empty();
        }
        final Iterator that = this;
        return new AbstractIterator<T>(){
            Option<T> next = Option.none();

            @Override
            public boolean hasNext() {
                while (this.next.isEmpty() && that.hasNext()) {
                    Object candidate = that.next();
                    if (!predicate.test(candidate)) continue;
                    this.next = Option.some(candidate);
                }
                return this.next.isDefined();
            }

            @Override
            public T getNext() {
                Object result = this.next.get();
                this.next = Option.none();
                return result;
            }
        };
    }

    @Override
    default public Iterator<T> reject(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.filter((Predicate)predicate.negate());
    }

    @Override
    default public Option<T> findLast(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        Object last = null;
        while (this.hasNext()) {
            Object elem = this.next();
            if (!predicate.test(elem)) continue;
            last = elem;
        }
        return Option.of(last);
    }

    @Override
    default public <U> Iterator<U> flatMap(final Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (!this.hasNext()) {
            return Iterator.empty();
        }
        final Iterator that = this;
        return new AbstractIterator<U>(){
            final Iterator<? extends T> inputs;
            java.util.Iterator<? extends U> current;
            {
                this.inputs = that;
                this.current = java.util.Collections.emptyIterator();
            }

            @Override
            public boolean hasNext() {
                boolean currentHasNext;
                while (!(currentHasNext = this.current.hasNext()) && this.inputs.hasNext()) {
                    this.current = ((Iterable)mapper.apply(this.inputs.next())).iterator();
                }
                return currentHasNext;
            }

            @Override
            public U getNext() {
                return this.current.next();
            }
        };
    }

    @Override
    default public <U> U foldRight(U zero, BiFunction<? super T, ? super U, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return Stream.ofAll(this).foldRight(zero, f);
    }

    @Override
    default public T get() {
        return this.head();
    }

    @Override
    default public <C> Map<C, Iterator<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return Collections.groupBy(this, classifier, Iterator::ofAll);
    }

    @Override
    default public Iterator<Seq<T>> grouped(int size) {
        return new IteratorModule.GroupedIterator(this, size, size);
    }

    @Override
    default public boolean hasDefiniteSize() {
        return false;
    }

    @Override
    default public T head() {
        if (!this.hasNext()) {
            throw new NoSuchElementException("head() on empty iterator");
        }
        return (T)this.next();
    }

    @Override
    default public Iterator<T> init() {
        if (!this.hasNext()) {
            throw new UnsupportedOperationException();
        }
        return this.dropRight(1);
    }

    @Override
    default public Option<Iterator<T>> initOption() {
        return this.hasNext() ? Option.some(this.init()) : Option.none();
    }

    @Override
    default public boolean isAsync() {
        return false;
    }

    @Override
    default public boolean isEmpty() {
        return !this.hasNext();
    }

    @Override
    default public boolean isLazy() {
        return true;
    }

    @Override
    default public boolean isTraversableAgain() {
        return false;
    }

    @Override
    default public boolean isSequential() {
        return true;
    }

    @Override
    default public Iterator<T> iterator() {
        return this;
    }

    @Override
    default public T last() {
        return Collections.last(this);
    }

    @Override
    default public int length() {
        return this.foldLeft(0, (n, ignored) -> n + 1);
    }

    @Override
    default public <U> Iterator<U> map(final Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (!this.hasNext()) {
            return Iterator.empty();
        }
        final Iterator that = this;
        return new AbstractIterator<U>(){

            @Override
            public boolean hasNext() {
                return that.hasNext();
            }

            @Override
            public U getNext() {
                return mapper.apply(that.next());
            }
        };
    }

    @Override
    default public Iterator<T> orElse(Iterable<? extends T> other) {
        return this.isEmpty() ? Iterator.ofAll(other) : this;
    }

    @Override
    default public Iterator<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return this.isEmpty() ? Iterator.ofAll(supplier.get()) : this;
    }

    @Override
    default public Tuple2<Iterator<T>, Iterator<T>> partition(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (!this.hasNext()) {
            return Tuple.of(Iterator.empty(), Iterator.empty());
        }
        Tuple2 dup = IteratorModule.duplicate(this);
        return Tuple.of(((Iterator)dup._1).filter(predicate), ((Iterator)dup._2).filter((Predicate)predicate.negate()));
    }

    @Override
    default public Iterator<T> peek(final Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (!this.hasNext()) {
            return Iterator.empty();
        }
        final Iterator that = this;
        return new AbstractIterator<T>(){

            @Override
            public boolean hasNext() {
                return that.hasNext();
            }

            @Override
            public T getNext() {
                Object next = that.next();
                action.accept(next);
                return next;
            }
        };
    }

    @Override
    default public T reduceLeft(BiFunction<? super T, ? super T, ? extends T> op) {
        Objects.requireNonNull(op, "op is null");
        if (this.isEmpty()) {
            throw new NoSuchElementException("reduceLeft on Nil");
        }
        Object xs = this.next();
        while (this.hasNext()) {
            xs = op.apply(xs, this.next());
        }
        return (T)xs;
    }

    @Override
    default public T reduceRight(BiFunction<? super T, ? super T, ? extends T> op) {
        Objects.requireNonNull(op, "op is null");
        if (this.isEmpty()) {
            throw new NoSuchElementException("reduceRight on Nil");
        }
        LinearSeq reversed = Stream.ofAll(this).reverse();
        return (T)reversed.reduceLeft((xs, x) -> op.apply(x, xs));
    }

    @Override
    default public Iterator<T> replace(final T currentElement, final T newElement) {
        if (!this.hasNext()) {
            return Iterator.empty();
        }
        final Iterator that = this;
        return new AbstractIterator<T>(){
            boolean isFirst = true;

            @Override
            public boolean hasNext() {
                return that.hasNext();
            }

            @Override
            public T getNext() {
                Object elem = that.next();
                if (this.isFirst && Objects.equals(currentElement, elem)) {
                    this.isFirst = false;
                    return newElement;
                }
                return elem;
            }
        };
    }

    @Override
    default public Iterator<T> replaceAll(final T currentElement, final T newElement) {
        if (!this.hasNext()) {
            return Iterator.empty();
        }
        final Iterator that = this;
        return new AbstractIterator<T>(){

            @Override
            public boolean hasNext() {
                return that.hasNext();
            }

            @Override
            public T getNext() {
                Object elem = that.next();
                if (Objects.equals(currentElement, elem)) {
                    return newElement;
                }
                return elem;
            }
        };
    }

    @Override
    default public Iterator<T> retainAll(Iterable<? extends T> elements) {
        return Collections.retainAll(this, elements);
    }

    @Override
    default public Traversable<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        return this.scanLeft(zero, operation);
    }

    @Override
    default public <U> Iterator<U> scanLeft(final U zero, final BiFunction<? super U, ? super T, ? extends U> operation) {
        Objects.requireNonNull(operation, "operation is null");
        if (this.isEmpty()) {
            return Iterator.of(zero);
        }
        final Iterator that = this;
        return new AbstractIterator<U>(){
            boolean isFirst = true;
            U acc = zero;

            @Override
            public boolean hasNext() {
                return this.isFirst || that.hasNext();
            }

            @Override
            public U getNext() {
                if (this.isFirst) {
                    this.isFirst = false;
                    return this.acc;
                }
                this.acc = operation.apply(this.acc, that.next());
                return this.acc;
            }
        };
    }

    @Override
    default public <U> Iterator<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        Objects.requireNonNull(operation, "operation is null");
        if (this.isEmpty()) {
            return Iterator.of(zero);
        }
        return (Iterator)Collections.scanRight(this, zero, operation, Function.identity());
    }

    @Override
    default public Iterator<Seq<T>> slideBy(final Function<? super T, ?> classifier) {
        Objects.requireNonNull(classifier, "classifier is null");
        if (!this.hasNext()) {
            return Iterator.empty();
        }
        final IteratorModule.CachedIterator source2 = new IteratorModule.CachedIterator(this);
        return new AbstractIterator<Seq<T>>(){
            private Stream<T> next = null;

            @Override
            public boolean hasNext() {
                if (this.next == null && source2.hasNext()) {
                    Object key = classifier.apply(source2.touch());
                    ArrayList acc = new ArrayList();
                    acc.add(source2.next());
                    while (source2.hasNext() && key.equals(classifier.apply(source2.touch()))) {
                        acc.add(source2.getNext());
                    }
                    this.next = Stream.ofAll(acc);
                }
                return this.next != null;
            }

            @Override
            public Stream<T> getNext() {
                Stream result = this.next;
                this.next = null;
                return result;
            }
        };
    }

    @Override
    default public Iterator<Seq<T>> sliding(int size) {
        return this.sliding(size, 1);
    }

    @Override
    default public Iterator<Seq<T>> sliding(int size, int step) {
        return new IteratorModule.GroupedIterator(this, size, step);
    }

    @Override
    default public Tuple2<Iterator<T>, Iterator<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (!this.hasNext()) {
            return Tuple.of(Iterator.empty(), Iterator.empty());
        }
        Stream that = Stream.ofAll(this);
        return Tuple.of(that.iterator().takeWhile(predicate), that.iterator().dropWhile(predicate));
    }

    @Override
    default public String stringPrefix() {
        return "Iterator";
    }

    @Override
    default public Iterator<T> tail() {
        if (!this.hasNext()) {
            throw new UnsupportedOperationException();
        }
        this.next();
        return this;
    }

    @Override
    default public Option<Iterator<T>> tailOption() {
        if (this.hasNext()) {
            this.next();
            return Option.some(this);
        }
        return Option.none();
    }

    @Override
    default public Iterator<T> take(final int n) {
        if (n <= 0 || !this.hasNext()) {
            return Iterator.empty();
        }
        final Iterator that = this;
        return new AbstractIterator<T>(){
            long count;
            {
                this.count = n;
            }

            @Override
            public boolean hasNext() {
                return this.count > 0L && that.hasNext();
            }

            @Override
            public T getNext() {
                --this.count;
                return that.next();
            }
        };
    }

    @Override
    default public Iterator<T> takeRight(final int n) {
        if (n <= 0) {
            return Iterator.empty();
        }
        final Iterator that = this;
        return new AbstractIterator<T>(){
            private Queue<T> queue = Queue.empty();

            @Override
            public boolean hasNext() {
                while (that.hasNext()) {
                    this.queue = this.queue.enqueue(that.next());
                    if (this.queue.length() <= n) continue;
                    this.queue = (Queue)this.queue.dequeue()._2;
                }
                return this.queue.length() > 0;
            }

            @Override
            public T getNext() {
                Tuple2 t = this.queue.dequeue();
                this.queue = (Queue)t._2;
                return t._1;
            }
        };
    }

    @Override
    default public Iterator<T> takeUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.takeWhile((Predicate)predicate.negate());
    }

    @Override
    default public Iterator<T> takeWhile(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (!this.hasNext()) {
            return Iterator.empty();
        }
        final Iterator that = this;
        return new AbstractIterator<T>(){
            private T next;
            private boolean cached = false;
            private boolean finished = false;

            @Override
            public boolean hasNext() {
                if (this.cached) {
                    return true;
                }
                if (this.finished) {
                    return false;
                }
                if (that.hasNext()) {
                    this.next = that.next();
                    if (predicate.test(this.next)) {
                        this.cached = true;
                        return true;
                    }
                }
                this.finished = true;
                return false;
            }

            @Override
            public T getNext() {
                this.cached = false;
                return this.next;
            }
        };
    }
}

