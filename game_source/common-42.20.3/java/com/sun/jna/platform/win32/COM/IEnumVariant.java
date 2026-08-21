/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32.COM;

import com.sun.jna.platform.win32.COM.IUnknown;
import com.sun.jna.platform.win32.Variant;

public interface IEnumVariant
extends IUnknown {
    public IEnumVariant Clone();

    public Variant.VARIANT[] Next(int var1);

    public void Reset();

    public void Skip(int var1);
}

