/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.util.Objects;
import java.util.Spliterator;
import org.apache.commons.io.function.IOSpliterator;

final class IOSpliteratorAdapter<T>
implements IOSpliterator<T> {
    private final Spliterator<T> delegate;

    static <E> IOSpliteratorAdapter<E> adapt(Spliterator<E> delegate) {
        return new IOSpliteratorAdapter<E>(delegate);
    }

    IOSpliteratorAdapter(Spliterator<T> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public Spliterator<T> unwrap() {
        return this.delegate;
    }
}

