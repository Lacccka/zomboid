/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class FutureTasks {
    public static <V> FutureTask<V> run(Callable<V> callable) {
        FutureTask<V> futureTask = new FutureTask<V>(callable);
        futureTask.run();
        return futureTask;
    }

    private FutureTasks() {
    }
}

