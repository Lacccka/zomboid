/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.unix.aix;

import com.sun.jna.platform.unix.aix.Perfstat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.unix.aix.perfstat.PerfstatMemory;
import oshi.hardware.PhysicalMemory;
import oshi.hardware.VirtualMemory;
import oshi.hardware.common.AbstractGlobalMemory;
import oshi.hardware.platform.unix.aix.AixVirtualMemory;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;

@ThreadSafe
final class AixGlobalMemory
extends AbstractGlobalMemory {
    private final Supplier<Perfstat.perfstat_memory_total_t> perfstatMem = Memoizer.memoize(AixGlobalMemory::queryPerfstat, Memoizer.defaultExpiration());
    private final Supplier<List<String>> lscfg;
    private static final long PAGESIZE = 4096L;
    private final Supplier<VirtualMemory> vm = Memoizer.memoize(this::createVirtualMemory);

    AixGlobalMemory(Supplier<List<String>> lscfg) {
        this.lscfg = lscfg;
    }

    @Override
    public long getAvailable() {
        return this.perfstatMem.get().real_avail * 4096L;
    }

    @Override
    public long getTotal() {
        return this.perfstatMem.get().real_total * 4096L;
    }

    @Override
    public long getPageSize() {
        return 4096L;
    }

    @Override
    public VirtualMemory getVirtualMemory() {
        return this.vm.get();
    }

    @Override
    public List<PhysicalMemory> getPhysicalMemory() {
        ArrayList<PhysicalMemory> pmList = new ArrayList<PhysicalMemory>();
        boolean isMemModule = false;
        boolean isMemoryDIMM = false;
        String bankLabel = "unknown";
        Object locator = "";
        String partNumber = "unknown";
        long capacity = 0L;
        for (String line : this.lscfg.get()) {
            String s = line.trim();
            if (s.endsWith("memory-module")) {
                isMemModule = true;
                continue;
            }
            if (s.startsWith("Memory DIMM")) {
                isMemoryDIMM = true;
                continue;
            }
            if (isMemModule) {
                if (s.startsWith("Node:")) {
                    bankLabel = s.substring(5).trim();
                    if (!bankLabel.startsWith("IBM,")) continue;
                    bankLabel = bankLabel.substring(4);
                    continue;
                }
                if (s.startsWith("Physical Location:")) {
                    locator = "/" + s.substring(18).trim();
                    continue;
                }
                if (s.startsWith("Size")) {
                    capacity = ParseUtil.parseLongOrDefault(ParseUtil.removeLeadingDots(s.substring(4).trim()), 0L) << 20;
                    continue;
                }
                if (!s.startsWith("Hardware Location Code")) continue;
                if (capacity > 0L) {
                    pmList.add(new PhysicalMemory(bankLabel + (String)locator, capacity, 0L, "IBM", "unknown", "unknown", "unknown"));
                }
                bankLabel = "unknown";
                locator = "";
                capacity = 0L;
                isMemModule = false;
                continue;
            }
            if (!isMemoryDIMM) continue;
            if (s.startsWith("Hardware Location Code")) {
                locator = ParseUtil.removeLeadingDots(s.substring(23).trim());
                continue;
            }
            if (s.startsWith("Size")) {
                capacity = ParseUtil.parseLongOrDefault(ParseUtil.removeLeadingDots(s.substring(4).trim()), 0L) << 20;
                continue;
            }
            if (s.startsWith("Part Number") || s.startsWith("FRU Number")) {
                partNumber = ParseUtil.removeLeadingDots(s.substring(11).trim());
                continue;
            }
            if (!s.startsWith("Physical Location:")) continue;
            if (capacity > 0L) {
                pmList.add(new PhysicalMemory((String)locator, capacity, 0L, "IBM", "unknown", partNumber, "unknown"));
            }
            partNumber = "unknown";
            locator = "";
            capacity = 0L;
            isMemoryDIMM = false;
        }
        return pmList;
    }

    private static Perfstat.perfstat_memory_total_t queryPerfstat() {
        return PerfstatMemory.queryMemoryTotal();
    }

    private VirtualMemory createVirtualMemory() {
        return new AixVirtualMemory(this.perfstatMem);
    }
}

