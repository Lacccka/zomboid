/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Function1;
import io.vavr.Tuple2;
import io.vavr.collection.AbstractIterator;
import io.vavr.collection.BitSet;
import io.vavr.collection.Collections;
import io.vavr.collection.Iterator;
import io.vavr.collection.LinearSeq;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.SortedSet;
import io.vavr.collection.Stream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

interface BitSetModule {
    public static final int ADDRESS_BITS_PER_WORD = 6;
    public static final int BITS_PER_WORD = 64;

    public static class BitSetN<T>
    extends AbstractBitSet<T> {
        private static final long serialVersionUID = 1L;
        private final long[] elements;
        private final int len;

        BitSetN(Function1<Integer, T> fromInt, Function1<T, Integer> toInt, long[] elements) {
            super(fromInt, toInt);
            this.elements = elements;
            this.len = BitSetN.calcLength(elements);
        }

        private static int calcLength(long[] elements) {
            int len = 0;
            for (long element : elements) {
                len += Long.bitCount(element);
            }
            return len;
        }

        @Override
        int getWordsNum() {
            return this.elements.length;
        }

        @Override
        long[] copyExpand(int wordsNum) {
            if (wordsNum < this.elements.length) {
                wordsNum = this.elements.length;
            }
            long[] arr = new long[wordsNum];
            System.arraycopy(this.elements, 0, arr, 0, this.elements.length);
            return arr;
        }

        @Override
        long getWord(int index) {
            return this.elements[index];
        }

        @Override
        public T head() {
            int offset = 0;
            int element = 0;
            for (int i = 0; i < this.getWordsNum(); ++i) {
                if (this.elements[i] == 0L) {
                    offset += 64;
                    continue;
                }
                element = offset + Long.numberOfTrailingZeros(this.elements[i]);
                break;
            }
            return (T)this.fromInt.apply(element);
        }

        @Override
        public int length() {
            return this.len;
        }

        @Override
        public BitSet<T> add(T t) {
            if (this.contains(t)) {
                return this;
            }
            return this.addElement((Integer)this.toInt.apply(t));
        }
    }

    public static class BitSet2<T>
    extends AbstractBitSet<T> {
        private static final long serialVersionUID = 1L;
        private final long elements1;
        private final long elements2;
        private final int len;

        BitSet2(Function1<Integer, T> fromInt, Function1<T, Integer> toInt, long elements1, long elements2) {
            super(fromInt, toInt);
            this.elements1 = elements1;
            this.elements2 = elements2;
            this.len = Long.bitCount(elements1) + Long.bitCount(elements2);
        }

        @Override
        int getWordsNum() {
            return 2;
        }

        @Override
        long[] copyExpand(int wordsNum) {
            if (wordsNum < 2) {
                wordsNum = 2;
            }
            long[] arr = new long[wordsNum];
            arr[0] = this.elements1;
            arr[1] = this.elements2;
            return arr;
        }

        @Override
        long getWord(int index) {
            if (index == 0) {
                return this.elements1;
            }
            return this.elements2;
        }

        @Override
        public T head() {
            if (this.elements1 == 0L) {
                return (T)this.fromInt.apply(64 + Long.numberOfTrailingZeros(this.elements2));
            }
            return (T)this.fromInt.apply(Long.numberOfTrailingZeros(this.elements1));
        }

        @Override
        public int length() {
            return this.len;
        }

        @Override
        public BitSet<T> add(T t) {
            int element = (Integer)this.toInt.apply(t);
            if (element < 0) {
                throw new IllegalArgumentException("bitset element must be >= 0");
            }
            long mask = 1L << element;
            if (element < 64) {
                if ((this.elements1 & mask) != 0L) {
                    return this;
                }
                return new BitSet2<T>(this.fromInt, this.toInt, this.elements1 | mask, this.elements2);
            }
            if (element < 128) {
                if ((this.elements2 & mask) != 0L) {
                    return this;
                }
                return new BitSet2<T>(this.fromInt, this.toInt, this.elements1, this.elements2 | mask);
            }
            return this.addElement(element);
        }
    }

