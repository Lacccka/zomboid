/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.io.IOExceptionList;
import org.apache.commons.io.function.Erase;
import org.apache.commons.io.function.IOBaseStream;
import org.apache.commons.io.function.IOBiConsumer;
import org.apache.commons.io.function.IOBiFunction;
import org.apache.commons.io.function.IOBinaryOperator;
import org.apache.commons.io.function.IOComparator;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.function.IOFunction;
import org.apache.commons.io.function.IOPredicate;
import org.apache.commons.io.function.IOStreamAdapter;
import org.apache.commons.io.function.IOStreams;
import org.apache.commons.io.function.IOSupplier;
import org.apache.commons.io.function.IOUnaryOperator;

public interface IOStream<T>
extends IOBaseStream<T, IOStream<T>, Stream<T>> {
    public static <T> IOStream<T> adapt(Stream<T> stream) {
        return IOStreamAdapter.adapt(stream);
    }

    public static <T> IOStream<T> empty() {
        return IOStreamAdapter.adapt(Stream.empty());
    }

    public static <T> IOStream<T> iterate(final T seed, final IOUnaryOperator<T> f) {
        Objects.requireNonNull(f);
        Iterator iterator2 = new Iterator<T>(){
            T t = IOStreams.NONE;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() throws NoSuchElementException {
                try {
                    this.t = this.t == IOStreams.NONE ? seed : f.apply(this.t);
                    return this.t;
                }
                catch (IOException e) {
                    NoSuchElementException nsee = new NoSuchElementException();
                    nsee.initCause(e);
                    throw nsee;
                }
            }
        };
        return IOStream.adapt(StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator2, 1040), false));
    }

    public static <T> IOStream<T> of(Iterable<T> values2) {
        return values2 == null ? IOStream.empty() : IOStream.adapt(StreamSupport.stream(values2.spliterator(), false));
    }

    @SafeVarargs
    public static <T> IOStream<T> of(T ... values2) {
        return values2 == null || values2.length == 0 ? IOStream.empty() : IOStream.adapt(Arrays.stream(values2));
    }

    public static <T> IOStream<T> of(T t) {
        return IOStream.adapt(Stream.of(t));
    }

    default public boolean allMatch(IOPredicate<? super T> predicate) throws IOException {
        return ((Stream)this.unwrap()).allMatch((? super T t) -> Erase.test(predicate, t));
    }

    default public boolean anyMatch(IOPredicate<? super T> predicate) throws IOException {
        return ((Stream)this.unwrap()).anyMatch((? super T t) -> Erase.test(predicate, t));
    }

    default public <R, A> R collect(Collector<? super T, A, R> collector) {
        return ((Stream)this.unwrap()).collect(collector);
    }

    default public <R> R collect(IOSupplier<R> supplier, IOBiConsumer<R, ? super T> accumulator, IOBiConsumer<R, R> combiner) throws IOException {
        return (R)((Stream)this.unwrap()).collect(() -> Erase.get(supplier), (R t, ? super T u) -> Erase.accept(accumulator, t, u), (R t, R u) -> Erase.accept(combiner, t, u));
    }

    default public long count() {
        return ((Stream)this.unwrap()).count();
    }

    default public IOStream<T> distinct() {
        return IOStream.adapt(((Stream)this.unwrap()).distinct());
    }

    default public IOStream<T> filter(IOPredicate<? super T> predicate) throws IOException {
        return IOStream.adapt(((Stream)this.unwrap()).filter((? super T t) -> Erase.test(predicate, t)));
    }

    default public Optional<T> findAny() {
        return ((Stream)this.unwrap()).findAny();
    }

    default public Optional<T> findFirst() {
        return ((Stream)this.unwrap()).findFirst();
    }

    default public <R> IOStream<R> flatMap(IOFunction<? super T, ? extends IOStream<? extends R>> mapper) throws IOException {
        return IOStream.adapt(((Stream)this.unwrap()).flatMap((? super T t) -> (Stream)((IOStream)Erase.apply(mapper, t)).unwrap()));
    }

    default public DoubleStream flatMapToDouble(IOFunction<? super T, ? extends DoubleStream> mapper) throws IOException {
        return ((Stream)this.unwrap()).flatMapToDouble((? super T t) -> (DoubleStream)Erase.apply(mapper, t));
    }

    default public IntStream flatMapToInt(IOFunction<? super T, ? extends IntStream> mapper) throws IOException {
        return ((Stream)this.unwrap()).flatMapToInt((? super T t) -> (IntStream)Erase.apply(mapper, t));
    }

    default public LongStream flatMapToLong(IOFunction<? super T, ? extends LongStream> mapper) throws IOException {
        return ((Stream)this.unwrap()).flatMapToLong((? super T t) -> (LongStream)Erase.apply(mapper, t));
    }

    default public void forAll(IOConsumer<T> action) throws IOExceptionList {
        this.forAll(action, (i, e) -> e);
    }

    default public void forAll(IOConsumer<T> action, BiFunction<Integer, IOException, IOException> exSupplier) throws IOExceptionList {
        AtomicReference causeList = new AtomicReference();
        AtomicInteger index = new AtomicInteger();
        IOConsumer safeAction = IOStreams.toIOConsumer(action);
        ((Stream)this.unwrap()).forEach((? super T e) -> {
            block3: {
                try {
                    safeAction.accept(e);
                }
                catch (IOException innerEx) {
                    if (causeList.get() == null) {
                        causeList.set(new ArrayList());
                    }
                    if (exSupplier == null) break block3;
                    ((List)causeList.get()).add((IOException)exSupplier.apply(index.get(), innerEx));
                }
            }
            index.incrementAndGet();
        });
        IOExceptionList.checkEmpty((List)causeList.get(), null);
    }

    default public void forEach(IOConsumer<? super T> action) throws IOException {
        ((Stream)this.unwrap()).forEach((? super T e) -> Erase.accept(action, e));
    }

    default public void forEachOrdered(IOConsumer<? super T> action) throws IOException {
        ((Stream)this.unwrap()).forEachOrdered((? super T e) -> Erase.accept(action, e));
    }

    default public IOStream<T> limit(long maxSize) {
        return IOStream.adapt(((Stream)this.unwrap()).limit(maxSize));
    }

    default public <R> IOStream<R> map(IOFunction<? super T, ? extends R> mapper) throws IOException {
        return IOStream.adapt(((Stream)this.unwrap()).map((? super T t) -> Erase.apply(mapper, t)));
    }

    default public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return ((Stream)this.unwrap()).mapToDouble(mapper);
    }

    default public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return ((Stream)this.unwrap()).mapToInt(mapper);
    }

    default public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return ((Stream)this.unwrap()).mapToLong(mapper);
    }

    default public Optional<T> max(IOComparator<? super T> comparator) throws IOException {
        return ((Stream)this.unwrap()).max((t, u) -> Erase.compare(comparator, t, u));
    }

    default public Optional<T> min(IOComparator<? super T> comparator) throws IOException {
        return ((Stream)this.unwrap()).min((t, u) -> Erase.compare(comparator, t, u));
    }

    default public boolean noneMatch(IOPredicate<? super T> predicate) throws IOException {
        return ((Stream)this.unwrap()).noneMatch((? super T t) -> Erase.test(predicate, t));
    }

    default public IOStream<T> peek(IOConsumer<? super T> action) throws IOException {
        return IOStream.adapt(((Stream)this.unwrap()).peek((? super T t) -> Erase.accept(action, t)));
    }

    default public Optional<T> reduce(IOBinaryOperator<T> accumulator) throws IOException {
        return ((Stream)this.unwrap()).reduce((t, u) -> Erase.apply(accumulator, t, u));
    }

    default public T reduce(T identity, IOBinaryOperator<T> accumulator) throws IOException {
        return ((Stream)this.unwrap()).reduce(identity, (t, u) -> Erase.apply(accumulator, t, u));
    }

    default public <U> U reduce(U identity, IOBiFunction<U, ? super T, U> accumulator, IOBinaryOperator<U> combiner) throws IOException {
        return (U)((Stream)this.unwrap()).reduce(identity, (U t, ? super T u) -> Erase.apply(accumulator, t, u), (t, u) -> Erase.apply(combiner, t, u));
    }

    default public IOStream<T> skip(long n) {
        return IOStream.adapt(((Stream)this.unwrap()).skip(n));
    }

    default public IOStream<T> sorted() {
        return IOStream.adapt(((Stream)this.unwrap()).sorted());
    }

    default public IOStream<T> sorted(IOComparator<? super T> comparator) throws IOException {
        return IOStream.adapt(((Stream)this.unwrap()).sorted((t, u) -> Erase.compare(comparator, t, u)));
    }

    default public Object[] toArray() {
        return ((Stream)this.unwrap()).toArray();
    }

    default public <A> A[] toArray(IntFunction<A[]> generator) {
        return ((Stream)this.unwrap()).toArray(generator);
    }
}

