/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.util;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public final class FlattenIterator<T>
implements Iterator<T> {
    private final Iterator<? extends Map<?, ? extends T>> parent;
    private Iterator<? extends T> child = null;
    private T next;

    public FlattenIterator(Iterable<? extends Map<?, ? extends T>> core) {
        this.parent = core.iterator();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasNext() {
        this.getNext();
        return this.next != null;
    }

    @Override
    public T next() {
        T r = this.next;
        this.next = null;
        if (r == null) {
            throw new NoSuchElementException();
        }
        return r;
    }

    private void getNext() {
        if (this.next != null) {
            return;
        }
        if (this.child != null && this.child.hasNext()) {
            this.next = this.child.next();
            return;
        }
        if (this.parent.hasNext()) {
            this.child = this.parent.next().values().iterator();
            this.getNext();
        }
    }
}