    public static class BitSet1<T>
    extends AbstractBitSet<T> {
        private static final long serialVersionUID = 1L;
        private final long elements;
        private final int len;

        BitSet1(Function1<Integer, T> fromInt, Function1<T, Integer> toInt, long elements) {
            super(fromInt, toInt);
            this.elements = elements;
            this.len = Long.bitCount(elements);
        }

        @Override
        int getWordsNum() {
            return 1;
        }

        @Override
        long[] copyExpand(int wordsNum) {
            if (wordsNum < 1) {
                wordsNum = 1;
            }
            long[] arr = new long[wordsNum];
            arr[0] = this.elements;
            return arr;
        }

        @Override
        long getWord(int index) {
            return this.elements;
        }

        @Override
        public T head() {
            if (this.elements == 0L) {
                throw new NoSuchElementException("head of empty BitSet");
            }
            return (T)this.fromInt.apply(Long.numberOfTrailingZeros(this.elements));
        }

        @Override
        public int length() {
            return this.len;
        }

        @Override
        public BitSet<T> add(T t) {
            int element = (Integer)this.toInt.apply(t);
            if (element < 0) {
                throw new IllegalArgumentException("bitset element must be >= 0");
            }
            if (element < 64) {
                long mask = 1L << element;
                if ((this.elements & mask) != 0L) {
                    return this;
                }
                return new BitSet1<T>(this.fromInt, this.toInt, this.elements | mask);
            }
            return this.addElement(element);
        }
    }

    public static class BitSetIterator<T>
    extends AbstractIterator<T> {
        private final AbstractBitSet<T> bitSet;
        private long element;
        private int index;

        BitSetIterator(AbstractBitSet<T> bitSet) {
            this.bitSet = bitSet;
            this.element = bitSet.getWord(0);
            this.index = 0;
        }

        @Override
        protected T getNext() {
            int pos = Long.numberOfTrailingZeros(this.element);
            this.element &= 1L << pos ^ 0xFFFFFFFFFFFFFFFFL;
            return this.bitSet.fromInt.apply(pos + (this.index << 6));
        }

        @Override
        public boolean hasNext() {
            if (this.element == 0L) {
                while (this.element == 0L && this.index < this.bitSet.getWordsNum() - 1) {
                    this.element = this.bitSet.getWord(++this.index);
                }
                return this.element != 0L;
            }
            return true;
        }
    }

    public static abstract class AbstractBitSet<T>
    implements BitSet<T>,
    Serializable {
        private static final long serialVersionUID = 1L;
        final Function1<Integer, T> fromInt;
        final Function1<T, Integer> toInt;

        AbstractBitSet(Function1<Integer, T> fromInt, Function1<T, Integer> toInt) {
            this.fromInt = fromInt;
            this.toInt = toInt;
        }

        abstract int getWordsNum();

        abstract long[] copyExpand(int var1);

        abstract long getWord(int var1);

        BitSet<T> createEmpty() {
            return new BitSet1<T>(this.fromInt, this.toInt, 0L);
        }

        BitSet<T> createFromAll(Iterable<? extends T> values2) {
            return values2 instanceof BitSet ? (BitSet)values2 : this.createEmpty().addAll((Iterable)values2);
        }

        BitSet<T> fromBitMaskNoCopy(long[] elements) {
            switch (elements.length) {
                case 0: {
                    return this.createEmpty();
                }
                case 1: {
                    return new BitSet1<T>(this.fromInt, this.toInt, elements[0]);
                }
                case 2: {
                    return new BitSet2<T>(this.fromInt, this.toInt, elements[0], elements[1]);
                }
            }
            return new BitSetN<T>(this.fromInt, this.toInt, elements);
        }

        private void setElement(long[] words, int element) {
            int index;
            int n = index = element >> 6;
            words[n] = words[n] | 1L << element;
        }

        private void unsetElement(long[] words, int element) {
            int index;
            int n = index = element >> 6;
            words[n] = words[n] & (1L << element ^ 0xFFFFFFFFFFFFFFFFL);
        }

        long[] shrink(long[] elements) {
            int newlen;
            for (newlen = elements.length; newlen > 0 && elements[newlen - 1] == 0L; --newlen) {
            }
            long[] newelems = new long[newlen];
            System.arraycopy(elements, 0, newelems, 0, newlen);
            return newelems;
        }

        BitSet<T> addElement(int element) {
            long[] copy = this.copyExpand(1 + (element >> 6));
            this.setElement(copy, element);
            return this.fromBitMaskNoCopy(copy);
        }

        @Override
        public BitSet<T> distinctBy(Comparator<? super T> comparator) {
            Objects.requireNonNull(comparator, "comparator is null");
            return this.isEmpty() ? this : this.createFromAll(this.iterator().distinctBy(comparator));
        }

        @Override
        public <U> BitSet<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
            Objects.requireNonNull(keyExtractor, "keyExtractor is null");
            return this.isEmpty() ? this : this.createFromAll(this.iterator().distinctBy(keyExtractor));
        }

