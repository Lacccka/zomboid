/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.lang3.concurrent.AbstractConcurrentInitializer;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableSupplier;

public class BackgroundInitializer<T>
extends AbstractConcurrentInitializer<T, Exception> {
    private ExecutorService externalExecutor;
    private ExecutorService executor;
    private Future<T> future;

    public static <T> Builder<BackgroundInitializer<T>, T> builder() {
        return new Builder();
    }

    protected BackgroundInitializer() {
        this(null);
    }

    protected BackgroundInitializer(ExecutorService exec) {
        this.setExternalExecutor(exec);
    }

    private BackgroundInitializer(FailableSupplier<T, ConcurrentException> initializer, FailableConsumer<T, ConcurrentException> closer, ExecutorService exec) {
        super(initializer, closer);
        this.setExternalExecutor(exec);
    }

    private ExecutorService createExecutor() {
        return Executors.newFixedThreadPool(this.getTaskCount());
    }

    private Callable<T> createTask(ExecutorService execDestroy) {
        return new InitializationTask(execDestroy);
    }

    @Override
    public T get() throws ConcurrentException {
        try {
            return this.getFuture().get();
        }
        catch (ExecutionException execex) {
            ConcurrentUtils.handleCause(execex);
            return null;
        }
        catch (InterruptedException iex) {
            Thread.currentThread().interrupt();
            throw new ConcurrentException(iex);
        }
    }

    protected final synchronized ExecutorService getActiveExecutor() {
        return this.executor;
    }

    public final synchronized ExecutorService getExternalExecutor() {
        return this.externalExecutor;
    }

    public synchronized Future<T> getFuture() {
        if (this.future == null) {
            throw new IllegalStateException("start() must be called first!");
        }
        return this.future;
    }

    protected int getTaskCount() {
        return 1;
    }

    @Override
    protected Exception getTypedException(Exception e) {
        return new Exception(e);
    }

    @Override
    public synchronized boolean isInitialized() {
        if (this.future == null || !this.future.isDone()) {
            return false;
        }
        try {
            this.future.get();
            return true;
        }
        catch (InterruptedException | CancellationException | ExecutionException e) {
            return false;
        }
    }

    public synchronized boolean isStarted() {
        return this.future != null;
    }

    public final synchronized void setExternalExecutor(ExecutorService externalExecutor) {
        if (this.isStarted()) {
            throw new IllegalStateException("Cannot set ExecutorService after start()!");
        }
        this.externalExecutor = externalExecutor;
    }

    public synchronized boolean start() {
        if (!this.isStarted()) {
            ExecutorService tempExec;
            this.executor = this.getExternalExecutor();
            if (this.executor == null) {
                this.executor = tempExec = this.createExecutor();
            } else {
                tempExec = null;
            }
            this.future = this.executor.submit(this.createTask(tempExec));
            return true;
        }
        return false;
    }

    public static class Builder<I extends BackgroundInitializer<T>, T>
    extends AbstractConcurrentInitializer.AbstractBuilder<I, T, Builder<I, T>, Exception> {
        private ExecutorService externalExecutor;

        @Override
        public I get() {
            return (I)new BackgroundInitializer(this.getInitializer(), this.getCloser(), this.externalExecutor);
        }

        public Builder<I, T> setExternalExecutor(ExecutorService externalExecutor) {
            this.externalExecutor = externalExecutor;
            return (Builder)this.asThis();
        }
    }

    private final class InitializationTask
    implements Callable<T> {
        private final ExecutorService execFinally;

        InitializationTask(ExecutorService exec) {
            this.execFinally = exec;
        }

        @Override
        public T call() throws Exception {
            try {
                Object t = BackgroundInitializer.this.initialize();
                return t;
            }
            finally {
                if (this.execFinally != null) {
                    this.execFinally.shutdown();
                }
            }
        }
    }
}

