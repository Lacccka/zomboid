/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import org.apache.commons.io.function.IOIterator;

final class IOIteratorAdapter<E>
implements IOIterator<E> {
    private final Iterator<E> delegate;

    static <E> IOIteratorAdapter<E> adapt(Iterator<E> delegate) {
        return new IOIteratorAdapter<E>(delegate);
    }

    IOIteratorAdapter(Iterator<E> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public boolean hasNext() throws IOException {
        return this.delegate.hasNext();
    }

    @Override
    public E next() throws IOException {
        return this.delegate.next();
    }

    @Override
    public Iterator<E> unwrap() {
        return this.delegate;
    }
}

