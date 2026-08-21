/*
 * Decompiled with CFR 0.152.
 */
package oshi.ffm.mac;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import oshi.ffm.ForeignFunctions;

public final class DiskArbitrationFunctions
extends ForeignFunctions {
    private static final MethodHandle DASessionCreate = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("DASessionCreate"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle DADiskCreateFromBSDName = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("DADiskCreateFromBSDName"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle DADiskCreateFromIOMedia = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("DADiskCreateFromIOMedia"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle DADiskCopyDescription = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("DADiskCopyDescription"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);
    private static final MethodHandle DADiskGetBSDName = LINKER.downcallHandle(SYMBOL_LOOKUP.findOrThrow("DADiskGetBSDName"), FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS), new Linker.Option[0]);

    public static MemorySegment DASessionCreate(MemorySegment allocator) {
        try {
            return DASessionCreate.invokeExact(allocator);
        }
        catch (Throwable e) {
            return MemorySegment.NULL;
        }
    }

    public static MemorySegment DADiskCreateFromBSDName(MemorySegment allocator, MemorySegment session, MemorySegment bsdName) {
        try {
            return DADiskCreateFromBSDName.invokeExact(allocator, session, bsdName);
        }
        catch (Throwable e) {
            return MemorySegment.NULL;
        }
    }

    public static MemorySegment DADiskCreateFromIOMedia(MemorySegment allocator, MemorySegment session, MemorySegment media) {
        try {
            return DADiskCreateFromIOMedia.invokeExact(allocator, session, media);
        }
        catch (Throwable e) {
            return MemorySegment.NULL;
        }
    }

    public static MemorySegment DADiskCopyDescription(MemorySegment disk) {
        try {
            return DADiskCopyDescription.invokeExact(disk);
        }
        catch (Throwable e) {
            return MemorySegment.NULL;
        }
    }

    public static MemorySegment DADiskGetBSDName(MemorySegment disk) {
        try {
            return DADiskGetBSDName.invokeExact(disk);
        }
        catch (Throwable e) {
            return MemorySegment.NULL;
        }
    }
}

