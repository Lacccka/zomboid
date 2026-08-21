/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.Labels;

public abstract class DistributionDataPointSnapshot
extends DataPointSnapshot {
    private final long count;
    private final double sum;
    private final Exemplars exemplars;

    protected DistributionDataPointSnapshot(long count, double sum, Exemplars exemplars, Labels labels, long createdTimestampMillis, long scrapeTimestampMillis) {
        super(labels, createdTimestampMillis, scrapeTimestampMillis);
        this.count = count;
        this.sum = sum;
        this.exemplars = exemplars == null ? Exemplars.EMPTY : exemplars;
        this.validate();
    }

    private void validate() {
    }

    public boolean hasCount() {
        return this.count >= 0L;
    }

    public boolean hasSum() {
        return !Double.isNaN(this.sum);
    }

    public long getCount() {
        return this.count;
    }

    public double getSum() {
        return this.sum;
    }

    public Exemplars getExemplars() {
        return this.exemplars;
    }

    static abstract class Builder<T extends Builder<T>>
    extends DataPointSnapshot.Builder<T> {
        protected long count = -1L;
        protected double sum = Double.NaN;
        protected long createdTimestampMillis = 0L;
        protected Exemplars exemplars = Exemplars.EMPTY;

        Builder() {
        }

        protected T count(long count) {
            this.count = count;
            return (T)((Builder)this.self());
        }

        public T sum(double sum) {
            this.sum = sum;
            return (T)((Builder)this.self());
        }

        public T exemplars(Exemplars exemplars) {
            this.exemplars = exemplars;
            return (T)((Builder)this.self());
        }

        public T createdTimestampMillis(long createdTimestampMillis) {
            this.createdTimestampMillis = createdTimestampMillis;
            return (T)((Builder)this.self());
        }
    }
}

