/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface Psapi
extends StdCallLibrary {
    public static final Psapi INSTANCE = Native.load("psapi", Psapi.class, W32APIOptions.DEFAULT_OPTIONS);

    public int GetModuleFileNameExA(WinNT.HANDLE var1, WinNT.HANDLE var2, byte[] var3, int var4);

    public int GetModuleFileNameExW(WinNT.HANDLE var1, WinNT.HANDLE var2, char[] var3, int var4);

    public int GetModuleFileNameEx(WinNT.HANDLE var1, WinNT.HANDLE var2, Pointer var3, int var4);

    public boolean EnumProcessModules(WinNT.HANDLE var1, WinDef.HMODULE[] var2, int var3, IntByReference var4);

    public boolean GetModuleInformation(WinNT.HANDLE var1, WinDef.HMODULE var2, MODULEINFO var3, int var4);

    public int GetProcessImageFileName(WinNT.HANDLE var1, char[] var2, int var3);

    public boolean GetPerformanceInfo(PERFORMANCE_INFORMATION var1, int var2);

    public boolean EnumProcesses(int[] var1, int var2, IntByReference var3);

    public boolean QueryWorkingSetEx(WinNT.HANDLE var1, Pointer var2, int var3);

    @Structure.FieldOrder(value={"VirtualAddress", "VirtualAttributes"})
    public static class PSAPI_WORKING_SET_EX_INFORMATION
    extends Structure {
        public Pointer VirtualAddress;
        public BaseTSD.ULONG_PTR VirtualAttributes;

        public boolean isValid() {
            return this.getBitFieldValue(1, 0) == 1;
        }

        public int getShareCount() {
            return this.getBitFieldValue(3, 1);
        }

        public int getWin32Protection() {
            return this.getBitFieldValue(11, 4);
        }

        public boolean isShared() {
            return this.getBitFieldValue(1, 15) == 1;
        }

        public int getNode() {
            return this.getBitFieldValue(6, 16);
        }

        public boolean isLocked() {
            return this.getBitFieldValue(1, 22) == 1;
        }

        public boolean isLargePage() {
            return this.getBitFieldValue(1, 23) == 1;
        }

        public boolean isBad() {
            return this.getBitFieldValue(1, 25) == 1;
        }

        private int getBitFieldValue(int maskLength, int rightShiftAmount) {
            long bitMask = 0L;
            for (int l = 0; l < maskLength; ++l) {
                bitMask |= (long)(1 << l);
            }
            return (int)(this.VirtualAttributes.longValue() >>> rightShiftAmount & bitMask);
        }
    }

    @Structure.FieldOrder(value={"cb", "CommitTotal", "CommitLimit", "CommitPeak", "PhysicalTotal", "PhysicalAvailable", "SystemCache", "KernelTotal", "KernelPaged", "KernelNonpaged", "PageSize", "HandleCount", "ProcessCount", "ThreadCount"})
    public static class PERFORMANCE_INFORMATION
    extends Structure {
        public WinDef.DWORD cb;
        public BaseTSD.SIZE_T CommitTotal;
        public BaseTSD.SIZE_T CommitLimit;
        public BaseTSD.SIZE_T CommitPeak;
        public BaseTSD.SIZE_T PhysicalTotal;
        public BaseTSD.SIZE_T PhysicalAvailable;
        public BaseTSD.SIZE_T SystemCache;
        public BaseTSD.SIZE_T KernelTotal;
        public BaseTSD.SIZE_T KernelPaged;
        public BaseTSD.SIZE_T KernelNonpaged;
        public BaseTSD.SIZE_T PageSize;
        public WinDef.DWORD HandleCount;
        public WinDef.DWORD ProcessCount;
        public WinDef.DWORD ThreadCount;
    }

    @Structure.FieldOrder(value={"lpBaseOfDll", "SizeOfImage", "EntryPoint"})
    public static class MODULEINFO
    extends Structure {
        public Pointer EntryPoint;
        public Pointer lpBaseOfDll;
        public int SizeOfImage;
    }
}

