/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.jemalloc;

import java.lang.invoke.MethodHandles;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.libffi.LibFFI;

@FunctionalInterface
@NativeType(value="extent_commit_t")
public interface ExtentCommitI
extends CallbackI {
    public static final Callback.Descriptor DESCRIPTOR = new Callback.Descriptor(MethodHandles.lookup(), APIUtil.apiCreateCIF(LibFFI.ffi_type_uint8, LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer, LibFFI.ffi_type_uint32));

    @Override
    default public Callback.Descriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    default public void callback(long ret, long args2) {
        boolean __result = this.invoke(MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args2)), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args2 + (long)POINTER_SIZE)), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args2 + (long)(2 * POINTER_SIZE))), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args2 + (long)(3 * POINTER_SIZE))), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args2 + (long)(4 * POINTER_SIZE))), MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args2 + (long)(5 * POINTER_SIZE))));
        APIUtil.apiClosureRet(ret, __result);
    }

    @NativeType(value="bool")
    public boolean invoke(@NativeType(value="extent_hooks_t *") long var1, @NativeType(value="void *") long var3, @NativeType(value="size_t") long var5, @NativeType(value="size_t") long var7, @NativeType(value="size_t") long var9, @NativeType(value="unsigned int") int var11);
}

