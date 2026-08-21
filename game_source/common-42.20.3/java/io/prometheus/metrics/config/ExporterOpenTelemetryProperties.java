/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.config;

import io.prometheus.metrics.config.PrometheusPropertiesException;
import io.prometheus.metrics.config.Util;
import java.util.HashMap;
import java.util.Map;

public class ExporterOpenTelemetryProperties {
    private static final String PROTOCOL = "protocol";
    private static final String ENDPOINT = "endpoint";
    private static final String HEADERS = "headers";
    private static final String INTERVAL_SECONDS = "intervalSeconds";
    private static final String TIMEOUT_SECONDS = "timeoutSeconds";
    private static final String SERVICE_NAME = "serviceName";
    private static final String SERVICE_NAMESPACE = "serviceNamespace";
    private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
    private static final String SERVICE_VERSION = "serviceVersion";
    private static final String RESOURCE_ATTRIBUTES = "resourceAttributes";
    private static final String PREFIX = "io.prometheus.exporter.opentelemetry";
    private final String protocol;
    private final String endpoint;
    private final Map<String, String> headers;
    private final String interval;
    private final String timeout;
    private final String serviceName;
    private final String serviceNamespace;
    private final String serviceInstanceId;
    private final String serviceVersion;
    private final Map<String, String> resourceAttributes;

    private ExporterOpenTelemetryProperties(String protocol, String endpoint, Map<String, String> headers, String interval, String timeout2, String serviceName, String serviceNamespace, String serviceInstanceId, String serviceVersion, Map<String, String> resourceAttributes) {
        this.protocol = protocol;
        this.endpoint = endpoint;
        this.headers = headers;
        this.interval = interval;
        this.timeout = timeout2;
        this.serviceName = serviceName;
        this.serviceNamespace = serviceNamespace;
        this.serviceInstanceId = serviceInstanceId;
        this.serviceVersion = serviceVersion;
        this.resourceAttributes = resourceAttributes;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public String getInterval() {
        return this.interval;
    }

    public String getTimeout() {
        return this.timeout;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getServiceNamespace() {
        return this.serviceNamespace;
    }

    public String getServiceInstanceId() {
        return this.serviceInstanceId;
    }

    public String getServiceVersion() {
        return this.serviceVersion;
    }

    public Map<String, String> getResourceAttributes() {
        return this.resourceAttributes;
    }

    static ExporterOpenTelemetryProperties load(Map<Object, Object> properties) throws PrometheusPropertiesException {
        String protocol = Util.loadString("io.prometheus.exporter.opentelemetry.protocol", properties);
        String endpoint = Util.loadString("io.prometheus.exporter.opentelemetry.endpoint", properties);
        Map<String, String> headers = Util.loadMap("io.prometheus.exporter.opentelemetry.headers", properties);
        String interval = Util.loadStringAddSuffix("io.prometheus.exporter.opentelemetry.intervalSeconds", properties, "s");
        String timeout2 = Util.loadStringAddSuffix("io.prometheus.exporter.opentelemetry.timeoutSeconds", properties, "s");
        String serviceName = Util.loadString("io.prometheus.exporter.opentelemetry.serviceName", properties);
        String serviceNamespace = Util.loadString("io.prometheus.exporter.opentelemetry.serviceNamespace", properties);
        String serviceInstanceId = Util.loadString("io.prometheus.exporter.opentelemetry.serviceInstanceId", properties);
        String serviceVersion = Util.loadString("io.prometheus.exporter.opentelemetry.serviceVersion", properties);
        Map<String, String> resourceAttributes = Util.loadMap("io.prometheus.exporter.opentelemetry.resourceAttributes", properties);
        return new ExporterOpenTelemetryProperties(protocol, endpoint, headers, interval, timeout2, serviceName, serviceNamespace, serviceInstanceId, serviceVersion, resourceAttributes);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String protocol;
        private String endpoint;
        private final Map<String, String> headers = new HashMap<String, String>();
        private String interval;
        private String timeout;
        private String serviceName;
        private String serviceNamespace;
        private String serviceInstanceId;
        private String serviceVersion;
        private final Map<String, String> resourceAttributes = new HashMap<String, String>();

        private Builder() {
        }

        public Builder protocol(String protocol) {
            if (!protocol.equals("grpc") && !protocol.equals("http/protobuf")) {
                throw new IllegalArgumentException(protocol + ": Unsupported protocol. Expecting grpc or http/protobuf");
            }
            this.protocol = protocol;
            return this;
        }

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public Builder intervalSeconds(int intervalSeconds) {
            if (intervalSeconds <= 0) {
                throw new IllegalArgumentException(intervalSeconds + ": Expecting intervalSeconds > 0");
            }
            this.interval = intervalSeconds + "s";
            return this;
        }

        public Builder timeoutSeconds(int timeoutSeconds) {
            if (timeoutSeconds <= 0) {
                throw new IllegalArgumentException(timeoutSeconds + ": Expecting timeoutSeconds > 0");
            }
            this.timeout = timeoutSeconds + "s";
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder serviceNamespace(String serviceNamespace) {
            this.serviceNamespace = serviceNamespace;
            return this;
        }

        public Builder serviceInstanceId(String serviceInstanceId) {
            this.serviceInstanceId = serviceInstanceId;
            return this;
        }

        public Builder serviceVersion(String serviceVersion) {
            this.serviceVersion = serviceVersion;
            return this;
        }

        public Builder resourceAttribute(String name, String value) {
            this.resourceAttributes.put(name, value);
            return this;
        }

        public ExporterOpenTelemetryProperties build() {
            return new ExporterOpenTelemetryProperties(this.protocol, this.endpoint, this.headers, this.interval, this.timeout, this.serviceName, this.serviceNamespace, this.serviceInstanceId, this.serviceVersion, this.resourceAttributes);
        }
    }
}

