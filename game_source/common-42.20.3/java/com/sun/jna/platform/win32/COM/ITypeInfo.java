/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32.COM;

import com.sun.jna.platform.win32.COM.IUnknown;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.OaIdl;
import com.sun.jna.platform.win32.OleAuto;
import com.sun.jna.platform.win32.Variant;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public interface ITypeInfo
extends IUnknown {
    public WinNT.HRESULT GetTypeAttr(PointerByReference var1);

    public WinNT.HRESULT GetTypeComp(PointerByReference var1);

    public WinNT.HRESULT GetFuncDesc(WinDef.UINT var1, PointerByReference var2);

    public WinNT.HRESULT GetVarDesc(WinDef.UINT var1, PointerByReference var2);

    public WinNT.HRESULT GetNames(OaIdl.MEMBERID var1, WTypes.BSTR[] var2, WinDef.UINT var3, WinDef.UINTByReference var4);

    public WinNT.HRESULT GetRefTypeOfImplType(WinDef.UINT var1, OaIdl.HREFTYPEByReference var2);

    public WinNT.HRESULT GetImplTypeFlags(WinDef.UINT var1, IntByReference var2);

    public WinNT.HRESULT GetIDsOfNames(WTypes.LPOLESTR[] var1, WinDef.UINT var2, OaIdl.MEMBERID[] var3);

    public WinNT.HRESULT Invoke(WinDef.PVOID var1, OaIdl.MEMBERID var2, WinDef.WORD var3, OleAuto.DISPPARAMS.ByReference var4, Variant.VARIANT.ByReference var5, OaIdl.EXCEPINFO.ByReference var6, WinDef.UINTByReference var7);

    public WinNT.HRESULT GetDocumentation(OaIdl.MEMBERID var1, WTypes.BSTRByReference var2, WTypes.BSTRByReference var3, WinDef.DWORDByReference var4, WTypes.BSTRByReference var5);

    public WinNT.HRESULT GetDllEntry(OaIdl.MEMBERID var1, OaIdl.INVOKEKIND var2, WTypes.BSTRByReference var3, WTypes.BSTRByReference var4, WinDef.WORDByReference var5);

    public WinNT.HRESULT GetRefTypeInfo(OaIdl.HREFTYPE var1, PointerByReference var2);

    public WinNT.HRESULT AddressOfMember(OaIdl.MEMBERID var1, OaIdl.INVOKEKIND var2, PointerByReference var3);

    public WinNT.HRESULT CreateInstance(IUnknown var1, Guid.REFIID var2, PointerByReference var3);

    public WinNT.HRESULT GetMops(OaIdl.MEMBERID var1, WTypes.BSTRByReference var2);

    public WinNT.HRESULT GetContainingTypeLib(PointerByReference var1, WinDef.UINTByReference var2);

    public void ReleaseTypeAttr(OaIdl.TYPEATTR var1);

    public void ReleaseFuncDesc(OaIdl.FUNCDESC var1);

    public void ReleaseVarDesc(OaIdl.VARDESC var1);
}

