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

@NativeType(value="struct io_uring_getevents_arg")
public class IOURingGeteventsArg
extends Struct<IOURingGeteventsArg>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int SIGMASK;
    public static final int SIGMASK_SZ;
    public static final int MIN_WAIT_USEC;
    public static final int TS;

    protected IOURingGeteventsArg(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected IOURingGeteventsArg create(long address, @Nullable ByteBuffer container) {
        return new IOURingGeteventsArg(address, container);
    }

    public IOURingGeteventsArg(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOURingGeteventsArg.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__u64")
    public long sigmask() {
        return IOURingGeteventsArg.nsigmask(this.address());
    }

    @NativeType(value="__u32")
    public int sigmask_sz() {
        return IOURingGeteventsArg.nsigmask_sz(this.address());
    }

    @NativeType(value="__u32")
    public int min_wait_usec() {
        return IOURingGeteventsArg.nmin_wait_usec(this.address());
    }

    @NativeType(value="__u64")
    public long ts() {
        return IOURingGeteventsArg.nts(this.address());
    }

    public IOURingGeteventsArg sigmask(@NativeType(value="__u64") long value) {
        IOURingGeteventsArg.nsigmask(this.address(), value);
        return this;
    }

    public IOURingGeteventsArg sigmask_sz(@NativeType(value="__u32") int value) {
        IOURingGeteventsArg.nsigmask_sz(this.address(), value);
        return this;
    }

    public IOURingGeteventsArg min_wait_usec(@NativeType(value="__u32") int value) {
        IOURingGeteventsArg.nmin_wait_usec(this.address(), value);
        return this;
    }

    public IOURingGeteventsArg ts(@NativeType(value="__u64") long value) {
        IOURingGeteventsArg.nts(this.address(), value);
        return this;
    }

    public IOURingGeteventsArg set(long sigmask, int sigmask_sz, int min_wait_usec, long ts) {
        this.sigmask(sigmask);
        this.sigmask_sz(sigmask_sz);
        this.min_wait_usec(min_wait_usec);
        this.ts(ts);
        return this;
    }

    public IOURingGeteventsArg set(IOURingGeteventsArg src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOURingGeteventsArg malloc() {
        return new IOURingGeteventsArg(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static IOURingGeteventsArg calloc() {
        return new IOURingGeteventsArg(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static IOURingGeteventsArg create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new IOURingGeteventsArg(MemoryUtil.memAddress(container), container);
    }

    public static IOURingGeteventsArg create(long address) {
        return new IOURingGeteventsArg(address, null);
    }

    public static @Nullable IOURingGeteventsArg createSafe(long address) {
        return address == 0L ? null : new IOURingGeteventsArg(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(IOURingGeteventsArg.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOURingGeteventsArg.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static IOURingGeteventsArg malloc(MemoryStack stack) {
        return new IOURingGeteventsArg(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static IOURingGeteventsArg calloc(MemoryStack stack) {
        return new IOURingGeteventsArg(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static long nsigmask(long struct) {
        return MemoryUtil.memGetLong(struct + (long)SIGMASK);
    }

    public static int nsigmask_sz(long struct) {
        return MemoryUtil.memGetInt(struct + (long)SIGMASK_SZ);
    }

    public static int nmin_wait_usec(long struct) {
        return MemoryUtil.memGetInt(struct + (long)MIN_WAIT_USEC);
    }

    public static long nts(long struct) {
        return MemoryUtil.memGetLong(struct + (long)TS);
    }

    public static void nsigmask(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)SIGMASK, value);
    }

    public static void nsigmask_sz(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)SIGMASK_SZ, value);
    }

    public static void nmin_wait_usec(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)MIN_WAIT_USEC, value);
    }

    public static void nts(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)TS, value);
    }

    static {
        Struct.Layout layout = IOURingGeteventsArg.__struct(IOURingGeteventsArg.__member(8), IOURingGeteventsArg.__member(4), IOURingGeteventsArg.__member(4), IOURingGeteventsArg.__member(8));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        SIGMASK = layout.offsetof(0);
        SIGMASK_SZ = layout.offsetof(1);
        MIN_WAIT_USEC = layout.offsetof(2);
        TS = layout.offsetof(3);
    }

    public static class Buffer
    extends StructBuffer<IOURingGeteventsArg, Buffer>
    implements NativeResource {
        private static final IOURingGeteventsArg ELEMENT_FACTORY = IOURingGeteventsArg.create(-1L);

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
        protected IOURingGeteventsArg getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__u64")
        public long sigmask() {
            return IOURingGeteventsArg.nsigmask(this.address());
        }

        @NativeType(value="__u32")
        public int sigmask_sz() {
            return IOURingGeteventsArg.nsigmask_sz(this.address());
        }

        @NativeType(value="__u32")
        public int min_wait_usec() {
            return IOURingGeteventsArg.nmin_wait_usec(this.address());
        }

        @NativeType(value="__u64")
        public long ts() {
            return IOURingGeteventsArg.nts(this.address());
        }

        public Buffer sigmask(@NativeType(value="__u64") long value) {
            IOURingGeteventsArg.nsigmask(this.address(), value);
            return this;
        }

        public Buffer sigmask_sz(@NativeType(value="__u32") int value) {
            IOURingGeteventsArg.nsigmask_sz(this.address(), value);
            return this;
        }

        public Buffer min_wait_usec(@NativeType(value="__u32") int value) {
            IOURingGeteventsArg.nmin_wait_usec(this.address(), value);
            return this;
        }

        public Buffer ts(@NativeType(value="__u64") long value) {
            IOURingGeteventsArg.nts(this.address(), value);
            return this;
        }
    }
}

