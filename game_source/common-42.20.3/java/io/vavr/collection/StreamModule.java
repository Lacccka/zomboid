/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.collection.AbstractIterator;
import io.vavr.collection.GwtIncompatible;
import io.vavr.collection.Iterator;
import io.vavr.collection.LinearSeq;
import io.vavr.collection.List;
import io.vavr.collection.Queue;
import io.vavr.collection.Stream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

interface StreamModule {

    public static final class FlatMapIterator<T, U>
    implements Iterator<U> {
        final Function<? super T, ? extends Iterable<? extends U>> mapper;
        final Iterator<? extends T> inputs;
        java.util.Iterator<? extends U> current = Collections.emptyIterator();

        FlatMapIterator(Iterator<? extends T> inputs, Function<? super T, ? extends Iterable<? extends U>> mapper) {
            this.inputs = inputs;
            this.mapper = mapper;
        }

        @Override
        public boolean hasNext() {
            boolean currentHasNext;
            while (!(currentHasNext = this.current.hasNext()) && this.inputs.hasNext()) {
                this.current = this.mapper.apply(this.inputs.next()).iterator();
            }
            return currentHasNext;
        }

        @Override
        public U next() {
            return this.current.next();
        }
    }

    public static final class StreamIterator<T>
    extends AbstractIterator<T> {
        private Supplier<Stream<T>> current = () -> stream;

        StreamIterator(Stream.Cons<T> stream) {
        }

        @Override
        public boolean hasNext() {
            return !this.current.get().isEmpty();
        }

        @Override
        public T getNext() {
            Stream<T> stream = this.current.get();
            this.current = stream::tail;
            return stream.head();
        }
    }

    public static interface StreamFactory {
        public static <T> Stream<T> create(java.util.Iterator<? extends T> iterator2) {
            return iterator2.hasNext() ? Stream.cons(iterator2.next(), () -> StreamFactory.create(iterator2)) : Stream.Empty.instance();
        }
    }

    public static interface DropRight {
        public static <T> Stream<T> apply(List<T> front, List<T> rear, Stream<T> remaining) {
            if (remaining.isEmpty()) {
                return remaining;
            }
            if (front.isEmpty()) {
                return DropRight.apply(rear.reverse(), List.empty(), remaining);
            }
            return Stream.cons(front.head(), () -> DropRight.apply(front.tail(), rear.prepend(remaining.head()), remaining.tail()));
        }
    }

    public static interface Combinations {
        public static <T> Stream<Stream<T>> apply(Stream<T> elements, int k) {
            if (k == 0) {
                return Stream.of(Stream.empty());
            }
            return elements.zipWithIndex().flatMap(t -> Combinations.apply(elements.drop((Integer)t._2 + 1), k - 1).map(c -> c.prepend(t._1)));
        }
    }

    public static final class AppendSelf<T> {
        private final Stream.Cons<T> self;

        AppendSelf(Stream.Cons<T> self, Function<? super Stream<T>, ? extends Stream<T>> mapper) {
            this.self = this.appendAll(self, mapper);
        }

        private Stream.Cons<T> appendAll(Stream.Cons<T> stream, Function<? super Stream<T>, ? extends Stream<T>> mapper) {
            return (Stream.Cons)Stream.cons(stream.head(), () -> {
                LinearSeq tail = stream.tail();
                return tail.isEmpty() ? (Stream.Cons<T>)mapper.apply(this.self) : this.appendAll((Stream.Cons)tail, mapper);
            });
        }

        Stream.Cons<T> stream() {
            return this.self;
        }
    }

    @GwtIncompatible(value="The Java serialization protocol is explicitly not supported")
    public static final class SerializationProxy<T>
    implements Serializable {
        private static final long serialVersionUID = 1L;
        private transient Stream.Cons<T> stream;

        SerializationProxy(Stream.Cons<T> stream) {
            this.stream = stream;
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            s.writeInt(this.stream.length());
            LinearSeq<T> l = this.stream;
            while (!l.isEmpty()) {
                s.writeObject(l.head());
                l = l.tail();
            }
        }

        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            s.defaultReadObject();
            int size = s.readInt();
            if (size <= 0) {
                throw new InvalidObjectException("No elements");
            }
            LinearSeq temp = Stream.Empty.instance();
            for (int i = 0; i < size; ++i) {
                Object element = s.readObject();
                temp = temp.append(element);
            }
            this.stream = (Stream.Cons)temp;
        }

        private Object readResolve() {
            return this.stream;
        }
    }

    public static final class AppendElements<T>
    extends Stream.Cons<T>
    implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Queue<T> queue;

        AppendElements(T head, Queue<T> queue, Supplier<Stream<T>> tail) {
            super(head, tail);
            this.queue = queue;
        }

        @Override
        public Stream<T> append(T element) {
            return new AppendElements<Object>(this.head, (Queue<Object>)this.queue.append((Object)element), this.tail);
        }

        @Override
        public Stream<T> appendAll(Iterable<? extends T> elements) {
            Objects.requireNonNull(elements, "elements is null");
            return this.isEmpty() ? Stream.ofAll(this.queue) : new AppendElements<Object>(this.head, (Queue<Object>)this.queue.appendAll((Iterable)elements), this.tail);
        }

        @Override
        public Stream<T> tail() {
            Stream t = (Stream)this.tail.get();
            if (t.isEmpty()) {
                return Stream.ofAll(this.queue);
            }
            if (t instanceof ConsImpl) {
                ConsImpl c = (ConsImpl)t;
                return new AppendElements(c.head(), this.queue, c.tail);
            }
            AppendElements a = (AppendElements)t;
            return new AppendElements(a.head(), a.queue.appendAll((Iterable)this.queue), a.tail);
        }

        @GwtIncompatible(value="The Java serialization protocol is explicitly not supported")
        private Object writeReplace() {
            return new SerializationProxy(this);
        }

        @GwtIncompatible(value="The Java serialization protocol is explicitly not supported")
        private void readObject(ObjectInputStream stream) throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required");
        }
    }

    public static final class ConsImpl<T>
    extends Stream.Cons<T>
    implements Serializable {
        private static final long serialVersionUID = 1L;

        ConsImpl(T head, Supplier<Stream<T>> tail) {
            super(head, tail);
        }

        @Override
        public Stream<T> tail() {
            return (Stream)this.tail.get();
        }

        @GwtIncompatible(value="The Java serialization protocol is explicitly not supported")
        private Object writeReplace() {
            return new SerializationProxy(this);
        }

        @GwtIncompatible(value="The Java serialization protocol is explicitly not supported")
        private void readObject(ObjectInputStream stream) throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required");
        }
    }
}

