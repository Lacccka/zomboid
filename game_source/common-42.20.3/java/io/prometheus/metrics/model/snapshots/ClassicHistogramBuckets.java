/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.ClassicHistogramBucket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class ClassicHistogramBuckets
implements Iterable<ClassicHistogramBucket> {
    public static final ClassicHistogramBuckets EMPTY = new ClassicHistogramBuckets(new double[0], new long[0]);
    private final double[] upperBounds;
    private final long[] counts;

    private ClassicHistogramBuckets(double[] upperBounds, long[] counts) {
        this.upperBounds = upperBounds;
        this.counts = counts;
    }

    public static ClassicHistogramBuckets of(List<Double> upperBounds, List<? extends Number> counts) {
        double[] upperBoundsCopy = new double[upperBounds.size()];
        for (int i = 0; i < upperBounds.size(); ++i) {
            upperBoundsCopy[i] = upperBounds.get(i);
        }
        long[] countsCopy = new long[counts.size()];
        for (int i = 0; i < counts.size(); ++i) {
            countsCopy[i] = counts.get(i).longValue();
        }
        ClassicHistogramBuckets.sortAndValidate(upperBoundsCopy, countsCopy);
        return new ClassicHistogramBuckets(upperBoundsCopy, countsCopy);
    }

    public static ClassicHistogramBuckets of(double[] upperBounds, Number[] counts) {
        double[] upperBoundsCopy = Arrays.copyOf(upperBounds, upperBounds.length);
        long[] countsCopy = new long[counts.length];
        for (int i = 0; i < counts.length; ++i) {
            countsCopy[i] = counts[i].longValue();
        }
        ClassicHistogramBuckets.sortAndValidate(upperBoundsCopy, countsCopy);
        return new ClassicHistogramBuckets(upperBoundsCopy, countsCopy);
    }

    public static ClassicHistogramBuckets of(double[] upperBounds, long[] counts) {
        double[] upperBoundsCopy = Arrays.copyOf(upperBounds, upperBounds.length);
        long[] countsCopy = Arrays.copyOf(counts, counts.length);
        ClassicHistogramBuckets.sortAndValidate(upperBoundsCopy, countsCopy);
        return new ClassicHistogramBuckets(upperBoundsCopy, countsCopy);
    }

    private static void sortAndValidate(double[] upperBounds, long[] counts) {
        if (upperBounds.length != counts.length) {
            throw new IllegalArgumentException("upperBounds.length == " + upperBounds.length + " but counts.length == " + counts.length + ". Expected the same length.");
        }
        ClassicHistogramBuckets.sort(upperBounds, counts);
        ClassicHistogramBuckets.validate(upperBounds, counts);
    }

    private static void sort(double[] upperBounds, long[] counts) {
        int n = upperBounds.length;
        for (int i = 0; i < n - 1; ++i) {
            for (int j = 0; j < n - i - 1; ++j) {
                if (!(upperBounds[j] > upperBounds[j + 1])) continue;
                ClassicHistogramBuckets.swap(j, j + 1, upperBounds, counts);
            }
        }
    }

    private static void swap(int i, int j, double[] upperBounds, long[] counts) {
        double tmpDouble = upperBounds[j];
        upperBounds[j] = upperBounds[i];
        upperBounds[i] = tmpDouble;
        long tmpLong = counts[j];
        counts[j] = counts[i];
        counts[i] = tmpLong;
    }

    private static void validate(double[] upperBounds, long[] counts) {
        if (upperBounds.length == 0) {
            throw new IllegalArgumentException(ClassicHistogramBuckets.class.getSimpleName() + " cannot be empty. They must contain at least the +Inf bucket.");
        }
        if (upperBounds[upperBounds.length - 1] != Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException(ClassicHistogramBuckets.class.getSimpleName() + " must contain the +Inf bucket.");
        }
        for (int i = 0; i < upperBounds.length; ++i) {
            if (Double.isNaN(upperBounds[i])) {
                throw new IllegalArgumentException("Cannot use NaN as an upper bound in " + ClassicHistogramBuckets.class.getSimpleName());
            }
            if (counts[i] < 0L) {
                throw new IllegalArgumentException("Counts in " + ClassicHistogramBuckets.class.getSimpleName() + " cannot be negative.");
            }
            if (i <= 0 || upperBounds[i - 1] != upperBounds[i]) continue;
            throw new IllegalArgumentException("Duplicate upper bound " + upperBounds[i]);
        }
    }

    public int size() {
        return this.upperBounds.length;
    }

    public double getUpperBound(int i) {
        return this.upperBounds[i];
    }

    public long getCount(int i) {
        return this.counts[i];
    }

    public boolean isEmpty() {
        return this.upperBounds.length == 0;
    }

    private List<ClassicHistogramBucket> asList() {
        ArrayList<ClassicHistogramBucket> result = new ArrayList<ClassicHistogramBucket>(this.size());
        for (int i = 0; i < this.upperBounds.length; ++i) {
            result.add(new ClassicHistogramBucket(this.upperBounds[i], this.counts[i]));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Iterator<ClassicHistogramBucket> iterator() {
        return this.asList().iterator();
    }

    public Stream<ClassicHistogramBucket> stream() {
        return this.asList().stream();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<Double> upperBounds = new ArrayList<Double>();
        private final List<Long> counts = new ArrayList<Long>();

        private Builder() {
        }

        public Builder bucket(double upperBound, long count) {
            this.upperBounds.add(upperBound);
            this.counts.add(count);
            return this;
        }

        public ClassicHistogramBuckets build() {
            return ClassicHistogramBuckets.of(this.upperBounds, this.counts);
        }
    }
}

