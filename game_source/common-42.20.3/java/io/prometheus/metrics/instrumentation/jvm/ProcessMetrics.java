/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.CounterWithCallback;
import io.prometheus.metrics.core.metrics.GaugeWithCallback;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Unit;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;

public class ProcessMetrics {
    private static final String PROCESS_CPU_SECONDS_TOTAL = "process_cpu_seconds_total";
    private static final String PROCESS_START_TIME_SECONDS = "process_start_time_seconds";
    private static final String PROCESS_OPEN_FDS = "process_open_fds";
    private static final String PROCESS_MAX_FDS = "process_max_fds";
    private static final String PROCESS_VIRTUAL_MEMORY_BYTES = "process_virtual_memory_bytes";
    private static final String PROCESS_RESIDENT_MEMORY_BYTES = "process_resident_memory_bytes";
    static final File PROC_SELF_STATUS = new File("/proc/self/status");
    private final PrometheusProperties config;
    private final OperatingSystemMXBean osBean;
    private final RuntimeMXBean runtimeBean;
    private final Grepper grepper;
    private final boolean linux;

    private ProcessMetrics(OperatingSystemMXBean osBean, RuntimeMXBean runtimeBean, Grepper grepper, PrometheusProperties config) {
        this.osBean = osBean;
        this.runtimeBean = runtimeBean;
        this.grepper = grepper;
        this.config = config;
        this.linux = PROC_SELF_STATUS.canRead();
    }

    private void register(PrometheusRegistry registry) {
        ((CounterWithCallback.Builder)((CounterWithCallback.Builder)CounterWithCallback.builder(this.config).name(PROCESS_CPU_SECONDS_TOTAL).help("Total user and system CPU time spent in seconds.")).unit(Unit.SECONDS)).callback(callback -> {
            try {
                Long processCpuTime = this.callLongGetter("getProcessCpuTime", (Object)this.osBean);
                if (processCpuTime != null) {
                    callback.call(Unit.nanosToSeconds(processCpuTime), new String[0]);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }).register(registry);
        ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(PROCESS_START_TIME_SECONDS)).help("Start time of the process since unix epoch in seconds.")).unit(Unit.SECONDS)).callback(callback -> callback.call(Unit.millisToSeconds(this.runtimeBean.getStartTime()), new String[0])).register(registry);
        ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(PROCESS_OPEN_FDS)).help("Number of open file descriptors.")).callback(callback -> {
            try {
                Long openFds = this.callLongGetter("getOpenFileDescriptorCount", (Object)this.osBean);
                if (openFds != null) {
                    callback.call(openFds.longValue(), new String[0]);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }).register(registry);
        ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(PROCESS_MAX_FDS)).help("Maximum number of open file descriptors.")).callback(callback -> {
            try {
                Long maxFds = this.callLongGetter("getMaxFileDescriptorCount", (Object)this.osBean);
                if (maxFds != null) {
                    callback.call(maxFds.longValue(), new String[0]);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }).register(registry);
        if (this.linux) {
            ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(PROCESS_VIRTUAL_MEMORY_BYTES)).help("Virtual memory size in bytes.")).unit(Unit.BYTES)).callback(callback -> {
                try {
                    String line = this.grepper.lineStartingWith(PROC_SELF_STATUS, "VmSize:");
                    callback.call(Unit.kiloBytesToBytes(Double.parseDouble(line.split("\\s+")[1])), new String[0]);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }).register(registry);
            ((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)((GaugeWithCallback.Builder)GaugeWithCallback.builder(this.config).name(PROCESS_RESIDENT_MEMORY_BYTES)).help("Resident memory size in bytes.")).unit(Unit.BYTES)).callback(callback -> {
                try {
                    String line = this.grepper.lineStartingWith(PROC_SELF_STATUS, "VmRSS:");
                    callback.call(Unit.kiloBytesToBytes(Double.parseDouble(line.split("\\s+")[1])), new String[0]);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }).register(registry);
        }
    }

    private Long callLongGetter(String getterName, Object obj) throws NoSuchMethodException, InvocationTargetException {
        return this.callLongGetter(obj.getClass().getMethod(getterName, new Class[0]), obj);
    }

    private Long callLongGetter(Method method, Object obj) throws InvocationTargetException {
        try {
            return (Long)method.invoke(obj, new Object[0]);
        }
        catch (IllegalAccessException illegalAccessException) {
            for (Class<?> clazz : method.getDeclaringClass().getInterfaces()) {
                try {
                    Method interfaceMethod = clazz.getMethod(method.getName(), method.getParameterTypes());
                    Long result = this.callLongGetter(interfaceMethod, obj);
                    if (result == null) continue;
                    return result;
                }
                catch (NoSuchMethodException noSuchMethodException) {
                    // empty catch block
                }
            }
            return null;
        }
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties config) {
        return new Builder(config);
    }

    static interface Grepper {
        public String lineStartingWith(File var1, String var2) throws IOException;
    }

    public static class Builder {
        private final PrometheusProperties config;
        private OperatingSystemMXBean osBean;
        private RuntimeMXBean runtimeBean;
        private Grepper grepper;

        private Builder(PrometheusProperties config) {
            this.config = config;
        }

        Builder osBean(OperatingSystemMXBean osBean) {
            this.osBean = osBean;
            return this;
        }

        Builder runtimeBean(RuntimeMXBean runtimeBean) {
            this.runtimeBean = runtimeBean;
            return this;
        }

        Builder grepper(Grepper grepper) {
            this.grepper = grepper;
            return this;
        }

        public void register() {
            this.register(PrometheusRegistry.defaultRegistry);
        }

        public void register(PrometheusRegistry registry) {
            OperatingSystemMXBean osBean = this.osBean != null ? this.osBean : ManagementFactory.getOperatingSystemMXBean();
            RuntimeMXBean bean = this.runtimeBean != null ? this.runtimeBean : ManagementFactory.getRuntimeMXBean();
            Grepper grepper = this.grepper != null ? this.grepper : new FileGrepper();
            new ProcessMetrics(osBean, bean, grepper, this.config).register(registry);
        }
    }

    private static class FileGrepper
    implements Grepper {
        private FileGrepper() {
        }

        @Override
        public String lineStartingWith(File file, String prefix) throws IOException {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath(), new OpenOption[0]), StandardCharsets.UTF_8));){
                String line = reader.readLine();
                while (line != null) {
                    if (line.startsWith(prefix)) {
                        String string = line;
                        return string;
                    }
                    line = reader.readLine();
                }
            }
            return null;
        }
    }
}

