/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.util.Iterator;
import java.util.Objects;
import org.apache.commons.io.function.IOIterator;
import org.apache.commons.io.function.Uncheck;

final class UncheckedIOIterator<E>
implements Iterator<E> {
    private final IOIterator<E> delegate;

    UncheckedIOIterator(IOIterator<E> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public boolean hasNext() {
        return Uncheck.get(this.delegate::hasNext);
    }

    @Override
    public E next() {
        return (E)Uncheck.get(this.delegate::next);
    }

    @Override
    public void remove() {
        Uncheck.run(this.delegate::remove);
    }
}

