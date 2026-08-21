/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32.COM;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.IUnknown;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;

public interface IRunningObjectTable
extends IUnknown {
    public static final Guid.IID IID = new Guid.IID("{00000010-0000-0000-C000-000000000046}");

    public WinNT.HRESULT EnumRunning(PointerByReference var1);

    public WinNT.HRESULT GetObject(Pointer var1, PointerByReference var2);

    public WinNT.HRESULT GetTimeOfLastChange(Pointer var1, WinBase.FILETIME.ByReference var2);

    public WinNT.HRESULT IsRunning(Pointer var1);

    public WinNT.HRESULT NoteChangeTime(WinDef.DWORD var1, WinBase.FILETIME var2);

    public WinNT.HRESULT Register(WinDef.DWORD var1, Pointer var2, Pointer var3, WinDef.DWORDByReference var4);

    public WinNT.HRESULT Revoke(WinDef.DWORD var1);
}

