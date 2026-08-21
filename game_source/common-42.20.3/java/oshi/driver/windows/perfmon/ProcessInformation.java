/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows.perfmon;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.windows.perfmon.PerfmonDisabled;
import oshi.util.platform.windows.PerfCounterWildcardQuery;
import oshi.util.tuples.Pair;

@ThreadSafe
public final class ProcessInformation {
    private ProcessInformation() {
    }

    public static Pair<List<String>, Map<ProcessPerformanceProperty, List<Long>>> queryProcessCounters() {
        if (PerfmonDisabled.PERF_PROC_DISABLED) {
            return new Pair<List<String>, Map<ProcessPerformanceProperty, List<Long>>>(Collections.emptyList(), Collections.emptyMap());
        }
        return PerfCounterWildcardQuery.queryInstancesAndValues(ProcessPerformanceProperty.class, "Process", "Win32_PerfRawData_PerfProc_Process WHERE NOT Name LIKE \"%_Total\"");
    }

    public static Pair<List<String>, Map<HandleCountProperty, List<Long>>> queryHandles() {
        if (PerfmonDisabled.PERF_PROC_DISABLED) {
            return new Pair<List<String>, Map<HandleCountProperty, List<Long>>>(Collections.emptyList(), Collections.emptyMap());
        }
        return PerfCounterWildcardQuery.queryInstancesAndValues(HandleCountProperty.class, "Process", "Win32_PerfRawData_PerfProc_Process");
    }

    public static Pair<List<String>, Map<IdleProcessorTimeProperty, List<Long>>> queryIdleProcessCounters() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return new Pair<List<String>, Map<IdleProcessorTimeProperty, List<Long>>>(Collections.emptyList(), Collections.emptyMap());
        }
        return PerfCounterWildcardQuery.queryInstancesAndValues(IdleProcessorTimeProperty.class, "Process", "Win32_PerfRawData_PerfProc_Process WHERE IDProcess=0");
    }

    public static enum ProcessPerformanceProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty
    {
        NAME("^*_Total"),
        PRIORITYBASE("Priority Base"),
        ELAPSEDTIME("Elapsed Time"),
        IDPROCESS("ID Process"),
        CREATINGPROCESSID("Creating Process ID"),
        IOREADBYTESPERSEC("IO Read Bytes/sec"),
        IOWRITEBYTESPERSEC("IO Write Bytes/sec"),
        PRIVATEBYTES("Working Set - Private"),
        PAGEFAULTSPERSEC("Page Faults/sec");

        private final String counter;

        private ProcessPerformanceProperty(String counter) {
            this.counter = counter;
        }

        @Override
        public String getCounter() {
            return this.counter;
        }
    }

    public static enum HandleCountProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty
    {
        NAME("_Total"),
        HANDLECOUNT("Handle Count");

        private final String counter;

        private HandleCountProperty(String counter) {
            this.counter = counter;
        }

        @Override
        public String getCounter() {
            return this.counter;
        }
    }

    public static enum IdleProcessorTimeProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty
    {
        NAME("_Total|Idle"),
        PERCENTPROCESSORTIME("% Processor Time"),
        ELAPSEDTIME("Elapsed Time");

        private final String counter;

        private IdleProcessorTimeProperty(String counter) {
            this.counter = counter;
        }

        @Override
        public String getCounter() {
            return this.counter;
        }
    }
}

