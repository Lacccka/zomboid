/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.ShlObj;
import com.sun.jna.platform.win32.W32Errors;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public abstract class Shell32Util {
    public static String getFolderPath(WinDef.HWND hwnd, int nFolder, WinDef.DWORD dwFlags) {
        char[] pszPath = new char[260];
        WinNT.HRESULT hr = Shell32.INSTANCE.SHGetFolderPath(hwnd, nFolder, null, dwFlags, pszPath);
        if (!hr.equals(W32Errors.S_OK)) {
            throw new Win32Exception(hr);
        }
        return Native.toString(pszPath);
    }

    public static String getFolderPath(int nFolder) {
        return Shell32Util.getFolderPath(null, nFolder, ShlObj.SHGFP_TYPE_CURRENT);
    }

    public static String getKnownFolderPath(Guid.GUID guid) throws Win32Exception {
        PointerByReference outPath;
        WinNT.HANDLE token;
        int flags = ShlObj.KNOWN_FOLDER_FLAG.NONE.getFlag();
        WinNT.HRESULT hr = Shell32.INSTANCE.SHGetKnownFolderPath(guid, flags, token = null, outPath = new PointerByReference());
        if (!W32Errors.SUCCEEDED(hr.intValue())) {
            throw new Win32Exception(hr);
        }
        String result = outPath.getValue().getWideString(0L);
        Ole32.INSTANCE.CoTaskMemFree(outPath.getValue());
        return result;
    }

    public static final String getSpecialFolderPath(int csidl, boolean create) {
        char[] pszPath = new char[260];
        if (!Shell32.INSTANCE.SHGetSpecialFolderPath(null, pszPath, csidl, create)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        return Native.toString(pszPath);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final String[] CommandLineToArgv(String cmdLine) {
        WString cl = new WString(cmdLine);
        IntByReference nargs = new IntByReference();
        Pointer strArr = Shell32.INSTANCE.CommandLineToArgvW(cl, nargs);
        if (strArr != null) {
            try {
                String[] stringArray = strArr.getWideStringArray(0L, nargs.getValue());
                return stringArray;
            }
            finally {
                Kernel32.INSTANCE.LocalFree(strArr);
            }
        }
        throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
    }
}

