/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm.mapping;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import org.lwjgl.system.SegmentStack;
import org.lwjgl.system.ffm.mapping.Mapping;

public interface DataMapping<L extends MemoryLayout>
extends Mapping<L> {
    public DataMapping<L> withByteAlignment(long var1);

    default public Mapping.Sequence array(long elementCount) {
        return new Mapping.Sequence(this, elementCount);
    }

    default public MemorySegment mallocSegment(SegmentStack stack) {
        return stack.allocate((MemoryLayout)this.layout());
    }

    default public MemorySegment mallocSegment(SegmentStack stack, long elementCount) {
        return stack.allocate((MemoryLayout)this.layout(), elementCount);
    }

    default public MemorySegment allocateSegment(SegmentStack stack) {
        return stack.calloc((MemoryLayout)this.layout());
    }

    default public MemorySegment allocateSegment(SegmentAllocator allocator) {
        return allocator.allocate((MemoryLayout)this.layout());
    }

    default public MemorySegment allocateSegment(SegmentStack stack, long elementCount) {
        return stack.calloc((MemoryLayout)this.layout(), elementCount);
    }

    default public MemorySegment allocateSegment(SegmentAllocator allocator, long elementCount) {
        return allocator.allocate((MemoryLayout)this.layout(), elementCount);
    }
}

