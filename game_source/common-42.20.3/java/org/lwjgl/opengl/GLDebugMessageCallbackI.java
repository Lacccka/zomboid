/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.opengl;

import java.lang.invoke.MethodHandles;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.libffi.LibFFI;

@FunctionalInterface
@NativeType(value="GLDEBUGPROC")
public interface GLDebugMessageCallbackI
extends CallbackI {
    public static final Callback.Descriptor DESCRIPTOR = new Callback.Descriptor(MethodHandles.lookup(), APIUtil.apiCreateCIF(APIUtil.apiStdcall(), LibFFI.ffi_type_void, LibFFI.ffi_type_uint32, LibFFI.ffi_type_uint32, LibFFI.ffi_type_uint32, LibFFI.ffi_type_uint32, LibFFI.ffi_type_sint32, LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer));

    @Override
    default public Callback.Descriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    default public void callback(long ret, long args2) {
        this.invoke(MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args2)), MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args2 + (long)POINTER_SIZE)), MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args2 + (long)(2 * POINTER_SIZE))), MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args2 + (long)(3 * POINTER_SIZE))), MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args2 + (long)(4 * POINTER_SIZE))), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args2 + (long)(5 * POINTER_SIZE))), MemoryUtil.memGetAddress(MemoryUtil.memGetAddress(args2 + (long)(6 * POINTER_SIZE))));
    }

    public void invoke(@NativeType(value="GLenum") int var1, @NativeType(value="GLenum") int var2, @NativeType(value="GLuint") int var3, @NativeType(value="GLenum") int var4, @NativeType(value="GLsizei") int var5, @NativeType(value="GLchar const *") long var6, @NativeType(value="void const *") long var8);
}

