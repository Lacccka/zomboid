/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.opengl;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL44C;
import org.lwjgl.system.NativeType;

public class GL44
extends GL43 {
    public static final int GL_MAX_VERTEX_ATTRIB_STRIDE = 33509;
    public static final int GL_PRIMITIVE_RESTART_FOR_PATCHES_SUPPORTED = 33313;
    public static final int GL_TEXTURE_BUFFER_BINDING = 35882;
    public static final int GL_MAP_PERSISTENT_BIT = 64;
    public static final int GL_MAP_COHERENT_BIT = 128;
    public static final int GL_DYNAMIC_STORAGE_BIT = 256;
    public static final int GL_CLIENT_STORAGE_BIT = 512;
    public static final int GL_BUFFER_IMMUTABLE_STORAGE = 33311;
    public static final int GL_BUFFER_STORAGE_FLAGS = 33312;
    public static final int GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT = 16384;
    public static final int GL_CLEAR_TEXTURE = 37733;
    public static final int GL_LOCATION_COMPONENT = 37706;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_INDEX = 37707;
    public static final int GL_TRANSFORM_FEEDBACK_BUFFER_STRIDE = 37708;
    public static final int GL_QUERY_RESULT_NO_WAIT = 37268;
    public static final int GL_QUERY_BUFFER = 37266;
    public static final int GL_QUERY_BUFFER_BINDING = 37267;
    public static final int GL_QUERY_BUFFER_BARRIER_BIT = 32768;
    public static final int GL_MIRROR_CLAMP_TO_EDGE = 34627;

    protected GL44() {
        throw new UnsupportedOperationException();
    }

    public static void nglBufferStorage(int target, long size, long data, int flags) {
        GL44C.nglBufferStorage(target, size, data, flags);
    }

    public static void glBufferStorage(@NativeType(value="GLenum") int target, @NativeType(value="GLsizeiptr") long size, @NativeType(value="GLbitfield") int flags) {
        GL44C.glBufferStorage(target, size, flags);
    }

    public static void glBufferStorage(@NativeType(value="GLenum") int target, @NativeType(value="void const *") ByteBuffer data, @NativeType(value="GLbitfield") int flags) {
        GL44C.glBufferStorage(target, data, flags);
    }

    public static void glBufferStorage(@NativeType(value="GLenum") int target, @NativeType(value="void const *") ShortBuffer data, @NativeType(value="GLbitfield") int flags) {
        GL44C.glBufferStorage(target, data, flags);
    }

    public static void glBufferStorage(@NativeType(value="GLenum") int target, @NativeType(value="void const *") IntBuffer data, @NativeType(value="GLbitfield") int flags) {
        GL44C.glBufferStorage(target, data, flags);
    }

    public static void glBufferStorage(@NativeType(value="GLenum") int target, @NativeType(value="void const *") FloatBuffer data, @NativeType(value="GLbitfield") int flags) {
        GL44C.glBufferStorage(target, data, flags);
    }

    public static void glBufferStorage(@NativeType(value="GLenum") int target, @NativeType(value="void const *") DoubleBuffer data, @NativeType(value="GLbitfield") int flags) {
        GL44C.glBufferStorage(target, data, flags);
    }

    public static void nglClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, long data) {
        GL44C.nglClearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
    }

    public static void glClearTexSubImage(@NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level, @NativeType(value="GLint") int xoffset, @NativeType(value="GLint") int yoffset, @NativeType(value="GLint") int zoffset, @NativeType(value="GLsizei") int width, @NativeType(value="GLsizei") int height, @NativeType(value="GLsizei") int depth, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") @Nullable ByteBuffer data) {
        GL44C.glClearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
    }

    public static void glClearTexSubImage(@NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level, @NativeType(value="GLint") int xoffset, @NativeType(value="GLint") int yoffset, @NativeType(value="GLint") int zoffset, @NativeType(value="GLsizei") int width, @NativeType(value="GLsizei") int height, @NativeType(value="GLsizei") int depth, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") @Nullable ShortBuffer data) {
        GL44C.glClearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
    }

    public static void glClearTexSubImage(@NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level, @NativeType(value="GLint") int xoffset, @NativeType(value="GLint") int yoffset, @NativeType(value="GLint") int zoffset, @NativeType(value="GLsizei") int width, @NativeType(value="GLsizei") int height, @NativeType(value="GLsizei") int depth, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") @Nullable IntBuffer data) {
        GL44C.glClearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
    }

    public static void glClearTexSubImage(@NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level, @NativeType(value="GLint") int xoffset, @NativeType(value="GLint") int yoffset, @NativeType(value="GLint") int zoffset, @NativeType(value="GLsizei") int width, @NativeType(value="GLsizei") int height, @NativeType(value="GLsizei") int depth, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") @Nullable FloatBuffer data) {
        GL44C.glClearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
    }

    public static void glClearTexSubImage(@NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level, @NativeType(value="GLint") int xoffset, @NativeType(value="GLint") int yoffset, @NativeType(value="GLint") int zoffset, @NativeType(value="GLsizei") int width, @NativeType(value="GLsizei") int height, @NativeType(value="GLsizei") int depth, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") @Nullable DoubleBuffer data) {
        GL44C.glClearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
    }

    public static void nglClearTexImage(int texture, int level, int format, int type, long data) {
        GL44C.nglClearTexImage(texture, level, format, type, data);
    }

    public static void glClearTexImage(@NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") @Nullable ByteBuffer data) {
        GL44C.glClearTexImage(texture, level, format, type, data);
    }

    public static void glClearTexImage(@NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") @Nullable ShortBuffer data) {
        GL44C.glClearTexImage(texture, level, format, type, data);
    }

    public static void glClearTexImage(@NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") @Nullable IntBuffer data) {
        GL44C.glClearTexImage(texture, level, format, type, data);
    }

    public static void glClearTexImage(@NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") @Nullable FloatBuffer data) {
        GL44C.glClearTexImage(texture, level, format, type, data);
    }

    public static void glClearTexImage(@NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") @Nullable DoubleBuffer data) {
        GL44C.glClearTexImage(texture, level, format, type, data);
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

    public static void glBufferStorage(@NativeType(value="GLenum") int target, @NativeType(value="void const *") short[] data, @NativeType(value="GLbitfield") int flags) {
        GL44C.glBufferStorage(target, data, flags);
    }

    public static void glBufferStorage(@NativeType(value="GLenum") int target, @NativeType(value="void const *") int[] data, @NativeType(value="GLbitfield") int flags) {
        GL44C.glBufferStorage(target, data, flags);
    }

    public static void glBufferStorage(@NativeType(value="GLenum") int target, @NativeType(value="void const *") float[] data, @NativeType(value="GLbitfield") int flags) {
        GL44C.glBufferStorage(target, data, flags);
    }

    public static void glBufferStorage(@NativeType(value="GLenum") int target, @NativeType(value="void const *") double[] data, @NativeType(value="GLbitfield") int flags) {
        GL44C.glBufferStorage(target, data, flags);
    }

    public static void glClearTexSubImage(@NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level, @NativeType(value="GLint") int xoffset, @NativeType(value="GLint") int yoffset, @NativeType(value="GLint") int zoffset, @NativeType(value="GLsizei") int width, @NativeType(value="GLsizei") int height, @NativeType(value="GLsizei") int depth, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") short @Nullable [] data) {
        GL44C.glClearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
    }

    public static void glClearTexSubImage(@NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level, @NativeType(value="GLint") int xoffset, @NativeType(value="GLint") int yoffset, @NativeType(value="GLint") int zoffset, @NativeType(value="GLsizei") int width, @NativeType(value="GLsizei") int height, @NativeType(value="GLsizei") int depth, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") int @Nullable [] data) {
        GL44C.glClearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
    }

    public static void glClearTexSubImage(@NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level, @NativeType(value="GLint") int xoffset, @NativeType(value="GLint") int yoffset, @NativeType(value="GLint") int zoffset, @NativeType(value="GLsizei") int width, @NativeType(value="GLsizei") int height, @NativeType(value="GLsizei") int depth, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") float @Nullable [] data) {
        GL44C.glClearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
    }

    public static void glClearTexSubImage(@NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level, @NativeType(value="GLint") int xoffset, @NativeType(value="GLint") int yoffset, @NativeType(value="GLint") int zoffset, @NativeType(value="GLsizei") int width, @NativeType(value="GLsizei") int height, @NativeType(value="GLsizei") int depth, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") double @Nullable [] data) {
        GL44C.glClearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
    }

    public static void glClearTexImage(@NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") short @Nullable [] data) {
        GL44C.glClearTexImage(texture, level, format, type, data);
    }

    public static void glClearTexImage(@NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") int @Nullable [] data) {
        GL44C.glClearTexImage(texture, level, format, type, data);
    }

    public static void glClearTexImage(@NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") float @Nullable [] data) {
        GL44C.glClearTexImage(texture, level, format, type, data);
    }

    public static void glClearTexImage(@NativeType(value="GLuint") int texture, @NativeType(value="GLint") int level, @NativeType(value="GLenum") int format, @NativeType(value="GLenum") int type, @NativeType(value="void const *") double @Nullable [] data) {
        GL44C.glClearTexImage(texture, level, format, type, data);
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