        @Override
        public BitSet<T> drop(int n) {
            if (n <= 0 || this.isEmpty()) {
                return this;
            }
            if (n >= this.length()) {
                return this.createEmpty();
            }
            return this.createFromAll(this.iterator().drop(n));
        }

        @Override
        public BitSet<T> dropRight(int n) {
            if (n <= 0 || this.isEmpty()) {
                return this;
            }
            if (n >= this.length()) {
                return this.createEmpty();
            }
            return this.createFromAll(this.iterator().dropRight(n));
        }

        @Override
        public BitSet<T> dropWhile(Predicate<? super T> predicate) {
            Objects.requireNonNull(predicate, "predicate is null");
            BitSet<T> bitSet = this.createFromAll(this.iterator().dropWhile(predicate));
            return bitSet.length() == this.length() ? this : bitSet;
        }

        @Override
        public BitSet<T> intersect(Set<? extends T> elements) {
            Objects.requireNonNull(elements, "elements is null");
            if (this.isEmpty()) {
                return this;
            }
            if (elements.isEmpty()) {
                return this.createEmpty();
            }
            int size = this.size();
            if (size <= elements.size()) {
                return this.retainAll(elements);
            }
            SortedSet results = this.createFromAll(elements).retainAll((Iterable)this);
            return size == results.size() ? this : results;
        }

        @Override
        public BitSet<T> orElse(Iterable<? extends T> other) {
            return this.isEmpty() ? this.createFromAll(other) : this;
        }

