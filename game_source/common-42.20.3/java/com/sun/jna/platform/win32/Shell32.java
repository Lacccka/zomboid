/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.ShellAPI;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface Shell32
extends ShellAPI,
StdCallLibrary {
    public static final Shell32 INSTANCE = Native.load("shell32", Shell32.class, W32APIOptions.DEFAULT_OPTIONS);
    public static final int SHERB_NOCONFIRMATION = 1;
    public static final int SHERB_NOPROGRESSUI = 2;
    public static final int SHERB_NOSOUND = 4;
    public static final int SEE_MASK_NOCLOSEPROCESS = 64;
    public static final int SEE_MASK_FLAG_NO_UI = 1024;

    public int SHFileOperation(ShellAPI.SHFILEOPSTRUCT var1);

    public WinNT.HRESULT SHGetFolderPath(WinDef.HWND var1, int var2, WinNT.HANDLE var3, WinDef.DWORD var4, char[] var5);

    public WinNT.HRESULT SHGetKnownFolderPath(Guid.GUID var1, int var2, WinNT.HANDLE var3, PointerByReference var4);

    public WinNT.HRESULT SHGetDesktopFolder(PointerByReference var1);

    public WinDef.INT_PTR ShellExecute(WinDef.HWND var1, String var2, String var3, String var4, String var5, int var6);

    public boolean SHGetSpecialFolderPath(WinDef.HWND var1, char[] var2, int var3, boolean var4);

    public WinDef.UINT_PTR SHAppBarMessage(WinDef.DWORD var1, ShellAPI.APPBARDATA var2);

    public int SHEmptyRecycleBin(WinNT.HANDLE var1, String var2, int var3);

    public boolean ShellExecuteEx(ShellAPI.SHELLEXECUTEINFO var1);

    public WinNT.HRESULT SHGetSpecialFolderLocation(WinDef.HWND var1, int var2, PointerByReference var3);

    public int ExtractIconEx(String var1, int var2, WinDef.HICON[] var3, WinDef.HICON[] var4, int var5);

    public WinNT.HRESULT GetCurrentProcessExplicitAppUserModelID(PointerByReference var1);

    public WinNT.HRESULT SetCurrentProcessExplicitAppUserModelID(WString var1);

    public Pointer CommandLineToArgvW(WString var1, IntByReference var2);
}

