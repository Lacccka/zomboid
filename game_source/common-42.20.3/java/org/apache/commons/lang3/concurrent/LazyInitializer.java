/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.concurrent;

import org.apache.commons.lang3.concurrent.AbstractConcurrentInitializer;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableSupplier;

public class LazyInitializer<T>
extends AbstractConcurrentInitializer<T, ConcurrentException> {
    private static final Object NO_INIT = new Object();
    private volatile T object = NO_INIT;

    public static <T> Builder<LazyInitializer<T>, T> builder() {
        return new Builder();
    }

    public LazyInitializer() {
    }

    private LazyInitializer(FailableSupplier<T, ConcurrentException> initializer, FailableConsumer<T, ConcurrentException> closer) {
        super(initializer, closer);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public T get() throws ConcurrentException {
        T result = this.object;
        if (result == NO_INIT) {
            LazyInitializer lazyInitializer = this;
            synchronized (lazyInitializer) {
                result = this.object;
                if (result == NO_INIT) {
                    this.object = result = this.initialize();
                }
            }
        }
        return result;
    }

    @Override
    protected ConcurrentException getTypedException(Exception e) {
        return new ConcurrentException(e);
    }

    @Override
    public boolean isInitialized() {
        return this.object != NO_INIT;
    }

    public static class Builder<I extends LazyInitializer<T>, T>
    extends AbstractConcurrentInitializer.AbstractBuilder<I, T, Builder<I, T>, ConcurrentException> {
        @Override
        public I get() {
            return (I)new LazyInitializer(this.getInitializer(), this.getCloser());
        }
    }
}

