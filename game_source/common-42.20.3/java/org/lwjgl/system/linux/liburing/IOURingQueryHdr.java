/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.linux.liburing;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType(value="struct io_uring_query_hdr")
public class IOURingQueryHdr
extends Struct<IOURingQueryHdr>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int NEXT_ENTRY;
    public static final int QUERY_DATA;
    public static final int QUERY_OP;
    public static final int SIZE;
    public static final int RESULT;
    public static final int __RESV;

    protected IOURingQueryHdr(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected IOURingQueryHdr create(long address, @Nullable ByteBuffer container) {
        return new IOURingQueryHdr(address, container);
    }

    public IOURingQueryHdr(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOURingQueryHdr.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__u64")
    public long next_entry() {
        return IOURingQueryHdr.nnext_entry(this.address());
    }

    @NativeType(value="__u64")
    public long query_data() {
        return IOURingQueryHdr.nquery_data(this.address());
    }

    @NativeType(value="__u32")
    public int query_op() {
        return IOURingQueryHdr.nquery_op(this.address());
    }

    @NativeType(value="__u32")
    public int size() {
        return IOURingQueryHdr.nsize(this.address());
    }

    @NativeType(value="__s32")
    public int result() {
        return IOURingQueryHdr.nresult(this.address());
    }

    public IOURingQueryHdr next_entry(@NativeType(value="__u64") long value) {
        IOURingQueryHdr.nnext_entry(this.address(), value);
        return this;
    }

    public IOURingQueryHdr query_data(@NativeType(value="__u64") long value) {
        IOURingQueryHdr.nquery_data(this.address(), value);
        return this;
    }

    public IOURingQueryHdr query_op(@NativeType(value="__u32") int value) {
        IOURingQueryHdr.nquery_op(this.address(), value);
        return this;
    }

    public IOURingQueryHdr size(@NativeType(value="__u32") int value) {
        IOURingQueryHdr.nsize(this.address(), value);
        return this;
    }

    public IOURingQueryHdr result(@NativeType(value="__s32") int value) {
        IOURingQueryHdr.nresult(this.address(), value);
        return this;
    }

    public IOURingQueryHdr set(long next_entry, long query_data, int query_op, int size, int result) {
        this.next_entry(next_entry);
        this.query_data(query_data);
        this.query_op(query_op);
        this.size(size);
        this.result(result);
        return this;
    }

    public IOURingQueryHdr set(IOURingQueryHdr src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOURingQueryHdr malloc() {
        return new IOURingQueryHdr(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static IOURingQueryHdr calloc() {
        return new IOURingQueryHdr(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static IOURingQueryHdr create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new IOURingQueryHdr(MemoryUtil.memAddress(container), container);
    }

    public static IOURingQueryHdr create(long address) {
        return new IOURingQueryHdr(address, null);
    }

    public static @Nullable IOURingQueryHdr createSafe(long address) {
        return address == 0L ? null : new IOURingQueryHdr(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(IOURingQueryHdr.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOURingQueryHdr.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static IOURingQueryHdr malloc(MemoryStack stack) {
        return new IOURingQueryHdr(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static IOURingQueryHdr calloc(MemoryStack stack) {
        return new IOURingQueryHdr(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static long nnext_entry(long struct) {
        return MemoryUtil.memGetLong(struct + (long)NEXT_ENTRY);
    }

    public static long nquery_data(long struct) {
        return MemoryUtil.memGetLong(struct + (long)QUERY_DATA);
    }

    public static int nquery_op(long struct) {
        return MemoryUtil.memGetInt(struct + (long)QUERY_OP);
    }

    public static int nsize(long struct) {
        return MemoryUtil.memGetInt(struct + (long)SIZE);
    }

    public static int nresult(long struct) {
        return MemoryUtil.memGetInt(struct + (long)RESULT);
    }

    public static IntBuffer n__resv(long struct) {
        return MemoryUtil.memIntBuffer(struct + (long)__RESV, 3);
    }

    public static int n__resv(long struct, int index) {
        return MemoryUtil.memGetInt(struct + (long)__RESV + Checks.check(index, 3) * 4L);
    }

    public static void nnext_entry(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)NEXT_ENTRY, value);
    }

    public static void nquery_data(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)QUERY_DATA, value);
    }

    public static void nquery_op(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)QUERY_OP, value);
    }

    public static void nsize(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)SIZE, value);
    }

    public static void nresult(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)RESULT, value);
    }

    public static void n__resv(long struct, IntBuffer value) {
        if (Checks.CHECKS) {
            Checks.checkGT(value, 3);
        }
        MemoryUtil.memCopy(MemoryUtil.memAddress(value), struct + (long)__RESV, value.remaining() * 4);
    }

    public static void n__resv(long struct, int index, int value) {
        MemoryUtil.memPutInt(struct + (long)__RESV + Checks.check(index, 3) * 4L, value);
    }

    static {
        Struct.Layout layout = IOURingQueryHdr.__struct(IOURingQueryHdr.__member(8), IOURingQueryHdr.__member(8), IOURingQueryHdr.__member(4), IOURingQueryHdr.__member(4), IOURingQueryHdr.__member(4), IOURingQueryHdr.__array(4, 3));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        NEXT_ENTRY = layout.offsetof(0);
        QUERY_DATA = layout.offsetof(1);
        QUERY_OP = layout.offsetof(2);
        SIZE = layout.offsetof(3);
        RESULT = layout.offsetof(4);
        __RESV = layout.offsetof(5);
    }

    public static class Buffer
    extends StructBuffer<IOURingQueryHdr, Buffer>
    implements NativeResource {
        private static final IOURingQueryHdr ELEMENT_FACTORY = IOURingQueryHdr.create(-1L);

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
        protected IOURingQueryHdr getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__u64")
        public long next_entry() {
            return IOURingQueryHdr.nnext_entry(this.address());
        }

        @NativeType(value="__u64")
        public long query_data() {
            return IOURingQueryHdr.nquery_data(this.address());
        }

        @NativeType(value="__u32")
        public int query_op() {
            return IOURingQueryHdr.nquery_op(this.address());
        }

        @NativeType(value="__u32")
        public int size() {
            return IOURingQueryHdr.nsize(this.address());
        }

        @NativeType(value="__s32")
        public int result() {
            return IOURingQueryHdr.nresult(this.address());
        }

        public Buffer next_entry(@NativeType(value="__u64") long value) {
            IOURingQueryHdr.nnext_entry(this.address(), value);
            return this;
        }

        public Buffer query_data(@NativeType(value="__u64") long value) {
            IOURingQueryHdr.nquery_data(this.address(), value);
            return this;
        }

        public Buffer query_op(@NativeType(value="__u32") int value) {
            IOURingQueryHdr.nquery_op(this.address(), value);
            return this;
        }

        public Buffer size(@NativeType(value="__u32") int value) {
            IOURingQueryHdr.nsize(this.address(), value);
            return this;
        }

        public Buffer result(@NativeType(value="__s32") int value) {
            IOURingQueryHdr.nresult(this.address(), value);
            return this;
        }
    }
}

