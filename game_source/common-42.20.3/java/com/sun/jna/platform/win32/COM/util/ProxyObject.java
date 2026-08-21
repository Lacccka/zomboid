/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32.COM.util;

import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.internal.ReflectionUtils;
import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.ConnectionPoint;
import com.sun.jna.platform.win32.COM.ConnectionPointContainer;
import com.sun.jna.platform.win32.COM.Dispatch;
import com.sun.jna.platform.win32.COM.IDispatch;
import com.sun.jna.platform.win32.COM.IDispatchCallback;
import com.sun.jna.platform.win32.COM.util.ComEventCallbackCookie;
import com.sun.jna.platform.win32.COM.util.Convert;
import com.sun.jna.platform.win32.COM.util.IComEventCallbackCookie;
import com.sun.jna.platform.win32.COM.util.IComEventCallbackListener;
import com.sun.jna.platform.win32.COM.util.IConnectionPoint;
import com.sun.jna.platform.win32.COM.util.IConnectionPointContainer;
import com.sun.jna.platform.win32.COM.util.IRawDispatchHandle;
import com.sun.jna.platform.win32.COM.util.IUnknown;
import com.sun.jna.platform.win32.COM.util.ObjectFactory;
import com.sun.jna.platform.win32.COM.util.annotation.ComInterface;
import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;
import com.sun.jna.platform.win32.COM.util.annotation.ComProperty;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.OaIdl;
import com.sun.jna.platform.win32.OleAuto;
import com.sun.jna.platform.win32.Variant;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyObject
implements InvocationHandler,
com.sun.jna.platform.win32.COM.util.IDispatch,
IRawDispatchHandle,
IConnectionPoint {
    private long unknownId = -1L;
    private final Class<?> theInterface;
    private final ObjectFactory factory;
    private final IDispatch rawDispatch;

    public ProxyObject(Class<?> theInterface, IDispatch rawDispatch, ObjectFactory factory2) {
        this.rawDispatch = rawDispatch;
        this.theInterface = theInterface;
        this.factory = factory2;
        int n = this.rawDispatch.AddRef();
        this.getUnknownId();
        factory2.register(this);
    }

    private long getUnknownId() {
        assert (COMUtils.comIsInitialized()) : "COM not initialized";
        if (-1L == this.unknownId) {
            try {
                PointerByReference ppvObject = new PointerByReference();
                Thread current = Thread.currentThread();
                String tn = current.getName();
                Guid.IID iid = com.sun.jna.platform.win32.COM.IUnknown.IID_IUNKNOWN;
                WinNT.HRESULT hr = this.getRawDispatch().QueryInterface(new Guid.REFIID(iid), ppvObject);
                if (!WinNT.S_OK.equals(hr)) {
                    String formatMessageFromHR = Kernel32Util.formatMessage(hr);
                    throw new COMException("getUnknownId: " + formatMessageFromHR, hr);
                }
                Dispatch dispatch = new Dispatch(ppvObject.getValue());
                this.unknownId = Pointer.nativeValue(dispatch.getPointer());
                int n = dispatch.Release();
            }
            catch (RuntimeException e) {
                if (e instanceof COMException) {
                    throw e;
                }
                throw new COMException("Error occured when trying get Unknown Id ", e);
            }
        }
        return this.unknownId;
    }

    protected void finalize() throws Throwable {
        this.dispose();
        super.finalize();
    }

    public synchronized void dispose() {
        if (((Dispatch)this.rawDispatch).getPointer() != Pointer.NULL) {
            this.rawDispatch.Release();
            ((Dispatch)this.rawDispatch).setPointer(Pointer.NULL);
            this.factory.unregister(this);
        }
    }

    @Override
    public IDispatch getRawDispatch() {
        return this.rawDispatch;
    }

    public boolean equals(Object arg) {
        if (null == arg) {
            return false;
        }
        if (arg instanceof ProxyObject) {
            ProxyObject other = (ProxyObject)arg;
            return this.getUnknownId() == other.getUnknownId();
        }
        if (Proxy.isProxyClass(arg.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(arg);
            if (handler instanceof ProxyObject) {
                try {
                    ProxyObject other = (ProxyObject)handler;
                    return this.getUnknownId() == other.getUnknownId();
                }
                catch (Exception e) {
                    return false;
                }
            }
            return false;
        }
        return false;
    }

    public int hashCode() {
        long id = this.getUnknownId();
        return (int)((id >>> 32) + (id & 0xFFFFFFFFL));
    }

    public String toString() {
        return this.theInterface.getName() + "{unk=" + this.hashCode() + "}";
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args2) throws Throwable {
        boolean declaredAsInterface;
        boolean bl = declaredAsInterface = method.getAnnotation(ComMethod.class) != null || method.getAnnotation(ComProperty.class) != null;
        if (!declaredAsInterface && (method.getDeclaringClass().equals(Object.class) || method.getDeclaringClass().equals(IRawDispatchHandle.class) || method.getDeclaringClass().equals(IUnknown.class) || method.getDeclaringClass().equals(com.sun.jna.platform.win32.COM.util.IDispatch.class) || method.getDeclaringClass().equals(IConnectionPoint.class))) {
            try {
                return method.invoke((Object)this, args2);
            }
            catch (InvocationTargetException ex) {
                throw ex.getCause();
            }
        }
        if (!declaredAsInterface && ReflectionUtils.isDefault(method)) {
            Object methodHandle = ReflectionUtils.getMethodHandle(method);
            return ReflectionUtils.invokeDefaultMethod(proxy, methodHandle, args2);
        }
        Class<?> returnType = method.getReturnType();
        boolean isVoid = Void.TYPE.equals(returnType);
        ComProperty prop = method.getAnnotation(ComProperty.class);
        if (null != prop) {
            int dispId = prop.dispId();
            Object[] fullLengthArgs = this.unfoldWhenVarargs(method, args2);
            if (isVoid) {
                if (dispId != -1) {
                    this.setProperty(new OaIdl.DISPID(dispId), fullLengthArgs);
                    return null;
                }
                String propName = this.getMutatorName(method, prop);
                this.setProperty(propName, fullLengthArgs);
                return null;
            }
            if (dispId != -1) {
                return this.getProperty(returnType, new OaIdl.DISPID(dispId), args2);
            }
            String propName = this.getAccessorName(method, prop);
            return this.getProperty(returnType, propName, args2);
        }
        ComMethod meth = method.getAnnotation(ComMethod.class);
        if (null != meth) {
            Object[] fullLengthArgs = this.unfoldWhenVarargs(method, args2);
            int dispId = meth.dispId();
            if (dispId != -1) {
                return this.invokeMethod(returnType, new OaIdl.DISPID(dispId), fullLengthArgs);
            }
            String methName = this.getMethodName(method, meth);
            return this.invokeMethod(returnType, methName, fullLengthArgs);
        }
        return null;
    }

    private ConnectionPoint fetchRawConnectionPoint(Guid.IID iid) {
        assert (COMUtils.comIsInitialized()) : "COM not initialized";
        IConnectionPointContainer cpc = this.queryInterface(IConnectionPointContainer.class);
        Dispatch rawCpcDispatch = (Dispatch)cpc.getRawDispatch();
        ConnectionPointContainer rawCpc = new ConnectionPointContainer(rawCpcDispatch.getPointer());
        Guid.REFIID adviseRiid = new Guid.REFIID(iid.getPointer());
        PointerByReference ppCp = new PointerByReference();
        WinNT.HRESULT hr = rawCpc.FindConnectionPoint(adviseRiid, ppCp);
        COMUtils.checkRC(hr);
        ConnectionPoint rawCp = new ConnectionPoint(ppCp.getValue());
        return rawCp;
    }

    @Override
    public IComEventCallbackCookie advise(Class<?> comEventCallbackInterface, IComEventCallbackListener comEventCallbackListener) throws COMException {
        assert (COMUtils.comIsInitialized()) : "COM not initialized";
        try {
            ComInterface comInterfaceAnnotation = comEventCallbackInterface.getAnnotation(ComInterface.class);
            if (null == comInterfaceAnnotation) {
                throw new COMException("advise: Interface must define a value for either iid via the ComInterface annotation");
            }
            Guid.IID iid = this.getIID(comInterfaceAnnotation);
            ConnectionPoint rawCp = this.fetchRawConnectionPoint(iid);
            IDispatchCallback rawListener = this.factory.createDispatchCallback(comEventCallbackInterface, comEventCallbackListener);
            comEventCallbackListener.setDispatchCallbackListener(rawListener);
            WinDef.DWORDByReference pdwCookie = new WinDef.DWORDByReference();
            WinNT.HRESULT hr = rawCp.Advise(rawListener, pdwCookie);
            int n = rawCp.Release();
            COMUtils.checkRC(hr);
            return new ComEventCallbackCookie(pdwCookie.getValue());
        }
        catch (RuntimeException e) {
            if (e instanceof COMException) {
                throw e;
            }
            throw new COMException("Error occured in advise when trying to connect the listener " + comEventCallbackListener, e);
        }
    }

    @Override
    public void unadvise(Class<?> comEventCallbackInterface, IComEventCallbackCookie cookie) throws COMException {
        assert (COMUtils.comIsInitialized()) : "COM not initialized";
        try {
            ComInterface comInterfaceAnnotation = comEventCallbackInterface.getAnnotation(ComInterface.class);
            if (null == comInterfaceAnnotation) {
                throw new COMException("unadvise: Interface must define a value for iid via the ComInterface annotation");
            }
            Guid.IID iid = this.getIID(comInterfaceAnnotation);
            ConnectionPoint rawCp = this.fetchRawConnectionPoint(iid);
            WinNT.HRESULT hr = rawCp.Unadvise(((ComEventCallbackCookie)cookie).getValue());
            rawCp.Release();
            COMUtils.checkRC(hr);
        }
        catch (RuntimeException e) {
            if (e instanceof COMException) {
                throw e;
            }
            throw new COMException("Error occured in unadvise when trying to disconnect the listener from " + this, e);
        }
    }

    @Override
    public <T> void setProperty(String name, T value) {
        OaIdl.DISPID dispID = this.resolveDispId(this.getRawDispatch(), name);
        this.setProperty(dispID, value);
    }

    @Override
    public <T> void setProperty(OaIdl.DISPID dispId, T value) {
        assert (COMUtils.comIsInitialized()) : "COM not initialized";
        Variant.VARIANT v = Convert.toVariant(value);
        WinNT.HRESULT hr = this.oleMethod(4, null, this.getRawDispatch(), dispId, v);
        Convert.free(v, value);
        COMUtils.checkRC(hr);
    }

    private void setProperty(String name, Object ... args2) {
        assert (COMUtils.comIsInitialized()) : "COM not initialized";
        OaIdl.DISPID dispID = this.resolveDispId(this.getRawDispatch(), name);
        this.setProperty(dispID, args2);
    }

    private void setProperty(OaIdl.DISPID dispID, Object ... args2) {
        assert (COMUtils.comIsInitialized()) : "COM not initialized";
        Variant.VARIANT[] vargs = null == args2 ? new Variant.VARIANT[]{} : new Variant.VARIANT[args2.length];
        for (int i = 0; i < vargs.length; ++i) {
            vargs[i] = Convert.toVariant(args2[i]);
        }
        WinNT.HRESULT hr = this.oleMethod(4, null, this.getRawDispatch(), dispID, vargs);
        for (int i = 0; i < vargs.length; ++i) {
            Convert.free(vargs[i], args2[i]);
        }
        COMUtils.checkRC(hr);
    }

    @Override
    public <T> T getProperty(Class<T> returnType, String name, Object ... args2) {
        OaIdl.DISPID dispID = this.resolveDispId(this.getRawDispatch(), name);
        return this.getProperty(returnType, dispID, args2);
    }

    @Override
    public <T> T getProperty(Class<T> returnType, OaIdl.DISPID dispID, Object ... args2) {
        Variant.VARIANT[] vargs = null == args2 ? new Variant.VARIANT[]{} : new Variant.VARIANT[args2.length];
        for (int i = 0; i < vargs.length; ++i) {
            vargs[i] = Convert.toVariant(args2[i]);
        }
        Variant.VARIANT.ByReference result = new Variant.VARIANT.ByReference();
        WinNT.HRESULT hr = this.oleMethod(2, result, this.getRawDispatch(), dispID, vargs);
        for (int i = 0; i < vargs.length; ++i) {
            Convert.free(vargs[i], args2[i]);
        }
        COMUtils.checkRC(hr);
        return (T)Convert.toJavaObject(result, returnType, this.factory, false, true);
    }

    @Override
    public <T> T invokeMethod(Class<T> returnType, String name, Object ... args2) {
        OaIdl.DISPID dispID = this.resolveDispId(this.getRawDispatch(), name);
        return this.invokeMethod(returnType, dispID, args2);
    }

    @Override
    public <T> T invokeMethod(Class<T> returnType, OaIdl.DISPID dispID, Object ... args2) {
        assert (COMUtils.comIsInitialized()) : "COM not initialized";
        Variant.VARIANT[] vargs = null == args2 ? new Variant.VARIANT[]{} : new Variant.VARIANT[args2.length];
        for (int i = 0; i < vargs.length; ++i) {
            vargs[i] = Convert.toVariant(args2[i]);
        }
        Variant.VARIANT.ByReference result = new Variant.VARIANT.ByReference();
        WinNT.HRESULT hr = this.oleMethod(1, result, this.getRawDispatch(), dispID, vargs);
        for (int i = 0; i < vargs.length; ++i) {
            Convert.free(vargs[i], args2[i]);
        }
        COMUtils.checkRC(hr);
        return (T)Convert.toJavaObject(result, returnType, this.factory, false, true);
    }

    private Object[] unfoldWhenVarargs(Method method, Object[] argParams) {
        if (null == argParams) {
            return null;
        }
        if (argParams.length == 0 || !method.isVarArgs() || !(argParams[argParams.length - 1] instanceof Object[])) {
            return argParams;
        }
        Object[] varargs = (Object[])argParams[argParams.length - 1];
        Object[] args2 = new Object[argParams.length - 1 + varargs.length];
        System.arraycopy(argParams, 0, args2, 0, argParams.length - 1);
        System.arraycopy(varargs, 0, args2, argParams.length - 1, varargs.length);
        return args2;
    }

    @Override
    public <T> T queryInterface(Class<T> comInterface) throws COMException {
        assert (COMUtils.comIsInitialized()) : "COM not initialized";
        try {
            ComInterface comInterfaceAnnotation = comInterface.getAnnotation(ComInterface.class);
            if (null == comInterfaceAnnotation) {
                throw new COMException("queryInterface: Interface must define a value for iid via the ComInterface annotation");
            }
            Guid.IID iid = this.getIID(comInterfaceAnnotation);
            PointerByReference ppvObject = new PointerByReference();
            WinNT.HRESULT hr = this.getRawDispatch().QueryInterface(new Guid.REFIID(iid), ppvObject);
            if (WinNT.S_OK.equals(hr)) {
                Dispatch dispatch = new Dispatch(ppvObject.getValue());
                T t = this.factory.createProxy(comInterface, dispatch);
                int n = dispatch.Release();
                return t;
            }
            String formatMessageFromHR = Kernel32Util.formatMessage(hr);
            throw new COMException("queryInterface: " + formatMessageFromHR, hr);
        }
        catch (RuntimeException e) {
            if (e instanceof COMException) {
                throw e;
            }
            throw new COMException("Error occured when trying to query for interface " + comInterface.getName(), e);
        }
    }

    private Guid.IID getIID(ComInterface annotation) {
        String iidStr = annotation.iid();
        if (null != iidStr && !iidStr.isEmpty()) {
            return new Guid.IID(iidStr);
        }
        throw new COMException("ComInterface must define a value for iid");
    }

    private String getAccessorName(Method method, ComProperty prop) {
        if (prop.name().isEmpty()) {
            String methName = method.getName();
            if (methName.startsWith("get")) {
                return methName.replaceFirst("get", "");
            }
            throw new RuntimeException("Property Accessor name must start with 'get', or set the anotation 'name' value");
        }
        return prop.name();
    }

    private String getMutatorName(Method method, ComProperty prop) {
        if (prop.name().isEmpty()) {
            String methName = method.getName();
            if (methName.startsWith("set")) {
                return methName.replaceFirst("set", "");
            }
            throw new RuntimeException("Property Mutator name must start with 'set', or set the anotation 'name' value");
        }
        return prop.name();
    }

    private String getMethodName(Method method, ComMethod meth) {
        if (meth.name().isEmpty()) {
            String methName = method.getName();
            return methName;
        }
        return meth.name();
    }

    protected OaIdl.DISPID resolveDispId(String name) {
        return this.resolveDispId(this.getRawDispatch(), name);
    }

    protected WinNT.HRESULT oleMethod(int nType, Variant.VARIANT.ByReference pvResult, String name, Variant.VARIANT pArg) throws COMException {
        return this.oleMethod(nType, pvResult, name, new Variant.VARIANT[]{pArg});
    }

    protected WinNT.HRESULT oleMethod(int nType, Variant.VARIANT.ByReference pvResult, OaIdl.DISPID dispId, Variant.VARIANT pArg) throws COMException {
        return this.oleMethod(nType, pvResult, dispId, new Variant.VARIANT[]{pArg});
    }

    protected WinNT.HRESULT oleMethod(int nType, Variant.VARIANT.ByReference pvResult, String name) throws COMException {
        return this.oleMethod(nType, pvResult, name, (Variant.VARIANT[])null);
    }

    protected WinNT.HRESULT oleMethod(int nType, Variant.VARIANT.ByReference pvResult, OaIdl.DISPID dispId) throws COMException {
        return this.oleMethod(nType, pvResult, dispId, (Variant.VARIANT[])null);
    }

    protected WinNT.HRESULT oleMethod(int nType, Variant.VARIANT.ByReference pvResult, String name, Variant.VARIANT[] pArgs) throws COMException {
        return this.oleMethod(nType, pvResult, this.resolveDispId(name), pArgs);
    }

    protected WinNT.HRESULT oleMethod(int nType, Variant.VARIANT.ByReference pvResult, OaIdl.DISPID dispId, Variant.VARIANT[] pArgs) throws COMException {
        return this.oleMethod(nType, pvResult, this.getRawDispatch(), dispId, pArgs);
    }

    @Deprecated
    protected OaIdl.DISPID resolveDispId(IDispatch pDisp, String name) {
        assert (COMUtils.comIsInitialized()) : "COM not initialized";
        if (pDisp == null) {
            throw new COMException("pDisp (IDispatch) parameter is null!");
        }
        WString[] ptName = new WString[]{new WString(name)};
        OaIdl.DISPIDByReference pdispID = new OaIdl.DISPIDByReference();
        WinNT.HRESULT hr = pDisp.GetIDsOfNames(new Guid.REFIID(Guid.IID_NULL), ptName, 1, this.factory.getLCID(), pdispID);
        COMUtils.checkRC(hr);
        return pdispID.getValue();
    }

    @Deprecated
    protected WinNT.HRESULT oleMethod(int nType, Variant.VARIANT.ByReference pvResult, IDispatch pDisp, String name, Variant.VARIANT pArg) throws COMException {
        return this.oleMethod(nType, pvResult, pDisp, name, new Variant.VARIANT[]{pArg});
    }

    @Deprecated
    protected WinNT.HRESULT oleMethod(int nType, Variant.VARIANT.ByReference pvResult, IDispatch pDisp, OaIdl.DISPID dispId, Variant.VARIANT pArg) throws COMException {
        return this.oleMethod(nType, pvResult, pDisp, dispId, new Variant.VARIANT[]{pArg});
    }

    @Deprecated
    protected WinNT.HRESULT oleMethod(int nType, Variant.VARIANT.ByReference pvResult, IDispatch pDisp, String name) throws COMException {
        return this.oleMethod(nType, pvResult, pDisp, name, (Variant.VARIANT[])null);
    }

    @Deprecated
    protected WinNT.HRESULT oleMethod(int nType, Variant.VARIANT.ByReference pvResult, IDispatch pDisp, OaIdl.DISPID dispId) throws COMException {
        return this.oleMethod(nType, pvResult, pDisp, dispId, (Variant.VARIANT[])null);
    }

    @Deprecated
    protected WinNT.HRESULT oleMethod(int nType, Variant.VARIANT.ByReference pvResult, IDispatch pDisp, String name, Variant.VARIANT[] pArgs) throws COMException {
        return this.oleMethod(nType, pvResult, pDisp, this.resolveDispId(pDisp, name), pArgs);
    }

    @Deprecated
    protected WinNT.HRESULT oleMethod(int nType, Variant.VARIANT.ByReference pvResult, IDispatch pDisp, OaIdl.DISPID dispId, Variant.VARIANT[] pArgs) throws COMException {
        assert (COMUtils.comIsInitialized()) : "COM not initialized";
        if (pDisp == null) {
            throw new COMException("pDisp (IDispatch) parameter is null!");
        }
        int _argsLen = 0;
        Variant.VARIANT[] _args = null;
        OleAuto.DISPPARAMS.ByReference dp = new OleAuto.DISPPARAMS.ByReference();
        OaIdl.EXCEPINFO.ByReference pExcepInfo = new OaIdl.EXCEPINFO.ByReference();
        IntByReference puArgErr = new IntByReference();
        if (pArgs != null && pArgs.length > 0) {
            _argsLen = pArgs.length;
            _args = new Variant.VARIANT[_argsLen];
            int revCount = _argsLen;
            for (int i = 0; i < _argsLen; ++i) {
                _args[i] = pArgs[--revCount];
            }
        }
        if (nType == 4) {
            dp.setRgdispidNamedArgs(new OaIdl.DISPID[]{OaIdl.DISPID_PROPERTYPUT});
        }
        int finalNType = nType == 1 || nType == 2 ? 3 : nType;
        if (_argsLen > 0) {
            dp.setArgs(_args);
            dp.write();
        }
        WinNT.HRESULT hr = pDisp.Invoke(dispId, new Guid.REFIID(Guid.IID_NULL), this.factory.getLCID(), new WinDef.WORD((long)finalNType), dp, pvResult, pExcepInfo, puArgErr);
        COMUtils.checkRC(hr, pExcepInfo, puArgErr);
        return hr;
    }
}

