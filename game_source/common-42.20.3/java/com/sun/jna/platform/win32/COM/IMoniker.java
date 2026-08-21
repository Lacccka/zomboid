/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32.COM;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.IPersistStream;

public interface IMoniker
extends IPersistStream {
    public void BindToObject();

    public void BindToStorage();

    public void Reduce();

    public void ComposeWith();

    public void Enum();

    public void IsEqual();

    public void Hash();

    public void IsRunning();

    public void GetTimeOfLastChange();

    public void Inverse();

    public void CommonPrefixWith();

    public String GetDisplayName(Pointer var1, Pointer var2);

    public void ParseDisplayName();

    public void IsSystemMoniker();

    public void RelativePathTo();
}

