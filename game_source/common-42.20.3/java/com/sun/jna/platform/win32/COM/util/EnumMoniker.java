/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32.COM.util;

import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.Dispatch;
import com.sun.jna.platform.win32.COM.IEnumMoniker;
import com.sun.jna.platform.win32.COM.IRunningObjectTable;
import com.sun.jna.platform.win32.COM.Moniker;
import com.sun.jna.platform.win32.COM.util.IDispatch;
import com.sun.jna.platform.win32.COM.util.ObjectFactory;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;
import java.util.Iterator;

public class EnumMoniker
implements Iterable<IDispatch> {
    ObjectFactory factory;
    IRunningObjectTable rawRot;
    IEnumMoniker raw;
    Moniker rawNext;

    protected EnumMoniker(IEnumMoniker raw, IRunningObjectTable rawRot, ObjectFactory factory2) {
        assert (COMUtils.comIsInitialized()) : "COM not initialized";
        this.rawRot = rawRot;
        this.raw = raw;
        this.factory = factory2;
        WinNT.HRESULT hr = raw.Reset();
        COMUtils.checkRC(hr);
        this.cacheNext();
    }

    protected void cacheNext() {
        assert (COMUtils.comIsInitialized()) : "COM not initialized";
        PointerByReference rgelt = new PointerByReference();
        WinDef.ULONGByReference pceltFetched = new WinDef.ULONGByReference();
        WinNT.HRESULT hr = this.raw.Next(new WinDef.ULONG(1L), rgelt, pceltFetched);
        if (WinNT.S_OK.equals(hr) && pceltFetched.getValue().intValue() > 0) {
            this.rawNext = new Moniker(rgelt.getValue());
        } else {
            if (!WinNT.S_FALSE.equals(hr)) {
                COMUtils.checkRC(hr);
            }
            this.rawNext = null;
        }
    }

    @Override
    public Iterator<IDispatch> iterator() {
        return new Iterator<IDispatch>(){

            @Override
            public boolean hasNext() {
                return null != EnumMoniker.this.rawNext;
            }

            @Override
            public IDispatch next() {
                assert (COMUtils.comIsInitialized()) : "COM not initialized";
                Moniker moniker = EnumMoniker.this.rawNext;
                PointerByReference ppunkObject = new PointerByReference();
                WinNT.HRESULT hr = EnumMoniker.this.rawRot.GetObject(moniker.getPointer(), ppunkObject);
                COMUtils.checkRC(hr);
                Dispatch dispatch = new Dispatch(ppunkObject.getValue());
                EnumMoniker.this.cacheNext();
                IDispatch d = EnumMoniker.this.factory.createProxy(IDispatch.class, dispatch);
                int n = dispatch.Release();
                return d;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }
}

