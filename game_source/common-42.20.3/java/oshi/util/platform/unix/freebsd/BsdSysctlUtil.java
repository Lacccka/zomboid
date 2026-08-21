/*
 * Decompiled with CFR 0.152.
 */
package oshi.util.platform.unix.freebsd;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.unix.LibCAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.ByRef;
import oshi.jna.platform.unix.FreeBsdLibc;

@ThreadSafe
public final class BsdSysctlUtil {
    private static final Logger LOG = LoggerFactory.getLogger(BsdSysctlUtil.class);
    private static final String SYSCTL_FAIL = "Failed sysctl call: {}, Error code: {}";

    private BsdSysctlUtil() {
    }

    public static int sysctl(String name, int def) {
        int intSize = FreeBsdLibc.INT_SIZE;
        try (Memory p = new Memory(intSize);){
            ByRef.CloseableSizeTByReference size;
            block12: {
                size = new ByRef.CloseableSizeTByReference((long)intSize);
                try {
                    if (0 == FreeBsdLibc.INSTANCE.sysctlbyname(name, p, size, null, LibCAPI.size_t.ZERO)) break block12;
                    LOG.warn(SYSCTL_FAIL, (Object)name, (Object)Native.getLastError());
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
        int uint64Size = FreeBsdLibc.UINT64_SIZE;
        try (Memory p = new Memory(uint64Size);){
            ByRef.CloseableSizeTByReference size;
            block12: {
                size = new ByRef.CloseableSizeTByReference((long)uint64Size);
                try {
                    if (0 == FreeBsdLibc.INSTANCE.sysctlbyname(name, p, size, null, LibCAPI.size_t.ZERO)) break block12;
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
        try (ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference();){
            Memory p;
            block14: {
                if (0 != FreeBsdLibc.INSTANCE.sysctlbyname(name, null, size, null, LibCAPI.size_t.ZERO)) {
                    LOG.warn(SYSCTL_FAIL, (Object)name, (Object)Native.getLastError());
                    String string = def;
                    return string;
                }
                p = new Memory(size.longValue() + 1L);
                try {
                    if (0 == FreeBsdLibc.INSTANCE.sysctlbyname(name, p, size, null, LibCAPI.size_t.ZERO)) break block14;
                    LOG.warn(SYSCTL_FAIL, (Object)name, (Object)Native.getLastError());
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
            if (0 != FreeBsdLibc.INSTANCE.sysctlbyname(name, struct.getPointer(), size, null, LibCAPI.size_t.ZERO)) {
                LOG.error(SYSCTL_FAIL, (Object)name, (Object)Native.getLastError());
                boolean bl = false;
                return bl;
            }
        }
        struct.read();
        return true;
    }

    public static Memory sysctl(String name) {
        try (ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference();){
            if (0 != FreeBsdLibc.INSTANCE.sysctlbyname(name, null, size, null, LibCAPI.size_t.ZERO)) {
                LOG.error(SYSCTL_FAIL, (Object)name, (Object)Native.getLastError());
                Memory memory = null;
                return memory;
            }
            Memory m = new Memory(size.longValue());
            if (0 != FreeBsdLibc.INSTANCE.sysctlbyname(name, m, size, null, LibCAPI.size_t.ZERO)) {
                LOG.error(SYSCTL_FAIL, (Object)name, (Object)Native.getLastError());
                m.close();
                Memory memory = null;
                return memory;
            }
            Memory memory = m;
            return memory;
        }
    }
}

