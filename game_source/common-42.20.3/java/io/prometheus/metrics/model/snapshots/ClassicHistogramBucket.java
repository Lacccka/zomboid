/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;

public class ClassicHistogramBucket
implements Comparable<ClassicHistogramBucket> {
    private final long count;
    private final double upperBound;

    public ClassicHistogramBucket(double upperBound, long count) {
        this.count = count;
        this.upperBound = upperBound;
        if (Double.isNaN(upperBound)) {
            throw new IllegalArgumentException("Cannot use NaN as an upper bound for a histogram bucket");
        }
        if (count < 0L) {
            throw new IllegalArgumentException(count + ": " + ClassicHistogramBuckets.class.getSimpleName() + " cannot have a negative count");
        }
    }

    public long getCount() {
        return this.count;
    }

    public double getUpperBound() {
        return this.upperBound;
    }

    @Override
    public int compareTo(ClassicHistogramBucket other) {
        int result = Double.compare(this.upperBound, other.upperBound);
        if (result != 0) {
            return result;
        }
        return Long.compare(this.count, other.count);
    }
}

