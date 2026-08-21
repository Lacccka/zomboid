/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.config.ExemplarsProperties;
import io.prometheus.metrics.config.MetricsProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.datapoints.DistributionDataPoint;
import io.prometheus.metrics.core.exemplars.ExemplarSampler;
import io.prometheus.metrics.core.exemplars.ExemplarSamplerConfig;
import io.prometheus.metrics.core.metrics.Buffer;
import io.prometheus.metrics.core.metrics.StatefulMetric;
import io.prometheus.metrics.core.util.Scheduler;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.NativeHistogramBuckets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

public class Histogram
extends StatefulMetric<DistributionDataPoint, DataPoint>
implements DistributionDataPoint {
    private static final int CLASSIC_HISTOGRAM = Integer.MIN_VALUE;
    private static final double[][] NATIVE_BOUNDS = new double[8][];
    private final boolean exemplarsEnabled;
    private final ExemplarSamplerConfig exemplarSamplerConfig;
    private final double[] classicUpperBounds;
    private final int nativeInitialSchema;
    private final double nativeMinZeroThreshold;
    private final double nativeMaxZeroThreshold;
    private final int nativeMaxBuckets;
    private final long nativeResetDurationSeconds;

    private Histogram(Builder builder, PrometheusProperties prometheusProperties) {
        super(builder);
        MetricsProperties[] properties = this.getMetricProperties(builder, prometheusProperties);
        this.exemplarsEnabled = this.getConfigProperty(properties, MetricsProperties::getExemplarsEnabled);
        this.nativeInitialSchema = this.getConfigProperty(properties, props -> {
            if (Boolean.TRUE.equals(props.getHistogramClassicOnly())) {
                return Integer.MIN_VALUE;
            }
            return props.getHistogramNativeInitialSchema();
        });
        this.classicUpperBounds = this.getConfigProperty(properties, props -> {
            if (Boolean.TRUE.equals(props.getHistogramNativeOnly())) {
                return new double[0];
            }
            if (props.getHistogramClassicUpperBounds() != null) {
                TreeSet<Double> upperBounds = new TreeSet<Double>(props.getHistogramClassicUpperBounds());
                upperBounds.add(Double.POSITIVE_INFINITY);
                double[] result = new double[upperBounds.size()];
                int i = 0;
                Iterator iterator2 = upperBounds.iterator();
                while (iterator2.hasNext()) {
                    double upperBound = (Double)iterator2.next();
                    result[i++] = upperBound;
                }
                return result;
            }
            return null;
        });
        double max = this.getConfigProperty(properties, MetricsProperties::getHistogramNativeMaxZeroThreshold);
        double min = this.getConfigProperty(properties, MetricsProperties::getHistogramNativeMinZeroThreshold);
        this.nativeMaxZeroThreshold = max == Builder.DEFAULT_NATIVE_MAX_ZERO_THRESHOLD && min > max ? min : max;
        this.nativeMinZeroThreshold = Math.min(min, this.nativeMaxZeroThreshold);
        this.nativeMaxBuckets = this.getConfigProperty(properties, MetricsProperties::getHistogramNativeMaxNumberOfBuckets);
        this.nativeResetDurationSeconds = this.getConfigProperty(properties, MetricsProperties::getHistogramNativeResetDurationSeconds);
        ExemplarsProperties exemplarsProperties = prometheusProperties.getExemplarProperties();
        this.exemplarSamplerConfig = this.classicUpperBounds.length == 0 ? new ExemplarSamplerConfig(exemplarsProperties, 4) : new ExemplarSamplerConfig(exemplarsProperties, this.classicUpperBounds);
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
    protected boolean isExemplarsEnabled() {
        return this.exemplarsEnabled;
    }

    @Override
    public HistogramSnapshot collect() {
        return (HistogramSnapshot)super.collect();
    }

    protected HistogramSnapshot collect(List<Labels> labels, List<DataPoint> metricData) {
        ArrayList<HistogramSnapshot.HistogramDataPointSnapshot> data = new ArrayList<HistogramSnapshot.HistogramDataPointSnapshot>(labels.size());
        for (int i = 0; i < labels.size(); ++i) {
            data.add(metricData.get(i).collect(labels.get(i)));
        }
        return new HistogramSnapshot(this.getMetadata(), (Collection<HistogramSnapshot.HistogramDataPointSnapshot>)data);
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

    static {
        for (int schema = 1; schema <= 8; ++schema) {
            Histogram.NATIVE_BOUNDS[schema - 1] = new double[1 << schema];
            Histogram.NATIVE_BOUNDS[schema - 1][0] = 0.5;
            double base = Math.pow(2.0, Math.pow(2.0, -schema));
            for (int i = 1; i < NATIVE_BOUNDS[schema - 1].length; ++i) {
                Histogram.NATIVE_BOUNDS[schema - 1][i] = i % 2 == 0 && schema > 1 ? NATIVE_BOUNDS[schema - 2][i / 2] : NATIVE_BOUNDS[schema - 1][i - 1] * base;
            }
        }
    }

    public static class Builder
    extends StatefulMetric.Builder<Builder, Histogram> {
        public static final double[] DEFAULT_CLASSIC_UPPER_BOUNDS = new double[]{0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0, 10.0};
        private static final double DEFAULT_NATIVE_MIN_ZERO_THRESHOLD = Math.pow(2.0, -128.0);
        private static final double DEFAULT_NATIVE_MAX_ZERO_THRESHOLD = Math.pow(2.0, -128.0);
        private static final int DEFAULT_NATIVE_INITIAL_SCHEMA = 5;
        private static final int DEFAULT_NATIVE_MAX_NUMBER_OF_BUCKETS = 160;
        private static final long DEFAULT_NATIVE_RESET_DURATION_SECONDS = 0L;
        private Boolean nativeOnly;
        private Boolean classicOnly;
        private double[] classicUpperBounds;
        private Integer nativeInitialSchema;
        private Double nativeMaxZeroThreshold;
        private Double nativeMinZeroThreshold;
        private Integer nativeMaxNumberOfBuckets;
        private Long nativeResetDurationSeconds;

        @Override
        public Histogram build() {
            return new Histogram(this, this.properties);
        }

        @Override
        protected MetricsProperties toProperties() {
            return MetricsProperties.builder().exemplarsEnabled(this.exemplarsEnabled).histogramNativeOnly(this.nativeOnly).histogramClassicOnly(this.classicOnly).histogramClassicUpperBounds(this.classicUpperBounds).histogramNativeInitialSchema(this.nativeInitialSchema).histogramNativeMinZeroThreshold(this.nativeMinZeroThreshold).histogramNativeMaxZeroThreshold(this.nativeMaxZeroThreshold).histogramNativeMaxNumberOfBuckets(this.nativeMaxNumberOfBuckets).histogramNativeResetDurationSeconds(this.nativeResetDurationSeconds).build();
        }

        @Override
        public MetricsProperties getDefaultProperties() {
            return MetricsProperties.builder().exemplarsEnabled(true).histogramNativeOnly(false).histogramClassicOnly(false).histogramClassicUpperBounds(DEFAULT_CLASSIC_UPPER_BOUNDS).histogramNativeInitialSchema(5).histogramNativeMinZeroThreshold(DEFAULT_NATIVE_MIN_ZERO_THRESHOLD).histogramNativeMaxZeroThreshold(DEFAULT_NATIVE_MAX_ZERO_THRESHOLD).histogramNativeMaxNumberOfBuckets(160).histogramNativeResetDurationSeconds(0L).build();
        }

        private Builder(PrometheusProperties config) {
            super(Collections.singletonList("le"), config);
        }

        public Builder nativeOnly() {
            if (Boolean.TRUE.equals(this.classicOnly)) {
                throw new IllegalArgumentException("Cannot call nativeOnly() after calling classicOnly().");
            }
            this.nativeOnly = true;
            return this;
        }

        public Builder classicOnly() {
            if (Boolean.TRUE.equals(this.nativeOnly)) {
                throw new IllegalArgumentException("Cannot call classicOnly() after calling nativeOnly().");
            }
            this.classicOnly = true;
            return this;
        }

        public Builder classicUpperBounds(double ... upperBounds) {
            this.classicUpperBounds = upperBounds;
            for (double bound : upperBounds) {
                if (!Double.isNaN(bound)) continue;
                throw new IllegalArgumentException("Cannot use NaN as upper bound for a histogram");
            }
            return this;
        }

        public Builder classicLinearUpperBounds(double start, double width, int count) {
            this.classicUpperBounds = new double[count];
            BigDecimal s = new BigDecimal(Double.toString(start));
            BigDecimal w = new BigDecimal(Double.toString(width));
            for (int i = 0; i < count; ++i) {
                this.classicUpperBounds[i] = s.add(w.multiply(new BigDecimal(i))).doubleValue();
            }
            return this;
        }

        public Builder classicExponentialUpperBounds(double start, double factor, int count) {
            this.classicUpperBounds = new double[count];
            for (int i = 0; i < count; ++i) {
                this.classicUpperBounds[i] = start * Math.pow(factor, i);
            }
            return this;
        }

        public Builder nativeInitialSchema(int nativeSchema) {
            if (nativeSchema < -4 || nativeSchema > 8) {
                throw new IllegalArgumentException("Unsupported native histogram schema " + nativeSchema + ": expecting -4 <= schema <= 8.");
            }
            this.nativeInitialSchema = nativeSchema;
            return this;
        }

        public Builder nativeMaxZeroThreshold(double nativeMaxZeroThreshold) {
            if (nativeMaxZeroThreshold < 0.0) {
                throw new IllegalArgumentException("Illegal native max zero threshold " + nativeMaxZeroThreshold + ": must be >= 0");
            }
            this.nativeMaxZeroThreshold = nativeMaxZeroThreshold;
            return this;
        }

        public Builder nativeMinZeroThreshold(double nativeMinZeroThreshold) {
            if (nativeMinZeroThreshold < 0.0) {
                throw new IllegalArgumentException("Illegal native min zero threshold " + nativeMinZeroThreshold + ": must be >= 0");
            }
            this.nativeMinZeroThreshold = nativeMinZeroThreshold;
            return this;
        }

        public Builder nativeMaxNumberOfBuckets(int nativeMaxBuckets) {
            this.nativeMaxNumberOfBuckets = nativeMaxBuckets;
            return this;
        }

        public Builder nativeResetDuration(long duration, TimeUnit unit) {
            if (duration <= 0L) {
                throw new IllegalArgumentException(duration + ": value > 0 expected");
            }
            this.nativeResetDurationSeconds = unit.toSeconds(duration);
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    public class DataPoint
    implements DistributionDataPoint {
        private final LongAdder[] classicBuckets;
        private final ConcurrentHashMap<Integer, LongAdder> nativeBucketsForPositiveValues = new ConcurrentHashMap();
        private final ConcurrentHashMap<Integer, LongAdder> nativeBucketsForNegativeValues = new ConcurrentHashMap();
        private final LongAdder nativeZeroCount = new LongAdder();
        private final LongAdder count = new LongAdder();
        private final DoubleAdder sum = new DoubleAdder();
        private volatile int nativeSchema = Histogram.access$100(Histogram.this);
        private volatile double nativeZeroThreshold = Histogram.access$200(Histogram.this);
        private volatile long createdTimeMillis = System.currentTimeMillis();
        private final Buffer buffer = new Buffer();
        private volatile boolean resetDurationExpired = false;
        private final ExemplarSampler exemplarSampler;

        private DataPoint() {
            this.exemplarSampler = Histogram.this.exemplarsEnabled ? new ExemplarSampler(Histogram.this.exemplarSamplerConfig) : null;
            this.classicBuckets = new LongAdder[Histogram.this.classicUpperBounds.length];
            for (int i = 0; i < Histogram.this.classicUpperBounds.length; ++i) {
                this.classicBuckets[i] = new LongAdder();
            }
            this.maybeScheduleNextReset();
        }

        @Override
        public void observe(double value) {
            if (Double.isNaN(value)) {
                return;
            }
            if (!this.buffer.append(value)) {
                this.doObserve(value, false);
            }
            if (Histogram.this.isExemplarsEnabled()) {
                this.exemplarSampler.observe(value);
            }
        }

        @Override
        public void observeWithExemplar(double value, Labels labels) {
            if (Double.isNaN(value)) {
                return;
            }
            if (!this.buffer.append(value)) {
                this.doObserve(value, false);
            }
            if (Histogram.this.isExemplarsEnabled()) {
                this.exemplarSampler.observeWithExemplar(value, labels);
            }
        }

        private void doObserve(double value, boolean fromBuffer) {
            for (int i = 0; i < Histogram.this.classicUpperBounds.length; ++i) {
                if (!(value <= Histogram.this.classicUpperBounds[i])) continue;
                this.classicBuckets[i].add(1L);
                break;
            }
            boolean nativeBucketCreated = false;
            if (Histogram.this.nativeInitialSchema != Integer.MIN_VALUE) {
                if (value > this.nativeZeroThreshold) {
                    nativeBucketCreated = this.addToNativeBucket(value, this.nativeBucketsForPositiveValues);
                } else if (value < -this.nativeZeroThreshold) {
                    nativeBucketCreated = this.addToNativeBucket(-value, this.nativeBucketsForNegativeValues);
                } else {
                    this.nativeZeroCount.add(1L);
                }
            }
            this.sum.add(value);
            this.count.increment();
            if (!fromBuffer) {
                this.maybeResetOrScaleDown(value, nativeBucketCreated);
            }
        }

        private HistogramSnapshot.HistogramDataPointSnapshot collect(Labels labels) {
            Exemplars exemplars = this.exemplarSampler != null ? this.exemplarSampler.collect() : Exemplars.EMPTY;
            return this.buffer.run(expectedCount -> this.count.sum() == expectedCount.longValue(), () -> {
                if (Histogram.this.classicUpperBounds.length == 0) {
                    return new HistogramSnapshot.HistogramDataPointSnapshot(this.nativeSchema, this.nativeZeroCount.sum(), this.nativeZeroThreshold, this.toBucketList(this.nativeBucketsForPositiveValues), this.toBucketList(this.nativeBucketsForNegativeValues), this.sum.sum(), labels, exemplars, this.createdTimeMillis);
                }
                if (Histogram.this.nativeInitialSchema == Integer.MIN_VALUE) {
                    return new HistogramSnapshot.HistogramDataPointSnapshot(ClassicHistogramBuckets.of(Histogram.this.classicUpperBounds, this.classicBuckets), this.sum.sum(), labels, exemplars, this.createdTimeMillis);
                }
                return new HistogramSnapshot.HistogramDataPointSnapshot(ClassicHistogramBuckets.of(Histogram.this.classicUpperBounds, this.classicBuckets), this.nativeSchema, this.nativeZeroCount.sum(), this.nativeZeroThreshold, this.toBucketList(this.nativeBucketsForPositiveValues), this.toBucketList(this.nativeBucketsForNegativeValues), this.sum.sum(), labels, exemplars, this.createdTimeMillis);
            }, v -> this.doObserve((double)v, true));
        }

        private boolean addToNativeBucket(double value, ConcurrentHashMap<Integer, LongAdder> buckets) {
            boolean newBucketCreated = false;
            int bucketIndex = Double.isInfinite(value) ? this.findBucketIndex(Double.MAX_VALUE) + 1 : this.findBucketIndex(value);
            LongAdder bucketCount = buckets.get(bucketIndex);
            if (bucketCount == null) {
                LongAdder newBucketCount = new LongAdder();
                LongAdder existingBucketCount = buckets.putIfAbsent(bucketIndex, newBucketCount);
                if (existingBucketCount == null) {
                    newBucketCreated = true;
                    bucketCount = newBucketCount;
                } else {
                    bucketCount = existingBucketCount;
                }
            }
            bucketCount.increment();
            return newBucketCreated;
        }

        private int findBucketIndex(double value) {
            double frac = value;
            int exp = 0;
            while (frac < 0.5) {
                frac *= 2.0;
                --exp;
            }
            while (frac >= 1.0) {
                frac /= 2.0;
                ++exp;
            }
            if (this.nativeSchema >= 1) {
                return this.findIndex(NATIVE_BOUNDS[this.nativeSchema - 1], frac) + (exp - 1) * NATIVE_BOUNDS[this.nativeSchema - 1].length;
            }
            int bucketIndex = exp;
            if (frac == 0.5) {
                --bucketIndex;
            }
            int offset = (1 << -this.nativeSchema) - 1;
            bucketIndex = bucketIndex + offset >> -this.nativeSchema;
            return bucketIndex;
        }

        private int findIndex(double[] bounds, double frac) {
            int first = 0;
            int last = bounds.length - 1;
            while (first <= last) {
                int mid = (first + last) / 2;
                if (bounds[mid] == frac) {
                    return mid;
                }
                if (bounds[mid] < frac) {
                    first = mid + 1;
                    continue;
                }
                last = mid - 1;
            }
            return last + 1;
        }

        private void maybeResetOrScaleDown(double value, boolean nativeBucketCreated) {
            AtomicBoolean wasReset = new AtomicBoolean(false);
            if (this.resetDurationExpired && this.nativeSchema < Histogram.this.nativeInitialSchema) {
                this.buffer.run(expectedCount -> this.count.sum() == expectedCount.longValue(), () -> {
                    if (this.maybeReset()) {
                        wasReset.set(true);
                    }
                    return null;
                }, v -> this.doObserve((double)v, true));
            } else if (nativeBucketCreated) {
                this.maybeScaleDown(wasReset);
            }
            if (wasReset.get() && !this.buffer.append(value)) {
                this.doObserve(value, true);
            }
        }

        private void maybeScaleDown(AtomicBoolean wasReset) {
            if (Histogram.this.nativeMaxBuckets == 0 || this.nativeSchema == -4) {
                return;
            }
            int numberOfBuckets = this.nativeBucketsForPositiveValues.size() + this.nativeBucketsForNegativeValues.size();
            if (numberOfBuckets <= Histogram.this.nativeMaxBuckets) {
                return;
            }
            this.buffer.run(expectedCount -> this.count.sum() == expectedCount.longValue(), () -> {
                int numBuckets = this.nativeBucketsForPositiveValues.size() + this.nativeBucketsForNegativeValues.size();
                if (numBuckets <= Histogram.this.nativeMaxBuckets || this.nativeSchema == -4) {
                    return null;
                }
                if (this.maybeReset()) {
                    wasReset.set(true);
                    return null;
                }
                if (this.maybeWidenZeroBucket()) {
                    return null;
                }
                this.doubleBucketWidth();
                return null;
            }, v -> this.doObserve((double)v, true));
        }

        private boolean maybeReset() {
            if (!this.resetDurationExpired) {
                return false;
            }
            this.resetDurationExpired = false;
            this.buffer.reset();
            this.nativeBucketsForPositiveValues.clear();
            this.nativeBucketsForNegativeValues.clear();
            this.nativeZeroCount.reset();
            this.count.reset();
            this.sum.reset();
            for (LongAdder classicBucket : this.classicBuckets) {
                classicBucket.reset();
            }
            this.nativeZeroThreshold = Histogram.this.nativeMinZeroThreshold;
            this.nativeSchema = Histogram.this.nativeInitialSchema;
            this.createdTimeMillis = System.currentTimeMillis();
            if (this.exemplarSampler != null) {
                this.exemplarSampler.reset();
            }
            this.maybeScheduleNextReset();
            return true;
        }

        private boolean maybeWidenZeroBucket() {
            if (this.nativeZeroThreshold >= Histogram.this.nativeMaxZeroThreshold) {
                return false;
            }
            int smallestIndex = this.findSmallestIndex(this.nativeBucketsForPositiveValues);
            int smallestNegativeIndex = this.findSmallestIndex(this.nativeBucketsForNegativeValues);
            if (smallestNegativeIndex < smallestIndex) {
                smallestIndex = smallestNegativeIndex;
            }
            if (smallestIndex == Integer.MAX_VALUE) {
                return false;
            }
            double newZeroThreshold = this.nativeBucketIndexToUpperBound(this.nativeSchema, smallestIndex);
            if (newZeroThreshold > Histogram.this.nativeMaxZeroThreshold) {
                return false;
            }
            this.mergeWithZeroBucket(smallestIndex, this.nativeBucketsForPositiveValues);
            this.mergeWithZeroBucket(smallestIndex, this.nativeBucketsForNegativeValues);
            this.nativeZeroThreshold = newZeroThreshold;
            return true;
        }

        private void mergeWithZeroBucket(int index, Map<Integer, LongAdder> buckets) {
            LongAdder count = buckets.remove(index);
            if (count != null) {
                this.nativeZeroCount.add(count.sum());
            }
        }

        private double nativeBucketIndexToUpperBound(int schema, int index) {
            double previousBucketBoundary;
            double result = this.calcUpperBound(schema, index);
            if (Double.isInfinite(result) && Double.isFinite(previousBucketBoundary = this.calcUpperBound(schema, index - 1)) && previousBucketBoundary < Double.MAX_VALUE) {
                return Double.MAX_VALUE;
            }
            return result;
        }

        private double calcUpperBound(int schema, int index) {
            double factor = 1.0;
            while (index > 0) {
                if (index % 2 == 0) {
                    index /= 2;
                    --schema;
                    continue;
                }
                --index;
                factor *= Math.pow(2.0, Math.pow(2.0, -schema));
            }
            return factor * Math.pow(2.0, (double)index * Math.pow(2.0, -schema));
        }

        private int findSmallestIndex(Map<Integer, LongAdder> nativeBuckets) {
            int result = Integer.MAX_VALUE;
            for (int key : nativeBuckets.keySet()) {
                if (key >= result) continue;
                result = key;
            }
            return result;
        }

        private void doubleBucketWidth() {
            this.doubleBucketWidth(this.nativeBucketsForPositiveValues);
            this.doubleBucketWidth(this.nativeBucketsForNegativeValues);
            --this.nativeSchema;
        }

        private void doubleBucketWidth(Map<Integer, LongAdder> buckets) {
            int[] keys2 = new int[buckets.size()];
            long[] values2 = new long[keys2.length];
            int i = 0;
            for (Map.Entry<Integer, LongAdder> entry : buckets.entrySet()) {
                keys2[i] = entry.getKey();
                values2[i] = entry.getValue().sum();
                ++i;
            }
            buckets.clear();
            for (i = 0; i < keys2.length; ++i) {
                int index = (keys2[i] + 1) / 2;
                LongAdder count = buckets.computeIfAbsent(index, k -> new LongAdder());
                count.add(values2[i]);
            }
        }

        private NativeHistogramBuckets toBucketList(ConcurrentHashMap<Integer, LongAdder> map) {
            int[] bucketIndexes = new int[map.size()];
            long[] counts = new long[map.size()];
            int i = 0;
            for (Map.Entry<Integer, LongAdder> entry : map.entrySet()) {
                bucketIndexes[i] = entry.getKey();
                counts[i] = entry.getValue().sum();
                ++i;
            }
            return NativeHistogramBuckets.of(bucketIndexes, counts);
        }

        private void maybeScheduleNextReset() {
            if (Histogram.this.nativeResetDurationSeconds > 0L) {
                Scheduler.schedule(() -> {
                    this.resetDurationExpired = true;
                }, Histogram.this.nativeResetDurationSeconds, TimeUnit.SECONDS);
            }
        }
    }
}

