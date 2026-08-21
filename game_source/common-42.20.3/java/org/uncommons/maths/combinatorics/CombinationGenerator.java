/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.combinatorics;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.uncommons.maths.Maths;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class CombinationGenerator<T>
implements Iterable<List<T>> {
    private final T[] elements;
    private final int[] combinationIndices;
    private long remainingCombinations;
    private long totalCombinations;

    public CombinationGenerator(T[] elements, int combinationLength) {
        if (combinationLength > elements.length) {
            throw new IllegalArgumentException("Combination length cannot be greater than set size.");
        }
        this.elements = (Object[])elements.clone();
        this.combinationIndices = new int[combinationLength];
        BigInteger sizeFactorial = Maths.bigFactorial(elements.length);
        BigInteger lengthFactorial = Maths.bigFactorial(combinationLength);
        BigInteger differenceFactorial = Maths.bigFactorial(elements.length - combinationLength);
        BigInteger total = sizeFactorial.divide(differenceFactorial.multiply(lengthFactorial));
        if (total.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            throw new IllegalArgumentException("Total number of combinations must not be more than 2^63.");
        }
        this.totalCombinations = total.longValue();
        this.reset();
    }

    public CombinationGenerator(Collection<T> elements, int combinationLength) {
        this(elements.toArray(new Object[elements.size()]), combinationLength);
    }

    public final void reset() {
        for (int i = 0; i < this.combinationIndices.length; ++i) {
            this.combinationIndices[i] = i;
        }
        this.remainingCombinations = this.totalCombinations;
    }

    public long getRemainingCombinations() {
        return this.remainingCombinations;
    }

    public boolean hasMore() {
        return this.remainingCombinations > 0L;
    }

    public long getTotalCombinations() {
        return this.totalCombinations;
    }

    public T[] nextCombinationAsArray() {
        Object[] combination = (Object[])Array.newInstance(this.elements.getClass().getComponentType(), this.combinationIndices.length);
        return this.nextCombinationAsArray(combination);
    }

    public T[] nextCombinationAsArray(T[] destination) {
        if (destination.length != this.combinationIndices.length) {
            throw new IllegalArgumentException("Destination array must be the same length as combinations.");
        }
        this.generateNextCombinationIndices();
        for (int i = 0; i < this.combinationIndices.length; ++i) {
            destination[i] = this.elements[this.combinationIndices[i]];
        }
        return destination;
    }

    public List<T> nextCombinationAsList() {
        return this.nextCombinationAsList(new ArrayList(this.elements.length));
    }

    public List<T> nextCombinationAsList(List<T> destination) {
        this.generateNextCombinationIndices();
        destination.clear();
        for (int i : this.combinationIndices) {
            destination.add(this.elements[i]);
        }
        return destination;
    }

    private void generateNextCombinationIndices() {
        if (this.remainingCombinations == 0L) {
            throw new IllegalStateException("There are no combinations remaining.  Generator must be reset to continue using.");
        }
        if (this.remainingCombinations < this.totalCombinations) {
            int i = this.combinationIndices.length - 1;
            while (this.combinationIndices[i] == this.elements.length - this.combinationIndices.length + i) {
                --i;
            }
            int n = i;
            this.combinationIndices[n] = this.combinationIndices[n] + 1;
            for (int j = i + 1; j < this.combinationIndices.length; ++j) {
                this.combinationIndices[j] = this.combinationIndices[i] + j - i;
            }
        }
        --this.remainingCombinations;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Iterator<List<T>>(){

            @Override
            public boolean hasNext() {
                return CombinationGenerator.this.hasMore();
            }

            @Override
            public List<T> next() {
                return CombinationGenerator.this.nextCombinationAsList();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Iterator does not support removal.");
            }
        };
    }
}

