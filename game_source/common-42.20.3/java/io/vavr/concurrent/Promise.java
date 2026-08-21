/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.concurrent;

import io.vavr.concurrent.Future;
import io.vavr.concurrent.FutureImpl;
import io.vavr.concurrent.PromiseImpl;
import io.vavr.control.Try;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public interface Promise<T> {
    public static <T> Promise<T> failed(Throwable exception) {
        Objects.requireNonNull(exception, "exception is null");
        return Promise.failed(Future.DEFAULT_EXECUTOR, exception);
    }

    public static <T> Promise<T> failed(Executor executor, Throwable exception) {
        Objects.requireNonNull(executor, "executor is null");
        Objects.requireNonNull(exception, "exception is null");
        return Promise.make(executor).failure(exception);
    }

    public static <T> Promise<T> fromTry(Try<? extends T> result) {
        return Promise.fromTry(Future.DEFAULT_EXECUTOR, result);
    }

    public static <T> Promise<T> fromTry(Executor executor, Try<? extends T> result) {
        Objects.requireNonNull(executor, "executor is null");
        Objects.requireNonNull(result, "result is null");
        return Promise.make(executor).complete(result);
    }

    public static <T> Promise<T> make() {
        return Promise.make(Future.DEFAULT_EXECUTOR);
    }

    public static <T> Promise<T> make(Executor executor) {
        Objects.requireNonNull(executor, "executor is null");
        return new PromiseImpl(FutureImpl.of(executor));
    }

    public static <T> Promise<T> narrow(Promise<? extends T> promise) {
        return promise;
    }

    public static <T> Promise<T> successful(T result) {
        return Promise.successful(Future.DEFAULT_EXECUTOR, result);
    }

    public static <T> Promise<T> successful(Executor executor, T result) {
        Objects.requireNonNull(executor, "executor is null");
        return Promise.make(executor).success(result);
    }

    default public Executor executor() {
        return this.executorService();
    }

    @Deprecated
    public ExecutorService executorService();

    public Future<T> future();

    default public boolean isCompleted() {
        return this.future().isCompleted();
    }

    default public Promise<T> complete(Try<? extends T> value) {
        if (this.tryComplete(value)) {
            return this;
        }
        throw new IllegalStateException("Promise already completed.");
    }

    public boolean tryComplete(Try<? extends T> var1);

    default public Promise<T> completeWith(Future<? extends T> other) {
        return this.tryCompleteWith(other);
    }

    default public Promise<T> tryCompleteWith(Future<? extends T> other) {
        other.onComplete(this::tryComplete);
        return this;
    }

    default public Promise<T> success(T value) {
        return this.complete(Try.success(value));
    }

    default public boolean trySuccess(T value) {
        return this.tryComplete(Try.success(value));
    }

    default public Promise<T> failure(Throwable exception) {
        return this.complete(Try.failure(exception));
    }

    default public boolean tryFailure(Throwable exception) {
        return this.tryComplete(Try.failure(exception));
    }
}

