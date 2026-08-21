/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.MetricWithFixedMetadata;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.Unit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Info
extends MetricWithFixedMetadata {
    private final Set<Labels> labels = new CopyOnWriteArraySet<Labels>();

    private Info(Builder builder) {
        super(builder);
    }

    public void setLabelValues(String ... labelValues) {
        if (labelValues.length != this.labelNames.length) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " " + this.getMetadata().getName() + " was created with " + this.labelNames.length + " label names, but you called setLabelValues() with " + labelValues.length + " label values.");
        }
        Labels newLabels = Labels.of(this.labelNames, labelValues);
        this.labels.add(newLabels);
        this.labels.retainAll(Collections.singletonList(newLabels));
    }

    public void addLabelValues(String ... labelValues) {
        if (labelValues.length != this.labelNames.length) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " " + this.getMetadata().getName() + " was created with " + this.labelNames.length + " label names, but you called addLabelValues() with " + labelValues.length + " label values.");
        }
        this.labels.add(Labels.of(this.labelNames, labelValues));
    }

    public void remove(String ... labelValues) {
        if (labelValues.length != this.labelNames.length) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " " + this.getMetadata().getName() + " was created with " + this.labelNames.length + " label names, but you called remove() with " + labelValues.length + " label values.");
        }
        Labels toBeRemoved = Labels.of(this.labelNames, labelValues);
        this.labels.remove(toBeRemoved);
    }

    @Override
    public InfoSnapshot collect() {
        ArrayList<InfoSnapshot.InfoDataPointSnapshot> data = new ArrayList<InfoSnapshot.InfoDataPointSnapshot>(this.labels.size());
        if (this.labelNames.length == 0) {
            data.add(new InfoSnapshot.InfoDataPointSnapshot(this.constLabels));
        } else {
            for (Labels label : this.labels) {
                data.add(new InfoSnapshot.InfoDataPointSnapshot(label.merge(this.constLabels)));
            }
        }
        return new InfoSnapshot(this.getMetadata(), (Collection<InfoSnapshot.InfoDataPointSnapshot>)data);
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties config) {
        return new Builder(config);
    }

    public static class Builder
    extends MetricWithFixedMetadata.Builder<Builder, Info> {
        private Builder(PrometheusProperties config) {
            super(Collections.emptyList(), config);
        }

        @Override
        public Builder name(String name) {
            return (Builder)super.name(Builder.stripInfoSuffix(name));
        }

        @Override
        public Builder unit(Unit unit) {
            if (unit != null) {
                throw new UnsupportedOperationException("Info metrics cannot have a unit.");
            }
            return this;
        }

        private static String stripInfoSuffix(String name) {
            if (name != null && (name.endsWith("_info") || name.endsWith(".info"))) {
                name = name.substring(0, name.length() - 5);
            }
            return name;
        }

        @Override
        public Info build() {
            return new Info(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}

