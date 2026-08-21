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
@NativeType(value="GLFWpreeditcandidatefun")
public interface GLFWPreeditCandidateCallbackI
extends CallbackI {
    public static final Callback.Descriptor DESCRIPTOR = new Callback.Descriptor(MethodHandles.lookup(), APIUtil.apiCreateCIF(LibFFI.ffi_type_void, LibFFI.ffi_type_pointer, LibFFI.ffi_type_sint32, LibFFI.ffi_type_sint32, LibFFI.ffi_type_sint32, LibFFI.ffi_type_sint32));

    @Override
    default public Callback.Descriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    default public void callback(long ret, long args2) {
        this.invoke(MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args2)), MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args2 + (long)POINTER_SIZE)), MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args2 + (long)(2 * POINTER_SIZE))), MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args2 + (long)(3 * POINTER_SIZE))), MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args2 + (long)(4 * POINTER_SIZE))));
    }

    public void invoke(@NativeType(value="GLFWwindow *") long var1, int var3, int var4, int var5, int var6);
}

