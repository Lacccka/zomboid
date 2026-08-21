/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.registry.Collector;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Label;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import java.util.ArrayList;
import java.util.List;

public abstract class Metric
implements Collector {
    protected final Labels constLabels;

    protected Metric(Builder<?, ?> builder) {
        this.constLabels = builder.constLabels;
    }

    @Override
    public abstract MetricSnapshot collect();

    protected static abstract class Builder<B extends Builder<B, M>, M extends Metric> {
        protected final List<String> illegalLabelNames;
        protected final PrometheusProperties properties;
        protected Labels constLabels = Labels.EMPTY;

        protected Builder(List<String> illegalLabelNames, PrometheusProperties properties) {
            this.illegalLabelNames = new ArrayList<String>(illegalLabelNames);
            this.properties = properties;
        }

        public B constLabels(Labels constLabels) {
            for (Label label : constLabels) {
                if (!this.illegalLabelNames.contains(label.getName())) continue;
                throw new IllegalArgumentException(label.getName() + ": illegal label name for this metric type");
            }
            this.constLabels = constLabels;
            return this.self();
        }

        public M register() {
            return this.register(PrometheusRegistry.defaultRegistry);
        }

        public M register(PrometheusRegistry registry) {
            M metric = this.build();
            registry.register((Collector)metric);
            return metric;
        }

        public abstract M build();

        protected abstract B self();
    }
}

