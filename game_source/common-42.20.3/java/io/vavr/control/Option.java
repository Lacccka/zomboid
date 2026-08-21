/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.control;

import io.vavr.PartialFunction;
import io.vavr.Value;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Iterator;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Option<T>
extends Value<T>,
Serializable {
    public static final long serialVersionUID = 1L;

    public static <T> Option<T> of(T value) {
        return value == null ? Option.none() : Option.some(value);
    }

    public static <T> Option<Seq<T>> sequence(Iterable<? extends Option<? extends T>> values2) {
        Objects.requireNonNull(values2, "values is null");
        IndexedSeq vector = Vector.empty();
        for (Option<T> value : values2) {
            if (value.isEmpty()) {
                return Option.none();
            }
            vector = vector.append((Object)value.get());
        }
        return Option.some(vector);
    }

    public static <T, U> Option<Seq<U>> traverse(Iterable<? extends T> values2, Function<? super T, ? extends Option<? extends U>> mapper) {
        Objects.requireNonNull(values2, "values is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return Option.sequence(Iterator.ofAll(values2).map(mapper));
    }

    public static <T> Option<T> some(T value) {
        return new Some(value);
    }

    public static <T> Option<T> none() {
        None none = None.INSTANCE;
        return none;
    }

    public static <T> Option<T> narrow(Option<? extends T> option) {
        return option;
    }

    public static <T> Option<T> when(boolean condition, Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return condition ? Option.some(supplier.get()) : Option.none();
    }

    public static <T> Option<T> when(boolean condition, T value) {
        return condition ? Option.some(value) : Option.none();
    }

    public static <T> Option<T> ofOptional(Optional<? extends T> optional) {
        Objects.requireNonNull(optional, "optional is null");
        return optional.map(Option::of).orElseGet(Option::none);
    }

    default public <R> Option<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        Objects.requireNonNull(partialFunction, "partialFunction is null");
        return this.flatMap(partialFunction.lift()::apply);
    }

    @Override
    public boolean isEmpty();

    default public Option<T> onEmpty(Runnable action) {
        Objects.requireNonNull(action, "action is null");
        if (this.isEmpty()) {
            action.run();
        }
        return this;
    }

    @Override
    default public boolean isAsync() {
        return false;
    }

    default public boolean isDefined() {
        return !this.isEmpty();
    }

    @Override
    default public boolean isLazy() {
        return false;
    }

    @Override
    default public boolean isSingleValued() {
        return true;
    }

    @Override
    public T get();

    @Override
    default public T getOrElse(T other) {
        return this.isEmpty() ? other : this.get();
    }

    default public Option<T> orElse(Option<? extends T> other) {
        Objects.requireNonNull(other, "other is null");
        return this.isEmpty() ? other : this;
    }

    default public Option<T> orElse(Supplier<? extends Option<? extends T>> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return this.isEmpty() ? supplier.get() : this;
    }

    @Override
    default public T getOrElse(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return this.isEmpty() ? supplier.get() : this.get();
    }

    @Override
    default public <X extends Throwable> T getOrElseThrow(Supplier<X> exceptionSupplier) throws X {
        Objects.requireNonNull(exceptionSupplier, "exceptionSupplier is null");
        if (this.isEmpty()) {
            throw (Throwable)exceptionSupplier.get();
        }
        return this.get();
    }

    default public Option<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.isEmpty() || predicate.test(this.get()) ? this : Option.none();
    }

    default public <U> Option<U> flatMap(Function<? super T, ? extends Option<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.isEmpty() ? Option.none() : mapper.apply(this.get());
    }

    @Override
    default public <U> Option<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.isEmpty() ? Option.none() : Option.some(mapper.apply(this.get()));
    }

    default public <U> U fold(Supplier<? extends U> ifNone, Function<? super T, ? extends U> f) {
        return this.map((Function)f).getOrElse(ifNone);
    }

    @Override
    default public Option<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (this.isDefined()) {
            action.accept(this.get());
        }
        return this;
    }

    default public <U> U transform(Function<? super Option<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    default public Iterator<T> iterator() {
        return this.isEmpty() ? Iterator.empty() : Iterator.of(this.get());
    }

    @Override
    public boolean equals(Object var1);

    @Override
    public int hashCode();

    @Override
    public String toString();

    public static final class None<T>
    implements Option<T>,
    Serializable {
        private static final long serialVersionUID = 1L;
        private static final None<?> INSTANCE = new None();

        private None() {
        }

        @Override
        public T get() {
            throw new NoSuchElementException("No value present");
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            return o == this;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public String stringPrefix() {
            return "None";
        }

        @Override
        public String toString() {
            return this.stringPrefix();
        }

        private Object readResolve() {
            return INSTANCE;
        }
    }

    public static final class Some<T>
    implements Option<T>,
    Serializable {
        private static final long serialVersionUID = 1L;
        private final T value;

        private Some(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return this.value;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof Some && Objects.equals(this.value, ((Some)obj).value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.value);
        }

        @Override
        public String stringPrefix() {
            return "Some";
        }

        @Override
        public String toString() {
            return this.stringPrefix() + "(" + this.value + ")";
        }
    }
}

