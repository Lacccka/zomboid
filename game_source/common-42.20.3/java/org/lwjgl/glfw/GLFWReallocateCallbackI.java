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
@NativeType(value="GLFWreallocatefun")
public interface GLFWReallocateCallbackI
extends CallbackI {
    public static final Callback.Descriptor DESCRIPTOR = new Callback.Descriptor(MethodHandles.lookup(), APIUtil.apiCreateCIF(LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer));

    @Override
    default public Callback.Descriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    default public void callback(long ret, long args2) {
        long __result = this.invoke(MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args2)), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args2 + (long)POINTER_SIZE)), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args2 + (long)(2 * POINTER_SIZE))));
        APIUtil.apiClosureRetP(ret, __result);
    }

    @NativeType(value="void *")
    public long invoke(@NativeType(value="void *") long var1, @NativeType(value="size_t") long var3, @NativeType(value="void *") long var5);
}

