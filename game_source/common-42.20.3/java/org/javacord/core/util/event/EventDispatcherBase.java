/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.logging.LoggerUtil;

public abstract class EventDispatcherBase {
    private static final Logger logger = LoggerUtil.getLogger(EventDispatcherBase.class);
    private static final long MAX_EXECUTION_TIME = TimeUnit.MINUTES.toNanos(2L);
    private static final long INFO_WARNING_DELAY = TimeUnit.SECONDS.toNanos(10L);
    private static final long DEBUG_WARNING_DELAY = TimeUnit.MILLISECONDS.toNanos(500L);
    private static final long EXECUTION_TIME_CHECKING_INTERVAL = TimeUnit.MILLISECONDS.toNanos(200L);
    private volatile boolean executionTimeCheckingEnabled = true;
    private final DiscordApiImpl api;
    private final Map<DispatchQueueSelector, Queue<Runnable>> queuedListenerTasks = Collections.synchronizedMap(new HashMap());
    private final Set<DispatchQueueSelector> runningListeners = Collections.synchronizedSet(new HashSet());
    private final Map<AtomicReference<Future<?>>, Object[]> activeListeners = Collections.synchronizedMap(new HashMap());
    private final Map<AtomicReference<Future<?>>, Long> alreadyCanceledListeners = new ConcurrentHashMap();

    protected EventDispatcherBase(DiscordApiImpl api) {
        this.api = api;
        this.queuedListenerTasks.put(null, new ConcurrentLinkedQueue());
        api.getThreadPool().getScheduler().scheduleAtFixedRate(() -> {
            try {
                if (!this.executionTimeCheckingEnabled) {
                    return;
                }
                Map<AtomicReference<Future<?>>, Object[]> map = this.activeListeners;
                synchronized (map) {
                    long currentNanoTime = System.nanoTime();
                    for (Map.Entry<AtomicReference<Future<?>>, Object[]> entry : this.activeListeners.entrySet()) {
                        long difference = currentNanoTime - (Long)entry.getValue()[0];
                        DispatchQueueSelector queueSelector = (DispatchQueueSelector)entry.getValue()[1];
                        if (difference > DEBUG_WARNING_DELAY && difference <= DEBUG_WARNING_DELAY + EXECUTION_TIME_CHECKING_INTERVAL) {
                            logger.debug("Detected {} which is now running for over {} ms ({} ms). This is an unusually long execution time for a listener task. Make sure to not do any heavy computations in listener threads!", () -> this.getThreadType(queueSelector), () -> TimeUnit.NANOSECONDS.toMillis(DEBUG_WARNING_DELAY), () -> TimeUnit.NANOSECONDS.toMillis(difference));
                        }
                        if (difference > INFO_WARNING_DELAY && difference <= INFO_WARNING_DELAY + EXECUTION_TIME_CHECKING_INTERVAL) {
                            logger.warn("Detected {} which is now running for over {} seconds ({} ms). This is a very unusually long execution time for a listener task. Make sure to not do any heavy computations in listener threads!", () -> this.getThreadType(queueSelector), () -> TimeUnit.NANOSECONDS.toSeconds(INFO_WARNING_DELAY), () -> TimeUnit.NANOSECONDS.toMillis(difference));
                        }
                        if (difference <= MAX_EXECUTION_TIME) continue;
                        AtomicReference<Future<?>> listener = entry.getKey();
                        this.alreadyCanceledListeners.compute(listener, (l, lastWarning) -> {
                            if (lastWarning == null) {
                                ((Future)listener.get()).cancel(true);
                                logger.error("Interrupted {}, because it was running over {} seconds! This was most likely caused by a deadlock or very heavy computation/blocking operations in the listener thread. Make sure to not block listener threads!", () -> this.getThreadType(queueSelector), () -> TimeUnit.NANOSECONDS.toSeconds(MAX_EXECUTION_TIME));
                                return currentNanoTime;
                            }
                            if (currentNanoTime - lastWarning > INFO_WARNING_DELAY) {
                                logger.error("Interrupted {} previously but the listener did not react to being interrupted! This is most likely caused by a deadlock or very heavy computation in the listener thread. Make sure to not block listener threads!", () -> this.getThreadType(queueSelector));
                                return currentNanoTime;
                            }
                            return lastWarning;
                        });
                    }
                }
            }
            catch (Throwable t) {
                logger.error("Failed to check execution times!", t);
            }
        }, 200L, 200L, TimeUnit.MILLISECONDS);
    }

    protected DiscordApiImpl getApi() {
        return this.api;
    }

    public void setExecutionTimeCheckingEnabled(boolean enable) {
        this.executionTimeCheckingEnabled = enable;
    }

