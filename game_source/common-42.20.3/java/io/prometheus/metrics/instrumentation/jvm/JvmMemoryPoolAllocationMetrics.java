/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.instrumentation.jvm;

import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.datapoints.CounterDataPoint;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

public class JvmMemoryPoolAllocationMetrics {
    private static final String JVM_MEMORY_POOL_ALLOCATED_BYTES_TOTAL = "jvm_memory_pool_allocated_bytes_total";
    private final List<GarbageCollectorMXBean> garbageCollectorBeans;

    private JvmMemoryPoolAllocationMetrics(List<GarbageCollectorMXBean> garbageCollectorBeans) {
        this.garbageCollectorBeans = garbageCollectorBeans;
    }

    private void register(PrometheusRegistry registry) {
        Counter allocatedCounter = (Counter)((Counter.Builder)((Counter.Builder)Counter.builder().name(JVM_MEMORY_POOL_ALLOCATED_BYTES_TOTAL).help("Total bytes allocated in a given JVM memory pool. Only updated after GC, not continuously.")).labelNames("pool")).register(registry);
        AllocationCountingNotificationListener listener = new AllocationCountingNotificationListener(allocatedCounter);
        for (GarbageCollectorMXBean bean : this.garbageCollectorBeans) {
            if (!(bean instanceof NotificationEmitter)) continue;
            ((NotificationEmitter)((Object)bean)).addNotificationListener(listener, null, null);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(PrometheusProperties config) {
        return new Builder();
    }

    static class AllocationCountingNotificationListener
    implements NotificationListener {
        private final Map<String, Long> lastMemoryUsage = new HashMap<String, Long>();
        private final Counter counter;

        AllocationCountingNotificationListener(Counter counter) {
            this.counter = counter;
        }

        @Override
        public synchronized void handleNotification(Notification notification, Object handback) {
            GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData)notification.getUserData());
            GcInfo gcInfo = info.getGcInfo();
            Map<String, MemoryUsage> memoryUsageBeforeGc = gcInfo.getMemoryUsageBeforeGc();
            Map<String, MemoryUsage> memoryUsageAfterGc = gcInfo.getMemoryUsageAfterGc();
            for (Map.Entry<String, MemoryUsage> entry : memoryUsageBeforeGc.entrySet()) {
                String memoryPool = entry.getKey();
                long before = entry.getValue().getUsed();
                long after = memoryUsageAfterGc.get(memoryPool).getUsed();
                this.handleMemoryPool(memoryPool, before, after);
            }
        }

        void handleMemoryPool(String memoryPool, long before, long after) {
            long increase;
            long last = AllocationCountingNotificationListener.getAndSet(this.lastMemoryUsage, memoryPool, after);
            long diff1 = before - last;
            long diff2 = after - before;
            if (diff1 < 0L) {
                diff1 = 0L;
            }
            if (diff2 < 0L) {
                diff2 = 0L;
            }
            if ((increase = diff1 + diff2) > 0L) {
                ((CounterDataPoint)this.counter.labelValues(new String[]{memoryPool})).inc(increase);
            }
        }

        private static long getAndSet(Map<String, Long> map, String key, long value) {
            Long last = map.put(key, value);
            return last == null ? 0L : last;
        }
    }

    public static class Builder {
        private List<GarbageCollectorMXBean> garbageCollectorBeans;

        private Builder() {
        }

        Builder withGarbageCollectorBeans(List<GarbageCollectorMXBean> garbageCollectorBeans) {
            this.garbageCollectorBeans = garbageCollectorBeans;
            return this;
        }

        public void register() {
            this.register(PrometheusRegistry.defaultRegistry);
        }

        public void register(PrometheusRegistry registry) {
            List<GarbageCollectorMXBean> garbageCollectorBeans = this.garbageCollectorBeans;
            if (garbageCollectorBeans == null) {
                garbageCollectorBeans = ManagementFactory.getGarbageCollectorMXBeans();
            }
            new JvmMemoryPoolAllocationMetrics(garbageCollectorBeans).register(registry);
        }
    }
}

