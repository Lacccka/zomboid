/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32.COM;

import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.OaIdl;
import com.sun.jna.platform.win32.OaIdlUtil;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.OleAuto;
import com.sun.jna.platform.win32.Variant;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public interface Wbemcli {
    public static final int WBEM_FLAG_RETURN_WBEM_COMPLETE = 0;
    public static final int WBEM_FLAG_RETURN_IMMEDIATELY = 16;
    public static final int WBEM_FLAG_FORWARD_ONLY = 32;
    public static final int WBEM_FLAG_NO_ERROR_OBJECT = 64;
    public static final int WBEM_FLAG_SEND_STATUS = 128;
    public static final int WBEM_FLAG_ENSURE_LOCATABLE = 256;
    public static final int WBEM_FLAG_DIRECT_READ = 512;
    public static final int WBEM_MASK_RESERVED_FLAGS = 126976;
    public static final int WBEM_FLAG_USE_AMENDED_QUALIFIERS = 131072;
    public static final int WBEM_FLAG_STRONG_VALIDATION = 0x100000;
    public static final int WBEM_INFINITE = -1;
    public static final int WBEM_S_NO_ERROR = 0;
    public static final int WBEM_S_FALSE = 1;
    public static final int WBEM_S_TIMEDOUT = 262148;
    public static final int WBEM_S_NO_MORE_DATA = 262149;
    public static final int WBEM_E_INVALID_NAMESPACE = -2147217394;
    public static final int WBEM_E_INVALID_CLASS = -2147217392;
    public static final int WBEM_E_INVALID_QUERY = -2147217385;
    public static final int CIM_ILLEGAL = 4095;
    public static final int CIM_EMPTY = 0;
    public static final int CIM_SINT8 = 16;
    public static final int CIM_UINT8 = 17;
    public static final int CIM_SINT16 = 2;
    public static final int CIM_UINT16 = 18;
    public static final int CIM_SINT32 = 3;
    public static final int CIM_UINT32 = 19;
    public static final int CIM_SINT64 = 20;
    public static final int CIM_UINT64 = 21;
    public static final int CIM_REAL32 = 4;
    public static final int CIM_REAL64 = 5;
    public static final int CIM_BOOLEAN = 11;
    public static final int CIM_STRING = 8;
    public static final int CIM_DATETIME = 101;
    public static final int CIM_REFERENCE = 102;
    public static final int CIM_CHAR16 = 103;
    public static final int CIM_OBJECT = 13;
    public static final int CIM_FLAG_ARRAY = 8192;

    public static class IWbemContext
    extends Unknown {
        public static final Guid.CLSID CLSID_WbemContext = new Guid.CLSID("674B6698-EE92-11D0-AD71-00C04FD8FDFF");
        public static final Guid.GUID IID_IWbemContext = new Guid.GUID("44aca674-e8fc-11d0-a07c-00c04fb68820");

        public IWbemContext() {
        }

        public static IWbemContext create() {
            PointerByReference pbr = new PointerByReference();
            WinNT.HRESULT hres = Ole32.INSTANCE.CoCreateInstance(CLSID_WbemContext, null, 1, IID_IWbemContext, pbr);
            if (COMUtils.FAILED(hres)) {
                return null;
            }
            return new IWbemContext(pbr.getValue());
        }

        public IWbemContext(Pointer pvInstance) {
            super(pvInstance);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void SetValue(String wszName, int lFlag, Variant.VARIANT pValue) {
            WTypes.BSTR wszNameBSTR = OleAuto.INSTANCE.SysAllocString(wszName);
            try {
                WinNT.HRESULT res = (WinNT.HRESULT)this._invokeNativeObject(8, new Object[]{this.getPointer(), wszNameBSTR, lFlag, pValue}, WinNT.HRESULT.class);
                COMUtils.checkRC(res);
            }
            finally {
                OleAuto.INSTANCE.SysFreeString(wszNameBSTR);
            }
        }

        public void SetValue(String wszName, int lFlag, boolean pValue) {
            Variant.VARIANT aVariant = new Variant.VARIANT();
            aVariant.setValue(11, (Object)(pValue ? Variant.VARIANT_TRUE : Variant.VARIANT_FALSE));
            this.SetValue(wszName, lFlag, aVariant);
            OleAuto.INSTANCE.VariantClear(aVariant);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void SetValue(String wszName, int lFlag, String pValue) {
            Variant.VARIANT aVariant = new Variant.VARIANT();
            WTypes.BSTR strValue = OleAuto.INSTANCE.SysAllocString(pValue);
            try {
                aVariant.setValue(30, (Object)strValue);
                this.SetValue(wszName, lFlag, aVariant);
            }
            finally {
                OleAuto.INSTANCE.SysFreeString(strValue);
            }
        }
    }

    public static class IWbemServices
    extends Unknown {
        public IWbemServices() {
        }

        public IWbemServices(Pointer pvInstance) {
            super(pvInstance);
        }

        public WinNT.HRESULT ExecMethod(WTypes.BSTR strObjectPath, WTypes.BSTR strMethodName, int lFlags, IWbemContext pCtx, Pointer pInParams, PointerByReference ppOutParams, PointerByReference ppCallResult) {
            return (WinNT.HRESULT)this._invokeNativeObject(24, new Object[]{this.getPointer(), strObjectPath, strMethodName, lFlags, pCtx, pInParams, ppOutParams, ppCallResult}, WinNT.HRESULT.class);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public IWbemClassObject ExecMethod(String strObjectPath, String strMethodName, int lFlags, IWbemContext pCtx, IWbemClassObject inParams) {
            WTypes.BSTR strObjectPathBSTR = OleAuto.INSTANCE.SysAllocString(strObjectPath);
            WTypes.BSTR strMethodNameBSTR = OleAuto.INSTANCE.SysAllocString(strMethodName);
            try {
                PointerByReference ppOutParams = new PointerByReference();
                WinNT.HRESULT res = this.ExecMethod(strObjectPathBSTR, strMethodNameBSTR, lFlags, pCtx, inParams.getPointer(), ppOutParams, null);
                COMUtils.checkRC(res);
                IWbemClassObject iWbemClassObject = new IWbemClassObject(ppOutParams.getValue());
                return iWbemClassObject;
            }
            finally {
                OleAuto.INSTANCE.SysFreeString(strObjectPathBSTR);
                OleAuto.INSTANCE.SysFreeString(strMethodNameBSTR);
            }
        }

        public WinNT.HRESULT ExecQuery(WTypes.BSTR strQueryLanguage, WTypes.BSTR strQuery, int lFlags, IWbemContext pCtx, PointerByReference ppEnum) {
            return (WinNT.HRESULT)this._invokeNativeObject(20, new Object[]{this.getPointer(), strQueryLanguage, strQuery, lFlags, pCtx, ppEnum}, WinNT.HRESULT.class);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public IEnumWbemClassObject ExecQuery(String strQueryLanguage, String strQuery, int lFlags, IWbemContext pCtx) {
            WTypes.BSTR strQueryLanguageBSTR = OleAuto.INSTANCE.SysAllocString(strQueryLanguage);
            WTypes.BSTR strQueryBSTR = OleAuto.INSTANCE.SysAllocString(strQuery);
            try {
                PointerByReference pbr = new PointerByReference();
                WinNT.HRESULT res = this.ExecQuery(strQueryLanguageBSTR, strQueryBSTR, lFlags, pCtx, pbr);
                COMUtils.checkRC(res);
                IEnumWbemClassObject iEnumWbemClassObject = new IEnumWbemClassObject(pbr.getValue());
                return iEnumWbemClassObject;
            }
            finally {
                OleAuto.INSTANCE.SysFreeString(strQueryLanguageBSTR);
                OleAuto.INSTANCE.SysFreeString(strQueryBSTR);
            }
        }

        public WinNT.HRESULT GetObject(WTypes.BSTR strObjectPath, int lFlags, IWbemContext pCtx, PointerByReference ppObject, PointerByReference ppCallResult) {
            return (WinNT.HRESULT)this._invokeNativeObject(6, new Object[]{this.getPointer(), strObjectPath, lFlags, pCtx, ppObject, ppCallResult}, WinNT.HRESULT.class);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public IWbemClassObject GetObject(String strObjectPath, int lFlags, IWbemContext pCtx) {
            WTypes.BSTR strObjectPathBSTR = OleAuto.INSTANCE.SysAllocString(strObjectPath);
            try {
                PointerByReference ppObject = new PointerByReference();
                WinNT.HRESULT res = this.GetObject(strObjectPathBSTR, lFlags, pCtx, ppObject, null);
                COMUtils.checkRC(res);
                IWbemClassObject iWbemClassObject = new IWbemClassObject(ppObject.getValue());
                return iWbemClassObject;
            }
            finally {
                OleAuto.INSTANCE.SysFreeString(strObjectPathBSTR);
            }
        }
    }

    public static class IWbemLocator
    extends Unknown {
        public static final Guid.CLSID CLSID_WbemLocator = new Guid.CLSID("4590f811-1d3a-11d0-891f-00aa004b2e24");
        public static final Guid.GUID IID_IWbemLocator = new Guid.GUID("dc12a687-737f-11cf-884d-00aa004b2e24");

        public IWbemLocator() {
        }

        private IWbemLocator(Pointer pvInstance) {
            super(pvInstance);
        }

        public static IWbemLocator create() {
            PointerByReference pbr = new PointerByReference();
            WinNT.HRESULT hres = Ole32.INSTANCE.CoCreateInstance(CLSID_WbemLocator, null, 1, IID_IWbemLocator, pbr);
            if (COMUtils.FAILED(hres)) {
                return null;
            }
            return new IWbemLocator(pbr.getValue());
        }

        public WinNT.HRESULT ConnectServer(WTypes.BSTR strNetworkResource, WTypes.BSTR strUser, WTypes.BSTR strPassword, WTypes.BSTR strLocale, int lSecurityFlags, WTypes.BSTR strAuthority, IWbemContext pCtx, PointerByReference ppNamespace) {
            return (WinNT.HRESULT)this._invokeNativeObject(3, new Object[]{this.getPointer(), strNetworkResource, strUser, strPassword, strLocale, lSecurityFlags, strAuthority, pCtx, ppNamespace}, WinNT.HRESULT.class);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public IWbemServices ConnectServer(String strNetworkResource, String strUser, String strPassword, String strLocale, int lSecurityFlags, String strAuthority, IWbemContext pCtx) {
            WTypes.BSTR strNetworkResourceBSTR = OleAuto.INSTANCE.SysAllocString(strNetworkResource);
            WTypes.BSTR strUserBSTR = OleAuto.INSTANCE.SysAllocString(strUser);
            WTypes.BSTR strPasswordBSTR = OleAuto.INSTANCE.SysAllocString(strPassword);
            WTypes.BSTR strLocaleBSTR = OleAuto.INSTANCE.SysAllocString(strLocale);
            WTypes.BSTR strAuthorityBSTR = OleAuto.INSTANCE.SysAllocString(strAuthority);
            PointerByReference pbr = new PointerByReference();
            try {
                WinNT.HRESULT result = this.ConnectServer(strNetworkResourceBSTR, strUserBSTR, strPasswordBSTR, strLocaleBSTR, lSecurityFlags, strAuthorityBSTR, pCtx, pbr);
                COMUtils.checkRC(result);
                IWbemServices iWbemServices = new IWbemServices(pbr.getValue());
                return iWbemServices;
            }
            finally {
                OleAuto.INSTANCE.SysFreeString(strNetworkResourceBSTR);
                OleAuto.INSTANCE.SysFreeString(strUserBSTR);
                OleAuto.INSTANCE.SysFreeString(strPasswordBSTR);
                OleAuto.INSTANCE.SysFreeString(strLocaleBSTR);
                OleAuto.INSTANCE.SysFreeString(strAuthorityBSTR);
            }
        }
    }

    public static class IEnumWbemClassObject
    extends Unknown {
        public IEnumWbemClassObject() {
        }

        public IEnumWbemClassObject(Pointer pvInstance) {
            super(pvInstance);
        }

        public WinNT.HRESULT Next(int lTimeOut, int uCount, Pointer[] ppObjects, IntByReference puReturned) {
            return (WinNT.HRESULT)this._invokeNativeObject(4, new Object[]{this.getPointer(), lTimeOut, uCount, ppObjects, puReturned}, WinNT.HRESULT.class);
        }

        public IWbemClassObject[] Next(int lTimeOut, int uCount) {
            Pointer[] resultArray = new Pointer[uCount];
            IntByReference resultCount = new IntByReference();
            WinNT.HRESULT result = this.Next(lTimeOut, uCount, resultArray, resultCount);
            COMUtils.checkRC(result);
            IWbemClassObject[] returnValue = new IWbemClassObject[resultCount.getValue()];
            for (int i = 0; i < resultCount.getValue(); ++i) {
                returnValue[i] = new IWbemClassObject(resultArray[i]);
            }
            return returnValue;
        }
    }

    public static class IWbemQualifierSet
    extends Unknown {
        public IWbemQualifierSet(Pointer pvInstance) {
            super(pvInstance);
        }

        public WinNT.HRESULT Get(WString wszName, int lFlags, Variant.VARIANT.ByReference pVal, IntByReference plFlavor) {
            return (WinNT.HRESULT)this._invokeNativeObject(3, new Object[]{this.getPointer(), wszName, lFlags, pVal, plFlavor}, WinNT.HRESULT.class);
        }

        public String Get(String wszName) {
            WString wszNameStr = new WString(wszName);
            Variant.VARIANT.ByReference pQualifierVal = new Variant.VARIANT.ByReference();
            WinNT.HRESULT hres = this.Get(wszNameStr, 0, pQualifierVal, null);
            if (hres.intValue() == -2147217406) {
                return null;
            }
            int qualifierInt = pQualifierVal.getVarType().intValue();
            switch (qualifierInt) {
                case 11: {
                    return String.valueOf(pQualifierVal.booleanValue());
                }
                case 8: {
                    return pQualifierVal.stringValue();
                }
            }
            return null;
        }

        public WinNT.HRESULT GetNames(int lFlags, PointerByReference pNames) {
            return (WinNT.HRESULT)this._invokeNativeObject(6, new Object[]{this.getPointer(), lFlags, pNames}, WinNT.HRESULT.class);
        }

        public String[] GetNames() {
            PointerByReference pbr = new PointerByReference();
            COMUtils.checkRC(this.GetNames(0, pbr));
            Object[] nameObjects = (Object[])OaIdlUtil.toPrimitiveArray(new OaIdl.SAFEARRAY(pbr.getValue()), true);
            String[] qualifierNames = new String[nameObjects.length];
            for (int i = 0; i < nameObjects.length; ++i) {
                qualifierNames[i] = (String)nameObjects[i];
            }
            return qualifierNames;
        }
    }

    public static class IWbemClassObject
    extends Unknown {
        public IWbemClassObject() {
        }

        public IWbemClassObject(Pointer pvInstance) {
            super(pvInstance);
        }

        public WinNT.HRESULT Get(WString wszName, int lFlags, Variant.VARIANT.ByReference pVal, IntByReference pType, IntByReference plFlavor) {
            return (WinNT.HRESULT)this._invokeNativeObject(4, new Object[]{this.getPointer(), wszName, lFlags, pVal, pType, plFlavor}, WinNT.HRESULT.class);
        }

        public WinNT.HRESULT Get(String wszName, int lFlags, Variant.VARIANT.ByReference pVal, IntByReference pType, IntByReference plFlavor) {
            return this.Get(wszName == null ? null : new WString(wszName), lFlags, pVal, pType, plFlavor);
        }

        public WinNT.HRESULT GetMethod(String wszName, int lFlags, PointerByReference ppInSignature, PointerByReference ppOutSignature) {
            return this.GetMethod(wszName == null ? null : new WString(wszName), lFlags, ppInSignature, ppOutSignature);
        }

        public WinNT.HRESULT GetMethod(WString wszName, int lFlags, PointerByReference ppInSignature, PointerByReference ppOutSignature) {
            return (WinNT.HRESULT)this._invokeNativeObject(19, new Object[]{this.getPointer(), wszName, lFlags, ppInSignature, ppOutSignature}, WinNT.HRESULT.class);
        }

        public IWbemClassObject GetMethod(String wszName) {
            PointerByReference ppInSignature = new PointerByReference();
            WinNT.HRESULT res = this.GetMethod(wszName, 0, ppInSignature, null);
            COMUtils.checkRC(res);
            return new IWbemClassObject(ppInSignature.getValue());
        }

        public WinNT.HRESULT GetNames(String wszQualifierName, int lFlags, Variant.VARIANT.ByReference pQualifierVal, PointerByReference pNames) {
            return this.GetNames(wszQualifierName == null ? null : new WString(wszQualifierName), lFlags, pQualifierVal, pNames);
        }

        public WinNT.HRESULT GetNames(WString wszQualifierName, int lFlags, Variant.VARIANT.ByReference pQualifierVal, PointerByReference pNames) {
            return (WinNT.HRESULT)this._invokeNativeObject(7, new Object[]{this.getPointer(), wszQualifierName, lFlags, pQualifierVal, pNames}, WinNT.HRESULT.class);
        }

        public String[] GetNames(String wszQualifierName, int lFlags, Variant.VARIANT.ByReference pQualifierVal) {
            PointerByReference pbr = new PointerByReference();
            COMUtils.checkRC(this.GetNames(wszQualifierName, lFlags, pQualifierVal, pbr));
            Object[] nameObjects = (Object[])OaIdlUtil.toPrimitiveArray(new OaIdl.SAFEARRAY(pbr.getValue()), true);
            String[] names = new String[nameObjects.length];
            for (int i = 0; i < nameObjects.length; ++i) {
                names[i] = (String)nameObjects[i];
            }
            return names;
        }

        public WinNT.HRESULT GetQualifierSet(PointerByReference ppQualSet) {
            return (WinNT.HRESULT)this._invokeNativeObject(3, new Object[]{this.getPointer(), ppQualSet}, WinNT.HRESULT.class);
        }

        public IWbemQualifierSet GetQualifierSet() {
            PointerByReference ppQualSet = new PointerByReference();
            WinNT.HRESULT hr = this.GetQualifierSet(ppQualSet);
            COMUtils.checkRC(hr);
            IWbemQualifierSet qualifier = new IWbemQualifierSet(ppQualSet.getValue());
            return qualifier;
        }

        public WinNT.HRESULT GetPropertyQualifierSet(WString wszProperty, PointerByReference ppQualSet) {
            return (WinNT.HRESULT)this._invokeNativeObject(11, new Object[]{this.getPointer(), wszProperty, ppQualSet}, WinNT.HRESULT.class);
        }

        public IWbemQualifierSet GetPropertyQualifierSet(String strProperty) {
            WString wszProperty = new WString(strProperty);
            PointerByReference ppQualSet = new PointerByReference();
            COMUtils.checkRC(this.GetPropertyQualifierSet(wszProperty, ppQualSet));
            IWbemQualifierSet qualifier = new IWbemQualifierSet(ppQualSet.getValue());
            return qualifier;
        }

        public WinNT.HRESULT Put(String wszName, int lFlags, Variant.VARIANT pVal, int Type2) {
            return this.Put(wszName == null ? null : new WString(wszName), lFlags, pVal, Type2);
        }

        public WinNT.HRESULT Put(WString wszName, int lFlags, Variant.VARIANT pVal, int Type2) {
            return (WinNT.HRESULT)this._invokeNativeObject(5, new Object[]{this.getPointer(), wszName, lFlags, pVal, Type2}, WinNT.HRESULT.class);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void Put(String wszName, String pValue) {
            Variant.VARIANT aVariant = new Variant.VARIANT();
            WTypes.BSTR strValue = OleAuto.INSTANCE.SysAllocString(pValue);
            try {
                aVariant.setValue(8, (Object)strValue);
                WinNT.HRESULT res = this.Put(wszName, 0, aVariant, 0);
                COMUtils.checkRC(res);
            }
            finally {
                OleAuto.INSTANCE.VariantClear(aVariant);
            }
        }

        public WinNT.HRESULT SpawnInstance(int lFlags, PointerByReference ppNewInstance) {
            return (WinNT.HRESULT)this._invokeNativeObject(15, new Object[]{this.getPointer(), lFlags, ppNewInstance}, WinNT.HRESULT.class);
        }

        public IWbemClassObject SpawnInstance() {
            PointerByReference ppNewInstance = new PointerByReference();
            WinNT.HRESULT res = this.SpawnInstance(0, ppNewInstance);
            COMUtils.checkRC(res);
            return new IWbemClassObject(ppNewInstance.getValue());
        }
    }

    public static interface WBEM_CONDITION_FLAG_TYPE {
        public static final int WBEM_FLAG_ALWAYS = 0;
        public static final int WBEM_FLAG_ONLY_IF_TRUE = 1;
        public static final int WBEM_FLAG_ONLY_IF_FALSE = 2;
        public static final int WBEM_FLAG_ONLY_IF_IDENTICAL = 3;
        public static final int WBEM_MASK_PRIMARY_CONDITION = 3;
        public static final int WBEM_FLAG_KEYS_ONLY = 4;
        public static final int WBEM_FLAG_REFS_ONLY = 8;
        public static final int WBEM_FLAG_LOCAL_ONLY = 16;
        public static final int WBEM_FLAG_PROPAGATED_ONLY = 32;
        public static final int WBEM_FLAG_SYSTEM_ONLY = 48;
        public static final int WBEM_FLAG_NONSYSTEM_ONLY = 64;
        public static final int WBEM_MASK_CONDITION_ORIGIN = 112;
        public static final int WBEM_FLAG_CLASS_OVERRIDES_ONLY = 256;
        public static final int WBEM_FLAG_CLASS_LOCAL_AND_OVERRIDES = 512;
        public static final int WBEM_MASK_CLASS_CONDITION = 768;
    }
}

