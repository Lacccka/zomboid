/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Lazy;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.AbstractIterator;
import io.vavr.collection.Array;
import io.vavr.collection.GwtIncompatible;
import io.vavr.collection.Iterator;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

interface IteratorModule {
    public static <T> Tuple2<Iterator<T>, Iterator<T>> duplicate(final Iterator<T> iterator2) {
        final LinkedList gap = new LinkedList();
        final AtomicReference ahead = new AtomicReference();
        class Partner
        implements Iterator<T> {
            Partner() {
            }

            @Override
            public boolean hasNext() {
                return this != ahead.get() && !gap.isEmpty() || iterator2.hasNext();
            }

            @Override
            public T next() {
                if (gap.isEmpty()) {
                    ahead.set(this);
                }
                if (this == ahead.get()) {
                    Object element = iterator2.next();
                    gap.add(element);
                    return element;
                }
                return gap.poll();
            }
        }
        return Tuple.of(new Partner(), new Partner());
    }

    public static final class BigDecimalHelper {
        @GwtIncompatible(value="Math::nextDown is not implemented")
        private static final Lazy<BigDecimal> INFINITY_DISTANCE = Lazy.of(() -> {
            BigDecimal supremum;
            BigDecimal two = BigDecimal.valueOf(2L);
            BigDecimal lowerBound = supremum = BigDecimal.valueOf(Math.nextDown(Double.POSITIVE_INFINITY));
            BigDecimal upperBound = two.pow(1024);
            while (true) {
                BigDecimal magicValue;
                if (Double.isInfinite((magicValue = lowerBound.add(upperBound).divide(two, RoundingMode.HALF_UP)).doubleValue())) {
                    if (BigDecimalHelper.areEqual(magicValue, upperBound)) {
                        return magicValue.subtract(supremum);
                    }
                    upperBound = magicValue;
                    continue;
                }
                lowerBound = magicValue;
            }
        });

        static boolean areEqual(BigDecimal from, BigDecimal toExclusive) {
            return from.compareTo(toExclusive) == 0;
        }

        @GwtIncompatible(value="Math::nextUp is not implemented")
        static BigDecimal asDecimal(double number) {
            if (number == Double.NEGATIVE_INFINITY) {
                BigDecimal result = BigDecimal.valueOf(Math.nextUp(Double.NEGATIVE_INFINITY));
                return result.subtract(INFINITY_DISTANCE.get());
            }
            if (number == Double.POSITIVE_INFINITY) {
                BigDecimal result = BigDecimal.valueOf(Math.nextDown(Double.POSITIVE_INFINITY));
                return result.add(INFINITY_DISTANCE.get());
            }
            return BigDecimal.valueOf(number);
        }
    }

    public static final class CachedIterator<T>
    extends AbstractIterator<T> {
        private final Iterator<T> that;
        private T next;
        private boolean cached = false;

        CachedIterator(Iterator<T> that) {
            this.that = that;
        }

        @Override
        public boolean hasNext() {
            return this.cached || this.that.hasNext();
        }

        @Override
        public T getNext() {
            if (this.cached) {
                T result = this.next;
                this.next = null;
                this.cached = false;
                return result;
            }
            return (T)this.that.next();
        }

        T touch() {
            this.next = this.next();
            this.cached = true;
            return this.next;
        }
    }

