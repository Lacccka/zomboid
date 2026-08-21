/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.CallbackMetric;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

public class GaugeWithCallback
extends CallbackMetric {
    private final Consumer<Callback> callback;

    private GaugeWithCallback(Builder builder) {
        super(builder);
        this.callback = builder.callback;
        if (this.callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }
    }

    @Override
    public GaugeSnapshot collect() {
        ArrayList<GaugeSnapshot.GaugeDataPointSnapshot> dataPoints = new ArrayList<GaugeSnapshot.GaugeDataPointSnapshot>();
        this.callback.accept((value, labelValues) -> dataPoints.add(new GaugeSnapshot.GaugeDataPointSnapshot(value, this.makeLabels(labelValues), null, 0L)));
        return new GaugeSnapshot(this.getMetadata(), (Collection<GaugeSnapshot.GaugeDataPointSnapshot>)dataPoints);
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties properties) {
        return new Builder(properties);
    }

    public static class Builder
    extends CallbackMetric.Builder<Builder, GaugeWithCallback> {
        private Consumer<Callback> callback;

        public Builder callback(Consumer<Callback> callback) {
            this.callback = callback;
            return this.self();
        }

        private Builder(PrometheusProperties properties) {
            super(Collections.emptyList(), properties);
        }

        @Override
        public GaugeWithCallback build() {
            return new GaugeWithCallback(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    @FunctionalInterface
    public static interface Callback {
        public void call(double var1, String ... var3);
    }
}

