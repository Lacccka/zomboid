/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.jni;

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

public class JNINativeMethod
extends Struct<JNINativeMethod>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int NAME;
    public static final int SIGNATURE;
    public static final int FNPTR;

    protected JNINativeMethod(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected JNINativeMethod create(long address, @Nullable ByteBuffer container) {
        return new JNINativeMethod(address, container);
    }

    public JNINativeMethod(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), JNINativeMethod.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="char *")
    public ByteBuffer name() {
        return JNINativeMethod.nname(this.address());
    }

    @NativeType(value="char *")
    public String nameString() {
        return JNINativeMethod.nnameString(this.address());
    }

    @NativeType(value="char *")
    public ByteBuffer signature() {
        return JNINativeMethod.nsignature(this.address());
    }

    @NativeType(value="char *")
    public String signatureString() {
        return JNINativeMethod.nsignatureString(this.address());
    }

    @NativeType(value="void *")
    public long fnPtr() {
        return JNINativeMethod.nfnPtr(this.address());
    }

    public JNINativeMethod name(@NativeType(value="char *") ByteBuffer value) {
        JNINativeMethod.nname(this.address(), value);
        return this;
    }

    public JNINativeMethod signature(@NativeType(value="char *") ByteBuffer value) {
        JNINativeMethod.nsignature(this.address(), value);
        return this;
    }

    public JNINativeMethod fnPtr(@NativeType(value="void *") long value) {
        JNINativeMethod.nfnPtr(this.address(), value);
        return this;
    }

    public JNINativeMethod set(ByteBuffer name, ByteBuffer signature, long fnPtr) {
        this.name(name);
        this.signature(signature);
        this.fnPtr(fnPtr);
        return this;
    }

    public JNINativeMethod set(JNINativeMethod src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static JNINativeMethod malloc() {
        return new JNINativeMethod(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static JNINativeMethod calloc() {
        return new JNINativeMethod(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static JNINativeMethod create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new JNINativeMethod(MemoryUtil.memAddress(container), container);
    }

    public static JNINativeMethod create(long address) {
        return new JNINativeMethod(address, null);
    }

    public static @Nullable JNINativeMethod createSafe(long address) {
        return address == 0L ? null : new JNINativeMethod(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(JNINativeMethod.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = JNINativeMethod.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static JNINativeMethod malloc(MemoryStack stack) {
        return new JNINativeMethod(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static JNINativeMethod calloc(MemoryStack stack) {
        return new JNINativeMethod(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
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

    public static ByteBuffer nsignature(long struct) {
        return MemoryUtil.memByteBufferNT1(MemoryUtil.memGetAddress(struct + (long)SIGNATURE));
    }

    public static String nsignatureString(long struct) {
        return MemoryUtil.memUTF8(MemoryUtil.memGetAddress(struct + (long)SIGNATURE));
    }

    public static long nfnPtr(long struct) {
        return MemoryUtil.memGetAddress(struct + (long)FNPTR);
    }

    public static void nname(long struct, ByteBuffer value) {
        if (Checks.CHECKS) {
            Checks.checkNT1(value);
        }
        MemoryUtil.memPutAddress(struct + (long)NAME, MemoryUtil.memAddress(value));
    }

    public static void nsignature(long struct, ByteBuffer value) {
        if (Checks.CHECKS) {
            Checks.checkNT1(value);
        }
        MemoryUtil.memPutAddress(struct + (long)SIGNATURE, MemoryUtil.memAddress(value));
    }

    public static void nfnPtr(long struct, long value) {
        MemoryUtil.memPutAddress(struct + (long)FNPTR, Checks.check(value));
    }

    public static void validate(long struct) {
        Checks.check(MemoryUtil.memGetAddress(struct + (long)NAME));
        Checks.check(MemoryUtil.memGetAddress(struct + (long)SIGNATURE));
        Checks.check(MemoryUtil.memGetAddress(struct + (long)FNPTR));
    }

    static {
        Struct.Layout layout = JNINativeMethod.__struct(JNINativeMethod.__member(POINTER_SIZE), JNINativeMethod.__member(POINTER_SIZE), JNINativeMethod.__member(POINTER_SIZE));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        NAME = layout.offsetof(0);
        SIGNATURE = layout.offsetof(1);
        FNPTR = layout.offsetof(2);
    }

    public static class Buffer
    extends StructBuffer<JNINativeMethod, Buffer>
    implements NativeResource {
        private static final JNINativeMethod ELEMENT_FACTORY = JNINativeMethod.create(-1L);

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
        protected JNINativeMethod getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="char *")
        public ByteBuffer name() {
            return JNINativeMethod.nname(this.address());
        }

        @NativeType(value="char *")
        public String nameString() {
            return JNINativeMethod.nnameString(this.address());
        }

        @NativeType(value="char *")
        public ByteBuffer signature() {
            return JNINativeMethod.nsignature(this.address());
        }

        @NativeType(value="char *")
        public String signatureString() {
            return JNINativeMethod.nsignatureString(this.address());
        }

        @NativeType(value="void *")
        public long fnPtr() {
            return JNINativeMethod.nfnPtr(this.address());
        }

        public Buffer name(@NativeType(value="char *") ByteBuffer value) {
            JNINativeMethod.nname(this.address(), value);
            return this;
        }

        public Buffer signature(@NativeType(value="char *") ByteBuffer value) {
            JNINativeMethod.nsignature(this.address(), value);
            return this;
        }

        public Buffer fnPtr(@NativeType(value="void *") long value) {
            JNINativeMethod.nfnPtr(this.address(), value);
            return this;
        }
    }
}

