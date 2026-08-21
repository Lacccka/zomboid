/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.config;

import io.prometheus.metrics.config.PrometheusPropertiesException;
import io.prometheus.metrics.config.Util;
import java.util.Map;

public class ExporterHttpServerProperties {
    private static final String PORT = "port";
    private static final String PREFIX = "io.prometheus.exporter.httpServer";
    private final Integer port;

    private ExporterHttpServerProperties(Integer port) {
        this.port = port;
    }

    public Integer getPort() {
        return this.port;
    }

    static ExporterHttpServerProperties load(Map<Object, Object> properties) throws PrometheusPropertiesException {
        Integer port = Util.loadInteger("io.prometheus.exporter.httpServer.port", properties);
        Util.assertValue(port, t -> t > 0, "Expecting value > 0.", PREFIX, PORT);
        return new ExporterHttpServerProperties(port);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer port;

        private Builder() {
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public ExporterHttpServerProperties build() {
            return new ExporterHttpServerProperties(this.port);
        }
    }
}

