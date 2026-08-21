/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.opengl;

import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GLDebugMessageARBCallbackI;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryUtil;

public abstract class GLDebugMessageARBCallback
extends Callback
implements GLDebugMessageARBCallbackI {
    public static GLDebugMessageARBCallback create(long functionPointer) {
        GLDebugMessageARBCallbackI instance = (GLDebugMessageARBCallbackI)Callback.get(functionPointer);
        return instance instanceof GLDebugMessageARBCallback ? (GLDebugMessageARBCallback)instance : new Container(functionPointer, instance);
    }

    public static @Nullable GLDebugMessageARBCallback createSafe(long functionPointer) {
        return functionPointer == 0L ? null : GLDebugMessageARBCallback.create(functionPointer);
    }

    public static GLDebugMessageARBCallback create(GLDebugMessageARBCallbackI instance) {
        return instance instanceof GLDebugMessageARBCallback ? (GLDebugMessageARBCallback)instance : new Container(instance.address(), instance);
    }

    protected GLDebugMessageARBCallback() {
        super(DESCRIPTOR);
    }

    GLDebugMessageARBCallback(long functionPointer) {
        super(functionPointer);
    }

    public static String getMessage(int length, long message) {
        return MemoryUtil.memUTF8(MemoryUtil.memByteBuffer(message, length));
    }

    private static final class Container
    extends GLDebugMessageARBCallback {
        private final GLDebugMessageARBCallbackI delegate;

        Container(long functionPointer, GLDebugMessageARBCallbackI delegate) {
            super(functionPointer);
            this.delegate = delegate;
        }

        @Override
        public void invoke(int source2, int type, int id, int severity, int length, long message, long userParam) {
            this.delegate.invoke(source2, type, id, severity, length, message, userParam);
        }
    }
}

