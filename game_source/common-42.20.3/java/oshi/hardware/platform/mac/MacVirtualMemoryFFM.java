/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.mac;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.ffm.mac.MacSystem;
import oshi.hardware.platform.mac.MacGlobalMemoryFFM;
import oshi.hardware.platform.mac.MacMemoryUtilFFM;
import oshi.hardware.platform.mac.MacVirtualMemory;
import oshi.util.ParseUtil;
import oshi.util.platform.mac.SysctlUtilFFM;
import oshi.util.tuples.Pair;

@ThreadSafe
final class MacVirtualMemoryFFM
extends MacVirtualMemory {
    private static final Logger LOG = LoggerFactory.getLogger(MacVirtualMemoryFFM.class);

    MacVirtualMemoryFFM(MacGlobalMemoryFFM macGlobalMemory) {
        super(macGlobalMemory);
    }

    @Override
    protected Pair<Long, Long> querySwapUsage() {
        long swapUsed = 0L;
        long swapTotal = 0L;
        try (Arena arena = Arena.ofConfined();){
            MemorySegment xswUsage = arena.allocate(MacSystem.XSW_USAGE);
            if (SysctlUtilFFM.sysctl("vm.swapusage", xswUsage)) {
                swapUsed = xswUsage.get(ValueLayout.JAVA_LONG, MacSystem.XSW_USAGE.byteOffset(MacSystem.XSW_USAGE_USED));
                swapTotal = xswUsage.get(ValueLayout.JAVA_LONG, MacSystem.XSW_USAGE.byteOffset(MacSystem.XSW_USAGE_TOTAL));
            }
        }
        return new Pair<Long, Long>(swapUsed, swapTotal);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    protected Pair<Long, Long> queryVmStat() {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment vmStats = arena.allocate(MacSystem.VM_STATISTICS);
            if (!MacMemoryUtilFFM.callVmStat(arena, vmStats)) return new Pair<Long, Long>(0L, 0L);
            long swapPagesIn = ParseUtil.unsignedIntToLong(vmStats.get(ValueLayout.JAVA_INT, MacSystem.VM_STATISTICS.byteOffset(MacSystem.VM_PAGEINS)));
            long swapPagesOut = ParseUtil.unsignedIntToLong(vmStats.get(ValueLayout.JAVA_INT, MacSystem.VM_STATISTICS.byteOffset(MacSystem.VM_PAGEOUTS)));
            Pair<Long, Long> pair = new Pair<Long, Long>(swapPagesIn, swapPagesOut);
            return pair;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return new Pair<Long, Long>(0L, 0L);
    }
}

