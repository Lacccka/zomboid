/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32.COM;

import com.sun.jna.WString;
import com.sun.jna.platform.win32.COM.ITypeInfo;
import com.sun.jna.platform.win32.COM.IUnknown;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Variant;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;

public interface IRecordInfo
extends IUnknown {
    public static final Guid.IID IID_IRecordInfo = new Guid.IID("{0000002F-0000-0000-C000-000000000046}");

    public WinNT.HRESULT RecordInit(WinDef.PVOID var1);

    public WinNT.HRESULT RecordClear(WinDef.PVOID var1);

    public WinNT.HRESULT RecordCopy(WinDef.PVOID var1, WinDef.PVOID var2);

    public WinNT.HRESULT GetGuid(Guid.GUID var1);

    public WinNT.HRESULT GetName(WTypes.BSTR var1);

    public WinNT.HRESULT GetSize(WinDef.ULONG var1);

    public WinNT.HRESULT GetTypeInfo(ITypeInfo var1);

    public WinNT.HRESULT GetField(WinDef.PVOID var1, WString var2, Variant.VARIANT var3);

    public WinNT.HRESULT GetFieldNoCopy(WinDef.PVOID var1, WString var2, Variant.VARIANT var3, WinDef.PVOID var4);

    public WinNT.HRESULT PutField(WinDef.ULONG var1, WinDef.PVOID var2, WString var3, Variant.VARIANT var4);

    public WinNT.HRESULT PutFieldNoCopy(WinDef.ULONG var1, WinDef.PVOID var2, WString var3, Variant.VARIANT var4);

    public WinNT.HRESULT GetFieldNames(WinDef.ULONG var1, WTypes.BSTR var2);

    public WinDef.BOOL IsMatchingType(IRecordInfo var1);

    public WinDef.PVOID RecordCreate();

    public WinNT.HRESULT RecordCreateCopy(WinDef.PVOID var1, WinDef.PVOID var2);

    public WinNT.HRESULT RecordDestroy(WinDef.PVOID var1);
}

