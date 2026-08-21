/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemorySegment;
import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.ffm.GroupBinder;

final class GroupSpliterator<L extends GroupLayout, T>
implements Spliterator<T> {
    private final GroupBinder<L, T> binder;
    private final MemorySegment segment;
    private long index;
    private final long fence;

    GroupSpliterator(GroupBinder<L, T> binder, MemorySegment segment, long index, long fence) {
        this.binder = binder;
        this.segment = segment;
        this.index = index;
        this.fence = fence;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (this.index < this.fence) {
            action.accept(this.binder.getAtIndex(this.segment, this.index++));
            return true;
        }
        return false;
    }

    @Override
    public @Nullable Spliterator<T> trySplit() {
        GroupSpliterator<L, T> groupSpliterator;
        long lo = this.index;
        long mid = lo + this.fence >>> 1;
        if (lo < mid) {
            this.index = mid;
            GroupSpliterator<L, T> groupSpliterator2 = new GroupSpliterator<L, T>(this.binder, this.segment, lo, this.index);
            groupSpliterator = groupSpliterator2;
        } else {
            groupSpliterator = null;
        }
        return groupSpliterator;
    }

    @Override
    public long estimateSize() {
        return this.fence - this.index;
    }

    @Override
    public int characteristics() {
        return 17744;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        long i;
        Objects.requireNonNull(action);
        try {
            for (i = this.index; i < this.fence; ++i) {
                action.accept(this.binder.getAtIndex(this.segment, i));
            }
        }
        finally {
            this.index = i;
        }
    }

    @Override
    public Comparator<? super T> getComparator() {
        throw new IllegalStateException();
    }
}

