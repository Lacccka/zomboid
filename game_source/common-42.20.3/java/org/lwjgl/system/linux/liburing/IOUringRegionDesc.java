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

@NativeType(value="struct io_uring_region_desc")
public class IOUringRegionDesc
extends Struct<IOUringRegionDesc>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int USER_ADDR;
    public static final int SIZE;
    public static final int FLAGS;
    public static final int ID;
    public static final int MMAP_OFFSET;
    public static final int __RESV;

    protected IOUringRegionDesc(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected IOUringRegionDesc create(long address, @Nullable ByteBuffer container) {
        return new IOUringRegionDesc(address, container);
    }

    public IOUringRegionDesc(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOUringRegionDesc.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__u64")
    public long user_addr() {
        return IOUringRegionDesc.nuser_addr(this.address());
    }

    @NativeType(value="__u64")
    public long size() {
        return IOUringRegionDesc.nsize(this.address());
    }

    @NativeType(value="__u32")
    public int flags() {
        return IOUringRegionDesc.nflags(this.address());
    }

    @NativeType(value="__u32")
    public int id() {
        return IOUringRegionDesc.nid(this.address());
    }

    @NativeType(value="__u64")
    public long mmap_offset() {
        return IOUringRegionDesc.nmmap_offset(this.address());
    }

    public IOUringRegionDesc user_addr(@NativeType(value="__u64") long value) {
        IOUringRegionDesc.nuser_addr(this.address(), value);
        return this;
    }

    public IOUringRegionDesc size(@NativeType(value="__u64") long value) {
        IOUringRegionDesc.nsize(this.address(), value);
        return this;
    }

    public IOUringRegionDesc flags(@NativeType(value="__u32") int value) {
        IOUringRegionDesc.nflags(this.address(), value);
        return this;
    }

    public IOUringRegionDesc id(@NativeType(value="__u32") int value) {
        IOUringRegionDesc.nid(this.address(), value);
        return this;
    }

    public IOUringRegionDesc mmap_offset(@NativeType(value="__u64") long value) {
        IOUringRegionDesc.nmmap_offset(this.address(), value);
        return this;
    }

    public IOUringRegionDesc set(long user_addr, long size, int flags, int id, long mmap_offset) {
        this.user_addr(user_addr);
        this.size(size);
        this.flags(flags);
        this.id(id);
        this.mmap_offset(mmap_offset);
        return this;
    }

    public IOUringRegionDesc set(IOUringRegionDesc src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOUringRegionDesc malloc() {
        return new IOUringRegionDesc(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static IOUringRegionDesc calloc() {
        return new IOUringRegionDesc(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static IOUringRegionDesc create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new IOUringRegionDesc(MemoryUtil.memAddress(container), container);
    }

    public static IOUringRegionDesc create(long address) {
        return new IOUringRegionDesc(address, null);
    }

    public static @Nullable IOUringRegionDesc createSafe(long address) {
        return address == 0L ? null : new IOUringRegionDesc(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(IOUringRegionDesc.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOUringRegionDesc.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static IOUringRegionDesc malloc(MemoryStack stack) {
        return new IOUringRegionDesc(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static IOUringRegionDesc calloc(MemoryStack stack) {
        return new IOUringRegionDesc(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static long nuser_addr(long struct) {
        return MemoryUtil.memGetLong(struct + (long)USER_ADDR);
    }

    public static long nsize(long struct) {
        return MemoryUtil.memGetLong(struct + (long)SIZE);
    }

    public static int nflags(long struct) {
        return MemoryUtil.memGetInt(struct + (long)FLAGS);
    }

    public static int nid(long struct) {
        return MemoryUtil.memGetInt(struct + (long)ID);
    }

    public static long nmmap_offset(long struct) {
        return MemoryUtil.memGetLong(struct + (long)MMAP_OFFSET);
    }

    public static LongBuffer n__resv(long struct) {
        return MemoryUtil.memLongBuffer(struct + (long)__RESV, 4);
    }

    public static long n__resv(long struct, int index) {
        return MemoryUtil.memGetLong(struct + (long)__RESV + Checks.check(index, 4) * 8L);
    }

    public static void nuser_addr(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)USER_ADDR, value);
    }

    public static void nsize(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)SIZE, value);
    }

    public static void nflags(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)FLAGS, value);
    }

    public static void nid(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)ID, value);
    }

    public static void nmmap_offset(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)MMAP_OFFSET, value);
    }

    public static void n__resv(long struct, LongBuffer value) {
        if (Checks.CHECKS) {
            Checks.checkGT(value, 4);
        }
        MemoryUtil.memCopy(MemoryUtil.memAddress(value), struct + (long)__RESV, value.remaining() * 8);
    }

    public static void n__resv(long struct, int index, long value) {
        MemoryUtil.memPutLong(struct + (long)__RESV + Checks.check(index, 4) * 8L, value);
    }

    static {
        Struct.Layout layout = IOUringRegionDesc.__struct(IOUringRegionDesc.__member(8), IOUringRegionDesc.__member(8), IOUringRegionDesc.__member(4), IOUringRegionDesc.__member(4), IOUringRegionDesc.__member(8), IOUringRegionDesc.__array(8, 4));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        USER_ADDR = layout.offsetof(0);
        SIZE = layout.offsetof(1);
        FLAGS = layout.offsetof(2);
        ID = layout.offsetof(3);
        MMAP_OFFSET = layout.offsetof(4);
        __RESV = layout.offsetof(5);
    }

    public static class Buffer
    extends StructBuffer<IOUringRegionDesc, Buffer>
    implements NativeResource {
        private static final IOUringRegionDesc ELEMENT_FACTORY = IOUringRegionDesc.create(-1L);

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
        protected IOUringRegionDesc getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__u64")
        public long user_addr() {
            return IOUringRegionDesc.nuser_addr(this.address());
        }

        @NativeType(value="__u64")
        public long size() {
            return IOUringRegionDesc.nsize(this.address());
        }

        @NativeType(value="__u32")
        public int flags() {
            return IOUringRegionDesc.nflags(this.address());
        }

        @NativeType(value="__u32")
        public int id() {
            return IOUringRegionDesc.nid(this.address());
        }

        @NativeType(value="__u64")
        public long mmap_offset() {
            return IOUringRegionDesc.nmmap_offset(this.address());
        }

        public Buffer user_addr(@NativeType(value="__u64") long value) {
            IOUringRegionDesc.nuser_addr(this.address(), value);
            return this;
        }

        public Buffer size(@NativeType(value="__u64") long value) {
            IOUringRegionDesc.nsize(this.address(), value);
            return this;
        }

        public Buffer flags(@NativeType(value="__u32") int value) {
            IOUringRegionDesc.nflags(this.address(), value);
            return this;
        }

        public Buffer id(@NativeType(value="__u32") int value) {
            IOUringRegionDesc.nid(this.address(), value);
            return this;
        }

        public Buffer mmap_offset(@NativeType(value="__u64") long value) {
            IOUringRegionDesc.nmmap_offset(this.address(), value);
            return this;
        }
    }
}

