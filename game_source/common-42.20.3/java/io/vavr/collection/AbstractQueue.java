/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Collections;
import io.vavr.collection.List;
import io.vavr.collection.Traversable;
import io.vavr.control.Option;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

abstract class AbstractQueue<T, Q extends AbstractQueue<T, Q>>
implements Traversable<T> {
    AbstractQueue() {
    }

    public Tuple2<T, Q> dequeue() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("dequeue of empty " + this.getClass().getSimpleName());
        }
        return Tuple.of(this.head(), this.tail());
    }

    public Option<Tuple2<T, Q>> dequeueOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.dequeue());
    }

    public abstract Q enqueue(T var1);

    public Q enqueue(T ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return this.enqueueAll(List.of(elements));
    }

    public abstract Q enqueueAll(Iterable<? extends T> var1);

    public T peek() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("peek of empty " + this.getClass().getSimpleName());
        }
        return this.head();
    }

    public Option<T> peekOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.peek());
    }

    public Q dropUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return (Q)this.dropWhile((Predicate)predicate.negate());
    }

    public abstract Q dropWhile(Predicate<? super T> var1);

    public abstract Q init();

    @Override
    public Option<Q> initOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.init());
    }

    public abstract Q tail();

    @Override
    public Option<Q> tailOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.tail());
    }

    public Q retainAll(Iterable<? extends T> elements) {
        return (Q)Collections.retainAll(this, elements);
    }

    public Q removeAll(Iterable<? extends T> elements) {
        return (Q)Collections.removeAll(this, elements);
    }

    @Deprecated
    public Q removeAll(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return (Q)this.reject((Predicate)predicate);
    }

    public Q reject(Predicate<? super T> predicate) {
        return (Q)Collections.reject(this, predicate);
    }

    public Q takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return (Q)this.takeUntil((Predicate)predicate.negate());
    }

    public abstract Q takeUntil(Predicate<? super T> var1);

    public Q peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (!this.isEmpty()) {
            action.accept(this.head());
        }
        return (Q)this;
    }

    @Override
    public String toString() {
        return this.mkString(this.stringPrefix() + "(", ", ", ")");
    }
}

