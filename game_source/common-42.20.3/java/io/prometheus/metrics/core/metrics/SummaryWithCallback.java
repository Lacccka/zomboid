/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.CallbackMetric;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

public class SummaryWithCallback
extends CallbackMetric {
    private final Consumer<Callback> callback;

    private SummaryWithCallback(Builder builder) {
        super(builder);
        this.callback = builder.callback;
        if (this.callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }
    }

    @Override
    public SummarySnapshot collect() {
        ArrayList<SummarySnapshot.SummaryDataPointSnapshot> dataPoints = new ArrayList<SummarySnapshot.SummaryDataPointSnapshot>();
        this.callback.accept((count, sum, quantiles, labelValues) -> dataPoints.add(new SummarySnapshot.SummaryDataPointSnapshot(count, sum, quantiles, this.makeLabels(labelValues), Exemplars.EMPTY, 0L)));
        return new SummarySnapshot(this.getMetadata(), (Collection<SummarySnapshot.SummaryDataPointSnapshot>)dataPoints);
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties properties) {
        return new Builder(properties);
    }

    public static class Builder
    extends CallbackMetric.Builder<Builder, SummaryWithCallback> {
        private Consumer<Callback> callback;

        public Builder callback(Consumer<Callback> callback) {
            this.callback = callback;
            return this.self();
        }

        private Builder(PrometheusProperties properties) {
            super(Collections.singletonList("quantile"), properties);
        }

        @Override
        public SummaryWithCallback build() {
            return new SummaryWithCallback(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    @FunctionalInterface
    public static interface Callback {
        public void call(long var1, double var3, Quantiles var5, String ... var6);
    }
}

