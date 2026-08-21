/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.linux.liburing;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
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
import org.lwjgl.system.linux.KernelTimespec;

@NativeType(value="struct io_uring_reg_wait")
public class IOURingRegWait
extends Struct<IOURingRegWait>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int TS;
    public static final int MIN_WAIT_USEC;
    public static final int FLAGS;
    public static final int SIGMASK;
    public static final int SIGMASK_SZ;
    public static final int PAD;
    public static final int PAD2;

    protected IOURingRegWait(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected IOURingRegWait create(long address, @Nullable ByteBuffer container) {
        return new IOURingRegWait(address, container);
    }

    public IOURingRegWait(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOURingRegWait.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="struct __kernel_timespec")
    public KernelTimespec ts() {
        return IOURingRegWait.nts(this.address());
    }

    @NativeType(value="__u32")
    public int min_wait_usec() {
        return IOURingRegWait.nmin_wait_usec(this.address());
    }

    @NativeType(value="__u32")
    public int flags() {
        return IOURingRegWait.nflags(this.address());
    }

    @NativeType(value="__u64")
    public long sigmask() {
        return IOURingRegWait.nsigmask(this.address());
    }

    @NativeType(value="__u32")
    public int sigmask_sz() {
        return IOURingRegWait.nsigmask_sz(this.address());
    }

    public IOURingRegWait ts(@NativeType(value="struct __kernel_timespec") KernelTimespec value) {
        IOURingRegWait.nts(this.address(), value);
        return this;
    }

    public IOURingRegWait ts(Consumer<KernelTimespec> consumer) {
        consumer.accept(this.ts());
        return this;
    }

    public IOURingRegWait min_wait_usec(@NativeType(value="__u32") int value) {
        IOURingRegWait.nmin_wait_usec(this.address(), value);
        return this;
    }

    public IOURingRegWait flags(@NativeType(value="__u32") int value) {
        IOURingRegWait.nflags(this.address(), value);
        return this;
    }

    public IOURingRegWait sigmask(@NativeType(value="__u64") long value) {
        IOURingRegWait.nsigmask(this.address(), value);
        return this;
    }

    public IOURingRegWait sigmask_sz(@NativeType(value="__u32") int value) {
        IOURingRegWait.nsigmask_sz(this.address(), value);
        return this;
    }

    public IOURingRegWait set(KernelTimespec ts, int min_wait_usec, int flags, long sigmask, int sigmask_sz) {
        this.ts(ts);
        this.min_wait_usec(min_wait_usec);
        this.flags(flags);
        this.sigmask(sigmask);
        this.sigmask_sz(sigmask_sz);
        return this;
    }

    public IOURingRegWait set(IOURingRegWait src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOURingRegWait malloc() {
        return new IOURingRegWait(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static IOURingRegWait calloc() {
        return new IOURingRegWait(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static IOURingRegWait create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new IOURingRegWait(MemoryUtil.memAddress(container), container);
    }

    public static IOURingRegWait create(long address) {
        return new IOURingRegWait(address, null);
    }

    public static @Nullable IOURingRegWait createSafe(long address) {
        return address == 0L ? null : new IOURingRegWait(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(IOURingRegWait.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOURingRegWait.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static IOURingRegWait malloc(MemoryStack stack) {
        return new IOURingRegWait(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static IOURingRegWait calloc(MemoryStack stack) {
        return new IOURingRegWait(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static KernelTimespec nts(long struct) {
        return KernelTimespec.create(struct + (long)TS);
    }

    public static int nmin_wait_usec(long struct) {
        return MemoryUtil.memGetInt(struct + (long)MIN_WAIT_USEC);
    }

    public static int nflags(long struct) {
        return MemoryUtil.memGetInt(struct + (long)FLAGS);
    }

    public static long nsigmask(long struct) {
        return MemoryUtil.memGetLong(struct + (long)SIGMASK);
    }

    public static int nsigmask_sz(long struct) {
        return MemoryUtil.memGetInt(struct + (long)SIGMASK_SZ);
    }

    public static IntBuffer npad(long struct) {
        return MemoryUtil.memIntBuffer(struct + (long)PAD, 3);
    }

    public static int npad(long struct, int index) {
        return MemoryUtil.memGetInt(struct + (long)PAD + Checks.check(index, 3) * 4L);
    }

    public static LongBuffer npad2(long struct) {
        return MemoryUtil.memLongBuffer(struct + (long)PAD2, 2);
    }

    public static long npad2(long struct, int index) {
        return MemoryUtil.memGetLong(struct + (long)PAD2 + Checks.check(index, 2) * 8L);
    }

    public static void nts(long struct, KernelTimespec value) {
        MemoryUtil.memCopy(value.address(), struct + (long)TS, KernelTimespec.SIZEOF);
    }

    public static void nmin_wait_usec(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)MIN_WAIT_USEC, value);
    }

    public static void nflags(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)FLAGS, value);
    }

    public static void nsigmask(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)SIGMASK, value);
    }

    public static void nsigmask_sz(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)SIGMASK_SZ, value);
    }

    public static void npad(long struct, IntBuffer value) {
        if (Checks.CHECKS) {
            Checks.checkGT(value, 3);
        }
        MemoryUtil.memCopy(MemoryUtil.memAddress(value), struct + (long)PAD, value.remaining() * 4);
    }

    public static void npad(long struct, int index, int value) {
        MemoryUtil.memPutInt(struct + (long)PAD + Checks.check(index, 3) * 4L, value);
    }

    public static void npad2(long struct, LongBuffer value) {
        if (Checks.CHECKS) {
            Checks.checkGT(value, 2);
        }
        MemoryUtil.memCopy(MemoryUtil.memAddress(value), struct + (long)PAD2, value.remaining() * 8);
    }

    public static void npad2(long struct, int index, long value) {
        MemoryUtil.memPutLong(struct + (long)PAD2 + Checks.check(index, 2) * 8L, value);
    }

    static {
        Struct.Layout layout = IOURingRegWait.__struct(IOURingRegWait.__member(KernelTimespec.SIZEOF, KernelTimespec.ALIGNOF), IOURingRegWait.__member(4), IOURingRegWait.__member(4), IOURingRegWait.__member(8), IOURingRegWait.__member(4), IOURingRegWait.__array(4, 3), IOURingRegWait.__array(8, 2));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        TS = layout.offsetof(0);
        MIN_WAIT_USEC = layout.offsetof(1);
        FLAGS = layout.offsetof(2);
        SIGMASK = layout.offsetof(3);
        SIGMASK_SZ = layout.offsetof(4);
        PAD = layout.offsetof(5);
        PAD2 = layout.offsetof(6);
    }

    public static class Buffer
    extends StructBuffer<IOURingRegWait, Buffer>
    implements NativeResource {
        private static final IOURingRegWait ELEMENT_FACTORY = IOURingRegWait.create(-1L);

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
        protected IOURingRegWait getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="struct __kernel_timespec")
        public KernelTimespec ts() {
            return IOURingRegWait.nts(this.address());
        }

        @NativeType(value="__u32")
        public int min_wait_usec() {
            return IOURingRegWait.nmin_wait_usec(this.address());
        }

        @NativeType(value="__u32")
        public int flags() {
            return IOURingRegWait.nflags(this.address());
        }

        @NativeType(value="__u64")
        public long sigmask() {
            return IOURingRegWait.nsigmask(this.address());
        }

        @NativeType(value="__u32")
        public int sigmask_sz() {
            return IOURingRegWait.nsigmask_sz(this.address());
        }

        public Buffer ts(@NativeType(value="struct __kernel_timespec") KernelTimespec value) {
            IOURingRegWait.nts(this.address(), value);
            return this;
        }

        public Buffer ts(Consumer<KernelTimespec> consumer) {
            consumer.accept(this.ts());
            return this;
        }

        public Buffer min_wait_usec(@NativeType(value="__u32") int value) {
            IOURingRegWait.nmin_wait_usec(this.address(), value);
            return this;
        }

        public Buffer flags(@NativeType(value="__u32") int value) {
            IOURingRegWait.nflags(this.address(), value);
            return this;
        }

        public Buffer sigmask(@NativeType(value="__u64") long value) {
            IOURingRegWait.nsigmask(this.address(), value);
            return this;
        }

        public Buffer sigmask_sz(@NativeType(value="__u32") int value) {
            IOURingRegWait.nsigmask_sz(this.address(), value);
            return this;
        }
    }
}

