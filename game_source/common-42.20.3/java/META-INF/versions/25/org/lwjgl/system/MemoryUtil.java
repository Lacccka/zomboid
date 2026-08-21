/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.runtime.SwitchBootstraps;
import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lwjgl.CLongBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Checks;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.Library;
import org.lwjgl.system.MathUtil;
import org.lwjgl.system.MemoryManage;
import org.lwjgl.system.MemoryUtilTunables;
import org.lwjgl.system.MultiReleaseTextDecoding;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.Struct;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
@NullMarked
public final class MemoryUtil {
    public static final long NULL = 0L;
    public static final int PAGE_SIZE;
    public static final int CACHE_LINE_SIZE;
    static final int ARRAY_TLC_SIZE;
    static final ThreadLocal<byte[]> ARRAY_TLC_BYTE;
    static final ThreadLocal<char[]> ARRAY_TLC_CHAR;
    static final ByteOrder NATIVE_ORDER;
    private static final Charset UTF16;
    private static final int MAX_BUFFER_SIZE = 0x7FFFFFF7;
    private static final VarHandle VH_JAVA_BYTE;
    private static final VarHandle VH_JAVA_SHORT;
    private static final VarHandle VH_JAVA_INT;
    private static final VarHandle VH_JAVA_LONG;
    private static final VarHandle VH_JAVA_FLOAT;
    private static final VarHandle VH_JAVA_DOUBLE;
    private static final VarHandle VH_CLONG;
    private static final VarHandle VH_ADDRESS;

    private MemoryUtil() {
    }

    public static MemoryAllocator getAllocator() {
        return MemoryUtil.getAllocator(false);
    }

    public static MemoryAllocator getAllocator(boolean tracked) {
        return tracked ? LazyInit.ALLOCATOR : LazyInit.ALLOCATOR_IMPL;
    }

    public static long nmemAlloc(long size) {
        return LazyInit.ALLOCATOR.malloc(size);
    }

    public static long nmemAllocChecked(long size) {
        long address = MemoryUtil.nmemAlloc(size != 0L ? size : 1L);
        if (Checks.CHECKS && address == 0L) {
            throw new OutOfMemoryError();
        }
        return address;
    }

    private static long getAllocationSize(int elements, int elementShift) {
        return APIUtil.apiCheckAllocation(elements, Integer.toUnsignedLong(elements) << elementShift, Pointer.BITS64 ? Long.MAX_VALUE : 0xFFFFFFFFL);
    }

    public static ByteBuffer memAlloc(int size) {
        return MemoryUtil.wrapBufferByte(MemoryUtil.nmemAllocChecked(size), size);
    }

    public static ShortBuffer memAllocShort(int size) {
        return MemoryUtil.wrapBufferShort(MemoryUtil.nmemAllocChecked(MemoryUtil.getAllocationSize(size, 1)), size);
    }

    public static IntBuffer memAllocInt(int size) {
        return MemoryUtil.wrapBufferInt(MemoryUtil.nmemAllocChecked(MemoryUtil.getAllocationSize(size, 2)), size);
    }

    public static FloatBuffer memAllocFloat(int size) {
        return MemoryUtil.wrapBufferFloat(MemoryUtil.nmemAllocChecked(MemoryUtil.getAllocationSize(size, 2)), size);
    }

    public static LongBuffer memAllocLong(int size) {
        return MemoryUtil.wrapBufferLong(MemoryUtil.nmemAllocChecked(MemoryUtil.getAllocationSize(size, 3)), size);
    }

    public static CLongBuffer memAllocCLong(int size) {
        return CLongBuffer.create(MemoryUtil.nmemAllocChecked(MemoryUtil.getAllocationSize(size, Pointer.CLONG_SHIFT)), size);
    }

    public static DoubleBuffer memAllocDouble(int size) {
        return MemoryUtil.wrapBufferDouble(MemoryUtil.nmemAllocChecked(MemoryUtil.getAllocationSize(size, 3)), size);
    }

    public static PointerBuffer memAllocPointer(int size) {
        return PointerBuffer.create(MemoryUtil.nmemAllocChecked(MemoryUtil.getAllocationSize(size, Pointer.POINTER_SHIFT)), size);
    }

    public static void nmemFree(long ptr) {
        LazyInit.ALLOCATOR.free(ptr);
    }

    public static void memFree(@Nullable Buffer ptr) {
        if (ptr != null) {
            MemoryUtil.nmemFree(MemoryUtil.memAddress0(ptr));
        }
    }

    public static void memFree(@Nullable ByteBuffer ptr) {
        if (ptr != null) {
            MemoryUtil.nmemFree(MemorySegment.ofBuffer(ptr).address() - (long)ptr.position());
        }
    }

    public static void memFree(@Nullable ShortBuffer ptr) {
        if (ptr != null) {
            MemoryUtil.nmemFree(MemorySegment.ofBuffer(ptr).address() - ((long)ptr.position() << 1));
        }
    }

    public static void memFree(@Nullable CharBuffer ptr) {
        if (ptr != null) {
            MemoryUtil.nmemFree(MemorySegment.ofBuffer(ptr).address() - ((long)ptr.position() << 1));
        }
    }

    public static void memFree(@Nullable IntBuffer ptr) {
        if (ptr != null) {
            MemoryUtil.nmemFree(MemorySegment.ofBuffer(ptr).address() - ((long)ptr.position() << 2));
        }
    }

    public static void memFree(@Nullable LongBuffer ptr) {
        if (ptr != null) {
            MemoryUtil.nmemFree(MemorySegment.ofBuffer(ptr).address() - ((long)ptr.position() << 3));
        }
    }

    public static void memFree(@Nullable FloatBuffer ptr) {
        if (ptr != null) {
            MemoryUtil.nmemFree(MemorySegment.ofBuffer(ptr).address() - ((long)ptr.position() << 2));
        }
    }

    public static void memFree(@Nullable DoubleBuffer ptr) {
        if (ptr != null) {
            MemoryUtil.nmemFree(MemorySegment.ofBuffer(ptr).address() - ((long)ptr.position() << 3));
        }
    }

    public static void memFree(@Nullable CustomBuffer<?> ptr) {
        if (ptr != null) {
            MemoryUtil.nmemFree(ptr.address);
        }
    }

    public static long nmemCalloc(long num, long size) {
        return LazyInit.ALLOCATOR.calloc(num, size);
    }

    public static long nmemCallocChecked(long num, long size) {
        if (num == 0L || size == 0L) {
            num = 1L;
            size = 1L;
        }
        long address = MemoryUtil.nmemCalloc(num, size);
        if (Checks.CHECKS && address == 0L) {
            throw new OutOfMemoryError();
        }
        return address;
    }

    public static ByteBuffer memCalloc(int num, int size) {
        return MemoryUtil.wrapBufferByte(MemoryUtil.nmemCallocChecked(num, size), num * size);
    }

    public static ByteBuffer memCalloc(int num) {
        return MemoryUtil.wrapBufferByte(MemoryUtil.nmemCallocChecked(num, 1L), num);
    }

    public static ShortBuffer memCallocShort(int num) {
        return MemoryUtil.wrapBufferShort(MemoryUtil.nmemCallocChecked(num, 2L), num);
    }

    public static IntBuffer memCallocInt(int num) {
        return MemoryUtil.wrapBufferInt(MemoryUtil.nmemCallocChecked(num, 4L), num);
    }

    public static FloatBuffer memCallocFloat(int num) {
        return MemoryUtil.wrapBufferFloat(MemoryUtil.nmemCallocChecked(num, 4L), num);
    }

    public static LongBuffer memCallocLong(int num) {
        return MemoryUtil.wrapBufferLong(MemoryUtil.nmemCallocChecked(num, 8L), num);
    }

    public static CLongBuffer memCallocCLong(int num) {
        return CLongBuffer.create(MemoryUtil.nmemCallocChecked(num, Pointer.CLONG_SIZE), num);
    }

    public static DoubleBuffer memCallocDouble(int num) {
        return MemoryUtil.wrapBufferDouble(MemoryUtil.nmemCallocChecked(num, 8L), num);
    }

    public static PointerBuffer memCallocPointer(int num) {
        return PointerBuffer.create(MemoryUtil.nmemCallocChecked(num, Pointer.POINTER_SIZE), num);
    }

    public static long nmemRealloc(long ptr, long size) {
        return LazyInit.ALLOCATOR.realloc(ptr, size);
    }

    public static long nmemReallocChecked(long ptr, long size) {
        long address = MemoryUtil.nmemRealloc(ptr, size != 0L ? size : 1L);
        if (Checks.CHECKS && address == 0L) {
            throw new OutOfMemoryError();
        }
        return address;
    }

    private static <T extends Buffer> T realloc(@Nullable T old_p, T new_p, int size) {
        if (old_p != null) {
            new_p.position(Math.min(old_p.position(), size));
        }
        return new_p;
    }

    public static ByteBuffer memRealloc(@Nullable ByteBuffer ptr, int size) {
        return MemoryUtil.realloc(ptr, MemoryUtil.memByteBuffer(MemoryUtil.nmemReallocChecked(ptr == null ? 0L : MemoryUtil.memAddress0(ptr), size), size), size);
    }

