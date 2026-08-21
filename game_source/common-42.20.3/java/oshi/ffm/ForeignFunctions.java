/*
 * Decompiled with CFR 0.152.
 */
package oshi.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

public abstract class ForeignFunctions {
    protected static final Linker LINKER = Linker.nativeLinker();
    protected static final Arena LIBRARY_ARENA = Arena.ofAuto();
    protected static final SymbolLookup SYMBOL_LOOKUP = SymbolLookup.loaderLookup();
    protected static final Linker.Option CAPTURE_CALL_STATE = Linker.Option.captureCallState("errno");
    public static final StructLayout CAPTURED_STATE_LAYOUT = Linker.Option.captureStateLayout();
    protected static final VarHandle ERRNO_HANDLE = CAPTURED_STATE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("errno"));

    protected ForeignFunctions() {
    }

    public static SymbolLookup libraryLookup(String libraryName) {
        return SymbolLookup.libraryLookup(System.mapLibraryName(libraryName), LIBRARY_ARENA);
    }

    public static MemorySegment getStructFromNativePointer(MemorySegment pointer, StructLayout layout, Arena arena) {
        if (pointer == null || pointer.equals(MemorySegment.NULL)) {
            return null;
        }
        return MemorySegment.ofAddress(pointer.address()).reinterpret(layout.byteSize(), arena, null);
    }

    public static String getStringFromNativePointer(MemorySegment pointer, Arena arena) {
        if (pointer == null || pointer.equals(MemorySegment.NULL)) {
            return null;
        }
        return MemorySegment.ofAddress(pointer.address()).reinterpret(1024L, arena, null).getString(0L);
    }

    public static byte[] getByteArrayFromNativePointer(MemorySegment pointer, long length, Arena arena) {
        if (pointer == null || pointer.equals(MemorySegment.NULL)) {
            return null;
        }
        MemorySegment bytesSegment = MemorySegment.ofAddress(pointer.address()).reinterpret(length, arena, null);
        byte[] result = new byte[(int)length];
        MemorySegment.copy(bytesSegment, ValueLayout.JAVA_BYTE, 0L, result, 0, (int)length);
        return result;
    }

    public static SymbolLookup lib(String name) {
        return SymbolLookup.libraryLookup(name, Arena.global());
    }

    public static MethodHandle downcall(SymbolLookup lib, String symbol, MemoryLayout resLayout, MemoryLayout ... argLayouts) {
        MemorySegment sym = lib.findOrThrow(symbol);
        FunctionDescriptor fd = resLayout == null ? FunctionDescriptor.ofVoid(argLayouts) : FunctionDescriptor.of(resLayout, argLayouts);
        return LINKER.downcallHandle(sym, fd, new Linker.Option[0]);
    }

    public static int getErrno(MemorySegment callState) {
        return ERRNO_HANDLE.get(callState, 0);
    }
}

