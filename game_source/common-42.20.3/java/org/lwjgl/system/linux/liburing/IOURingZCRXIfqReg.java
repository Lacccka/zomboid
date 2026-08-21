/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.linux.liburing;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;
import org.lwjgl.system.linux.liburing.IOURingZCRXOffsets;

@NativeType(value="struct io_uring_zcrx_ifq_reg")
public class IOURingZCRXIfqReg
extends Struct<IOURingZCRXIfqReg>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int IF_IDX;
    public static final int IF_RXQ;
    public static final int RQ_ENTRIES;
    public static final int FLAGS;
    public static final int AREA_PTR;
    public static final int REGION_PTR;
    public static final int OFFSETS;
    public static final int ZCRX_ID;
    public static final int __RESV2;
    public static final int __RESV;

    protected IOURingZCRXIfqReg(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected IOURingZCRXIfqReg create(long address, @Nullable ByteBuffer container) {
        return new IOURingZCRXIfqReg(address, container);
    }

    public IOURingZCRXIfqReg(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOURingZCRXIfqReg.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__u32")
    public int if_idx() {
        return IOURingZCRXIfqReg.nif_idx(this.address());
    }

    @NativeType(value="__u32")
    public int if_rxq() {
        return IOURingZCRXIfqReg.nif_rxq(this.address());
    }

    @NativeType(value="__u32")
    public int rq_entries() {
        return IOURingZCRXIfqReg.nrq_entries(this.address());
    }

    @NativeType(value="__u32")
    public int flags() {
        return IOURingZCRXIfqReg.nflags(this.address());
    }

    @NativeType(value="__u64")
    public long area_ptr() {
        return IOURingZCRXIfqReg.narea_ptr(this.address());
    }

    @NativeType(value="__u64")
    public long region_ptr() {
        return IOURingZCRXIfqReg.nregion_ptr(this.address());
    }

    @NativeType(value="struct io_uring_zcrx_offsets")
    public IOURingZCRXOffsets offsets() {
        return IOURingZCRXIfqReg.noffsets(this.address());
    }

    @NativeType(value="__u32")
    public int zcrx_id() {
        return IOURingZCRXIfqReg.nzcrx_id(this.address());
    }

    public IOURingZCRXIfqReg if_idx(@NativeType(value="__u32") int value) {
        IOURingZCRXIfqReg.nif_idx(this.address(), value);
        return this;
    }

    public IOURingZCRXIfqReg if_rxq(@NativeType(value="__u32") int value) {
        IOURingZCRXIfqReg.nif_rxq(this.address(), value);
        return this;
    }

    public IOURingZCRXIfqReg rq_entries(@NativeType(value="__u32") int value) {
        IOURingZCRXIfqReg.nrq_entries(this.address(), value);
        return this;
    }

    public IOURingZCRXIfqReg flags(@NativeType(value="__u32") int value) {
        IOURingZCRXIfqReg.nflags(this.address(), value);
        return this;
    }

    public IOURingZCRXIfqReg area_ptr(@NativeType(value="__u64") long value) {
        IOURingZCRXIfqReg.narea_ptr(this.address(), value);
        return this;
    }

    public IOURingZCRXIfqReg region_ptr(@NativeType(value="__u64") long value) {
        IOURingZCRXIfqReg.nregion_ptr(this.address(), value);
        return this;
    }

    public IOURingZCRXIfqReg offsets(@NativeType(value="struct io_uring_zcrx_offsets") IOURingZCRXOffsets value) {
        IOURingZCRXIfqReg.noffsets(this.address(), value);
        return this;
    }

    public IOURingZCRXIfqReg offsets(Consumer<IOURingZCRXOffsets> consumer) {
        consumer.accept(this.offsets());
        return this;
    }

    public IOURingZCRXIfqReg zcrx_id(@NativeType(value="__u32") int value) {
        IOURingZCRXIfqReg.nzcrx_id(this.address(), value);
        return this;
    }

    public IOURingZCRXIfqReg set(int if_idx, int if_rxq, int rq_entries, int flags, long area_ptr, long region_ptr, IOURingZCRXOffsets offsets, int zcrx_id) {
        this.if_idx(if_idx);
        this.if_rxq(if_rxq);
        this.rq_entries(rq_entries);
        this.flags(flags);
        this.area_ptr(area_ptr);
        this.region_ptr(region_ptr);
        this.offsets(offsets);
        this.zcrx_id(zcrx_id);
        return this;
    }

    public IOURingZCRXIfqReg set(IOURingZCRXIfqReg src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOURingZCRXIfqReg malloc() {
        return new IOURingZCRXIfqReg(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static IOURingZCRXIfqReg calloc() {
        return new IOURingZCRXIfqReg(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static IOURingZCRXIfqReg create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new IOURingZCRXIfqReg(MemoryUtil.memAddress(container), container);
    }

    public static IOURingZCRXIfqReg create(long address) {
        return new IOURingZCRXIfqReg(address, null);
    }

    public static @Nullable IOURingZCRXIfqReg createSafe(long address) {
        return address == 0L ? null : new IOURingZCRXIfqReg(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(IOURingZCRXIfqReg.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOURingZCRXIfqReg.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static IOURingZCRXIfqReg malloc(MemoryStack stack) {
        return new IOURingZCRXIfqReg(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static IOURingZCRXIfqReg calloc(MemoryStack stack) {
        return new IOURingZCRXIfqReg(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int nif_idx(long struct) {
        return MemoryUtil.memGetInt(struct + (long)IF_IDX);
    }

    public static int nif_rxq(long struct) {
        return MemoryUtil.memGetInt(struct + (long)IF_RXQ);
    }

    public static int nrq_entries(long struct) {
        return MemoryUtil.memGetInt(struct + (long)RQ_ENTRIES);
    }

    public static int nflags(long struct) {
        return MemoryUtil.memGetInt(struct + (long)FLAGS);
    }

    public static long narea_ptr(long struct) {
        return MemoryUtil.memGetLong(struct + (long)AREA_PTR);
    }

    public static long nregion_ptr(long struct) {
        return MemoryUtil.memGetLong(struct + (long)REGION_PTR);
    }

    public static IOURingZCRXOffsets noffsets(long struct) {
        return IOURingZCRXOffsets.create(struct + (long)OFFSETS);
    }

    public static int nzcrx_id(long struct) {
        return MemoryUtil.memGetInt(struct + (long)ZCRX_ID);
    }

    public static int n__resv2(long struct) {
        return MemoryUtil.memGetInt(struct + (long)__RESV2);
    }

    public static LongBuffer n__resv(long struct) {
        return MemoryUtil.memLongBuffer(struct + (long)__RESV, 3);
    }

    public static long n__resv(long struct, int index) {
        return MemoryUtil.memGetLong(struct + (long)__RESV + Checks.check(index, 3) * 8L);
    }

    public static void nif_idx(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)IF_IDX, value);
    }

    public static void nif_rxq(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)IF_RXQ, value);
    }

    public static void nrq_entries(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)RQ_ENTRIES, value);
    }

    public static void nflags(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)FLAGS, value);
    }

    public static void narea_ptr(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)AREA_PTR, value);
    }

    public static void nregion_ptr(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)REGION_PTR, value);
    }

    public static void noffsets(long struct, IOURingZCRXOffsets value) {
        MemoryUtil.memCopy(value.address(), struct + (long)OFFSETS, IOURingZCRXOffsets.SIZEOF);
    }

    public static void nzcrx_id(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)ZCRX_ID, value);
    }

    public static void n__resv2(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)__RESV2, value);
    }

    public static void n__resv(long struct, LongBuffer value) {
        if (Checks.CHECKS) {
            Checks.checkGT(value, 3);
        }
        MemoryUtil.memCopy(MemoryUtil.memAddress(value), struct + (long)__RESV, value.remaining() * 8);
    }

    public static void n__resv(long struct, int index, long value) {
        MemoryUtil.memPutLong(struct + (long)__RESV + Checks.check(index, 3) * 8L, value);
    }

    static {
        Struct.Layout layout = IOURingZCRXIfqReg.__struct(IOURingZCRXIfqReg.__member(4), IOURingZCRXIfqReg.__member(4), IOURingZCRXIfqReg.__member(4), IOURingZCRXIfqReg.__member(4), IOURingZCRXIfqReg.__member(8), IOURingZCRXIfqReg.__member(8), IOURingZCRXIfqReg.__member(IOURingZCRXOffsets.SIZEOF, IOURingZCRXOffsets.ALIGNOF), IOURingZCRXIfqReg.__member(4), IOURingZCRXIfqReg.__member(4), IOURingZCRXIfqReg.__array(8, 3));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        IF_IDX = layout.offsetof(0);
        IF_RXQ = layout.offsetof(1);
        RQ_ENTRIES = layout.offsetof(2);
        FLAGS = layout.offsetof(3);
        AREA_PTR = layout.offsetof(4);
        REGION_PTR = layout.offsetof(5);
        OFFSETS = layout.offsetof(6);
        ZCRX_ID = layout.offsetof(7);
        __RESV2 = layout.offsetof(8);
        __RESV = layout.offsetof(9);
    }

    public static class Buffer
    extends StructBuffer<IOURingZCRXIfqReg, Buffer>
    implements NativeResource {
        private static final IOURingZCRXIfqReg ELEMENT_FACTORY = IOURingZCRXIfqReg.create(-1L);

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
        protected IOURingZCRXIfqReg getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__u32")
        public int if_idx() {
            return IOURingZCRXIfqReg.nif_idx(this.address());
        }

        @NativeType(value="__u32")
        public int if_rxq() {
            return IOURingZCRXIfqReg.nif_rxq(this.address());
        }

        @NativeType(value="__u32")
        public int rq_entries() {
            return IOURingZCRXIfqReg.nrq_entries(this.address());
        }

        @NativeType(value="__u32")
        public int flags() {
            return IOURingZCRXIfqReg.nflags(this.address());
        }

        @NativeType(value="__u64")
        public long area_ptr() {
            return IOURingZCRXIfqReg.narea_ptr(this.address());
        }

        @NativeType(value="__u64")
        public long region_ptr() {
            return IOURingZCRXIfqReg.nregion_ptr(this.address());
        }

        @NativeType(value="struct io_uring_zcrx_offsets")
        public IOURingZCRXOffsets offsets() {
            return IOURingZCRXIfqReg.noffsets(this.address());
        }

        @NativeType(value="__u32")
        public int zcrx_id() {
            return IOURingZCRXIfqReg.nzcrx_id(this.address());
        }

        public Buffer if_idx(@NativeType(value="__u32") int value) {
            IOURingZCRXIfqReg.nif_idx(this.address(), value);
            return this;
        }

        public Buffer if_rxq(@NativeType(value="__u32") int value) {
            IOURingZCRXIfqReg.nif_rxq(this.address(), value);
            return this;
        }

        public Buffer rq_entries(@NativeType(value="__u32") int value) {
            IOURingZCRXIfqReg.nrq_entries(this.address(), value);
            return this;
        }

        public Buffer flags(@NativeType(value="__u32") int value) {
            IOURingZCRXIfqReg.nflags(this.address(), value);
            return this;
        }

        public Buffer area_ptr(@NativeType(value="__u64") long value) {
            IOURingZCRXIfqReg.narea_ptr(this.address(), value);
            return this;
        }

        public Buffer region_ptr(@NativeType(value="__u64") long value) {
            IOURingZCRXIfqReg.nregion_ptr(this.address(), value);
            return this;
        }

        public Buffer offsets(@NativeType(value="struct io_uring_zcrx_offsets") IOURingZCRXOffsets value) {
            IOURingZCRXIfqReg.noffsets(this.address(), value);
            return this;
        }

        public Buffer offsets(Consumer<IOURingZCRXOffsets> consumer) {
            consumer.accept(this.offsets());
            return this;
        }

        public Buffer zcrx_id(@NativeType(value="__u32") int value) {
            IOURingZCRXIfqReg.nzcrx_id(this.address(), value);
            return this;
        }
    }
}

