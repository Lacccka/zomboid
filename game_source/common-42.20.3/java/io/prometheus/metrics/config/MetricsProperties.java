/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.config;

import io.prometheus.metrics.config.PrometheusPropertiesException;
import io.prometheus.metrics.config.Util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MetricsProperties {
    private static final String EXEMPLARS_ENABLED = "exemplarsEnabled";
    private static final String HISTOGRAM_NATIVE_ONLY = "histogramNativeOnly";
    private static final String HISTOGRAM_CLASSIC_ONLY = "histogramClassicOnly";
    private static final String HISTOGRAM_CLASSIC_UPPER_BOUNDS = "histogramClassicUpperBounds";
    private static final String HISTOGRAM_NATIVE_INITIAL_SCHEMA = "histogramNativeInitialSchema";
    private static final String HISTOGRAM_NATIVE_MIN_ZERO_THRESHOLD = "histogramNativeMinZeroThreshold";
    private static final String HISTOGRAM_NATIVE_MAX_ZERO_THRESHOLD = "histogramNativeMaxZeroThreshold";
    private static final String HISTOGRAM_NATIVE_MAX_NUMBER_OF_BUCKETS = "histogramNativeMaxNumberOfBuckets";
    private static final String HISTOGRAM_NATIVE_RESET_DURATION_SECONDS = "histogramNativeResetDurationSeconds";
    private static final String SUMMARY_QUANTILES = "summaryQuantiles";
    private static final String SUMMARY_QUANTILE_ERRORS = "summaryQuantileErrors";
    private static final String SUMMARY_MAX_AGE_SECONDS = "summaryMaxAgeSeconds";
    private static final String SUMMARY_NUMBER_OF_AGE_BUCKETS = "summaryNumberOfAgeBuckets";
    private final Boolean exemplarsEnabled;
    private final Boolean histogramNativeOnly;
    private final Boolean histogramClassicOnly;
    private final List<Double> histogramClassicUpperBounds;
    private final Integer histogramNativeInitialSchema;
    private final Double histogramNativeMinZeroThreshold;
    private final Double histogramNativeMaxZeroThreshold;
    private final Integer histogramNativeMaxNumberOfBuckets;
    private final Long histogramNativeResetDurationSeconds;
    private final List<Double> summaryQuantiles;
    private final List<Double> summaryQuantileErrors;
    private final Long summaryMaxAgeSeconds;
    private final Integer summaryNumberOfAgeBuckets;

    public MetricsProperties(Boolean exemplarsEnabled, Boolean histogramNativeOnly, Boolean histogramClassicOnly, List<Double> histogramClassicUpperBounds, Integer histogramNativeInitialSchema, Double histogramNativeMinZeroThreshold, Double histogramNativeMaxZeroThreshold, Integer histogramNativeMaxNumberOfBuckets, Long histogramNativeResetDurationSeconds, List<Double> summaryQuantiles, List<Double> summaryQuantileErrors, Long summaryMaxAgeSeconds, Integer summaryNumberOfAgeBuckets) {
        this(exemplarsEnabled, histogramNativeOnly, histogramClassicOnly, histogramClassicUpperBounds, histogramNativeInitialSchema, histogramNativeMinZeroThreshold, histogramNativeMaxZeroThreshold, histogramNativeMaxNumberOfBuckets, histogramNativeResetDurationSeconds, summaryQuantiles, summaryQuantileErrors, summaryMaxAgeSeconds, summaryNumberOfAgeBuckets, "");
    }

    private MetricsProperties(Boolean exemplarsEnabled, Boolean histogramNativeOnly, Boolean histogramClassicOnly, List<Double> histogramClassicUpperBounds, Integer histogramNativeInitialSchema, Double histogramNativeMinZeroThreshold, Double histogramNativeMaxZeroThreshold, Integer histogramNativeMaxNumberOfBuckets, Long histogramNativeResetDurationSeconds, List<Double> summaryQuantiles, List<Double> summaryQuantileErrors, Long summaryMaxAgeSeconds, Integer summaryNumberOfAgeBuckets, String configPropertyPrefix) {
        this.exemplarsEnabled = exemplarsEnabled;
        this.histogramNativeOnly = this.isHistogramNativeOnly(histogramClassicOnly, histogramNativeOnly);
        this.histogramClassicOnly = this.isHistogramClassicOnly(histogramClassicOnly, histogramNativeOnly);
        this.histogramClassicUpperBounds = histogramClassicUpperBounds == null ? null : Collections.unmodifiableList(new ArrayList<Double>(histogramClassicUpperBounds));
        this.histogramNativeInitialSchema = histogramNativeInitialSchema;
        this.histogramNativeMinZeroThreshold = histogramNativeMinZeroThreshold;
        this.histogramNativeMaxZeroThreshold = histogramNativeMaxZeroThreshold;
        this.histogramNativeMaxNumberOfBuckets = histogramNativeMaxNumberOfBuckets;
        this.histogramNativeResetDurationSeconds = histogramNativeResetDurationSeconds;
        this.summaryQuantiles = summaryQuantiles == null ? null : Collections.unmodifiableList(new ArrayList<Double>(summaryQuantiles));
        this.summaryQuantileErrors = summaryQuantileErrors == null ? null : Collections.unmodifiableList(new ArrayList<Double>(summaryQuantileErrors));
        this.summaryMaxAgeSeconds = summaryMaxAgeSeconds;
        this.summaryNumberOfAgeBuckets = summaryNumberOfAgeBuckets;
        this.validate(configPropertyPrefix);
    }

    private Boolean isHistogramClassicOnly(Boolean histogramClassicOnly, Boolean histogramNativeOnly) {
        if (histogramClassicOnly == null && histogramNativeOnly == null) {
            return null;
        }
        if (histogramClassicOnly != null) {
            return histogramClassicOnly;
        }
        return histogramNativeOnly == false;
    }

    private Boolean isHistogramNativeOnly(Boolean histogramClassicOnly, Boolean histogramNativeOnly) {
        if (histogramClassicOnly == null && histogramNativeOnly == null) {
            return null;
        }
        if (histogramNativeOnly != null) {
            return histogramNativeOnly;
        }
        return histogramClassicOnly == false;
    }

    private void validate(String prefix) throws PrometheusPropertiesException {
        Util.assertValue(this.histogramNativeInitialSchema, s -> s >= -4 && s <= 8, "Expecting number between -4 and +8.", prefix, HISTOGRAM_NATIVE_INITIAL_SCHEMA);
        Util.assertValue(this.histogramNativeMinZeroThreshold, t -> t >= 0.0, "Expecting value >= 0.", prefix, HISTOGRAM_NATIVE_MIN_ZERO_THRESHOLD);
        Util.assertValue(this.histogramNativeMaxZeroThreshold, t -> t >= 0.0, "Expecting value >= 0.", prefix, HISTOGRAM_NATIVE_MAX_ZERO_THRESHOLD);
        Util.assertValue(this.histogramNativeMaxNumberOfBuckets, n -> n >= 0, "Expecting value >= 0.", prefix, HISTOGRAM_NATIVE_MAX_NUMBER_OF_BUCKETS);
        Util.assertValue(this.histogramNativeResetDurationSeconds, t -> t >= 0L, "Expecting value >= 0.", prefix, HISTOGRAM_NATIVE_RESET_DURATION_SECONDS);
        Util.assertValue(this.summaryMaxAgeSeconds, t -> t > 0L, "Expecting value > 0.", prefix, SUMMARY_MAX_AGE_SECONDS);
        Util.assertValue(this.summaryNumberOfAgeBuckets, t -> t > 0, "Expecting value > 0.", prefix, SUMMARY_NUMBER_OF_AGE_BUCKETS);
        if (Boolean.TRUE.equals(this.histogramNativeOnly) && Boolean.TRUE.equals(this.histogramClassicOnly)) {
            throw new PrometheusPropertiesException(prefix + "." + HISTOGRAM_NATIVE_ONLY + " and " + prefix + "." + HISTOGRAM_CLASSIC_ONLY + " cannot both be true");
        }
        if (this.histogramNativeMinZeroThreshold != null && this.histogramNativeMaxZeroThreshold != null && this.histogramNativeMinZeroThreshold > this.histogramNativeMaxZeroThreshold) {
            throw new PrometheusPropertiesException(prefix + "." + HISTOGRAM_NATIVE_MIN_ZERO_THRESHOLD + " cannot be greater than " + prefix + "." + HISTOGRAM_NATIVE_MAX_ZERO_THRESHOLD);
        }
        if (this.summaryQuantiles != null) {
            for (double quantile : this.summaryQuantiles) {
                if (!(quantile < 0.0) && !(quantile > 1.0)) continue;
                throw new PrometheusPropertiesException(prefix + "." + SUMMARY_QUANTILES + ": Expecting 0.0 <= quantile <= 1.0. Found: " + quantile);
            }
        }
        if (this.summaryQuantileErrors != null) {
            if (this.summaryQuantiles == null) {
                throw new PrometheusPropertiesException(prefix + "." + SUMMARY_QUANTILE_ERRORS + ": Can't configure " + SUMMARY_QUANTILE_ERRORS + " without configuring " + SUMMARY_QUANTILES);
            }
            if (this.summaryQuantileErrors.size() != this.summaryQuantiles.size()) {
                throw new PrometheusPropertiesException(prefix + "." + SUMMARY_QUANTILE_ERRORS + ": must have the same length as " + SUMMARY_QUANTILES);
            }
            for (double error : this.summaryQuantileErrors) {
                if (!(error < 0.0) && !(error > 1.0)) continue;
                throw new PrometheusPropertiesException(prefix + "." + SUMMARY_QUANTILE_ERRORS + ": Expecting 0.0 <= error <= 1.0");
            }
        }
    }

    public Boolean getExemplarsEnabled() {
        return this.exemplarsEnabled;
    }

    public Boolean getHistogramNativeOnly() {
        return this.histogramNativeOnly;
    }

    public Boolean getHistogramClassicOnly() {
        return this.histogramClassicOnly;
    }

    public List<Double> getHistogramClassicUpperBounds() {
        return this.histogramClassicUpperBounds;
    }

    public Integer getHistogramNativeInitialSchema() {
        return this.histogramNativeInitialSchema;
    }

    public Double getHistogramNativeMinZeroThreshold() {
        return this.histogramNativeMinZeroThreshold;
    }

    public Double getHistogramNativeMaxZeroThreshold() {
        return this.histogramNativeMaxZeroThreshold;
    }

    public Integer getHistogramNativeMaxNumberOfBuckets() {
        return this.histogramNativeMaxNumberOfBuckets;
    }

    public Long getHistogramNativeResetDurationSeconds() {
        return this.histogramNativeResetDurationSeconds;
    }

    public List<Double> getSummaryQuantiles() {
        return this.summaryQuantiles;
    }

    public List<Double> getSummaryQuantileErrors() {
        if (this.summaryQuantiles != null && this.summaryQuantileErrors == null) {
            return Collections.emptyList();
        }
        return this.summaryQuantileErrors;
    }

    public Long getSummaryMaxAgeSeconds() {
        return this.summaryMaxAgeSeconds;
    }

    public Integer getSummaryNumberOfAgeBuckets() {
        return this.summaryNumberOfAgeBuckets;
    }

    static MetricsProperties load(String prefix, Map<Object, Object> properties) throws PrometheusPropertiesException {
        return new MetricsProperties(Util.loadBoolean(prefix + "." + EXEMPLARS_ENABLED, properties), Util.loadBoolean(prefix + "." + HISTOGRAM_NATIVE_ONLY, properties), Util.loadBoolean(prefix + "." + HISTOGRAM_CLASSIC_ONLY, properties), Util.loadDoubleList(prefix + "." + HISTOGRAM_CLASSIC_UPPER_BOUNDS, properties), Util.loadInteger(prefix + "." + HISTOGRAM_NATIVE_INITIAL_SCHEMA, properties), Util.loadDouble(prefix + "." + HISTOGRAM_NATIVE_MIN_ZERO_THRESHOLD, properties), Util.loadDouble(prefix + "." + HISTOGRAM_NATIVE_MAX_ZERO_THRESHOLD, properties), Util.loadInteger(prefix + "." + HISTOGRAM_NATIVE_MAX_NUMBER_OF_BUCKETS, properties), Util.loadLong(prefix + "." + HISTOGRAM_NATIVE_RESET_DURATION_SECONDS, properties), Util.loadDoubleList(prefix + "." + SUMMARY_QUANTILES, properties), Util.loadDoubleList(prefix + "." + SUMMARY_QUANTILE_ERRORS, properties), Util.loadLong(prefix + "." + SUMMARY_MAX_AGE_SECONDS, properties), Util.loadInteger(prefix + "." + SUMMARY_NUMBER_OF_AGE_BUCKETS, properties), prefix);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Boolean exemplarsEnabled;
        private Boolean histogramNativeOnly;
        private Boolean histogramClassicOnly;
        private List<Double> histogramClassicUpperBounds;
        private Integer histogramNativeInitialSchema;
        private Double histogramNativeMinZeroThreshold;
        private Double histogramNativeMaxZeroThreshold;
        private Integer histogramNativeMaxNumberOfBuckets;
        private Long histogramNativeResetDurationSeconds;
        private List<Double> summaryQuantiles;
        private List<Double> summaryQuantileErrors;
        private Long summaryMaxAgeSeconds;
        private Integer summaryNumberOfAgeBuckets;

        private Builder() {
        }

        public MetricsProperties build() {
            return new MetricsProperties(this.exemplarsEnabled, this.histogramNativeOnly, this.histogramClassicOnly, this.histogramClassicUpperBounds, this.histogramNativeInitialSchema, this.histogramNativeMinZeroThreshold, this.histogramNativeMaxZeroThreshold, this.histogramNativeMaxNumberOfBuckets, this.histogramNativeResetDurationSeconds, this.summaryQuantiles, this.summaryQuantileErrors, this.summaryMaxAgeSeconds, this.summaryNumberOfAgeBuckets);
        }

        public Builder exemplarsEnabled(Boolean exemplarsEnabled) {
            this.exemplarsEnabled = exemplarsEnabled;
            return this;
        }

        public Builder histogramNativeOnly(Boolean histogramNativeOnly) {
            this.histogramNativeOnly = histogramNativeOnly;
            return this;
        }

        public Builder histogramClassicOnly(Boolean histogramClassicOnly) {
            this.histogramClassicOnly = histogramClassicOnly;
            return this;
        }

        public Builder histogramClassicUpperBounds(double ... histogramClassicUpperBounds) {
            this.histogramClassicUpperBounds = Util.toList(histogramClassicUpperBounds);
            return this;
        }

        public Builder histogramNativeInitialSchema(Integer histogramNativeInitialSchema) {
            this.histogramNativeInitialSchema = histogramNativeInitialSchema;
            return this;
        }

        public Builder histogramNativeMinZeroThreshold(Double histogramNativeMinZeroThreshold) {
            this.histogramNativeMinZeroThreshold = histogramNativeMinZeroThreshold;
            return this;
        }

        public Builder histogramNativeMaxZeroThreshold(Double histogramNativeMaxZeroThreshold) {
            this.histogramNativeMaxZeroThreshold = histogramNativeMaxZeroThreshold;
            return this;
        }

        public Builder histogramNativeMaxNumberOfBuckets(Integer histogramNativeMaxNumberOfBuckets) {
            this.histogramNativeMaxNumberOfBuckets = histogramNativeMaxNumberOfBuckets;
            return this;
        }

        public Builder histogramNativeResetDurationSeconds(Long histogramNativeResetDurationSeconds) {
            this.histogramNativeResetDurationSeconds = histogramNativeResetDurationSeconds;
            return this;
        }

        public Builder summaryQuantiles(double ... summaryQuantiles) {
            this.summaryQuantiles = Util.toList(summaryQuantiles);
            return this;
        }

        public Builder summaryQuantileErrors(double ... summaryQuantileErrors) {
            this.summaryQuantileErrors = Util.toList(summaryQuantileErrors);
            return this;
        }

        public Builder summaryMaxAgeSeconds(Long summaryMaxAgeSeconds) {
            this.summaryMaxAgeSeconds = summaryMaxAgeSeconds;
            return this;
        }

        public Builder summaryNumberOfAgeBuckets(Integer summaryNumberOfAgeBuckets) {
            this.summaryNumberOfAgeBuckets = summaryNumberOfAgeBuckets;
            return this;
        }
    }
}

