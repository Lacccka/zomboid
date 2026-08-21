/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.mac;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.IOKit;
import com.sun.jna.platform.mac.IOKitUtil;
import com.sun.jna.platform.mac.SystemB;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.CentralProcessor;
import oshi.hardware.common.AbstractCentralProcessor;
import oshi.jna.ByRef;
import oshi.jna.Struct;
import oshi.util.ExecutingCommand;
import oshi.util.FormatUtil;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;
import oshi.util.Util;
import oshi.util.platform.mac.SysctlUtil;
import oshi.util.tuples.Quartet;

@ThreadSafe
final class MacCentralProcessor
extends AbstractCentralProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MacCentralProcessor.class);
    private static final Set<String> ARM_P_CORES = Stream.of("apple,firestorm arm,v8", "apple,avalanche arm,v8", "apple,everest arm,v8").collect(Collectors.toSet());
    private static final int ARM_CPUTYPE = 0x100000C;
    private static final int M1_CPUFAMILY = 458787763;
    private static final int M2_CPUFAMILY = -634136515;
    private static final int M3_CPUFAMILY = -2023363094;
    private static final long DEFAULT_FREQUENCY = 2400000000L;
    private static final Pattern CPU_N = Pattern.compile("^cpu(\\d+)");
    private final Supplier<String> vendor = Memoizer.memoize(MacCentralProcessor::platformExpert);
    private final boolean isArmCpu = this.isArmCpu();
    private long performanceCoreFrequency = 2400000000L;
    private long efficiencyCoreFrequency = 2400000000L;

    MacCentralProcessor() {
    }

    @Override
    protected CentralProcessor.ProcessorIdentifier queryProcessorId() {
        String processorID;
        String cpuFamily;
        String cpuModel;
        String cpuStepping;
        String cpuVendor;
        String cpuName = SysctlUtil.sysctl("machdep.cpu.brand_string", "");
        if (cpuName.startsWith("Apple")) {
            int family;
            int type;
            cpuVendor = this.vendor.get();
            cpuStepping = "0";
            cpuModel = "0";
            if (this.isArmCpu) {
                type = 0x100000C;
                int mSeries = ParseUtil.getFirstIntValue(cpuName);
                switch (mSeries) {
                    case 2: {
                        family = -634136515;
                        break;
                    }
                    case 3: {
                        family = -2023363094;
                        break;
                    }
                    default: {
                        family = 458787763;
                        break;
                    }
                }
            } else {
                type = SysctlUtil.sysctl("hw.cputype", 0);
                family = SysctlUtil.sysctl("hw.cpufamily", 0);
            }
            cpuFamily = String.format(Locale.ROOT, "0x%08x", family);
            processorID = String.format(Locale.ROOT, "%08x%08x", type, family);
        } else {
            cpuVendor = SysctlUtil.sysctl("machdep.cpu.vendor", "");
            int i = SysctlUtil.sysctl("machdep.cpu.stepping", -1);
            cpuStepping = i < 0 ? "" : Integer.toString(i);
            i = SysctlUtil.sysctl("machdep.cpu.model", -1);
            cpuModel = i < 0 ? "" : Integer.toString(i);
            i = SysctlUtil.sysctl("machdep.cpu.family", -1);
            cpuFamily = i < 0 ? "" : Integer.toString(i);
            long processorIdBits = 0L;
            processorIdBits |= (long)SysctlUtil.sysctl("machdep.cpu.signature", 0);
            processorID = String.format(Locale.ROOT, "%016x", processorIdBits |= (SysctlUtil.sysctl("machdep.cpu.feature_bits", 0L) & 0xFFFFFFFFFFFFFFFFL) << 32);
        }
        if (this.isArmCpu) {
            this.calculateNominalFrequencies();
        }
        long cpuFreq = this.isArmCpu ? this.performanceCoreFrequency : SysctlUtil.sysctl("hw.cpufrequency", 0L);
        boolean cpu64bit = SysctlUtil.sysctl("hw.cpu64bit_capable", 0) != 0;
        return new CentralProcessor.ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, processorID, cpu64bit, cpuFreq);
    }

    @Override
    protected Quartet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.PhysicalProcessor>, List<CentralProcessor.ProcessorCache>, List<String>> initProcessorCounts() {
        int logicalProcessorCount = SysctlUtil.sysctl("hw.logicalcpu", 1);
        int physicalProcessorCount = SysctlUtil.sysctl("hw.physicalcpu", 1);
        int physicalPackageCount = SysctlUtil.sysctl("hw.packages", 1);
        ArrayList<CentralProcessor.LogicalProcessor> logProcs = new ArrayList<CentralProcessor.LogicalProcessor>(logicalProcessorCount);
        HashSet<Integer> pkgCoreKeys = new HashSet<Integer>();
        for (int i = 0; i < logicalProcessorCount; ++i) {
            int coreId = i * physicalProcessorCount / logicalProcessorCount;
            int pkgId = i * physicalPackageCount / logicalProcessorCount;
            logProcs.add(new CentralProcessor.LogicalProcessor(i, coreId, pkgId));
            pkgCoreKeys.add((pkgId << 16) + coreId);
        }
        Map<Integer, String> compatMap = MacCentralProcessor.queryCompatibleStrings();
        int perflevels = SysctlUtil.sysctl("hw.nperflevels", 1, false);
        List physProcs = pkgCoreKeys.stream().sorted().map(k -> {
            String compat = compatMap.getOrDefault(k, "").toLowerCase(Locale.ROOT);
            int efficiency = ARM_P_CORES.contains(compat) ? 1 : 0;
            return new CentralProcessor.PhysicalProcessor(k >> 16, (int)(k & 0xFFFF), efficiency, compat);
        }).collect(Collectors.toList());
        List<CentralProcessor.ProcessorCache> caches = MacCentralProcessor.orderedProcCaches(this.getCacheValues(perflevels));
        List<String> featureFlags = this.getFeatureFlagsFromSysctl();
        return new Quartet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.PhysicalProcessor>, List<CentralProcessor.ProcessorCache>, List<String>>(logProcs, physProcs, caches, featureFlags);
    }

    private Set<CentralProcessor.ProcessorCache> getCacheValues(int perflevels) {
        int linesize = (int)SysctlUtil.sysctl("hw.cachelinesize", 0L);
        int l1associativity = SysctlUtil.sysctl("machdep.cpu.cache.L1_associativity", 0, false);
        int l2associativity = SysctlUtil.sysctl("machdep.cpu.cache.L2_associativity", 0, false);
        HashSet<CentralProcessor.ProcessorCache> caches = new HashSet<CentralProcessor.ProcessorCache>();
        for (int i = 0; i < perflevels; ++i) {
            int size = SysctlUtil.sysctl("hw.perflevel" + i + ".l1icachesize", 0, false);
            if (size > 0) {
                caches.add(new CentralProcessor.ProcessorCache(1, l1associativity, linesize, (long)size, CentralProcessor.ProcessorCache.Type.INSTRUCTION));
            }
            if ((size = SysctlUtil.sysctl("hw.perflevel" + i + ".l1dcachesize", 0, false)) > 0) {
                caches.add(new CentralProcessor.ProcessorCache(1, l1associativity, linesize, (long)size, CentralProcessor.ProcessorCache.Type.DATA));
            }
            if ((size = SysctlUtil.sysctl("hw.perflevel" + i + ".l2cachesize", 0, false)) > 0) {
                caches.add(new CentralProcessor.ProcessorCache(2, l2associativity, linesize, (long)size, CentralProcessor.ProcessorCache.Type.UNIFIED));
            }
            if ((size = SysctlUtil.sysctl("hw.perflevel" + i + ".l3cachesize", 0, false)) <= 0) continue;
            caches.add(new CentralProcessor.ProcessorCache(3, 0, linesize, (long)size, CentralProcessor.ProcessorCache.Type.UNIFIED));
        }
        return caches;
    }

    private List<String> getFeatureFlagsFromSysctl() {
        List x86Features = Arrays.asList("features", "extfeatures", "leaf7_features").stream().map(f -> {
            String key = "machdep.cpu." + f;
            String features = SysctlUtil.sysctl(key, "", false);
            return Util.isBlank(features) ? null : key + ": " + features;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return x86Features.isEmpty() ? ExecutingCommand.runNative("sysctl -a hw.optional") : x86Features;
    }

    @Override
    public long[] querySystemCpuLoadTicks() {
        long[] ticks = new long[CentralProcessor.TickType.values().length];
        int machPort = SystemB.INSTANCE.mach_host_self();
        try (Struct.CloseableHostCpuLoadInfo cpuLoadInfo = new Struct.CloseableHostCpuLoadInfo();
             ByRef.CloseableIntByReference size = new ByRef.CloseableIntByReference(cpuLoadInfo.size());){
            if (0 != SystemB.INSTANCE.host_statistics(machPort, 3, cpuLoadInfo, size)) {
                LOG.error("Failed to get System CPU ticks. Error code: {} ", (Object)Native.getLastError());
                long[] lArray = ticks;
                return lArray;
            }
            ticks[CentralProcessor.TickType.USER.getIndex()] = cpuLoadInfo.cpu_ticks[0];
            ticks[CentralProcessor.TickType.NICE.getIndex()] = cpuLoadInfo.cpu_ticks[3];
            ticks[CentralProcessor.TickType.SYSTEM.getIndex()] = cpuLoadInfo.cpu_ticks[1];
            ticks[CentralProcessor.TickType.IDLE.getIndex()] = cpuLoadInfo.cpu_ticks[2];
        }
        return ticks;
    }

    @Override
    public long[] queryCurrentFreq() {
        if (this.isArmCpu) {
            HashMap physFreqMap = new HashMap();
            this.getPhysicalProcessors().stream().forEach(p -> physFreqMap.put(p.getPhysicalProcessorNumber(), p.getEfficiency() > 0 ? this.performanceCoreFrequency : this.efficiencyCoreFrequency));
            return this.getLogicalProcessors().stream().map(CentralProcessor.LogicalProcessor::getPhysicalProcessorNumber).map(p -> physFreqMap.getOrDefault(p, this.performanceCoreFrequency)).mapToLong(f -> f).toArray();
        }
        return new long[]{this.getProcessorIdentifier().getVendorFreq()};
    }

    @Override
    public long queryMaxFreq() {
        if (this.isArmCpu) {
            return this.performanceCoreFrequency;
        }
        return SysctlUtil.sysctl("hw.cpufrequency_max", this.getProcessorIdentifier().getVendorFreq());
    }

    @Override
    public double[] getSystemLoadAverage(int nelem) {
        if (nelem < 1 || nelem > 3) {
            throw new IllegalArgumentException("Must include from one to three elements.");
        }
        double[] average = new double[nelem];
        int retval = SystemB.INSTANCE.getloadavg(average, nelem);
        if (retval < nelem) {
            Arrays.fill(average, -1.0);
        }
        return average;
    }

    @Override
    public long[][] queryProcessorCpuLoadTicks() {
        long[][] ticks = new long[this.getLogicalProcessorCount()][CentralProcessor.TickType.values().length];
        int machPort = SystemB.INSTANCE.mach_host_self();
        try (ByRef.CloseableIntByReference procCount = new ByRef.CloseableIntByReference();
             ByRef.CloseablePointerByReference procCpuLoadInfo = new ByRef.CloseablePointerByReference();
             ByRef.CloseableIntByReference procInfoCount = new ByRef.CloseableIntByReference();){
            if (0 != SystemB.INSTANCE.host_processor_info(machPort, 2, procCount, procCpuLoadInfo, procInfoCount)) {
                LOG.error("Failed to update CPU Load. Error code: {}", (Object)Native.getLastError());
                long[][] lArray = ticks;
                return lArray;
            }
            int[] cpuTicks = procCpuLoadInfo.getValue().getIntArray(0L, procInfoCount.getValue());
            for (int cpu = 0; cpu < procCount.getValue(); ++cpu) {
                int offset = cpu * 4;
                ticks[cpu][CentralProcessor.TickType.USER.getIndex()] = FormatUtil.getUnsignedInt(cpuTicks[offset + 0]);
                ticks[cpu][CentralProcessor.TickType.NICE.getIndex()] = FormatUtil.getUnsignedInt(cpuTicks[offset + 3]);
                ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] = FormatUtil.getUnsignedInt(cpuTicks[offset + 1]);
                ticks[cpu][CentralProcessor.TickType.IDLE.getIndex()] = FormatUtil.getUnsignedInt(cpuTicks[offset + 2]);
            }
        }
        return ticks;
    }

    @Override
    public long queryContextSwitches() {
        return 0L;
    }

    @Override
    public long queryInterrupts() {
        return 0L;
    }

    private static String platformExpert() {
        String manufacturer = null;
        IOKit.IOService platformExpert = IOKitUtil.getMatchingService("IOPlatformExpertDevice");
        if (platformExpert != null) {
            byte[] data = platformExpert.getByteArrayProperty("manufacturer");
            if (data != null) {
                manufacturer = Native.toString(data, StandardCharsets.UTF_8);
            }
            platformExpert.release();
        }
        return Util.isBlank(manufacturer) ? "Apple Inc." : manufacturer;
    }

    private static Map<Integer, String> queryCompatibleStrings() {
        HashMap<Integer, String> compatibleStrMap = new HashMap<Integer, String>();
        IOKit.IOIterator iter = IOKitUtil.getMatchingServices("IOPlatformDevice");
        if (iter != null) {
            IOKit.IORegistryEntry cpu = iter.next();
            while (cpu != null) {
                Matcher m = CPU_N.matcher(cpu.getName().toLowerCase(Locale.ROOT));
                if (m.matches()) {
                    int procId = ParseUtil.parseIntOrDefault(m.group(1), 0);
                    byte[] data = cpu.getByteArrayProperty("compatible");
                    if (data != null) {
                        compatibleStrMap.put(procId, new String(data, StandardCharsets.UTF_8).replace('\u0000', ' ').trim());
                    }
                }
                cpu.release();
                cpu = iter.next();
            }
            iter.release();
        }
        return compatibleStrMap;
    }

    private boolean isArmCpu() {
        return this.getPhysicalProcessors().stream().map(CentralProcessor.PhysicalProcessor::getIdString).anyMatch(id -> id.contains("arm"));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void calculateNominalFrequencies() {
        IOKit.IOIterator iter = IOKitUtil.getMatchingServices("AppleARMIODevice");
        if (iter != null) {
            try {
                IOKit.IORegistryEntry device = iter.next();
                try {
                    while (true) {
                        if (device != null) {
                            if (device.getName().equalsIgnoreCase("pmgr")) {
                                this.performanceCoreFrequency = this.getMaxFreqFromByteArray(device.getByteArrayProperty("voltage-states5-sram"));
                                this.efficiencyCoreFrequency = this.getMaxFreqFromByteArray(device.getByteArrayProperty("voltage-states1-sram"));
                                return;
                            }
                            device.release();
                            device = iter.next();
                            continue;
                        }
                        break;
                    }
                }
                finally {
                    if (device != null) {
                        device.release();
                    }
                }
            }
            finally {
                iter.release();
            }
        }
    }

    private long getMaxFreqFromByteArray(byte[] data) {
        if (data != null && data.length >= 8) {
            byte[] freqData = Arrays.copyOfRange(data, data.length - 8, data.length - 4);
            return ParseUtil.byteArrayToLong(freqData, 4, false);
        }
        return 2400000000L;
    }
}

