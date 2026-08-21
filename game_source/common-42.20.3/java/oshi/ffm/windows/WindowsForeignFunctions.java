/*
 * Decompiled with CFR 0.152.
 */
package oshi.ffm.windows;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;
import oshi.ffm.ForeignFunctions;
import oshi.ffm.windows.Win32Exception;
import oshi.ffm.windows.WinNTFFM;

public abstract class WindowsForeignFunctions
extends ForeignFunctions {
    protected WindowsForeignFunctions() {
    }

    public static int checkSuccess(int rc, int ... allowedErrors) {
        if (rc == 0) {
            return rc;
        }
        for (int allowed : allowedErrors) {
            if (rc != allowed) continue;
            return rc;
        }
        throw new Win32Exception(rc);
    }

    public static boolean isSuccess(int winBool) {
        return winBool != 0;
    }

    public static String readWideString(MemorySegment seg) {
        char c;
        StringBuilder sb = new StringBuilder();
        int offset = 0;
        while ((c = seg.get(ValueLayout.JAVA_CHAR, (long)offset)) != '\u0000') {
            sb.append(c);
            offset += 2;
        }
        return sb.toString();
    }

    public static String readAnsiString(MemorySegment seg, int maxLen) {
        byte b;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxLen && (b = seg.get(ValueLayout.JAVA_BYTE, (long)i)) != 0; ++i) {
            sb.append((char)b);
        }
        return sb.toString();
    }

    public static MemorySegment setupTokenPrivileges(Arena arena, MemorySegment luid) {
        MemorySegment tkp = arena.allocate(WinNTFFM.TOKEN_PRIVILEGES);
        tkp.set(ValueLayout.JAVA_INT, WinNTFFM.TOKEN_PRIVILEGES_PRIVILEGE_COUNT_OFFSET, 1);
        MemorySegment luidSegment = tkp.asSlice(WinNTFFM.TOKEN_PRIVILEGES_LUID_OFFSET, luid.byteSize());
        luidSegment.copyFrom(luid);
        tkp.set(ValueLayout.JAVA_INT, WinNTFFM.TOKEN_PRIVILEGES_ATTRIBUTES_OFFSET, 2);
        return tkp;
    }

    public static MemorySegment toWideString(Arena arena, String s) {
        return arena.allocateFrom(s + "\u0000", StandardCharsets.UTF_16LE);
    }
}

