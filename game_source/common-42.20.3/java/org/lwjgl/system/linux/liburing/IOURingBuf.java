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

@NativeType(value="struct io_uring_buf")
public class IOURingBuf
extends Struct<IOURingBuf>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int ADDR;
    public static final int LEN;
    public static final int BID;
    public static final int RESV;

    protected IOURingBuf(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected IOURingBuf create(long address, @Nullable ByteBuffer container) {
        return new IOURingBuf(address, container);
    }

    public IOURingBuf(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOURingBuf.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__u64")
    public long addr() {
        return IOURingBuf.naddr(this.address());
    }

    @NativeType(value="__u32")
    public int len() {
        return IOURingBuf.nlen(this.address());
    }

    @NativeType(value="__u16")
    public short bid() {
        return IOURingBuf.nbid(this.address());
    }

    public IOURingBuf addr(@NativeType(value="__u64") long value) {
        IOURingBuf.naddr(this.address(), value);
        return this;
    }

    public IOURingBuf len(@NativeType(value="__u32") int value) {
        IOURingBuf.nlen(this.address(), value);
        return this;
    }

    public IOURingBuf bid(@NativeType(value="__u16") short value) {
        IOURingBuf.nbid(this.address(), value);
        return this;
    }

    public IOURingBuf set(long addr, int len, short bid) {
        this.addr(addr);
        this.len(len);
        this.bid(bid);
        return this;
    }

    public IOURingBuf set(IOURingBuf src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOURingBuf malloc() {
        return new IOURingBuf(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static IOURingBuf calloc() {
        return new IOURingBuf(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static IOURingBuf create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new IOURingBuf(MemoryUtil.memAddress(container), container);
    }

    public static IOURingBuf create(long address) {
        return new IOURingBuf(address, null);
    }

    public static @Nullable IOURingBuf createSafe(long address) {
        return address == 0L ? null : new IOURingBuf(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(IOURingBuf.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOURingBuf.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static IOURingBuf malloc(MemoryStack stack) {
        return new IOURingBuf(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static IOURingBuf calloc(MemoryStack stack) {
        return new IOURingBuf(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
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

    public static int nlen(long struct) {
        return MemoryUtil.memGetInt(struct + (long)LEN);
    }

    public static short nbid(long struct) {
        return MemoryUtil.memGetShort(struct + (long)BID);
    }

    public static short nresv(long struct) {
        return MemoryUtil.memGetShort(struct + (long)RESV);
    }

    public static void naddr(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)ADDR, value);
    }

    public static void nlen(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)LEN, value);
    }

    public static void nbid(long struct, short value) {
        MemoryUtil.memPutShort(struct + (long)BID, value);
    }

    public static void nresv(long struct, short value) {
        MemoryUtil.memPutShort(struct + (long)RESV, value);
    }

    static {
        Struct.Layout layout = IOURingBuf.__struct(IOURingBuf.__member(8), IOURingBuf.__member(4), IOURingBuf.__member(2), IOURingBuf.__member(2));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        ADDR = layout.offsetof(0);
        LEN = layout.offsetof(1);
        BID = layout.offsetof(2);
        RESV = layout.offsetof(3);
    }

    public static class Buffer
    extends StructBuffer<IOURingBuf, Buffer>
    implements NativeResource {
        private static final IOURingBuf ELEMENT_FACTORY = IOURingBuf.create(-1L);

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
        protected IOURingBuf getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__u64")
        public long addr() {
            return IOURingBuf.naddr(this.address());
        }

        @NativeType(value="__u32")
        public int len() {
            return IOURingBuf.nlen(this.address());
        }

        @NativeType(value="__u16")
        public short bid() {
            return IOURingBuf.nbid(this.address());
        }

        public Buffer addr(@NativeType(value="__u64") long value) {
            IOURingBuf.naddr(this.address(), value);
            return this;
        }

        public Buffer len(@NativeType(value="__u32") int value) {
            IOURingBuf.nlen(this.address(), value);
            return this;
        }

        public Buffer bid(@NativeType(value="__u16") short value) {
            IOURingBuf.nbid(this.address(), value);
            return this;
        }
    }
}

