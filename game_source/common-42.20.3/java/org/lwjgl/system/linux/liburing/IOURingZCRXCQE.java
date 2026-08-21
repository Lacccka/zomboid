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

@NativeType(value="struct io_uring_zcrx_cqe")
public class IOURingZCRXCQE
extends Struct<IOURingZCRXCQE>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int OFF;
    public static final int __PAD;

    protected IOURingZCRXCQE(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected IOURingZCRXCQE create(long address, @Nullable ByteBuffer container) {
        return new IOURingZCRXCQE(address, container);
    }

    public IOURingZCRXCQE(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOURingZCRXCQE.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__u64")
    public long off() {
        return IOURingZCRXCQE.noff(this.address());
    }

    public IOURingZCRXCQE off(@NativeType(value="__u64") long value) {
        IOURingZCRXCQE.noff(this.address(), value);
        return this;
    }

    public IOURingZCRXCQE set(IOURingZCRXCQE src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOURingZCRXCQE malloc() {
        return new IOURingZCRXCQE(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static IOURingZCRXCQE calloc() {
        return new IOURingZCRXCQE(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static IOURingZCRXCQE create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new IOURingZCRXCQE(MemoryUtil.memAddress(container), container);
    }

    public static IOURingZCRXCQE create(long address) {
        return new IOURingZCRXCQE(address, null);
    }

    public static @Nullable IOURingZCRXCQE createSafe(long address) {
        return address == 0L ? null : new IOURingZCRXCQE(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(IOURingZCRXCQE.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOURingZCRXCQE.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static IOURingZCRXCQE malloc(MemoryStack stack) {
        return new IOURingZCRXCQE(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static IOURingZCRXCQE calloc(MemoryStack stack) {
        return new IOURingZCRXCQE(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static long noff(long struct) {
        return MemoryUtil.memGetLong(struct + (long)OFF);
    }

    public static long n__pad(long struct) {
        return MemoryUtil.memGetLong(struct + (long)__PAD);
    }

    public static void noff(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)OFF, value);
    }

    public static void n__pad(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)__PAD, value);
    }

    static {
        Struct.Layout layout = IOURingZCRXCQE.__struct(IOURingZCRXCQE.__member(8), IOURingZCRXCQE.__member(8));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        OFF = layout.offsetof(0);
        __PAD = layout.offsetof(1);
    }

    public static class Buffer
    extends StructBuffer<IOURingZCRXCQE, Buffer>
    implements NativeResource {
        private static final IOURingZCRXCQE ELEMENT_FACTORY = IOURingZCRXCQE.create(-1L);

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
        protected IOURingZCRXCQE getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__u64")
        public long off() {
            return IOURingZCRXCQE.noff(this.address());
        }

        public Buffer off(@NativeType(value="__u64") long value) {
            IOURingZCRXCQE.noff(this.address(), value);
            return this;
        }
    }
}

