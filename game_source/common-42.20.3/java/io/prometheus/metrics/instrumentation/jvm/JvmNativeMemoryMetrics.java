/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.GaugeWithCallback;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Unit;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

public class JvmNativeMemoryMetrics {
    private static final String JVM_NATIVE_MEMORY_RESERVED_BYTES = "jvm_native_memory_reserved_bytes";
    private static final String JVM_NATIVE_MEMORY_COMMITTED_BYTES = "jvm_native_memory_committed_bytes";
    private static final Pattern pattern = Pattern.compile("\\s*([A-Z][A-Za-z\\s]*[A-Za-z]+).*reserved=(\\d+), committed=(\\d+)");
    static final AtomicBoolean isEnabled = new AtomicBoolean(true);
    private final PrometheusProperties config;
    private final PlatformMBeanServerAdapter adapter;

    private JvmNativeMemoryMetrics(PrometheusProperties config, PlatformMBeanServerAdapter adapter) {
        this.config = config;
        this.adapter = adapter;
    }

    private void register(PrometheusRegistry registry) {
        this.vmNativeMemorySummaryInBytesOrEmpty();
        if (isEnabled.get()) {
            ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(JVM_NATIVE_MEMORY_RESERVED_BYTES)).help("Reserved bytes of a given JVM. Reserved memory represents the total amount of memory the JVM can potentially use.")).unit(Unit.BYTES)).labelNames("pool")).callback(this.makeCallback(true)).register(registry);
            ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(JVM_NATIVE_MEMORY_COMMITTED_BYTES)).help("Committed bytes of a given JVM. Committed memory represents the amount of memory the JVM is using right now.")).unit(Unit.BYTES)).labelNames("pool")).callback(this.makeCallback(false)).register(registry);
        }
    }

    private Consumer<GaugeWithCallback.Callback> makeCallback(Boolean reserved) {
        return callback -> {
            String summary = this.vmNativeMemorySummaryInBytesOrEmpty();
            if (!summary.isEmpty()) {
                Matcher matcher = pattern.matcher(summary);
                while (matcher.find()) {
                    String category = matcher.group(1);
                    long value = reserved != false ? Long.parseLong(matcher.group(2)) : Long.parseLong(matcher.group(3));
                    callback.call(value, category);
                }
            }
        };
    }

    private String vmNativeMemorySummaryInBytesOrEmpty() {
        if (!isEnabled.get()) {
            return "";
        }
        try {
            String summary = this.adapter.vmNativeMemorySummaryInBytes();
            if (summary.isEmpty() || summary.trim().contains("Native memory tracking is not enabled")) {
                isEnabled.set(false);
                return "";
            }
            return summary;
        }
        catch (Exception ex) {
            isEnabled.set(false);
            return "";
        }
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties config) {
        return new Builder(config);
    }

    static interface PlatformMBeanServerAdapter {
        public String vmNativeMemorySummaryInBytes();
    }

    public static class Builder {
        private final PrometheusProperties config;
        private final PlatformMBeanServerAdapter adapter;

        private Builder(PrometheusProperties config) {
            this(config, new DefaultPlatformMBeanServerAdapter());
        }

        Builder(PrometheusProperties config, PlatformMBeanServerAdapter adapter) {
            this.config = config;
            this.adapter = adapter;
        }

        public void register() {
            this.register(PrometheusRegistry.defaultRegistry);
        }

        public void register(PrometheusRegistry registry) {
            new JvmNativeMemoryMetrics(this.config, this.adapter).register(registry);
        }
    }

    static class DefaultPlatformMBeanServerAdapter
    implements PlatformMBeanServerAdapter {
        DefaultPlatformMBeanServerAdapter() {
        }

        @Override
        public String vmNativeMemorySummaryInBytes() {
            try {
                return (String)ManagementFactory.getPlatformMBeanServer().invoke(new ObjectName("com.sun.management:type=DiagnosticCommand"), "vmNativeMemory", new Object[]{new String[]{"summary", "scale=B"}}, new String[]{"[Ljava.lang.String;"});
            }
            catch (InstanceNotFoundException | MBeanException | MalformedObjectNameException | ReflectionException e) {
                throw new IllegalStateException("Native memory tracking is not enabled", e);
            }
        }
    }
}

