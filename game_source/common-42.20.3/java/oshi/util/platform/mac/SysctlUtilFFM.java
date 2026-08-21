/*
 * Decompiled with CFR 0.152.
 */
package oshi.util.platform.mac;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.ffm.ForeignFunctions;
import oshi.ffm.mac.MacSystemFunctions;

@ThreadSafe
public final class SysctlUtilFFM {
    private static final Logger LOG = LoggerFactory.getLogger(SysctlUtilFFM.class);
    private static final String SYSCTL_FAIL = "Failed sysctl call: {}, Error code: {}";

    private SysctlUtilFFM() {
    }

    public static int sysctl(String name, int def) {
        return SysctlUtilFFM.sysctl(name, def, true);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static int sysctl(String name, int def, boolean logWarning) {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment nameSeg = arena.allocateFrom(name);
            MemorySegment valueSeg = arena.allocate(ValueLayout.JAVA_INT);
            MemorySegment sizeSeg = arena.allocateFrom(MacSystemFunctions.SIZE_T, ValueLayout.JAVA_INT.byteSize());
            if (!SysctlUtilFFM.sysctlbyname(arena, nameSeg, valueSeg, sizeSeg, logWarning)) {
                int n = def;
                return n;
            }
            int n = valueSeg.get(ValueLayout.JAVA_INT, 0L);
            return n;
        }
        catch (Throwable e) {
            if (!logWarning) return def;
            LOG.warn("Failed to get sysctl value for {}", (Object)name, (Object)e);
            return def;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static long sysctl(String name, long def) {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment nameSeg = arena.allocateFrom(name);
            MemorySegment valueSeg = arena.allocate(ValueLayout.JAVA_LONG);
            MemorySegment sizeSeg = arena.allocateFrom(MacSystemFunctions.SIZE_T, ValueLayout.JAVA_LONG.byteSize());
            if (!SysctlUtilFFM.sysctlbyname(arena, nameSeg, valueSeg, sizeSeg, true)) {
                long l = def;
                return l;
            }
            long l = valueSeg.get(ValueLayout.JAVA_LONG, 0L);
            return l;
        }
        catch (Throwable e) {
            LOG.warn("Failed to get sysctl value for {}", (Object)name, (Object)e);
            return def;
        }
    }

    public static String sysctl(String name, String def) {
        return SysctlUtilFFM.sysctl(name, def, true);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static String sysctl(String name, String def, boolean logWarning) {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment nameSeg = arena.allocateFrom(name);
            MemorySegment sizeSeg = arena.allocate(MacSystemFunctions.SIZE_T);
            if (!SysctlUtilFFM.sysctlbyname(arena, nameSeg, MemorySegment.NULL, sizeSeg, logWarning)) {
                String string = def;
                return string;
            }
            long size = sizeSeg.get(MacSystemFunctions.SIZE_T, 0L);
            MemorySegment valueSeg = arena.allocate(size + 1L);
            if (!SysctlUtilFFM.sysctlbyname(arena, nameSeg, valueSeg, sizeSeg, logWarning)) {
                String string = def;
                return string;
            }
            String string = valueSeg.getString(0L);
            return string;
        }
        catch (Throwable e) {
            if (!logWarning) return def;
            LOG.warn("Failed to get sysctl value for {}", (Object)name, (Object)e);
            return def;
        }
    }

    public static boolean sysctl(String name, MemorySegment struct) {
        Arena arena = Arena.ofConfined();
        try {
            MemorySegment nameSeg = arena.allocateFrom(name);
            MemorySegment sizeSeg = arena.allocateFrom(MacSystemFunctions.SIZE_T, struct.byteSize());
            boolean bl = SysctlUtilFFM.sysctlbyname(arena, nameSeg, struct, sizeSeg, true);
            if (arena != null) {
                arena.close();
            }
            return bl;
        }
        catch (Throwable throwable) {
            try {
                if (arena != null) {
                    try {
                        arena.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
            catch (Throwable e) {
                LOG.warn("Failed to get sysctl value for {}", (Object)name, (Object)e);
                return false;
            }
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static MemorySegment sysctl(String name) {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment nameSeg = arena.allocateFrom(name);
            MemorySegment sizeSeg = arena.allocate(MacSystemFunctions.SIZE_T);
            if (!SysctlUtilFFM.sysctlbyname(arena, nameSeg, MemorySegment.NULL, sizeSeg, true)) {
                MemorySegment memorySegment = null;
                return memorySegment;
            }
            long size = sizeSeg.get(MacSystemFunctions.SIZE_T, 0L);
            MemorySegment valueSeg = arena.allocate(size);
            if (!SysctlUtilFFM.sysctlbyname(arena, nameSeg, valueSeg, sizeSeg, true)) {
                MemorySegment memorySegment = null;
                return memorySegment;
            }
            MemorySegment returnSeg = Arena.ofAuto().allocate(size);
            returnSeg.copyFrom(valueSeg);
            MemorySegment memorySegment = returnSeg;
            return memorySegment;
        }
        catch (Throwable e) {
            LOG.warn("Failed to get sysctl value for {}", (Object)name, (Object)e);
            return null;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static long sysctl(int[] mib, MemorySegment buffer) {
        try (Arena arena = Arena.ofConfined();){
            MemorySegment mibSeg = arena.allocateFrom(ValueLayout.JAVA_INT, mib);
            MemorySegment sizeSeg = arena.allocateFrom(MacSystemFunctions.SIZE_T, buffer.byteSize());
            MemorySegment callState = arena.allocate(ForeignFunctions.CAPTURED_STATE_LAYOUT);
            int result = MacSystemFunctions.sysctl(callState, mibSeg, mib.length, buffer, sizeSeg, MemorySegment.NULL, 0L);
            if (result != 0) {
                LOG.warn(SYSCTL_FAIL, (Object)Arrays.toString(mib), (Object)ForeignFunctions.getErrno(callState));
                long l = -1L;
                return l;
            }
            long l = sizeSeg.get(MacSystemFunctions.SIZE_T, 0L);
            return l;
        }
        catch (Throwable e) {
            LOG.warn("Failed to get sysctl value for {}", (Object)Arrays.toString(mib), (Object)e);
            return -1L;
        }
    }

    private static boolean sysctlbyname(Arena arena, MemorySegment name, MemorySegment oldp, MemorySegment oldlenp, boolean logWarning) throws Throwable {
        MemorySegment callState = arena.allocate(ForeignFunctions.CAPTURED_STATE_LAYOUT);
        int result = MacSystemFunctions.sysctlbyname(callState, name, oldp, oldlenp, MemorySegment.NULL, 0L);
        if (result != 0) {
            if (logWarning) {
                LOG.warn(SYSCTL_FAIL, (Object)name, (Object)ForeignFunctions.getErrno(callState));
            }
            return false;
        }
        return true;
    }
}

