/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32.COM.util;

import com.sun.jna.platform.win32.COM.util.IDispatch;
import java.util.List;

public interface IRunningObjectTable {
    public Iterable<IDispatch> enumRunning();

    public <T> List<T> getActiveObjectsByInterface(Class<T> var1);
}

