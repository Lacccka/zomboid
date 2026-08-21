/*
 * Decompiled with CFR 0.152.
 */
package oshi.ffm.windows;

import java.util.Locale;

public class Win32Exception
extends RuntimeException {
    private final int errorCode;

    public Win32Exception(int errorCode) {
        super(Win32Exception.formatMessage(errorCode));
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    private static String formatMessage(int errorCode) {
        return String.format(Locale.ROOT, "Win32 API call failed with error code 0x%08X (%d)", errorCode, errorCode);
    }
}

