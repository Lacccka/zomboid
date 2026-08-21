/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.opengl;

import java.nio.IntBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Checks;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;

public class EXTEGLImageStorage {
    protected EXTEGLImageStorage() {
        throw new UnsupportedOperationException();
    }

    public static native void nglEGLImageTargetTexStorageEXT(int var0, long var1, long var3);

    public static void glEGLImageTargetTexStorageEXT(@NativeType(value="GLenum") int target, @NativeType(value="GLeglImageOES") long image, @NativeType(value="int const *") @Nullable IntBuffer attrib_list) {
        if (Checks.CHECKS) {
            Checks.check(image);
            Checks.checkNTSafe(attrib_list);
        }
        EXTEGLImageStorage.nglEGLImageTargetTexStorageEXT(target, image, MemoryUtil.memAddressSafe(attrib_list));
    }

    public static native void nglEGLImageTargetTextureStorageEXT(int var0, long var1, long var3);

    public static void glEGLImageTargetTextureStorageEXT(@NativeType(value="GLuint") int texture, @NativeType(value="GLeglImageOES") long image, @NativeType(value="int const *") @Nullable IntBuffer attrib_list) {
        if (Checks.CHECKS) {
            Checks.check(image);
            Checks.checkNTSafe(attrib_list);
        }
        EXTEGLImageStorage.nglEGLImageTargetTextureStorageEXT(texture, image, MemoryUtil.memAddressSafe(attrib_list));
    }

    public static void glEGLImageTargetTexStorageEXT(@NativeType(value="GLenum") int target, @NativeType(value="GLeglImageOES") long image, @NativeType(value="int const *") int @Nullable [] attrib_list) {
        long __functionAddress = GL.getICD().glEGLImageTargetTexStorageEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(image);
            Checks.checkNTSafe(attrib_list);
        }
        JNI.callPPV(target, image, attrib_list, __functionAddress);
    }

    public static void glEGLImageTargetTextureStorageEXT(@NativeType(value="GLuint") int texture, @NativeType(value="GLeglImageOES") long image, @NativeType(value="int const *") int @Nullable [] attrib_list) {
        long __functionAddress = GL.getICD().glEGLImageTargetTextureStorageEXT;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
            Checks.check(image);
            Checks.checkNTSafe(attrib_list);
        }
        JNI.callPPV(texture, image, attrib_list, __functionAddress);
    }

    static {
        GL.initialize();
    }
}

