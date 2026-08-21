/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util.ratelimit;

import java.time.Duration;
import org.javacord.api.util.ratelimit.Ratelimiter;

public class LocalRatelimiter
implements Ratelimiter {
    private volatile long nextResetNanos;
    private volatile int remainingQuota;
    private final int amount;
    private final Duration bucketDuration;

    public LocalRatelimiter(int amount, Duration bucketDuration) {
        this.amount = amount;
        this.bucketDuration = bucketDuration;
    }

    public int getAmount() {
        return this.amount;
    }

    public Duration getBucketDuration() {
        return this.bucketDuration;
    }

    public long getNextResetNanos() {
        return this.nextResetNanos;
    }

    public int getRemainingQuota() {
        return this.remainingQuota;
    }

    @Override
    public synchronized void requestQuota() throws InterruptedException {
        if (this.remainingQuota <= 0) {
            long sleepTime;
            while ((sleepTime = this.calculateSleepTime()) > 0L) {
                Thread.sleep(sleepTime / 1000000L, (int)(sleepTime % 1000000L));
            }
        }
        if (System.nanoTime() >= this.nextResetNanos) {
            this.remainingQuota = this.amount;
            try {
                this.nextResetNanos = System.nanoTime() + this.bucketDuration.toNanos();
            }
            catch (ArithmeticException e) {
                this.nextResetNanos = Long.MAX_VALUE;
            }
        }
        --this.remainingQuota;
    }

    private long calculateSleepTime() {
        return this.nextResetNanos - System.nanoTime();
    }
}

