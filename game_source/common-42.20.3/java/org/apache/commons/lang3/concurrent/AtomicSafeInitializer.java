/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.concurrent;

import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.concurrent.AbstractConcurrentInitializer;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableSupplier;

public class AtomicSafeInitializer<T>
extends AbstractConcurrentInitializer<T, ConcurrentException> {
    private static final Object NO_INIT = new Object();
    private final AtomicReference<AtomicSafeInitializer<T>> factory = new AtomicReference();
    private final AtomicReference<T> reference = new AtomicReference<T>(this.getNoInit());

    public static <T> Builder<AtomicSafeInitializer<T>, T> builder() {
        return new Builder();
    }

    public AtomicSafeInitializer() {
    }

    private AtomicSafeInitializer(FailableSupplier<T, ConcurrentException> initializer, FailableConsumer<T, ConcurrentException> closer) {
        super(initializer, closer);
    }

    @Override
    public final T get() throws ConcurrentException {
        T result;
        while ((result = this.reference.get()) == this.getNoInit()) {
            if (!this.factory.compareAndSet(null, this)) continue;
            this.reference.set(this.initialize());
        }
        return result;
    }

    private T getNoInit() {
        return (T)NO_INIT;
    }

    @Override
    protected ConcurrentException getTypedException(Exception e) {
        return new ConcurrentException(e);
    }

    @Override
    public boolean isInitialized() {
        return this.reference.get() != NO_INIT;
    }

    public static class Builder<I extends AtomicSafeInitializer<T>, T>
    extends AbstractConcurrentInitializer.AbstractBuilder<I, T, Builder<I, T>, ConcurrentException> {
        @Override
        public I get() {
            return (I)new AtomicSafeInitializer(this.getInitializer(), this.getCloser());
        }
    }
}

