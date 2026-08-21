/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.opengl;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GLDebugMessageCallbackI;
import org.lwjgl.system.NativeType;

public class KHRDebug {
    public static final int GL_DEBUG_OUTPUT = 37600;
    public static final int GL_DEBUG_OUTPUT_SYNCHRONOUS = 33346;
    public static final int GL_CONTEXT_FLAG_DEBUG_BIT = 2;
    public static final int GL_MAX_DEBUG_MESSAGE_LENGTH = 37187;
    public static final int GL_MAX_DEBUG_LOGGED_MESSAGES = 37188;
    public static final int GL_DEBUG_LOGGED_MESSAGES = 37189;
    public static final int GL_DEBUG_NEXT_LOGGED_MESSAGE_LENGTH = 33347;
    public static final int GL_MAX_DEBUG_GROUP_STACK_DEPTH = 33388;
    public static final int GL_DEBUG_GROUP_STACK_DEPTH = 33389;
    public static final int GL_MAX_LABEL_LENGTH = 33512;
    public static final int GL_DEBUG_CALLBACK_FUNCTION = 33348;
    public static final int GL_DEBUG_CALLBACK_USER_PARAM = 33349;
    public static final int GL_DEBUG_SOURCE_API = 33350;
    public static final int GL_DEBUG_SOURCE_WINDOW_SYSTEM = 33351;
    public static final int GL_DEBUG_SOURCE_SHADER_COMPILER = 33352;
    public static final int GL_DEBUG_SOURCE_THIRD_PARTY = 33353;
    public static final int GL_DEBUG_SOURCE_APPLICATION = 33354;
    public static final int GL_DEBUG_SOURCE_OTHER = 33355;
    public static final int GL_DEBUG_TYPE_ERROR = 33356;
    public static final int GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR = 33357;
    public static final int GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR = 33358;
    public static final int GL_DEBUG_TYPE_PORTABILITY = 33359;
    public static final int GL_DEBUG_TYPE_PERFORMANCE = 33360;
    public static final int GL_DEBUG_TYPE_OTHER = 33361;
    public static final int GL_DEBUG_TYPE_MARKER = 33384;
    public static final int GL_DEBUG_TYPE_PUSH_GROUP = 33385;
    public static final int GL_DEBUG_TYPE_POP_GROUP = 33386;
    public static final int GL_DEBUG_SEVERITY_HIGH = 37190;
    public static final int GL_DEBUG_SEVERITY_MEDIUM = 37191;
    public static final int GL_DEBUG_SEVERITY_LOW = 37192;
    public static final int GL_DEBUG_SEVERITY_NOTIFICATION = 33387;
    public static final int GL_BUFFER = 33504;
    public static final int GL_SHADER = 33505;
    public static final int GL_PROGRAM = 33506;
    public static final int GL_QUERY = 33507;
    public static final int GL_PROGRAM_PIPELINE = 33508;
    public static final int GL_SAMPLER = 33510;
    public static final int GL_DISPLAY_LIST = 33511;

    protected KHRDebug() {
        throw new UnsupportedOperationException();
    }

    public static void nglDebugMessageControl(int source2, int type, int severity, int count, long ids, boolean enabled) {
        GL43C.nglDebugMessageControl(source2, type, severity, count, ids, enabled);
    }

    public static void glDebugMessageControl(@NativeType(value="GLenum") int source2, @NativeType(value="GLenum") int type, @NativeType(value="GLenum") int severity, @NativeType(value="GLuint const *") @Nullable IntBuffer ids, @NativeType(value="GLboolean") boolean enabled) {
        GL43C.glDebugMessageControl(source2, type, severity, ids, enabled);
    }

    public static void glDebugMessageControl(@NativeType(value="GLenum") int source2, @NativeType(value="GLenum") int type, @NativeType(value="GLenum") int severity, @NativeType(value="GLuint const *") int id, @NativeType(value="GLboolean") boolean enabled) {
        GL43C.glDebugMessageControl(source2, type, severity, id, enabled);
    }