    public static final class GroupedIterator<T>
    implements Iterator<Seq<T>> {
        private final Iterator<T> that;
        private final int size;
        private final int step;
        private final int gap;
        private final int preserve;
        private Object[] buffer;

        GroupedIterator(Iterator<T> that, int size, int step) {
            if (size < 1 || step < 1) {
                throw new IllegalArgumentException("size (" + size + ") and step (" + step + ") must both be positive");
            }
            this.that = that;
            this.size = size;
            this.step = step;
            this.gap = Math.max(step - size, 0);
            this.preserve = Math.max(size - step, 0);
            this.buffer = GroupedIterator.take(that, new Object[size], 0, size);
        }

        @Override
        public boolean hasNext() {
            return this.buffer.length > 0;
        }

        @Override
        public Seq<T> next() {
            if (this.buffer.length == 0) {
                throw new NoSuchElementException();
            }
            Object[] result = this.buffer;
            if (this.that.hasNext()) {
                this.buffer = new Object[this.size];
                if (this.preserve > 0) {
                    System.arraycopy(result, this.step, this.buffer, 0, this.preserve);
                }
                if (this.gap > 0) {
                    GroupedIterator.drop(this.that, this.gap);
                    this.buffer = GroupedIterator.take(this.that, this.buffer, this.preserve, this.size);
                } else {
                    this.buffer = GroupedIterator.take(this.that, this.buffer, this.preserve, this.step);
                }
            } else {
                this.buffer = new Object[0];
            }
            return Array.wrap(result);
        }

        private static void drop(Iterator<?> source2, int count) {
            for (int i = 0; i < count && source2.hasNext(); ++i) {
                source2.next();
            }
        }

        private static Object[] take(Iterator<?> source2, Object[] target, int offset, int count) {
            int i;
            for (i = offset; i < count + offset && source2.hasNext(); ++i) {
                target[i] = source2.next();
            }
            if (i < target.length) {
                Object[] result = new Object[i];
                System.arraycopy(target, 0, result, 0, i);
                return result;
            }
            return target;
        }
    }

    public static final class EmptyIterator
    implements Iterator<Object> {
        static final EmptyIterator INSTANCE = new EmptyIterator();

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException(this.stringPrefix() + ".next()");
        }

        @Override
        public String stringPrefix() {
            return "EmptyIterator";
        }

        @Override
        public String toString() {
            return this.stringPrefix() + "()";
        }
    }

    public static final class DistinctIterator<T, U>
    extends AbstractIterator<T> {
        private final Iterator<? extends T> that;
        private Set<U> known;
        private final Function<? super T, ? extends U> keyExtractor;
        private boolean nextDefined = false;
        private T next;

        DistinctIterator(Iterator<? extends T> that, Set<U> set, Function<? super T, ? extends U> keyExtractor) {
            this.that = that;
            this.known = set;
            this.keyExtractor = keyExtractor;
        }

        @Override
        public boolean hasNext() {
            return this.nextDefined || this.searchNext();
        }

        private boolean searchNext() {
            while (this.that.hasNext()) {
                Object elem = this.that.next();
                U key = this.keyExtractor.apply(elem);
                if (this.known.contains(key)) continue;
                this.known = this.known.add(key);
                this.nextDefined = true;
                this.next = elem;
                return true;
            }
            return false;
        }

        @Override
        public T getNext() {
            T result = this.next;
            this.nextDefined = false;
            this.next = null;
            return result;
        }
    }

    public static final class ConcatIterator<T>
    extends AbstractIterator<T> {
        private Iterator<T> curr;
        private Cell<T> tail;
        private Cell<T> last;
        private boolean hasNextCalculated;

        void append(java.util.Iterator<? extends T> that) {
            Iterator<? extends T> it = Iterator.ofAll(that);
            if (this.tail == null) {
                this.last = Cell.of(it);
                this.tail = this.last;
            } else {
                this.last = this.last.append(it);
            }
        }

        @Override
        public Iterator<T> concat(java.util.Iterator<? extends T> that) {
            this.append(that);
            return this;
        }

        @Override
        public boolean hasNext() {
            if (this.hasNextCalculated) {
                return this.curr != null;
            }
            this.hasNextCalculated = true;
            block0: while (true) {
                if (this.curr != null) {
                    if (this.curr.hasNext()) {
                        return true;
                    }
                    this.curr = null;
                }
                if (this.tail == null) {
                    return false;
                }
                this.curr = this.tail.it;
                this.tail = this.tail.next;
                while (true) {
                    if (!(this.curr instanceof ConcatIterator)) continue block0;
                    ConcatIterator it = (ConcatIterator)this.curr;
                    this.curr = it.curr;
                    it.last.next = this.tail;
                    this.tail = it.tail;
                }
                break;
            }
        }

        @Override
        public T getNext() {
            this.hasNextCalculated = false;
            return (T)this.curr.next();
        }

        private static class Cell<T> {
            Iterator<T> it;
            Cell<T> next;

            private Cell() {
            }

            static <T> Cell<T> of(Iterator<T> it) {
                Cell<T> cell = new Cell<T>();
                cell.it = it;
                return cell;
            }

            Cell<T> append(Iterator<T> it) {
                Cell<T> cell = Cell.of(it);
                this.next = cell;
                return cell;
            }
        }
    }
}

