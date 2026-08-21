/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows.perfmon;

import com.sun.jna.platform.win32.VersionHelpers;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.windows.perfmon.PerfmonDisabled;
import oshi.util.platform.windows.PerfCounterQuery;
import oshi.util.platform.windows.PerfCounterWildcardQuery;
import oshi.util.tuples.Pair;

@ThreadSafe
public final class ProcessorInformation {
    private static final boolean IS_WIN7_OR_GREATER = VersionHelpers.IsWindows7OrGreater();

    private ProcessorInformation() {
    }

    public static Pair<List<String>, Map<ProcessorTickCountProperty, List<Long>>> queryProcessorCounters() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return new Pair<List<String>, Map<ProcessorTickCountProperty, List<Long>>>(Collections.emptyList(), Collections.emptyMap());
        }
        return IS_WIN7_OR_GREATER ? PerfCounterWildcardQuery.queryInstancesAndValues(ProcessorTickCountProperty.class, "Processor Information", "Win32_PerfRawData_Counters_ProcessorInformation WHERE NOT Name LIKE \"%_Total\"") : PerfCounterWildcardQuery.queryInstancesAndValues(ProcessorTickCountProperty.class, "Processor", "Win32_PerfRawData_PerfOS_Processor WHERE Name!=\"_Total\"");
    }

    public static Map<SystemTickCountProperty, Long> querySystemCounters() {
        return PerfCounterQuery.queryValues(SystemTickCountProperty.class, "Processor", "Win32_PerfRawData_PerfOS_Processor WHERE Name=\"_Total\"");
    }

    public static Pair<List<String>, Map<ProcessorUtilityTickCountProperty, List<Long>>> queryProcessorCapacityCounters() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return new Pair<List<String>, Map<ProcessorUtilityTickCountProperty, List<Long>>>(Collections.emptyList(), Collections.emptyMap());
        }
        return PerfCounterWildcardQuery.queryInstancesAndValues(ProcessorUtilityTickCountProperty.class, "Processor Information", "Win32_PerfRawData_Counters_ProcessorInformation WHERE NOT Name LIKE \"%_Total\"");
    }

    public static Map<InterruptsProperty, Long> queryInterruptCounters() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return Collections.emptyMap();
        }
        return PerfCounterQuery.queryValues(InterruptsProperty.class, "Processor", "Win32_PerfRawData_PerfOS_Processor WHERE Name=\"_Total\"");
    }

    public static Pair<List<String>, Map<ProcessorFrequencyProperty, List<Long>>> queryFrequencyCounters() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return new Pair<List<String>, Map<ProcessorFrequencyProperty, List<Long>>>(Collections.emptyList(), Collections.emptyMap());
        }
        return PerfCounterWildcardQuery.queryInstancesAndValues(ProcessorFrequencyProperty.class, "Processor Information", "Win32_PerfRawData_Counters_ProcessorInformation WHERE NOT Name LIKE \"%_Total\"");
    }

    public static enum ProcessorTickCountProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty
    {
        NAME("^*_Total"),
        PERCENTDPCTIME("% DPC Time"),
        PERCENTINTERRUPTTIME("% Interrupt Time"),
        PERCENTPRIVILEGEDTIME("% Privileged Time"),
        PERCENTPROCESSORTIME("% Processor Time"),
        PERCENTUSERTIME("% User Time");

        private final String counter;

        private ProcessorTickCountProperty(String counter) {
            this.counter = counter;
        }

        @Override
        public String getCounter() {
            return this.counter;
        }
    }

    public static enum SystemTickCountProperty implements PerfCounterQuery.PdhCounterProperty
    {
        PERCENTDPCTIME("_Total", "% DPC Time"),
        PERCENTINTERRUPTTIME("_Total", "% Interrupt Time");

        private final String instance;
        private final String counter;

        private SystemTickCountProperty(String instance, String counter) {
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

    public static enum ProcessorUtilityTickCountProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty
    {
        NAME("^*_Total"),
        PERCENTDPCTIME("% DPC Time"),
        PERCENTINTERRUPTTIME("% Interrupt Time"),
        PERCENTPRIVILEGEDTIME("% Privileged Time"),
        PERCENTPROCESSORTIME("% Processor Time"),
        TIMESTAMP_SYS100NS("% Processor Time_Base"),
        PERCENTPRIVILEGEDUTILITY("% Privileged Utility"),
        PERCENTPROCESSORUTILITY("% Processor Utility"),
        PERCENTPROCESSORUTILITY_BASE("% Processor Utility_Base"),
        PERCENTUSERTIME("% User Time");

        private final String counter;

        private ProcessorUtilityTickCountProperty(String counter) {
            this.counter = counter;
        }

        @Override
        public String getCounter() {
            return this.counter;
        }
    }

    public static enum InterruptsProperty implements PerfCounterQuery.PdhCounterProperty
    {
        INTERRUPTSPERSEC("_Total", "Interrupts/sec");

        private final String instance;
        private final String counter;

        private InterruptsProperty(String instance, String counter) {
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

    public static enum ProcessorFrequencyProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty
    {
        NAME("^*_Total"),
        PERCENTOFMAXIMUMFREQUENCY("% of Maximum Frequency");

        private final String counter;

        private ProcessorFrequencyProperty(String counter) {
            this.counter = counter;
        }

        @Override
        public String getCounter() {
            return this.counter;
        }
    }
}

