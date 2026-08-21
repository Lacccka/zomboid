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

@NativeType(value="struct io_uring_attr_pi")
public class IOURingAttrPI
extends Struct<IOURingAttrPI>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int FLAGS;
    public static final int APP_TAG;
    public static final int LEN;
    public static final int ADDR;
    public static final int SEED;
    public static final int RSVD;

    protected IOURingAttrPI(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected IOURingAttrPI create(long address, @Nullable ByteBuffer container) {
        return new IOURingAttrPI(address, container);
    }

    public IOURingAttrPI(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOURingAttrPI.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__u16")
    public short flags() {
        return IOURingAttrPI.nflags(this.address());
    }

    @NativeType(value="__u16")
    public short app_tag() {
        return IOURingAttrPI.napp_tag(this.address());
    }

    @NativeType(value="__u32")
    public int len() {
        return IOURingAttrPI.nlen(this.address());
    }

    @NativeType(value="__u64")
    public long addr() {
        return IOURingAttrPI.naddr(this.address());
    }

    @NativeType(value="__u64")
    public long seed() {
        return IOURingAttrPI.nseed(this.address());
    }

    @NativeType(value="__u64")
    public long rsvd() {
        return IOURingAttrPI.nrsvd(this.address());
    }

    public IOURingAttrPI flags(@NativeType(value="__u16") short value) {
        IOURingAttrPI.nflags(this.address(), value);
        return this;
    }

    public IOURingAttrPI app_tag(@NativeType(value="__u16") short value) {
        IOURingAttrPI.napp_tag(this.address(), value);
        return this;
    }

    public IOURingAttrPI len(@NativeType(value="__u32") int value) {
        IOURingAttrPI.nlen(this.address(), value);
        return this;
    }

    public IOURingAttrPI addr(@NativeType(value="__u64") long value) {
        IOURingAttrPI.naddr(this.address(), value);
        return this;
    }

    public IOURingAttrPI seed(@NativeType(value="__u64") long value) {
        IOURingAttrPI.nseed(this.address(), value);
        return this;
    }

    public IOURingAttrPI rsvd(@NativeType(value="__u64") long value) {
        IOURingAttrPI.nrsvd(this.address(), value);
        return this;
    }

    public IOURingAttrPI set(short flags, short app_tag, int len, long addr, long seed, long rsvd) {
        this.flags(flags);
        this.app_tag(app_tag);
        this.len(len);
        this.addr(addr);
        this.seed(seed);
        this.rsvd(rsvd);
        return this;
    }

    public IOURingAttrPI set(IOURingAttrPI src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOURingAttrPI malloc() {
        return new IOURingAttrPI(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static IOURingAttrPI calloc() {
        return new IOURingAttrPI(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static IOURingAttrPI create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new IOURingAttrPI(MemoryUtil.memAddress(container), container);
    }

    public static IOURingAttrPI create(long address) {
        return new IOURingAttrPI(address, null);
    }

    public static @Nullable IOURingAttrPI createSafe(long address) {
        return address == 0L ? null : new IOURingAttrPI(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(IOURingAttrPI.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOURingAttrPI.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static IOURingAttrPI malloc(MemoryStack stack) {
        return new IOURingAttrPI(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static IOURingAttrPI calloc(MemoryStack stack) {
        return new IOURingAttrPI(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static short nflags(long struct) {
        return MemoryUtil.memGetShort(struct + (long)FLAGS);
    }

    public static short napp_tag(long struct) {
        return MemoryUtil.memGetShort(struct + (long)APP_TAG);
    }

    public static int nlen(long struct) {
        return MemoryUtil.memGetInt(struct + (long)LEN);
    }

    public static long naddr(long struct) {
        return MemoryUtil.memGetLong(struct + (long)ADDR);
    }

    public static long nseed(long struct) {
        return MemoryUtil.memGetLong(struct + (long)SEED);
    }

    public static long nrsvd(long struct) {
        return MemoryUtil.memGetLong(struct + (long)RSVD);
    }

    public static void nflags(long struct, short value) {
        MemoryUtil.memPutShort(struct + (long)FLAGS, value);
    }

    public static void napp_tag(long struct, short value) {
        MemoryUtil.memPutShort(struct + (long)APP_TAG, value);
    }

    public static void nlen(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)LEN, value);
    }

    public static void naddr(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)ADDR, value);
    }

    public static void nseed(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)SEED, value);
    }

    public static void nrsvd(long struct, long value) {
        MemoryUtil.memPutLong(struct + (long)RSVD, value);
    }

    static {
        Struct.Layout layout = IOURingAttrPI.__struct(IOURingAttrPI.__member(2), IOURingAttrPI.__member(2), IOURingAttrPI.__member(4), IOURingAttrPI.__member(8), IOURingAttrPI.__member(8), IOURingAttrPI.__member(8));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        FLAGS = layout.offsetof(0);
        APP_TAG = layout.offsetof(1);
        LEN = layout.offsetof(2);
        ADDR = layout.offsetof(3);
        SEED = layout.offsetof(4);
        RSVD = layout.offsetof(5);
    }

    public static class Buffer
    extends StructBuffer<IOURingAttrPI, Buffer>
    implements NativeResource {
        private static final IOURingAttrPI ELEMENT_FACTORY = IOURingAttrPI.create(-1L);

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
        protected IOURingAttrPI getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__u16")
        public short flags() {
            return IOURingAttrPI.nflags(this.address());
        }

        @NativeType(value="__u16")
        public short app_tag() {
            return IOURingAttrPI.napp_tag(this.address());
        }

        @NativeType(value="__u32")
        public int len() {
            return IOURingAttrPI.nlen(this.address());
        }

        @NativeType(value="__u64")
        public long addr() {
            return IOURingAttrPI.naddr(this.address());
        }

        @NativeType(value="__u64")
        public long seed() {
            return IOURingAttrPI.nseed(this.address());
        }

        @NativeType(value="__u64")
        public long rsvd() {
            return IOURingAttrPI.nrsvd(this.address());
        }

        public Buffer flags(@NativeType(value="__u16") short value) {
            IOURingAttrPI.nflags(this.address(), value);
            return this;
        }

        public Buffer app_tag(@NativeType(value="__u16") short value) {
            IOURingAttrPI.napp_tag(this.address(), value);
            return this;
        }

        public Buffer len(@NativeType(value="__u32") int value) {
            IOURingAttrPI.nlen(this.address(), value);
            return this;
        }

        public Buffer addr(@NativeType(value="__u64") long value) {
            IOURingAttrPI.naddr(this.address(), value);
            return this;
        }

        public Buffer seed(@NativeType(value="__u64") long value) {
            IOURingAttrPI.nseed(this.address(), value);
            return this;
        }

        public Buffer rsvd(@NativeType(value="__u64") long value) {
            IOURingAttrPI.nrsvd(this.address(), value);
            return this;
        }
    }
}

