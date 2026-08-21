/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.config;

import io.prometheus.metrics.config.PrometheusPropertiesException;
import io.prometheus.metrics.config.Util;
import java.util.Map;

public class ExporterPushgatewayProperties {
    private static final String ADDRESS = "address";
    private static final String JOB = "job";
    private static final String SCHEME = "scheme";
    private static final String PREFIX = "io.prometheus.exporter.pushgateway";
    private final String scheme;
    private final String address;
    private final String job;

    private ExporterPushgatewayProperties(String address, String job, String scheme) {
        this.address = address;
        this.job = job;
        this.scheme = scheme;
    }

    public String getAddress() {
        return this.address;
    }

    public String getJob() {
        return this.job;
    }

    public String getScheme() {
        return this.scheme;
    }

    static ExporterPushgatewayProperties load(Map<Object, Object> properties) throws PrometheusPropertiesException {
        String address = Util.loadString("io.prometheus.exporter.pushgateway.address", properties);
        String job = Util.loadString("io.prometheus.exporter.pushgateway.job", properties);
        String scheme = Util.loadString("io.prometheus.exporter.pushgateway.scheme", properties);
        if (scheme != null && !scheme.equals("http") && !scheme.equals("https")) {
            throw new PrometheusPropertiesException(String.format("%s.%s: Illegal value. Expecting 'http' or 'https'. Found: %s", PREFIX, SCHEME, scheme));
        }
        return new ExporterPushgatewayProperties(address, job, scheme);
    }
}

