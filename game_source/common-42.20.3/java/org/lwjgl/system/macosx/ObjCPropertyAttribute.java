/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.macosx;

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

@NativeType(value="struct objc_property_attribute_t")
public class ObjCPropertyAttribute
extends Struct<ObjCPropertyAttribute>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int NAME;
    public static final int VALUE;

    protected ObjCPropertyAttribute(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected ObjCPropertyAttribute create(long address, @Nullable ByteBuffer container) {
        return new ObjCPropertyAttribute(address, container);
    }

    public ObjCPropertyAttribute(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), ObjCPropertyAttribute.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="char *")
    public ByteBuffer name() {
        return ObjCPropertyAttribute.nname(this.address());
    }

    @NativeType(value="char *")
    public String nameString() {
        return ObjCPropertyAttribute.nnameString(this.address());
    }

    @NativeType(value="char *")
    public ByteBuffer value() {
        return ObjCPropertyAttribute.nvalue(this.address());
    }

    @NativeType(value="char *")
    public String valueString() {
        return ObjCPropertyAttribute.nvalueString(this.address());
    }

    public ObjCPropertyAttribute name(@NativeType(value="char *") ByteBuffer value) {
        ObjCPropertyAttribute.nname(this.address(), value);
        return this;
    }

    public ObjCPropertyAttribute value(@NativeType(value="char *") ByteBuffer value) {
        ObjCPropertyAttribute.nvalue(this.address(), value);
        return this;
    }

    public ObjCPropertyAttribute set(ByteBuffer name, ByteBuffer value) {
        this.name(name);
        this.value(value);
        return this;
    }

    public ObjCPropertyAttribute set(ObjCPropertyAttribute src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static ObjCPropertyAttribute malloc() {
        return new ObjCPropertyAttribute(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static ObjCPropertyAttribute calloc() {
        return new ObjCPropertyAttribute(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static ObjCPropertyAttribute create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new ObjCPropertyAttribute(MemoryUtil.memAddress(container), container);
    }

    public static ObjCPropertyAttribute create(long address) {
        return new ObjCPropertyAttribute(address, null);
    }

    public static @Nullable ObjCPropertyAttribute createSafe(long address) {
        return address == 0L ? null : new ObjCPropertyAttribute(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(ObjCPropertyAttribute.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = ObjCPropertyAttribute.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static ObjCPropertyAttribute malloc(MemoryStack stack) {
        return new ObjCPropertyAttribute(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static ObjCPropertyAttribute calloc(MemoryStack stack) {
        return new ObjCPropertyAttribute(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static ByteBuffer nname(long struct) {
        return MemoryUtil.memByteBufferNT1(MemoryUtil.memGetAddress(struct + (long)NAME));
    }

    public static String nnameString(long struct) {
        return MemoryUtil.memUTF8(MemoryUtil.memGetAddress(struct + (long)NAME));
    }

    public static ByteBuffer nvalue(long struct) {
        return MemoryUtil.memByteBufferNT1(MemoryUtil.memGetAddress(struct + (long)VALUE));
    }

    public static String nvalueString(long struct) {
        return MemoryUtil.memUTF8(MemoryUtil.memGetAddress(struct + (long)VALUE));
    }

    public static void nname(long struct, ByteBuffer value) {
        if (Checks.CHECKS) {
            Checks.checkNT1(value);
        }
        MemoryUtil.memPutAddress(struct + (long)NAME, MemoryUtil.memAddress(value));
    }

    public static void nvalue(long struct, ByteBuffer value) {
        if (Checks.CHECKS) {
            Checks.checkNT1(value);
        }
        MemoryUtil.memPutAddress(struct + (long)VALUE, MemoryUtil.memAddress(value));
    }

    public static void validate(long struct) {
        Checks.check(MemoryUtil.memGetAddress(struct + (long)NAME));
        Checks.check(MemoryUtil.memGetAddress(struct + (long)VALUE));
    }

    static {
        Struct.Layout layout = ObjCPropertyAttribute.__struct(ObjCPropertyAttribute.__member(POINTER_SIZE), ObjCPropertyAttribute.__member(POINTER_SIZE));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        NAME = layout.offsetof(0);
        VALUE = layout.offsetof(1);
    }

    public static class Buffer
    extends StructBuffer<ObjCPropertyAttribute, Buffer>
    implements NativeResource {
        private static final ObjCPropertyAttribute ELEMENT_FACTORY = ObjCPropertyAttribute.create(-1L);

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
        protected ObjCPropertyAttribute getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="char *")
        public ByteBuffer name() {
            return ObjCPropertyAttribute.nname(this.address());
        }

        @NativeType(value="char *")
        public String nameString() {
            return ObjCPropertyAttribute.nnameString(this.address());
        }

        @NativeType(value="char *")
        public ByteBuffer value() {
            return ObjCPropertyAttribute.nvalue(this.address());
        }

        @NativeType(value="char *")
        public String valueString() {
            return ObjCPropertyAttribute.nvalueString(this.address());
        }

        public Buffer name(@NativeType(value="char *") ByteBuffer value) {
            ObjCPropertyAttribute.nname(this.address(), value);
            return this;
        }

        public Buffer value(@NativeType(value="char *") ByteBuffer value) {
            ObjCPropertyAttribute.nvalue(this.address(), value);
            return this;
        }
    }
}

