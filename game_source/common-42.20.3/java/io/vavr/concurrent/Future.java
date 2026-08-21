/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.concurrent;

import io.vavr.CheckedFunction0;
import io.vavr.CheckedFunction1;
import io.vavr.CheckedPredicate;
import io.vavr.CheckedRunnable;
import io.vavr.PartialFunction;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Value;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.concurrent.FutureImpl;
import io.vavr.concurrent.GwtIncompatible;
import io.vavr.concurrent.Task;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Future<T>
extends Value<T> {
    @Deprecated
    public static final ExecutorService DEFAULT_EXECUTOR_SERVICE = ForkJoinPool.commonPool();
    public static final Executor DEFAULT_EXECUTOR = DEFAULT_EXECUTOR_SERVICE;

    public static <T> Future<T> failed(Throwable exception) {
        Objects.requireNonNull(exception, "exception is null");
        return Future.failed(DEFAULT_EXECUTOR, exception);
    }

    public static <T> Future<T> failed(Executor executor, Throwable exception) {
        Objects.requireNonNull(executor, "executor is null");
        Objects.requireNonNull(exception, "exception is null");
        return FutureImpl.of(executor, Try.failure(exception));
    }

    public static <T> Future<Option<T>> find(Iterable<? extends Future<? extends T>> futures, Predicate<? super T> predicate) {
        return Future.find(DEFAULT_EXECUTOR, futures, predicate);
    }

    public static <T> Future<Option<T>> find(Executor executor, Iterable<? extends Future<? extends T>> futures, Predicate<? super T> predicate) {
        Objects.requireNonNull(executor, "executor is null");
        Objects.requireNonNull(futures, "futures is null");
        Objects.requireNonNull(predicate, "predicate is null");
        List list = List.ofAll(futures);
        if (list.isEmpty()) {
            return Future.successful(executor, Option.none());
        }
        return Future.run(executor, (Task.Complete<? extends T> complete) -> {
            AtomicBoolean completed = new AtomicBoolean(false);
            AtomicInteger count = new AtomicInteger(list.length());
            list.forEach(future -> future.onComplete(result -> {
                AtomicInteger atomicInteger = count;
                synchronized (atomicInteger) {
                    if (!completed.get()) {
                        boolean wasLast = count.decrementAndGet() == 0;
                        result.filter(predicate).onSuccess(value -> completed.set(complete.with(Try.success(Option.some(value))))).onFailure(ignored -> {
                            if (wasLast) {
                                completed.set(complete.with(Try.success(Option.none())));
                            }
                        });
                    }
                }
            }));
        });
    }

    public static <T> Future<T> firstCompletedOf(Iterable<? extends Future<? extends T>> futures) {
        return Future.firstCompletedOf(DEFAULT_EXECUTOR, futures);
    }

    public static <T> Future<T> firstCompletedOf(Executor executor, Iterable<? extends Future<? extends T>> futures) {
        Objects.requireNonNull(executor, "executor is null");
        Objects.requireNonNull(futures, "futures is null");
        return Future.run(executor, (Task.Complete<? extends T> complete) -> futures.forEach(future -> future.onComplete(complete::with)));
    }

    public static <T, U> Future<U> fold(Iterable<? extends Future<? extends T>> futures, U zero, BiFunction<? super U, ? super T, ? extends U> f) {
        return Future.fold(DEFAULT_EXECUTOR, futures, zero, f);
    }

    public static <T, U> Future<U> fold(Executor executor, Iterable<? extends Future<? extends T>> futures, U zero, BiFunction<? super U, ? super T, ? extends U> f) {
        Objects.requireNonNull(executor, "executor is null");
        Objects.requireNonNull(futures, "futures is null");
        Objects.requireNonNull(f, "f is null");
        if (!futures.iterator().hasNext()) {
            return Future.successful(executor, zero);
        }
        return Future.sequence(executor, futures).map((T seq) -> seq.foldLeft(zero, f));
    }

    public static <T> Future<T> fromJavaFuture(java.util.concurrent.Future<T> future) {
        Objects.requireNonNull(future, "future is null");
        return Future.of(DEFAULT_EXECUTOR, future::get);
    }

    public static <T> Future<T> fromJavaFuture(Executor executor, java.util.concurrent.Future<T> future) {
        Objects.requireNonNull(executor, "executor is null");
        Objects.requireNonNull(future, "future is null");
        return Future.of(executor, future::get);
    }

    @GwtIncompatible
    public static <T> Future<T> fromCompletableFuture(CompletableFuture<T> future) {
        return Future.fromCompletableFuture(DEFAULT_EXECUTOR, future);
    }

    @GwtIncompatible
    public static <T> Future<T> fromCompletableFuture(Executor executor, CompletableFuture<T> future) {
        block3: {
            block2: {
                Objects.requireNonNull(executor, "executor is null");
                Objects.requireNonNull(future, "future is null");
                if (future.isDone() || future.isCompletedExceptionally()) break block2;
                if (!future.isCancelled()) break block3;
            }
            return Future.fromTry(Try.of(future::get).recoverWith((? super Throwable error) -> Try.failure(error.getCause())));
        }
        return Future.run(executor, (Task.Complete<? extends T> complete) -> future.handle((t, err) -> complete.with(err == null ? Try.success(t) : Try.failure(err))));
    }

    public static <T> Future<T> fromTry(Try<? extends T> result) {
        return Future.fromTry(DEFAULT_EXECUTOR, result);
    }

    public static <T> Future<T> fromTry(Executor executor, Try<? extends T> result) {
        Objects.requireNonNull(executor, "executor is null");
        Objects.requireNonNull(result, "result is null");
        return FutureImpl.of(executor, result);
    }

    public static <T> Future<T> narrow(Future<? extends T> future) {
        return future;
    }

    @Deprecated
    public static <T> Future<T> ofSupplier(Supplier<? extends T> computation) {
        Objects.requireNonNull(computation, "computation is null");
        return Future.of(DEFAULT_EXECUTOR, computation::get);
    }

    @Deprecated
    public static <T> Future<T> ofSupplier(Executor executor, Supplier<? extends T> computation) {
        Objects.requireNonNull(executor, "executor is null");
        Objects.requireNonNull(computation, "computation is null");
        return Future.of(executor, computation::get);
    }

    @Deprecated
    public static <T> Future<T> ofCallable(Callable<? extends T> computation) {
        Objects.requireNonNull(computation, "computation is null");
        return Future.of(DEFAULT_EXECUTOR, computation::call);
    }

    @Deprecated
    public static <T> Future<T> ofCallable(Executor executor, Callable<? extends T> computation) {
        Objects.requireNonNull(executor, "executor is null");
        Objects.requireNonNull(computation, "computation is null");
        return Future.of(executor, computation::call);
    }

    @Deprecated
    public static Future<Void> runRunnable(Runnable computation) {
        Objects.requireNonNull(computation, "computation is null");
        return Future.run(DEFAULT_EXECUTOR, computation::run);
    }

    @Deprecated
    public static Future<Void> runRunnable(Executor executor, Runnable computation) {
        Objects.requireNonNull(executor, "executor is null");
        Objects.requireNonNull(computation, "computation is null");
        return Future.run(executor, computation::run);
    }

    public static <T> Future<T> of(CheckedFunction0<? extends T> computation) {
        return Future.of(DEFAULT_EXECUTOR, computation);
    }

    public static <T> Future<T> of(Executor executor, CheckedFunction0<? extends T> computation) {
        Objects.requireNonNull(executor, "executor is null");
        Objects.requireNonNull(computation, "computation is null");
        return FutureImpl.async(executor, complete -> complete.with(Try.of(computation)));
    }

    @Deprecated
    public static <T> Future<T> run(Task<? extends T> task) {
        return Future.run(DEFAULT_EXECUTOR, task);
    }

    @Deprecated
    public static <T> Future<T> run(Executor executor, Task<? extends T> task) {
        return FutureImpl.sync(executor, task);
    }

    public static <T> Future<T> reduce(Iterable<? extends Future<? extends T>> futures, BiFunction<? super T, ? super T, ? extends T> f) {
        return Future.reduce(DEFAULT_EXECUTOR, futures, f);
    }

    public static <T> Future<T> reduce(Executor executor, Iterable<? extends Future<? extends T>> futures, BiFunction<? super T, ? super T, ? extends T> f) {
        Objects.requireNonNull(executor, "executor is null");
        Objects.requireNonNull(futures, "futures is null");
        Objects.requireNonNull(f, "f is null");
        if (!futures.iterator().hasNext()) {
            throw new NoSuchElementException("Future.reduce on empty futures");
        }
        return Future.sequence(executor, futures).map((T seq) -> seq.reduceLeft(f));
    }

    public static Future<Void> run(CheckedRunnable unit) {
        return Future.run(DEFAULT_EXECUTOR, unit);
    }

    public static Future<Void> run(Executor executor, CheckedRunnable unit) {
        Objects.requireNonNull(executor, "executor is null");
        Objects.requireNonNull(unit, "unit is null");
        return Future.of(executor, () -> {
            unit.run();
            return null;
        });
    }

    public static <T> Future<Seq<T>> sequence(Iterable<? extends Future<? extends T>> futures) {
        return Future.sequence(DEFAULT_EXECUTOR, futures);
    }

    public static <T> Future<Seq<T>> sequence(Executor executor, Iterable<? extends Future<? extends T>> futures) {
        Objects.requireNonNull(executor, "executor is null");
        Objects.requireNonNull(futures, "futures is null");
        Future zero = Future.successful(executor, Stream.empty());
        BiFunction<Future, Future, Future> f = (result, future) -> result.flatMap(seq -> future.map(seq::append));
        return Iterator.ofAll(futures).foldLeft(zero, f);
    }

    public static <T> Future<T> successful(T result) {
        return Future.successful(DEFAULT_EXECUTOR, result);
    }

    public static <T> Future<T> successful(Executor executor, T result) {
        Objects.requireNonNull(executor, "executor is null");
        return FutureImpl.of(executor, Try.success(result));
    }

    @Override
    @GwtIncompatible
    default public CompletableFuture<T> toCompletableFuture() {
        CompletableFuture future = new CompletableFuture();
        this.onSuccess(future::complete);
        this.onFailure(future::completeExceptionally);
        return future;
    }

    public static <T, U> Future<Seq<U>> traverse(Iterable<? extends T> values2, Function<? super T, ? extends Future<? extends U>> mapper) {
        return Future.traverse(DEFAULT_EXECUTOR, values2, mapper);
    }

    public static <T, U> Future<Seq<U>> traverse(Executor executor, Iterable<? extends T> values2, Function<? super T, ? extends Future<? extends U>> mapper) {
        Objects.requireNonNull(executor, "executor is null");
        Objects.requireNonNull(values2, "values is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return Future.sequence(executor, Iterator.ofAll(values2).map(mapper));
    }

    default public Future<T> andThen(Consumer<? super Try<T>> action) {
        Objects.requireNonNull(action, "action is null");
        return Future.run(this.executor(), (Task.Complete<? extends T> complete) -> this.onComplete(t -> {
            Try.run(() -> action.accept((Object)t));
            complete.with(t);
        }));
    }

    public Future<T> await();

    public Future<T> await(long var1, TimeUnit var3);

    default public boolean cancel() {
        return this.cancel(true);
    }

    public boolean cancel(boolean var1);

    default public <R> Future<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        Objects.requireNonNull(partialFunction, "partialFunction is null");
        return Future.run(this.executor(), (Task.Complete<? extends T> complete) -> this.onComplete(result -> complete.with(result.collect(partialFunction))));
    }

    default public Executor executor() {
        return this.executorService();
    }

    @Deprecated
    public ExecutorService executorService() throws UnsupportedOperationException;

    default public Future<Throwable> failed() {
        return Future.run(this.executor(), (Task.Complete<? extends T> complete) -> this.onComplete(result -> {
            if (result.isFailure()) {
                complete.with(Try.success(result.getCause()));
            } else {
                complete.with(Try.failure(new NoSuchElementException("Future.failed completed without a throwable")));
            }
        }));
    }

    default public Future<T> fallbackTo(Future<? extends T> that) {
        Objects.requireNonNull(that, "that is null");
        return Future.run(this.executor(), (Task.Complete<? extends T> complete) -> this.onComplete(t -> {
            if (t.isSuccess()) {
                complete.with(t);
            } else {
                that.onComplete(alt -> complete.with(alt.isSuccess() ? alt : t));
            }
        }));
    }

    default public Future<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.filterTry(predicate::test);
    }

    default public Future<T> filterTry(CheckedPredicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Future.run(this.executor(), (Task.Complete<? extends T> complete) -> this.onComplete(result -> complete.with(result.filterTry(predicate))));
    }

    default public Option<Throwable> getCause() {
        return this.getValue().map(Try::getCause);
    }

    public Option<Try<T>> getValue();

    public boolean isCancelled();

    public boolean isCompleted();

    default public boolean isSuccess() {
        return this.isCompleted() && this.getValue().get().isSuccess();
    }

    default public boolean isFailure() {
        return this.isCompleted() && this.getValue().get().isFailure();
    }

    public Future<T> onComplete(Consumer<? super Try<T>> var1);

    default public Future<T> onFailure(Consumer<? super Throwable> action) {
        Objects.requireNonNull(action, "action is null");
        return this.onComplete(result -> result.onFailure(action));
    }

    default public Future<T> onSuccess(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        return this.onComplete(result -> result.onSuccess(action));
    }

    default public Future<T> recover(Function<? super Throwable, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return this.transformValue(t -> t.recover(f));
    }

    default public Future<T> recoverWith(Function<? super Throwable, ? extends Future<? extends T>> f) {
        Objects.requireNonNull(f, "f is null");
        return Future.run(this.executor(), (Task.Complete<? extends T> complete) -> this.onComplete(t -> {
            if (t.isFailure()) {
                Try.run(() -> ((Future)f.apply(t.getCause())).onComplete(complete::with)).onFailure(x -> complete.with(Try.failure(x)));
            } else {
                complete.with(t);
            }
        }));
    }

    default public <U> U transform(Function<? super Future<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    default public <U> Future<U> transformValue(Function<? super Try<T>, ? extends Try<? extends U>> f) {
        Objects.requireNonNull(f, "f is null");
        return Future.run(this.executor(), (Task.Complete<? extends T> complete) -> this.onComplete(t -> Try.run(() -> complete.with((Try)f.apply((Object)t))).onFailure(x -> complete.with(Try.failure(x)))));
    }

    default public <U> Future<Tuple2<T, U>> zip(Future<? extends U> that) {
        Objects.requireNonNull(that, "that is null");
        return this.zipWith(that, Tuple::of);
    }

    default public <U, R> Future<R> zipWith(Future<? extends U> that, BiFunction<? super T, ? super U, ? extends R> combinator) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(combinator, "combinator is null");
        return Future.run(this.executor(), (Task.Complete<? extends T> complete) -> this.onComplete(res1 -> {
            if (res1.isFailure()) {
                complete.with((Try.Failure)res1);
            } else {
                that.onComplete(res2 -> {
                    Try result = res1.flatMap((? super T t) -> res2.map((T u) -> combinator.apply(t, u)));
                    complete.with(result);
                });
            }
        }));
    }

    default public <U> Future<U> flatMap(Function<? super T, ? extends Future<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.flatMapTry(mapper::apply);
    }

    default public <U> Future<U> flatMapTry(CheckedFunction1<? super T, ? extends Future<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return Future.run(this.executor(), (Task.Complete<? extends T> complete) -> this.onComplete(result -> result.mapTry(mapper).onSuccess(future -> future.onComplete(complete::with)).onFailure(x -> complete.with(Try.failure(x)))));
    }

    @Override
    default public void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        this.onComplete(result -> result.forEach(action));
    }

    @Override
    default public T get() {
        return this.await().getValue().get().get();
    }

    @Override
    default public boolean isAsync() {
        return true;
    }

    @Override
    default public boolean isEmpty() {
        return this.await().getValue().get().isEmpty();
    }

    @Override
    default public boolean isLazy() {
        return false;
    }

    @Override
    default public boolean isSingleValued() {
        return true;
    }

    @Override
    default public Iterator<T> iterator() {
        return this.isEmpty() ? Iterator.empty() : Iterator.of(this.get());
    }

    @Override
    default public <U> Future<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.transformValue(t -> t.map(mapper));
    }

    default public <U> Future<U> mapTry(CheckedFunction1<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.transformValue(t -> t.mapTry(mapper));
    }

    default public Future<T> orElse(Future<? extends T> other) {
        Objects.requireNonNull(other, "other is null");
        return Future.run(this.executor(), (Task.Complete<? extends T> complete) -> this.onComplete(result -> {
            if (result.isSuccess()) {
                complete.with(result);
            } else {
                other.onComplete(complete::with);
            }
        }));
    }

    default public Future<T> orElse(Supplier<? extends Future<? extends T>> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return Future.run(this.executor(), (Task.Complete<? extends T> complete) -> this.onComplete(arg_0 -> Future.lambda$null$52(complete, (Supplier)supplier, arg_0)));
    }

    @Override
    default public Future<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        this.onSuccess(action);
        return this;
    }

    @Override
    default public String stringPrefix() {
        return "Future";
    }

    private static /* synthetic */ void lambda$null$52(Task.Complete complete, Supplier supplier, Try result) {
        if (result.isSuccess()) {
            complete.with(result);
        } else {
            ((Future)supplier.get()).onComplete(complete::with);
        }
    }
}

