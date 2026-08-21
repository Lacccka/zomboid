/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

public abstract class WinioctlUtil {
    public static final int FSCTL_GET_COMPRESSION = WinioctlUtil.CTL_CODE(9, 15, 0, 0);
    public static final int FSCTL_SET_COMPRESSION = WinioctlUtil.CTL_CODE(9, 16, 0, 3);
    public static final int FSCTL_SET_REPARSE_POINT = WinioctlUtil.CTL_CODE(9, 41, 0, 0);
    public static final int FSCTL_GET_REPARSE_POINT = WinioctlUtil.CTL_CODE(9, 42, 0, 0);
    public static final int FSCTL_DELETE_REPARSE_POINT = WinioctlUtil.CTL_CODE(9, 43, 0, 0);

    public static int CTL_CODE(int DeviceType, int Function23, int Method2, int Access2) {
        return DeviceType << 16 | Access2 << 14 | Function23 << 2 | Method2;
    }
}

