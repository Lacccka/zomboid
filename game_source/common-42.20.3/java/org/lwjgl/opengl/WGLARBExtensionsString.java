/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.opengl;

import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Checks;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;

public class WGLARBExtensionsString {
    protected WGLARBExtensionsString() {
        throw new UnsupportedOperationException();
    }

    public static long nwglGetExtensionsStringARB(long hdc) {
        long __functionAddress = GL.getCapabilitiesWGL().wglGetExtensionsStringARB;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(hdc);
        }
        return JNI.callPP(hdc, __functionAddress);
    }

    @NativeType(value="char const *")
    public static @Nullable String wglGetExtensionsStringARB(@NativeType(value="HDC") long hdc) {
        long __result = WGLARBExtensionsString.nwglGetExtensionsStringARB(hdc);
        return MemoryUtil.memASCIISafe(__result);
    }
}

