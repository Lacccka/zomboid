/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());

    public static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return executor.schedule(command, delay, unit);
    }

    public static void awaitInitialization() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Scheduler.schedule(latch::countDown, 0L, TimeUnit.MILLISECONDS);
        latch.await();
    }

    private static class DaemonThreadFactory
    implements ThreadFactory {
        private static int threadNum;

        private DaemonThreadFactory() {
        }

        private static synchronized int nextThreadNum() {
            return threadNum++;
        }

        @Override
        public Thread newThread(Runnable runnable2) {
            Thread thread2 = new Thread(runnable2, "prometheus-metrics-scheduler-" + DaemonThreadFactory.nextThreadNum());
            thread2.setDaemon(true);
            return thread2;
        }
    }
}

