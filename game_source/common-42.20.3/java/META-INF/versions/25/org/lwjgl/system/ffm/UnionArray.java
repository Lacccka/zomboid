/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.UnionLayout;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Objects;
import java.util.function.Consumer;
import org.lwjgl.system.Checks;
import org.lwjgl.system.ffm.GroupArray;
import org.lwjgl.system.ffm.UnionBinder;

public final class UnionArray<T>
extends Record
implements GroupArray<UnionLayout, T> {
    private final UnionBinder<T> binder;
    private final MemorySegment segment;

    public UnionArray(UnionBinder<T> binder, MemorySegment segment) {
        this.binder = binder;
        this.segment = segment;
    }

    @Override
    public long address() {
        return this.segment.address();
    }

    @Override
    public T get(long index) {
        return this.binder.getAtIndex(this.segment, index);
    }

    public UnionArray<T> set(long index, T value) {
        this.binder.setAtIndex(this.segment, index, (Object)value);
        return this;
    }

    public <GA extends GroupArray<UnionLayout, T>> UnionArray<T> copy(long thisIndex, GA other, long otherIndex, long length) {
        if (Checks.DEBUG) {
            Objects.checkFromIndexSize(thisIndex, length, this.length());
            Objects.checkFromIndexSize(otherIndex, length, other.length());
        }
        for (long i = 0L; i < length; ++i) {
            this.binder.copy(this.binder.getAtIndex(this.segment, thisIndex + i), this.binder.getAtIndex(other.segment(), otherIndex + i));
        }
        return this;
    }

    @Override
    public void clear(long fromIndex, long toIndex) {
        if (Checks.DEBUG) {
            Objects.checkFromToIndex(fromIndex, toIndex, this.length());
        }
        for (long i = fromIndex; i < toIndex; ++i) {
            this.binder.clear(this.binder.getAtIndex(this.segment, i));
        }
    }

    public UnionArray<T> apply(long index, Consumer<T> consumer) {
        if (Checks.DEBUG) {
            Objects.checkIndex(index, this.length());
        }
        this.binder.applyAtIndex(this.segment, index, (Consumer)consumer);
        return this;
    }

    public UnionArray<T> slice(long index) {
        return new UnionArray<T>(this.binder, this.segment.asSlice(index * this.binder.sizeof()));
    }

    public UnionArray<T> slice(long index, long elementCount) {
        long sizeof = this.binder.sizeof();
        return new UnionArray<T>(this.binder, this.segment.asSlice(index * sizeof, elementCount * sizeof));
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{UnionArray.class, "binder;segment", "binder", "segment"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{UnionArray.class, "binder;segment", "binder", "segment"}, this);
    }

    @Override
    public final boolean equals(Object o) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{UnionArray.class, "binder;segment", "binder", "segment"}, this, o);
    }

    public UnionBinder<T> binder() {
        return this.binder;
    }

    @Override
    public MemorySegment segment() {
        return this.segment;
    }
}

