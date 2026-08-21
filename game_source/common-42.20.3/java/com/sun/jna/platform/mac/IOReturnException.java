/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.mac;

public class IOReturnException
extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private int ioReturn;

    public IOReturnException(int kr) {
        this(kr, IOReturnException.formatMessage(kr));
    }

    protected IOReturnException(int kr, String msg) {
        super(msg);
        this.ioReturn = kr;
    }

    public int getIOReturnCode() {
        return this.ioReturn;
    }

    public static int getSystem(int kr) {
        return kr >> 26 & 0x3F;
    }

    public static int getSubSystem(int kr) {
        return kr >> 14 & 0xFFF;
    }

    public static int getCode(int kr) {
        return kr & 0x3FFF;
    }

    private static String formatMessage(int kr) {
        return "IOReturn error code: " + kr + " (system=" + IOReturnException.getSystem(kr) + ", subSystem=" + IOReturnException.getSubSystem(kr) + ", code=" + IOReturnException.getCode(kr) + ")";
    }
}

