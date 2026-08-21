/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32.COM;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.IEnumMoniker;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;

public class EnumMoniker
extends Unknown
implements IEnumMoniker {
    public EnumMoniker(Pointer pointer) {
        super(pointer);
    }

    @Override
    public WinNT.HRESULT Next(WinDef.ULONG celt, PointerByReference rgelt, WinDef.ULONGByReference pceltFetched) {
        int vTableId = 3;
        WinNT.HRESULT hr = (WinNT.HRESULT)this._invokeNativeObject(3, new Object[]{this.getPointer(), celt, rgelt, pceltFetched}, WinNT.HRESULT.class);
        return hr;
    }

    @Override
    public WinNT.HRESULT Skip(WinDef.ULONG celt) {
        int vTableId = 4;
        WinNT.HRESULT hr = (WinNT.HRESULT)this._invokeNativeObject(4, new Object[]{this.getPointer(), celt}, WinNT.HRESULT.class);
        return hr;
    }

    @Override
    public WinNT.HRESULT Reset() {
        int vTableId = 5;
        WinNT.HRESULT hr = (WinNT.HRESULT)this._invokeNativeObject(5, new Object[]{this.getPointer()}, WinNT.HRESULT.class);
        return hr;
    }

    @Override
    public WinNT.HRESULT Clone(PointerByReference ppenum) {
        int vTableId = 6;
        WinNT.HRESULT hr = (WinNT.HRESULT)this._invokeNativeObject(6, new Object[]{this.getPointer(), ppenum}, WinNT.HRESULT.class);
        return hr;
    }
}

