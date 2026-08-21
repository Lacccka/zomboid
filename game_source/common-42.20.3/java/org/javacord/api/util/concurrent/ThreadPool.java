/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util.concurrent;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface ThreadPool {
    public ExecutorService getExecutorService();

    public ScheduledExecutorService getScheduler();

    public ScheduledExecutorService getDaemonScheduler();

    public ExecutorService getSingleThreadExecutorService(String var1);

    public ExecutorService getSingleDaemonThreadExecutorService(String var1);

    public Optional<ExecutorService> removeAndShutdownSingleThreadExecutorService(String var1);

    public <T> CompletableFuture<T> runAfter(Supplier<CompletableFuture<T>> var1, long var2, TimeUnit var4);
}

