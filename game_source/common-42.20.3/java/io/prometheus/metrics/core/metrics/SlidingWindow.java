/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.metrics;

import java.lang.reflect.Array;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Supplier;

public class SlidingWindow<T> {
    private final Supplier<T> constructor;
    private final ObjDoubleConsumer<T> observeFunction;
    private final T[] ringBuffer;
    private int currentBucket;
    private long lastRotateTimestampMillis;
    private final long durationBetweenRotatesMillis;
    private final LongSupplier currentTimeMillis;

    public SlidingWindow(Class<T> clazz, Supplier<T> constructor, ObjDoubleConsumer<T> observeFunction, long maxAgeSeconds, int ageBuckets) {
        this(clazz, constructor, observeFunction, maxAgeSeconds, ageBuckets, System::currentTimeMillis);
    }

    SlidingWindow(Class<T> clazz, Supplier<T> constructor, ObjDoubleConsumer<T> observeFunction, long maxAgeSeconds, int ageBuckets, LongSupplier currentTimeMillis) {
        this.constructor = constructor;
        this.observeFunction = observeFunction;
        this.ringBuffer = (Object[])Array.newInstance(clazz, ageBuckets);
        for (int i = 0; i < this.ringBuffer.length; ++i) {
            this.ringBuffer[i] = constructor.get();
        }
        this.currentBucket = 0;
        this.lastRotateTimestampMillis = currentTimeMillis.getAsLong();
        this.durationBetweenRotatesMillis = TimeUnit.SECONDS.toMillis(maxAgeSeconds) / (long)ageBuckets;
        this.currentTimeMillis = currentTimeMillis;
    }

    public synchronized T current() {
        return this.rotate();
    }

    public synchronized void observe(double value) {
        this.rotate();
        for (T t : this.ringBuffer) {
            this.observeFunction.accept(t, value);
        }
    }

    private T rotate() {
        long timeSinceLastRotateMillis = this.currentTimeMillis.getAsLong() - this.lastRotateTimestampMillis;
        while (timeSinceLastRotateMillis > this.durationBetweenRotatesMillis) {
            this.ringBuffer[this.currentBucket] = this.constructor.get();
            if (++this.currentBucket >= this.ringBuffer.length) {
                this.currentBucket = 0;
            }
            timeSinceLastRotateMillis -= this.durationBetweenRotatesMillis;
            this.lastRotateTimestampMillis += this.durationBetweenRotatesMillis;
        }
        return this.ringBuffer[this.currentBucket];
    }
}

