/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryManage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.jni.JNINativeInterface;
import org.lwjgl.system.libffi.FFIClosure;
import org.lwjgl.system.libffi.LibFFI;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
final class Upcalls {
    private static final boolean DEBUG_ALLOCATOR = Configuration.DEBUG_MEMORY_ALLOCATOR.get(false);
    private static final int CLOSURE_SIZE = (int)LibFFI.ffi_get_closure_size();
    private static final ClosureRegistry CLOSURE_REGISTRY;
    private static final long CALLBACK_HANDLER;

    private Upcalls() {
    }

    private static native long getCallbackHandler(Method var0);

    static long upcallCreate(Callback.Descriptor descriptor, Object instance) {
        long executableAddress;
        FFIClosure closure;
        try (MemoryStack stack = MemoryStack.stackPush();){
            PointerBuffer code = stack.mallocPointer(1);
            closure = LibFFI.ffi_closure_alloc(CLOSURE_SIZE, code);
            if (closure == null) {
                throw new OutOfMemoryError();
            }
            executableAddress = code.get(0);
            if (DEBUG_ALLOCATOR) {
                MemoryManage.DebugAllocator.track(executableAddress, CLOSURE_SIZE);
            }
        }
        long user_data = JNINativeInterface.NewGlobalRef(instance);
        int errcode = LibFFI.ffi_prep_closure_loc(closure, descriptor.cif, CALLBACK_HANDLER, user_data, executableAddress);
        if (errcode != 0) {
            JNINativeInterface.DeleteGlobalRef(user_data);
            LibFFI.ffi_closure_free(closure);
            throw new RuntimeException("Failed to prepare the libffi closure");
        }
        CLOSURE_REGISTRY.put(executableAddress, closure);
        return executableAddress;
    }

    static <T extends CallbackI> T upcallGet(long functionPointer) {
        return (T)((CallbackI)MemoryUtil.memGlobalRefToObject(CLOSURE_REGISTRY.get(functionPointer).user_data()));
    }

    static void upcallFree(long functionPointer) {
        if (DEBUG_ALLOCATOR) {
            MemoryManage.DebugAllocator.untrack(functionPointer);
        }
        FFIClosure closure = CLOSURE_REGISTRY.remove(functionPointer);
        JNINativeInterface.DeleteGlobalRef(closure.user_data());
        LibFFI.ffi_closure_free(closure);
    }

    static {
        try (MemoryStack stack = MemoryStack.stackPush();){
            PointerBuffer code = stack.mallocPointer(1);
            FFIClosure closure = LibFFI.ffi_closure_alloc(CLOSURE_SIZE, code);
            if (closure == null) {
                throw new OutOfMemoryError();
            }
            if (code.get(0) == closure.address()) {
                APIUtil.apiLog("Closure Registry: simple");
                CLOSURE_REGISTRY = new ClosureRegistry(){

                    @Override
                    public void put(long executableAddress, FFIClosure closure) {
                    }

                    @Override
                    public FFIClosure get(long executableAddress) {
                        return FFIClosure.create(executableAddress);
                    }

                    @Override
                    public FFIClosure remove(long executableAddress) {
                        return this.get(executableAddress);
                    }
                };
            } else {
                APIUtil.apiLog("Closure Registry: ConcurrentHashMap");
                CLOSURE_REGISTRY = new ClosureRegistry(){
                    private final ConcurrentHashMap<Long, FFIClosure> map = new ConcurrentHashMap();

                    @Override
                    public void put(long executableAddress, FFIClosure closure) {
                        this.map.put(executableAddress, closure);
                    }

                    @Override
                    public FFIClosure get(long executableAddress) {
                        return this.map.get(executableAddress);
                    }

                    @Override
                    public FFIClosure remove(long executableAddress) {
                        return this.map.remove(executableAddress);
                    }
                };
            }
            LibFFI.ffi_closure_free(closure);
        }
        try {
            CALLBACK_HANDLER = Upcalls.getCallbackHandler(CallbackI.class.getDeclaredMethod("callback", Long.TYPE, Long.TYPE));
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to initialize the native callback handler.", e);
        }
        MemoryUtil.getAllocator();
    }

    private static interface ClosureRegistry {
        public void put(long var1, FFIClosure var3);

        public FFIClosure get(long var1);

        public FFIClosure remove(long var1);
    }
}

