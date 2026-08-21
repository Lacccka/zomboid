/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;

public interface Winsock2
extends Library {
    public static final Winsock2 INSTANCE = Native.load("ws2_32", Winsock2.class, W32APIOptions.ASCII_OPTIONS);

    public int gethostname(byte[] var1, int var2);
}

