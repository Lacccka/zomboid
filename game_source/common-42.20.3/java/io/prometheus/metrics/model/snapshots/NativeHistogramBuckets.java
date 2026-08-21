/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.NativeHistogramBucket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class NativeHistogramBuckets
implements Iterable<NativeHistogramBucket> {
    public static final NativeHistogramBuckets EMPTY = new NativeHistogramBuckets(new int[0], new long[0]);
    private final int[] bucketIndexes;
    private final long[] counts;

    private NativeHistogramBuckets(int[] bucketIndexes, long[] counts) {
        this.bucketIndexes = bucketIndexes;
        this.counts = counts;
    }

    public static NativeHistogramBuckets of(int[] bucketIndexes, long[] counts) {
        int[] bucketIndexesCopy = Arrays.copyOf(bucketIndexes, bucketIndexes.length);
        long[] countsCopy = Arrays.copyOf(counts, counts.length);
        NativeHistogramBuckets.sortAndValidate(bucketIndexesCopy, countsCopy);
        return new NativeHistogramBuckets(bucketIndexesCopy, countsCopy);
    }

    public static NativeHistogramBuckets of(List<Integer> bucketIndexes, List<Long> counts) {
        int[] bucketIndexesCopy = new int[bucketIndexes.size()];
        for (int i = 0; i < bucketIndexes.size(); ++i) {
            bucketIndexesCopy[i] = bucketIndexes.get(i);
        }
        long[] countsCopy = new long[counts.size()];
        for (int i = 0; i < counts.size(); ++i) {
            countsCopy[i] = counts.get(i);
        }
        NativeHistogramBuckets.sortAndValidate(bucketIndexesCopy, countsCopy);
        return new NativeHistogramBuckets(bucketIndexesCopy, countsCopy);
    }

    public int size() {
        return this.bucketIndexes.length;
    }

    private List<NativeHistogramBucket> asList() {
        ArrayList<NativeHistogramBucket> result = new ArrayList<NativeHistogramBucket>(this.size());
        for (int i = 0; i < this.bucketIndexes.length; ++i) {
            result.add(new NativeHistogramBucket(this.bucketIndexes[i], this.counts[i]));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Iterator<NativeHistogramBucket> iterator() {
        return this.asList().iterator();
    }

    public Stream<NativeHistogramBucket> stream() {
        return this.asList().stream();
    }

    public int getBucketIndex(int i) {
        return this.bucketIndexes[i];
    }

    public long getCount(int i) {
        return this.counts[i];
    }

    private static void sortAndValidate(int[] bucketIndexes, long[] counts) {
        if (bucketIndexes.length != counts.length) {
            throw new IllegalArgumentException("bucketIndexes.length == " + bucketIndexes.length + " but counts.length == " + counts.length + ". Expected the same length.");
        }
        NativeHistogramBuckets.sort(bucketIndexes, counts);
        NativeHistogramBuckets.validate(bucketIndexes, counts);
    }

    private static void sort(int[] bucketIndexes, long[] counts) {
        int n = bucketIndexes.length;
        for (int i = 0; i < n - 1; ++i) {
            for (int j = 0; j < n - i - 1; ++j) {
                if (bucketIndexes[j] <= bucketIndexes[j + 1]) continue;
                NativeHistogramBuckets.swap(j, j + 1, bucketIndexes, counts);
            }
        }
    }

    private static void swap(int i, int j, int[] bucketIndexes, long[] counts) {
        int tmpInt = bucketIndexes[j];
        bucketIndexes[j] = bucketIndexes[i];
        bucketIndexes[i] = tmpInt;
        long tmpLong = counts[j];
        counts[j] = counts[i];
        counts[i] = tmpLong;
    }

    private static void validate(int[] bucketIndexes, long[] counts) {
        for (int i = 0; i < bucketIndexes.length; ++i) {
            if (counts[i] < 0L) {
                throw new IllegalArgumentException("Bucket counts cannot be negative.");
            }
            if (i <= 0 || bucketIndexes[i - 1] != bucketIndexes[i]) continue;
            throw new IllegalArgumentException("Duplicate bucket index " + bucketIndexes[i]);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<Integer> bucketIndexes = new ArrayList<Integer>();
        private final List<Long> counts = new ArrayList<Long>();

        private Builder() {
        }

        public Builder bucket(int bucketIndex, long count) {
            this.bucketIndexes.add(bucketIndex);
            this.counts.add(count);
            return this;
        }

        public NativeHistogramBuckets build() {
            return NativeHistogramBuckets.of(this.bucketIndexes, this.counts);
        }
    }
}

