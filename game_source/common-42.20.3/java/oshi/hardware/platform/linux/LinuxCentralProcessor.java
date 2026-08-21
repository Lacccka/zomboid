/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.linux;

import com.sun.jna.platform.linux.Udev;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.linux.Lshw;
import oshi.driver.linux.proc.CpuInfo;
import oshi.driver.linux.proc.CpuStat;
import oshi.hardware.CentralProcessor;
import oshi.hardware.common.AbstractCentralProcessor;
import oshi.jna.platform.linux.LinuxLibc;
import oshi.software.os.linux.LinuxOperatingSystem;
import oshi.util.ExecutingCommand;
import oshi.util.FileUtil;
import oshi.util.ParseUtil;
import oshi.util.Util;
import oshi.util.platform.linux.ProcPath;
import oshi.util.platform.linux.SysPath;
import oshi.util.tuples.Quartet;

@ThreadSafe
final class LinuxCentralProcessor
extends AbstractCentralProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(LinuxCentralProcessor.class);

    LinuxCentralProcessor() {
    }

    @Override
    protected CentralProcessor.ProcessorIdentifier queryProcessorId() {
        String cpuVendor = "";
        String cpuName = "";
        String cpuFamily = "";
        String cpuModel = "";
        String cpuStepping = "";
        long cpuFreq = 0L;
        boolean cpu64bit = false;
        StringBuilder armStepping = new StringBuilder();
        String[] flags = new String[]{};
        List<String> cpuInfo = FileUtil.readFile(ProcPath.CPUINFO);
        block25: for (String line : cpuInfo) {
            String[] splitLine = ParseUtil.whitespacesColonWhitespace.split(line);
            if (splitLine.length < 2) {
                if (!line.startsWith("CPU architecture: ")) continue;
                cpuFamily = line.replace("CPU architecture: ", "").trim();
                continue;
            }
            block14 : switch (splitLine[0].toLowerCase(Locale.ROOT)) {
                case "vendor_id": 
                case "cpu implementer": {
                    cpuVendor = splitLine[1];
                    break;
                }
                case "model name": 
                case "processor": {
                    if (splitLine[1].matches("[0-9]+")) break;
                    cpuName = splitLine[1];
                    break;
                }
                case "flags": {
                    for (String flag : flags = splitLine[1].toLowerCase(Locale.ROOT).split(" ")) {
                        if (!"lm".equals(flag)) continue;
                        cpu64bit = true;
                        break block14;
                    }
                    continue block25;
                }
                case "stepping": {
                    cpuStepping = splitLine[1];
                    break;
                }
                case "cpu variant": {
                    if (armStepping.toString().startsWith("r")) break;
                    int rev = ParseUtil.parseLastInt(splitLine[1], 0);
                    armStepping.insert(0, "r" + rev);
                    break;
                }
                case "cpu revision": {
                    if (armStepping.toString().contains("p")) break;
                    armStepping.append('p').append(splitLine[1]);
                    break;
                }
                case "model": 
                case "cpu part": {
                    cpuModel = splitLine[1];
                    break;
                }
                case "cpu family": {
                    cpuFamily = splitLine[1];
                    break;
                }
                case "cpu mhz": {
                    cpuFreq = ParseUtil.parseHertz(splitLine[1]);
                    break;
                }
            }
        }
        if (cpuName.isEmpty()) {
            cpuName = FileUtil.getStringFromFile(ProcPath.MODEL).trim();
        }
        if (cpuName.contains("Hz")) {
            cpuFreq = -1L;
        } else {
            long cpuCapacity = Lshw.queryCpuCapacity();
            if (cpuCapacity > cpuFreq) {
                cpuFreq = cpuCapacity;
            }
        }
        if (cpuStepping.isEmpty()) {
            cpuStepping = armStepping.toString();
        }
        String processorID = LinuxCentralProcessor.getProcessorID(cpuVendor, cpuStepping, cpuModel, cpuFamily, flags);
        if (cpuVendor.startsWith("0x") || cpuModel.isEmpty() || cpuName.isEmpty()) {
            List<String> lscpu = ExecutingCommand.runNative("lscpu");
            for (String line : lscpu) {
                if (line.startsWith("Architecture:") && cpuVendor.startsWith("0x")) {
                    cpuVendor = line.replace("Architecture:", "").trim();
                    continue;
                }
                if (line.startsWith("Vendor ID:")) {
                    cpuVendor = line.replace("Vendor ID:", "").trim();
                    continue;
                }
                if (!line.startsWith("Model name:")) continue;
                String modelName = line.replace("Model name:", "").trim();
                cpuModel = cpuModel.isEmpty() ? modelName : cpuModel;
                cpuName = cpuName.isEmpty() ? modelName : cpuName;
            }
        }
        return new CentralProcessor.ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, processorID, cpu64bit, cpuFreq);
    }

    @Override
    protected Quartet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.PhysicalProcessor>, List<CentralProcessor.ProcessorCache>, List<String>> initProcessorCounts() {
        Quartet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.ProcessorCache>, Map<Integer, Integer>, Map<Integer, String>> topology;
        Quartet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.ProcessorCache>, Map<Integer, Integer>, Map<Integer, String>> quartet = topology = LinuxOperatingSystem.HAS_UDEV ? LinuxCentralProcessor.readTopologyFromUdev() : LinuxCentralProcessor.readTopologyFromSysfs();
        if (topology.getA().isEmpty()) {
            topology = LinuxCentralProcessor.readTopologyFromCpuinfo();
        }
        List<CentralProcessor.LogicalProcessor> logProcs = topology.getA();
        List<CentralProcessor.ProcessorCache> caches = topology.getB();
        Map<Integer, Integer> coreEfficiencyMap = topology.getC();
        Map<Integer, String> modAliasMap = topology.getD();
        if (logProcs.isEmpty()) {
            logProcs.add(new CentralProcessor.LogicalProcessor(0, 0, 0));
        }
        if (coreEfficiencyMap.isEmpty()) {
            coreEfficiencyMap.put(0, 0);
        }
        logProcs.sort(Comparator.comparingInt(CentralProcessor.LogicalProcessor::getProcessorNumber));
        List physProcs = coreEfficiencyMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e -> {
            int pkgId = (Integer)e.getKey() >> 16;
            int coreId = (Integer)e.getKey() & 0xFFFF;
            return new CentralProcessor.PhysicalProcessor(pkgId, coreId, (Integer)e.getValue(), modAliasMap.getOrDefault(e.getKey(), ""));
        }).collect(Collectors.toList());
        List<String> featureFlags = CpuInfo.queryFeatureFlags();
        return new Quartet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.PhysicalProcessor>, List<CentralProcessor.ProcessorCache>, List<String>>(logProcs, physProcs, caches, featureFlags);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Quartet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.ProcessorCache>, Map<Integer, Integer>, Map<Integer, String>> readTopologyFromUdev() {
        ArrayList<CentralProcessor.LogicalProcessor> logProcs = new ArrayList<CentralProcessor.LogicalProcessor>();
        HashSet<CentralProcessor.ProcessorCache> caches = new HashSet<CentralProcessor.ProcessorCache>();
        HashMap<Integer, Integer> coreEfficiencyMap = new HashMap<Integer, Integer>();
        HashMap<Integer, String> modAliasMap = new HashMap<Integer, String>();
        Udev.UdevContext udev = Udev.INSTANCE.udev_new();
        try {
            Udev.UdevEnumerate enumerate = udev.enumerateNew();
            try {
                enumerate.addMatchSubsystem("cpu");
                enumerate.scanDevices();
                for (Udev.UdevListEntry entry = enumerate.getListEntry(); entry != null; entry = entry.getNext()) {
                    String syspath = entry.getName();
                    Udev.UdevDevice device = udev.deviceNewFromSyspath(syspath);
                    String modAlias = null;
                    if (device != null) {
                        try {
                            modAlias = device.getPropertyValue("MODALIAS");
                        }
                        finally {
                            device.unref();
                        }
                    }
                    logProcs.add(LinuxCentralProcessor.getLogicalProcessorFromSyspath(syspath, caches, modAlias, coreEfficiencyMap, modAliasMap));
                }
            }
            finally {
                enumerate.unref();
            }
        }
        finally {
            udev.unref();
        }
        return new Quartet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.ProcessorCache>, Map<Integer, Integer>, Map<Integer, String>>(logProcs, LinuxCentralProcessor.orderedProcCaches(caches), coreEfficiencyMap, modAliasMap);
    }

    private static Quartet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.ProcessorCache>, Map<Integer, Integer>, Map<Integer, String>> readTopologyFromSysfs() {
        ArrayList logProcs = new ArrayList();
        HashSet<CentralProcessor.ProcessorCache> caches = new HashSet<CentralProcessor.ProcessorCache>();
        HashMap coreEfficiencyMap = new HashMap();
        HashMap modAliasMap = new HashMap();
        try (Stream<Path> cpuFiles = Files.find(Paths.get(SysPath.CPU, new String[0]), Integer.MAX_VALUE, (path, basicFileAttributes) -> path.toFile().getName().matches("cpu\\d+"), new FileVisitOption[0]);){
            cpuFiles.forEach(cpu -> {
                String syspath = cpu.toString();
                Map<String, String> uevent = FileUtil.getKeyValueMapFromFile(syspath + "/uevent", "=");
                String modAlias = uevent.get("MODALIAS");
                logProcs.add(LinuxCentralProcessor.getLogicalProcessorFromSyspath(syspath, caches, modAlias, coreEfficiencyMap, modAliasMap));
            });
        }
        catch (IOException e) {
            LOG.warn("Unable to find CPU information in sysfs at path {}", (Object)SysPath.CPU);
        }
        return new Quartet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.ProcessorCache>, Map<Integer, Integer>, Map<Integer, String>>(logProcs, LinuxCentralProcessor.orderedProcCaches(caches), coreEfficiencyMap, modAliasMap);
    }

    private static CentralProcessor.LogicalProcessor getLogicalProcessorFromSyspath(String syspath, Set<CentralProcessor.ProcessorCache> caches, String modAlias, Map<Integer, Integer> coreEfficiencyMap, Map<Integer, String> modAliasMap) {
        int processor = ParseUtil.getFirstIntValue(syspath);
        int coreId = FileUtil.getIntFromFile(syspath + "/topology/core_id");
        int pkgId = FileUtil.getIntFromFile(syspath + "/topology/physical_package_id");
        int pkgCoreKey = (pkgId << 16) + coreId;
        coreEfficiencyMap.put(pkgCoreKey, FileUtil.getIntFromFile(syspath + "/cpu_capacity"));
        if (!Util.isBlank(modAlias)) {
            modAliasMap.put(pkgCoreKey, modAlias);
        }
        int nodeId = 0;
        String nodePrefix = syspath + "/node";
        try (Stream<Path> path2 = Files.list(Paths.get(syspath, new String[0]));){
            Optional<Path> first = path2.filter(p -> p.toString().startsWith(nodePrefix)).findFirst();
            if (first.isPresent()) {
                nodeId = ParseUtil.getFirstIntValue(first.get().getFileName().toString());
            }
        }
        catch (IOException path2) {
            // empty catch block
        }
        String cachePath = syspath + "/cache";
        String indexPrefix = cachePath + "/index";
        try (Stream<Path> path = Files.list(Paths.get(cachePath, new String[0]));){
            path.filter(p -> p.toString().startsWith(indexPrefix)).forEach(c -> {
                int level = FileUtil.getIntFromFile(String.valueOf(c) + "/level");
                CentralProcessor.ProcessorCache.Type type = LinuxCentralProcessor.parseCacheType(FileUtil.getStringFromFile(String.valueOf(c) + "/type"));
                int associativity = FileUtil.getIntFromFile(String.valueOf(c) + "/ways_of_associativity");
                int lineSize = FileUtil.getIntFromFile(String.valueOf(c) + "/coherency_line_size");
                long size = ParseUtil.parseDecimalMemorySizeToBinary(FileUtil.getStringFromFile(String.valueOf(c) + "/size"));
                caches.add(new CentralProcessor.ProcessorCache(level, associativity, lineSize, size, type));
            });
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return new CentralProcessor.LogicalProcessor(processor, coreId, pkgId, nodeId);
    }

    private static CentralProcessor.ProcessorCache.Type parseCacheType(String type) {
        try {
            return CentralProcessor.ProcessorCache.Type.valueOf(type.toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException e) {
            return CentralProcessor.ProcessorCache.Type.UNIFIED;
        }
    }

    private static Quartet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.ProcessorCache>, Map<Integer, Integer>, Map<Integer, String>> readTopologyFromCpuinfo() {
        ArrayList<CentralProcessor.LogicalProcessor> logProcs = new ArrayList<CentralProcessor.LogicalProcessor>();
        Set<CentralProcessor.ProcessorCache> caches = LinuxCentralProcessor.mapCachesFromLscpu();
        Map<Integer, Integer> numaNodeMap = LinuxCentralProcessor.mapNumaNodesFromLscpu();
        HashMap<Integer, Integer> coreEfficiencyMap = new HashMap<Integer, Integer>();
        List<String> procCpu = FileUtil.readFile(ProcPath.CPUINFO);
        int currentProcessor = 0;
        int currentCore = 0;
        int currentPackage = 0;
        boolean first = true;
        for (String cpu : procCpu) {
            if (cpu.startsWith("processor")) {
                if (first) {
                    first = false;
                } else {
                    logProcs.add(new CentralProcessor.LogicalProcessor(currentProcessor, currentCore, currentPackage, numaNodeMap.getOrDefault(currentProcessor, 0)));
                    coreEfficiencyMap.put((currentPackage << 16) + currentCore, 0);
                }
                currentProcessor = ParseUtil.parseLastInt(cpu, 0);
                continue;
            }
            if (cpu.startsWith("core id") || cpu.startsWith("cpu number")) {
                currentCore = ParseUtil.parseLastInt(cpu, 0);
                continue;
            }
            if (!cpu.startsWith("physical id")) continue;
            currentPackage = ParseUtil.parseLastInt(cpu, 0);
        }
        logProcs.add(new CentralProcessor.LogicalProcessor(currentProcessor, currentCore, currentPackage, numaNodeMap.getOrDefault(currentProcessor, 0)));
        coreEfficiencyMap.put((currentPackage << 16) + currentCore, 0);
        return new Quartet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.ProcessorCache>, Map<Integer, Integer>, Map<Integer, String>>(logProcs, LinuxCentralProcessor.orderedProcCaches(caches), coreEfficiencyMap, Collections.emptyMap());
    }

    private static Map<Integer, Integer> mapNumaNodesFromLscpu() {
        HashMap<Integer, Integer> numaNodeMap = new HashMap<Integer, Integer>();
        List<String> lscpu = ExecutingCommand.runNative("lscpu -p=cpu,node");
        for (String line : lscpu) {
            int pos;
            if (line.startsWith("#") || (pos = line.indexOf(44)) <= 0 || pos >= line.length()) continue;
            numaNodeMap.put(ParseUtil.parseIntOrDefault(line.substring(0, pos), 0), ParseUtil.parseIntOrDefault(line.substring(pos + 1), 0));
        }
        return numaNodeMap;
    }

    private static Set<CentralProcessor.ProcessorCache> mapCachesFromLscpu() {
        HashSet<CentralProcessor.ProcessorCache> caches = new HashSet<CentralProcessor.ProcessorCache>();
        int level = 0;
        CentralProcessor.ProcessorCache.Type type = null;
        int associativity = 0;
        int lineSize = 0;
        long size = 0L;
        List<String> lscpu = ExecutingCommand.runNative("lscpu -B -C --json");
        for (String line : lscpu) {
            String[] split;
            String s = line.trim();
            if (s.startsWith("}")) {
                if (level > 0 && type != null) {
                    caches.add(new CentralProcessor.ProcessorCache(level, associativity, lineSize, size, type));
                }
                level = 0;
                type = null;
                associativity = 0;
                lineSize = 0;
                size = 0L;
                continue;
            }
            if (s.contains("one-size")) {
                split = ParseUtil.notDigits.split(s);
                if (split.length <= 1) continue;
                size = ParseUtil.parseLongOrDefault(split[1], 0L);
                continue;
            }
            if (s.contains("ways")) {
                split = ParseUtil.notDigits.split(s);
                if (split.length <= 1) continue;
                associativity = ParseUtil.parseIntOrDefault(split[1], 0);
                continue;
            }
            if (s.contains("type")) {
                split = s.split("\"");
                if (split.length <= 2) continue;
                type = LinuxCentralProcessor.parseCacheType(split[split.length - 2]);
                continue;
            }
            if (s.contains("level")) {
                split = ParseUtil.notDigits.split(s);
                if (split.length <= 1) continue;
                level = ParseUtil.parseIntOrDefault(split[1], 0);
                continue;
            }
            if (!s.contains("coherency-size") || (split = ParseUtil.notDigits.split(s)).length <= 1) continue;
            lineSize = ParseUtil.parseIntOrDefault(split[1], 0);
        }
        return caches;
    }

    @Override
    public long[] querySystemCpuLoadTicks() {
        long[] ticks = CpuStat.getSystemCpuLoadTicks();
        if (LongStream.of(ticks).sum() == 0L) {
            ticks = CpuStat.getSystemCpuLoadTicks();
        }
        long hz = LinuxOperatingSystem.getHz();
        for (int i = 0; i < ticks.length; ++i) {
            ticks[i] = ticks[i] * 1000L / hz;
        }
        return ticks;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long[] queryCurrentFreq() {
        long[] freqs = new long[this.getLogicalProcessorCount()];
        long max = 0L;
        Udev.UdevContext udev = Udev.INSTANCE.udev_new();
        try {
            Udev.UdevEnumerate enumerate = udev.enumerateNew();
            try {
                enumerate.addMatchSubsystem("cpu");
                enumerate.scanDevices();
                for (Udev.UdevListEntry entry = enumerate.getListEntry(); entry != null; entry = entry.getNext()) {
                    String syspath = entry.getName();
                    int cpu = ParseUtil.getFirstIntValue(syspath);
                    if (cpu >= 0 && cpu < freqs.length) {
                        freqs[cpu] = FileUtil.getLongFromFile(syspath + "/cpufreq/scaling_cur_freq");
                        if (freqs[cpu] == 0L) {
                            freqs[cpu] = FileUtil.getLongFromFile(syspath + "/cpufreq/cpuinfo_cur_freq");
                        }
                    }
                    if (max >= freqs[cpu]) continue;
                    max = freqs[cpu];
                }
                if (max > 0L) {
                    int i22 = 0;
                    while (i22 < freqs.length) {
                        int n = i22++;
                        freqs[n] = freqs[n] * 1000L;
                    }
                    long[] i22 = freqs;
                    return i22;
                }
            }
            finally {
                enumerate.unref();
            }
        }
        finally {
            udev.unref();
        }
        Arrays.fill(freqs, -1L);
        List<String> cpuInfo = FileUtil.readFile(ProcPath.CPUINFO);
        int proc = 0;
        for (String s : cpuInfo) {
            if (!s.toLowerCase(Locale.ROOT).contains("cpu mhz")) continue;
            freqs[proc] = Math.round(ParseUtil.parseLastDouble(s, 0.0) * 1000000.0);
            if (++proc < freqs.length) continue;
            break;
        }
        return freqs;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long queryMaxFreq() {
        long policyMax;
        block15: {
            policyMax = -1L;
            Udev.UdevContext udev = Udev.INSTANCE.udev_new();
            try {
                Udev.UdevEnumerate enumerate = udev.enumerateNew();
                try {
                    enumerate.addMatchSubsystem("cpu");
                    enumerate.scanDevices();
                    Udev.UdevListEntry entry = enumerate.getListEntry();
                    if (entry == null) break block15;
                    String syspath = entry.getName();
                    String cpuFreqPath = syspath.substring(0, syspath.lastIndexOf(File.separatorChar)) + "/cpufreq";
                    String policyPrefix = cpuFreqPath + "/policy";
                    try (Stream<Path> path = Files.list(Paths.get(cpuFreqPath, new String[0]));){
                        Optional<Long> maxPolicy = path.filter(p -> p.toString().startsWith(policyPrefix)).map(p -> {
                            long freq = FileUtil.getLongFromFile(p.toString() + "/scaling_max_freq");
                            if (freq == 0L) {
                                freq = FileUtil.getLongFromFile(p.toString() + "/cpuinfo_max_freq");
                            }
                            return freq;
                        }).max(Long::compare);
                        if (maxPolicy.isPresent()) {
                            policyMax = maxPolicy.get() * 1000L;
                        }
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                }
                finally {
                    enumerate.unref();
                }
            }
            finally {
                udev.unref();
            }
        }
        long lshwMax = Lshw.queryCpuCapacity();
        return LongStream.concat(LongStream.of(policyMax, lshwMax), Arrays.stream(this.getCurrentFreq())).max().orElse(-1L);
    }

    @Override
    public double[] getSystemLoadAverage(int nelem) {
        if (nelem < 1 || nelem > 3) {
            throw new IllegalArgumentException("Must include from one to three elements.");
        }
        double[] average = new double[nelem];
        int retval = LinuxLibc.INSTANCE.getloadavg(average, nelem);
        if (retval < nelem) {
            for (int i = Math.max(retval, 0); i < average.length; ++i) {
                average[i] = -1.0;
            }
        }
        return average;
    }

    @Override
    public long[][] queryProcessorCpuLoadTicks() {
        long[][] ticks = CpuStat.getProcessorCpuLoadTicks(this.getLogicalProcessorCount());
        if (LongStream.of(ticks[0]).sum() == 0L) {
            ticks = CpuStat.getProcessorCpuLoadTicks(this.getLogicalProcessorCount());
        }
        long hz = LinuxOperatingSystem.getHz();
        for (int i = 0; i < ticks.length; ++i) {
            for (int j = 0; j < ticks[i].length; ++j) {
                ticks[i][j] = ticks[i][j] * 1000L / hz;
            }
        }
        return ticks;
    }

    private static String getProcessorID(String vendor, String stepping, String model, String family, String[] flags) {
        boolean procInfo = false;
        String marker = "Processor Information";
        for (String checkLine : ExecutingCommand.runNative("dmidecode -t 4")) {
            if (!procInfo && checkLine.contains(marker)) {
                marker = "ID:";
                procInfo = true;
                continue;
            }
            if (!procInfo || !checkLine.contains(marker)) continue;
            return checkLine.split(marker)[1].trim();
        }
        marker = "eax=";
        for (String checkLine : ExecutingCommand.runNative("cpuid -1r")) {
            if (!checkLine.contains(marker) || !checkLine.trim().startsWith("0x00000001")) continue;
            String eax = "";
            String edx = "";
            for (String register : ParseUtil.whitespaces.split(checkLine)) {
                if (register.startsWith("eax=")) {
                    eax = ParseUtil.removeMatchingString(register, "eax=0x");
                    continue;
                }
                if (!register.startsWith("edx=")) continue;
                edx = ParseUtil.removeMatchingString(register, "edx=0x");
            }
            return edx + eax;
        }
        if (vendor.startsWith("0x")) {
            return LinuxCentralProcessor.createMIDR(vendor, stepping, model, family) + "00000000";
        }
        return LinuxCentralProcessor.createProcessorID(stepping, model, family, flags);
    }

    private static String createMIDR(String vendor, String stepping, String model, String family) {
        int midrBytes = 0;
        if (stepping.startsWith("r") && stepping.contains("p")) {
            String[] rev = stepping.substring(1).split("p");
            midrBytes |= ParseUtil.parseLastInt(rev[1], 0);
            midrBytes |= ParseUtil.parseLastInt(rev[0], 0) << 20;
        }
        midrBytes |= ParseUtil.parseLastInt(model, 0) << 4;
        midrBytes |= ParseUtil.parseLastInt(family, 0) << 16;
        return String.format(Locale.ROOT, "%08X", midrBytes |= ParseUtil.parseLastInt(vendor, 0) << 24);
    }

    @Override
    public long queryContextSwitches() {
        return CpuStat.getContextSwitches();
    }

    @Override
    public long queryInterrupts() {
        return CpuStat.getInterrupts();
    }
}

