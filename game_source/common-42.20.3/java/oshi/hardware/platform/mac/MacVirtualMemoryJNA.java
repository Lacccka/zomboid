/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.mac;

import com.sun.jna.Native;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.platform.mac.MacGlobalMemoryJNA;
import oshi.hardware.platform.mac.MacVirtualMemory;
import oshi.jna.ByRef;
import oshi.jna.Struct;
import oshi.jna.platform.mac.SystemB;
import oshi.util.ParseUtil;
import oshi.util.platform.mac.SysctlUtil;
import oshi.util.tuples.Pair;

@ThreadSafe
final class MacVirtualMemoryJNA
extends MacVirtualMemory {
    private static final Logger LOG = LoggerFactory.getLogger(MacVirtualMemoryJNA.class);

    MacVirtualMemoryJNA(MacGlobalMemoryJNA macGlobalMemory) {
        super(macGlobalMemory);
    }

    @Override
    protected Pair<Long, Long> querySwapUsage() {
        long swapUsed = 0L;
        long swapTotal = 0L;
        try (Struct.CloseableXswUsage xswUsage = new Struct.CloseableXswUsage();){
            if (SysctlUtil.sysctl("vm.swapusage", xswUsage)) {
                swapUsed = xswUsage.xsu_used;
                swapTotal = xswUsage.xsu_total;
            }
        }
        return new Pair<Long, Long>(swapUsed, swapTotal);
    }

    @Override
    protected Pair<Long, Long> queryVmStat() {
        long swapPagesIn = 0L;
        long swapPagesOut = 0L;
        try (Struct.CloseableVMStatistics vmStats = new Struct.CloseableVMStatistics();
             ByRef.CloseableIntByReference size = new ByRef.CloseableIntByReference(vmStats.size() / SystemB.INT_SIZE);){
            if (0 == SystemB.INSTANCE.host_statistics(SystemB.INSTANCE.mach_host_self(), 2, vmStats, size)) {
                swapPagesIn = ParseUtil.unsignedIntToLong(vmStats.pageins);
                swapPagesOut = ParseUtil.unsignedIntToLong(vmStats.pageouts);
            } else {
                LOG.error("Failed to get host VM info. Error code: {}", (Object)Native.getLastError());
            }
        }
        return new Pair<Long, Long>(swapPagesIn, swapPagesOut);
    }
}

