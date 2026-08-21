/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows.perfmon;

import java.util.Collections;
import java.util.Map;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.windows.perfmon.PerfmonDisabled;
import oshi.util.platform.windows.PerfCounterQuery;

@ThreadSafe
public final class SystemInformation {
    private SystemInformation() {
    }

    public static Map<ContextSwitchProperty, Long> queryContextSwitchCounters() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return Collections.emptyMap();
        }
        return PerfCounterQuery.queryValues(ContextSwitchProperty.class, "System", "Win32_PerfRawData_PerfOS_System");
    }

    public static Map<ProcessorQueueLengthProperty, Long> queryProcessorQueueLength() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return Collections.emptyMap();
        }
        return PerfCounterQuery.queryValues(ProcessorQueueLengthProperty.class, "System", "Win32_PerfRawData_PerfOS_System");
    }

    public static enum ContextSwitchProperty implements PerfCounterQuery.PdhCounterProperty
    {
        CONTEXTSWITCHESPERSEC(null, "Context Switches/sec");

        private final String instance;
        private final String counter;

        private ContextSwitchProperty(String instance, String counter) {
            this.instance = instance;
            this.counter = counter;
        }

        @Override
        public String getInstance() {
            return this.instance;
        }

        @Override
        public String getCounter() {
            return this.counter;
        }
    }

    public static enum ProcessorQueueLengthProperty implements PerfCounterQuery.PdhCounterProperty
    {
        PROCESSORQUEUELENGTH(null, "Processor Queue Length");

        private final String instance;
        private final String counter;

        private ProcessorQueueLengthProperty(String instance, String counter) {
            this.instance = instance;
            this.counter = counter;
        }

        @Override
        public String getInstance() {
            return this.instance;
        }

        @Override
        public String getCounter() {
            return this.counter;
        }
    }
}

