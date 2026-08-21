/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.datapoints;

import io.prometheus.metrics.core.datapoints.DataPoint;
import io.prometheus.metrics.core.datapoints.Timer;
import io.prometheus.metrics.core.datapoints.TimerApi;
import io.prometheus.metrics.model.snapshots.Labels;

public interface DistributionDataPoint
extends DataPoint,
TimerApi {
    public void observe(double var1);

    public void observeWithExemplar(double var1, Labels var3);

    @Override
    default public Timer startTimer() {
        return new Timer(this::observe);
    }
}

