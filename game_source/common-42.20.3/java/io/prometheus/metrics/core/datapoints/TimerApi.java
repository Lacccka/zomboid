/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.core.datapoints;

import io.prometheus.metrics.core.datapoints.Timer;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public interface TimerApi {
    public Timer startTimer();

    default public void time(Runnable func) {
        try (Timer ignored = this.startTimer();){
            func.run();
        }
    }

    default public <T> T time(Supplier<T> func) {
        try (Timer ignored = this.startTimer();){
            T t = func.get();
            return t;
        }
    }

    default public <T> T timeChecked(Callable<T> func) throws Exception {
        try (Timer ignored = this.startTimer();){
            T t = func.call();
            return t;
        }
    }
}

