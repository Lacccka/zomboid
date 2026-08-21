/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.MetricWithFixedMetadata;
import io.prometheus.metrics.model.snapshots.Labels;
import java.util.List;

abstract class CallbackMetric
extends MetricWithFixedMetadata {
    protected CallbackMetric(Builder<?, ?> builder) {
        super((MetricWithFixedMetadata.Builder<?, ?>)builder);
    }

    protected Labels makeLabels(String ... labelValues) {
        if (this.labelNames.length == 0) {
            if (labelValues != null && labelValues.length > 0) {
                throw new IllegalArgumentException("Cannot pass label values to a " + this.getClass().getSimpleName() + " that was created without label names.");
            }
            return this.constLabels;
        }
        if (labelValues == null) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " was created with label names, but the callback was called without label values.");
        }
        if (labelValues.length != this.labelNames.length) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " was created with " + this.labelNames.length + " label names, but the callback was called with " + labelValues.length + " label values.");
        }
        return this.constLabels.merge(Labels.of(this.labelNames, labelValues));
    }

    static abstract class Builder<B extends Builder<B, M>, M extends CallbackMetric>
    extends MetricWithFixedMetadata.Builder<B, M> {
        protected Builder(List<String> illegalLabelNames, PrometheusProperties properties) {
            super(illegalLabelNames, properties);
        }
    }
}

