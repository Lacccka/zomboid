/*
 * Decompiled with CFR 0.152.
 */
package oshi.util.platform.mac;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.unix.LibCAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.ByRef;
import oshi.jna.platform.mac.SystemB;

@ThreadSafe
public final class SysctlUtil {
    private static final Logger LOG = LoggerFactory.getLogger(SysctlUtil.class);
    private static final String SYSCTL_FAIL = "Failed sysctl call: {}, Error code: {}";

    private SysctlUtil() {
    }

    public static int sysctl(String name, int def) {
        return SysctlUtil.sysctl(name, def, true);
    }

    public static int sysctl(String name, int def, boolean logWarning) {
        int intSize = com.sun.jna.platform.mac.SystemB.INT_SIZE;
        try (Memory p = new Memory(intSize);){
            ByRef.CloseableSizeTByReference size;
            block13: {
                size = new ByRef.CloseableSizeTByReference((long)intSize);
                try {
                    if (0 == SystemB.INSTANCE.sysctlbyname(name, (Pointer)p, size, null, LibCAPI.size_t.ZERO)) break block13;
                    if (logWarning) {
                        LOG.warn(SYSCTL_FAIL, (Object)name, (Object)Native.getLastError());
                    }
                    int n = def;
                    size.close();
                    return n;
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
            int n = p.getInt(0L);
            size.close();
            return n;
        }
    }

    public static long sysctl(String name, long def) {
        int uint64Size = com.sun.jna.platform.mac.SystemB.UINT64_SIZE;
        try (Memory p = new Memory(uint64Size);){
            ByRef.CloseableSizeTByReference size;
            block12: {
                size = new ByRef.CloseableSizeTByReference((long)uint64Size);
                try {
                    if (0 == SystemB.INSTANCE.sysctlbyname(name, (Pointer)p, size, null, LibCAPI.size_t.ZERO)) break block12;
                    LOG.warn(SYSCTL_FAIL, (Object)name, (Object)Native.getLastError());
                    long l = def;
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
            long l = p.getLong(0L);
            size.close();
            return l;
        }
    }

    public static String sysctl(String name, String def) {
        return SysctlUtil.sysctl(name, def, true);
    }

    public static String sysctl(String name, String def, boolean logWarning) {
        try (ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference();){
            Memory p;
            block16: {
                if (0 != SystemB.INSTANCE.sysctlbyname(name, null, size, null, LibCAPI.size_t.ZERO)) {
                    if (logWarning) {
                        LOG.warn(SYSCTL_FAIL, (Object)name, (Object)Native.getLastError());
                    }
                    String string = def;
                    return string;
                }
                p = new Memory(size.longValue() + 1L);
                try {
                    if (0 == SystemB.INSTANCE.sysctlbyname(name, (Pointer)p, size, null, LibCAPI.size_t.ZERO)) break block16;
                    if (logWarning) {
                        LOG.warn(SYSCTL_FAIL, (Object)name, (Object)Native.getLastError());
                    }
                    String string = def;
                    p.close();
                    return string;
                }
                catch (Throwable throwable) {
                    try {
                        p.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                    throw throwable;
                }
            }
            String string = p.getString(0L);
            p.close();
            return string;
        }
    }

    public static boolean sysctl(String name, Structure struct) {
        try (ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference((long)struct.size());){
            if (0 != SystemB.INSTANCE.sysctlbyname(name, struct.getPointer(), size, null, LibCAPI.size_t.ZERO)) {
                LOG.warn(SYSCTL_FAIL, (Object)name, (Object)Native.getLastError());
                boolean bl = false;
                return bl;
            }
        }
        struct.read();
        return true;
    }

    public static Memory sysctl(String name) {
        try (ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference();){
            if (0 != SystemB.INSTANCE.sysctlbyname(name, null, size, null, LibCAPI.size_t.ZERO)) {
                LOG.warn(SYSCTL_FAIL, (Object)name, (Object)Native.getLastError());
                Memory memory = null;
                return memory;
            }
            Memory m = new Memory(size.longValue());
            if (0 != SystemB.INSTANCE.sysctlbyname(name, (Pointer)m, size, null, LibCAPI.size_t.ZERO)) {
                LOG.warn(SYSCTL_FAIL, (Object)name, (Object)Native.getLastError());
                m.close();
                Memory memory = null;
                return memory;
            }
            Memory memory = m;
            return memory;
        }
    }
}

