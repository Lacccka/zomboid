/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.Metric;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.PrometheusNaming;
import io.prometheus.metrics.model.snapshots.Unit;
import java.util.Arrays;
import java.util.List;

public abstract class MetricWithFixedMetadata
extends Metric {
    private final MetricMetadata metadata;
    protected final String[] labelNames;

    protected MetricWithFixedMetadata(Builder<?, ?> builder) {
        super(builder);
        this.metadata = new MetricMetadata(this.makeName(builder.name, ((Builder)builder).unit), ((Builder)builder).help, ((Builder)builder).unit);
        this.labelNames = Arrays.copyOf(((Builder)builder).labelNames, ((Builder)builder).labelNames.length);
    }

    protected MetricMetadata getMetadata() {
        return this.metadata;
    }

    private String makeName(String name, Unit unit) {
        if (unit != null && !name.endsWith("_" + unit) && !name.endsWith("." + unit)) {
            name = name + "_" + unit;
        }
        return name;
    }

    @Override
    public String getPrometheusName() {
        return this.metadata.getPrometheusName();
    }

    public static abstract class Builder<B extends Builder<B, M>, M extends MetricWithFixedMetadata>
    extends Metric.Builder<B, M> {
        protected String name;
        private Unit unit;
        private String help;
        private String[] labelNames = new String[0];

        protected Builder(List<String> illegalLabelNames, PrometheusProperties properties) {
            super(illegalLabelNames, properties);
        }

        public B name(String name) {
            String error = PrometheusNaming.validateMetricName(name);
            if (error != null) {
                throw new IllegalArgumentException("'" + name + "': Illegal metric name: " + error);
            }
            this.name = name;
            return (B)this.self();
        }

        public B unit(Unit unit) {
            this.unit = unit;
            return (B)this.self();
        }

        public B help(String help) {
            this.help = help;
            return (B)this.self();
        }

        public B labelNames(String ... labelNames) {
            for (String labelName : labelNames) {
                if (!PrometheusNaming.isValidLabelName(labelName)) {
                    throw new IllegalArgumentException(labelName + ": illegal label name");
                }
                if (this.illegalLabelNames.contains(labelName)) {
                    throw new IllegalArgumentException(labelName + ": illegal label name for this metric type");
                }
                if (!this.constLabels.contains(labelName)) continue;
                throw new IllegalArgumentException(labelName + ": duplicate label name");
            }
            this.labelNames = labelNames;
            return (B)this.self();
        }

        @Override
        public B constLabels(Labels constLabels) {
            for (String labelName : this.labelNames) {
                if (!constLabels.contains(labelName)) continue;
                throw new IllegalArgumentException(labelName + ": duplicate label name");
            }
            return (B)((Builder)super.constLabels(constLabels));
        }

        @Override
        public abstract M build();

        @Override
        protected abstract B self();
    }
}

