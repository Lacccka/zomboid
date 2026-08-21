/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

public class NativeHistogramBucket {
    private final int bucketIndex;
    private final long count;

    public NativeHistogramBucket(int bucketIndex, long count) {
        this.bucketIndex = bucketIndex;
        this.count = count;
    }

    public int getBucketIndex() {
        return this.bucketIndex;
    }

    public long getCount() {
        return this.count;
    }
}

