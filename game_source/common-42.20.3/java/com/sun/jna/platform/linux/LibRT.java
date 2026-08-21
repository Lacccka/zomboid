/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.linux;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface LibRT
extends Library {
    public static final LibRT INSTANCE = Native.load("rt", LibRT.class);

    public int shm_open(String var1, int var2, int var3);

    public int shm_unlink(String var1);
}

