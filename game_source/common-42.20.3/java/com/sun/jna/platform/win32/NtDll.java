/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface NtDll
extends StdCallLibrary {
    public static final NtDll INSTANCE = Native.load("NtDll", NtDll.class, W32APIOptions.DEFAULT_OPTIONS);

    public int ZwQueryKey(WinNT.HANDLE var1, int var2, Structure var3, int var4, IntByReference var5);

    public int NtSetSecurityObject(WinNT.HANDLE var1, int var2, Pointer var3);

    public int NtQuerySecurityObject(WinNT.HANDLE var1, int var2, Pointer var3, int var4, IntByReference var5);

    public int RtlNtStatusToDosError(int var1);
}

