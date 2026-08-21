/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.config;

import io.prometheus.metrics.config.PrometheusPropertiesException;
import io.prometheus.metrics.config.Util;
import java.util.Map;

public class ExemplarsProperties {
    private static final String PREFIX = "io.prometheus.exemplars";
    private static final String MIN_RETENTION_PERIOD_SECONDS = "minRetentionPeriodSeconds";
    private static final String MAX_RETENTION_PERIOD_SECONDS = "maxRetentionPeriodSeconds";
    private static final String SAMPLE_INTERVAL_MILLISECONDS = "sampleIntervalMilliseconds";
    private final Integer minRetentionPeriodSeconds;
    private final Integer maxRetentionPeriodSeconds;
    private final Integer sampleIntervalMilliseconds;

    private ExemplarsProperties(Integer minRetentionPeriodSeconds, Integer maxRetentionPeriodSeconds, Integer sampleIntervalMilliseconds) {
        this.minRetentionPeriodSeconds = minRetentionPeriodSeconds;
        this.maxRetentionPeriodSeconds = maxRetentionPeriodSeconds;
        this.sampleIntervalMilliseconds = sampleIntervalMilliseconds;
    }

    public Integer getMinRetentionPeriodSeconds() {
        return this.minRetentionPeriodSeconds;
    }

    public Integer getMaxRetentionPeriodSeconds() {
        return this.maxRetentionPeriodSeconds;
    }

    public Integer getSampleIntervalMilliseconds() {
        return this.sampleIntervalMilliseconds;
    }

    static ExemplarsProperties load(Map<Object, Object> properties) throws PrometheusPropertiesException {
        Integer minRetentionPeriodSeconds = Util.loadInteger("io.prometheus.exemplars.minRetentionPeriodSeconds", properties);
        Integer maxRetentionPeriodSeconds = Util.loadInteger("io.prometheus.exemplars.maxRetentionPeriodSeconds", properties);
        Integer sampleIntervalMilliseconds = Util.loadInteger("io.prometheus.exemplars.sampleIntervalMilliseconds", properties);
        Util.assertValue(minRetentionPeriodSeconds, t -> t > 0, "Expecting value > 0.", PREFIX, MIN_RETENTION_PERIOD_SECONDS);
        Util.assertValue(maxRetentionPeriodSeconds, t -> t > 0, "Expecting value > 0.", PREFIX, MAX_RETENTION_PERIOD_SECONDS);
        Util.assertValue(sampleIntervalMilliseconds, t -> t > 0, "Expecting value > 0.", PREFIX, SAMPLE_INTERVAL_MILLISECONDS);
        if (minRetentionPeriodSeconds != null && maxRetentionPeriodSeconds != null && minRetentionPeriodSeconds > maxRetentionPeriodSeconds) {
            throw new PrometheusPropertiesException("io.prometheus.exemplars.minRetentionPeriodSeconds must not be greater than io.prometheus.exemplars.maxRetentionPeriodSeconds.");
        }
        return new ExemplarsProperties(minRetentionPeriodSeconds, maxRetentionPeriodSeconds, sampleIntervalMilliseconds);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer minRetentionPeriodSeconds;
        private Integer maxRetentionPeriodSeconds;
        private Integer sampleIntervalMilliseconds;

        private Builder() {
        }

        public Builder minRetentionPeriodSeconds(int minRetentionPeriodSeconds) {
            this.minRetentionPeriodSeconds = minRetentionPeriodSeconds;
            return this;
        }

        public Builder maxRetentionPeriodSeconds(int maxRetentionPeriodSeconds) {
            this.maxRetentionPeriodSeconds = maxRetentionPeriodSeconds;
            return this;
        }

        public Builder sampleIntervalMilliseconds(int sampleIntervalMilliseconds) {
            this.sampleIntervalMilliseconds = sampleIntervalMilliseconds;
            return this;
        }

        public ExemplarsProperties build() {
            return new ExemplarsProperties(this.minRetentionPeriodSeconds, this.maxRetentionPeriodSeconds, this.sampleIntervalMilliseconds);
        }
    }
}

