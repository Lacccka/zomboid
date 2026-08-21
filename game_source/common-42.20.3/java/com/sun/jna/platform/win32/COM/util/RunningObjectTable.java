/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32.COM.util;

import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.util.EnumMoniker;
import com.sun.jna.platform.win32.COM.util.IDispatch;
import com.sun.jna.platform.win32.COM.util.IRunningObjectTable;
import com.sun.jna.platform.win32.COM.util.ObjectFactory;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;
import java.util.ArrayList;
import java.util.List;

public class RunningObjectTable
implements IRunningObjectTable {
    ObjectFactory factory;
    com.sun.jna.platform.win32.COM.RunningObjectTable raw;

    protected RunningObjectTable(com.sun.jna.platform.win32.COM.RunningObjectTable raw, ObjectFactory factory2) {
        this.raw = raw;
        this.factory = factory2;
    }

    @Override
    public Iterable<IDispatch> enumRunning() {
        assert (COMUtils.comIsInitialized()) : "COM not initialized";
        PointerByReference ppenumMoniker = new PointerByReference();
        WinNT.HRESULT hr = this.raw.EnumRunning(ppenumMoniker);
        COMUtils.checkRC(hr);
        com.sun.jna.platform.win32.COM.EnumMoniker raw = new com.sun.jna.platform.win32.COM.EnumMoniker(ppenumMoniker.getValue());
        return new EnumMoniker(raw, this.raw, this.factory);
    }

    @Override
    public <T> List<T> getActiveObjectsByInterface(Class<T> comInterface) {
        assert (COMUtils.comIsInitialized()) : "COM not initialized";
        ArrayList<T> result = new ArrayList<T>();
        for (IDispatch obj : this.enumRunning()) {
            try {
                T dobj = obj.queryInterface(comInterface);
                result.add(dobj);
            }
            catch (COMException cOMException) {}
        }
        return result;
    }
}

