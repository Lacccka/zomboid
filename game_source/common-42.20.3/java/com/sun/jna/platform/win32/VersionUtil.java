/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.VerRsrc;
import com.sun.jna.platform.win32.Version;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class VersionUtil {
    public static VerRsrc.VS_FIXEDFILEINFO getFileVersionInfo(String filePath) {
        IntByReference dwDummy = new IntByReference();
        int versionLength = Version.INSTANCE.GetFileVersionInfoSize(filePath, dwDummy);
        if (versionLength == 0) {
            throw new Win32Exception(Native.getLastError());
        }
        Memory lpData = new Memory(versionLength);
        PointerByReference lplpBuffer = new PointerByReference();
        if (!Version.INSTANCE.GetFileVersionInfo(filePath, 0, versionLength, lpData)) {
            throw new Win32Exception(Native.getLastError());
        }
        IntByReference puLen = new IntByReference();
        if (!Version.INSTANCE.VerQueryValue(lpData, "\\", lplpBuffer, puLen)) {
            throw new UnsupportedOperationException("Unable to extract version info from the file: \"" + filePath + "\"");
        }
        VerRsrc.VS_FIXEDFILEINFO fileInfo = new VerRsrc.VS_FIXEDFILEINFO(lplpBuffer.getValue());
        fileInfo.read();
        return fileInfo;
    }
}

