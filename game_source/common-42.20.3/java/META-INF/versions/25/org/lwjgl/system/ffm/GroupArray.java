/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemorySegment;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.ffm.GroupBinder;
import org.lwjgl.system.ffm.StructArray;
import org.lwjgl.system.ffm.UnionArray;

public sealed interface GroupArray<L extends GroupLayout, T>
extends Pointer,
Iterable<T>
permits StructArray, UnionArray {
    @Override
    public long address();

    public GroupBinder<L, T> binder();

    public MemorySegment segment();

    public T get(long var1);

    public GroupArray<L, T> set(long var1, T var3);

    public <GA extends GroupArray<L, T>> GroupArray<L, T> copy(long var1, GA var3, long var4, long var6);

    public void clear(long var1, long var3);

    public GroupArray<L, T> apply(long var1, Consumer<T> var3);

    public GroupArray<L, T> slice(long var1);

    public GroupArray<L, T> slice(long var1, long var3);

    default public long length() {
        return this.segment().byteSize() / this.binder().sizeof();
    }

    default public <GA extends GroupArray<L, T>> GroupArray<L, T> copy(GA dst) {
        return this.copy(0L, dst, 0L, this.length());
    }

    default public <GA extends GroupArray<L, T>> GroupArray<L, T> copy(GA dst, long length) {
        return this.copy(0L, dst, 0L, length);
    }

    default public void clear() {
        this.clear(0L, this.length());
    }

    default public long sizeof() {
        return this.binder().sizeof() * this.length();
    }

    default public long alignof() {
        return this.binder().alignof();
    }

    @Override
    default public void forEach(Consumer<? super T> action) {
        this.binder().forEach(this.segment(), action);
    }

    @Override
    default public Iterator<T> iterator() {
        return this.binder().iterator(this.segment());
    }

    @Override
    default public Spliterator<T> spliterator() {
        return this.binder().spliterator(this.segment());
    }

    default public Stream<T> stream() {
        return this.binder().stream(this.segment());
    }

    default public Stream<T> parallelStream() {
        return this.binder().parallelStream(this.segment());
    }
}

