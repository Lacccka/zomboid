/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.unix.aix.perfstat;

import com.sun.jna.platform.unix.aix.Perfstat;
import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class PerfstatDisk {
    private static final Perfstat PERF = Perfstat.INSTANCE;

    private PerfstatDisk() {
    }

    public static Perfstat.perfstat_disk_t[] queryDiskStats() {
        Perfstat.perfstat_disk_t[] statp;
        Perfstat.perfstat_id_t firstdiskStats;
        int ret;
        Perfstat.perfstat_disk_t diskStats = new Perfstat.perfstat_disk_t();
        int total = PERF.perfstat_disk(null, null, diskStats.size(), 0);
        if (total > 0 && (ret = PERF.perfstat_disk(firstdiskStats = new Perfstat.perfstat_id_t(), statp = (Perfstat.perfstat_disk_t[])diskStats.toArray(total), diskStats.size(), total)) > 0) {
            return statp;
        }
        return new Perfstat.perfstat_disk_t[0];
    }
}

