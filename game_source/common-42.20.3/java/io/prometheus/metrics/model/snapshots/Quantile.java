/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

public class Quantile {
    private final double quantile;
    private final double value;

    public Quantile(double quantile, double value) {
        this.quantile = quantile;
        this.value = value;
        this.validate();
    }

    public double getQuantile() {
        return this.quantile;
    }

    public double getValue() {
        return this.value;
    }

    private void validate() {
        if (this.quantile < 0.0 || this.quantile > 1.0) {
            throw new IllegalArgumentException(this.quantile + ": Illegal quantile. Expecting 0 <= quantile <= 1");
        }
    }
}