        @Override
        public BitSet<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
            return this.isEmpty() ? this.createFromAll(supplier.get()) : this;
        }

        @Override
        public Iterator<BitSet<T>> slideBy(Function<? super T, ?> classifier) {
            return this.iterator().slideBy(classifier).map(this::createFromAll);
        }

        @Override
        public Iterator<BitSet<T>> sliding(int size, int step) {
            return this.iterator().sliding(size, step).map(this::createFromAll);
        }

        @Override
        public Tuple2<BitSet<T>, BitSet<T>> span(Predicate<? super T> predicate) {
            Objects.requireNonNull(predicate, "predicate is null");
            return this.iterator().span(predicate).map(this::createFromAll, this::createFromAll);
        }

        @Override
        public BitSet<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
            return Collections.scanLeft(this, zero, operation, this::createFromAll);
        }

        @Override
        public Tuple2<BitSet<T>, BitSet<T>> partition(Predicate<? super T> predicate) {
            return Collections.partition(this, this::createFromAll, predicate);
        }

        @Override
        public BitSet<T> filter(Predicate<? super T> predicate) {
            Objects.requireNonNull(predicate, "predicate is null");
            BitSet<T> bitSet = this.createFromAll(this.iterator().filter(predicate));
            return bitSet.length() == this.length() ? this : bitSet;
        }

        @Override
        public BitSet<T> reject(Predicate<? super T> predicate) {
            Objects.requireNonNull(predicate, "predicate is null");
            BitSet<T> bitSet = this.createFromAll(this.iterator().reject(predicate));
            return bitSet.length() == this.length() ? this : bitSet;
        }

        @Override
        public <C> Map<C, BitSet<T>> groupBy(Function<? super T, ? extends C> classifier) {
            return Collections.groupBy(this, classifier, this::createFromAll);
        }

        @Override
        public Comparator<T> comparator() {
            return Comparator.comparing(this.toInt);
        }

        @Override
        public BitSet<T> takeWhile(Predicate<? super T> predicate) {
            Objects.requireNonNull(predicate, "predicate is null");
            BitSet<T> result = this.createFromAll(this.iterator().takeWhile(predicate));
            return result.length() == this.length() ? this : result;
        }

        @Override
        public BitSet<T> addAll(Iterable<? extends T> elements) {
            LinearSeq source2 = Stream.ofAll(elements).map(this.toInt);
            if (source2.isEmpty()) {
                return this;
            }
            long[] copy = this.copyExpand(1 + (source2.max().getOrElse(0) >> 6));
            source2.forEach(element -> {
                if (element < 0) {
                    throw new IllegalArgumentException("bitset element must be >= 0");
                }
                this.setElement(copy, (int)element);
            });
            BitSet<T> bitSet = this.fromBitMaskNoCopy(copy);
            return bitSet.length() == this.length() ? this : bitSet;
        }

        @Override
        public boolean contains(T t) {
            int element = this.toInt.apply(t);
            if (element < 0) {
                throw new IllegalArgumentException("bitset element must be >= 0");
            }
            int index = element >> 6;
            return index < this.getWordsNum() && (this.getWord(index) & 1L << element) != 0L;
        }

        @Override
        public BitSet<T> init() {
            if (this.isEmpty()) {
                throw new UnsupportedOperationException("init of empty TreeSet");
            }
            long last = this.getWord(this.getWordsNum() - 1);
            int element = 64 * (this.getWordsNum() - 1) + 64 - Long.numberOfLeadingZeros(last) - 1;
            return this.remove((Object)this.fromInt.apply(element));
        }

        @Override
        public Iterator<T> iterator() {
            return new BitSetIterator(this);
        }

        @Override
        public BitSet<T> take(int n) {
            if (this.isEmpty() || n >= this.length()) {
                return this;
            }
            if (n <= 0) {
                return this.createEmpty();
            }
            return this.createFromAll(this.iterator().take(n));
        }

        @Override
        public BitSet<T> takeRight(int n) {
            if (this.isEmpty() || n >= this.length()) {
                return this;
            }
            if (n <= 0) {
                return this.createEmpty();
            }
            return this.createFromAll(this.iterator().takeRight(n));
        }

        @Override
        public BitSet<T> remove(T t) {
            if (this.contains(t)) {
                int element = this.toInt.apply(t);
                long[] copy = this.copyExpand(this.getWordsNum());
                this.unsetElement(copy, element);
                return this.fromBitMaskNoCopy(this.shrink(copy));
            }
            return this;
        }

        @Override
        public BitSet<T> removeAll(Iterable<? extends T> elements) {
            if (this.isEmpty()) {
                return this;
            }
            LinearSeq source2 = Stream.ofAll(elements).map(this.toInt);
            if (source2.isEmpty()) {
                return this;
            }
            long[] copy = this.copyExpand(this.getWordsNum());
            source2.forEach(element -> this.unsetElement(copy, (int)element));
            return this.fromBitMaskNoCopy(this.shrink(copy));
        }

        @Override
        public String toString() {
            return this.mkString(this.stringPrefix() + "(", ", ", ")");
        }

        @Override
        public boolean equals(Object o) {
            return Collections.equals(this, o);
        }

        @Override
        public int hashCode() {
            return Collections.hashUnordered(this);
        }
    }
}

