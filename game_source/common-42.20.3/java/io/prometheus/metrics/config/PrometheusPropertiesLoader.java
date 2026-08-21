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
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.config.PrometheusPropertiesException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrometheusPropertiesLoader {
    public static PrometheusProperties load() throws PrometheusPropertiesException {
        return PrometheusPropertiesLoader.load(new Properties());
    }

    public static PrometheusProperties load(Map<Object, Object> externalProperties) throws PrometheusPropertiesException {
        Map<Object, Object> properties = PrometheusPropertiesLoader.loadProperties(externalProperties);
        Map<String, MetricsProperties> metricsConfigs = PrometheusPropertiesLoader.loadMetricsConfigs(properties);
        MetricsProperties defaultMetricsProperties = MetricsProperties.load("io.prometheus.metrics", properties);
        ExemplarsProperties exemplarConfig = ExemplarsProperties.load(properties);
        ExporterProperties exporterProperties = ExporterProperties.load(properties);
        ExporterFilterProperties exporterFilterProperties = ExporterFilterProperties.load(properties);
        ExporterHttpServerProperties exporterHttpServerProperties = ExporterHttpServerProperties.load(properties);
        ExporterPushgatewayProperties exporterPushgatewayProperties = ExporterPushgatewayProperties.load(properties);
        ExporterOpenTelemetryProperties exporterOpenTelemetryProperties = ExporterOpenTelemetryProperties.load(properties);
        PrometheusPropertiesLoader.validateAllPropertiesProcessed(properties);
        return new PrometheusProperties(defaultMetricsProperties, metricsConfigs, exemplarConfig, exporterProperties, exporterFilterProperties, exporterHttpServerProperties, exporterPushgatewayProperties, exporterOpenTelemetryProperties);
    }

    private static Map<String, MetricsProperties> loadMetricsConfigs(Map<Object, Object> properties) {
        HashMap<String, MetricsProperties> result = new HashMap<String, MetricsProperties>();
        Pattern pattern = Pattern.compile("io\\.prometheus\\.metrics\\.([^.]+)\\.");
        HashSet<String> propertyNames = new HashSet<String>();
        for (Object key : properties.keySet()) {
            propertyNames.add(key.toString());
        }
        for (String propertyName : propertyNames) {
            String metricName;
            Matcher matcher = pattern.matcher(propertyName);
            if (!matcher.find() || result.containsKey(metricName = matcher.group(1).replace(".", "_"))) continue;
            result.put(metricName, MetricsProperties.load("io.prometheus.metrics." + metricName, properties));
        }
        return result;
    }

    private static void validateAllPropertiesProcessed(Map<Object, Object> properties) {
        for (Object key : properties.keySet()) {
            if (!key.toString().startsWith("io.prometheus")) continue;
            throw new PrometheusPropertiesException(key + ": Unknown property");
        }
    }

    private static Map<Object, Object> loadProperties(Map<Object, Object> externalProperties) {
        HashMap<Object, Object> properties = new HashMap<Object, Object>();
        properties.putAll(PrometheusPropertiesLoader.loadPropertiesFromClasspath());
        properties.putAll(PrometheusPropertiesLoader.loadPropertiesFromFile());
        System.getProperties().stringPropertyNames().stream().filter(key -> key.startsWith("io.prometheus")).forEach(key -> properties.put(key, System.getProperty(key)));
        properties.putAll(externalProperties);
        return properties;
    }

    private static Properties loadPropertiesFromClasspath() {
        Properties properties = new Properties();
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("prometheus.properties");){
            properties.load(stream);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return properties;
    }

    private static Properties loadPropertiesFromFile() throws PrometheusPropertiesException {
        Properties properties = new Properties();
        String path = System.getProperty("prometheus.config");
        if (System.getenv("PROMETHEUS_CONFIG") != null) {
            path = System.getenv("PROMETHEUS_CONFIG");
        }
        if (path != null) {
            try (InputStream stream = Files.newInputStream(Paths.get(path, new String[0]), new OpenOption[0]);){
                properties.load(stream);
            }
            catch (IOException e) {
                throw new PrometheusPropertiesException("Failed to read Prometheus properties from " + path + ": " + e.getMessage(), e);
            }
        }
        return properties;
    }
}

