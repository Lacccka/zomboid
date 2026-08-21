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
public final class PagingFile {
    private PagingFile() {
    }

    public static Map<PagingPercentProperty, Long> querySwapUsed() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return Collections.emptyMap();
        }
        return PerfCounterQuery.queryValues(PagingPercentProperty.class, "Paging File", "Win32_PerfRawData_PerfOS_PagingFile");
    }

    public static enum PagingPercentProperty implements PerfCounterQuery.PdhCounterProperty
    {
        PERCENTUSAGE("_Total", "% Usage");

        private final String instance;
        private final String counter;

        private PagingPercentProperty(String instance, String counter) {
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

