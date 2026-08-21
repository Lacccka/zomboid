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

@NativeType(value="struct io_uring_zcrx_rqe")
public class IOURingZCRXRQE
extends Struct<IOURingZCRXRQE>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int OFF;
    public static final int LEN;
    public static final int __PAD;

    protected IOURingZCRXRQE(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected IOURingZCRXRQE create(long address, @Nullable ByteBuffer container) {
        return new IOURingZCRXRQE(address, container);
    }

    public IOURingZCRXRQE(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOURingZCRXRQE.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__u64")
    public long off() {
        return IOURingZCRXRQE.noff(this.address());
    }

    @NativeType(value="__u32")
    public int len() {
        return IOURingZCRXRQE.nlen(this.address());
    }

    public IOURingZCRXRQE off(@NativeType(value="__u64") long value) {
        IOURingZCRXRQE.noff(this.address(), value);
        return this;
    }

    public IOURingZCRXRQE len(@NativeType(value="__u32") int value) {
        IOURingZCRXRQE.nlen(this.address(), value);
        return this;
    }

    public IOURingZCRXRQE set(long off, int len) {
        this.off(off);
        this.len(len);
        return this;
    }

    public IOURingZCRXRQE set(IOURingZCRXRQE src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOURingZCRXRQE malloc() {
        return new IOURingZCRXRQE(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static IOURingZCRXRQE calloc() {
        return new IOURingZCRXRQE(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static IOURingZCRXRQE create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new IOURingZCRXRQE(MemoryUtil.memAddress(container), container);
    }

    public static IOURingZCRXRQE create(long address) {
        return new IOURingZCRXRQE(address, null);
    }

    public static @Nullable IOURingZCRXRQE createSafe(long address) {
        return address == 0L ? null : new IOURingZCRXRQE(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(IOURingZCRXRQE.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOURingZCRXRQE.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static IOURingZCRXRQE malloc(MemoryStack stack) {
        return new IOURingZCRXRQE(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static IOURingZCRXRQE calloc(MemoryStack stack) {
        return new IOURingZCRXRQE(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
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

    public static int nlen(long struct) {
        return MemoryUtil.memGetInt(struct + (long)LEN);
    }

    public static int n__pad(long struct) {
        return MemoryUtil.memGetInt(struct + (long)__PAD);
    }

    public static void noff(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)OFF, value);
    }

    public static void nlen(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)LEN, value);
    }

    public static void n__pad(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)__PAD, value);
    }

    static {
        Struct.Layout layout = IOURingZCRXRQE.__struct(IOURingZCRXRQE.__member(8), IOURingZCRXRQE.__member(4), IOURingZCRXRQE.__member(4));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        OFF = layout.offsetof(0);
        LEN = layout.offsetof(1);
        __PAD = layout.offsetof(2);
    }

    public static class Buffer
    extends StructBuffer<IOURingZCRXRQE, Buffer>
    implements NativeResource {
        private static final IOURingZCRXRQE ELEMENT_FACTORY = IOURingZCRXRQE.create(-1L);

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
        protected IOURingZCRXRQE getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__u64")
        public long off() {
            return IOURingZCRXRQE.noff(this.address());
        }

        @NativeType(value="__u32")
        public int len() {
            return IOURingZCRXRQE.nlen(this.address());
        }

        public Buffer off(@NativeType(value="__u64") long value) {
            IOURingZCRXRQE.noff(this.address(), value);
            return this;
        }

        public Buffer len(@NativeType(value="__u32") int value) {
            IOURingZCRXRQE.nlen(this.address(), value);
            return this;
        }
    }
}

