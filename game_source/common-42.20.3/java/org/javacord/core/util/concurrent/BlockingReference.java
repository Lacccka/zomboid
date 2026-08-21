/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingReference<V> {
    private final Lock lock = new ReentrantLock();
    private final Condition hasValue = this.lock.newCondition();
    private volatile V value;

    public BlockingReference(V initialValue) {
        this.value = initialValue;
    }

    public BlockingReference() {
    }

    public boolean hasValue() {
        return this.value != null;
    }

    public V get() throws InterruptedException {
        this.lock.lock();
        try {
            while (this.value == null) {
                this.hasValue.await();
            }
            V v = this.value;
            return v;
        }
        finally {
            this.lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public V get(long timeout2, TimeUnit unit) throws InterruptedException {
        this.lock.lock();
        try {
            if (this.value == null) {
                this.hasValue.await(timeout2, unit);
            }
            V v = this.value;
            return v;
        }
        finally {
            this.lock.unlock();
        }
    }

    public void set(V value) {
        this.lock.lock();
        try {
            this.value = value;
            if (value != null) {
                this.hasValue.signalAll();
            }
        }
        finally {
            this.lock.unlock();
        }
    }
}

