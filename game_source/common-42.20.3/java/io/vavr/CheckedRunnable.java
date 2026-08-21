/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.CheckedRunnableModule;

@FunctionalInterface
public interface CheckedRunnable {
    public static CheckedRunnable of(CheckedRunnable methodReference) {
        return methodReference;
    }

    public void run() throws Throwable;

    default public Runnable unchecked() {
        return () -> {
            try {
                this.run();
            }
            catch (Throwable x) {
                CheckedRunnableModule.sneakyThrow(x);
            }
        };
    }
}

