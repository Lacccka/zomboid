/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.GaugeWithCallback;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Unit;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class JvmMemoryMetrics {
    private static final String JVM_MEMORY_OBJECTS_PENDING_FINALIZATION = "jvm_memory_objects_pending_finalization";
    private static final String JVM_MEMORY_USED_BYTES = "jvm_memory_used_bytes";
    private static final String JVM_MEMORY_COMMITTED_BYTES = "jvm_memory_committed_bytes";
    private static final String JVM_MEMORY_MAX_BYTES = "jvm_memory_max_bytes";
    private static final String JVM_MEMORY_INIT_BYTES = "jvm_memory_init_bytes";
    private static final String JVM_MEMORY_POOL_USED_BYTES = "jvm_memory_pool_used_bytes";
    private static final String JVM_MEMORY_POOL_COMMITTED_BYTES = "jvm_memory_pool_committed_bytes";
    private static final String JVM_MEMORY_POOL_MAX_BYTES = "jvm_memory_pool_max_bytes";
    private static final String JVM_MEMORY_POOL_INIT_BYTES = "jvm_memory_pool_init_bytes";
    private static final String JVM_MEMORY_POOL_COLLECTION_USED_BYTES = "jvm_memory_pool_collection_used_bytes";
    private static final String JVM_MEMORY_POOL_COLLECTION_COMMITTED_BYTES = "jvm_memory_pool_collection_committed_bytes";
    private static final String JVM_MEMORY_POOL_COLLECTION_MAX_BYTES = "jvm_memory_pool_collection_max_bytes";
    private static final String JVM_MEMORY_POOL_COLLECTION_INIT_BYTES = "jvm_memory_pool_collection_init_bytes";
    private final PrometheusProperties config;
    private final MemoryMXBean memoryBean;
    private final List<MemoryPoolMXBean> poolBeans;

    private JvmMemoryMetrics(List<MemoryPoolMXBean> poolBeans, MemoryMXBean memoryBean, PrometheusProperties config) {
        this.config = config;
        this.poolBeans = poolBeans;
        this.memoryBean = memoryBean;
    }

    private void register(PrometheusRegistry registry) {
        ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(JVM_MEMORY_OBJECTS_PENDING_FINALIZATION)).help("The number of objects waiting in the finalizer queue.")).callback(callback -> callback.call(this.memoryBean.getObjectPendingFinalizationCount(), new String[0])).register(registry);
        ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(JVM_MEMORY_USED_BYTES)).help("Used bytes of a given JVM memory area.")).unit(Unit.BYTES)).labelNames("area")).callback(callback -> {
            callback.call(this.memoryBean.getHeapMemoryUsage().getUsed(), "heap");
            callback.call(this.memoryBean.getNonHeapMemoryUsage().getUsed(), "nonheap");
        }).register(registry);
        ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(JVM_MEMORY_COMMITTED_BYTES)).help("Committed (bytes) of a given JVM memory area.")).unit(Unit.BYTES)).labelNames("area")).callback(callback -> {
            callback.call(this.memoryBean.getHeapMemoryUsage().getCommitted(), "heap");
            callback.call(this.memoryBean.getNonHeapMemoryUsage().getCommitted(), "nonheap");
        }).register(registry);
        ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(JVM_MEMORY_MAX_BYTES)).help("Max (bytes) of a given JVM memory area.")).unit(Unit.BYTES)).labelNames("area")).callback(callback -> {
            callback.call(this.memoryBean.getHeapMemoryUsage().getMax(), "heap");
            callback.call(this.memoryBean.getNonHeapMemoryUsage().getMax(), "nonheap");
        }).register(registry);
        ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(JVM_MEMORY_INIT_BYTES)).help("Initial bytes of a given JVM memory area.")).unit(Unit.BYTES)).labelNames("area")).callback(callback -> {
            callback.call(this.memoryBean.getHeapMemoryUsage().getInit(), "heap");
            callback.call(this.memoryBean.getNonHeapMemoryUsage().getInit(), "nonheap");
        }).register(registry);
        ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(JVM_MEMORY_POOL_USED_BYTES)).help("Used bytes of a given JVM memory pool.")).unit(Unit.BYTES)).labelNames("pool")).callback(this.makeCallback(this.poolBeans, MemoryPoolMXBean::getUsage, MemoryUsage::getUsed)).register(registry);
        ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(JVM_MEMORY_POOL_COMMITTED_BYTES)).help("Committed bytes of a given JVM memory pool.")).unit(Unit.BYTES)).labelNames("pool")).callback(this.makeCallback(this.poolBeans, MemoryPoolMXBean::getUsage, MemoryUsage::getCommitted)).register(registry);
        ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(JVM_MEMORY_POOL_MAX_BYTES)).help("Max bytes of a given JVM memory pool.")).unit(Unit.BYTES)).labelNames("pool")).callback(this.makeCallback(this.poolBeans, MemoryPoolMXBean::getUsage, MemoryUsage::getMax)).register(registry);
        ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(JVM_MEMORY_POOL_INIT_BYTES)).help("Initial bytes of a given JVM memory pool.")).unit(Unit.BYTES)).labelNames("pool")).callback(this.makeCallback(this.poolBeans, MemoryPoolMXBean::getUsage, MemoryUsage::getInit)).register(registry);
        ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(JVM_MEMORY_POOL_COLLECTION_USED_BYTES)).help("Used bytes after last collection of a given JVM memory pool.")).unit(Unit.BYTES)).labelNames("pool")).callback(this.makeCallback(this.poolBeans, MemoryPoolMXBean::getCollectionUsage, MemoryUsage::getUsed)).register(registry);
        ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(JVM_MEMORY_POOL_COLLECTION_COMMITTED_BYTES)).help("Committed after last collection bytes of a given JVM memory pool.")).unit(Unit.BYTES)).labelNames("pool")).callback(this.makeCallback(this.poolBeans, MemoryPoolMXBean::getCollectionUsage, MemoryUsage::getCommitted)).register(registry);
        ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(JVM_MEMORY_POOL_COLLECTION_MAX_BYTES)).help("Max bytes after last collection of a given JVM memory pool.")).unit(Unit.BYTES)).labelNames("pool")).callback(this.makeCallback(this.poolBeans, MemoryPoolMXBean::getCollectionUsage, MemoryUsage::getMax)).register(registry);
        ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(JVM_MEMORY_POOL_COLLECTION_INIT_BYTES)).help("Initial after last collection bytes of a given JVM memory pool.")).unit(Unit.BYTES)).labelNames("pool")).callback(this.makeCallback(this.poolBeans, MemoryPoolMXBean::getCollectionUsage, MemoryUsage::getInit)).register(registry);
    }

    private Consumer<GaugeWithCallback.Callback> makeCallback(List<MemoryPoolMXBean> poolBeans, Function<MemoryPoolMXBean, MemoryUsage> memoryUsageFunc, Function<MemoryUsage, Long> valueFunc) {
        return callback -> {
            for (MemoryPoolMXBean pool : poolBeans) {
                MemoryUsage poolUsage = (MemoryUsage)memoryUsageFunc.apply(pool);
                if (poolUsage == null) continue;
                callback.call(((Long)valueFunc.apply(poolUsage)).longValue(), pool.getName());
            }
        };
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties config) {
        return new Builder(config);
    }

    public static class Builder {
        private final PrometheusProperties config;
        private MemoryMXBean memoryBean;
        private List<MemoryPoolMXBean> poolBeans;

        private Builder(PrometheusProperties config) {
            this.config = config;
        }

        Builder withMemoryBean(MemoryMXBean memoryBean) {
            this.memoryBean = memoryBean;
            return this;
        }

        Builder withMemoryPoolBeans(List<MemoryPoolMXBean> memoryPoolBeans) {
            this.poolBeans = memoryPoolBeans;
            return this;
        }

        public void register() {
            this.register(PrometheusRegistry.defaultRegistry);
        }

        public void register(PrometheusRegistry registry) {
            MemoryMXBean bean = this.memoryBean != null ? this.memoryBean : ManagementFactory.getMemoryMXBean();
            List<MemoryPoolMXBean> poolBeans = this.poolBeans != null ? this.poolBeans : ManagementFactory.getMemoryPoolMXBeans();
            new JvmMemoryMetrics(poolBeans, bean, this.config).register(registry);
        }
    }
}

