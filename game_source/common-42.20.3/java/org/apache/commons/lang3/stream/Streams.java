/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.stream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.function.Failable;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.function.FailablePredicate;

public class Streams {
    public static <T> FailableStream<T> failableStream(Collection<T> stream) {
        return Streams.failableStream(Streams.of(stream));
    }

    public static <T> FailableStream<T> failableStream(Stream<T> stream) {
        return new FailableStream<T>(stream);
    }

    public static <T> FailableStream<T> failableStream(T value) {
        return Streams.failableStream(Streams.streamOf(value));
    }

    @SafeVarargs
    public static <T> FailableStream<T> failableStream(T ... values2) {
        return Streams.failableStream(Streams.of(values2));
    }

    public static <E> Stream<E> instancesOf(Class<? super E> clazz, Collection<? super E> collection) {
        return Streams.instancesOf(clazz, Streams.of(collection));
    }

    private static <E> Stream<E> instancesOf(Class<? super E> clazz, Stream<?> stream) {
        return Streams.of(stream).filter(clazz::isInstance);
    }

    public static <E> Stream<E> nonNull(Collection<E> collection) {
        return Streams.of(collection).filter(Objects::nonNull);
    }

    public static <E> Stream<E> nonNull(E array) {
        return Streams.nonNull(Streams.streamOf(array));
    }

    @SafeVarargs
    public static <E> Stream<E> nonNull(E ... array) {
        return Streams.nonNull(Streams.of(array));
    }

    public static <E> Stream<E> nonNull(Stream<E> stream) {
        return Streams.of(stream).filter(Objects::nonNull);
    }

    public static <E> Stream<E> of(Collection<E> collection) {
        return collection == null ? Stream.empty() : collection.stream();
    }

    public static <E> Stream<E> of(Enumeration<E> enumeration) {
        return StreamSupport.stream(new EnumerationSpliterator<E>(Long.MAX_VALUE, 16, enumeration), false);
    }

    public static <E> Stream<E> of(Iterable<E> iterable) {
        return iterable == null ? Stream.empty() : StreamSupport.stream(iterable.spliterator(), false);
    }

    public static <E> Stream<E> of(Iterator<E> iterator2) {
        return iterator2 == null ? Stream.empty() : StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator2, 16), false);
    }

    private static <E> Stream<E> of(Stream<E> stream) {
        return stream == null ? Stream.empty() : stream;
    }

    @SafeVarargs
    public static <T> Stream<T> of(T ... values2) {
        return values2 == null ? Stream.empty() : Stream.of(values2);
    }

    @Deprecated
    public static <E> FailableStream<E> stream(Collection<E> collection) {
        return Streams.failableStream(collection);
    }

    @Deprecated
    public static <T> FailableStream<T> stream(Stream<T> stream) {
        return Streams.failableStream(stream);
    }

    private static <T> Stream<T> streamOf(T value) {
        return value == null ? Stream.empty() : Stream.of(value);
    }

    public static <T> Collector<T, ?, T[]> toArray(Class<T> elementType) {
        return new ArrayCollector<T>(elementType);
    }

    @Deprecated
    public Streams() {
    }

    public static class FailableStream<T> {
        private Stream<T> stream;
        private boolean terminated;

        public FailableStream(Stream<T> stream) {
            this.stream = stream;
        }

        public boolean allMatch(FailablePredicate<T, ?> predicate) {
            this.assertNotTerminated();
            return this.stream().allMatch(Failable.asPredicate(predicate));
        }

        public boolean anyMatch(FailablePredicate<T, ?> predicate) {
            this.assertNotTerminated();
            return this.stream().anyMatch(Failable.asPredicate(predicate));
        }

        protected void assertNotTerminated() {
            if (this.terminated) {
                throw new IllegalStateException("This stream is already terminated.");
            }
        }

        public <A, R> R collect(Collector<? super T, A, R> collector) {
            this.makeTerminated();
            return this.stream().collect(collector);
        }

        public <A, R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
            this.makeTerminated();
            return this.stream().collect(supplier, accumulator, combiner);
        }

        public FailableStream<T> filter(FailablePredicate<T, ?> predicate) {
            this.assertNotTerminated();
            this.stream = this.stream.filter(Failable.asPredicate(predicate));
            return this;
        }

        public void forEach(FailableConsumer<T, ?> action) {
            this.makeTerminated();
            this.stream().forEach(Failable.asConsumer(action));
        }

        protected void makeTerminated() {
            this.assertNotTerminated();
            this.terminated = true;
        }

        public <R> FailableStream<R> map(FailableFunction<T, R, ?> mapper) {
            this.assertNotTerminated();
            return new FailableStream<R>(this.stream.map(Failable.asFunction(mapper)));
        }

        public T reduce(T identity, BinaryOperator<T> accumulator) {
            this.makeTerminated();
            return this.stream().reduce(identity, accumulator);
        }

        public Stream<T> stream() {
            return this.stream;
        }
    }

    private static final class EnumerationSpliterator<T>
    extends Spliterators.AbstractSpliterator<T> {
        private final Enumeration<T> enumeration;

        protected EnumerationSpliterator(long estimatedSize, int additionalCharacteristics, Enumeration<T> enumeration) {
            super(estimatedSize, additionalCharacteristics);
            this.enumeration = Objects.requireNonNull(enumeration, "enumeration");
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            while (this.enumeration.hasMoreElements()) {
                this.next(action);
            }
        }

        private boolean next(Consumer<? super T> action) {
            action.accept(this.enumeration.nextElement());
            return true;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            return this.enumeration.hasMoreElements() && this.next(action);
        }
    }

    public static class ArrayCollector<E>
    implements Collector<E, List<E>, E[]> {
        private static final Set<Collector.Characteristics> characteristics = Collections.emptySet();
        private final Class<E> elementType;

        public ArrayCollector(Class<E> elementType) {
            this.elementType = Objects.requireNonNull(elementType, "elementType");
        }

        @Override
        public BiConsumer<List<E>, E> accumulator() {
            return List::add;
        }

        @Override
        public Set<Collector.Characteristics> characteristics() {
            return characteristics;
        }

        @Override
        public BinaryOperator<List<E>> combiner() {
            return (left, right) -> {
                left.addAll(right);
                return left;
            };
        }

        @Override
        public Function<List<E>, E[]> finisher() {
            return list -> list.toArray(ArrayUtils.newInstance(this.elementType, list.size()));
        }

        @Override
        public Supplier<List<E>> supplier() {
            return ArrayList::new;
        }
    }
}

