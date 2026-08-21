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

@NativeType(value="struct io_uring_zcrx_area_reg")
public class IOURingZCRXAreaReg
extends Struct<IOURingZCRXAreaReg>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int ADDR;
    public static final int LEN;
    public static final int RQ_AREA_TOKEN;
    public static final int FLAGS;
    public static final int DMABUF_FD;
    public static final int __RESV2;

    protected IOURingZCRXAreaReg(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected IOURingZCRXAreaReg create(long address, @Nullable ByteBuffer container) {
        return new IOURingZCRXAreaReg(address, container);
    }

    public IOURingZCRXAreaReg(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOURingZCRXAreaReg.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__u64")
    public long addr() {
        return IOURingZCRXAreaReg.naddr(this.address());
    }

    @NativeType(value="__u64")
    public long len() {
        return IOURingZCRXAreaReg.nlen(this.address());
    }

    @NativeType(value="__u64")
    public long rq_area_token() {
        return IOURingZCRXAreaReg.nrq_area_token(this.address());
    }

    @NativeType(value="__u32")
    public int flags() {
        return IOURingZCRXAreaReg.nflags(this.address());
    }

    @NativeType(value="__u32")
    public int dmabuf_fd() {
        return IOURingZCRXAreaReg.ndmabuf_fd(this.address());
    }

    public IOURingZCRXAreaReg addr(@NativeType(value="__u64") long value) {
        IOURingZCRXAreaReg.naddr(this.address(), value);
        return this;
    }

    public IOURingZCRXAreaReg len(@NativeType(value="__u64") long value) {
        IOURingZCRXAreaReg.nlen(this.address(), value);
        return this;
    }

    public IOURingZCRXAreaReg rq_area_token(@NativeType(value="__u64") long value) {
        IOURingZCRXAreaReg.nrq_area_token(this.address(), value);
        return this;
    }

    public IOURingZCRXAreaReg flags(@NativeType(value="__u32") int value) {
        IOURingZCRXAreaReg.nflags(this.address(), value);
        return this;
    }

    public IOURingZCRXAreaReg dmabuf_fd(@NativeType(value="__u32") int value) {
        IOURingZCRXAreaReg.ndmabuf_fd(this.address(), value);
        return this;
    }

    public IOURingZCRXAreaReg set(long addr, long len, long rq_area_token, int flags, int dmabuf_fd) {
        this.addr(addr);
        this.len(len);
        this.rq_area_token(rq_area_token);
        this.flags(flags);
        this.dmabuf_fd(dmabuf_fd);
        return this;
    }

    public IOURingZCRXAreaReg set(IOURingZCRXAreaReg src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOURingZCRXAreaReg malloc() {
        return new IOURingZCRXAreaReg(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static IOURingZCRXAreaReg calloc() {
        return new IOURingZCRXAreaReg(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static IOURingZCRXAreaReg create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new IOURingZCRXAreaReg(MemoryUtil.memAddress(container), container);
    }

    public static IOURingZCRXAreaReg create(long address) {
        return new IOURingZCRXAreaReg(address, null);
    }

    public static @Nullable IOURingZCRXAreaReg createSafe(long address) {
        return address == 0L ? null : new IOURingZCRXAreaReg(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(IOURingZCRXAreaReg.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOURingZCRXAreaReg.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static IOURingZCRXAreaReg malloc(MemoryStack stack) {
        return new IOURingZCRXAreaReg(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static IOURingZCRXAreaReg calloc(MemoryStack stack) {
        return new IOURingZCRXAreaReg(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static long naddr(long struct) {
        return MemoryUtil.memGetLong(struct + (long)ADDR);
    }

    public static long nlen(long struct) {
        return MemoryUtil.memGetLong(struct + (long)LEN);
    }

    public static long nrq_area_token(long struct) {
        return MemoryUtil.memGetLong(struct + (long)RQ_AREA_TOKEN);
    }

    public static int nflags(long struct) {
        return MemoryUtil.memGetInt(struct + (long)FLAGS);
    }

    public static int ndmabuf_fd(long struct) {
        return MemoryUtil.memGetInt(struct + (long)DMABUF_FD);
    }

    public static LongBuffer n__resv2(long struct) {
        return MemoryUtil.memLongBuffer(struct + (long)__RESV2, 2);
    }

    public static long n__resv2(long struct, int index) {
        return MemoryUtil.memGetLong(struct + (long)__RESV2 + Checks.check(index, 2) * 8L);
    }

    public static void naddr(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)ADDR, value);
    }

    public static void nlen(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)LEN, value);
    }

    public static void nrq_area_token(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)RQ_AREA_TOKEN, value);
    }

    public static void nflags(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)FLAGS, value);
    }

    public static void ndmabuf_fd(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)DMABUF_FD, value);
    }

    public static void n__resv2(long struct, LongBuffer value) {
        if (Checks.CHECKS) {
            Checks.checkGT(value, 2);
        }
        MemoryUtil.memCopy(MemoryUtil.memAddress(value), struct + (long)__RESV2, value.remaining() * 8);
    }

    public static void n__resv2(long struct, int index, long value) {
        MemoryUtil.memPutLong(struct + (long)__RESV2 + Checks.check(index, 2) * 8L, value);
    }

    static {
        Struct.Layout layout = IOURingZCRXAreaReg.__struct(IOURingZCRXAreaReg.__member(8), IOURingZCRXAreaReg.__member(8), IOURingZCRXAreaReg.__member(8), IOURingZCRXAreaReg.__member(4), IOURingZCRXAreaReg.__member(4), IOURingZCRXAreaReg.__array(8, 2));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        ADDR = layout.offsetof(0);
        LEN = layout.offsetof(1);
        RQ_AREA_TOKEN = layout.offsetof(2);
        FLAGS = layout.offsetof(3);
        DMABUF_FD = layout.offsetof(4);
        __RESV2 = layout.offsetof(5);
    }

    public static class Buffer
    extends StructBuffer<IOURingZCRXAreaReg, Buffer>
    implements NativeResource {
        private static final IOURingZCRXAreaReg ELEMENT_FACTORY = IOURingZCRXAreaReg.create(-1L);

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
        protected IOURingZCRXAreaReg getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__u64")
        public long addr() {
            return IOURingZCRXAreaReg.naddr(this.address());
        }

        @NativeType(value="__u64")
        public long len() {
            return IOURingZCRXAreaReg.nlen(this.address());
        }

        @NativeType(value="__u64")
        public long rq_area_token() {
            return IOURingZCRXAreaReg.nrq_area_token(this.address());
        }

        @NativeType(value="__u32")
        public int flags() {
            return IOURingZCRXAreaReg.nflags(this.address());
        }

        @NativeType(value="__u32")
        public int dmabuf_fd() {
            return IOURingZCRXAreaReg.ndmabuf_fd(this.address());
        }

        public Buffer addr(@NativeType(value="__u64") long value) {
            IOURingZCRXAreaReg.naddr(this.address(), value);
            return this;
        }

        public Buffer len(@NativeType(value="__u64") long value) {
            IOURingZCRXAreaReg.nlen(this.address(), value);
            return this;
        }

        public Buffer rq_area_token(@NativeType(value="__u64") long value) {
            IOURingZCRXAreaReg.nrq_area_token(this.address(), value);
            return this;
        }

        public Buffer flags(@NativeType(value="__u32") int value) {
            IOURingZCRXAreaReg.nflags(this.address(), value);
            return this;
        }

        public Buffer dmabuf_fd(@NativeType(value="__u32") int value) {
            IOURingZCRXAreaReg.ndmabuf_fd(this.address(), value);
            return this;
        }
    }
}

