/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.datapoints;

import io.prometheus.metrics.core.datapoints.DataPoint;
import io.prometheus.metrics.core.datapoints.Timer;
import io.prometheus.metrics.core.datapoints.TimerApi;
import io.prometheus.metrics.model.snapshots.Labels;

public interface GaugeDataPoint
extends DataPoint,
TimerApi {
    default public void inc() {
        this.inc(1.0);
    }

    public void inc(double var1);

    default public void incWithExemplar(Labels labels) {
        this.incWithExemplar(1.0, labels);
    }

    public void incWithExemplar(double var1, Labels var3);

    default public void dec() {
        this.inc(-1.0);
    }

    default public void dec(double amount) {
        this.inc(-amount);
    }

    default public void decWithExemplar(Labels labels) {
        this.incWithExemplar(-1.0, labels);
    }

    default public void decWithExemplar(double amount, Labels labels) {
        this.incWithExemplar(-amount, labels);
    }

    public void set(double var1);

    public double get();

    public void setWithExemplar(double var1, Labels var3);

    @Override
    default public Timer startTimer() {
        return new Timer(this::set);
    }
}

