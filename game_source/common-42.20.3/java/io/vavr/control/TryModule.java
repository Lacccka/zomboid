/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.control;

interface TryModule {
    public static boolean isFatal(Throwable throwable) {
        return throwable instanceof InterruptedException || throwable instanceof LinkageError || throwable instanceof ThreadDeath || throwable instanceof VirtualMachineError;
    }

    public static <T extends Throwable, R> R sneakyThrow(Throwable t) throws T {
        throw t;
    }
}

