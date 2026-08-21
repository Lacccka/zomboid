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

@NativeType(value="struct io_uring_clock_register")
public class IOURingClockRegister
extends Struct<IOURingClockRegister>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int CLOCKID;
    public static final int __RESV;

    protected IOURingClockRegister(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected IOURingClockRegister create(long address, @Nullable ByteBuffer container) {
        return new IOURingClockRegister(address, container);
    }

    public IOURingClockRegister(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOURingClockRegister.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__u32")
    public int clockid() {
        return IOURingClockRegister.nclockid(this.address());
    }

    public IOURingClockRegister clockid(@NativeType(value="__u32") int value) {
        IOURingClockRegister.nclockid(this.address(), value);
        return this;
    }

    public IOURingClockRegister set(IOURingClockRegister src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOURingClockRegister malloc() {
        return new IOURingClockRegister(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static IOURingClockRegister calloc() {
        return new IOURingClockRegister(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static IOURingClockRegister create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new IOURingClockRegister(MemoryUtil.memAddress(container), container);
    }

    public static IOURingClockRegister create(long address) {
        return new IOURingClockRegister(address, null);
    }

    public static @Nullable IOURingClockRegister createSafe(long address) {
        return address == 0L ? null : new IOURingClockRegister(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(IOURingClockRegister.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOURingClockRegister.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static IOURingClockRegister malloc(MemoryStack stack) {
        return new IOURingClockRegister(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static IOURingClockRegister calloc(MemoryStack stack) {
        return new IOURingClockRegister(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int nclockid(long struct) {
        return MemoryUtil.memGetInt(struct + (long)CLOCKID);
    }

    public static IntBuffer n__resv(long struct) {
        return MemoryUtil.memIntBuffer(struct + (long)__RESV, 3);
    }

    public static int n__resv(long struct, int index) {
        return MemoryUtil.memGetInt(struct + (long)__RESV + Checks.check(index, 3) * 4L);
    }

    public static void nclockid(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)CLOCKID, value);
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
        Struct.Layout layout = IOURingClockRegister.__struct(IOURingClockRegister.__member(4), IOURingClockRegister.__array(4, 3));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        CLOCKID = layout.offsetof(0);
        __RESV = layout.offsetof(1);
    }

    public static class Buffer
    extends StructBuffer<IOURingClockRegister, Buffer>
    implements NativeResource {
        private static final IOURingClockRegister ELEMENT_FACTORY = IOURingClockRegister.create(-1L);

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
        protected IOURingClockRegister getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__u32")
        public int clockid() {
            return IOURingClockRegister.nclockid(this.address());
        }

        public Buffer clockid(@NativeType(value="__u32") int value) {
            IOURingClockRegister.nclockid(this.address(), value);
            return this;
        }
    }
}

