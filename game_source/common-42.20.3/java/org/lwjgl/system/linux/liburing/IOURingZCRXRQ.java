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
import org.lwjgl.system.linux.liburing.IOURingZCRXRQE;

@NativeType(value="struct io_uring_zcrx_rq")
public class IOURingZCRXRQ
extends Struct<IOURingZCRXRQ>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int KHEAD;
    public static final int KTAIL;
    public static final int RQ_TAIL;
    public static final int RING_ENTRIES;
    public static final int RQES;
    public static final int RING_PTR;

    protected IOURingZCRXRQ(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected IOURingZCRXRQ create(long address, @Nullable ByteBuffer container) {
        return new IOURingZCRXRQ(address, container);
    }

    public IOURingZCRXRQ(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOURingZCRXRQ.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__u32 *")
    public IntBuffer khead(int capacity) {
        return IOURingZCRXRQ.nkhead(this.address(), capacity);
    }

    @NativeType(value="__u32 *")
    public IntBuffer ktail(int capacity) {
        return IOURingZCRXRQ.nktail(this.address(), capacity);
    }

    @NativeType(value="__u32")
    public int rq_tail() {
        return IOURingZCRXRQ.nrq_tail(this.address());
    }

    @NativeType(value="unsigned")
    public int ring_entries() {
        return IOURingZCRXRQ.nring_entries(this.address());
    }

    @NativeType(value="struct io_uring_zcrx_rqe *")
    public IOURingZCRXRQE rqes() {
        return IOURingZCRXRQ.nrqes(this.address());
    }

    @NativeType(value="void *")
    public long ring_ptr() {
        return IOURingZCRXRQ.nring_ptr(this.address());
    }

    public IOURingZCRXRQ khead(@NativeType(value="__u32 *") IntBuffer value) {
        IOURingZCRXRQ.nkhead(this.address(), value);
        return this;
    }

    public IOURingZCRXRQ ktail(@NativeType(value="__u32 *") IntBuffer value) {
        IOURingZCRXRQ.nktail(this.address(), value);
        return this;
    }

    public IOURingZCRXRQ rq_tail(@NativeType(value="__u32") int value) {
        IOURingZCRXRQ.nrq_tail(this.address(), value);
        return this;
    }

    public IOURingZCRXRQ ring_entries(@NativeType(value="unsigned") int value) {
        IOURingZCRXRQ.nring_entries(this.address(), value);
        return this;
    }

    public IOURingZCRXRQ rqes(@NativeType(value="struct io_uring_zcrx_rqe *") IOURingZCRXRQE value) {
        IOURingZCRXRQ.nrqes(this.address(), value);
        return this;
    }

    public IOURingZCRXRQ ring_ptr(@NativeType(value="void *") long value) {
        IOURingZCRXRQ.nring_ptr(this.address(), value);
        return this;
    }

    public IOURingZCRXRQ set(IntBuffer khead, IntBuffer ktail, int rq_tail, int ring_entries, IOURingZCRXRQE rqes, long ring_ptr) {
        this.khead(khead);
        this.ktail(ktail);
        this.rq_tail(rq_tail);
        this.ring_entries(ring_entries);
        this.rqes(rqes);
        this.ring_ptr(ring_ptr);
        return this;
    }

    public IOURingZCRXRQ set(IOURingZCRXRQ src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOURingZCRXRQ malloc() {
        return new IOURingZCRXRQ(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static IOURingZCRXRQ calloc() {
        return new IOURingZCRXRQ(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static IOURingZCRXRQ create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new IOURingZCRXRQ(MemoryUtil.memAddress(container), container);
    }

    public static IOURingZCRXRQ create(long address) {
        return new IOURingZCRXRQ(address, null);
    }

    public static @Nullable IOURingZCRXRQ createSafe(long address) {
        return address == 0L ? null : new IOURingZCRXRQ(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(IOURingZCRXRQ.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOURingZCRXRQ.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static IOURingZCRXRQ malloc(MemoryStack stack) {
        return new IOURingZCRXRQ(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static IOURingZCRXRQ calloc(MemoryStack stack) {
        return new IOURingZCRXRQ(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static IntBuffer nkhead(long struct, int capacity) {
        return MemoryUtil.memIntBuffer(MemoryUtil.memGetAddress(struct + (long)KHEAD), capacity);
    }

    public static IntBuffer nktail(long struct, int capacity) {
        return MemoryUtil.memIntBuffer(MemoryUtil.memGetAddress(struct + (long)KTAIL), capacity);
    }

    public static int nrq_tail(long struct) {
        return MemoryUtil.memGetInt(struct + (long)RQ_TAIL);
    }

    public static int nring_entries(long struct) {
        return MemoryUtil.memGetInt(struct + (long)RING_ENTRIES);
    }

    public static IOURingZCRXRQE nrqes(long struct) {
        return IOURingZCRXRQE.create(MemoryUtil.memGetAddress(struct + (long)RQES));
    }

    public static long nring_ptr(long struct) {
        return MemoryUtil.memGetAddress(struct + (long)RING_PTR);
    }

    public static void nkhead(long struct, IntBuffer value) {
        MemoryUtil.memPutAddress(struct + (long)KHEAD, MemoryUtil.memAddress(value));
    }

    public static void nktail(long struct, IntBuffer value) {
        MemoryUtil.memPutAddress(struct + (long)KTAIL, MemoryUtil.memAddress(value));
    }

    public static void nrq_tail(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)RQ_TAIL, value);
    }

    public static void nring_entries(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)RING_ENTRIES, value);
    }

    public static void nrqes(long struct, IOURingZCRXRQE value) {
        MemoryUtil.memPutAddress(struct + (long)RQES, value.address());
    }

    public static void nring_ptr(long struct, long value) {
        MemoryUtil.memPutAddress(struct + (long)RING_PTR, Checks.check(value));
    }

    public static void validate(long struct) {
        Checks.check(MemoryUtil.memGetAddress(struct + (long)KHEAD));
        Checks.check(MemoryUtil.memGetAddress(struct + (long)KTAIL));
        Checks.check(MemoryUtil.memGetAddress(struct + (long)RQES));
        Checks.check(MemoryUtil.memGetAddress(struct + (long)RING_PTR));
    }

    static {
        Struct.Layout layout = IOURingZCRXRQ.__struct(IOURingZCRXRQ.__member(POINTER_SIZE), IOURingZCRXRQ.__member(POINTER_SIZE), IOURingZCRXRQ.__member(4), IOURingZCRXRQ.__member(4), IOURingZCRXRQ.__member(POINTER_SIZE), IOURingZCRXRQ.__member(POINTER_SIZE));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        KHEAD = layout.offsetof(0);
        KTAIL = layout.offsetof(1);
        RQ_TAIL = layout.offsetof(2);
        RING_ENTRIES = layout.offsetof(3);
        RQES = layout.offsetof(4);
        RING_PTR = layout.offsetof(5);
    }

    public static class Buffer
    extends StructBuffer<IOURingZCRXRQ, Buffer>
    implements NativeResource {
        private static final IOURingZCRXRQ ELEMENT_FACTORY = IOURingZCRXRQ.create(-1L);

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
        protected IOURingZCRXRQ getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__u32 *")
        public IntBuffer khead(int capacity) {
            return IOURingZCRXRQ.nkhead(this.address(), capacity);
        }

        @NativeType(value="__u32 *")
        public IntBuffer ktail(int capacity) {
            return IOURingZCRXRQ.nktail(this.address(), capacity);
        }

        @NativeType(value="__u32")
        public int rq_tail() {
            return IOURingZCRXRQ.nrq_tail(this.address());
        }

        @NativeType(value="unsigned")
        public int ring_entries() {
            return IOURingZCRXRQ.nring_entries(this.address());
        }

        @NativeType(value="struct io_uring_zcrx_rqe *")
        public IOURingZCRXRQE rqes() {
            return IOURingZCRXRQ.nrqes(this.address());
        }

        @NativeType(value="void *")
        public long ring_ptr() {
            return IOURingZCRXRQ.nring_ptr(this.address());
        }

        public Buffer khead(@NativeType(value="__u32 *") IntBuffer value) {
            IOURingZCRXRQ.nkhead(this.address(), value);
            return this;
        }

        public Buffer ktail(@NativeType(value="__u32 *") IntBuffer value) {
            IOURingZCRXRQ.nktail(this.address(), value);
            return this;
        }

        public Buffer rq_tail(@NativeType(value="__u32") int value) {
            IOURingZCRXRQ.nrq_tail(this.address(), value);
            return this;
        }

        public Buffer ring_entries(@NativeType(value="unsigned") int value) {
            IOURingZCRXRQ.nring_entries(this.address(), value);
            return this;
        }

        public Buffer rqes(@NativeType(value="struct io_uring_zcrx_rqe *") IOURingZCRXRQE value) {
            IOURingZCRXRQ.nrqes(this.address(), value);
            return this;
        }

        public Buffer ring_ptr(@NativeType(value="void *") long value) {
            IOURingZCRXRQ.nring_ptr(this.address(), value);
            return this;
        }
    }
}

