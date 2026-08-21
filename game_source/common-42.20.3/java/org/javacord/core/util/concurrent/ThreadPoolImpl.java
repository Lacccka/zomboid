/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.concurrent;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.javacord.api.util.concurrent.ThreadPool;
import org.javacord.core.util.concurrent.ThreadFactory;

public class ThreadPoolImpl
implements ThreadPool {
    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE = Integer.MAX_VALUE;
    private static final int KEEP_ALIVE_TIME = 60;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    private final ExecutorService executorService = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60L, TIME_UNIT, new SynchronousQueue<Runnable>(), new ThreadFactory("Javacord - Central ExecutorService - %d", false));
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory("Javacord - Central Scheduler - %d", false));
    private final ScheduledExecutorService daemonScheduler = Executors.newScheduledThreadPool(1, new ThreadFactory("Javacord - Central Daemon Scheduler - %d", true));
    private final ConcurrentHashMap<String, ExecutorService> executorServiceSingleThreads = new ConcurrentHashMap();

    public void shutdown() {
        this.executorService.shutdown();
        this.scheduler.shutdown();
        this.daemonScheduler.shutdown();
        this.executorServiceSingleThreads.values().forEach(ExecutorService::shutdown);
    }

    @Override
    public ExecutorService getExecutorService() {
        return this.executorService;
    }

    @Override
    public ScheduledExecutorService getScheduler() {
        return this.scheduler;
    }

    @Override
    public ScheduledExecutorService getDaemonScheduler() {
        return this.daemonScheduler;
    }

    @Override
    public ExecutorService getSingleThreadExecutorService(String threadName) {
        return this.executorServiceSingleThreads.computeIfAbsent(threadName, key -> new ThreadPoolExecutor(0, 1, 60L, TIME_UNIT, new LinkedBlockingQueue<Runnable>(), new ThreadFactory("Javacord - " + threadName, false)));
    }

    @Override
    public ExecutorService getSingleDaemonThreadExecutorService(String threadName) {
        return this.executorServiceSingleThreads.computeIfAbsent(threadName, key -> new ThreadPoolExecutor(0, 1, 60L, TIME_UNIT, new LinkedBlockingQueue<Runnable>(), new ThreadFactory("Javacord - " + threadName, true)));
    }

    @Override
    public Optional<ExecutorService> removeAndShutdownSingleThreadExecutorService(String threadName) {
        ExecutorService executorService = this.executorServiceSingleThreads.remove(threadName);
        if (executorService != null) {
            executorService.shutdown();
        }
        return Optional.ofNullable(executorService);
    }

    @Override
    public <T> CompletableFuture<T> runAfter(Supplier<CompletableFuture<T>> task, long duration, TimeUnit unit) {
        CompletableFuture future = new CompletableFuture();
        this.getDaemonScheduler().schedule(() -> ((CompletableFuture)task.get()).whenComplete((result, ex) -> {
            if (ex != null) {
                future.completeExceptionally((Throwable)ex);
            } else {
                future.complete(result);
            }
        }), duration, unit);
        return future;
    }
}

