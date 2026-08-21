/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.MetricsProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.datapoints.DistributionDataPoint;
import io.prometheus.metrics.core.exemplars.ExemplarSampler;
import io.prometheus.metrics.core.exemplars.ExemplarSamplerConfig;
import io.prometheus.metrics.core.metrics.Buffer;
import io.prometheus.metrics.core.metrics.CKMSQuantiles;
import io.prometheus.metrics.core.metrics.SlidingWindow;
import io.prometheus.metrics.core.metrics.StatefulMetric;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.Quantile;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

public class Summary
extends StatefulMetric<DistributionDataPoint, DataPoint>
implements DistributionDataPoint {
    private final List<CKMSQuantiles.Quantile> quantiles;
    private final long maxAgeSeconds;
    private final int ageBuckets;
    private final boolean exemplarsEnabled;
    private final ExemplarSamplerConfig exemplarSamplerConfig;

    private Summary(Builder builder, PrometheusProperties prometheusProperties) {
        super(builder);
        MetricsProperties[] properties = this.getMetricProperties(builder, prometheusProperties);
        this.exemplarsEnabled = this.getConfigProperty(properties, MetricsProperties::getExemplarsEnabled);
        this.quantiles = Collections.unmodifiableList(this.makeQuantiles(properties));
        this.maxAgeSeconds = this.getConfigProperty(properties, MetricsProperties::getSummaryMaxAgeSeconds);
        this.ageBuckets = this.getConfigProperty(properties, MetricsProperties::getSummaryNumberOfAgeBuckets);
        this.exemplarSamplerConfig = new ExemplarSamplerConfig(prometheusProperties.getExemplarProperties(), 4);
    }

    private List<CKMSQuantiles.Quantile> makeQuantiles(MetricsProperties[] properties) {
        ArrayList<CKMSQuantiles.Quantile> result = new ArrayList<CKMSQuantiles.Quantile>();
        List quantiles = this.getConfigProperty(properties, MetricsProperties::getSummaryQuantiles);
        List quantileErrors = this.getConfigProperty(properties, MetricsProperties::getSummaryQuantileErrors);
        if (quantiles != null) {
            for (int i = 0; i < quantiles.size(); ++i) {
                if (quantileErrors.size() > 0) {
                    result.add(new CKMSQuantiles.Quantile((Double)quantiles.get(i), (Double)quantileErrors.get(i)));
                    continue;
                }
                result.add(new CKMSQuantiles.Quantile((Double)quantiles.get(i), Builder.defaultError((Double)quantiles.get(i))));
            }
        }
        return result;
    }

    @Override
    protected boolean isExemplarsEnabled() {
        return this.exemplarsEnabled;
    }

    @Override
    public void observe(double amount) {
        ((DataPoint)this.getNoLabels()).observe(amount);
    }

    @Override
    public void observeWithExemplar(double amount, Labels labels) {
        ((DataPoint)this.getNoLabels()).observeWithExemplar(amount, labels);
    }

    @Override
    public SummarySnapshot collect() {
        return (SummarySnapshot)super.collect();
    }

    protected SummarySnapshot collect(List<Labels> labels, List<DataPoint> metricData) {
        ArrayList<SummarySnapshot.SummaryDataPointSnapshot> data = new ArrayList<SummarySnapshot.SummaryDataPointSnapshot>(labels.size());
        for (int i = 0; i < labels.size(); ++i) {
            data.add(metricData.get(i).collect(labels.get(i)));
        }
        return new SummarySnapshot(this.getMetadata(), (Collection<SummarySnapshot.SummaryDataPointSnapshot>)data);
    }

    @Override
    protected DataPoint newDataPoint() {
        return new DataPoint();
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties config) {
        return new Builder(config);
    }

    public static class Builder
    extends StatefulMetric.Builder<Builder, Summary> {
        public static final long DEFAULT_MAX_AGE_SECONDS = TimeUnit.MINUTES.toSeconds(5L);
        public static final int DEFAULT_NUMBER_OF_AGE_BUCKETS = 5;
        private final List<CKMSQuantiles.Quantile> quantiles = new ArrayList<CKMSQuantiles.Quantile>();
        private Long maxAgeSeconds;
        private Integer ageBuckets;

        private Builder(PrometheusProperties properties) {
            super(Collections.singletonList("quantile"), properties);
        }

        private static double defaultError(double quantile) {
            if (quantile <= 0.01 || quantile >= 0.99) {
                return 0.001;
            }
            if (quantile <= 0.02 || quantile >= 0.98) {
                return 0.005;
            }
            return 0.01;
        }

        public Builder quantile(double quantile) {
            return this.quantile(quantile, Builder.defaultError(quantile));
        }

        public Builder quantile(double quantile, double error) {
            if (quantile < 0.0 || quantile > 1.0) {
                throw new IllegalArgumentException("Quantile " + quantile + " invalid: Expected number between 0.0 and 1.0.");
            }
            if (error < 0.0 || error > 1.0) {
                throw new IllegalArgumentException("Error " + error + " invalid: Expected number between 0.0 and 1.0.");
            }
            this.quantiles.add(new CKMSQuantiles.Quantile(quantile, error));
            return this;
        }

        public Builder maxAgeSeconds(long maxAgeSeconds) {
            if (maxAgeSeconds <= 0L) {
                throw new IllegalArgumentException("maxAgeSeconds cannot be " + maxAgeSeconds);
            }
            this.maxAgeSeconds = maxAgeSeconds;
            return this;
        }

        public Builder numberOfAgeBuckets(int ageBuckets) {
            if (ageBuckets <= 0) {
                throw new IllegalArgumentException("ageBuckets cannot be " + ageBuckets);
            }
            this.ageBuckets = ageBuckets;
            return this;
        }

        @Override
        protected MetricsProperties toProperties() {
            double[] quantiles = null;
            double[] quantileErrors = null;
            if (!this.quantiles.isEmpty()) {
                quantiles = new double[this.quantiles.size()];
                quantileErrors = new double[this.quantiles.size()];
                for (int i = 0; i < this.quantiles.size(); ++i) {
                    quantiles[i] = this.quantiles.get((int)i).quantile;
                    quantileErrors[i] = this.quantiles.get((int)i).epsilon;
                }
            }
            return MetricsProperties.builder().exemplarsEnabled(this.exemplarsEnabled).summaryQuantiles(quantiles).summaryQuantileErrors(quantileErrors).summaryNumberOfAgeBuckets(this.ageBuckets).summaryMaxAgeSeconds(this.maxAgeSeconds).build();
        }

        @Override
        public MetricsProperties getDefaultProperties() {
            return MetricsProperties.builder().exemplarsEnabled(true).summaryQuantiles(new double[0]).summaryNumberOfAgeBuckets(5).summaryMaxAgeSeconds(DEFAULT_MAX_AGE_SECONDS).build();
        }

        @Override
        public Summary build() {
            return new Summary(this, this.properties);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    public class DataPoint
    implements DistributionDataPoint {
        private final LongAdder count = new LongAdder();
        private final DoubleAdder sum = new DoubleAdder();
        private final SlidingWindow<CKMSQuantiles> quantileValues;
        private final Buffer buffer = new Buffer();
        private final ExemplarSampler exemplarSampler;
        private final long createdTimeMillis = System.currentTimeMillis();

        private DataPoint() {
            if (Summary.this.quantiles.size() > 0) {
                CKMSQuantiles.Quantile[] quantilesArray = Summary.this.quantiles.toArray(new CKMSQuantiles.Quantile[0]);
                this.quantileValues = new SlidingWindow<CKMSQuantiles>(CKMSQuantiles.class, () -> new CKMSQuantiles(quantilesArray), CKMSQuantiles::insert, Summary.this.maxAgeSeconds, Summary.this.ageBuckets);
            } else {
                this.quantileValues = null;
            }
            this.exemplarSampler = Summary.this.exemplarsEnabled ? new ExemplarSampler(Summary.this.exemplarSamplerConfig) : null;
        }

        @Override
        public void observe(double value) {
            if (Double.isNaN(value)) {
                return;
            }
            if (!this.buffer.append(value)) {
                this.doObserve(value);
            }
            if (Summary.this.isExemplarsEnabled()) {
                this.exemplarSampler.observe(value);
            }
        }

        @Override
        public void observeWithExemplar(double value, Labels labels) {
            if (Double.isNaN(value)) {
                return;
            }
            if (!this.buffer.append(value)) {
                this.doObserve(value);
            }
            if (Summary.this.isExemplarsEnabled()) {
                this.exemplarSampler.observeWithExemplar(value, labels);
            }
        }

        private void doObserve(double amount) {
            this.sum.add(amount);
            if (this.quantileValues != null) {
                this.quantileValues.observe(amount);
            }
            this.count.increment();
        }

        private SummarySnapshot.SummaryDataPointSnapshot collect(Labels labels) {
            return this.buffer.run(expectedCount -> this.count.sum() == expectedCount.longValue(), () -> new SummarySnapshot.SummaryDataPointSnapshot(this.count.sum(), this.sum.sum(), this.makeQuantiles(), labels, Exemplars.EMPTY, this.createdTimeMillis), this::doObserve);
        }

        private List<CKMSQuantiles.Quantile> getQuantiles() {
            return Summary.this.quantiles;
        }

        private Quantiles makeQuantiles() {
            Quantile[] quantiles = new Quantile[this.getQuantiles().size()];
            for (int i = 0; i < this.getQuantiles().size(); ++i) {
                CKMSQuantiles.Quantile quantile = this.getQuantiles().get(i);
                quantiles[i] = new Quantile(quantile.quantile, this.quantileValues.current().get(quantile.quantile));
            }
            return Quantiles.of(quantiles);
        }
    }
}

