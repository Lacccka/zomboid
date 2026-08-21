/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.commons.io.IOExceptionList;
import org.apache.commons.io.IOIndexedException;
import org.apache.commons.io.function.IOStreams;
import org.apache.commons.io.function.Uncheck;

@FunctionalInterface
public interface IOConsumer<T> {
    public static final IOConsumer<?> NOOP_IO_CONSUMER = t -> {};

    public static <T> void forAll(IOConsumer<T> action, Iterable<T> iterable) throws IOExceptionList {
        IOStreams.forAll(IOStreams.of(iterable), action);
    }

    public static <T> void forAll(IOConsumer<T> action, Stream<T> stream) throws IOExceptionList {
        IOStreams.forAll(stream, action, IOIndexedException::new);
    }

    @SafeVarargs
    public static <T> void forAll(IOConsumer<T> action, T ... array) throws IOExceptionList {
        IOStreams.forAll(IOStreams.of(array), action);
    }

    public static <T> void forEach(Iterable<T> iterable, IOConsumer<T> action) throws IOException {
        IOStreams.forEach(IOStreams.of(iterable), action);
    }

    public static <T> void forEach(Stream<T> stream, IOConsumer<T> action) throws IOException {
        IOStreams.forEach(stream, action);
    }

    public static <T> void forEach(T[] array, IOConsumer<T> action) throws IOException {
        IOStreams.forEach(IOStreams.of(array), action);
    }

    public static <T> IOConsumer<T> noop() {
        return NOOP_IO_CONSUMER;
    }

    public void accept(T var1) throws IOException;

    default public IOConsumer<T> andThen(IOConsumer<? super T> after) {
        Objects.requireNonNull(after, "after");
        return t -> {
            this.accept(t);
            after.accept(t);
        };
    }

    default public Consumer<T> asConsumer() {
        return t -> Uncheck.accept(this, t);
    }
}

