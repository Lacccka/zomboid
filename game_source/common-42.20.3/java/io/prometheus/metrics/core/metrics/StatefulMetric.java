/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.MetricsProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.datapoints.DataPoint;
import io.prometheus.metrics.core.metrics.MetricWithFixedMetadata;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

abstract class StatefulMetric<D extends DataPoint, T extends D>
extends MetricWithFixedMetadata {
    private final ConcurrentHashMap<List<String>, T> data = new ConcurrentHashMap();
    private volatile T noLabels;

    protected StatefulMetric(Builder<?, ?> builder) {
        super((MetricWithFixedMetadata.Builder<?, ?>)builder);
    }

    protected abstract MetricSnapshot collect(List<Labels> var1, List<T> var2);

    @Override
    public MetricSnapshot collect() {
        if (this.labelNames.length == 0 && this.data.isEmpty()) {
            this.labelValues(new String[0]);
        }
        ArrayList<Labels> labels = new ArrayList<Labels>(this.data.size());
        ArrayList<DataPoint> metricData = new ArrayList<DataPoint>(this.data.size());
        for (Map.Entry<List<String>, T> entry : this.data.entrySet()) {
            String[] labelValues = entry.getKey().toArray(new String[this.labelNames.length]);
            labels.add(this.constLabels.merge(this.labelNames, labelValues));
            metricData.add((DataPoint)entry.getValue());
        }
        return this.collect(labels, metricData);
    }

    public void initLabelValues(String ... labelValues) {
        this.labelValues(labelValues);
    }

    public D labelValues(String ... labelValues) {
        if (labelValues.length != this.labelNames.length) {
            if (labelValues.length == 0) {
                throw new IllegalArgumentException(this.getClass().getSimpleName() + " " + this.getMetadata().getName() + " was created with label names, so you must call labelValues(...) when using it.");
            }
            throw new IllegalArgumentException("Expected " + this.labelNames.length + " label values, but got " + labelValues.length + ".");
        }
        return (D)this.data.computeIfAbsent(Arrays.asList(labelValues), l -> {
            for (int i = 0; i < l.size(); ++i) {
                if (l.get(i) != null) continue;
                throw new IllegalArgumentException("null label value for metric " + this.getMetadata().getName() + " and label " + this.labelNames[i]);
            }
            return this.newDataPoint();
        });
    }

    public void remove(String ... labelValues) {
        this.data.remove(Arrays.asList(labelValues));
    }

    public void removeIf(Function<List<String>, Boolean> f) {
        this.data.entrySet().removeIf((? super E entry) -> (Boolean)f.apply(Collections.unmodifiableList((List)entry.getKey())));
    }

    public void clear() {
        this.data.clear();
        this.noLabels = null;
    }

    protected abstract T newDataPoint();

    protected T getNoLabels() {
        if (this.noLabels == null) {
            this.noLabels = this.labelValues(new String[0]);
        }
        return this.noLabels;
    }

    protected MetricsProperties[] getMetricProperties(Builder<?, ?> builder, PrometheusProperties prometheusProperties) {
        String metricName = this.getMetadata().getName();
        if (prometheusProperties.getMetricProperties(metricName) != null) {
            return new MetricsProperties[]{prometheusProperties.getMetricProperties(metricName), builder.toProperties(), prometheusProperties.getDefaultMetricProperties(), builder.getDefaultProperties()};
        }
        return new MetricsProperties[]{builder.toProperties(), prometheusProperties.getDefaultMetricProperties(), builder.getDefaultProperties()};
    }

    protected <P> P getConfigProperty(MetricsProperties[] properties, Function<MetricsProperties, P> getter) {
        for (MetricsProperties props : properties) {
            P result = getter.apply(props);
            if (result == null) continue;
            return result;
        }
        throw new IllegalStateException("Missing default config. This is a bug in the Prometheus metrics core library.");
    }

    protected abstract boolean isExemplarsEnabled();

    static abstract class Builder<B extends Builder<B, M>, M extends StatefulMetric<?, ?>>
    extends MetricWithFixedMetadata.Builder<B, M> {
        protected Boolean exemplarsEnabled;

        protected Builder(List<String> illegalLabelNames, PrometheusProperties config) {
            super(illegalLabelNames, config);
        }

        public B withExemplars() {
            this.exemplarsEnabled = true;
            return (B)((Builder)this.self());
        }

        public B withoutExemplars() {
            this.exemplarsEnabled = false;
            return (B)((Builder)this.self());
        }

        protected MetricsProperties toProperties() {
            return MetricsProperties.builder().exemplarsEnabled(this.exemplarsEnabled).build();
        }

        public MetricsProperties getDefaultProperties() {
            return MetricsProperties.builder().exemplarsEnabled(true).build();
        }
    }
}

