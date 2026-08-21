/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.WinCrypt;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface Cryptui
extends StdCallLibrary {
    public static final Cryptui INSTANCE = Native.load("Cryptui", Cryptui.class, W32APIOptions.UNICODE_OPTIONS);

    public WinCrypt.CERT_CONTEXT.ByReference CryptUIDlgSelectCertificateFromStore(WinCrypt.HCERTSTORE var1, WinDef.HWND var2, String var3, String var4, int var5, int var6, PointerType var7);
}

