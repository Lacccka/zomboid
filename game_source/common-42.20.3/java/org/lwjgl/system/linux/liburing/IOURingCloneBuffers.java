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

@NativeType(value="struct io_uring_clone_buffers")
public class IOURingCloneBuffers
extends Struct<IOURingCloneBuffers>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int SRC_FD;
    public static final int FLAGS;
    public static final int SRC_OFF;
    public static final int DST_OFF;
    public static final int NR;
    public static final int PAD;

    protected IOURingCloneBuffers(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected IOURingCloneBuffers create(long address, @Nullable ByteBuffer container) {
        return new IOURingCloneBuffers(address, container);
    }

    public IOURingCloneBuffers(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOURingCloneBuffers.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__u32")
    public int src_fd() {
        return IOURingCloneBuffers.nsrc_fd(this.address());
    }

    @NativeType(value="__u32")
    public int flags() {
        return IOURingCloneBuffers.nflags(this.address());
    }

    @NativeType(value="__u32")
    public int src_off() {
        return IOURingCloneBuffers.nsrc_off(this.address());
    }

    @NativeType(value="__u32")
    public int dst_off() {
        return IOURingCloneBuffers.ndst_off(this.address());
    }

    @NativeType(value="__u32")
    public int nr() {
        return IOURingCloneBuffers.nnr(this.address());
    }

    public IOURingCloneBuffers src_fd(@NativeType(value="__u32") int value) {
        IOURingCloneBuffers.nsrc_fd(this.address(), value);
        return this;
    }

    public IOURingCloneBuffers flags(@NativeType(value="__u32") int value) {
        IOURingCloneBuffers.nflags(this.address(), value);
        return this;
    }

    public IOURingCloneBuffers src_off(@NativeType(value="__u32") int value) {
        IOURingCloneBuffers.nsrc_off(this.address(), value);
        return this;
    }

    public IOURingCloneBuffers dst_off(@NativeType(value="__u32") int value) {
        IOURingCloneBuffers.ndst_off(this.address(), value);
        return this;
    }

    public IOURingCloneBuffers nr(@NativeType(value="__u32") int value) {
        IOURingCloneBuffers.nnr(this.address(), value);
        return this;
    }

    public IOURingCloneBuffers set(int src_fd, int flags, int src_off, int dst_off, int nr) {
        this.src_fd(src_fd);
        this.flags(flags);
        this.src_off(src_off);
        this.dst_off(dst_off);
        this.nr(nr);
        return this;
    }

    public IOURingCloneBuffers set(IOURingCloneBuffers src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOURingCloneBuffers malloc() {
        return new IOURingCloneBuffers(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static IOURingCloneBuffers calloc() {
        return new IOURingCloneBuffers(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static IOURingCloneBuffers create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new IOURingCloneBuffers(MemoryUtil.memAddress(container), container);
    }

    public static IOURingCloneBuffers create(long address) {
        return new IOURingCloneBuffers(address, null);
    }

    public static @Nullable IOURingCloneBuffers createSafe(long address) {
        return address == 0L ? null : new IOURingCloneBuffers(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(IOURingCloneBuffers.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOURingCloneBuffers.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static IOURingCloneBuffers malloc(MemoryStack stack) {
        return new IOURingCloneBuffers(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static IOURingCloneBuffers calloc(MemoryStack stack) {
        return new IOURingCloneBuffers(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int nsrc_fd(long struct) {
        return MemoryUtil.memGetInt(struct + (long)SRC_FD);
    }

    public static int nflags(long struct) {
        return MemoryUtil.memGetInt(struct + (long)FLAGS);
    }

    public static int nsrc_off(long struct) {
        return MemoryUtil.memGetInt(struct + (long)SRC_OFF);
    }

    public static int ndst_off(long struct) {
        return MemoryUtil.memGetInt(struct + (long)DST_OFF);
    }

    public static int nnr(long struct) {
        return MemoryUtil.memGetInt(struct + (long)NR);
    }

    public static IntBuffer npad(long struct) {
        return MemoryUtil.memIntBuffer(struct + (long)PAD, 3);
    }

    public static int npad(long struct, int index) {
        return MemoryUtil.memGetInt(struct + (long)PAD + Checks.check(index, 3) * 4L);
    }

    public static void nsrc_fd(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)SRC_FD, value);
    }

    public static void nflags(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)FLAGS, value);
    }

    public static void nsrc_off(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)SRC_OFF, value);
    }

    public static void ndst_off(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)DST_OFF, value);
    }

    public static void nnr(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)NR, value);
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

    static {
        Struct.Layout layout = IOURingCloneBuffers.__struct(IOURingCloneBuffers.__member(4), IOURingCloneBuffers.__member(4), IOURingCloneBuffers.__member(4), IOURingCloneBuffers.__member(4), IOURingCloneBuffers.__member(4), IOURingCloneBuffers.__array(4, 3));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        SRC_FD = layout.offsetof(0);
        FLAGS = layout.offsetof(1);
        SRC_OFF = layout.offsetof(2);
        DST_OFF = layout.offsetof(3);
        NR = layout.offsetof(4);
        PAD = layout.offsetof(5);
    }

    public static class Buffer
    extends StructBuffer<IOURingCloneBuffers, Buffer>
    implements NativeResource {
        private static final IOURingCloneBuffers ELEMENT_FACTORY = IOURingCloneBuffers.create(-1L);

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
        protected IOURingCloneBuffers getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__u32")
        public int src_fd() {
            return IOURingCloneBuffers.nsrc_fd(this.address());
        }

        @NativeType(value="__u32")
        public int flags() {
            return IOURingCloneBuffers.nflags(this.address());
        }

        @NativeType(value="__u32")
        public int src_off() {
            return IOURingCloneBuffers.nsrc_off(this.address());
        }

        @NativeType(value="__u32")
        public int dst_off() {
            return IOURingCloneBuffers.ndst_off(this.address());
        }

        @NativeType(value="__u32")
        public int nr() {
            return IOURingCloneBuffers.nnr(this.address());
        }

        public Buffer src_fd(@NativeType(value="__u32") int value) {
            IOURingCloneBuffers.nsrc_fd(this.address(), value);
            return this;
        }

        public Buffer flags(@NativeType(value="__u32") int value) {
            IOURingCloneBuffers.nflags(this.address(), value);
            return this;
        }

        public Buffer src_off(@NativeType(value="__u32") int value) {
            IOURingCloneBuffers.nsrc_off(this.address(), value);
            return this;
        }

        public Buffer dst_off(@NativeType(value="__u32") int value) {
            IOURingCloneBuffers.ndst_off(this.address(), value);
            return this;
        }

        public Buffer nr(@NativeType(value="__u32") int value) {
            IOURingCloneBuffers.nnr(this.address(), value);
            return this;
        }
    }
}