    public static void nglDebugMessageInsert(int source2, int type, int id, int severity, int length, long message) {
        GL43C.nglDebugMessageInsert(source2, type, id, severity, length, message);
    }

    public static void glDebugMessageInsert(@NativeType(value="GLenum") int source2, @NativeType(value="GLenum") int type, @NativeType(value="GLuint") int id, @NativeType(value="GLenum") int severity, @NativeType(value="GLchar const *") ByteBuffer message) {
        GL43C.glDebugMessageInsert(source2, type, id, severity, message);
    }

    public static void glDebugMessageInsert(@NativeType(value="GLenum") int source2, @NativeType(value="GLenum") int type, @NativeType(value="GLuint") int id, @NativeType(value="GLenum") int severity, @NativeType(value="GLchar const *") CharSequence message) {
        GL43C.glDebugMessageInsert(source2, type, id, severity, message);
    }

    public static void nglDebugMessageCallback(long callback, long userParam) {
        GL43C.nglDebugMessageCallback(callback, userParam);
    }

    public static void glDebugMessageCallback(@NativeType(value="GLDEBUGPROC") @Nullable GLDebugMessageCallbackI callback, @NativeType(value="void const *") long userParam) {
        GL43C.glDebugMessageCallback(callback, userParam);
    }

    public static int nglGetDebugMessageLog(int count, int bufsize, long sources, long types, long ids, long severities, long lengths, long messageLog) {
        return GL43C.nglGetDebugMessageLog(count, bufsize, sources, types, ids, severities, lengths, messageLog);
    }

    @NativeType(value="GLuint")
    public static int glGetDebugMessageLog(@NativeType(value="GLuint") int count, @NativeType(value="GLenum *") @Nullable IntBuffer sources, @NativeType(value="GLenum *") @Nullable IntBuffer types, @NativeType(value="GLuint *") @Nullable IntBuffer ids, @NativeType(value="GLenum *") @Nullable IntBuffer severities, @NativeType(value="GLsizei *") @Nullable IntBuffer lengths, @NativeType(value="GLchar *") @Nullable ByteBuffer messageLog) {
        return GL43C.glGetDebugMessageLog(count, sources, types, ids, severities, lengths, messageLog);
    }

    public static void nglPushDebugGroup(int source2, int id, int length, long message) {
        GL43C.nglPushDebugGroup(source2, id, length, message);
    }

    public static void glPushDebugGroup(@NativeType(value="GLenum") int source2, @NativeType(value="GLuint") int id, @NativeType(value="GLchar const *") ByteBuffer message) {
        GL43C.glPushDebugGroup(source2, id, message);
    }

    public static void glPushDebugGroup(@NativeType(value="GLenum") int source2, @NativeType(value="GLuint") int id, @NativeType(value="GLchar const *") CharSequence message) {
        GL43C.glPushDebugGroup(source2, id, message);
    }

    public static void glPopDebugGroup() {
        GL43C.glPopDebugGroup();
    }

    public static void nglObjectLabel(int identifier, int name, int length, long label) {
        GL43C.nglObjectLabel(identifier, name, length, label);
    }

    public static void glObjectLabel(@NativeType(value="GLenum") int identifier, @NativeType(value="GLuint") int name, @NativeType(value="GLchar const *") ByteBuffer label) {
        GL43C.glObjectLabel(identifier, name, label);
    }

    public static void glObjectLabel(@NativeType(value="GLenum") int identifier, @NativeType(value="GLuint") int name, @NativeType(value="GLchar const *") CharSequence label) {
        GL43C.glObjectLabel(identifier, name, label);
    }

    public static void nglGetObjectLabel(int identifier, int name, int bufSize, long length, long label) {
        GL43C.nglGetObjectLabel(identifier, name, bufSize, length, label);
    }

