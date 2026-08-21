/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.windows;

import java.lang.invoke.MethodHandles;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.libffi.LibFFI;

@FunctionalInterface
@NativeType(value="WNDPROC")
public interface WindowProcI
extends CallbackI {
    public static final Callback.Descriptor DESCRIPTOR = new Callback.Descriptor(MethodHandles.lookup(), APIUtil.apiCreateCIF(APIUtil.apiStdcall(), LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer, LibFFI.ffi_type_uint32, LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer));

    @Override
    default public Callback.Descriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    default public void callback(long ret, long args2) {
        long __result = this.invoke(MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args2)), MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args2 + (long)POINTER_SIZE)), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args2 + (long)(2 * POINTER_SIZE))), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args2 + (long)(3 * POINTER_SIZE))));
        APIUtil.apiClosureRetP(ret, __result);
    }

    @NativeType(value="LRESULT")
    public long invoke(@NativeType(value="HWND") long var1, @NativeType(value="UINT") int var3, @NativeType(value="WPARAM") long var4, @NativeType(value="LPARAM") long var6);
}