    public static ShortBuffer memRealloc(@Nullable ShortBuffer ptr, int size) {
        return MemoryUtil.realloc(ptr, MemoryUtil.memShortBuffer(MemoryUtil.nmemReallocChecked(ptr == null ? 0L : MemoryUtil.memAddress0(ptr), MemoryUtil.getAllocationSize(size, 1)), size), size);
    }

    public static IntBuffer memRealloc(@Nullable IntBuffer ptr, int size) {
        return MemoryUtil.realloc(ptr, MemoryUtil.memIntBuffer(MemoryUtil.nmemReallocChecked(ptr == null ? 0L : MemoryUtil.memAddress0(ptr), MemoryUtil.getAllocationSize(size, 2)), size), size);
    }

    public static LongBuffer memRealloc(@Nullable LongBuffer ptr, int size) {
        return MemoryUtil.realloc(ptr, MemoryUtil.memLongBuffer(MemoryUtil.nmemReallocChecked(ptr == null ? 0L : MemoryUtil.memAddress0(ptr), MemoryUtil.getAllocationSize(size, 3)), size), size);
    }

    public static CLongBuffer memRealloc(@Nullable CLongBuffer ptr, int size) {
        CLongBuffer buffer = MemoryUtil.memCLongBuffer(MemoryUtil.nmemReallocChecked(ptr == null ? 0L : ptr.address, MemoryUtil.getAllocationSize(size, Pointer.CLONG_SIZE)), size);
        if (ptr != null) {
            buffer.position(Math.min(ptr.position(), size));
        }
        return buffer;
    }

    public static FloatBuffer memRealloc(@Nullable FloatBuffer ptr, int size) {
        return MemoryUtil.realloc(ptr, MemoryUtil.memFloatBuffer(MemoryUtil.nmemReallocChecked(ptr == null ? 0L : MemoryUtil.memAddress0(ptr), MemoryUtil.getAllocationSize(size, 2)), size), size);
    }

    public static DoubleBuffer memRealloc(@Nullable DoubleBuffer ptr, int size) {
        return MemoryUtil.realloc(ptr, MemoryUtil.memDoubleBuffer(MemoryUtil.nmemReallocChecked(ptr == null ? 0L : MemoryUtil.memAddress0(ptr), MemoryUtil.getAllocationSize(size, 3)), size), size);
    }

    public static PointerBuffer memRealloc(@Nullable PointerBuffer ptr, int size) {
        PointerBuffer buffer = MemoryUtil.memPointerBuffer(MemoryUtil.nmemReallocChecked(ptr == null ? 0L : ptr.address, MemoryUtil.getAllocationSize(size, Pointer.POINTER_SHIFT)), size);
        if (ptr != null) {
            buffer.position(Math.min(ptr.position(), size));
        }
        return buffer;
    }

    public static long nmemAlignedAlloc(long alignment, long size) {
        return LazyInit.ALLOCATOR.aligned_alloc(alignment, size);
    }

    public static long nmemAlignedAllocChecked(long alignment, long size) {
        long address = MemoryUtil.nmemAlignedAlloc(alignment, size != 0L ? size : 1L);
        if (Checks.CHECKS && address == 0L) {
            throw new OutOfMemoryError();
        }
        return address;
    }

    public static ByteBuffer memAlignedAlloc(int alignment, int size) {
        return MemoryUtil.wrapBufferByte(MemoryUtil.nmemAlignedAllocChecked(alignment, size), size);
    }

    public static void nmemAlignedFree(long ptr) {
        LazyInit.ALLOCATOR.aligned_free(ptr);
    }

    public static void memAlignedFree(@Nullable ByteBuffer ptr) {
        if (ptr != null) {
            MemoryUtil.nmemAlignedFree(MemorySegment.ofBuffer(ptr).address() - (long)ptr.position());
        }
    }

    public static void memReport(MemoryAllocationReport report) {
        MemoryManage.DebugAllocator.report(report);
    }

    public static void memReport(MemoryAllocationReport report, MemoryAllocationReport.Aggregate groupByStackTrace, boolean groupByThread) {
        MemoryManage.DebugAllocator.report(report, groupByStackTrace, groupByThread);
    }

