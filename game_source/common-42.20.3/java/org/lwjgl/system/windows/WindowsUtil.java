/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.windows;

import java.nio.IntBuffer;

public final class WindowsUtil {
    private WindowsUtil() {
    }

    public static void windowsThrowException(String msg, IntBuffer GetLastError) {
        throw new RuntimeException(msg + " (error code = " + GetLastError.get(GetLastError.position()) + ")");
    }
}

