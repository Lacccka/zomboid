/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.config;

import io.prometheus.metrics.config.ExemplarsProperties;
import io.prometheus.metrics.config.ExporterFilterProperties;
import io.prometheus.metrics.config.ExporterHttpServerProperties;
import io.prometheus.metrics.config.ExporterOpenTelemetryProperties;
import io.prometheus.metrics.config.ExporterProperties;
import io.prometheus.metrics.config.ExporterPushgatewayProperties;
import io.prometheus.metrics.config.MetricsProperties;
import io.prometheus.metrics.config.PrometheusPropertiesException;
import io.prometheus.metrics.config.PrometheusPropertiesLoader;
import java.util.HashMap;
import java.util.Map;

public class PrometheusProperties {
    private static final PrometheusProperties instance = PrometheusPropertiesLoader.load();
    private final MetricsProperties defaultMetricsProperties;
    private final Map<String, MetricsProperties> metricProperties = new HashMap<String, MetricsProperties>();
    private final ExemplarsProperties exemplarProperties;
    private final ExporterProperties exporterProperties;
    private final ExporterFilterProperties exporterFilterProperties;
    private final ExporterHttpServerProperties exporterHttpServerProperties;
    private final ExporterOpenTelemetryProperties exporterOpenTelemetryProperties;
    private final ExporterPushgatewayProperties exporterPushgatewayProperties;

    public static PrometheusProperties get() throws PrometheusPropertiesException {
        return instance;
    }

    public PrometheusProperties(MetricsProperties defaultMetricsProperties, Map<String, MetricsProperties> metricProperties, ExemplarsProperties exemplarProperties, ExporterProperties exporterProperties, ExporterFilterProperties exporterFilterProperties, ExporterHttpServerProperties httpServerConfig, ExporterPushgatewayProperties pushgatewayProperties, ExporterOpenTelemetryProperties otelConfig) {
        this.defaultMetricsProperties = defaultMetricsProperties;
        this.metricProperties.putAll(metricProperties);
        this.exemplarProperties = exemplarProperties;
        this.exporterProperties = exporterProperties;
        this.exporterFilterProperties = exporterFilterProperties;
        this.exporterHttpServerProperties = httpServerConfig;
        this.exporterPushgatewayProperties = pushgatewayProperties;
        this.exporterOpenTelemetryProperties = otelConfig;
    }

    public MetricsProperties getDefaultMetricProperties() {
        return this.defaultMetricsProperties;
    }

    public MetricsProperties getMetricProperties(String metricName) {
        return this.metricProperties.get(metricName.replace(".", "_"));
    }

    public ExemplarsProperties getExemplarProperties() {
        return this.exemplarProperties;
    }

    public ExporterProperties getExporterProperties() {
        return this.exporterProperties;
    }

    public ExporterFilterProperties getExporterFilterProperties() {
        return this.exporterFilterProperties;
    }

    public ExporterHttpServerProperties getExporterHttpServerProperties() {
        return this.exporterHttpServerProperties;
    }

    public ExporterPushgatewayProperties getExporterPushgatewayProperties() {
        return this.exporterPushgatewayProperties;
    }

    public ExporterOpenTelemetryProperties getExporterOpenTelemetryProperties() {
        return this.exporterOpenTelemetryProperties;
    }
}

