/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.combinatorics;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.uncommons.maths.Maths;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class PermutationGenerator<T>
implements Iterable<List<T>> {
    private final T[] elements;
    private final int[] permutationIndices;
    private long remainingPermutations;
    private long totalPermutations;

    public PermutationGenerator(T[] elements) {
        if (elements.length > 20) {
            throw new IllegalArgumentException("Size must be less than or equal to 20.");
        }
        this.elements = (Object[])elements.clone();
        this.permutationIndices = new int[elements.length];
        this.totalPermutations = Maths.factorial(elements.length);
        this.reset();
    }

    public PermutationGenerator(Collection<T> elements) {
        this(elements.toArray(new Object[elements.size()]));
    }

    public final void reset() {
        for (int i = 0; i < this.permutationIndices.length; ++i) {
            this.permutationIndices[i] = i;
        }
        this.remainingPermutations = this.totalPermutations;
    }

    public long getRemainingPermutations() {
        return this.remainingPermutations;
    }

    public long getTotalPermutations() {
        return this.totalPermutations;
    }

    public boolean hasMore() {
        return this.remainingPermutations > 0L;
    }

    public T[] nextPermutationAsArray() {
        Object[] permutation = (Object[])Array.newInstance(this.elements.getClass().getComponentType(), this.permutationIndices.length);
        return this.nextPermutationAsArray(permutation);
    }

    public T[] nextPermutationAsArray(T[] destination) {
        if (destination.length != this.elements.length) {
            throw new IllegalArgumentException("Destination array must be the same length as permutations.");
        }
        this.generateNextPermutationIndices();
        for (int i = 0; i < this.permutationIndices.length; ++i) {
            destination[i] = this.elements[this.permutationIndices[i]];
        }
        return destination;
    }

    public List<T> nextPermutationAsList() {
        ArrayList permutation = new ArrayList(this.elements.length);
        return this.nextPermutationAsList(permutation);
    }

    public List<T> nextPermutationAsList(List<T> destination) {
        this.generateNextPermutationIndices();
        destination.clear();
        for (int i : this.permutationIndices) {
            destination.add(this.elements[i]);
        }
        return destination;
    }

    private void generateNextPermutationIndices() {
        if (this.remainingPermutations == 0L) {
            throw new IllegalStateException("There are no permutations remaining.  Generator must be reset to continue using.");
        }
        if (this.remainingPermutations < this.totalPermutations) {
            int j = this.permutationIndices.length - 2;
            while (this.permutationIndices[j] > this.permutationIndices[j + 1]) {
                --j;
            }
            int k = this.permutationIndices.length - 1;
            while (this.permutationIndices[j] > this.permutationIndices[k]) {
                --k;
            }
            int temp = this.permutationIndices[k];
            this.permutationIndices[k] = this.permutationIndices[j];
            this.permutationIndices[j] = temp;
            int r = this.permutationIndices.length - 1;
            for (int s = j + 1; r > s; --r, ++s) {
                temp = this.permutationIndices[s];
                this.permutationIndices[s] = this.permutationIndices[r];
                this.permutationIndices[r] = temp;
            }
        }
        --this.remainingPermutations;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Iterator<List<T>>(){

            @Override
            public boolean hasNext() {
                return PermutationGenerator.this.hasMore();
            }

            @Override
            public List<T> next() {
                return PermutationGenerator.this.nextPermutationAsList();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Iterator does not support removal.");
            }
        };
    }
}

