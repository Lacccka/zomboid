/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io;

import java.time.Duration;
import org.apache.commons.io.ThreadUtils;

final class ThreadMonitor
implements Runnable {
    private final Thread thread;
    private final Duration timeout;

    static Thread start(Duration timeout2) {
        return ThreadMonitor.start(Thread.currentThread(), timeout2);
    }

    static Thread start(Thread thread2, Duration timeout2) {
        if (timeout2.isZero() || timeout2.isNegative()) {
            return null;
        }
        Thread monitor = new Thread(new ThreadMonitor(thread2, timeout2), ThreadMonitor.class.getSimpleName());
        monitor.setDaemon(true);
        monitor.start();
        return monitor;
    }

    static void stop(Thread thread2) {
        if (thread2 != null) {
            thread2.interrupt();
        }
    }

    private ThreadMonitor(Thread thread2, Duration timeout2) {
        this.thread = thread2;
        this.timeout = timeout2;
    }

    @Override
    public void run() {
        try {
            ThreadUtils.sleep(this.timeout);
            this.thread.interrupt();
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }
}

