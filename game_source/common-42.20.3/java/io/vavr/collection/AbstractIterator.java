/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.collection.Iterator;
import java.util.NoSuchElementException;

abstract class AbstractIterator<T>
implements Iterator<T> {
    AbstractIterator() {
    }

    @Override
    public String toString() {
        return this.stringPrefix() + "(" + (this.isEmpty() ? "" : "?") + ")";
    }

    protected abstract T getNext();

    @Override
    public final T next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException("next() on empty iterator");
        }
        return this.getNext();
    }
}

