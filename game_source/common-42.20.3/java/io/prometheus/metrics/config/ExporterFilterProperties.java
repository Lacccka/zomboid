/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.config;

import io.prometheus.metrics.config.PrometheusPropertiesException;
import io.prometheus.metrics.config.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ExporterFilterProperties {
    public static final String METRIC_NAME_MUST_BE_EQUAL_TO = "metricNameMustBeEqualTo";
    public static final String METRIC_NAME_MUST_NOT_BE_EQUAL_TO = "metricNameMustNotBeEqualTo";
    public static final String METRIC_NAME_MUST_START_WITH = "metricNameMustStartWith";
    public static final String METRIC_NAME_MUST_NOT_START_WITH = "metricNameMustNotStartWith";
    private static final String PREFIX = "io.prometheus.exporter.filter";
    private final List<String> allowedNames;
    private final List<String> excludedNames;
    private final List<String> allowedPrefixes;
    private final List<String> excludedPrefixes;

    private ExporterFilterProperties(List<String> allowedNames, List<String> excludedNames, List<String> allowedPrefixes, List<String> excludedPrefixes) {
        this.allowedNames = allowedNames == null ? null : Collections.unmodifiableList(new ArrayList<String>(allowedNames));
        this.excludedNames = excludedNames == null ? null : Collections.unmodifiableList(new ArrayList<String>(excludedNames));
        this.allowedPrefixes = allowedPrefixes == null ? null : Collections.unmodifiableList(new ArrayList<String>(allowedPrefixes));
        this.excludedPrefixes = excludedPrefixes == null ? null : Collections.unmodifiableList(new ArrayList<String>(excludedPrefixes));
    }

    public List<String> getAllowedMetricNames() {
        return this.allowedNames;
    }

    public List<String> getExcludedMetricNames() {
        return this.excludedNames;
    }

    public List<String> getAllowedMetricNamePrefixes() {
        return this.allowedPrefixes;
    }

    public List<String> getExcludedMetricNamePrefixes() {
        return this.excludedPrefixes;
    }

    static ExporterFilterProperties load(Map<Object, Object> properties) throws PrometheusPropertiesException {
        List<String> allowedNames = Util.loadStringList("io.prometheus.exporter.filter.metricNameMustBeEqualTo", properties);
        List<String> excludedNames = Util.loadStringList("io.prometheus.exporter.filter.metricNameMustNotBeEqualTo", properties);
        List<String> allowedPrefixes = Util.loadStringList("io.prometheus.exporter.filter.metricNameMustStartWith", properties);
        List<String> excludedPrefixes = Util.loadStringList("io.prometheus.exporter.filter.metricNameMustNotStartWith", properties);
        return new ExporterFilterProperties(allowedNames, excludedNames, allowedPrefixes, excludedPrefixes);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<String> allowedNames;
        private List<String> excludedNames;
        private List<String> allowedPrefixes;
        private List<String> excludedPrefixes;

        private Builder() {
        }

        public Builder allowedNames(String ... allowedNames) {
            this.allowedNames = Arrays.asList(allowedNames);
            return this;
        }

        public Builder excludedNames(String ... excludedNames) {
            this.excludedNames = Arrays.asList(excludedNames);
            return this;
        }

        public Builder allowedPrefixes(String ... allowedPrefixes) {
            this.allowedPrefixes = Arrays.asList(allowedPrefixes);
            return this;
        }

        public Builder excludedPrefixes(String ... excludedPrefixes) {
            this.excludedPrefixes = Arrays.asList(excludedPrefixes);
            return this;
        }

        public ExporterFilterProperties build() {
            return new ExporterFilterProperties(this.allowedNames, this.excludedNames, this.allowedPrefixes, this.excludedPrefixes);
        }
    }
}

