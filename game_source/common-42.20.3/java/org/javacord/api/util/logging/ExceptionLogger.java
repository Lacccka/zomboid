/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util.logging;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.javacord.api.util.internal.DelegateFactory;
import org.javacord.api.util.logging.internal.ExceptionLoggerDelegate;

public class ExceptionLogger {
    private static final ExceptionLoggerDelegate delegate = DelegateFactory.getExceptionLoggerDelegate();

    private ExceptionLogger() {
        throw new UnsupportedOperationException();
    }

    @SafeVarargs
    public static Consumer<Throwable> getConsumer(Predicate<Throwable> logFilter, Class<? extends Throwable> ... ignoredThrowableTypes) {
        return ExceptionLogger.get(logFilter, ignoredThrowableTypes)::apply;
    }

    @SafeVarargs
    public static Consumer<Throwable> getConsumer(Class<? extends Throwable> ... ignoredThrowableTypes) {
        return ExceptionLogger.getConsumer(null, ignoredThrowableTypes);
    }

    @SafeVarargs
    public static <T> Function<Throwable, T> get(Predicate<Throwable> logFilter, Class<? extends Throwable> ... ignoredThrowableTypes) {
        StackTraceElement[] stackTrace = (StackTraceElement[])Arrays.stream(Thread.currentThread().getStackTrace()).filter(element -> !element.getClassName().equals(ExceptionLogger.class.getName())).filter(element -> !element.getClassName().equals(Thread.class.getName()) || !"getStackTrace".equals(element.getMethodName())).toArray(StackTraceElement[]::new);
        List<Class<? extends Throwable>> ignoredThrowableTypesList = Arrays.asList(ignoredThrowableTypes);
        return delegate.get(logFilter, ignoredThrowableTypesList, stackTrace);
    }

    @SafeVarargs
    public static <T> Function<Throwable, T> get(Class<? extends Throwable> ... ignoredThrowableTypes) {
        return ExceptionLogger.get(null, ignoredThrowableTypes);
    }

    public static Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return delegate.getUncaughtExceptionHandler();
    }

    public static Throwable unwrapThrowable(Throwable throwable) {
        return ExceptionLoggerDelegate.unwrapThrowable(throwable);
    }
}

