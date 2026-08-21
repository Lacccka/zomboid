/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.linux.liburing;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType(value="struct io_uring_mem_region_reg")
public class IOURingMemRegionReg
extends Struct<IOURingMemRegionReg>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int REGION_UPTR;
    public static final int FLAGS;
    public static final int __RESV;

    protected IOURingMemRegionReg(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected IOURingMemRegionReg create(long address, @Nullable ByteBuffer container) {
        return new IOURingMemRegionReg(address, container);
    }

    public IOURingMemRegionReg(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOURingMemRegionReg.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__u64")
    public long region_uptr() {
        return IOURingMemRegionReg.nregion_uptr(this.address());
    }

    @NativeType(value="__u64")
    public long flags() {
        return IOURingMemRegionReg.nflags(this.address());
    }

    public IOURingMemRegionReg region_uptr(@NativeType(value="__u64") long value) {
        IOURingMemRegionReg.nregion_uptr(this.address(), value);
        return this;
    }

    public IOURingMemRegionReg flags(@NativeType(value="__u64") long value) {
        IOURingMemRegionReg.nflags(this.address(), value);
        return this;
    }

    public IOURingMemRegionReg set(long region_uptr, long flags) {
        this.region_uptr(region_uptr);
        this.flags(flags);
        return this;
    }

    public IOURingMemRegionReg set(IOURingMemRegionReg src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOURingMemRegionReg malloc() {
        return new IOURingMemRegionReg(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static IOURingMemRegionReg calloc() {
        return new IOURingMemRegionReg(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static IOURingMemRegionReg create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new IOURingMemRegionReg(MemoryUtil.memAddress(container), container);
    }

    public static IOURingMemRegionReg create(long address) {
        return new IOURingMemRegionReg(address, null);
    }

    public static @Nullable IOURingMemRegionReg createSafe(long address) {
        return address == 0L ? null : new IOURingMemRegionReg(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(IOURingMemRegionReg.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOURingMemRegionReg.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static IOURingMemRegionReg malloc(MemoryStack stack) {
        return new IOURingMemRegionReg(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static IOURingMemRegionReg calloc(MemoryStack stack) {
        return new IOURingMemRegionReg(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static long nregion_uptr(long struct) {
        return MemoryUtil.memGetLong(struct + (long)REGION_UPTR);
    }

    public static long nflags(long struct) {
        return MemoryUtil.memGetLong(struct + (long)FLAGS);
    }

    public static LongBuffer n__resv(long struct) {
        return MemoryUtil.memLongBuffer(struct + (long)__RESV, 2);
    }

    public static long n__resv(long struct, int index) {
        return MemoryUtil.memGetLong(struct + (long)__RESV + Checks.check(index, 2) * 8L);
    }

    public static void nregion_uptr(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)REGION_UPTR, value);
    }

    public static void nflags(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)FLAGS, value);
    }

    public static void n__resv(long struct, LongBuffer value) {
        if (Checks.CHECKS) {
            Checks.checkGT(value, 2);
        }
        MemoryUtil.memCopy(MemoryUtil.memAddress(value), struct + (long)__RESV, value.remaining() * 8);
    }

    public static void n__resv(long struct, int index, long value) {
        MemoryUtil.memPutLong(struct + (long)__RESV + Checks.check(index, 2) * 8L, value);
    }

    static {
        Struct.Layout layout = IOURingMemRegionReg.__struct(IOURingMemRegionReg.__member(8), IOURingMemRegionReg.__member(8), IOURingMemRegionReg.__array(8, 2));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        REGION_UPTR = layout.offsetof(0);
        FLAGS = layout.offsetof(1);
        __RESV = layout.offsetof(2);
    }

    public static class Buffer
    extends StructBuffer<IOURingMemRegionReg, Buffer>
    implements NativeResource {
        private static final IOURingMemRegionReg ELEMENT_FACTORY = IOURingMemRegionReg.create(-1L);

        public Buffer(ByteBuffer container) {
            super(container, container.remaining() / SIZEOF);
        }

        public Buffer(long address, int cap) {
            super(address, null, -1, 0, cap, cap);
        }

        Buffer(long address, @Nullable ByteBuffer container, int mark, int pos, int lim, int cap) {
            super(address, container, mark, pos, lim, cap);
        }

        @Override
        protected Buffer self() {
            return this;
        }

        @Override
        protected Buffer create(long address, @Nullable ByteBuffer container, int mark, int position, int limit, int capacity) {
            return new Buffer(address, container, mark, position, limit, capacity);
        }

        @Override
        protected IOURingMemRegionReg getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__u64")
        public long region_uptr() {
            return IOURingMemRegionReg.nregion_uptr(this.address());
        }

        @NativeType(value="__u64")
        public long flags() {
            return IOURingMemRegionReg.nflags(this.address());
        }

        public Buffer region_uptr(@NativeType(value="__u64") long value) {
            IOURingMemRegionReg.nregion_uptr(this.address(), value);
            return this;
        }

        public Buffer flags(@NativeType(value="__u64") long value) {
            IOURingMemRegionReg.nflags(this.address(), value);
            return this;
        }
    }
}

