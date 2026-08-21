/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.datapoints;

import io.prometheus.metrics.core.datapoints.DataPoint;
import io.prometheus.metrics.model.snapshots.Labels;

public interface CounterDataPoint
extends DataPoint {
    default public void inc() {
        this.inc(1L);
    }

    default public void inc(long amount) {
        this.inc((double)amount);
    }

    public void inc(double var1);

    default public void incWithExemplar(Labels labels) {
        this.incWithExemplar(1.0, labels);
    }

    default public void incWithExemplar(long amount, Labels labels) {
        this.inc((double)amount);
    }

    public void incWithExemplar(double var1, Labels var3);

    public double get();

    public long getLongValue();
}

