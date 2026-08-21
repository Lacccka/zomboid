/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.expositionformats;

import io.prometheus.metrics.config.ExporterProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.expositionformats.ExpositionFormatWriter;
import io.prometheus.metrics.expositionformats.OpenMetricsTextFormatWriter;
import io.prometheus.metrics.expositionformats.PrometheusProtobufWriter;
import io.prometheus.metrics.expositionformats.PrometheusTextFormatWriter;

public class ExpositionFormats {
    private final PrometheusProtobufWriter prometheusProtobufWriter;
    private final PrometheusTextFormatWriter prometheusTextFormatWriter;
    private final OpenMetricsTextFormatWriter openMetricsTextFormatWriter;

    private ExpositionFormats(PrometheusProtobufWriter prometheusProtobufWriter, PrometheusTextFormatWriter prometheusTextFormatWriter, OpenMetricsTextFormatWriter openMetricsTextFormatWriter) {
        this.prometheusProtobufWriter = prometheusProtobufWriter;
        this.prometheusTextFormatWriter = prometheusTextFormatWriter;
        this.openMetricsTextFormatWriter = openMetricsTextFormatWriter;
    }

    public static ExpositionFormats init() {
        return ExpositionFormats.init(PrometheusProperties.get().getExporterProperties());
    }

    public static ExpositionFormats init(ExporterProperties properties) {
        return new ExpositionFormats(new PrometheusProtobufWriter(), PrometheusTextFormatWriter.builder().setIncludeCreatedTimestamps(properties.getIncludeCreatedTimestamps()).setTimestampsInMs(properties.getPrometheusTimestampsInMs()).build(), OpenMetricsTextFormatWriter.builder().setCreatedTimestampsEnabled(properties.getIncludeCreatedTimestamps()).setExemplarsOnAllMetricTypesEnabled(properties.getExemplarsOnAllMetricTypes()).build());
    }

    public ExpositionFormatWriter findWriter(String acceptHeader) {
        if (this.prometheusProtobufWriter.accepts(acceptHeader)) {
            return this.prometheusProtobufWriter;
        }
        if (this.openMetricsTextFormatWriter.accepts(acceptHeader)) {
            return this.openMetricsTextFormatWriter;
        }
        return this.prometheusTextFormatWriter;
    }

    public PrometheusProtobufWriter getPrometheusProtobufWriter() {
        return this.prometheusProtobufWriter;
    }

    public PrometheusTextFormatWriter getPrometheusTextFormatWriter() {
        return this.prometheusTextFormatWriter;
    }

    public OpenMetricsTextFormatWriter getOpenMetricsTextFormatWriter() {
        return this.openMetricsTextFormatWriter;
    }
}

