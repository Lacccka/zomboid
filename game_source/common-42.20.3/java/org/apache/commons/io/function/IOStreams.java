/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.io.IOExceptionList;
import org.apache.commons.io.IOIndexedException;
import org.apache.commons.io.function.Erase;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.function.IOStream;

final class IOStreams {
    static final Object NONE = new Object();

    static <T> void forAll(Stream<T> stream, IOConsumer<T> action) throws IOExceptionList {
        IOStreams.forAll(stream, action, (i, e) -> e);
    }

    static <T> void forAll(Stream<T> stream, IOConsumer<T> action, BiFunction<Integer, IOException, IOException> exSupplier) throws IOExceptionList {
        IOStream.adapt(stream).forAll(action, IOIndexedException::new);
    }

    static <T> void forEach(Stream<T> stream, IOConsumer<T> action) throws IOException {
        IOConsumer actualAction = IOStreams.toIOConsumer(action);
        IOStreams.of(stream).forEach(e -> Erase.accept(actualAction, e));
    }

    static <T> Stream<T> of(Iterable<T> values2) {
        return values2 == null ? Stream.empty() : StreamSupport.stream(values2.spliterator(), false);
    }

    static <T> Stream<T> of(Stream<T> stream) {
        return stream == null ? Stream.empty() : stream;
    }

    @SafeVarargs
    static <T> Stream<T> of(T ... values2) {
        return values2 == null ? Stream.empty() : Stream.of(values2);
    }

    static <T> IOConsumer<T> toIOConsumer(IOConsumer<T> action) {
        return action != null ? action : IOConsumer.noop();
    }

    private IOStreams() {
    }
}

