/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Crypt32;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinCrypt;
import java.util.Arrays;

public abstract class Crypt32Util {
    public static byte[] cryptProtectData(byte[] data) {
        return Crypt32Util.cryptProtectData(data, 0);
    }

    public static byte[] cryptProtectData(byte[] data, int flags) {
        return Crypt32Util.cryptProtectData(data, null, flags, "", null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static byte[] cryptProtectData(byte[] data, byte[] entropy, int flags, String description, WinCrypt.CRYPTPROTECT_PROMPTSTRUCT prompt) {
        WinCrypt.DATA_BLOB pDataIn = new WinCrypt.DATA_BLOB(data);
        WinCrypt.DATA_BLOB pDataProtected = new WinCrypt.DATA_BLOB();
        WinCrypt.DATA_BLOB pEntropy = entropy == null ? null : new WinCrypt.DATA_BLOB(entropy);
        Win32Exception err = null;
        byte[] protectedData = null;
        try {
            if (!Crypt32.INSTANCE.CryptProtectData(pDataIn, description, pEntropy, null, prompt, flags, pDataProtected)) {
                err = new Win32Exception(Kernel32.INSTANCE.GetLastError());
            } else {
                protectedData = pDataProtected.getData();
            }
        }
        finally {
            if (pDataIn.pbData != null) {
                pDataIn.pbData.clear(pDataIn.cbData);
            }
            if (pEntropy != null && pEntropy.pbData != null) {
                pEntropy.pbData.clear(pEntropy.cbData);
            }
            if (pDataProtected.pbData != null) {
                pDataProtected.pbData.clear(pDataProtected.cbData);
                try {
                    Kernel32Util.freeLocalMemory(pDataProtected.pbData);
                }
                catch (Win32Exception e) {
                    if (err == null) {
                        err = e;
                    }
                    err.addSuppressedReflected(e);
                }
            }
        }
        if (err != null) {
            if (protectedData != null) {
                Arrays.fill(protectedData, (byte)0);
            }
            throw err;
        }
        return protectedData;
    }

    public static byte[] cryptUnprotectData(byte[] data) {
        return Crypt32Util.cryptUnprotectData(data, 0);
    }

    public static byte[] cryptUnprotectData(byte[] data, int flags) {
        return Crypt32Util.cryptUnprotectData(data, null, flags, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static byte[] cryptUnprotectData(byte[] data, byte[] entropy, int flags, WinCrypt.CRYPTPROTECT_PROMPTSTRUCT prompt) {
        WinCrypt.DATA_BLOB pDataIn = new WinCrypt.DATA_BLOB(data);
        WinCrypt.DATA_BLOB pDataUnprotected = new WinCrypt.DATA_BLOB();
        WinCrypt.DATA_BLOB pEntropy = entropy == null ? null : new WinCrypt.DATA_BLOB(entropy);
        Win32Exception err = null;
        byte[] unProtectedData = null;
        try {
            if (!Crypt32.INSTANCE.CryptUnprotectData(pDataIn, null, pEntropy, null, prompt, flags, pDataUnprotected)) {
                err = new Win32Exception(Kernel32.INSTANCE.GetLastError());
            } else {
                unProtectedData = pDataUnprotected.getData();
            }
        }
        finally {
            if (pDataIn.pbData != null) {
                pDataIn.pbData.clear(pDataIn.cbData);
            }
            if (pEntropy != null && pEntropy.pbData != null) {
                pEntropy.pbData.clear(pEntropy.cbData);
            }
            if (pDataUnprotected.pbData != null) {
                pDataUnprotected.pbData.clear(pDataUnprotected.cbData);
                try {
                    Kernel32Util.freeLocalMemory(pDataUnprotected.pbData);
                }
                catch (Win32Exception e) {
                    if (err == null) {
                        err = e;
                    }
                    err.addSuppressedReflected(e);
                }
            }
        }
        if (err != null) {
            if (unProtectedData != null) {
                Arrays.fill(unProtectedData, (byte)0);
            }
            throw err;
        }
        return unProtectedData;
    }

    public static String CertNameToStr(int dwCertEncodingType, int dwStrType, WinCrypt.DATA_BLOB pName) {
        int charToBytes = Boolean.getBoolean("w32.ascii") ? 1 : Native.WCHAR_SIZE;
        int requiredSize = Crypt32.INSTANCE.CertNameToStr(dwCertEncodingType, pName, dwStrType, Pointer.NULL, 0);
        Memory mem = new Memory(requiredSize * charToBytes);
        int resultBytes = Crypt32.INSTANCE.CertNameToStr(dwCertEncodingType, pName, dwStrType, mem, requiredSize);
        assert (resultBytes == requiredSize);
        if (Boolean.getBoolean("w32.ascii")) {
            return mem.getString(0L);
        }
        return mem.getWideString(0L);
    }
}

