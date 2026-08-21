/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.linux.liburing;

import java.nio.ByteBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType(value="struct io_timespec")
public class IOTimespec
extends Struct<IOTimespec>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int TV_SEC;
    public static final int TV_NSEC;

    protected IOTimespec(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected IOTimespec create(long address, @Nullable ByteBuffer container) {
        return new IOTimespec(address, container);
    }

    public IOTimespec(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOTimespec.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__u64")
    public long tv_sec() {
        return IOTimespec.ntv_sec(this.address());
    }

    @NativeType(value="__u64")
    public long tv_nsec() {
        return IOTimespec.ntv_nsec(this.address());
    }

    public IOTimespec tv_sec(@NativeType(value="__u64") long value) {
        IOTimespec.ntv_sec(this.address(), value);
        return this;
    }

    public IOTimespec tv_nsec(@NativeType(value="__u64") long value) {
        IOTimespec.ntv_nsec(this.address(), value);
        return this;
    }

    public IOTimespec set(long tv_sec, long tv_nsec) {
        this.tv_sec(tv_sec);
        this.tv_nsec(tv_nsec);
        return this;
    }

    public IOTimespec set(IOTimespec src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOTimespec malloc() {
        return new IOTimespec(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static IOTimespec calloc() {
        return new IOTimespec(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static IOTimespec create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new IOTimespec(MemoryUtil.memAddress(container), container);
    }

    public static IOTimespec create(long address) {
        return new IOTimespec(address, null);
    }

    public static @Nullable IOTimespec createSafe(long address) {
        return address == 0L ? null : new IOTimespec(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(IOTimespec.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOTimespec.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static IOTimespec malloc(MemoryStack stack) {
        return new IOTimespec(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static IOTimespec calloc(MemoryStack stack) {
        return new IOTimespec(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static long ntv_sec(long struct) {
        return MemoryUtil.memGetLong(struct + (long)TV_SEC);
    }

    public static long ntv_nsec(long struct) {
        return MemoryUtil.memGetLong(struct + (long)TV_NSEC);
    }

    public static void ntv_sec(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)TV_SEC, value);
    }

    public static void ntv_nsec(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)TV_NSEC, value);
    }

    static {
        Struct.Layout layout = IOTimespec.__struct(IOTimespec.__member(8), IOTimespec.__member(8));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        TV_SEC = layout.offsetof(0);
        TV_NSEC = layout.offsetof(1);
    }

    public static class Buffer
    extends StructBuffer<IOTimespec, Buffer>
    implements NativeResource {
        private static final IOTimespec ELEMENT_FACTORY = IOTimespec.create(-1L);

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
        protected IOTimespec getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__u64")
        public long tv_sec() {
            return IOTimespec.ntv_sec(this.address());
        }

        @NativeType(value="__u64")
        public long tv_nsec() {
            return IOTimespec.ntv_nsec(this.address());
        }

        public Buffer tv_sec(@NativeType(value="__u64") long value) {
            IOTimespec.ntv_sec(this.address(), value);
            return this;
        }

        public Buffer tv_nsec(@NativeType(value="__u64") long value) {
            IOTimespec.ntv_nsec(this.address(), value);
            return this;
        }
    }
}