    protected <T> void dispatchEvent(DispatchQueueSelector queueSelector, List<T> listeners, Consumer<T> consumer) {
        if (!this.api.canDispatchEvents()) {
            return;
        }
        this.api.getThreadPool().getSingleThreadExecutorService("Event Dispatch Queues Manager").submit(() -> {
            if (queueSelector != null) {
                Queue<Runnable> objectIndependentQueue = this.queuedListenerTasks.get(null);
                while (!objectIndependentQueue.isEmpty()) {
                    try {
                        Map<DispatchQueueSelector, Queue<Runnable>> map = this.queuedListenerTasks;
                        synchronized (map) {
                            this.queuedListenerTasks.wait(5000L);
                        }
                    }
                    catch (InterruptedException interruptedException) {
                    }
                }
            }
            if (!listeners.isEmpty()) {
                Map<DispatchQueueSelector, Queue<Runnable>> map = this.queuedListenerTasks;
                synchronized (map) {
                    Queue queue = this.queuedListenerTasks.computeIfAbsent(queueSelector, o -> new ConcurrentLinkedQueue());
                    listeners.forEach(listener -> queue.add(() -> consumer.accept(listener)));
                }
            }
            this.checkRunningListenersAndStartIfPossible(queueSelector);
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void checkRunningListenersAndStartIfPossible(DispatchQueueSelector queueSelector) {
        Map<DispatchQueueSelector, Queue<Runnable>> map = this.queuedListenerTasks;
        synchronized (map) {
            Queue<Runnable> queue;
            Queue<Runnable> queue2 = queue = queueSelector == null ? null : this.queuedListenerTasks.get(queueSelector);
            if (queue == null || queue.isEmpty()) {
                if (queueSelector != null) {
                    this.queuedListenerTasks.remove(queueSelector);
                }
                if (this.queuedListenerTasks.get(null).isEmpty()) {
                    return;
                }
                boolean moreObjectDependentTasks = this.queuedListenerTasks.entrySet().stream().filter(entry -> !((Queue)entry.getValue()).isEmpty()).anyMatch(entry -> entry.getKey() != null);
                if (moreObjectDependentTasks || !this.runningListeners.isEmpty()) {
                    return;
                }
                queueSelector = null;
                queue = this.queuedListenerTasks.get(null);
            }
            DispatchQueueSelector finalQueueSelector = queueSelector;
            Queue<Runnable> taskQueue = queue;
            if (!queue.isEmpty() && this.runningListeners.add(finalQueueSelector)) {
                AtomicReference activeListener = new AtomicReference();
                activeListener.set(this.api.getThreadPool().getExecutorService().submit(() -> {
                    if (finalQueueSelector instanceof ServerImpl) {
                        Object serverReadyNotifier = new Object();
                        ((ServerImpl)finalQueueSelector).addServerReadyConsumer(s -> {
                            Object object = serverReadyNotifier;
                            synchronized (object) {
                                serverReadyNotifier.notifyAll();
                            }
                        });
                        while (!((ServerImpl)finalQueueSelector).isReady()) {
                            try {
                                Object object = serverReadyNotifier;
                                synchronized (object) {
                                    serverReadyNotifier.wait(5000L);
                                }
                            }
                            catch (InterruptedException interruptedException) {
                            }
                        }
                    }
                    this.activeListeners.put(activeListener, new Object[]{System.nanoTime(), finalQueueSelector});
                    try {
                        ((Runnable)taskQueue.poll()).run();
                    }
                    catch (Throwable t) {
                        logger.error("Unhandled exception in {}!", () -> this.getThreadType(finalQueueSelector), () -> t);
                    }
                    this.activeListeners.remove(activeListener);
                    this.alreadyCanceledListeners.remove(activeListener);
                    this.runningListeners.remove(finalQueueSelector);
                    Map<DispatchQueueSelector, Queue<Runnable>> map = this.queuedListenerTasks;
                    synchronized (map) {
                        Queue<Runnable> remainingQueue;
                        if (finalQueueSelector != null && (remainingQueue = this.queuedListenerTasks.get(finalQueueSelector)) != null && remainingQueue.isEmpty()) {
                            this.queuedListenerTasks.remove(finalQueueSelector);
                        }
                        this.queuedListenerTasks.notifyAll();
                    }
                    this.checkRunningListenersAndStartIfPossible(finalQueueSelector);
                }));
            }
        }
    }

    private String getThreadType(DispatchQueueSelector queueSelector) {
        String threadType = queueSelector instanceof DiscordApi ? "a global listener thread" : (queueSelector == null ? "a connection listener thread" : String.format("a listener thread for %s", queueSelector));
        return threadType;
    }
}

