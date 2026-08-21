/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32.COM.util;

import com.sun.jna.platform.win32.COM.IDispatchCallback;
import com.sun.jna.platform.win32.COM.util.IComEventCallbackListener;

public abstract class AbstractComEventCallbackListener
implements IComEventCallbackListener {
    IDispatchCallback dispatchCallback = null;

    @Override
    public void setDispatchCallbackListener(IDispatchCallback dispatchCallback) {
        this.dispatchCallback = dispatchCallback;
    }
}

