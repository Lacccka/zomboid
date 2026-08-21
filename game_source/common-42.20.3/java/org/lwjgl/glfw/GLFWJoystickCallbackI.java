/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.glfw;

import java.lang.invoke.MethodHandles;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.libffi.LibFFI;

@FunctionalInterface
@NativeType(value="GLFWjoystickfun")
public interface GLFWJoystickCallbackI
extends CallbackI {
    public static final Callback.Descriptor DESCRIPTOR = new Callback.Descriptor(MethodHandles.lookup(), APIUtil.apiCreateCIF(LibFFI.ffi_type_void, LibFFI.ffi_type_sint32, LibFFI.ffi_type_sint32));

    @Override
    default public Callback.Descriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    default public void callback(long ret, long args2) {
        this.invoke(MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args2)), MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args2 + (long)POINTER_SIZE)));
    }

    public void invoke(int var1, int var2);
}

