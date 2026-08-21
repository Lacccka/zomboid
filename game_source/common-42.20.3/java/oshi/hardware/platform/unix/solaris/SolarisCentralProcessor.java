/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.unix.solaris;

import com.sun.jna.platform.unix.solaris.LibKstat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.CentralProcessor;
import oshi.hardware.common.AbstractCentralProcessor;
import oshi.jna.platform.unix.SolarisLibc;
import oshi.software.os.unix.solaris.SolarisOperatingSystem;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;
import oshi.util.platform.unix.solaris.KstatUtil;
import oshi.util.tuples.Quartet;

@ThreadSafe
final class SolarisCentralProcessor
extends AbstractCentralProcessor {
    private static final String KSTAT_SYSTEM_CPU = "kstat:/system/cpu/";
    private static final String INFO = "/info";
    private static final String SYS = "/sys";
    private static final String KSTAT_PM_CPU = "kstat:/pm/cpu/";
    private static final String PSTATE = "/pstate";
    private static final String CPU_INFO = "cpu_info";

    SolarisCentralProcessor() {
    }

    @Override
    protected CentralProcessor.ProcessorIdentifier queryProcessorId() {
        boolean cpu64bit = "64".equals(ExecutingCommand.getFirstAnswer("isainfo -b").trim());
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            return SolarisCentralProcessor.queryProcessorId2(cpu64bit);
        }
        String cpuVendor = "";
        String cpuName = "";
        String cpuFamily = "";
        String cpuModel = "";
        String cpuStepping = "";
        long cpuFreq = 0L;
        try (KstatUtil.KstatChain kc = KstatUtil.openChain();){
            LibKstat.Kstat ksp = kc.lookup(CPU_INFO, -1, null);
            if (ksp != null && kc.read(ksp)) {
                cpuVendor = KstatUtil.dataLookupString(ksp, "vendor_id");
                cpuName = KstatUtil.dataLookupString(ksp, "brand");
                cpuFamily = KstatUtil.dataLookupString(ksp, "family");
                cpuModel = KstatUtil.dataLookupString(ksp, "model");
                cpuStepping = KstatUtil.dataLookupString(ksp, "stepping");
                cpuFreq = KstatUtil.dataLookupLong(ksp, "clock_MHz") * 1000000L;
            }
        }
        String processorID = SolarisCentralProcessor.getProcessorID(cpuStepping, cpuModel, cpuFamily);
        return new CentralProcessor.ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, processorID, cpu64bit, cpuFreq);
    }

    private static CentralProcessor.ProcessorIdentifier queryProcessorId2(boolean cpu64bit) {
        Object[] results = KstatUtil.queryKstat2("kstat:/system/cpu/0/info", "vendor_id", "brand", "family", "model", "stepping", "clock_MHz");
        String cpuVendor = results[0] == null ? "" : (String)results[0];
        String cpuName = results[1] == null ? "" : (String)results[1];
        String cpuFamily = results[2] == null ? "" : results[2].toString();
        String cpuModel = results[3] == null ? "" : results[3].toString();
        String cpuStepping = results[4] == null ? "" : results[4].toString();
        long cpuFreq = results[5] == null ? 0L : (Long)results[5];
        String processorID = SolarisCentralProcessor.getProcessorID(cpuStepping, cpuModel, cpuFamily);
        return new CentralProcessor.ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, processorID, cpu64bit, cpuFreq);
    }

    @Override
    protected Quartet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.PhysicalProcessor>, List<CentralProcessor.ProcessorCache>, List<String>> initProcessorCounts() {
        List<CentralProcessor.LogicalProcessor> logProcs;
        Map<Integer, Integer> numaNodeMap = SolarisCentralProcessor.mapNumaNodes();
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            logProcs = SolarisCentralProcessor.initProcessorCounts2(numaNodeMap);
        } else {
            logProcs = new ArrayList<CentralProcessor.LogicalProcessor>();
            try (KstatUtil.KstatChain kc = KstatUtil.openChain();){
                List<LibKstat.Kstat> kstats = kc.lookupAll(CPU_INFO, -1, null);
                for (LibKstat.Kstat ksp : kstats) {
                    if (ksp == null || !kc.read(ksp)) continue;
                    int procId = logProcs.size();
                    String chipId = KstatUtil.dataLookupString(ksp, "chip_id");
                    String coreId = KstatUtil.dataLookupString(ksp, "core_id");
                    CentralProcessor.LogicalProcessor logProc = new CentralProcessor.LogicalProcessor(procId, ParseUtil.parseIntOrDefault(coreId, 0), ParseUtil.parseIntOrDefault(chipId, 0), numaNodeMap.getOrDefault(procId, 0));
                    logProcs.add(logProc);
                }
            }
        }
        if (logProcs.isEmpty()) {
            logProcs.add(new CentralProcessor.LogicalProcessor(0, 0, 0));
        }
        HashMap<Integer, String> dmesg = new HashMap<Integer, String>();
        Pattern p = Pattern.compile(".* cpu(\\\\d+): ((ARM|AMD|Intel).+)");
        for (String s : ExecutingCommand.runNative("dmesg")) {
            Matcher m = p.matcher(s);
            if (!m.matches()) continue;
            int coreId = ParseUtil.parseIntOrDefault(m.group(1), 0);
            dmesg.put(coreId, m.group(2).trim());
        }
        if (dmesg.isEmpty()) {
            return new Quartet(logProcs, null, null, Collections.emptyList());
        }
        List<String> featureFlags = ExecutingCommand.runNative("isainfo -x");
        return new Quartet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.PhysicalProcessor>, Object, List<String>>(logProcs, this.createProcListFromDmesg(logProcs, dmesg), null, featureFlags);
    }

    private static List<CentralProcessor.LogicalProcessor> initProcessorCounts2(Map<Integer, Integer> numaNodeMap) {
        ArrayList<CentralProcessor.LogicalProcessor> logProcs = new ArrayList<CentralProcessor.LogicalProcessor>();
        List<Object[]> results = KstatUtil.queryKstat2List(KSTAT_SYSTEM_CPU, INFO, "chip_id", "core_id");
        for (Object[] result : results) {
            int procId = logProcs.size();
            long chipId = result[0] == null ? 0L : (Long)result[0];
            long coreId = result[1] == null ? 0L : (Long)result[1];
            CentralProcessor.LogicalProcessor logProc = new CentralProcessor.LogicalProcessor(procId, (int)coreId, (int)chipId, numaNodeMap.getOrDefault(procId, 0));
            logProcs.add(logProc);
        }
        if (logProcs.isEmpty()) {
            logProcs.add(new CentralProcessor.LogicalProcessor(0, 0, 0));
        }
        return logProcs;
    }

    private static Map<Integer, Integer> mapNumaNodes() {
        HashMap<Integer, Integer> numaNodeMap = new HashMap<Integer, Integer>();
        int lgroup = 0;
        for (String line : ExecutingCommand.runNative("lgrpinfo -c leaves")) {
            if (line.startsWith("lgroup")) {
                lgroup = ParseUtil.getFirstIntValue(line);
                continue;
            }
            if (!line.contains("CPUs:") && !line.contains("CPU:")) continue;
            for (Integer cpu : ParseUtil.parseHyphenatedIntList(line.split(":")[1])) {
                numaNodeMap.put(cpu, lgroup);
            }
        }
        return numaNodeMap;
    }

    @Override
    public long[] querySystemCpuLoadTicks() {
        long[] ticks = new long[CentralProcessor.TickType.values().length];
        long[][] procTicks = this.getProcessorCpuLoadTicks();
        int i = 0;
        while (i < ticks.length) {
            for (long[] procTick : procTicks) {
                int n = i;
                ticks[n] = ticks[n] + procTick[i];
            }
            int n = i++;
            ticks[n] = ticks[n] / (long)procTicks.length;
        }
        return ticks;
    }

    @Override
    public long[] queryCurrentFreq() {
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            return SolarisCentralProcessor.queryCurrentFreq2(this.getLogicalProcessorCount());
        }
        long[] freqs = new long[this.getLogicalProcessorCount()];
        Arrays.fill(freqs, -1L);
        try (KstatUtil.KstatChain kc = KstatUtil.openChain();){
            for (int i = 0; i < freqs.length; ++i) {
                for (LibKstat.Kstat ksp : kc.lookupAll(CPU_INFO, i, null)) {
                    if (ksp == null || !kc.read(ksp)) continue;
                    freqs[i] = KstatUtil.dataLookupLong(ksp, "current_clock_Hz");
                }
            }
        }
        return freqs;
    }

    private static long[] queryCurrentFreq2(int processorCount) {
        long[] freqs = new long[processorCount];
        Arrays.fill(freqs, -1L);
        List<Object[]> results = KstatUtil.queryKstat2List(KSTAT_SYSTEM_CPU, INFO, "current_clock_Hz");
        int cpu = -1;
        for (Object[] result : results) {
            if (++cpu >= freqs.length) break;
            freqs[cpu] = result[0] == null ? -1L : (Long)result[0];
        }
        return freqs;
    }

    @Override
    public long queryMaxFreq() {
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            return SolarisCentralProcessor.queryMaxFreq2();
        }
        long max = -1L;
        try (KstatUtil.KstatChain kc = KstatUtil.openChain();){
            for (LibKstat.Kstat ksp : kc.lookupAll(CPU_INFO, 0, null)) {
                String suppFreq;
                if (!kc.read(ksp) || (suppFreq = KstatUtil.dataLookupString(ksp, "supported_frequencies_Hz")).isEmpty()) continue;
                for (String s : suppFreq.split(":")) {
                    long freq = ParseUtil.parseLongOrDefault(s, -1L);
                    if (max >= freq) continue;
                    max = freq;
                }
            }
        }
        return max;
    }

    private static long queryMaxFreq2() {
        long max = -1L;
        List<Object[]> results = KstatUtil.queryKstat2List(KSTAT_PM_CPU, PSTATE, "supported_frequencies");
        for (Object[] result : results) {
            for (long freq : result[0] == null ? new long[]{} : (long[])result[0]) {
                if (freq <= max) continue;
                max = freq;
            }
        }
        return max;
    }

    @Override
    public double[] getSystemLoadAverage(int nelem) {
        if (nelem < 1 || nelem > 3) {
            throw new IllegalArgumentException("Must include from one to three elements.");
        }
        double[] average = new double[nelem];
        int retval = SolarisLibc.INSTANCE.getloadavg(average, nelem);
        if (retval < nelem) {
            for (int i = Math.max(retval, 0); i < average.length; ++i) {
                average[i] = -1.0;
            }
        }
        return average;
    }

    @Override
    public long[][] queryProcessorCpuLoadTicks() {
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            return SolarisCentralProcessor.queryProcessorCpuLoadTicks2(this.getLogicalProcessorCount());
        }
        long[][] ticks = new long[this.getLogicalProcessorCount()][CentralProcessor.TickType.values().length];
        int cpu = -1;
        try (KstatUtil.KstatChain kc = KstatUtil.openChain();){
            for (LibKstat.Kstat ksp : kc.lookupAll("cpu", -1, "sys")) {
                if (++cpu >= ticks.length) {
                    break;
                }
                if (!kc.read(ksp)) continue;
                ticks[cpu][CentralProcessor.TickType.IDLE.getIndex()] = KstatUtil.dataLookupLong(ksp, "cpu_ticks_idle");
                ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] = KstatUtil.dataLookupLong(ksp, "cpu_ticks_kernel");
                ticks[cpu][CentralProcessor.TickType.USER.getIndex()] = KstatUtil.dataLookupLong(ksp, "cpu_ticks_user");
            }
        }
        return ticks;
    }

    private static long[][] queryProcessorCpuLoadTicks2(int processorCount) {
        long[][] ticks = new long[processorCount][CentralProcessor.TickType.values().length];
        List<Object[]> results = KstatUtil.queryKstat2List(KSTAT_SYSTEM_CPU, SYS, "cpu_ticks_idle", "cpu_ticks_kernel", "cpu_ticks_user");
        int cpu = -1;
        for (Object[] result : results) {
            if (++cpu >= ticks.length) break;
            ticks[cpu][CentralProcessor.TickType.IDLE.getIndex()] = result[0] == null ? 0L : (Long)result[0];
            ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] = result[1] == null ? 0L : (Long)result[1];
            ticks[cpu][CentralProcessor.TickType.USER.getIndex()] = result[2] == null ? 0L : (Long)result[2];
        }
        return ticks;
    }

    private static String getProcessorID(String stepping, String model, String family) {
        List<String> isainfo = ExecutingCommand.runNative("isainfo -v");
        StringBuilder flags = new StringBuilder();
        for (String line : isainfo) {
            if (line.startsWith("32-bit")) break;
            if (line.startsWith("64-bit")) continue;
            flags.append(' ').append(line.trim());
        }
        return SolarisCentralProcessor.createProcessorID(stepping, model, family, ParseUtil.whitespaces.split(flags.toString().toLowerCase(Locale.ROOT)));
    }

    @Override
    public long queryContextSwitches() {
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            return SolarisCentralProcessor.queryContextSwitches2();
        }
        long swtch = 0L;
        List<String> kstat = ExecutingCommand.runNative("kstat -p cpu_stat:::/pswitch\\\\|inv_swtch/");
        for (String s : kstat) {
            swtch += ParseUtil.parseLastLong(s, 0L);
        }
        return swtch;
    }

    private static long queryContextSwitches2() {
        long swtch = 0L;
        List<Object[]> results = KstatUtil.queryKstat2List(KSTAT_SYSTEM_CPU, SYS, "pswitch", "inv_swtch");
        for (Object[] result : results) {
            swtch += result[0] == null ? 0L : (Long)result[0];
            swtch += result[1] == null ? 0L : (Long)result[1];
        }
        return swtch;
    }

    @Override
    public long queryInterrupts() {
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            return SolarisCentralProcessor.queryInterrupts2();
        }
        long intr = 0L;
        List<String> kstat = ExecutingCommand.runNative("kstat -p cpu_stat:::/intr/");
        for (String s : kstat) {
            intr += ParseUtil.parseLastLong(s, 0L);
        }
        return intr;
    }

    private static long queryInterrupts2() {
        long intr = 0L;
        List<Object[]> results = KstatUtil.queryKstat2List(KSTAT_SYSTEM_CPU, SYS, "intr");
        for (Object[] result : results) {
            intr += result[0] == null ? 0L : (Long)result[0];
        }
        return intr;
    }
}

