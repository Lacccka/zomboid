/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.concurrent;

import io.vavr.concurrent.Future;
import io.vavr.concurrent.FutureImpl;
import io.vavr.concurrent.Promise;
import io.vavr.control.Try;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

final class PromiseImpl<T>
implements Promise<T> {
    private final FutureImpl<T> future;

    PromiseImpl(FutureImpl<T> future) {
        this.future = future;
    }

    @Override
    public Executor executor() {
        return this.future.executor();
    }

    @Override
    @Deprecated
    public ExecutorService executorService() {
        return this.future.executorService();
    }

    @Override
    public Future<T> future() {
        return this.future;
    }

    @Override
    public boolean tryComplete(Try<? extends T> value) {
        return this.future.tryComplete(value);
    }

    public String toString() {
        return "Promise(" + this.future.getValue().map(String::valueOf).getOrElse("?") + ")";
    }
}

