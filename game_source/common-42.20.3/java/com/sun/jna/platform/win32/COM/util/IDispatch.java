/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32.COM.util;

import com.sun.jna.platform.win32.COM.util.IUnknown;
import com.sun.jna.platform.win32.OaIdl;

public interface IDispatch
extends IUnknown {
    public <T> void setProperty(String var1, T var2);

    public <T> T getProperty(Class<T> var1, String var2, Object ... var3);

    public <T> T invokeMethod(Class<T> var1, String var2, Object ... var3);

    public <T> void setProperty(OaIdl.DISPID var1, T var2);

    public <T> T getProperty(Class<T> var1, OaIdl.DISPID var2, Object ... var3);

    public <T> T invokeMethod(Class<T> var1, OaIdl.DISPID var2, Object ... var3);
}

