/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.concurrent;

import io.vavr.control.Try;

@Deprecated
@FunctionalInterface
public interface Task<T> {
    public void run(Complete<T> var1) throws Throwable;

    @FunctionalInterface
    public static interface Complete<T> {
        public boolean with(Try<? extends T> var1);
    }
}

