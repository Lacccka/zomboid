/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util;

import io.vavr.collection.HashSet;
import io.vavr.collection.LinkedHashSet;
import io.vavr.collection.Set;
import io.vavr.collection.TreeSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class VavrSetView<E>
implements java.util.Set<E> {
    private Set<E> vavrSet;
    private final boolean unmodifiable;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public VavrSetView(Set<E> vavrSet, boolean unmodifiable) {
        this.vavrSet = vavrSet;
        this.unmodifiable = unmodifiable;
    }

    @Override
    public int size() {
        this.lock.readLock().lock();
        try {
            int n = this.vavrSet.size();
            return n;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        this.lock.readLock().lock();
        try {
            boolean bl = this.vavrSet.isEmpty();
            return bl;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public boolean contains(Object o) {
        this.lock.readLock().lock();
        try {
            boolean bl = this.vavrSet.contains(o);
            return bl;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public Iterator<E> iterator() {
        this.lock.readLock().lock();
        try {
            VavrSetViewIterator vavrSetViewIterator = new VavrSetViewIterator();
            return vavrSetViewIterator;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public Object[] toArray() {
        this.lock.readLock().lock();
        try {
            Object[] objectArray = this.vavrSet.toJavaArray();
            return objectArray;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        this.lock.readLock().lock();
        try {
            if (a.length < this.size()) {
                T[] TArray = Arrays.copyOf(this.toArray(), this.size(), a.getClass());
                return TArray;
            }
            System.arraycopy(this.toArray(), 0, a, 0, this.size());
            if (a.length > this.size()) {
                a[this.size()] = null;
            }
            T[] TArray = a;
            return TArray;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean add(E e) {
        this.throwIfUnmodifiable();
        this.lock.writeLock().lock();
        try {
            int oldSize = this.vavrSet.size();
            this.vavrSet = this.vavrSet.add(e);
            boolean bl = oldSize != this.vavrSet.size();
            return bl;
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean remove(Object o) {
        this.throwIfUnmodifiable();
        this.lock.writeLock().lock();
        try {
            int oldSize = this.vavrSet.size();
            this.vavrSet = this.vavrSet.remove(o);
            boolean bl = oldSize != this.vavrSet.size();
            return bl;
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        this.lock.readLock().lock();
        try {
            boolean bl = c.stream().allMatch(this::contains);
            return bl;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        this.throwIfUnmodifiable();
        this.lock.writeLock().lock();
        try {
            int oldSize = this.vavrSet.size();
            this.vavrSet = this.vavrSet.addAll(c);
            boolean bl = oldSize != this.vavrSet.size();
            return bl;
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        this.throwIfUnmodifiable();
        this.lock.writeLock().lock();
        try {
            int oldSize = this.vavrSet.size();
            this.vavrSet = this.vavrSet.retainAll((Iterable)c);
            boolean bl = oldSize != this.vavrSet.size();
            return bl;
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        this.throwIfUnmodifiable();
        this.lock.writeLock().lock();
        try {
            int oldSize = this.vavrSet.size();
            this.vavrSet = this.vavrSet.removeAll(c);
            boolean bl = oldSize != this.vavrSet.size();
            return bl;
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        this.throwIfUnmodifiable();
        this.lock.writeLock().lock();
        try {
            this.vavrSet = this.vavrSet instanceof HashSet ? HashSet.empty() : (this.vavrSet instanceof TreeSet ? TreeSet.empty(((TreeSet)this.vavrSet).comparator()) : (this.vavrSet instanceof LinkedHashSet ? LinkedHashSet.empty() : this.vavrSet.removeAll(this.vavrSet)));
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    private void throwIfUnmodifiable() {
        if (this.unmodifiable) {
            throw new UnsupportedOperationException();
        }
    }

    private class VavrSetViewIterator
    implements Iterator<E> {
        Iterator<E> vavrIterator;
        E currentElement;

        private VavrSetViewIterator() {
            this.vavrIterator = VavrSetView.this.vavrSet.iterator();
            this.currentElement = null;
        }

        @Override
        public boolean hasNext() {
            return this.vavrIterator.hasNext();
        }

        @Override
        public E next() {
            this.currentElement = this.vavrIterator.next();
            return this.currentElement;
        }

        @Override
        public void remove() {
            VavrSetView.this.throwIfUnmodifiable();
            if (this.currentElement == null) {
                throw new IllegalStateException();
            }
            VavrSetView.this.remove(this.currentElement);
            this.currentElement = null;
        }
    }
}

