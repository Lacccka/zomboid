/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.glfw;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFWAllocateCallbackI;
import org.lwjgl.system.Callback;

public abstract class GLFWAllocateCallback
extends Callback
implements GLFWAllocateCallbackI {
    public static GLFWAllocateCallback create(long functionPointer) {
        GLFWAllocateCallbackI instance = (GLFWAllocateCallbackI)Callback.get(functionPointer);
        return instance instanceof GLFWAllocateCallback ? (GLFWAllocateCallback)instance : new Container(functionPointer, instance);
    }

    public static @Nullable GLFWAllocateCallback createSafe(long functionPointer) {
        return functionPointer == 0L ? null : GLFWAllocateCallback.create(functionPointer);
    }

    public static GLFWAllocateCallback create(GLFWAllocateCallbackI instance) {
        return instance instanceof GLFWAllocateCallback ? (GLFWAllocateCallback)instance : new Container(instance.address(), instance);
    }

    protected GLFWAllocateCallback() {
        super(DESCRIPTOR);
    }

    GLFWAllocateCallback(long functionPointer) {
        super(functionPointer);
    }

    private static final class Container
    extends GLFWAllocateCallback {
        private final GLFWAllocateCallbackI delegate;

        Container(long functionPointer, GLFWAllocateCallbackI delegate) {
            super(functionPointer);
            this.delegate = delegate;
        }

        @Override
        public long invoke(long size, long user) {
            return this.delegate.invoke(size, user);
        }
    }
}

