/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.linux;

import java.nio.ByteBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType(value="struct cmsghdr")
public class CMsghdr
extends Struct<CMsghdr>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int CMSG_LEN;
    public static final int CMSG_LEVEL;
    public static final int CMSG_TYPE;
    public static final int CMSG_DATA;

    protected CMsghdr(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected CMsghdr create(long address, @Nullable ByteBuffer container) {
        return new CMsghdr(address, container);
    }

    public CMsghdr(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), CMsghdr.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="socklen_t")
    public int cmsg_len() {
        return CMsghdr.ncmsg_len(this.address());
    }

    public int cmsg_level() {
        return CMsghdr.ncmsg_level(this.address());
    }

    public int cmsg_type() {
        return CMsghdr.ncmsg_type(this.address());
    }

    @NativeType(value="char[0]")
    public ByteBuffer cmsg_data() {
        return CMsghdr.ncmsg_data(this.address());
    }

    @NativeType(value="char")
    public byte cmsg_data(int index) {
        return CMsghdr.ncmsg_data(this.address(), index);
    }

    public CMsghdr cmsg_len(@NativeType(value="socklen_t") int value) {
        CMsghdr.ncmsg_len(this.address(), value);
        return this;
    }

    public CMsghdr cmsg_level(int value) {
        CMsghdr.ncmsg_level(this.address(), value);
        return this;
    }

    public CMsghdr cmsg_type(int value) {
        CMsghdr.ncmsg_type(this.address(), value);
        return this;
    }

    public CMsghdr cmsg_data(@NativeType(value="char[0]") ByteBuffer value) {
        CMsghdr.ncmsg_data(this.address(), value);
        return this;
    }

    public CMsghdr cmsg_data(int index, @NativeType(value="char") byte value) {
        CMsghdr.ncmsg_data(this.address(), index, value);
        return this;
    }

    public CMsghdr set(int cmsg_len, int cmsg_level, int cmsg_type, ByteBuffer cmsg_data) {
        this.cmsg_len(cmsg_len);
        this.cmsg_level(cmsg_level);
        this.cmsg_type(cmsg_type);
        this.cmsg_data(cmsg_data);
        return this;
    }

    public CMsghdr set(CMsghdr src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static CMsghdr malloc() {
        return new CMsghdr(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static CMsghdr calloc() {
        return new CMsghdr(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static CMsghdr create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new CMsghdr(MemoryUtil.memAddress(container), container);
    }

    public static CMsghdr create(long address) {
        return new CMsghdr(address, null);
    }

    public static @Nullable CMsghdr createSafe(long address) {
        return address == 0L ? null : new CMsghdr(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(CMsghdr.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = CMsghdr.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static CMsghdr malloc(MemoryStack stack) {
        return new CMsghdr(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static CMsghdr calloc(MemoryStack stack) {
        return new CMsghdr(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int ncmsg_len(long struct) {
        return MemoryUtil.memGetInt(struct + (long)CMSG_LEN);
    }

    public static int ncmsg_level(long struct) {
        return MemoryUtil.memGetInt(struct + (long)CMSG_LEVEL);
    }

    public static int ncmsg_type(long struct) {
        return MemoryUtil.memGetInt(struct + (long)CMSG_TYPE);
    }

    public static ByteBuffer ncmsg_data(long struct) {
        return MemoryUtil.memByteBuffer(struct + (long)CMSG_DATA, 0);
    }

    public static byte ncmsg_data(long struct, int index) {
        return MemoryUtil.memGetByte(struct + (long)CMSG_DATA + Checks.check(index, 0) * 1L);
    }

    public static void ncmsg_len(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)CMSG_LEN, value);
    }

    public static void ncmsg_level(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)CMSG_LEVEL, value);
    }

    public static void ncmsg_type(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)CMSG_TYPE, value);
    }

    public static void ncmsg_data(long struct, ByteBuffer value) {
        if (Checks.CHECKS) {
            Checks.checkGT(value, 0);
        }
        MemoryUtil.memCopy(MemoryUtil.memAddress(value), struct + (long)CMSG_DATA, value.remaining() * 1);
    }

    public static void ncmsg_data(long struct, int index, byte value) {
        MemoryUtil.memPutByte(struct + (long)CMSG_DATA + Checks.check(index, 0) * 1L, value);
    }

    static {
        Struct.Layout layout = CMsghdr.__struct(CMsghdr.__member(4), CMsghdr.__member(4), CMsghdr.__member(4), CMsghdr.__array(1, 0));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        CMSG_LEN = layout.offsetof(0);
        CMSG_LEVEL = layout.offsetof(1);
        CMSG_TYPE = layout.offsetof(2);
        CMSG_DATA = layout.offsetof(3);
    }

    public static class Buffer
    extends StructBuffer<CMsghdr, Buffer>
    implements NativeResource {
        private static final CMsghdr ELEMENT_FACTORY = CMsghdr.create(-1L);

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
        protected CMsghdr getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="socklen_t")
        public int cmsg_len() {
            return CMsghdr.ncmsg_len(this.address());
        }

        public int cmsg_level() {
            return CMsghdr.ncmsg_level(this.address());
        }

        public int cmsg_type() {
            return CMsghdr.ncmsg_type(this.address());
        }

        @NativeType(value="char[0]")
        public ByteBuffer cmsg_data() {
            return CMsghdr.ncmsg_data(this.address());
        }

        @NativeType(value="char")
        public byte cmsg_data(int index) {
            return CMsghdr.ncmsg_data(this.address(), index);
        }

        public Buffer cmsg_len(@NativeType(value="socklen_t") int value) {
            CMsghdr.ncmsg_len(this.address(), value);
            return this;
        }

        public Buffer cmsg_level(int value) {
            CMsghdr.ncmsg_level(this.address(), value);
            return this;
        }

        public Buffer cmsg_type(int value) {
            CMsghdr.ncmsg_type(this.address(), value);
            return this;
        }

        public Buffer cmsg_data(@NativeType(value="char[0]") ByteBuffer value) {
            CMsghdr.ncmsg_data(this.address(), value);
            return this;
        }

        public Buffer cmsg_data(int index, @NativeType(value="char") byte value) {
            CMsghdr.ncmsg_data(this.address(), index, value);
            return this;
        }
    }
}

