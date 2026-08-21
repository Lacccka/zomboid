/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.linux;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.Checks;
import org.lwjgl.system.Library;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;

public class UNISTD {
    public static final int _SC_OPEN_MAX = 4;
    public static final int _SC_PAGE_SIZE = 30;
    public static final int _SC_IOV_MAX = 60;

    protected UNISTD() {
        throw new UnsupportedOperationException();
    }

    public static native int nclose(long var0, int var2);

    public static int close(@NativeType(value="int *") @Nullable IntBuffer _errno, int fd) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)_errno, 1);
        }
        return UNISTD.nclose(MemoryUtil.memAddressSafe(_errno), fd);
    }

    public static native long nsysconf(long var0, int var2);

    public static long sysconf(@NativeType(value="int *") @Nullable IntBuffer _errno, int name) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)_errno, 1);
        }
        return UNISTD.nsysconf(MemoryUtil.memAddressSafe(_errno), name);
    }

    public static native long nread(long var0, int var2, long var3, long var5);

    @NativeType(value="ssize_t")
    public static long read(@NativeType(value="int *") @Nullable IntBuffer _errno, int fd, @NativeType(value="void *") ByteBuffer buf) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)_errno, 1);
        }
        return UNISTD.nread(MemoryUtil.memAddressSafe(_errno), fd, MemoryUtil.memAddress(buf), buf.remaining());
    }

    @NativeType(value="pid_t")
    public static native int getpid();

    @NativeType(value="pid_t")
    public static native int getppid();

    @NativeType(value="pid_t")
    public static native int gettid();

    static {
        Library.initialize();
    }
}

