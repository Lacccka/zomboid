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
import oshi.ffm.mac.MacSystemFunctions;
import oshi.hardware.VirtualMemory;
import oshi.hardware.platform.mac.MacGlobalMemory;
import oshi.hardware.platform.mac.MacMemoryUtilFFM;
import oshi.hardware.platform.mac.MacVirtualMemoryFFM;
import oshi.util.platform.mac.SysctlUtilFFM;

@ThreadSafe
final class MacGlobalMemoryFFM
extends MacGlobalMemory {
    private static final Logger LOG = LoggerFactory.getLogger(MacGlobalMemoryFFM.class);

    MacGlobalMemoryFFM() {
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    protected long queryVmStats() {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment vmStats = arena.allocate(MacSystem.VM_STATISTICS);
            if (!MacMemoryUtilFFM.callVmStat(arena, vmStats)) return 0L;
            int freeCount = vmStats.get(ValueLayout.JAVA_INT, MacSystem.VM_STATISTICS.byteOffset(MacSystem.VM_FREE_COUNT));
            int inactiveCount = vmStats.get(ValueLayout.JAVA_INT, MacSystem.VM_STATISTICS.byteOffset(MacSystem.VM_INACTIVE_COUNT));
            long l = (long)(freeCount + inactiveCount) * this.getPageSize();
            return l;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return 0L;
    }

    @Override
    protected long sysctl(String name, long defaultValue) {
        return SysctlUtilFFM.sysctl(name, defaultValue);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    protected long host_page_size() {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment pageSize = arena.allocate(ValueLayout.JAVA_LONG);
            int result = MacSystemFunctions.host_page_size(MacSystemFunctions.mach_host_self(), pageSize);
            if (result != 0) return -1L;
            long l = pageSize.get(ValueLayout.JAVA_LONG, 0L);
            return l;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return -1L;
    }

    @Override
    protected VirtualMemory createVirtualMemory() {
        return new MacVirtualMemoryFFM(this);
    }
}

