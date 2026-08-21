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

@NativeType(value="struct io_uring_restriction")
public class IOURingRestriction
extends Struct<IOURingRestriction>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int OPCODE;
    public static final int REGISTER_OP;
    public static final int SQE_OP;
    public static final int SQE_FLAGS;
    public static final int RESV;
    public static final int RESV2;

    protected IOURingRestriction(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected IOURingRestriction create(long address, @Nullable ByteBuffer container) {
        return new IOURingRestriction(address, container);
    }

    public IOURingRestriction(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOURingRestriction.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__u16")
    public short opcode() {
        return IOURingRestriction.nopcode(this.address());
    }

    @NativeType(value="__u8")
    public byte register_op() {
        return IOURingRestriction.nregister_op(this.address());
    }

    @NativeType(value="__u8")
    public byte sqe_op() {
        return IOURingRestriction.nsqe_op(this.address());
    }

    @NativeType(value="__u8")
    public byte sqe_flags() {
        return IOURingRestriction.nsqe_flags(this.address());
    }

    public IOURingRestriction opcode(@NativeType(value="__u16") short value) {
        IOURingRestriction.nopcode(this.address(), value);
        return this;
    }

    public IOURingRestriction register_op(@NativeType(value="__u8") byte value) {
        IOURingRestriction.nregister_op(this.address(), value);
        return this;
    }

    public IOURingRestriction sqe_op(@NativeType(value="__u8") byte value) {
        IOURingRestriction.nsqe_op(this.address(), value);
        return this;
    }

    public IOURingRestriction sqe_flags(@NativeType(value="__u8") byte value) {
        IOURingRestriction.nsqe_flags(this.address(), value);
        return this;
    }

    public IOURingRestriction set(IOURingRestriction src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOURingRestriction malloc() {
        return new IOURingRestriction(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static IOURingRestriction calloc() {
        return new IOURingRestriction(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static IOURingRestriction create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new IOURingRestriction(MemoryUtil.memAddress(container), container);
    }

    public static IOURingRestriction create(long address) {
        return new IOURingRestriction(address, null);
    }

    public static @Nullable IOURingRestriction createSafe(long address) {
        return address == 0L ? null : new IOURingRestriction(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(IOURingRestriction.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOURingRestriction.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static IOURingRestriction malloc(MemoryStack stack) {
        return new IOURingRestriction(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static IOURingRestriction calloc(MemoryStack stack) {
        return new IOURingRestriction(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static short nopcode(long struct) {
        return MemoryUtil.memGetShort(struct + (long)OPCODE);
    }

    public static byte nregister_op(long struct) {
        return MemoryUtil.memGetByte(struct + (long)REGISTER_OP);
    }

    public static byte nsqe_op(long struct) {
        return MemoryUtil.memGetByte(struct + (long)SQE_OP);
    }

    public static byte nsqe_flags(long struct) {
        return MemoryUtil.memGetByte(struct + (long)SQE_FLAGS);
    }

    public static byte nresv(long struct) {
        return MemoryUtil.memGetByte(struct + (long)RESV);
    }

    public static IntBuffer nresv2(long struct) {
        return MemoryUtil.memIntBuffer(struct + (long)RESV2, 3);
    }

    public static int nresv2(long struct, int index) {
        return MemoryUtil.memGetInt(struct + (long)RESV2 + Checks.check(index, 3) * 4L);
    }

    public static void nopcode(long struct, short value) {
        MemoryUtil.memPutShort(struct + (long)OPCODE, value);
    }

    public static void nregister_op(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)REGISTER_OP, value);
    }

    public static void nsqe_op(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)SQE_OP, value);
    }

    public static void nsqe_flags(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)SQE_FLAGS, value);
    }

    public static void nresv(long struct, byte value) {
        MemoryUtil.memPutByte(struct + (long)RESV, value);
    }

    public static void nresv2(long struct, IntBuffer value) {
        if (Checks.CHECKS) {
            Checks.checkGT(value, 3);
        }
        MemoryUtil.memCopy(MemoryUtil.memAddress(value), struct + (long)RESV2, value.remaining() * 4);
    }

    public static void nresv2(long struct, int index, int value) {
        MemoryUtil.memPutInt(struct + (long)RESV2 + Checks.check(index, 3) * 4L, value);
    }

    static {
        Struct.Layout layout = IOURingRestriction.__struct(IOURingRestriction.__member(2), IOURingRestriction.__union(IOURingRestriction.__member(1), IOURingRestriction.__member(1), IOURingRestriction.__member(1)), IOURingRestriction.__member(1), IOURingRestriction.__array(4, 3));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        OPCODE = layout.offsetof(0);
        REGISTER_OP = layout.offsetof(2);
        SQE_OP = layout.offsetof(3);
        SQE_FLAGS = layout.offsetof(4);
        RESV = layout.offsetof(5);
        RESV2 = layout.offsetof(6);
    }

    public static class Buffer
    extends StructBuffer<IOURingRestriction, Buffer>
    implements NativeResource {
        private static final IOURingRestriction ELEMENT_FACTORY = IOURingRestriction.create(-1L);

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
        protected IOURingRestriction getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__u16")
        public short opcode() {
            return IOURingRestriction.nopcode(this.address());
        }

        @NativeType(value="__u8")
        public byte register_op() {
            return IOURingRestriction.nregister_op(this.address());
        }

        @NativeType(value="__u8")
        public byte sqe_op() {
            return IOURingRestriction.nsqe_op(this.address());
        }

        @NativeType(value="__u8")
        public byte sqe_flags() {
            return IOURingRestriction.nsqe_flags(this.address());
        }

        public Buffer opcode(@NativeType(value="__u16") short value) {
            IOURingRestriction.nopcode(this.address(), value);
            return this;
        }

        public Buffer register_op(@NativeType(value="__u8") byte value) {
            IOURingRestriction.nregister_op(this.address(), value);
            return this;
        }

        public Buffer sqe_op(@NativeType(value="__u8") byte value) {
            IOURingRestriction.nsqe_op(this.address(), value);
            return this;
        }

        public Buffer sqe_flags(@NativeType(value="__u8") byte value) {
            IOURingRestriction.nsqe_flags(this.address(), value);
            return this;
        }
    }
}

