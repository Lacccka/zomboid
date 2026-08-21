/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32.COM;

import com.sun.jna.platform.win32.COM.IPersist;
import com.sun.jna.platform.win32.COM.IStream;

public interface IPersistStream
extends IPersist {
    public boolean IsDirty();

    public void Load(IStream var1);

    public void Save(IStream var1);

    public void GetSizeMax();
}

