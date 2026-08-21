/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.mac;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.SystemB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.VirtualMemory;
import oshi.hardware.platform.mac.MacGlobalMemory;
import oshi.hardware.platform.mac.MacVirtualMemoryJNA;
import oshi.jna.ByRef;
import oshi.jna.Struct;
import oshi.util.platform.mac.SysctlUtil;

@ThreadSafe
final class MacGlobalMemoryJNA
extends MacGlobalMemory {
    private static final Logger LOG = LoggerFactory.getLogger(MacGlobalMemoryJNA.class);

    MacGlobalMemoryJNA() {
    }

    @Override
    protected long queryVmStats() {
        try (Struct.CloseableVMStatistics vmStats = new Struct.CloseableVMStatistics();){
            ByRef.CloseableIntByReference size;
            block12: {
                size = new ByRef.CloseableIntByReference(vmStats.size() / SystemB.INT_SIZE);
                try {
                    if (0 == SystemB.INSTANCE.host_statistics(SystemB.INSTANCE.mach_host_self(), 2, vmStats, size)) break block12;
                    LOG.error("Failed to get host VM info. Error code: {}", (Object)Native.getLastError());
                    long l = 0L;
                    size.close();
                    return l;
                }
                catch (Throwable throwable) {
                    try {
                        size.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                    throw throwable;
                }
            }
            long l = (long)(vmStats.free_count + vmStats.inactive_count) * this.getPageSize();
            size.close();
            return l;
        }
    }

    @Override
    protected long sysctl(String name, long defaultValue) {
        return SysctlUtil.sysctl(name, defaultValue);
    }

    @Override
    protected long host_page_size() {
        try (ByRef.CloseableLongByReference pPageSize = new ByRef.CloseableLongByReference();){
            if (0 == SystemB.INSTANCE.host_page_size(SystemB.INSTANCE.mach_host_self(), pPageSize)) {
                long l = pPageSize.getValue();
                return l;
            }
        }
        return -1L;
    }

    @Override
    protected VirtualMemory createVirtualMemory() {
        return new MacVirtualMemoryJNA(this);
    }
}

