/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.instrumentation.jvm.JvmBufferPoolMetrics;
import io.prometheus.metrics.instrumentation.jvm.JvmClassLoadingMetrics;
import io.prometheus.metrics.instrumentation.jvm.JvmCompilationMetrics;
import io.prometheus.metrics.instrumentation.jvm.JvmGarbageCollectorMetrics;
import io.prometheus.metrics.instrumentation.jvm.JvmMemoryMetrics;
import io.prometheus.metrics.instrumentation.jvm.JvmMemoryPoolAllocationMetrics;
import io.prometheus.metrics.instrumentation.jvm.JvmNativeMemoryMetrics;
import io.prometheus.metrics.instrumentation.jvm.JvmRuntimeInfoMetric;
import io.prometheus.metrics.instrumentation.jvm.JvmThreadsMetrics;
import io.prometheus.metrics.instrumentation.jvm.ProcessMetrics;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JvmMetrics {
    private static final Set<PrometheusRegistry> REGISTERED = ConcurrentHashMap.newKeySet();

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties config) {
        return new Builder(config);
    }

    public static class Builder {
        private final PrometheusProperties config;

        private Builder(PrometheusProperties config) {
            this.config = config;
        }

        public void register() {
            this.register(PrometheusRegistry.defaultRegistry);
        }

        public void register(PrometheusRegistry registry) {
            if (REGISTERED.add(registry)) {
                JvmThreadsMetrics.builder(this.config).register(registry);
                JvmBufferPoolMetrics.builder(this.config).register(registry);
                JvmClassLoadingMetrics.builder(this.config).register(registry);
                JvmCompilationMetrics.builder(this.config).register(registry);
                JvmGarbageCollectorMetrics.builder(this.config).register(registry);
                JvmMemoryPoolAllocationMetrics.builder(this.config).register(registry);
                JvmMemoryMetrics.builder(this.config).register(registry);
                JvmNativeMemoryMetrics.builder(this.config).register(registry);
                JvmRuntimeInfoMetric.builder(this.config).register(registry);
                ProcessMetrics.builder(this.config).register(registry);
            }
        }
    }
}