    public static long memAddress0(Buffer buffer) {
        int n;
        Buffer buffer2 = buffer;
        Objects.requireNonNull(buffer2);
        Buffer buffer3 = buffer2;
        int n2 = 0;
        block5: while (true) {
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ByteBuffer.class, ShortBuffer.class, CharBuffer.class, IntBuffer.class, FloatBuffer.class}, (Buffer)buffer3, n2)) {
                case 0: {
                    n = 0;
                    break block5;
                }
                case 1: 
                case 2: {
                    if (!(buffer3 instanceof ShortBuffer) && !(buffer3 instanceof CharBuffer)) {
                        n2 = 3;
                        continue block5;
                    }
                    n = 1;
                    break block5;
                }
                case 3: 
                case 4: {
                    if (!(buffer3 instanceof IntBuffer) && !(buffer3 instanceof FloatBuffer)) {
                        n2 = 5;
                        continue block5;
                    }
                    n = 2;
                    break block5;
                }
                default: {
                    n = 3;
                    break block5;
                }
            }
            break;
        }
        int elementShift = n;
        return MemorySegment.ofBuffer(buffer).address() - ((long)buffer.position() << elementShift);
    }

    public static long memAddress0(ByteBuffer buffer) {
        return MemorySegment.ofBuffer(buffer).address() - (long)buffer.position();
    }

    public static long memAddress0(ShortBuffer buffer) {
        return MemorySegment.ofBuffer(buffer).address() - ((long)buffer.position() << 1);
    }

    public static long memAddress0(CharBuffer buffer) {
        return MemorySegment.ofBuffer(buffer).address() - ((long)buffer.position() << 1);
    }

    public static long memAddress0(IntBuffer buffer) {
        return MemorySegment.ofBuffer(buffer).address() - ((long)buffer.position() << 2);
    }

    public static long memAddress0(LongBuffer buffer) {
        return MemorySegment.ofBuffer(buffer).address() - ((long)buffer.position() << 3);
    }

    public static long memAddress0(FloatBuffer buffer) {
        return MemorySegment.ofBuffer(buffer).address() - ((long)buffer.position() << 2);
    }

    public static long memAddress0(DoubleBuffer buffer) {
        return MemorySegment.ofBuffer(buffer).address() - ((long)buffer.position() << 3);
    }

    public static long memAddress(ByteBuffer buffer) {
        return MemorySegment.ofBuffer(buffer).address();
    }

    public static long memAddress(ByteBuffer buffer, int position) {
        return MemorySegment.ofBuffer(buffer).address() + Integer.toUnsignedLong(position) - (long)buffer.position();
    }

    private static long address(Buffer buffer, int position, int elementShift) {
        return MemorySegment.ofBuffer(buffer).address() + (Integer.toUnsignedLong(position) - (long)buffer.position() << elementShift);
    }

    public static long memAddress(ShortBuffer buffer) {
        return MemorySegment.ofBuffer(buffer).address();
    }

    public static long memAddress(ShortBuffer buffer, int position) {
        return MemoryUtil.address(buffer, position, 1);
    }

    public static long memAddress(CharBuffer buffer) {
        return MemorySegment.ofBuffer(buffer).address();
    }

    public static long memAddress(CharBuffer buffer, int position) {
        return MemoryUtil.address(buffer, position, 1);
    }

    public static long memAddress(IntBuffer buffer) {
        return MemorySegment.ofBuffer(buffer).address();
    }

    public static long memAddress(IntBuffer buffer, int position) {
        return MemoryUtil.address(buffer, position, 2);
    }

    public static long memAddress(FloatBuffer buffer) {
        return MemorySegment.ofBuffer(buffer).address();
    }

    public static long memAddress(FloatBuffer buffer, int position) {
        return MemoryUtil.address(buffer, position, 2);
    }

    public static long memAddress(LongBuffer buffer) {
        return MemorySegment.ofBuffer(buffer).address();
    }

    public static long memAddress(LongBuffer buffer, int position) {
        return MemoryUtil.address(buffer, position, 3);
    }

    public static long memAddress(DoubleBuffer buffer) {
        return MemorySegment.ofBuffer(buffer).address();
    }

    public static long memAddress(DoubleBuffer buffer, int position) {
        return MemoryUtil.address(buffer, position, 3);
    }

    public static long memAddress(Buffer buffer) {
        return MemorySegment.ofBuffer(buffer).address();
    }

    public static long memAddress(CustomBuffer<?> buffer) {
        return buffer.address();
    }

    public static long memAddress(CustomBuffer<?> buffer, int position) {
        return buffer.address(position);
    }

    public static long memAddressSafe(@Nullable ByteBuffer buffer) {
        return buffer == null ? 0L : MemorySegment.ofBuffer(buffer).address();
    }

    public static long memAddressSafe(@Nullable ShortBuffer buffer) {
        return buffer == null ? 0L : MemorySegment.ofBuffer(buffer).address();
    }

    public static long memAddressSafe(@Nullable CharBuffer buffer) {
        return buffer == null ? 0L : MemorySegment.ofBuffer(buffer).address();
    }

    public static long memAddressSafe(@Nullable IntBuffer buffer) {
        return buffer == null ? 0L : MemorySegment.ofBuffer(buffer).address();
    }

    public static long memAddressSafe(@Nullable FloatBuffer buffer) {
        return buffer == null ? 0L : MemorySegment.ofBuffer(buffer).address();
    }

    public static long memAddressSafe(@Nullable LongBuffer buffer) {
        return buffer == null ? 0L : MemorySegment.ofBuffer(buffer).address();
    }

    public static long memAddressSafe(@Nullable DoubleBuffer buffer) {
        return buffer == null ? 0L : MemorySegment.ofBuffer(buffer).address();
    }

    public static long memAddressSafe(@Nullable Pointer pointer) {
        return pointer == null ? 0L : pointer.address();
    }

    public static ByteBuffer memByteBuffer(long address, int capacity) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return MemoryUtil.wrapBufferByte(address, capacity);
    }

    public static @Nullable ByteBuffer memByteBufferSafe(long address, int capacity) {
        return address == 0L ? null : MemoryUtil.wrapBufferByte(address, capacity);
    }

    public static ByteBuffer memByteBuffer(ShortBuffer buffer) {
        return MemoryUtil.wrapBufferByte(MemoryUtil.memAddress(buffer), buffer.remaining() << 1);
    }

    public static ByteBuffer memByteBuffer(CharBuffer buffer) {
        return MemoryUtil.wrapBufferByte(MemoryUtil.memAddress(buffer), buffer.remaining() << 1);
    }

    public static ByteBuffer memByteBuffer(IntBuffer buffer) {
        return MemoryUtil.wrapBufferByte(MemoryUtil.memAddress(buffer), buffer.remaining() << 2);
    }

    public static ByteBuffer memByteBuffer(LongBuffer buffer) {
        return MemoryUtil.wrapBufferByte(MemoryUtil.memAddress(buffer), buffer.remaining() << 3);
    }

    public static ByteBuffer memByteBuffer(FloatBuffer buffer) {
        return MemoryUtil.wrapBufferByte(MemoryUtil.memAddress(buffer), buffer.remaining() << 2);
    }

    public static ByteBuffer memByteBuffer(DoubleBuffer buffer) {
        return MemoryUtil.wrapBufferByte(MemoryUtil.memAddress(buffer), buffer.remaining() << 3);
    }

    public static ByteBuffer memByteBuffer(CustomBuffer<?> buffer) {
        if (Checks.CHECKS && 0x7FFFFFF7 / buffer.sizeof() < buffer.remaining()) {
            throw new IllegalArgumentException("The source buffer range is too wide");
        }
        return MemoryUtil.wrapBufferByte(MemoryUtil.memAddress(buffer), buffer.remaining() * buffer.sizeof());
    }

    public static <T extends Struct<T>> ByteBuffer memByteBuffer(T value) {
        return MemoryUtil.wrapBufferByte(value.address, value.sizeof());
    }

    public static ShortBuffer memShortBuffer(long address, int capacity) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return MemoryUtil.wrapBufferShort(address, capacity);
    }

    public static @Nullable ShortBuffer memShortBufferSafe(long address, int capacity) {
        return address == 0L ? null : MemoryUtil.wrapBufferShort(address, capacity);
    }

    public static CharBuffer memCharBuffer(long address, int capacity) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return MemoryUtil.wrapBufferChar(address, capacity);
    }

    public static @Nullable CharBuffer memCharBufferSafe(long address, int capacity) {
        return address == 0L ? null : MemoryUtil.wrapBufferChar(address, capacity);
    }

    public static IntBuffer memIntBuffer(long address, int capacity) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return MemoryUtil.wrapBufferInt(address, capacity);
    }

    public static @Nullable IntBuffer memIntBufferSafe(long address, int capacity) {
        return address == 0L ? null : MemoryUtil.wrapBufferInt(address, capacity);
    }

    public static LongBuffer memLongBuffer(long address, int capacity) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return MemoryUtil.wrapBufferLong(address, capacity);
    }

    public static @Nullable LongBuffer memLongBufferSafe(long address, int capacity) {
        return address == 0L ? null : MemoryUtil.wrapBufferLong(address, capacity);
    }

    public static CLongBuffer memCLongBuffer(long address, int capacity) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return CLongBuffer.create(address, capacity);
    }

    public static @Nullable CLongBuffer memCLongBufferSafe(long address, int capacity) {
        return address == 0L ? null : CLongBuffer.create(address, capacity);
    }

    public static FloatBuffer memFloatBuffer(long address, int capacity) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return MemoryUtil.wrapBufferFloat(address, capacity);
    }

    public static @Nullable FloatBuffer memFloatBufferSafe(long address, int capacity) {
        return address == 0L ? null : MemoryUtil.wrapBufferFloat(address, capacity);
    }

    public static DoubleBuffer memDoubleBuffer(long address, int capacity) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return MemoryUtil.wrapBufferDouble(address, capacity);
    }

    public static @Nullable DoubleBuffer memDoubleBufferSafe(long address, int capacity) {
        return address == 0L ? null : MemoryUtil.wrapBufferDouble(address, capacity);
    }

    public static PointerBuffer memPointerBuffer(long address, int capacity) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return PointerBuffer.create(address, capacity);
    }

    public static @Nullable PointerBuffer memPointerBufferSafe(long address, int capacity) {
        return address == 0L ? null : PointerBuffer.create(address, capacity);
    }

    public static ByteBuffer memDuplicate(ByteBuffer buffer) {
        return buffer.duplicate().order(buffer.order());
    }

    public static ShortBuffer memDuplicate(ShortBuffer buffer) {
        return buffer.duplicate();
    }

    public static CharBuffer memDuplicate(CharBuffer buffer) {
        return buffer.duplicate();
    }

    public static IntBuffer memDuplicate(IntBuffer buffer) {
        return buffer.duplicate();
    }

    public static LongBuffer memDuplicate(LongBuffer buffer) {
        return buffer.duplicate();
    }

    public static FloatBuffer memDuplicate(FloatBuffer buffer) {
        return buffer.duplicate();
    }

    public static DoubleBuffer memDuplicate(DoubleBuffer buffer) {
        return buffer.duplicate();
    }

    public static ByteBuffer memSlice(ByteBuffer buffer) {
        return buffer.slice().order(NATIVE_ORDER);
    }

    public static ShortBuffer memSlice(ShortBuffer buffer) {
        return buffer.slice();
    }

    public static CharBuffer memSlice(CharBuffer buffer) {
        return buffer.slice();
    }

    public static IntBuffer memSlice(IntBuffer buffer) {
        return buffer.slice();
    }

    public static LongBuffer memSlice(LongBuffer buffer) {
        return buffer.slice();
    }

    public static FloatBuffer memSlice(FloatBuffer buffer) {
        return buffer.slice();
    }

    public static DoubleBuffer memSlice(DoubleBuffer buffer) {
        return buffer.slice();
    }

    public static ByteBuffer memSlice(ByteBuffer buffer, int offset, int capacity) {
        return buffer.slice(buffer.position() + offset, capacity).order(NATIVE_ORDER);
    }

    public static ShortBuffer memSlice(ShortBuffer buffer, int offset, int capacity) {
        return buffer.slice(buffer.position() + offset, capacity);
    }

    public static CharBuffer memSlice(CharBuffer buffer, int offset, int capacity) {
        return buffer.slice(buffer.position() + offset, capacity);
    }

    public static IntBuffer memSlice(IntBuffer buffer, int offset, int capacity) {
        return buffer.slice(buffer.position() + offset, capacity);
    }

    public static LongBuffer memSlice(LongBuffer buffer, int offset, int capacity) {
        return buffer.slice(buffer.position() + offset, capacity);
    }

    public static FloatBuffer memSlice(FloatBuffer buffer, int offset, int capacity) {
        return buffer.slice(buffer.position() + offset, capacity);
    }

    public static DoubleBuffer memSlice(DoubleBuffer buffer, int offset, int capacity) {
        return buffer.slice(buffer.position() + offset, capacity);
    }

    public static <T extends CustomBuffer<T>> T memSlice(T buffer, int offset, int capacity) {
        return buffer.slice(offset, capacity);
    }

    public static void memSet(ByteBuffer ptr, int value) {
        MemoryUtil.memSet(MemoryUtil.memAddress(ptr), value, ptr.remaining());
    }

    public static void memSet(ShortBuffer ptr, int value) {
        MemoryUtil.memSet(MemoryUtil.memAddress(ptr), value, APIUtil.apiGetBytes(ptr.remaining(), 1));
    }

    public static void memSet(CharBuffer ptr, int value) {
        MemoryUtil.memSet(MemoryUtil.memAddress(ptr), value, APIUtil.apiGetBytes(ptr.remaining(), 1));
    }

    public static void memSet(IntBuffer ptr, int value) {
        MemoryUtil.memSet(MemoryUtil.memAddress(ptr), value, APIUtil.apiGetBytes(ptr.remaining(), 2));
    }

    public static void memSet(LongBuffer ptr, int value) {
        MemoryUtil.memSet(MemoryUtil.memAddress(ptr), value, APIUtil.apiGetBytes(ptr.remaining(), 3));
    }

    public static void memSet(FloatBuffer ptr, int value) {
        MemoryUtil.memSet(MemoryUtil.memAddress(ptr), value, APIUtil.apiGetBytes(ptr.remaining(), 2));
    }

    public static void memSet(DoubleBuffer ptr, int value) {
        MemoryUtil.memSet(MemoryUtil.memAddress(ptr), value, APIUtil.apiGetBytes(ptr.remaining(), 3));
    }

    public static <T extends CustomBuffer<T>> void memSet(T ptr, int value) {
        MemoryUtil.memSet(MemoryUtil.memAddress(ptr), value, Integer.toUnsignedLong(ptr.remaining()) * (long)ptr.sizeof());
    }

    public static <T extends Struct<T>> void memSet(T ptr, int value) {
        MemoryUtil.memSet(ptr.address, value, ptr.sizeof());
    }

    public static void memCopy(ByteBuffer src, ByteBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.remaining());
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), MemoryUtil.memAddress(dst), src.remaining());
    }

    public static void memCopy(ShortBuffer src, ShortBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.remaining());
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), MemoryUtil.memAddress(dst), APIUtil.apiGetBytes(src.remaining(), 1));
    }

    public static void memCopy(CharBuffer src, CharBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.remaining());
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), MemoryUtil.memAddress(dst), APIUtil.apiGetBytes(src.remaining(), 1));
    }

    public static void memCopy(IntBuffer src, IntBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.remaining());
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), MemoryUtil.memAddress(dst), APIUtil.apiGetBytes(src.remaining(), 2));
    }

    public static void memCopy(LongBuffer src, LongBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.remaining());
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), MemoryUtil.memAddress(dst), APIUtil.apiGetBytes(src.remaining(), 3));
    }

    public static void memCopy(FloatBuffer src, FloatBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.remaining());
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), MemoryUtil.memAddress(dst), APIUtil.apiGetBytes(src.remaining(), 2));
    }

    public static void memCopy(DoubleBuffer src, DoubleBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.remaining());
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), MemoryUtil.memAddress(dst), APIUtil.apiGetBytes(src.remaining(), 3));
    }

    public static <T extends CustomBuffer<T>> void memCopy(T src, T dst) {
        if (Checks.CHECKS) {
            Checks.check(dst, src.remaining());
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), MemoryUtil.memAddress(dst), Integer.toUnsignedLong(src.remaining()) * (long)src.sizeof());
    }

    public static <T extends Struct<T>> void memCopy(T src, T dst) {
        MemoryUtilTunables.memcpy(src.address, dst.address, src.sizeof());
    }

    public static void memCopy(byte[] src, ByteBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.length);
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), 0, src.length);
    }

    public static void memCopy(short[] src, ByteBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, APIUtil.apiGetBytes(src.length, 1));
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), 0, src.length);
    }

    public static void memCopy(short[] src, ShortBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.length);
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), 0, src.length);
    }

    public static void memCopy(int[] src, ByteBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, APIUtil.apiGetBytes(src.length, 2));
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), 0, src.length);
    }

    public static void memCopy(int[] src, IntBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.length);
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), 0, src.length);
    }

    public static void memCopy(long[] src, ByteBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, APIUtil.apiGetBytes(src.length, 3));
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), 0, src.length);
    }

    public static void memCopy(long[] src, LongBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.length);
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), 0, src.length);
    }

    public static void memCopy(float[] src, ByteBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, APIUtil.apiGetBytes(src.length, 2));
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), 0, src.length);
    }

    public static void memCopy(float[] src, FloatBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.length);
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), 0, src.length);
    }

    public static void memCopy(double[] src, ByteBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, APIUtil.apiGetBytes(src.length, 3));
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), 0, src.length);
    }

    public static void memCopy(double[] src, DoubleBuffer dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, src.length);
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), 0, src.length);
    }

    public static void memCopy(byte[] src, ByteBuffer dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, size);
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), offset, size);
    }

    public static void memCopy(short[] src, ByteBuffer dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, APIUtil.apiGetBytes(size, 1));
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), offset, size);
    }

    public static void memCopy(short[] src, ShortBuffer dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, size);
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), offset, size);
    }

    public static void memCopy(int[] src, ByteBuffer dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, APIUtil.apiGetBytes(size, 2));
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), offset, size);
    }

    public static void memCopy(int[] src, IntBuffer dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, size);
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), offset, size);
    }

    public static void memCopy(long[] src, ByteBuffer dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, APIUtil.apiGetBytes(size, 3));
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), offset, size);
    }

    public static void memCopy(long[] src, LongBuffer dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, size);
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), offset, size);
    }

    public static void memCopy(float[] src, ByteBuffer dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, APIUtil.apiGetBytes(size, 2));
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), offset, size);
    }

    public static void memCopy(float[] src, FloatBuffer dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, size);
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), offset, size);
    }

    public static void memCopy(double[] src, ByteBuffer dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, APIUtil.apiGetBytes(size, 3));
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), offset, size);
    }

    public static void memCopy(double[] src, DoubleBuffer dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)dst, size);
        }
        MemoryUtilTunables.memcpy(src, MemoryUtil.memAddress(dst), offset, size);
    }

    public static void memCopy(ByteBuffer src, byte[] dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, dst.length);
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, 0, dst.length);
    }

    public static void memCopy(ByteBuffer src, short[] dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, APIUtil.apiGetBytes(dst.length, 1));
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, 0, dst.length);
    }

    public static void memCopy(ShortBuffer src, short[] dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, dst.length);
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, 0, dst.length);
    }

    public static void memCopy(ByteBuffer src, int[] dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, APIUtil.apiGetBytes(dst.length, 2));
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, 0, dst.length);
    }

    public static void memCopy(IntBuffer src, int[] dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, dst.length);
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, 0, dst.length);
    }

    public static void memCopy(ByteBuffer src, long[] dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, APIUtil.apiGetBytes(dst.length, 3));
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, 0, dst.length);
    }

    public static void memCopy(LongBuffer src, long[] dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, dst.length);
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, 0, dst.length);
    }

    public static void memCopy(ByteBuffer src, float[] dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, APIUtil.apiGetBytes(dst.length, 2));
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, 0, dst.length);
    }

    public static void memCopy(FloatBuffer src, float[] dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, dst.length);
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, 0, dst.length);
    }

    public static void memCopy(ByteBuffer src, double[] dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, APIUtil.apiGetBytes(dst.length, 3));
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, 0, dst.length);
    }

    public static void memCopy(DoubleBuffer src, double[] dst) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, dst.length);
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, 0, dst.length);
    }

    public static void memCopy(ByteBuffer src, byte[] dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, size);
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, offset, size);
    }

    public static void memCopy(ByteBuffer src, short[] dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, APIUtil.apiGetBytes(size, 1));
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, offset, size);
    }

    public static void memCopy(ShortBuffer src, short[] dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, size);
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, offset, size);
    }

    public static void memCopy(ByteBuffer src, int[] dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, APIUtil.apiGetBytes(size, 2));
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, offset, size);
    }

    public static void memCopy(IntBuffer src, int[] dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, size);
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, offset, size);
    }

    public static void memCopy(ByteBuffer src, long[] dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, APIUtil.apiGetBytes(size, 3));
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, offset, size);
    }

    public static void memCopy(LongBuffer src, long[] dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, size);
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, offset, size);
    }

    public static void memCopy(ByteBuffer src, float[] dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, APIUtil.apiGetBytes(size, 2));
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, offset, size);
    }

    public static void memCopy(FloatBuffer src, float[] dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, size);
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, offset, size);
    }

    public static void memCopy(ByteBuffer src, double[] dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, APIUtil.apiGetBytes(size, 3));
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, offset, size);
    }

    public static void memCopy(DoubleBuffer src, double[] dst, int offset, int size) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)src, size);
        }
        MemoryUtilTunables.memcpy(MemoryUtil.memAddress(src), dst, offset, size);
    }

    public static void memSet(long ptr, int value, long bytes) {
        if (Checks.DEBUG && (ptr == 0L || bytes < 0L)) {
            throw new IllegalArgumentException();
        }
        MemoryUtilTunables.memset(ptr, value, bytes);
    }

    public static void memCopy(long src, long dst, long bytes) {
        if (Checks.DEBUG && (src == 0L || dst == 0L || bytes < 0L)) {
            throw new IllegalArgumentException();
        }
        MemoryUtilTunables.memcpy(src, dst, bytes);
    }

    public static void memCopy(byte[] src, long dst) {
        MemoryUtilTunables.memcpy(src, dst, 0, src.length);
    }

    public static void memCopy(short[] src, long dst) {
        MemoryUtilTunables.memcpy(src, dst, 0, src.length);
    }

    public static void memCopy(int[] src, long dst) {
        MemoryUtilTunables.memcpy(src, dst, 0, src.length);
    }

    public static void memCopy(long[] src, long dst) {
        MemoryUtilTunables.memcpy(src, dst, 0, src.length);
    }

    public static void memCopy(float[] src, long dst) {
        MemoryUtilTunables.memcpy(src, dst, 0, src.length);
    }

    public static void memCopy(double[] src, long dst) {
        MemoryUtilTunables.memcpy(src, dst, 0, src.length);
    }

    public static void memCopy(byte[] src, long dst, int offset, int size) {
        MemoryUtilTunables.memcpy(src, dst, offset, size);
    }

    public static void memCopy(short[] src, long dst, int offset, int size) {
        MemoryUtilTunables.memcpy(src, dst, offset, size);
    }

    public static void memCopy(int[] src, long dst, int offset, int size) {
        MemoryUtilTunables.memcpy(src, dst, offset, size);
    }

    public static void memCopy(long[] src, long dst, int offset, int size) {
        MemoryUtilTunables.memcpy(src, dst, offset, size);
    }

    public static void memCopy(float[] src, long dst, int offset, int size) {
        MemoryUtilTunables.memcpy(src, dst, offset, size);
    }

    public static void memCopy(double[] src, long dst, int offset, int size) {
        MemoryUtilTunables.memcpy(src, dst, offset, size);
    }

    public static void memCopy(long src, byte[] dst) {
        MemoryUtilTunables.memcpy(src, dst, 0, dst.length);
    }

    public static void memCopy(long src, short[] dst) {
        MemoryUtilTunables.memcpy(src, dst, 0, dst.length);
    }

    public static void memCopy(long src, int[] dst) {
        MemoryUtilTunables.memcpy(src, dst, 0, dst.length);
    }

    public static void memCopy(long src, long[] dst) {
        MemoryUtilTunables.memcpy(src, dst, 0, dst.length);
    }

    public static void memCopy(long src, float[] dst) {
        MemoryUtilTunables.memcpy(src, dst, 0, dst.length);
    }

    public static void memCopy(long src, double[] dst) {
        MemoryUtilTunables.memcpy(src, dst, 0, dst.length);
    }

    public static void memCopy(long src, byte[] dst, int offset, int size) {
        MemoryUtilTunables.memcpy(src, dst, offset, size);
    }

    public static void memCopy(long src, short[] dst, int offset, int size) {
        MemoryUtilTunables.memcpy(src, dst, offset, size);
    }

    public static void memCopy(long src, int[] dst, int offset, int size) {
        MemoryUtilTunables.memcpy(src, dst, offset, size);
    }

    public static void memCopy(long src, long[] dst, int offset, int size) {
        MemoryUtilTunables.memcpy(src, dst, offset, size);
    }

    public static void memCopy(long src, float[] dst, int offset, int size) {
        MemoryUtilTunables.memcpy(src, dst, offset, size);
    }

    public static void memCopy(long src, double[] dst, int offset, int size) {
        MemoryUtilTunables.memcpy(src, dst, offset, size);
    }

    private static VarHandle createMemoryAccessVH(ValueLayout layout, MethodHandle ofAddress, MethodHandle reinterpret) {
        VarHandle vh = layout.varHandle();
        vh = MethodHandles.insertCoordinates(vh, 1, 0L);
        vh = MethodHandles.filterCoordinates(vh, 0, MethodHandles.filterReturnValue(ofAddress, MethodHandles.insertArguments(reinterpret, 1, layout.byteSize())));
        return vh;
    }

    private static long castAddress32(int ptr) {
        return (long)ptr & 0xFFFFFFFFL;
    }

    public static boolean memGetBoolean(long ptr) {
        return VH_JAVA_BYTE.get(ptr) != 0;
    }

    public static byte memGetByte(long ptr) {
        return VH_JAVA_BYTE.get(ptr);
    }

    public static short memGetShort(long ptr) {
        return VH_JAVA_SHORT.get(ptr);
    }

    public static int memGetInt(long ptr) {
        return VH_JAVA_INT.get(ptr);
    }

    public static long memGetLong(long ptr) {
        return VH_JAVA_LONG.get(ptr);
    }

    public static float memGetFloat(long ptr) {
        return VH_JAVA_FLOAT.get(ptr);
    }

    public static double memGetDouble(long ptr) {
        return VH_JAVA_DOUBLE.get(ptr);
    }

    public static void memPutByte(long ptr, byte value) {
        VH_JAVA_BYTE.set(ptr, value);
    }

    public static void memPutShort(long ptr, short value) {
        VH_JAVA_SHORT.set(ptr, value);
    }

    public static void memPutInt(long ptr, int value) {
        VH_JAVA_INT.set(ptr, value);
    }

    public static void memPutLong(long ptr, long value) {
        VH_JAVA_LONG.set(ptr, value);
    }

    public static void memPutFloat(long ptr, float value) {
        VH_JAVA_FLOAT.set(ptr, value);
    }

    public static void memPutDouble(long ptr, double value) {
        VH_JAVA_DOUBLE.set(ptr, value);
    }

    public static long memGetCLong(long ptr) {
        return VH_CLONG.get(ptr);
    }

    public static long memGetAddress(long ptr) {
        return VH_ADDRESS.get(ptr);
    }

    public static void memPutCLong(long ptr, long value) {
        VH_CLONG.set(ptr, value);
    }

    public static void memPutAddress(long ptr, long value) {
        VH_ADDRESS.set(ptr, value);
    }

    public static boolean memGetBoolean(MemorySegment segment, long offset) {
        return MemoryUtil.memGetBoolean(segment.address() + offset);
    }

    public static byte memGetByte(MemorySegment segment, long offset) {
        return MemoryUtil.memGetByte(segment.address() + offset);
    }

    public static short memGetShort(MemorySegment segment, long offset) {
        return MemoryUtil.memGetShort(segment.address() + offset);
    }

    public static int memGetInt(MemorySegment segment, long offset) {
        return MemoryUtil.memGetInt(segment.address() + offset);
    }

    public static long memGetLong(MemorySegment segment, long offset) {
        return MemoryUtil.memGetLong(segment.address() + offset);
    }

    public static float memGetFloat(MemorySegment segment, long offset) {
        return MemoryUtil.memGetFloat(segment.address() + offset);
    }

    public static double memGetDouble(MemorySegment segment, long offset) {
        return MemoryUtil.memGetDouble(segment.address() + offset);
    }

    public static long memGetCLong(MemorySegment segment, long offset) {
        return MemoryUtil.memGetCLong(segment.address() + offset);
    }

    public static long memGetAddress(MemorySegment segment, long offset) {
        return MemoryUtil.memGetAddress(segment.address() + offset);
    }

    public static void memPutByte(MemorySegment segment, long offset, byte value) {
        MemoryUtil.memPutByte(segment.address() + offset, value);
    }

    public static void memPutShort(MemorySegment segment, long offset, short value) {
        MemoryUtil.memPutShort(segment.address() + offset, value);
    }

    public static void memPutInt(MemorySegment segment, long offset, int value) {
        MemoryUtil.memPutInt(segment.address() + offset, value);
    }

    public static void memPutLong(MemorySegment segment, long offset, long value) {
        MemoryUtil.memPutLong(segment.address() + offset, value);
    }

    public static void memPutFloat(MemorySegment segment, long offset, float value) {
        MemoryUtil.memPutFloat(segment.address() + offset, value);
    }

    public static void memPutDouble(MemorySegment segment, long offset, double value) {
        MemoryUtil.memPutDouble(segment.address() + offset, value);
    }

    public static void memPutCLong(MemorySegment segment, long offset, long value) {
        MemoryUtil.memPutCLong(segment.address() + offset, value);
    }

    public static void memPutAddress(MemorySegment segment, long offset, long value) {
        MemoryUtil.memPutAddress(segment.address() + offset, value);
    }

    public static short memGetShortAtIndex(MemorySegment segment, long index) {
        return MemoryUtil.memGetShort(segment.address() + (index << 1));
    }

    public static int memGetIntAtIndex(MemorySegment segment, long index) {
        return MemoryUtil.memGetInt(segment.address() + (index << 2));
    }

    public static long memGetLongAtIndex(MemorySegment segment, long index) {
        return MemoryUtil.memGetLong(segment.address() + (index << 3));
    }

    public static float memGetFloatAtIndex(MemorySegment segment, long index) {
        return MemoryUtil.memGetFloat(segment.address() + (index << 2));
    }

    public static double memGetDoubleAtIndex(MemorySegment segment, long index) {
        return MemoryUtil.memGetDouble(segment.address() + (index << 3));
    }

    public static long memGetCLongAtIndex(MemorySegment segment, long index) {
        return MemoryUtil.memGetCLong(segment.address() + (index << Pointer.CLONG_SHIFT));
    }

    public static long memGetAddressAtIndex(MemorySegment segment, long index) {
        return MemoryUtil.memGetAddress(segment.address() + (index << Pointer.POINTER_SHIFT));
    }

    public static void memPutShortAtIndex(MemorySegment segment, long index, short value) {
        MemoryUtil.memPutShort(segment.address() + (index << 1), value);
    }

    public static void memPutIntAtIndex(MemorySegment segment, long index, int value) {
        MemoryUtil.memPutInt(segment.address() + (index << 2), value);
    }

    public static void memPutLongAtIndex(MemorySegment segment, long index, long value) {
        MemoryUtil.memPutLong(segment.address() + (index << 3), value);
    }

    public static void memPutFloatAtIndex(MemorySegment segment, long index, float value) {
        MemoryUtil.memPutFloat(segment.address() + (index << 2), value);
    }

    public static void memPutDoubleAtIndex(MemorySegment segment, long index, double value) {
        MemoryUtil.memPutDouble(segment.address() + (index << 3), value);
    }

    public static void memPutCLongAtIndex(MemorySegment segment, long index, long value) {
        MemoryUtil.memPutCLong(segment.address() + (index << Pointer.CLONG_SHIFT), value);
    }

    public static void memPutAddressAtIndex(MemorySegment segment, long index, long value) {
        MemoryUtil.memPutAddress(segment.address() + (index << Pointer.POINTER_SHIFT), value);
    }

    public static native <T> T memGlobalRefToObject(long var0);

    private static int write8(long target, int offset, int value) {
        MemoryUtil.memPutByte(target + Integer.toUnsignedLong(offset), (byte)value);
        return offset + 1;
    }

    private static int write8Safe(long target, int offset, int maxLength, int value) {
        if (offset == maxLength) {
            throw new BufferOverflowException();
        }
        MemoryUtil.memPutByte(target + Integer.toUnsignedLong(offset), (byte)value);
        return offset + 1;
    }

    private static int write16(long target, int offset, char value) {
        MemoryUtil.memPutShort(target + Integer.toUnsignedLong(offset), (short)value);
        return offset + 2;
    }

    public static ByteBuffer memASCII(CharSequence text) {
        return MemoryUtil.memASCII(text, true);
    }

    public static @Nullable ByteBuffer memASCIISafe(@Nullable CharSequence text) {
        return text == null ? null : MemoryUtil.memASCII(text, true);
    }

    public static ByteBuffer memASCII(CharSequence text, boolean nullTerminated) {
        int length = MemoryUtil.memLengthASCII(text, nullTerminated);
        long target = MemoryUtil.nmemAlloc(length);
        if (Checks.CHECKS && target == 0L) {
            throw new OutOfMemoryError();
        }
        MemoryUtil.encodeASCIIUnsafe(text, nullTerminated, target);
        return MemoryUtil.wrapBufferByte(target, length);
    }

    public static @Nullable ByteBuffer memASCIISafe(@Nullable CharSequence text, boolean nullTerminated) {
        return text == null ? null : MemoryUtil.memASCII(text, nullTerminated);
    }

    public static int memASCII(CharSequence text, boolean nullTerminated, ByteBuffer target) {
        if (target.remaining() < MemoryUtil.memLengthASCII(text, nullTerminated)) {
            throw new BufferOverflowException();
        }
        long address = MemoryUtil.memAddress(target);
        return MemoryUtil.encodeASCIIUnsafe(text, nullTerminated, address);
    }

    public static int memASCII(CharSequence text, boolean nullTerminated, ByteBuffer target, int offset) {
        if (target.capacity() - offset < MemoryUtil.memLengthASCII(text, nullTerminated)) {
            throw new BufferOverflowException();
        }
        return MemoryUtil.encodeASCIIUnsafe(text, nullTerminated, MemoryUtil.memAddress(target, offset));
    }

    static int encodeASCIIUnsafe(CharSequence text, boolean nullTerminated, long target) {
        int i = 0;
        int len = text.length();
        while (i < len) {
            i = MemoryUtil.write8(target, i, text.charAt(i));
        }
        if (nullTerminated) {
            i = MemoryUtil.write8(target, i, 0);
        }
        return i;
    }

    public static int memLengthASCII(CharSequence value, boolean nullTerminated) {
        int len = value.length() + (nullTerminated ? 1 : 0);
        if (len < 0) {
            throw new BufferOverflowException();
        }
        return len;
    }

    public static ByteBuffer memUTF8(CharSequence text) {
        return MemoryUtil.memUTF8(text, true);
    }

    public static @Nullable ByteBuffer memUTF8Safe(@Nullable CharSequence text) {
        return text == null ? null : MemoryUtil.memUTF8(text, true);
    }

    public static ByteBuffer memUTF8(CharSequence text, boolean nullTerminated) {
        int length = MemoryUtil.memLengthUTF8(text, nullTerminated);
        long target = MemoryUtil.nmemAlloc(length);
        if (Checks.CHECKS && target == 0L) {
            throw new OutOfMemoryError();
        }
        MemoryUtil.encodeUTF8Unsafe(text, nullTerminated, target);
        return MemoryUtil.wrapBufferByte(target, length);
    }

    public static @Nullable ByteBuffer memUTF8Safe(@Nullable CharSequence text, boolean nullTerminated) {
        return text == null ? null : MemoryUtil.memUTF8(text, nullTerminated);
    }

    public static int memUTF8(CharSequence text, boolean nullTerminated, ByteBuffer target) {
        if (target.remaining() < MemoryUtil.memLengthASCII(text, nullTerminated)) {
            throw new BufferOverflowException();
        }
        return MemoryUtil.encodeUTF8Safe(text, nullTerminated, MemoryUtil.memAddress(target), target.remaining());
    }

    public static int memUTF8(CharSequence text, boolean nullTerminated, ByteBuffer target, int offset) {
        if (target.capacity() - offset < MemoryUtil.memLengthASCII(text, nullTerminated)) {
            throw new BufferOverflowException();
        }
        return MemoryUtil.encodeUTF8Safe(text, nullTerminated, MemoryUtil.memAddress(target, offset), target.capacity() - offset);
    }

    static int encodeUTF8Unsafe(CharSequence text, boolean nullTerminated, long target) {
        int p = 0;
        int i = 0;
        int len = text.length();
        while (i < len) {
            int c;
            if ((c = text.charAt(i++)) < 128) {
                p = MemoryUtil.write8(target, p, c);
                continue;
            }
            int cp = c;
            if (c < 2048) {
                p = MemoryUtil.write8(target, p, 0xC0 | cp >> 6);
            } else {
                if (!Character.isHighSurrogate((char)c)) {
                    p = MemoryUtil.write8(target, p, 0xE0 | cp >> 12);
                } else {
                    cp = Character.toCodePoint((char)c, text.charAt(i++));
                    p = MemoryUtil.write8(target, p, 0xF0 | cp >> 18);
                    p = MemoryUtil.write8(target, p, 0x80 | cp >> 12 & 0x3F);
                }
                p = MemoryUtil.write8(target, p, 0x80 | cp >> 6 & 0x3F);
            }
            p = MemoryUtil.write8(target, p, 0x80 | cp & 0x3F);
        }
        if (nullTerminated) {
            p = MemoryUtil.write8(target, p, 0);
        }
        return p;
    }

    static int encodeUTF8Safe(CharSequence text, boolean nullTerminated, long target, int maxLength) {
        int c;
        int i;
        int p = 0;
        int length = text.length();
        for (i = 0; i < length && 128 > (c = text.charAt(i)); ++i) {
            p = MemoryUtil.write8(target, p, c);
        }
        while (i < length) {
            if ((c = text.charAt(i++)) < 128) {
                p = MemoryUtil.write8Safe(target, p, maxLength, c);
                continue;
            }
            int cp = c;
            if (c < 2048) {
                p = MemoryUtil.write8Safe(target, p, maxLength, 0xC0 | cp >> 6);
            } else {
                if (!Character.isHighSurrogate((char)c)) {
                    p = MemoryUtil.write8Safe(target, p, maxLength, 0xE0 | cp >> 12);
                } else {
                    cp = Character.toCodePoint((char)c, text.charAt(i++));
                    p = MemoryUtil.write8Safe(target, p, maxLength, 0xF0 | cp >> 18);
                    p = MemoryUtil.write8Safe(target, p, maxLength, 0x80 | cp >> 12 & 0x3F);
                }
                p = MemoryUtil.write8Safe(target, p, maxLength, 0x80 | cp >> 6 & 0x3F);
            }
            p = MemoryUtil.write8Safe(target, p, maxLength, 0x80 | cp & 0x3F);
        }
        if (nullTerminated) {
            p = MemoryUtil.write8Safe(target, p, maxLength, 0);
        }
        return p;
    }

    public static int memLengthUTF8(CharSequence value, boolean nullTerminated) {
        int len = value.length();
        int bytes = len + (nullTerminated ? 1 : 0);
        for (int i = 0; i < len; ++i) {
            char c = value.charAt(i);
            if (c < '\u0080') continue;
            if (c < '\u0800') {
                bytes += 127 - c >>> 31;
            } else {
                bytes += 2;
                if (Character.isHighSurrogate(c)) {
                    ++i;
                }
            }
            if (bytes >= 0) continue;
            throw new BufferOverflowException();
        }
        if (bytes < 0) {
            throw new BufferOverflowException();
        }
        return bytes;
    }

    public static ByteBuffer memUTF16(CharSequence text) {
        return MemoryUtil.memUTF16(text, true);
    }

    public static @Nullable ByteBuffer memUTF16Safe(@Nullable CharSequence text) {
        return text == null ? null : MemoryUtil.memUTF16(text, true);
    }

    public static ByteBuffer memUTF16(CharSequence text, boolean nullTerminated) {
        int length = MemoryUtil.memLengthUTF16(text, nullTerminated);
        long target = MemoryUtil.nmemAlloc(length);
        if (Checks.CHECKS && target == 0L) {
            throw new OutOfMemoryError();
        }
        MemoryUtil.encodeUTF16Unsafe(text, nullTerminated, target);
        return MemoryUtil.wrapBufferByte(target, length);
    }

    public static @Nullable ByteBuffer memUTF16Safe(@Nullable CharSequence text, boolean nullTerminated) {
        return text == null ? null : MemoryUtil.memUTF16(text, nullTerminated);
    }

    public static int memUTF16(CharSequence text, boolean nullTerminated, ByteBuffer target) {
        if (target.remaining() < MemoryUtil.memLengthUTF16(text, nullTerminated)) {
            throw new BufferOverflowException();
        }
        long address = MemoryUtil.memAddress(target);
        return MemoryUtil.encodeUTF16Unsafe(text, nullTerminated, address);
    }

    public static int memUTF16(CharSequence text, boolean nullTerminated, ByteBuffer target, int offset) {
        if (target.capacity() - offset < MemoryUtil.memLengthUTF16(text, nullTerminated)) {
            throw new BufferOverflowException();
        }
        long address = MemoryUtil.memAddress(target, offset);
        return MemoryUtil.encodeUTF16Unsafe(text, nullTerminated, address);
    }

    static int encodeUTF16Unsafe(CharSequence text, boolean nullTerminated, long target) {
        int p = 0;
        int i = 0;
        int len = text.length();
        while (i < len) {
            p = MemoryUtil.write16(target, p, text.charAt(i++));
        }
        if (nullTerminated) {
            p = MemoryUtil.write16(target, p, '\u0000');
        }
        return p;
    }

    public static int memLengthUTF16(CharSequence value, boolean nullTerminated) {
        int len = value.length() + (nullTerminated ? 1 : 0);
        if (len < 0 || 0x3FFFFFFF < len) {
            throw new BufferOverflowException();
        }
        return len << 1;
    }

    private static int memLengthNT1(long address, int maxLength) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return Pointer.BITS64 ? MemoryUtil.strlen64NT1(address, maxLength) : MemoryUtil.strlen32NT1(address, maxLength);
    }

    private static int strlen64NT1(long address, int maxLength) {
        int i;
        if (8 <= maxLength) {
            int misalignment = (int)address & 7;
            if (misalignment != 0) {
                int len = 8 - misalignment;
                for (i = 0; i < len; ++i) {
                    if (MemoryUtil.memGetByte(address + (long)i) != 0) continue;
                    return i;
                }
            }
            while (i <= maxLength - 8 && !MathUtil.mathHasZeroByte(MemoryUtil.memGetLong(address + (long)i))) {
                i += 8;
            }
        }
        while (i < maxLength && MemoryUtil.memGetByte(address + (long)i) != 0) {
            ++i;
        }
        return i;
    }

    private static int strlen32NT1(long address, int maxLength) {
        int i;
        if (4 <= maxLength) {
            int misalignment = (int)address & 3;
            if (misalignment != 0) {
                int len = 4 - misalignment;
                for (i = 0; i < len; ++i) {
                    if (MemoryUtil.memGetByte(address + (long)i) != 0) continue;
                    return i;
                }
            }
            while (i <= maxLength - 4 && !MathUtil.mathHasZeroByte(MemoryUtil.memGetInt(address + (long)i))) {
                i += 4;
            }
        }
        while (i < maxLength && MemoryUtil.memGetByte(address + (long)i) != 0) {
            ++i;
        }
        return i;
    }

    public static int memLengthNT1(ByteBuffer buffer) {
        return MemoryUtil.memLengthNT1(MemoryUtil.memAddress(buffer), buffer.remaining());
    }

    private static int memLengthNT2(long address, int maxLength) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        return Pointer.BITS64 ? MemoryUtil.strlen64NT2(address, maxLength) : MemoryUtil.strlen32NT2((int)address, maxLength);
    }

    private static int strlen64NT2(long address, int maxLength) {
        int i;
        if (8 <= maxLength) {
            int misalignment = (int)address & 7;
            if (misalignment != 0) {
                int len = 8 - misalignment;
                for (i = 0; i < len; i += 2) {
                    if (MemoryUtil.memGetShort(address + (long)i) != 0) continue;
                    return i;
                }
            }
            while (i <= maxLength - 8 && !MathUtil.mathHasZeroShort(MemoryUtil.memGetLong(address + (long)i))) {
                i += 8;
            }
        }
        while (i < maxLength && MemoryUtil.memGetShort(address + (long)i) != 0) {
            i += 2;
        }
        return i;
    }

    private static int strlen32NT2(long address, int maxLength) {
        int i;
        if (4 <= maxLength) {
            int misalignment = (int)address & 3;
            if (misalignment != 0) {
                int len = 4 - misalignment;
                for (i = 0; i < len; i += 2) {
                    if (MemoryUtil.memGetShort(address + (long)i) != 0) continue;
                    return i;
                }
            }
            while (i <= maxLength - 4 && !MathUtil.mathHasZeroShort(MemoryUtil.memGetInt(address + (long)i))) {
                i += 4;
            }
        }
        while (i < maxLength && MemoryUtil.memGetShort(address + (long)i) != 0) {
            i += 2;
        }
        return i;
    }

    public static int memLengthNT2(ByteBuffer buffer) {
        return MemoryUtil.memLengthNT2(MemoryUtil.memAddress(buffer), buffer.remaining());
    }

    public static ByteBuffer memByteBufferNT1(long address) {
        return MemoryUtil.memByteBuffer(address, MemoryUtil.memLengthNT1(address, 0x7FFFFFF7));
    }

    public static ByteBuffer memByteBufferNT1(long address, int maxLength) {
        return MemoryUtil.memByteBuffer(address, MemoryUtil.memLengthNT1(address, maxLength));
    }

    public static @Nullable ByteBuffer memByteBufferNT1Safe(long address) {
        return address == 0L ? null : MemoryUtil.memByteBuffer(address, MemoryUtil.memLengthNT1(address, 0x7FFFFFF7));
    }

    public static @Nullable ByteBuffer memByteBufferNT1Safe(long address, int maxLength) {
        return address == 0L ? null : MemoryUtil.memByteBuffer(address, MemoryUtil.memLengthNT1(address, maxLength));
    }

    public static ByteBuffer memByteBufferNT2(long address) {
        return MemoryUtil.memByteBufferNT2(address, 0x7FFFFFF6);
    }

    public static ByteBuffer memByteBufferNT2(long address, int maxLength) {
        if (Checks.DEBUG && (maxLength & 1) != 0) {
            throw new IllegalArgumentException("The maximum length must be an even number.");
        }
        return MemoryUtil.memByteBuffer(address, MemoryUtil.memLengthNT2(address, maxLength));
    }

    public static @Nullable ByteBuffer memByteBufferNT2Safe(long address) {
        return address == 0L ? null : MemoryUtil.memByteBufferNT2(address, 0x7FFFFFF6);
    }

    public static @Nullable ByteBuffer memByteBufferNT2Safe(long address, int maxLength) {
        return address == 0L ? null : MemoryUtil.memByteBufferNT2(address, maxLength);
    }

    public static String memASCII(long address) {
        return MemoryUtil.memASCII(address, MemoryUtil.memLengthNT1(address, 0x7FFFFFF7));
    }

    public static String memASCII(long address, int length) {
        if (length <= 0) {
            return "";
        }
        byte[] ascii = length <= ARRAY_TLC_SIZE ? ARRAY_TLC_BYTE.get() : new byte[length];
        MemoryUtil.memByteBuffer(address, length).get(ascii, 0, length);
        return new String(ascii, 0, 0, length);
    }

    public static String memASCII(ByteBuffer buffer) {
        return MemoryUtil.memASCII(MemoryUtil.memAddress(buffer), buffer.remaining());
    }

    public static @Nullable String memASCIISafe(long address) {
        return address == 0L ? null : MemoryUtil.memASCII(address, MemoryUtil.memLengthNT1(address, 0x7FFFFFF7));
    }

    public static @Nullable String memASCIISafe(long address, int length) {
        return address == 0L ? null : MemoryUtil.memASCII(address, length);
    }

    public static @Nullable String memASCIISafe(@Nullable ByteBuffer buffer) {
        return buffer == null ? null : MemoryUtil.memASCII(MemoryUtil.memAddress(buffer), buffer.remaining());
    }

    public static String memASCII(ByteBuffer buffer, int length) {
        return MemoryUtil.memASCII(MemoryUtil.memAddress(buffer), length);
    }

    public static String memASCII(ByteBuffer buffer, int length, int offset) {
        return MemoryUtil.memASCII(MemoryUtil.memAddress(buffer, offset), length);
    }

    public static String memUTF8(long address) {
        return MultiReleaseTextDecoding.decodeUTF8(address, MemoryUtil.memLengthNT1(address, 0x7FFFFFF7));
    }

    public static String memUTF8(long address, int length) {
        return MultiReleaseTextDecoding.decodeUTF8(address, length);
    }

    public static String memUTF8(ByteBuffer buffer) {
        return MultiReleaseTextDecoding.decodeUTF8(MemoryUtil.memAddress(buffer), buffer.remaining());
    }

    public static @Nullable String memUTF8Safe(long address) {
        return address == 0L ? null : MultiReleaseTextDecoding.decodeUTF8(address, MemoryUtil.memLengthNT1(address, 0x7FFFFFF7));
    }

    public static @Nullable String memUTF8Safe(long address, int length) {
        return address == 0L ? null : MultiReleaseTextDecoding.decodeUTF8(address, length);
    }

    public static @Nullable String memUTF8Safe(@Nullable ByteBuffer buffer) {
        return buffer == null ? null : MultiReleaseTextDecoding.decodeUTF8(MemoryUtil.memAddress(buffer), buffer.remaining());
    }

    public static String memUTF8(ByteBuffer buffer, int length) {
        return MultiReleaseTextDecoding.decodeUTF8(MemoryUtil.memAddress(buffer), length);
    }

    public static String memUTF8(ByteBuffer buffer, int length, int offset) {
        return MultiReleaseTextDecoding.decodeUTF8(MemoryUtil.memAddress(buffer, offset), length);
    }

    public static String memUTF16(long address) {
        return MemoryUtil.memUTF16(address, MemoryUtil.memLengthNT2(address, 0x7FFFFFF6) >> 1);
    }

    public static String memUTF16(long address, int length) {
        if (length <= 0) {
            return "";
        }
        if (Checks.DEBUG) {
            int len = length << 1;
            byte[] bytes = len <= ARRAY_TLC_SIZE ? ARRAY_TLC_BYTE.get() : new byte[len];
            MemoryUtil.memByteBuffer(address, len).get(bytes, 0, len);
            return new String(bytes, 0, len, UTF16);
        }
        char[] chars = length <= ARRAY_TLC_SIZE ? ARRAY_TLC_CHAR.get() : new char[length];
        MemoryUtil.memCharBuffer(address, length).get(chars, 0, length);
        return new String(chars, 0, length);
    }

    public static String memUTF16(ByteBuffer buffer) {
        return MemoryUtil.memUTF16(MemoryUtil.memAddress(buffer), buffer.remaining() >> 1);
    }

    public static @Nullable String memUTF16Safe(long address) {
        return address == 0L ? null : MemoryUtil.memUTF16(address, MemoryUtil.memLengthNT2(address, 0x7FFFFFF6) >> 1);
    }

    public static @Nullable String memUTF16Safe(long address, int length) {
        return address == 0L ? null : MemoryUtil.memUTF16(address, length);
    }

    public static @Nullable String memUTF16Safe(@Nullable ByteBuffer buffer) {
        return buffer == null ? null : MemoryUtil.memUTF16(MemoryUtil.memAddress(buffer), buffer.remaining() >> 1);
    }

    public static String memUTF16(ByteBuffer buffer, int length) {
        return MemoryUtil.memUTF16(MemoryUtil.memAddress(buffer), length);
    }

    public static String memUTF16(ByteBuffer buffer, int length, int offset) {
        return MemoryUtil.memUTF16(MemoryUtil.memAddress(buffer, offset), length);
    }

    static ByteBuffer wrapBufferByte(long address, int capacity) {
        return MemorySegment.ofAddress(address).reinterpret((long)capacity & 0xFFFFFFFFL).asByteBuffer().order(ByteOrder.nativeOrder());
    }

    static ShortBuffer wrapBufferShort(long address, int capacity) {
        return MemoryUtil.wrapBufferByte(address, capacity << 1).asShortBuffer();
    }

    static CharBuffer wrapBufferChar(long address, int capacity) {
        return MemoryUtil.wrapBufferByte(address, capacity << 1).asCharBuffer();
    }

    static IntBuffer wrapBufferInt(long address, int capacity) {
        return MemoryUtil.wrapBufferByte(address, capacity << 2).asIntBuffer();
    }

    static LongBuffer wrapBufferLong(long address, int capacity) {
        return MemoryUtil.wrapBufferByte(address, capacity << 3).asLongBuffer();
    }

    static FloatBuffer wrapBufferFloat(long address, int capacity) {
        return MemoryUtil.wrapBufferByte(address, capacity << 2).asFloatBuffer();
    }

    static DoubleBuffer wrapBufferDouble(long address, int capacity) {
        return MemoryUtil.wrapBufferByte(address, capacity << 3).asDoubleBuffer();
    }

    static {
        ARRAY_TLC_SIZE = Configuration.ARRAY_TLC_SIZE.get(8192);
        ARRAY_TLC_BYTE = ThreadLocal.withInitial(() -> new byte[ARRAY_TLC_SIZE]);
        ARRAY_TLC_CHAR = ThreadLocal.withInitial(() -> new char[ARRAY_TLC_SIZE]);
        NATIVE_ORDER = ByteOrder.nativeOrder();
        UTF16 = NATIVE_ORDER == ByteOrder.LITTLE_ENDIAN ? StandardCharsets.UTF_16LE : StandardCharsets.UTF_16BE;
        Library.initialize();
        PAGE_SIZE = 4096;
        CACHE_LINE_SIZE = 64;
        APIUtil.apiLog("Java 25 MemoryUtil enabled");
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle ofAddress = lookup.findStatic(MemorySegment.class, "ofAddress", MethodType.methodType(MemorySegment.class, Long.TYPE));
            MethodHandle reinterpret = lookup.findVirtual(MemorySegment.class, "reinterpret", MethodType.methodType(MemorySegment.class, Long.TYPE));
            VH_JAVA_BYTE = MemoryUtil.createMemoryAccessVH(ValueLayout.JAVA_BYTE, ofAddress, reinterpret).withInvokeExactBehavior();
            VH_JAVA_SHORT = MemoryUtil.createMemoryAccessVH(Checks.DEBUG ? ValueLayout.JAVA_SHORT : ValueLayout.JAVA_SHORT_UNALIGNED, ofAddress, reinterpret).withInvokeExactBehavior();
            VH_JAVA_INT = MemoryUtil.createMemoryAccessVH(Checks.DEBUG ? ValueLayout.JAVA_INT : ValueLayout.JAVA_INT_UNALIGNED, ofAddress, reinterpret).withInvokeExactBehavior();
            VH_JAVA_LONG = MemoryUtil.createMemoryAccessVH(Checks.DEBUG ? ValueLayout.JAVA_LONG : ValueLayout.JAVA_LONG_UNALIGNED, ofAddress, reinterpret).withInvokeExactBehavior();
            VH_JAVA_FLOAT = MemoryUtil.createMemoryAccessVH(Checks.DEBUG ? ValueLayout.JAVA_FLOAT : ValueLayout.JAVA_FLOAT_UNALIGNED, ofAddress, reinterpret).withInvokeExactBehavior();
            VH_JAVA_DOUBLE = MemoryUtil.createMemoryAccessVH(Checks.DEBUG ? ValueLayout.JAVA_DOUBLE : ValueLayout.JAVA_DOUBLE_UNALIGNED, ofAddress, reinterpret).withInvokeExactBehavior();
            VarHandle vh = MemoryUtil.createMemoryAccessVH(Pointer.CLONG_SIZE == 8 ? (Checks.DEBUG ? ValueLayout.JAVA_LONG : ValueLayout.JAVA_LONG_UNALIGNED) : (Checks.DEBUG ? ValueLayout.JAVA_INT : ValueLayout.JAVA_INT_UNALIGNED), ofAddress, reinterpret);
            if (Pointer.CLONG_SIZE == 4) {
                vh = MethodHandles.filterValue(vh, MethodHandles.explicitCastArguments(MethodHandles.identity(Integer.TYPE), MethodType.methodType(Integer.TYPE, Long.TYPE)), MethodHandles.explicitCastArguments(MethodHandles.identity(Long.TYPE), MethodType.methodType(Long.TYPE, Integer.TYPE)));
            }
            VH_CLONG = vh.withInvokeExactBehavior();
            vh = MemoryUtil.createMemoryAccessVH(Pointer.BITS64 ? (Checks.DEBUG ? ValueLayout.JAVA_LONG : ValueLayout.JAVA_LONG_UNALIGNED) : (Checks.DEBUG ? ValueLayout.JAVA_INT : ValueLayout.JAVA_INT_UNALIGNED), ofAddress, reinterpret);
            if (Pointer.BITS32) {
                vh = MethodHandles.filterValue(vh, MethodHandles.explicitCastArguments(MethodHandles.identity(Integer.TYPE), MethodType.methodType(Integer.TYPE, Long.TYPE)), lookup.findStatic(MemoryUtil.class, "castAddress32", MethodType.methodType(Long.TYPE, Integer.TYPE)));
            }
            VH_ADDRESS = vh.withInvokeExactBehavior();
        }
        catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
     */
    public static interface MemoryAllocator {
        public long getMalloc();

        public long getCalloc();

        public long getRealloc();

        public long getFree();

        public long getAlignedAlloc();

        public long getAlignedFree();

        public long malloc(long var1);

        public long calloc(long var1, long var3);

        public long realloc(long var1, long var3);

        public void free(long var1);

        public long aligned_alloc(long var1, long var3);

        public void aligned_free(long var1);
    }

    /*
     * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
     */
    static final class LazyInit {
        static final MemoryAllocator ALLOCATOR_IMPL;
        static final MemoryAllocator ALLOCATOR;

        private LazyInit() {
        }

        static {
            boolean debug = Configuration.DEBUG_MEMORY_ALLOCATOR.get(false);
            ALLOCATOR_IMPL = MemoryManage.getInstance();
            ALLOCATOR = debug ? new MemoryManage.DebugAllocator(ALLOCATOR_IMPL) : ALLOCATOR_IMPL;
            APIUtil.apiLog("MemoryUtil allocator: " + ALLOCATOR.getClass().getSimpleName());
            if (debug && !Configuration.DEBUG_MEMORY_ALLOCATOR_FAST.get(false).booleanValue()) {
                APIUtil.apiLogMore("Reminder: enable Configuration.DEBUG_MEMORY_ALLOCATOR_FAST for low overhead allocation tracking.");
            }
        }
    }

    /*
     * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
     */
    public static interface MemoryAllocationReport {
        public void invoke(long var1, long var3, long var5, @Nullable String var7, StackTraceElement ... var8);

        /*
         * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
         */
        public static enum Aggregate {
            ALL,
            GROUP_BY_METHOD,
            GROUP_BY_STACKTRACE;

        }
    }
}

