/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32.COM.util;

import com.sun.jna.platform.win32.COM.IDispatchCallback;

public interface IComEventCallbackListener {
    public void setDispatchCallbackListener(IDispatchCallback var1);

    public void errorReceivingCallbackEvent(String var1, Exception var2);
}

