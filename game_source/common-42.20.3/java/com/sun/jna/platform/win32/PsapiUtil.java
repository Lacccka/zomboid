/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import java.util.Arrays;

public abstract class PsapiUtil {
    public static int[] enumProcesses() {
        int size = 0;
        int[] lpidProcess = null;
        IntByReference lpcbNeeded = new IntByReference();
        do {
            if (Psapi.INSTANCE.EnumProcesses(lpidProcess = new int[size += 1024], size * 4, lpcbNeeded)) continue;
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        } while (size == lpcbNeeded.getValue() / 4);
        return Arrays.copyOf(lpidProcess, lpcbNeeded.getValue() / 4);
    }

    public static String GetProcessImageFileName(WinNT.HANDLE hProcess) {
        char[] filePath;
        int length;
        int size = 2048;
        while ((length = Psapi.INSTANCE.GetProcessImageFileName(hProcess, filePath = new char[size], filePath.length)) == 0) {
            if (Native.getLastError() != 122) {
                throw new Win32Exception(Native.getLastError());
            }
            size += 2048;
        }
        return Native.toString(filePath);
    }
}

