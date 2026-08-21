/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.exemplars;

import io.prometheus.metrics.config.ExemplarsProperties;
import java.util.concurrent.TimeUnit;

public class ExemplarSamplerConfig {
    public static final int DEFAULT_MIN_RETENTION_PERIOD_SECONDS = 7;
    public static final int DEFAULT_MAX_RETENTION_PERIOD_SECONDS = 70;
    private static final int DEFAULT_SAMPLE_INTERVAL_MILLISECONDS = 90;
    private final long minRetentionPeriodMillis;
    private final long maxRetentionPeriodMillis;
    private final long sampleIntervalMillis;
    private final double[] histogramClassicUpperBounds;
    private final int numberOfExemplars;

    public ExemplarSamplerConfig(ExemplarsProperties properties, int numberOfExemplars) {
        this(properties, numberOfExemplars, null);
    }

    public ExemplarSamplerConfig(ExemplarsProperties properties, double[] histogramClassicUpperBounds) {
        this(properties, histogramClassicUpperBounds.length, histogramClassicUpperBounds);
    }

    private ExemplarSamplerConfig(ExemplarsProperties properties, int numberOfExemplars, double[] histogramClassicUpperBounds) {
        this(TimeUnit.SECONDS.toMillis(ExemplarSamplerConfig.getOrDefault(properties.getMinRetentionPeriodSeconds(), 7).intValue()), TimeUnit.SECONDS.toMillis(ExemplarSamplerConfig.getOrDefault(properties.getMaxRetentionPeriodSeconds(), 70).intValue()), ExemplarSamplerConfig.getOrDefault(properties.getSampleIntervalMilliseconds(), 90).intValue(), numberOfExemplars, histogramClassicUpperBounds);
    }

    ExemplarSamplerConfig(long minRetentionPeriodMillis, long maxRetentionPeriodMillis, long sampleIntervalMillis, int numberOfExemplars, double[] histogramClassicUpperBounds) {
        this.minRetentionPeriodMillis = minRetentionPeriodMillis;
        this.maxRetentionPeriodMillis = maxRetentionPeriodMillis;
        this.sampleIntervalMillis = sampleIntervalMillis;
        this.numberOfExemplars = numberOfExemplars;
        this.histogramClassicUpperBounds = histogramClassicUpperBounds;
        this.validate();
    }

    private void validate() {
        if (this.minRetentionPeriodMillis <= 0L) {
            throw new IllegalArgumentException(this.minRetentionPeriodMillis + ": minRetentionPeriod must be > 0.");
        }
        if (this.maxRetentionPeriodMillis <= 0L) {
            throw new IllegalArgumentException(this.maxRetentionPeriodMillis + ": maxRetentionPeriod must be > 0.");
        }
        if (this.histogramClassicUpperBounds != null) {
            if (this.histogramClassicUpperBounds.length == 0 || this.histogramClassicUpperBounds[this.histogramClassicUpperBounds.length - 1] != Double.POSITIVE_INFINITY) {
                throw new IllegalArgumentException("histogramClassicUpperBounds must contain the +Inf bucket.");
            }
            if (this.histogramClassicUpperBounds.length != this.numberOfExemplars) {
                throw new IllegalArgumentException("histogramClassicUpperBounds.length must be equal to numberOfExemplars.");
            }
            double bound = this.histogramClassicUpperBounds[0];
            for (int i = 1; i < this.histogramClassicUpperBounds.length; ++i) {
                if (!(bound >= this.histogramClassicUpperBounds[i])) continue;
                throw new IllegalArgumentException("histogramClassicUpperBounds must be sorted and must not contain duplicates.");
            }
        }
        if (this.numberOfExemplars <= 0) {
            throw new IllegalArgumentException(this.numberOfExemplars + ": numberOfExemplars must be > 0.");
        }
    }

    private static <T> T getOrDefault(T result, T defaultValue) {
        return result != null ? result : defaultValue;
    }

    public double[] getHistogramClassicUpperBounds() {
        return this.histogramClassicUpperBounds;
    }

    public long getMinRetentionPeriodMillis() {
        return this.minRetentionPeriodMillis;
    }

    public long getMaxRetentionPeriodMillis() {
        return this.maxRetentionPeriodMillis;
    }

    public long getSampleIntervalMillis() {
        return this.sampleIntervalMillis;
    }

    public int getNumberOfExemplars() {
        return this.numberOfExemplars;
    }
}

