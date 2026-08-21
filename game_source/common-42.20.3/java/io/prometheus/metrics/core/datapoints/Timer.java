/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.datapoints;

import io.prometheus.metrics.model.snapshots.Unit;
import java.io.Closeable;
import java.util.function.DoubleConsumer;

public class Timer
implements Closeable {
    private final DoubleConsumer observeFunction;
    private final long startTimeNanos = System.nanoTime();

    Timer(DoubleConsumer observeFunction) {
        this.observeFunction = observeFunction;
    }

    public double observeDuration() {
        double elapsed = Unit.nanosToSeconds(System.nanoTime() - this.startTimeNanos);
        this.observeFunction.accept(elapsed);
        return elapsed;
    }

    @Override
    public void close() {
        this.observeDuration();
    }
}

