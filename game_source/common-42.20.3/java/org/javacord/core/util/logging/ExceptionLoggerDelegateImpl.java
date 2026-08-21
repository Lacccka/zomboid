/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.logging;

import java.util.Collection;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;
import org.javacord.api.util.logging.internal.ExceptionLoggerDelegate;
import org.javacord.core.util.logging.LoggerUtil;

public class ExceptionLoggerDelegateImpl
implements ExceptionLoggerDelegate {
    private static final Logger logger = LoggerUtil.getLogger(ExceptionLoggerDelegateImpl.class);

    @Override
    public <T> Function<Throwable, T> get(Predicate<Throwable> logFilter, Collection<Class<? extends Throwable>> ignoredThrowableTypes, StackTraceElement[] stackTrace) {
        return throwable -> {
            Throwable unwrappedThrowable = ExceptionLoggerDelegate.unwrapThrowable(throwable);
            if (ignoredThrowableTypes.contains(unwrappedThrowable.getClass()) || logFilter != null && !logFilter.test(unwrappedThrowable)) {
                logger.debug("Suppressed exception {}", (Object)throwable.getMessage());
            } else {
                CompletionException enrichedThrowable = new CompletionException(unwrappedThrowable.getMessage(), unwrappedThrowable);
                enrichedThrowable.setStackTrace(stackTrace);
                logger.error("Caught unhandled exception!", (Throwable)enrichedThrowable);
            }
            return null;
        };
    }

    @Override
    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return (thread2, throwable) -> {
            Supplier[] supplierArray = new Supplier[2];
            supplierArray[0] = thread2::getName;
            supplierArray[1] = () -> ExceptionLoggerDelegate.unwrapThrowable(throwable);
            logger.error("Caught unhandled exception on thread '{}'!", supplierArray);
        };
    }
}

