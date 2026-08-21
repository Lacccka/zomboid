/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32.COM;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;

@Structure.FieldOrder(value={"QueryInterfaceCallback", "AddRefCallback", "ReleaseCallback"})
public class UnknownVTable
extends Structure {
    public QueryInterfaceCallback QueryInterfaceCallback;
    public AddRefCallback AddRefCallback;
    public ReleaseCallback ReleaseCallback;

    public static interface ReleaseCallback
    extends StdCallLibrary.StdCallCallback {
        public int invoke(Pointer var1);
    }

    public static interface AddRefCallback
    extends StdCallLibrary.StdCallCallback {
        public int invoke(Pointer var1);
    }

    public static interface QueryInterfaceCallback
    extends StdCallLibrary.StdCallCallback {
        public WinNT.HRESULT invoke(Pointer var1, Guid.REFIID var2, PointerByReference var3);
    }

    public static class ByReference
    extends UnknownVTable
    implements Structure.ByReference {
    }
}

