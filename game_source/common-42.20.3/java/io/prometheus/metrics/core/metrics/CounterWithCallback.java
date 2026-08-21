/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.CallbackMetric;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

public class CounterWithCallback
extends CallbackMetric {
    private final Consumer<Callback> callback;

    private CounterWithCallback(Builder builder) {
        super(builder);
        this.callback = builder.callback;
        if (this.callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }
    }

    @Override
    public CounterSnapshot collect() {
        ArrayList<CounterSnapshot.CounterDataPointSnapshot> dataPoints = new ArrayList<CounterSnapshot.CounterDataPointSnapshot>();
        this.callback.accept((value, labelValues) -> dataPoints.add(new CounterSnapshot.CounterDataPointSnapshot(value, this.makeLabels(labelValues), null, 0L)));
        return new CounterSnapshot(this.getMetadata(), (Collection<CounterSnapshot.CounterDataPointSnapshot>)dataPoints);
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties properties) {
        return new Builder(properties);
    }

    public static class Builder
    extends CallbackMetric.Builder<Builder, CounterWithCallback> {
        private Consumer<Callback> callback;

        public Builder callback(Consumer<Callback> callback) {
            this.callback = callback;
            return this.self();
        }

        private Builder(PrometheusProperties properties) {
            super(Collections.emptyList(), properties);
        }

        @Override
        public Builder name(String name) {
            return (Builder)super.name(Counter.stripTotalSuffix(name));
        }

        @Override
        public CounterWithCallback build() {
            return new CounterWithCallback(this);
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

