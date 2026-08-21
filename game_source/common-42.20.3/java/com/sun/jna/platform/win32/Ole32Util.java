/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.W32Errors;
import com.sun.jna.platform.win32.WinNT;

public abstract class Ole32Util {
    public static Guid.GUID getGUIDFromString(String guidString) {
        Guid.GUID lpiid = new Guid.GUID();
        WinNT.HRESULT hr = Ole32.INSTANCE.IIDFromString(guidString, lpiid);
        if (!hr.equals(W32Errors.S_OK)) {
            throw new RuntimeException(hr.toString());
        }
        return lpiid;
    }

    public static String getStringFromGUID(Guid.GUID guid) {
        int max;
        char[] lpsz;
        Guid.GUID pguid = new Guid.GUID(guid.getPointer());
        int len = Ole32.INSTANCE.StringFromGUID2(pguid, lpsz = new char[max = 39], max);
        if (len == 0) {
            throw new RuntimeException("StringFromGUID2");
        }
        lpsz[len - 1] = '\u0000';
        return Native.toString(lpsz);
    }

    public static Guid.GUID generateGUID() {
        Guid.GUID pguid = new Guid.GUID();
        WinNT.HRESULT hr = Ole32.INSTANCE.CoCreateGuid(pguid);
        if (!hr.equals(W32Errors.S_OK)) {
            throw new RuntimeException(hr.toString());
        }
        return pguid;
    }
}

