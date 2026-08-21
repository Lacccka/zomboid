/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.opengl;

import java.nio.IntBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL44C;
import org.lwjgl.system.NativeType;

public class ARBMultiBind {
    protected ARBMultiBind() {
        throw new UnsupportedOperationException();
    }

    public static void nglBindBuffersBase(int target, int first, int count, long buffers) {
        GL44C.nglBindBuffersBase(target, first, count, buffers);
    }

    public static void glBindBuffersBase(@NativeType(value="GLenum") int target, @NativeType(value="GLuint") int first, @NativeType(value="GLuint const *") @Nullable IntBuffer buffers) {
        GL44C.glBindBuffersBase(target, first, buffers);
    }

    public static void nglBindBuffersRange(int target, int first, int count, long buffers, long offsets, long sizes) {
        GL44C.nglBindBuffersRange(target, first, count, buffers, offsets, sizes);
    }

    public static void glBindBuffersRange(@NativeType(value="GLenum") int target, @NativeType(value="GLuint") int first, @NativeType(value="GLuint const *") @Nullable IntBuffer buffers, @NativeType(value="GLintptr const *") @Nullable PointerBuffer offsets, @NativeType(value="GLsizeiptr const *") @Nullable PointerBuffer sizes) {
        GL44C.glBindBuffersRange(target, first, buffers, offsets, sizes);
    }

    public static void nglBindTextures(int first, int count, long textures) {
        GL44C.nglBindTextures(first, count, textures);
    }

    public static void glBindTextures(@NativeType(value="GLuint") int first, @NativeType(value="GLuint const *") @Nullable IntBuffer textures) {
        GL44C.glBindTextures(first, textures);
    }

    public static void nglBindSamplers(int first, int count, long samplers) {
        GL44C.nglBindSamplers(first, count, samplers);
    }

    public static void glBindSamplers(@NativeType(value="GLuint") int first, @NativeType(value="GLuint const *") @Nullable IntBuffer samplers) {
        GL44C.glBindSamplers(first, samplers);
    }

    public static void nglBindImageTextures(int first, int count, long textures) {
        GL44C.nglBindImageTextures(first, count, textures);
    }

    public static void glBindImageTextures(@NativeType(value="GLuint") int first, @NativeType(value="GLuint const *") @Nullable IntBuffer textures) {
        GL44C.glBindImageTextures(first, textures);
    }

    public static void nglBindVertexBuffers(int first, int count, long buffers, long offsets, long strides) {
        GL44C.nglBindVertexBuffers(first, count, buffers, offsets, strides);
    }

    public static void glBindVertexBuffers(@NativeType(value="GLuint") int first, @NativeType(value="GLuint const *") @Nullable IntBuffer buffers, @NativeType(value="GLintptr const *") @Nullable PointerBuffer offsets, @NativeType(value="GLsizei const *") @Nullable IntBuffer strides) {
        GL44C.glBindVertexBuffers(first, buffers, offsets, strides);
    }

    public static void glBindBuffersBase(@NativeType(value="GLenum") int target, @NativeType(value="GLuint") int first, @NativeType(value="GLuint const *") int @Nullable [] buffers) {
        GL44C.glBindBuffersBase(target, first, buffers);
    }

    public static void glBindBuffersRange(@NativeType(value="GLenum") int target, @NativeType(value="GLuint") int first, @NativeType(value="GLuint const *") int @Nullable [] buffers, @NativeType(value="GLintptr const *") @Nullable PointerBuffer offsets, @NativeType(value="GLsizeiptr const *") @Nullable PointerBuffer sizes) {
        GL44C.glBindBuffersRange(target, first, buffers, offsets, sizes);
    }

    public static void glBindTextures(@NativeType(value="GLuint") int first, @NativeType(value="GLuint const *") int @Nullable [] textures) {
        GL44C.glBindTextures(first, textures);
    }

    public static void glBindSamplers(@NativeType(value="GLuint") int first, @NativeType(value="GLuint const *") int @Nullable [] samplers) {
        GL44C.glBindSamplers(first, samplers);
    }

    public static void glBindImageTextures(@NativeType(value="GLuint") int first, @NativeType(value="GLuint const *") int @Nullable [] textures) {
        GL44C.glBindImageTextures(first, textures);
    }

    public static void glBindVertexBuffers(@NativeType(value="GLuint") int first, @NativeType(value="GLuint const *") int @Nullable [] buffers, @NativeType(value="GLintptr const *") @Nullable PointerBuffer offsets, @NativeType(value="GLsizei const *") int @Nullable [] strides) {
        GL44C.glBindVertexBuffers(first, buffers, offsets, strides);
    }

    static {
        GL.initialize();
    }
}

