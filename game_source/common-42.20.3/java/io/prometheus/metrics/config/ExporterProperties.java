/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.config;

import io.prometheus.metrics.config.PrometheusPropertiesException;
import io.prometheus.metrics.config.Util;
import java.util.Map;

public class ExporterProperties {
    private static final String INCLUDE_CREATED_TIMESTAMPS = "includeCreatedTimestamps";
    private static final String PROMETHEUS_TIMESTAMPS_IN_MS = "prometheusTimestampsInMs";
    private static final String EXEMPLARS_ON_ALL_METRIC_TYPES = "exemplarsOnAllMetricTypes";
    private static final String PREFIX = "io.prometheus.exporter";
    private final Boolean includeCreatedTimestamps;
    private final Boolean prometheusTimestampsInMs;
    private final Boolean exemplarsOnAllMetricTypes;

    private ExporterProperties(Boolean includeCreatedTimestamps, Boolean prometheusTimestampsInMs, Boolean exemplarsOnAllMetricTypes) {
        this.includeCreatedTimestamps = includeCreatedTimestamps;
        this.prometheusTimestampsInMs = prometheusTimestampsInMs;
        this.exemplarsOnAllMetricTypes = exemplarsOnAllMetricTypes;
    }

    public boolean getIncludeCreatedTimestamps() {
        return this.includeCreatedTimestamps != null && this.includeCreatedTimestamps != false;
    }

    public boolean getPrometheusTimestampsInMs() {
        return this.prometheusTimestampsInMs != null && this.prometheusTimestampsInMs != false;
    }

    public boolean getExemplarsOnAllMetricTypes() {
        return this.exemplarsOnAllMetricTypes != null && this.exemplarsOnAllMetricTypes != false;
    }

    static ExporterProperties load(Map<Object, Object> properties) throws PrometheusPropertiesException {
        Boolean includeCreatedTimestamps = Util.loadBoolean("io.prometheus.exporter.includeCreatedTimestamps", properties);
        Boolean timestampsInMs = Util.loadBoolean("io.prometheus.exporter.prometheusTimestampsInMs", properties);
        Boolean exemplarsOnAllMetricTypes = Util.loadBoolean("io.prometheus.exporter.exemplarsOnAllMetricTypes", properties);
        return new ExporterProperties(includeCreatedTimestamps, timestampsInMs, exemplarsOnAllMetricTypes);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Boolean includeCreatedTimestamps;
        private Boolean exemplarsOnAllMetricTypes;
        boolean prometheusTimestampsInMs;

        private Builder() {
        }

        public Builder includeCreatedTimestamps(boolean includeCreatedTimestamps) {
            this.includeCreatedTimestamps = includeCreatedTimestamps;
            return this;
        }

        public Builder exemplarsOnAllMetricTypes(boolean exemplarsOnAllMetricTypes) {
            this.exemplarsOnAllMetricTypes = exemplarsOnAllMetricTypes;
            return this;
        }

        public Builder prometheusTimestampsInMs(boolean prometheusTimestampsInMs) {
            this.prometheusTimestampsInMs = prometheusTimestampsInMs;
            return this;
        }

        public ExporterProperties build() {
            return new ExporterProperties(this.includeCreatedTimestamps, this.prometheusTimestampsInMs, this.exemplarsOnAllMetricTypes);
        }
    }
}

