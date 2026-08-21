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
import org.lwjgl.system.linux.IOVec;

@NativeType(value="struct msghdr")
public class Msghdr
extends Struct<Msghdr>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int MSG_NAME;
    public static final int MSG_NAMELEN;
    public static final int MSG_IOV;
    public static final int MSG_IOVLEN;
    public static final int MSG_CONTROL;
    public static final int MSG_CONTROLLEN;
    public static final int MSG_FLAGS;

    protected Msghdr(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected Msghdr create(long address, @Nullable ByteBuffer container) {
        return new Msghdr(address, container);
    }

    public Msghdr(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), Msghdr.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="void *")
    public ByteBuffer msg_name() {
        return Msghdr.nmsg_name(this.address());
    }

    @NativeType(value="socklen_t")
    public int msg_namelen() {
        return Msghdr.nmsg_namelen(this.address());
    }

    @NativeType(value="struct iovec *")
    public IOVec.Buffer msg_iov() {
        return Msghdr.nmsg_iov(this.address());
    }

    @NativeType(value="size_t")
    public long msg_iovlen() {
        return Msghdr.nmsg_iovlen(this.address());
    }

    @NativeType(value="void *")
    public ByteBuffer msg_control() {
        return Msghdr.nmsg_control(this.address());
    }

    @NativeType(value="size_t")
    public long msg_controllen() {
        return Msghdr.nmsg_controllen(this.address());
    }

    public int msg_flags() {
        return Msghdr.nmsg_flags(this.address());
    }

    public Msghdr msg_name(@NativeType(value="void *") ByteBuffer value) {
        Msghdr.nmsg_name(this.address(), value);
        return this;
    }

    public Msghdr msg_iov(@NativeType(value="struct iovec *") IOVec.Buffer value) {
        Msghdr.nmsg_iov(this.address(), value);
        return this;
    }

    public Msghdr msg_control(@NativeType(value="void *") ByteBuffer value) {
        Msghdr.nmsg_control(this.address(), value);
        return this;
    }

    public Msghdr msg_flags(int value) {
        Msghdr.nmsg_flags(this.address(), value);
        return this;
    }

    public Msghdr set(ByteBuffer msg_name, IOVec.Buffer msg_iov, ByteBuffer msg_control, int msg_flags) {
        this.msg_name(msg_name);
        this.msg_iov(msg_iov);
        this.msg_control(msg_control);
        this.msg_flags(msg_flags);
        return this;
    }

    public Msghdr set(Msghdr src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static Msghdr malloc() {
        return new Msghdr(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static Msghdr calloc() {
        return new Msghdr(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static Msghdr create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new Msghdr(MemoryUtil.memAddress(container), container);
    }

    public static Msghdr create(long address) {
        return new Msghdr(address, null);
    }

    public static @Nullable Msghdr createSafe(long address) {
        return address == 0L ? null : new Msghdr(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(Msghdr.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = Msghdr.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static Msghdr malloc(MemoryStack stack) {
        return new Msghdr(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static Msghdr calloc(MemoryStack stack) {
        return new Msghdr(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static ByteBuffer nmsg_name(long struct) {
        return MemoryUtil.memByteBuffer(MemoryUtil.memGetAddress(struct + (long)MSG_NAME), Msghdr.nmsg_namelen(struct));
    }

    public static int nmsg_namelen(long struct) {
        return MemoryUtil.memGetInt(struct + (long)MSG_NAMELEN);
    }

    public static IOVec.Buffer nmsg_iov(long struct) {
        return IOVec.create(MemoryUtil.memGetAddress(struct + (long)MSG_IOV), (int)Msghdr.nmsg_iovlen(struct));
    }

    public static long nmsg_iovlen(long struct) {
        return MemoryUtil.memGetAddress(struct + (long)MSG_IOVLEN);
    }

    public static ByteBuffer nmsg_control(long struct) {
        return MemoryUtil.memByteBuffer(MemoryUtil.memGetAddress(struct + (long)MSG_CONTROL), (int)Msghdr.nmsg_controllen(struct));
    }

    public static long nmsg_controllen(long struct) {
        return MemoryUtil.memGetAddress(struct + (long)MSG_CONTROLLEN);
    }

    public static int nmsg_flags(long struct) {
        return MemoryUtil.memGetInt(struct + (long)MSG_FLAGS);
    }

    public static void nmsg_name(long struct, ByteBuffer value) {
        MemoryUtil.memPutAddress(struct + (long)MSG_NAME, MemoryUtil.memAddress(value));
        Msghdr.nmsg_namelen(struct, value.remaining());
    }

    public static void nmsg_namelen(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)MSG_NAMELEN, value);
    }

    public static void nmsg_iov(long struct, IOVec.Buffer value) {
        MemoryUtil.memPutAddress(struct + (long)MSG_IOV, value.address());
        Msghdr.nmsg_iovlen(struct, value.remaining());
    }

    public static void nmsg_iovlen(long struct, long value) {
        MemoryUtil.memPutAddress(struct + (long)MSG_IOVLEN, value);
    }

    public static void nmsg_control(long struct, ByteBuffer value) {
        MemoryUtil.memPutAddress(struct + (long)MSG_CONTROL, MemoryUtil.memAddress(value));
        Msghdr.nmsg_controllen(struct, value.remaining());
    }

    public static void nmsg_controllen(long struct, long value) {
        MemoryUtil.memPutAddress(struct + (long)MSG_CONTROLLEN, value);
    }

    public static void nmsg_flags(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)MSG_FLAGS, value);
    }

    public static void validate(long struct) {
        Checks.check(MemoryUtil.memGetAddress(struct + (long)MSG_NAME));
        Checks.check(MemoryUtil.memGetAddress(struct + (long)MSG_IOV));
        Checks.check(MemoryUtil.memGetAddress(struct + (long)MSG_CONTROL));
    }

    static {
        Struct.Layout layout = Msghdr.__struct(Msghdr.__member(POINTER_SIZE), Msghdr.__member(4), Msghdr.__member(POINTER_SIZE), Msghdr.__member(POINTER_SIZE), Msghdr.__member(POINTER_SIZE), Msghdr.__member(POINTER_SIZE), Msghdr.__member(4));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        MSG_NAME = layout.offsetof(0);
        MSG_NAMELEN = layout.offsetof(1);
        MSG_IOV = layout.offsetof(2);
        MSG_IOVLEN = layout.offsetof(3);
        MSG_CONTROL = layout.offsetof(4);
        MSG_CONTROLLEN = layout.offsetof(5);
        MSG_FLAGS = layout.offsetof(6);
    }

    public static class Buffer
    extends StructBuffer<Msghdr, Buffer>
    implements NativeResource {
        private static final Msghdr ELEMENT_FACTORY = Msghdr.create(-1L);

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
        protected Msghdr getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="void *")
        public ByteBuffer msg_name() {
            return Msghdr.nmsg_name(this.address());
        }

        @NativeType(value="socklen_t")
        public int msg_namelen() {
            return Msghdr.nmsg_namelen(this.address());
        }

        @NativeType(value="struct iovec *")
        public IOVec.Buffer msg_iov() {
            return Msghdr.nmsg_iov(this.address());
        }

        @NativeType(value="size_t")
        public long msg_iovlen() {
            return Msghdr.nmsg_iovlen(this.address());
        }

        @NativeType(value="void *")
        public ByteBuffer msg_control() {
            return Msghdr.nmsg_control(this.address());
        }

        @NativeType(value="size_t")
        public long msg_controllen() {
            return Msghdr.nmsg_controllen(this.address());
        }

        public int msg_flags() {
            return Msghdr.nmsg_flags(this.address());
        }

        public Buffer msg_name(@NativeType(value="void *") ByteBuffer value) {
            Msghdr.nmsg_name(this.address(), value);
            return this;
        }

        public Buffer msg_iov(@NativeType(value="struct iovec *") IOVec.Buffer value) {
            Msghdr.nmsg_iov(this.address(), value);
            return this;
        }

        public Buffer msg_control(@NativeType(value="void *") ByteBuffer value) {
            Msghdr.nmsg_control(this.address(), value);
            return this;
        }

        public Buffer msg_flags(int value) {
            Msghdr.nmsg_flags(this.address(), value);
            return this;
        }
    }
}

