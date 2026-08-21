/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.macosx;

import java.nio.ByteBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

public class CGPoint
extends Struct<CGPoint>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int X;
    public static final int Y;

    protected CGPoint(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected CGPoint create(long address, @Nullable ByteBuffer container) {
        return new CGPoint(address, container);
    }

    public CGPoint(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), CGPoint.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="CGFloat")
    public double x() {
        return CGPoint.nx(this.address());
    }

    @NativeType(value="CGFloat")
    public double y() {
        return CGPoint.ny(this.address());
    }

    public CGPoint x(@NativeType(value="CGFloat") double value) {
        CGPoint.nx(this.address(), value);
        return this;
    }

    public CGPoint y(@NativeType(value="CGFloat") double value) {
        CGPoint.ny(this.address(), value);
        return this;
    }

    public CGPoint set(double x, double y) {
        this.x(x);
        this.y(y);
        return this;
    }

    public CGPoint set(CGPoint src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static CGPoint malloc() {
        return new CGPoint(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static CGPoint calloc() {
        return new CGPoint(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static CGPoint create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new CGPoint(MemoryUtil.memAddress(container), container);
    }

    public static CGPoint create(long address) {
        return new CGPoint(address, null);
    }

    public static @Nullable CGPoint createSafe(long address) {
        return address == 0L ? null : new CGPoint(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(CGPoint.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = CGPoint.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static CGPoint malloc(MemoryStack stack) {
        return new CGPoint(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static CGPoint calloc(MemoryStack stack) {
        return new CGPoint(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static double nx(long struct) {
        return MemoryUtil.memGetDouble(struct + (long)X);
    }

    public static double ny(long struct) {
        return MemoryUtil.memGetDouble(struct + (long)Y);
    }

    public static void nx(long struct, double value) {
        MemoryUtil.memPutDouble(struct + (long)X, value);
    }

    public static void ny(long struct, double value) {
        MemoryUtil.memPutDouble(struct + (long)Y, value);
    }

    static {
        Struct.Layout layout = CGPoint.__struct(CGPoint.__member(8), CGPoint.__member(8));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        X = layout.offsetof(0);
        Y = layout.offsetof(1);
    }

    public static class Buffer
    extends StructBuffer<CGPoint, Buffer>
    implements NativeResource {
        private static final CGPoint ELEMENT_FACTORY = CGPoint.create(-1L);

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
        protected CGPoint getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="CGFloat")
        public double x() {
            return CGPoint.nx(this.address());
        }

        @NativeType(value="CGFloat")
        public double y() {
            return CGPoint.ny(this.address());
        }

        public Buffer x(@NativeType(value="CGFloat") double value) {
            CGPoint.nx(this.address(), value);
            return this;
        }

        public Buffer y(@NativeType(value="CGFloat") double value) {
            CGPoint.ny(this.address(), value);
            return this;
        }
    }
}