    public static void glGetObjectLabel(@NativeType(value="GLenum") int identifier, @NativeType(value="GLuint") int name, @NativeType(value="GLsizei *") @Nullable IntBuffer length, @NativeType(value="GLchar *") ByteBuffer label) {
        GL43C.glGetObjectLabel(identifier, name, length, label);
    }

    @NativeType(value="void")
    public static String glGetObjectLabel(@NativeType(value="GLenum") int identifier, @NativeType(value="GLuint") int name, @NativeType(value="GLsizei") int bufSize) {
        return GL43C.glGetObjectLabel(identifier, name, bufSize);
    }

    @NativeType(value="void")
    public static String glGetObjectLabel(@NativeType(value="GLenum") int identifier, @NativeType(value="GLuint") int name) {
        return KHRDebug.glGetObjectLabel(identifier, name, GL11.glGetInteger(33512));
    }

    public static void nglObjectPtrLabel(long ptr, int length, long label) {
        GL43C.nglObjectPtrLabel(ptr, length, label);
    }

    public static void glObjectPtrLabel(@NativeType(value="void *") long ptr, @NativeType(value="GLchar const *") ByteBuffer label) {
        GL43C.glObjectPtrLabel(ptr, label);
    }

    public static void glObjectPtrLabel(@NativeType(value="void *") long ptr, @NativeType(value="GLchar const *") CharSequence label) {
        GL43C.glObjectPtrLabel(ptr, label);
    }

    public static void nglGetObjectPtrLabel(long ptr, int bufSize, long length, long label) {
        GL43C.nglGetObjectPtrLabel(ptr, bufSize, length, label);
    }

    public static void glGetObjectPtrLabel(@NativeType(value="void *") long ptr, @NativeType(value="GLsizei *") @Nullable IntBuffer length, @NativeType(value="GLchar *") ByteBuffer label) {
        GL43C.glGetObjectPtrLabel(ptr, length, label);
    }

    @NativeType(value="void")
    public static String glGetObjectPtrLabel(@NativeType(value="void *") long ptr, @NativeType(value="GLsizei") int bufSize) {
        return GL43C.glGetObjectPtrLabel(ptr, bufSize);
    }

    @NativeType(value="void")
    public static String glGetObjectPtrLabel(@NativeType(value="void *") long ptr) {
        return KHRDebug.glGetObjectPtrLabel(ptr, GL11.glGetInteger(33512));
    }

    public static void glDebugMessageControl(@NativeType(value="GLenum") int source2, @NativeType(value="GLenum") int type, @NativeType(value="GLenum") int severity, @NativeType(value="GLuint const *") int @Nullable [] ids, @NativeType(value="GLboolean") boolean enabled) {
        GL43C.glDebugMessageControl(source2, type, severity, ids, enabled);
    }

    @NativeType(value="GLuint")
    public static int glGetDebugMessageLog(@NativeType(value="GLuint") int count, @NativeType(value="GLenum *") int @Nullable [] sources, @NativeType(value="GLenum *") int @Nullable [] types, @NativeType(value="GLuint *") int @Nullable [] ids, @NativeType(value="GLenum *") int @Nullable [] severities, @NativeType(value="GLsizei *") int @Nullable [] lengths, @NativeType(value="GLchar *") @Nullable ByteBuffer messageLog) {
        return GL43C.glGetDebugMessageLog(count, sources, types, ids, severities, lengths, messageLog);
    }

    public static void glGetObjectLabel(@NativeType(value="GLenum") int identifier, @NativeType(value="GLuint") int name, @NativeType(value="GLsizei *") int @Nullable [] length, @NativeType(value="GLchar *") ByteBuffer label) {
        GL43C.glGetObjectLabel(identifier, name, length, label);
    }

    public static void glGetObjectPtrLabel(@NativeType(value="void *") long ptr, @NativeType(value="GLsizei *") int @Nullable [] length, @NativeType(value="GLchar *") ByteBuffer label) {
        GL43C.glGetObjectPtrLabel(ptr, length, label);
    }

    static {
        GL.initialize();
    }
}

