/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFactory
implements java.util.concurrent.ThreadFactory {
    private final AtomicInteger counter = new AtomicInteger();
    private final String namePattern;
    private final boolean daemon;

    public ThreadFactory(String namePattern, boolean daemon) {
        this.namePattern = namePattern;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread2 = new Thread(r, String.format(this.namePattern, this.counter.incrementAndGet()));
        thread2.setDaemon(this.daemon);
        return thread2;
    }
}

