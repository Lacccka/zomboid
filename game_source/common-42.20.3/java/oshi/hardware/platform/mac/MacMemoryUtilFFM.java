/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.mac;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.ffm.ForeignFunctions;
import oshi.ffm.mac.MacSystem;
import oshi.ffm.mac.MacSystemFunctions;

final class MacMemoryUtilFFM {
    private static final Logger LOG = LoggerFactory.getLogger(MacMemoryUtilFFM.class);

    private MacMemoryUtilFFM() {
    }

    static boolean callVmStat(Arena arena, MemorySegment vmStats) throws Throwable {
        MemorySegment count = arena.allocate(ValueLayout.JAVA_INT);
        MemorySegment callState = arena.allocate(ForeignFunctions.CAPTURED_STATE_LAYOUT);
        count.set(ValueLayout.JAVA_INT, 0L, (int)(MacSystem.VM_STATISTICS.byteSize() / ValueLayout.JAVA_INT.byteSize()));
        int result = MacSystemFunctions.host_statistics(callState, MacSystemFunctions.mach_host_self(), 2, vmStats, count);
        if (result != 0) {
            LOG.error("Failed to get host VM info. Error code: {}", (Object)ForeignFunctions.getErrno(callState));
            return false;
        }
        return true;
    }
}

