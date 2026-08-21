/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.util.Objects;
import java.util.Spliterator;
import org.apache.commons.io.function.IOComparator;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.function.IOSpliteratorAdapter;
import org.apache.commons.io.function.UncheckedIOSpliterator;

public interface IOSpliterator<T> {
    public static <E> IOSpliterator<E> adapt(Spliterator<E> iterator2) {
        return IOSpliteratorAdapter.adapt(iterator2);
    }

    default public Spliterator<T> asSpliterator() {
        return new UncheckedIOSpliterator(this);
    }

    default public int characteristics() {
        return this.unwrap().characteristics();
    }

    default public long estimateSize() {
        return this.unwrap().estimateSize();
    }

    default public void forEachRemaining(IOConsumer<? super T> action) {
        while (this.tryAdvance(action)) {
        }
    }

    default public IOComparator<? super T> getComparator() {
        return (IOComparator)((Object)this.unwrap().getComparator());
    }

    default public long getExactSizeIfKnown() {
        return this.unwrap().getExactSizeIfKnown();
    }

    default public boolean hasCharacteristics(int characteristics) {
        return this.unwrap().hasCharacteristics(characteristics);
    }

    default public boolean tryAdvance(IOConsumer<? super T> action) {
        return this.unwrap().tryAdvance(Objects.requireNonNull(action, "action").asConsumer());
    }

    default public IOSpliterator<T> trySplit() {
        return IOSpliterator.adapt(this.unwrap().trySplit());
    }

    public Spliterator<T> unwrap();
}

