/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.StructLayout;
import java.util.function.Consumer;
import org.lwjgl.system.SegmentStack;
import org.lwjgl.system.ffm.GroupBinder;
import org.lwjgl.system.ffm.StructArray;

public interface StructBinder<T>
extends GroupBinder<StructLayout, T> {
    @Override
    public StructBinder<T> withByteAlignment(long var1);

    default public StructBinder<T> set(MemorySegment segment, T value) {
        this.copy(value, this.get(segment));
        return this;
    }

    default public StructBinder<T> set(MemorySegment segment, long offset, T value) {
        this.copy(value, this.get(segment, offset));
        return this;
    }

    default public StructBinder<T> setAtIndex(MemorySegment segment, long index, T value) {
        this.copy(value, this.getAtIndex(segment, index));
        return this;
    }

    default public StructBinder<T> apply(MemorySegment array, long offset, Consumer<T> consumer) {
        consumer.accept(this.get(array, offset));
        return this;
    }

    default public StructBinder<T> applyAtIndex(MemorySegment segment, long index, Consumer<T> consumer) {
        consumer.accept(this.getAtIndex(segment, index));
        return this;
    }

    default public StructArray<T> array(MemorySegment segment) {
        return new StructArray(this, segment);
    }

    default public StructArray<T> array(MemorySegment segment, long index) {
        return new StructArray(this, this.asSlice(segment, index));
    }

    default public StructArray<T> array(MemorySegment segment, long index, long elementCount) {
        return new StructArray(this, this.asSlice(segment, index, elementCount));
    }

    default public StructArray<T> malloc(SegmentStack stack, long elementCount) {
        return new StructArray(this, stack.allocate(this.layout(), elementCount));
    }

    default public StructArray<T> allocate(SegmentStack stack, long elementCount) {
        return new StructArray(this, stack.calloc(this.layout(), elementCount));
    }

    default public StructArray<T> allocate(SegmentAllocator allocator, long elementCount) {
        return new StructArray(this, allocator.allocate(this.layout(), elementCount));
    }
}

