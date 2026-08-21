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

@NativeType(value="struct io_uring_zcrx_offsets")
public class IOURingZCRXOffsets
extends Struct<IOURingZCRXOffsets>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int HEAD;
    public static final int TAIL;
    public static final int RQES;
    public static final int __RESV2;
    public static final int __RESV;

    protected IOURingZCRXOffsets(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected IOURingZCRXOffsets create(long address, @Nullable ByteBuffer container) {
        return new IOURingZCRXOffsets(address, container);
    }

    public IOURingZCRXOffsets(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOURingZCRXOffsets.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__u32")
    public int head() {
        return IOURingZCRXOffsets.nhead(this.address());
    }

    @NativeType(value="__u32")
    public int tail() {
        return IOURingZCRXOffsets.ntail(this.address());
    }

    @NativeType(value="__u32")
    public int rqes() {
        return IOURingZCRXOffsets.nrqes(this.address());
    }

    public IOURingZCRXOffsets head(@NativeType(value="__u32") int value) {
        IOURingZCRXOffsets.nhead(this.address(), value);
        return this;
    }

    public IOURingZCRXOffsets tail(@NativeType(value="__u32") int value) {
        IOURingZCRXOffsets.ntail(this.address(), value);
        return this;
    }

    public IOURingZCRXOffsets rqes(@NativeType(value="__u32") int value) {
        IOURingZCRXOffsets.nrqes(this.address(), value);
        return this;
    }

    public IOURingZCRXOffsets set(int head, int tail, int rqes) {
        this.head(head);
        this.tail(tail);
        this.rqes(rqes);
        return this;
    }

    public IOURingZCRXOffsets set(IOURingZCRXOffsets src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOURingZCRXOffsets malloc() {
        return new IOURingZCRXOffsets(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static IOURingZCRXOffsets calloc() {
        return new IOURingZCRXOffsets(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static IOURingZCRXOffsets create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new IOURingZCRXOffsets(MemoryUtil.memAddress(container), container);
    }

    public static IOURingZCRXOffsets create(long address) {
        return new IOURingZCRXOffsets(address, null);
    }

    public static @Nullable IOURingZCRXOffsets createSafe(long address) {
        return address == 0L ? null : new IOURingZCRXOffsets(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(IOURingZCRXOffsets.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOURingZCRXOffsets.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static IOURingZCRXOffsets malloc(MemoryStack stack) {
        return new IOURingZCRXOffsets(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static IOURingZCRXOffsets calloc(MemoryStack stack) {
        return new IOURingZCRXOffsets(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int nhead(long struct) {
        return MemoryUtil.memGetInt(struct + (long)HEAD);
    }

    public static int ntail(long struct) {
        return MemoryUtil.memGetInt(struct + (long)TAIL);
    }

    public static int nrqes(long struct) {
        return MemoryUtil.memGetInt(struct + (long)RQES);
    }

    public static int n__resv2(long struct) {
        return MemoryUtil.memGetInt(struct + (long)__RESV2);
    }

    public static LongBuffer n__resv(long struct) {
        return MemoryUtil.memLongBuffer(struct + (long)__RESV, 2);
    }

    public static long n__resv(long struct, int index) {
        return MemoryUtil.memGetLong(struct + (long)__RESV + Checks.check(index, 2) * 8L);
    }

    public static void nhead(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)HEAD, value);
    }

    public static void ntail(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)TAIL, value);
    }

    public static void nrqes(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)RQES, value);
    }

    public static void n__resv2(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)__RESV2, value);
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
        Struct.Layout layout = IOURingZCRXOffsets.__struct(IOURingZCRXOffsets.__member(4), IOURingZCRXOffsets.__member(4), IOURingZCRXOffsets.__member(4), IOURingZCRXOffsets.__member(4), IOURingZCRXOffsets.__array(8, 2));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        HEAD = layout.offsetof(0);
        TAIL = layout.offsetof(1);
        RQES = layout.offsetof(2);
        __RESV2 = layout.offsetof(3);
        __RESV = layout.offsetof(4);
    }

    public static class Buffer
    extends StructBuffer<IOURingZCRXOffsets, Buffer>
    implements NativeResource {
        private static final IOURingZCRXOffsets ELEMENT_FACTORY = IOURingZCRXOffsets.create(-1L);

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
        protected IOURingZCRXOffsets getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__u32")
        public int head() {
            return IOURingZCRXOffsets.nhead(this.address());
        }

        @NativeType(value="__u32")
        public int tail() {
            return IOURingZCRXOffsets.ntail(this.address());
        }

        @NativeType(value="__u32")
        public int rqes() {
            return IOURingZCRXOffsets.nrqes(this.address());
        }

        public Buffer head(@NativeType(value="__u32") int value) {
            IOURingZCRXOffsets.nhead(this.address(), value);
            return this;
        }

        public Buffer tail(@NativeType(value="__u32") int value) {
            IOURingZCRXOffsets.ntail(this.address(), value);
            return this;
        }

        public Buffer rqes(@NativeType(value="__u32") int value) {
            IOURingZCRXOffsets.nrqes(this.address(), value);
            return this;
        }
    }
}

