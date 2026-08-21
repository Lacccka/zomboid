/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.DistributionDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Exemplars;
import io.prometheus.metrics.model.snapshots.Label;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.NativeHistogramBuckets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class HistogramSnapshot
extends MetricSnapshot {
    private final boolean gaugeHistogram;
    public static final int CLASSIC_HISTOGRAM = Integer.MIN_VALUE;

    public HistogramSnapshot(MetricMetadata metadata, Collection<HistogramDataPointSnapshot> data) {
        this(false, metadata, data);
    }

    public HistogramSnapshot(boolean isGaugeHistogram, MetricMetadata metadata, Collection<HistogramDataPointSnapshot> data) {
        super(metadata, data);
        this.gaugeHistogram = isGaugeHistogram;
    }

    public boolean isGaugeHistogram() {
        return this.gaugeHistogram;
    }

    public List<HistogramDataPointSnapshot> getDataPoints() {
        return this.dataPoints;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
    extends MetricSnapshot.Builder<Builder> {
        private final List<HistogramDataPointSnapshot> dataPoints = new ArrayList<HistogramDataPointSnapshot>();
        private boolean isGaugeHistogram = false;

        private Builder() {
        }

        public Builder dataPoint(HistogramDataPointSnapshot dataPoint) {
            this.dataPoints.add(dataPoint);
            return this;
        }

        public Builder gaugeHistogram(boolean isGaugeHistogram) {
            this.isGaugeHistogram = isGaugeHistogram;
            return this;
        }

        @Override
        public HistogramSnapshot build() {
            return new HistogramSnapshot(this.isGaugeHistogram, this.buildMetadata(), this.dataPoints);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    public static final class HistogramDataPointSnapshot
    extends DistributionDataPointSnapshot {
        private final ClassicHistogramBuckets classicBuckets;
        private final int nativeSchema;
        private final long nativeZeroCount;
        private final double nativeZeroThreshold;
        private final NativeHistogramBuckets nativeBucketsForPositiveValues;
        private final NativeHistogramBuckets nativeBucketsForNegativeValues;

        public HistogramDataPointSnapshot(ClassicHistogramBuckets classicBuckets, double sum, Labels labels, Exemplars exemplars, long createdTimestampMillis) {
            this(classicBuckets, Integer.MIN_VALUE, 0L, 0.0, NativeHistogramBuckets.EMPTY, NativeHistogramBuckets.EMPTY, sum, labels, exemplars, createdTimestampMillis, 0L);
        }

        public HistogramDataPointSnapshot(int nativeSchema, long nativeZeroCount, double nativeZeroThreshold, NativeHistogramBuckets nativeBucketsForPositiveValues, NativeHistogramBuckets nativeBucketsForNegativeValues, double sum, Labels labels, Exemplars exemplars, long createdTimestampMillis) {
            this(ClassicHistogramBuckets.EMPTY, nativeSchema, nativeZeroCount, nativeZeroThreshold, nativeBucketsForPositiveValues, nativeBucketsForNegativeValues, sum, labels, exemplars, createdTimestampMillis, 0L);
        }

        public HistogramDataPointSnapshot(ClassicHistogramBuckets classicBuckets, int nativeSchema, long nativeZeroCount, double nativeZeroThreshold, NativeHistogramBuckets nativeBucketsForPositiveValues, NativeHistogramBuckets nativeBucketsForNegativeValues, double sum, Labels labels, Exemplars exemplars, long createdTimestampMillis) {
            this(classicBuckets, nativeSchema, nativeZeroCount, nativeZeroThreshold, nativeBucketsForPositiveValues, nativeBucketsForNegativeValues, sum, labels, exemplars, createdTimestampMillis, 0L);
        }

        public HistogramDataPointSnapshot(ClassicHistogramBuckets classicBuckets, int nativeSchema, long nativeZeroCount, double nativeZeroThreshold, NativeHistogramBuckets nativeBucketsForPositiveValues, NativeHistogramBuckets nativeBucketsForNegativeValues, double sum, Labels labels, Exemplars exemplars, long createdTimestampMillis, long scrapeTimestampMillis) {
            super(HistogramDataPointSnapshot.calculateCount(classicBuckets, nativeSchema, nativeZeroCount, nativeBucketsForPositiveValues, nativeBucketsForNegativeValues), sum, exemplars, labels, createdTimestampMillis, scrapeTimestampMillis);
            this.classicBuckets = classicBuckets;
            this.nativeSchema = nativeSchema;
            this.nativeZeroCount = nativeSchema == Integer.MIN_VALUE ? 0L : nativeZeroCount;
            this.nativeZeroThreshold = nativeSchema == Integer.MIN_VALUE ? 0.0 : nativeZeroThreshold;
            this.nativeBucketsForPositiveValues = nativeSchema == Integer.MIN_VALUE ? NativeHistogramBuckets.EMPTY : nativeBucketsForPositiveValues;
            this.nativeBucketsForNegativeValues = nativeSchema == Integer.MIN_VALUE ? NativeHistogramBuckets.EMPTY : nativeBucketsForNegativeValues;
            this.validate();
        }

        private static long calculateCount(ClassicHistogramBuckets classicBuckets, int nativeSchema, long nativeZeroCount, NativeHistogramBuckets nativeBucketsForPositiveValues, NativeHistogramBuckets nativeBucketsForNegativeValues) {
            long nativeCount;
            if (classicBuckets.isEmpty()) {
                return HistogramDataPointSnapshot.calculateNativeCount(nativeZeroCount, nativeBucketsForPositiveValues, nativeBucketsForNegativeValues);
            }
            if (nativeSchema == Integer.MIN_VALUE) {
                return HistogramDataPointSnapshot.calculateClassicCount(classicBuckets);
            }
            long classicCount = HistogramDataPointSnapshot.calculateClassicCount(classicBuckets);
            if (classicCount != (nativeCount = HistogramDataPointSnapshot.calculateNativeCount(nativeZeroCount, nativeBucketsForPositiveValues, nativeBucketsForNegativeValues))) {
                throw new IllegalArgumentException("Inconsistent observation count: If a histogram has both classic and native data the observation count must be the same. Classic count is " + classicCount + " but native count is " + nativeCount + ".");
            }
            return classicCount;
        }

        private static long calculateClassicCount(ClassicHistogramBuckets classicBuckets) {
            long count = 0L;
            for (int i = 0; i < classicBuckets.size(); ++i) {
                count += classicBuckets.getCount(i);
            }
            return count;
        }

        private static long calculateNativeCount(long nativeZeroCount, NativeHistogramBuckets nativeBucketsForPositiveValues, NativeHistogramBuckets nativeBucketsForNegativeValues) {
            int i;
            long count = nativeZeroCount;
            for (i = 0; i < nativeBucketsForNegativeValues.size(); ++i) {
                count += nativeBucketsForNegativeValues.getCount(i);
            }
            for (i = 0; i < nativeBucketsForPositiveValues.size(); ++i) {
                count += nativeBucketsForPositiveValues.getCount(i);
            }
            return count;
        }

        public boolean hasClassicHistogramData() {
            return !this.classicBuckets.isEmpty();
        }

        public boolean hasNativeHistogramData() {
            return this.nativeSchema != Integer.MIN_VALUE;
        }

        public ClassicHistogramBuckets getClassicBuckets() {
            return this.classicBuckets;
        }

        public int getNativeSchema() {
            return this.nativeSchema;
        }

        public long getNativeZeroCount() {
            return this.nativeZeroCount;
        }

        public double getNativeZeroThreshold() {
            return this.nativeZeroThreshold;
        }

        public NativeHistogramBuckets getNativeBucketsForPositiveValues() {
            return this.nativeBucketsForPositiveValues;
        }

        public NativeHistogramBuckets getNativeBucketsForNegativeValues() {
            return this.nativeBucketsForNegativeValues;
        }

        private void validate() {
            for (Label label : this.getLabels()) {
                if (!label.getName().equals("le")) continue;
                throw new IllegalArgumentException("le is a reserved label name for histograms");
            }
            if (this.nativeSchema == Integer.MIN_VALUE && this.classicBuckets.isEmpty()) {
                throw new IllegalArgumentException("Histogram buckets cannot be empty, must at least have the +Inf bucket.");
            }
            if (this.nativeSchema != Integer.MIN_VALUE) {
                if (this.nativeSchema < -4 || this.nativeSchema > 8) {
                    throw new IllegalArgumentException(this.nativeSchema + ": illegal schema. Expecting number in [-4, 8].");
                }
                if (this.nativeZeroCount < 0L) {
                    throw new IllegalArgumentException(this.nativeZeroCount + ": nativeZeroCount cannot be negative");
                }
                if (Double.isNaN(this.nativeZeroThreshold) || this.nativeZeroThreshold < 0.0) {
                    throw new IllegalArgumentException(this.nativeZeroThreshold + ": illegal nativeZeroThreshold. Must be >= 0.");
                }
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder
        extends DistributionDataPointSnapshot.Builder<Builder> {
            private ClassicHistogramBuckets classicHistogramBuckets = ClassicHistogramBuckets.EMPTY;
            private int nativeSchema = Integer.MIN_VALUE;
            private long nativeZeroCount = 0L;
            private double nativeZeroThreshold = 0.0;
            private NativeHistogramBuckets nativeBucketsForPositiveValues = NativeHistogramBuckets.EMPTY;
            private NativeHistogramBuckets nativeBucketsForNegativeValues = NativeHistogramBuckets.EMPTY;

            private Builder() {
            }

            @Override
            protected Builder self() {
                return this;
            }

            public Builder classicHistogramBuckets(ClassicHistogramBuckets classicBuckets) {
                this.classicHistogramBuckets = classicBuckets;
                return this;
            }

            public Builder nativeSchema(int nativeSchema) {
                this.nativeSchema = nativeSchema;
                return this;
            }

            public Builder nativeZeroCount(long zeroCount) {
                this.nativeZeroCount = zeroCount;
                return this;
            }

            public Builder nativeZeroThreshold(double zeroThreshold) {
                this.nativeZeroThreshold = zeroThreshold;
                return this;
            }

            public Builder nativeBucketsForPositiveValues(NativeHistogramBuckets bucketsForPositiveValues) {
                this.nativeBucketsForPositiveValues = bucketsForPositiveValues;
                return this;
            }

            public Builder nativeBucketsForNegativeValues(NativeHistogramBuckets bucketsForNegativeValues) {
                this.nativeBucketsForNegativeValues = bucketsForNegativeValues;
                return this;
            }

            public HistogramDataPointSnapshot build() {
                if (this.nativeSchema == Integer.MIN_VALUE && this.classicHistogramBuckets.isEmpty()) {
                    throw new IllegalArgumentException("One of nativeSchema and classicHistogramBuckets is required.");
                }
                return new HistogramDataPointSnapshot(this.classicHistogramBuckets, this.nativeSchema, this.nativeZeroCount, this.nativeZeroThreshold, this.nativeBucketsForPositiveValues, this.nativeBucketsForNegativeValues, this.sum, this.labels, this.exemplars, this.createdTimestampMillis, this.scrapeTimestampMillis);
            }
        }
    }
}

