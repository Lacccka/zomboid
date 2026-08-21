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
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;

public class Stat {
    protected Stat() {
        throw new UnsupportedOperationException();
    }

    public static native int nstat(long var0, long var2, long var4);

    public static int stat(@NativeType(value="int *") @Nullable IntBuffer _errno, @NativeType(value="char const *") ByteBuffer __file, @NativeType(value="struct stat *") long __buf) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)_errno, 1);
            Checks.checkNT1(__file);
            Checks.check(__buf);
        }
        return Stat.nstat(MemoryUtil.memAddressSafe(_errno), MemoryUtil.memAddress(__file), __buf);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int stat(@NativeType(value="int *") @Nullable IntBuffer _errno, @NativeType(value="char const *") CharSequence __file, @NativeType(value="struct stat *") long __buf) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)_errno, 1);
            Checks.check(__buf);
        }
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8(__file, true);
            long __fileEncoded = stack.getPointerAddress();
            int n = Stat.nstat(MemoryUtil.memAddressSafe(_errno), __fileEncoded, __buf);
            return n;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native int nfstat(long var0, int var2, long var3);

    public static int fstat(@NativeType(value="int *") @Nullable IntBuffer _errno, int __fd, @NativeType(value="struct stat *") long __buf) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)_errno, 1);
            Checks.check(__buf);
        }
        return Stat.nfstat(MemoryUtil.memAddressSafe(_errno), __fd, __buf);
    }

    static {
        Library.initialize();
    }
}

