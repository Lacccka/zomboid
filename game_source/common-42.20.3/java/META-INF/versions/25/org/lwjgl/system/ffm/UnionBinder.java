/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.UnionLayout;
import java.util.function.Consumer;
import org.lwjgl.system.SegmentStack;
import org.lwjgl.system.ffm.GroupBinder;
import org.lwjgl.system.ffm.UnionArray;

public interface UnionBinder<T>
extends GroupBinder<UnionLayout, T> {
    @Override
    public UnionBinder<T> withByteAlignment(long var1);

    default public UnionBinder<T> set(MemorySegment segment, T value) {
        this.copy(value, this.get(segment));
        return this;
    }

    default public UnionBinder<T> set(MemorySegment segment, long offset, T value) {
        this.copy(value, this.get(segment, offset));
        return this;
    }

    default public UnionBinder<T> setAtIndex(MemorySegment segment, long index, T value) {
        this.copy(value, this.getAtIndex(segment, index));
        return this;
    }

    default public UnionBinder<T> apply(MemorySegment array, long offset, Consumer<T> consumer) {
        consumer.accept(this.get(array, offset));
        return this;
    }

    default public UnionBinder<T> applyAtIndex(MemorySegment segment, long index, Consumer<T> consumer) {
        consumer.accept(this.getAtIndex(segment, index));
        return this;
    }

    default public UnionArray<T> array(MemorySegment segment) {
        return new UnionArray(this, segment);
    }

    default public UnionArray<T> array(MemorySegment segment, long index) {
        return new UnionArray(this, this.asSlice(segment, index));
    }

    default public UnionArray<T> array(MemorySegment segment, long index, long elementCount) {
        return new UnionArray(this, this.asSlice(segment, index, elementCount));
    }

    default public UnionArray<T> malloc(SegmentStack stack, long elementCount) {
        return new UnionArray(this, stack.allocate(this.layout(), elementCount));
    }

    default public UnionArray<T> allocate(SegmentStack stack, long elementCount) {
        return new UnionArray(this, stack.calloc(this.layout(), elementCount));
    }

    default public UnionArray<T> allocate(SegmentAllocator allocator, long elementCount) {
        return new UnionArray(this, allocator.allocate(this.layout(), elementCount));
    }
}

