/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util.logging.internal;

import java.util.Collection;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ExceptionLoggerDelegate {
    public <T> Function<Throwable, T> get(Predicate<Throwable> var1, Collection<Class<? extends Throwable>> var2, StackTraceElement[] var3);

    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler();

    public static Throwable unwrapThrowable(Throwable throwable) {
        Throwable result = throwable;
        Throwable cause = result.getCause();
        while (result instanceof CompletionException && cause != null) {
            result = cause;
            cause = result.getCause();
        }
        return result instanceof CompletionException ? throwable : result;
    }
}

