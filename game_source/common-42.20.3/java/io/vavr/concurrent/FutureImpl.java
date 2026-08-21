/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.concurrent;

import io.vavr.collection.Queue;
import io.vavr.concurrent.Future;
import io.vavr.concurrent.Task;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

final class FutureImpl<T>
implements Future<T> {
    private final Executor executor;
    private final Object lock = new Object();
    private volatile boolean cancelled;
    private volatile Option<Try<T>> value;
    private Queue<Consumer<Try<T>>> actions;
    private Queue<Thread> waiters;
    private Thread thread;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private FutureImpl(Executor executor, Option<Try<T>> value, Queue<Consumer<Try<T>>> actions, Queue<Thread> waiters, Computation<T> computation) {
        this.executor = executor;
        Object object = this.lock;
        synchronized (object) {
            this.cancelled = false;
            this.value = value;
            this.actions = actions;
            this.waiters = waiters;
            try {
                computation.execute(this::tryComplete, this::updateThread);
            }
            catch (Throwable x) {
                this.tryComplete(Try.failure(x));
            }
        }
    }

    static <T> FutureImpl<T> of(Executor executor) {
        return new FutureImpl<T>(executor, Option.none(), Queue.empty(), Queue.empty(), (complete, updateThread) -> {});
    }

    static <T> FutureImpl<T> of(Executor executor, Try<? extends T> value) {
        return new FutureImpl<T>(executor, Option.some(Try.narrow(value)), null, null, (complete, updateThread) -> {});
    }

    static <T> FutureImpl<T> sync(Executor executor, Task<? extends T> task) {
        return new FutureImpl<T>(executor, Option.none(), Queue.empty(), Queue.empty(), (complete, updateThread) -> task.run(complete::with));
    }

    static <T> FutureImpl<T> async(Executor executor, Task<? extends T> task) {
        return new FutureImpl<T>(executor, Option.none(), Queue.empty(), Queue.empty(), (complete, updateThread) -> executor.execute(() -> {
            updateThread.run();
            try {
                task.run(complete::with);
            }
            catch (Throwable x) {
                complete.with(Try.failure(x));
            }
        }));
    }

    @Override
    public Future<T> await() {
        if (!this.isCompleted()) {
            this._await(-1L, -1L, null);
        }
        return this;
    }

    @Override
    public Future<T> await(long timeout2, TimeUnit unit) {
        long now = System.nanoTime();
        Objects.requireNonNull(unit, "unit is null");
        if (timeout2 < 0L) {
            throw new IllegalArgumentException("negative timeout");
        }
        if (!this.isCompleted()) {
            this._await(now, timeout2, unit);
        }
        return this;
    }

    private void _await(final long start, final long timeout2, final TimeUnit unit) {
        try {
            ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker(){
                final long duration;
                final Thread waitingThread;
                boolean threadEnqueued;
                {
                    this.duration = unit == null ? -1L : unit.toNanos(timeout2);
                    this.waitingThread = Thread.currentThread();
                    this.threadEnqueued = false;
                }

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public boolean block() {
                    try {
                        if (!this.threadEnqueued) {
                            Object object = FutureImpl.this.lock;
                            synchronized (object) {
                                FutureImpl.this.waiters = (Queue)FutureImpl.this.waiters.enqueue(this.waitingThread);
                            }
                            this.threadEnqueued = true;
                        }
                        if (timeout2 > -1L) {
                            long delta = System.nanoTime() - start;
                            long remainder = this.duration - delta;
                            LockSupport.parkNanos(remainder);
                            if (System.nanoTime() - start > this.duration) {
                                FutureImpl.this.tryComplete(Try.failure(new TimeoutException("timeout after " + timeout2 + " " + unit.name().toLowerCase())));
                            }
                        } else {
                            LockSupport.park();
                        }
                        if (this.waitingThread.isInterrupted()) {
                            FutureImpl.this.tryComplete(Try.failure(new ExecutionException(new InterruptedException())));
                        }
                    }
                    catch (Throwable x) {
                        FutureImpl.this.tryComplete(Try.failure(x));
                    }
                    return FutureImpl.this.isCompleted();
                }

                @Override
                public boolean isReleasable() {
                    return FutureImpl.this.isCompleted();
                }
            });
        }
        catch (Throwable x) {
            this.tryComplete(Try.failure(x));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!this.isCompleted()) {
            Object object = this.lock;
            synchronized (object) {
                if (!this.isCompleted()) {
                    if (mayInterruptIfRunning && this.thread != null) {
                        this.thread.interrupt();
                    }
                    this.cancelled = this.tryComplete(Try.failure(new CancellationException()));
                    return this.cancelled;
                }
            }
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void updateThread() {
        if (!this.isCompleted()) {
            Object object = this.lock;
            synchronized (object) {
                if (!this.isCompleted()) {
                    this.thread = Thread.currentThread();
                    try {
                        this.thread.setUncaughtExceptionHandler((thread2, x) -> this.handleUncaughtException(x));
                    }
                    catch (SecurityException securityException) {
                        // empty catch block
                    }
                }
            }
        }
    }

    @Override
    public Executor executor() {
        return this.executor;
    }

    @Override
    @Deprecated
    public ExecutorService executorService() {
        if (this.executor instanceof ExecutorService) {
            return (ExecutorService)this.executor;
        }
        throw new UnsupportedOperationException("Removed starting with Vavr 0.10.0, use executor() instead.");
    }

    @Override
    public Option<Try<T>> getValue() {
        return this.value;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public boolean isCompleted() {
        return this.value.isDefined();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Future<T> onComplete(Consumer<? super Try<T>> action) {
        Objects.requireNonNull(action, "action is null");
        if (this.isCompleted()) {
            this.perform(action);
        } else {
            Object object = this.lock;
            synchronized (object) {
                if (this.isCompleted()) {
                    this.perform(action);
                } else {
                    this.actions = this.actions.enqueue(action);
                }
            }
        }
        return this;
    }

    @Override
    public String toString() {
        Option<Try<T>> value = this.value;
        String s = value == null || value.isEmpty() ? "?" : value.get().toString();
        return this.stringPrefix() + "(" + s + ")";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    boolean tryComplete(Try<? extends T> value) {
        Queue<Thread> waiters;
        Queue<Consumer<Try<Consumer>>> actions;
        Objects.requireNonNull(value, "value is null");
        if (this.isCompleted()) {
            return false;
        }
        Object object = this.lock;
        synchronized (object) {
            if (this.isCompleted()) {
                actions = null;
                waiters = null;
            } else {
                actions = this.actions;
                waiters = this.waiters;
                this.value = Option.some(Try.narrow(value));
                this.actions = null;
                this.waiters = null;
                this.thread = null;
            }
        }
        if (waiters != null) {
            waiters.forEach(this::unlock);
        }
        if (actions != null) {
            actions.forEach(this::perform);
            return true;
        }
        return false;
    }

    private void perform(Consumer<? super Try<T>> action) {
        try {
            this.executor.execute(() -> action.accept(this.value.get()));
        }
        catch (Throwable x) {
            this.handleUncaughtException(x);
        }
    }

    private void unlock(Thread waiter) {
        try {
            LockSupport.unpark(waiter);
        }
        catch (Throwable x) {
            this.handleUncaughtException(x);
        }
    }

    private void handleUncaughtException(Throwable x) {
        this.tryComplete(Try.failure(x));
    }

    private static interface Computation<T> {
        public void execute(Task.Complete<T> var1, Runnable var2) throws Throwable;
    }
}

