/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.function.IOIteratorAdapter;
import org.apache.commons.io.function.UncheckedIOIterator;

public interface IOIterator<E> {
    public static <E> IOIterator<E> adapt(Iterator<E> iterator2) {
        return IOIteratorAdapter.adapt(iterator2);
    }

    default public Iterator<E> asIterator() {
        return new UncheckedIOIterator(this);
    }

    default public void forEachRemaining(IOConsumer<? super E> action) throws IOException {
        Objects.requireNonNull(action);
        while (this.hasNext()) {
            action.accept(this.next());
        }
    }

    public boolean hasNext() throws IOException;

    public E next() throws IOException;

    default public void remove() throws IOException {
        this.unwrap().remove();
    }

    public Iterator<E> unwrap();
}

