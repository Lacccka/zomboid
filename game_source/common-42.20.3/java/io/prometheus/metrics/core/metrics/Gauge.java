/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.MetricsProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.datapoints.GaugeDataPoint;
import io.prometheus.metrics.core.exemplars.ExemplarSampler;
import io.prometheus.metrics.core.exemplars.ExemplarSamplerConfig;
import io.prometheus.metrics.core.metrics.StatefulMetric;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Gauge
extends StatefulMetric<GaugeDataPoint, DataPoint>
implements GaugeDataPoint {
    private final boolean exemplarsEnabled;
    private final ExemplarSamplerConfig exemplarSamplerConfig;

    private Gauge(Builder builder, PrometheusProperties prometheusProperties) {
        super(builder);
        MetricsProperties[] properties = this.getMetricProperties(builder, prometheusProperties);
        this.exemplarsEnabled = this.getConfigProperty(properties, MetricsProperties::getExemplarsEnabled);
        this.exemplarSamplerConfig = this.exemplarsEnabled ? new ExemplarSamplerConfig(prometheusProperties.getExemplarProperties(), 1) : null;
    }

    @Override
    public void inc(double amount) {
        ((DataPoint)this.getNoLabels()).inc(amount);
    }

    @Override
    public double get() {
        return ((DataPoint)this.getNoLabels()).get();
    }

    @Override
    public void incWithExemplar(double amount, Labels labels) {
        ((DataPoint)this.getNoLabels()).incWithExemplar(amount, labels);
    }

    @Override
    public void set(double value) {
        ((DataPoint)this.getNoLabels()).set(value);
    }

    @Override
    public void setWithExemplar(double value, Labels labels) {
        ((DataPoint)this.getNoLabels()).setWithExemplar(value, labels);
    }

    @Override
    public GaugeSnapshot collect() {
        return (GaugeSnapshot)super.collect();
    }

    protected GaugeSnapshot collect(List<Labels> labels, List<DataPoint> metricData) {
        ArrayList<GaugeSnapshot.GaugeDataPointSnapshot> dataPointSnapshots = new ArrayList<GaugeSnapshot.GaugeDataPointSnapshot>(labels.size());
        for (int i = 0; i < labels.size(); ++i) {
            dataPointSnapshots.add(metricData.get(i).collect(labels.get(i)));
        }
        return new GaugeSnapshot(this.getMetadata(), (Collection<GaugeSnapshot.GaugeDataPointSnapshot>)dataPointSnapshots);
    }

    @Override
    protected DataPoint newDataPoint() {
        if (this.isExemplarsEnabled()) {
            return new DataPoint(new ExemplarSampler(this.exemplarSamplerConfig));
        }
        return new DataPoint(null);
    }

    @Override
    protected boolean isExemplarsEnabled() {
        return this.exemplarsEnabled;
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties config) {
        return new Builder(config);
    }

    public static class Builder
    extends StatefulMetric.Builder<Builder, Gauge> {
        private Builder(PrometheusProperties config) {
            super(Collections.emptyList(), config);
        }

        @Override
        public Gauge build() {
            return new Gauge(this, this.properties);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    class DataPoint
    implements GaugeDataPoint {
        private final ExemplarSampler exemplarSampler;
        private final AtomicLong value = new AtomicLong(Double.doubleToRawLongBits(0.0));

        private DataPoint(ExemplarSampler exemplarSampler) {
            this.exemplarSampler = exemplarSampler;
        }

        @Override
        public void inc(double amount) {
            long next = this.value.updateAndGet(l -> Double.doubleToRawLongBits(Double.longBitsToDouble(l) + amount));
            if (Gauge.this.isExemplarsEnabled()) {
                this.exemplarSampler.observe(Double.longBitsToDouble(next));
            }
        }

        @Override
        public void incWithExemplar(double amount, Labels labels) {
            long next = this.value.updateAndGet(l -> Double.doubleToRawLongBits(Double.longBitsToDouble(l) + amount));
            if (Gauge.this.isExemplarsEnabled()) {
                this.exemplarSampler.observeWithExemplar(Double.longBitsToDouble(next), labels);
            }
        }

        @Override
        public void set(double value) {
            this.value.set(Double.doubleToRawLongBits(value));
            if (Gauge.this.isExemplarsEnabled()) {
                this.exemplarSampler.observe(value);
            }
        }

        @Override
        public double get() {
            return Double.longBitsToDouble(this.value.get());
        }

        @Override
        public void setWithExemplar(double value, Labels labels) {
            this.value.set(Double.doubleToRawLongBits(value));
            if (Gauge.this.isExemplarsEnabled()) {
                this.exemplarSampler.observeWithExemplar(value, labels);
            }
        }

        private GaugeSnapshot.GaugeDataPointSnapshot collect(Labels labels) {
            Exemplar oldest = null;
            if (Gauge.this.isExemplarsEnabled()) {
                for (Exemplar exemplar : this.exemplarSampler.collect()) {
                    if (oldest != null && exemplar.getTimestampMillis() >= oldest.getTimestampMillis()) continue;
                    oldest = exemplar;
                }
            }
            return new GaugeSnapshot.GaugeDataPointSnapshot(this.get(), labels, oldest);
        }
    }
}

