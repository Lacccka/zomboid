/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Supplier;
import org.apache.commons.io.function.IOBiConsumer;
import org.apache.commons.io.function.IOBiFunction;
import org.apache.commons.io.function.IOComparator;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.function.IOFunction;
import org.apache.commons.io.function.IOIntSupplier;
import org.apache.commons.io.function.IOLongSupplier;
import org.apache.commons.io.function.IOPredicate;
import org.apache.commons.io.function.IOQuadFunction;
import org.apache.commons.io.function.IORunnable;
import org.apache.commons.io.function.IOSupplier;
import org.apache.commons.io.function.IOTriConsumer;
import org.apache.commons.io.function.IOTriFunction;

public final class Uncheck {
    public static <T, U> void accept(IOBiConsumer<T, U> consumer, T t, U u) {
        try {
            consumer.accept(t, u);
        }
        catch (IOException e) {
            throw Uncheck.wrap(e);
        }
    }

    public static <T> void accept(IOConsumer<T> consumer, T t) {
        try {
            consumer.accept(t);
        }
        catch (IOException e) {
            throw Uncheck.wrap(e);
        }
    }

    public static <T, U, V> void accept(IOTriConsumer<T, U, V> consumer, T t, U u, V v) {
        try {
            consumer.accept(t, u, v);
        }
        catch (IOException e) {
            throw Uncheck.wrap(e);
        }
    }

    public static <T, U, R> R apply(IOBiFunction<T, U, R> function, T t, U u) {
        try {
            return function.apply(t, u);
        }
        catch (IOException e) {
            throw Uncheck.wrap(e);
        }
    }

    public static <T, R> R apply(IOFunction<T, R> function, T t) {
        try {
            return function.apply(t);
        }
        catch (IOException e) {
            throw Uncheck.wrap(e);
        }
    }

    public static <T, U, V, W, R> R apply(IOQuadFunction<T, U, V, W, R> function, T t, U u, V v, W w) {
        try {
            return function.apply(t, u, v, w);
        }
        catch (IOException e) {
            throw Uncheck.wrap(e);
        }
    }

    public static <T, U, V, R> R apply(IOTriFunction<T, U, V, R> function, T t, U u, V v) {
        try {
            return function.apply(t, u, v);
        }
        catch (IOException e) {
            throw Uncheck.wrap(e);
        }
    }

    public static <T> int compare(IOComparator<T> comparator, T t, T u) {
        try {
            return comparator.compare(t, u);
        }
        catch (IOException e) {
            throw Uncheck.wrap(e);
        }
    }

    public static <T> T get(IOSupplier<T> supplier) {
        try {
            return supplier.get();
        }
        catch (IOException e) {
            throw Uncheck.wrap(e);
        }
    }

    public static <T> T get(IOSupplier<T> supplier, Supplier<String> message) {
        try {
            return supplier.get();
        }
        catch (IOException e) {
            throw Uncheck.wrap(e, message);
        }
    }

    public static int getAsInt(IOIntSupplier supplier) {
        try {
            return supplier.getAsInt();
        }
        catch (IOException e) {
            throw Uncheck.wrap(e);
        }
    }

    public static int getAsInt(IOIntSupplier supplier, Supplier<String> message) {
        try {
            return supplier.getAsInt();
        }
        catch (IOException e) {
            throw Uncheck.wrap(e, message);
        }
    }

    public static long getAsLong(IOLongSupplier supplier) {
        try {
            return supplier.getAsLong();
        }
        catch (IOException e) {
            throw Uncheck.wrap(e);
        }
    }

    public static long getAsLong(IOLongSupplier supplier, Supplier<String> message) {
        try {
            return supplier.getAsLong();
        }
        catch (IOException e) {
            throw Uncheck.wrap(e, message);
        }
    }

    public static void run(IORunnable runnable2) {
        try {
            runnable2.run();
        }
        catch (IOException e) {
            throw Uncheck.wrap(e);
        }
    }

    public static void run(IORunnable runnable2, Supplier<String> message) {
        try {
            runnable2.run();
        }
        catch (IOException e) {
            throw Uncheck.wrap(e, message);
        }
    }

    public static <T> boolean test(IOPredicate<T> predicate, T t) {
        try {
            return predicate.test(t);
        }
        catch (IOException e) {
            throw Uncheck.wrap(e);
        }
    }

    private static UncheckedIOException wrap(IOException e) {
        return new UncheckedIOException(e);
    }

    private static UncheckedIOException wrap(IOException e, Supplier<String> message) {
        return new UncheckedIOException(message.get(), e);
    }

    private Uncheck() {
    }
}

