/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.linux.proc;

import java.util.List;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.CentralProcessor;
import oshi.util.FileUtil;
import oshi.util.ParseUtil;
import oshi.util.platform.linux.ProcPath;

@ThreadSafe
public final class CpuStat {
    private CpuStat() {
    }

    public static long[] getSystemCpuLoadTicks() {
        long[] ticks = new long[CentralProcessor.TickType.values().length];
        List<String> procStat = FileUtil.readLines(ProcPath.STAT, 1);
        if (procStat.isEmpty()) {
            return ticks;
        }
        String tickStr = procStat.get(0);
        String[] tickArr = ParseUtil.whitespaces.split(tickStr);
        if (tickArr.length <= CentralProcessor.TickType.IDLE.getIndex()) {
            return ticks;
        }
        for (int i = 0; i < CentralProcessor.TickType.values().length; ++i) {
            ticks[i] = ParseUtil.parseLongOrDefault(tickArr[i + 1], 0L);
        }
        return ticks;
    }

    public static long[][] getProcessorCpuLoadTicks(int logicalProcessorCount) {
        long[][] ticks = new long[logicalProcessorCount][CentralProcessor.TickType.values().length];
        int cpu = 0;
        List<String> procStat = FileUtil.readFile(ProcPath.STAT);
        for (String stat : procStat) {
            if (!stat.startsWith("cpu") || stat.startsWith("cpu ")) continue;
            String[] tickArr = ParseUtil.whitespaces.split(stat);
            if (tickArr.length <= CentralProcessor.TickType.IDLE.getIndex()) {
                return ticks;
            }
            for (int i = 0; i < CentralProcessor.TickType.values().length; ++i) {
                ticks[cpu][i] = ParseUtil.parseLongOrDefault(tickArr[i + 1], 0L);
            }
            if (++cpu < logicalProcessorCount) continue;
            break;
        }
        return ticks;
    }

    public static long getContextSwitches() {
        List<String> procStat = FileUtil.readFile(ProcPath.STAT);
        for (String stat : procStat) {
            String[] ctxtArr;
            if (!stat.startsWith("ctxt ") || (ctxtArr = ParseUtil.whitespaces.split(stat)).length != 2) continue;
            return ParseUtil.parseLongOrDefault(ctxtArr[1], 0L);
        }
        return 0L;
    }

    public static long getInterrupts() {
        List<String> procStat = FileUtil.readFile(ProcPath.STAT);
        for (String stat : procStat) {
            String[] intrArr;
            if (!stat.startsWith("intr ") || (intrArr = ParseUtil.whitespaces.split(stat)).length <= 2) continue;
            return ParseUtil.parseLongOrDefault(intrArr[1], 0L);
        }
        return 0L;
    }

    public static long getBootTime() {
        List<String> procStat = FileUtil.readFile(ProcPath.STAT);
        for (String stat : procStat) {
            if (!stat.startsWith("btime")) continue;
            String[] bTime = ParseUtil.whitespaces.split(stat);
            return ParseUtil.parseLongOrDefault(bTime[1], 0L);
        }
        return 0L;
    }
}

