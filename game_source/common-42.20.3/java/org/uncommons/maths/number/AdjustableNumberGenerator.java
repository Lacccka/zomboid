/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.number;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.uncommons.maths.number.NumberGenerator;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class AdjustableNumberGenerator<T extends Number>
implements NumberGenerator<T> {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private T value;

    public AdjustableNumberGenerator(T value) {
        this.value = value;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setValue(T value) {
        try {
            this.lock.writeLock().lock();
            this.value = value;
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public T nextValue() {
        try {
            this.lock.readLock().lock();
            T t = this.value;
            return t;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }
}

