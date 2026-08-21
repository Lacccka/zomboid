/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.OaIdl;
import com.sun.jna.platform.win32.Variant;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface OleAuto
extends StdCallLibrary {
    public static final OleAuto INSTANCE = Native.load("OleAut32", OleAuto.class, W32APIOptions.DEFAULT_OPTIONS);
    public static final int DISPATCH_METHOD = 1;
    public static final int DISPATCH_PROPERTYGET = 2;
    public static final int DISPATCH_PROPERTYPUT = 4;
    public static final int DISPATCH_PROPERTYPUTREF = 8;
    public static final int FADF_AUTO = 1;
    public static final int FADF_STATIC = 2;
    public static final int FADF_EMBEDDED = 4;
    public static final int FADF_FIXEDSIZE = 16;
    public static final int FADF_RECORD = 32;
    public static final int FADF_HAVEIID = 64;
    public static final int FADF_HAVEVARTYPE = 128;
    public static final int FADF_BSTR = 256;
    public static final int FADF_UNKNOWN = 512;
    public static final int FADF_DISPATCH = 1024;
    public static final int FADF_VARIANT = 2048;
    public static final int FADF_RESERVED = 61448;
    public static final short VARIANT_NOVALUEPROP = 1;
    public static final short VARIANT_ALPHABOOL = 2;
    public static final short VARIANT_NOUSEROVERRIDE = 4;
    public static final short VARIANT_CALENDAR_HIJRI = 8;
    public static final short VARIANT_LOCALBOOL = 16;
    public static final short VARIANT_CALENDAR_THAI = 32;
    public static final short VARIANT_CALENDAR_GREGORIAN = 64;
    public static final short VARIANT_USE_NLS = 128;

    public WTypes.BSTR SysAllocString(String var1);

    public void SysFreeString(WTypes.BSTR var1);

    public int SysStringByteLen(WTypes.BSTR var1);

    public int SysStringLen(WTypes.BSTR var1);

    public void VariantInit(Variant.VARIANT.ByReference var1);

    public void VariantInit(Variant.VARIANT var1);

    public WinNT.HRESULT VariantCopy(Pointer var1, Variant.VARIANT var2);

    public WinNT.HRESULT VariantClear(Variant.VARIANT var1);

    public WinNT.HRESULT VariantChangeType(Variant.VARIANT var1, Variant.VARIANT var2, short var3, WTypes.VARTYPE var4);

    public WinNT.HRESULT VariantChangeType(Variant.VARIANT.ByReference var1, Variant.VARIANT.ByReference var2, short var3, WTypes.VARTYPE var4);

    public OaIdl.SAFEARRAY.ByReference SafeArrayCreate(WTypes.VARTYPE var1, WinDef.UINT var2, OaIdl.SAFEARRAYBOUND[] var3);

    public WinNT.HRESULT SafeArrayPutElement(OaIdl.SAFEARRAY var1, WinDef.LONG[] var2, Pointer var3);

    public WinNT.HRESULT SafeArrayGetUBound(OaIdl.SAFEARRAY var1, WinDef.UINT var2, WinDef.LONGByReference var3);

    public WinNT.HRESULT SafeArrayGetLBound(OaIdl.SAFEARRAY var1, WinDef.UINT var2, WinDef.LONGByReference var3);

    public WinNT.HRESULT SafeArrayGetElement(OaIdl.SAFEARRAY var1, WinDef.LONG[] var2, Pointer var3);

    public WinNT.HRESULT SafeArrayPtrOfIndex(OaIdl.SAFEARRAY var1, WinDef.LONG[] var2, PointerByReference var3);

    public WinNT.HRESULT SafeArrayLock(OaIdl.SAFEARRAY var1);

    public WinNT.HRESULT SafeArrayUnlock(OaIdl.SAFEARRAY var1);

    public WinNT.HRESULT SafeArrayDestroy(OaIdl.SAFEARRAY var1);

    public WinNT.HRESULT SafeArrayRedim(OaIdl.SAFEARRAY var1, OaIdl.SAFEARRAYBOUND var2);

    public WinNT.HRESULT SafeArrayGetVartype(OaIdl.SAFEARRAY var1, WTypes.VARTYPEByReference var2);

    public WinDef.UINT SafeArrayGetDim(OaIdl.SAFEARRAY var1);

    public WinNT.HRESULT SafeArrayAccessData(OaIdl.SAFEARRAY var1, PointerByReference var2);

    public WinNT.HRESULT SafeArrayUnaccessData(OaIdl.SAFEARRAY var1);

    public WinDef.UINT SafeArrayGetElemsize(OaIdl.SAFEARRAY var1);

    public WinNT.HRESULT GetActiveObject(Guid.GUID var1, WinDef.PVOID var2, PointerByReference var3);

    public WinNT.HRESULT LoadRegTypeLib(Guid.GUID var1, int var2, int var3, WinDef.LCID var4, PointerByReference var5);

    public WinNT.HRESULT LoadTypeLib(String var1, PointerByReference var2);

    public int SystemTimeToVariantTime(WinBase.SYSTEMTIME var1, DoubleByReference var2);

    public int VariantTimeToSystemTime(double var1, WinBase.SYSTEMTIME var3);

    @Structure.FieldOrder(value={"rgvarg", "rgdispidNamedArgs", "cArgs", "cNamedArgs"})
    public static class DISPPARAMS
    extends Structure {
        public Variant.VariantArg.ByReference rgvarg;
        public Pointer rgdispidNamedArgs = Pointer.NULL;
        public WinDef.UINT cArgs = new WinDef.UINT(0L);
        public WinDef.UINT cNamedArgs = new WinDef.UINT(0L);

        public OaIdl.DISPID[] getRgdispidNamedArgs() {
            OaIdl.DISPID[] namedArgs = null;
            int count = this.cNamedArgs.intValue();
            if (this.rgdispidNamedArgs != null && count > 0) {
                int[] rawData = this.rgdispidNamedArgs.getIntArray(0L, count);
                namedArgs = new OaIdl.DISPID[count];
                for (int i = 0; i < count; ++i) {
                    namedArgs[i] = new OaIdl.DISPID(rawData[i]);
                }
            } else {
                namedArgs = new OaIdl.DISPID[]{};
            }
            return namedArgs;
        }

        public void setRgdispidNamedArgs(OaIdl.DISPID[] namedArgs) {
            if (namedArgs == null) {
                namedArgs = new OaIdl.DISPID[]{};
            }
            this.cNamedArgs = new WinDef.UINT((long)namedArgs.length);
            this.rgdispidNamedArgs = new Memory(OaIdl.DISPID.SIZE * namedArgs.length);
            int[] rawData = new int[namedArgs.length];
            for (int i = 0; i < rawData.length; ++i) {
                rawData[i] = namedArgs[i].intValue();
            }
            this.rgdispidNamedArgs.write(0L, rawData, 0, namedArgs.length);
        }

        public Variant.VARIANT[] getArgs() {
            if (this.rgvarg != null) {
                this.rgvarg.setArraySize(this.cArgs.intValue());
                return this.rgvarg.variantArg;
            }
            return new Variant.VARIANT[0];
        }

        public void setArgs(Variant.VARIANT[] arguments) {
            if (arguments == null) {
                arguments = new Variant.VARIANT[]{};
            }
            this.rgvarg = new Variant.VariantArg.ByReference(arguments);
            this.cArgs = new WinDef.UINT((long)arguments.length);
        }

        public DISPPARAMS() {
        }

        public DISPPARAMS(Pointer memory) {
            super(memory);
            this.read();
        }

        public static class ByReference
        extends DISPPARAMS
        implements Structure.ByReference {
        }
    }
}

