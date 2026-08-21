/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.unix.aix.perfstat;

import com.sun.jna.platform.unix.aix.Perfstat;
import java.util.Arrays;
import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class PerfstatProcess {
    private static final Perfstat PERF = Perfstat.INSTANCE;

    private PerfstatProcess() {
    }

    public static Perfstat.perfstat_process_t[] queryProcesses() {
        Perfstat.perfstat_process_t[] proct;
        Perfstat.perfstat_id_t firstprocess;
        int ret;
        Perfstat.perfstat_process_t process = new Perfstat.perfstat_process_t();
        int procCount = PERF.perfstat_process(null, null, process.size(), 0);
        if (procCount > 0 && (ret = PERF.perfstat_process(firstprocess = new Perfstat.perfstat_id_t(), proct = (Perfstat.perfstat_process_t[])process.toArray(procCount), process.size(), procCount)) > 0) {
            return Arrays.copyOf(proct, ret);
        }
        return new Perfstat.perfstat_process_t[0];
    }
}

