/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.MetricsProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.datapoints.CounterDataPoint;
import io.prometheus.metrics.core.exemplars.ExemplarSampler;
import io.prometheus.metrics.core.exemplars.ExemplarSamplerConfig;
import io.prometheus.metrics.core.metrics.StatefulMetric;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Labels;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

public class Counter
extends StatefulMetric<CounterDataPoint, DataPoint>
implements CounterDataPoint {
    private final boolean exemplarsEnabled;
    private final ExemplarSamplerConfig exemplarSamplerConfig;

    private Counter(Builder builder, PrometheusProperties prometheusProperties) {
        super(builder);
        MetricsProperties[] properties = this.getMetricProperties(builder, prometheusProperties);
        this.exemplarsEnabled = this.getConfigProperty(properties, MetricsProperties::getExemplarsEnabled);
        this.exemplarSamplerConfig = this.exemplarsEnabled ? new ExemplarSamplerConfig(prometheusProperties.getExemplarProperties(), 1) : null;
    }

    @Override
    public void inc(long amount) {
        ((DataPoint)this.getNoLabels()).inc(amount);
    }

    @Override
    public void inc(double amount) {
        ((DataPoint)this.getNoLabels()).inc(amount);
    }

    @Override
    public void incWithExemplar(long amount, Labels labels) {
        ((DataPoint)this.getNoLabels()).incWithExemplar(amount, labels);
    }

    @Override
    public void incWithExemplar(double amount, Labels labels) {
        ((DataPoint)this.getNoLabels()).incWithExemplar(amount, labels);
    }

    @Override
    public double get() {
        return ((DataPoint)this.getNoLabels()).get();
    }

    @Override
    public long getLongValue() {
        return ((DataPoint)this.getNoLabels()).getLongValue();
    }

    @Override
    public CounterSnapshot collect() {
        return (CounterSnapshot)super.collect();
    }

    protected CounterSnapshot collect(List<Labels> labels, List<DataPoint> metricData) {
        ArrayList<CounterSnapshot.CounterDataPointSnapshot> data = new ArrayList<CounterSnapshot.CounterDataPointSnapshot>(labels.size());
        for (int i = 0; i < labels.size(); ++i) {
            data.add(metricData.get(i).collect(labels.get(i)));
        }
        return new CounterSnapshot(this.getMetadata(), (Collection<CounterSnapshot.CounterDataPointSnapshot>)data);
    }

    @Override
    protected boolean isExemplarsEnabled() {
        return this.exemplarsEnabled;
    }

    @Override
    protected DataPoint newDataPoint() {
        if (this.isExemplarsEnabled()) {
            return new DataPoint(new ExemplarSampler(this.exemplarSamplerConfig));
        }
        return new DataPoint(null);
    }

    static String stripTotalSuffix(String name) {
        if (name != null && (name.endsWith("_total") || name.endsWith(".total"))) {
            name = name.substring(0, name.length() - 6);
        }
        return name;
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties config) {
        return new Builder(config);
    }

    public static class Builder
    extends StatefulMetric.Builder<Builder, Counter> {
        private Builder(PrometheusProperties properties) {
            super(Collections.emptyList(), properties);
        }

        @Override
        public Builder name(String name) {
            return (Builder)super.name(Counter.stripTotalSuffix(name));
        }

        @Override
        public Counter build() {
            return new Counter(this, this.properties);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    class DataPoint
    implements CounterDataPoint {
        private final DoubleAdder doubleValue = new DoubleAdder();
        private final LongAdder longValue = new LongAdder();
        private final long createdTimeMillis = System.currentTimeMillis();
        private final ExemplarSampler exemplarSampler;

        private DataPoint(ExemplarSampler exemplarSampler) {
            this.exemplarSampler = exemplarSampler;
        }

        @Override
        public double get() {
            return (double)this.longValue.sum() + this.doubleValue.sum();
        }

        @Override
        public long getLongValue() {
            return this.longValue.sum() + (long)this.doubleValue.sum();
        }

        @Override
        public void inc(long amount) {
            this.validateAndAdd(amount);
            if (Counter.this.isExemplarsEnabled()) {
                this.exemplarSampler.observe(amount);
            }
        }

        @Override
        public void inc(double amount) {
            this.validateAndAdd(amount);
            if (Counter.this.isExemplarsEnabled()) {
                this.exemplarSampler.observe(amount);
            }
        }

        @Override
        public void incWithExemplar(long amount, Labels labels) {
            this.validateAndAdd(amount);
            if (Counter.this.isExemplarsEnabled()) {
                this.exemplarSampler.observeWithExemplar(amount, labels);
            }
        }

        @Override
        public void incWithExemplar(double amount, Labels labels) {
            this.validateAndAdd(amount);
            if (Counter.this.isExemplarsEnabled()) {
                this.exemplarSampler.observeWithExemplar(amount, labels);
            }
        }

        private void validateAndAdd(long amount) {
            if (amount < 0L) {
                throw new IllegalArgumentException("Negative increment " + amount + " is illegal for Counter metrics.");
            }
            this.longValue.add(amount);
        }

        private void validateAndAdd(double amount) {
            if (amount < 0.0) {
                throw new IllegalArgumentException("Negative increment " + amount + " is illegal for Counter metrics.");
            }
            this.doubleValue.add(amount);
        }

        private CounterSnapshot.CounterDataPointSnapshot collect(Labels labels) {
            Exemplar latestExemplar = null;
            if (this.exemplarSampler != null) {
                for (Exemplar exemplar : this.exemplarSampler.collect()) {
                    if (latestExemplar != null && exemplar.getTimestampMillis() <= latestExemplar.getTimestampMillis()) continue;
                    latestExemplar = exemplar;
                }
            }
            return new CounterSnapshot.CounterDataPointSnapshot(this.get(), labels, latestExemplar, this.createdTimeMillis);
        }
    }
}

