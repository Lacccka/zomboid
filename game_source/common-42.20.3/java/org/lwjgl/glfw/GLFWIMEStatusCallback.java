/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.glfw;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWIMEStatusCallbackI;
import org.lwjgl.system.Callback;

public abstract class GLFWIMEStatusCallback
extends Callback
implements GLFWIMEStatusCallbackI {
    public static GLFWIMEStatusCallback create(long functionPointer) {
        GLFWIMEStatusCallbackI instance = (GLFWIMEStatusCallbackI)Callback.get(functionPointer);
        return instance instanceof GLFWIMEStatusCallback ? (GLFWIMEStatusCallback)instance : new Container(functionPointer, instance);
    }

    public static @Nullable GLFWIMEStatusCallback createSafe(long functionPointer) {
        return functionPointer == 0L ? null : GLFWIMEStatusCallback.create(functionPointer);
    }

    public static GLFWIMEStatusCallback create(GLFWIMEStatusCallbackI instance) {
        return instance instanceof GLFWIMEStatusCallback ? (GLFWIMEStatusCallback)instance : new Container(instance.address(), instance);
    }

    protected GLFWIMEStatusCallback() {
        super(DESCRIPTOR);
    }

    GLFWIMEStatusCallback(long functionPointer) {
        super(functionPointer);
    }

    public GLFWIMEStatusCallback set(long window) {
        GLFW.glfwSetIMEStatusCallback(window, this);
        return this;
    }

    private static final class Container
    extends GLFWIMEStatusCallback {
        private final GLFWIMEStatusCallbackI delegate;

        Container(long functionPointer, GLFWIMEStatusCallbackI delegate) {
            super(functionPointer);
            this.delegate = delegate;
        }

        @Override
        public void invoke(long window) {
            this.delegate.invoke(window);
        }
    }
}

