/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.datapoints;

import io.prometheus.metrics.core.datapoints.DataPoint;

public interface StateSetDataPoint
extends DataPoint {
    public void setTrue(String var1);

    default public void setTrue(Enum<?> state) {
        this.setTrue(state.toString());
    }

    public void setFalse(String var1);

    default public void setFalse(Enum<?> state) {
        this.setFalse(state.toString());
    }
}

