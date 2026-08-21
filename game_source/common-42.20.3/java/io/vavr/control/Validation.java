/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.control;

import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.Function4;
import io.vavr.Function5;
import io.vavr.Function6;
import io.vavr.Function7;
import io.vavr.Function8;
import io.vavr.Value;
import io.vavr.collection.Iterator;
import io.vavr.collection.LinearSeq;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Validation<E, T>
extends Value<T>,
Serializable {
    public static final long serialVersionUID = 1L;

    public static <E, T> Validation<E, T> valid(T value) {
        return new Valid(value);
    }

    public static <E, T> Validation<E, T> invalid(E error) {
        Objects.requireNonNull(error, "error is null");
        return new Invalid(error);
    }

    public static <E, T> Validation<E, T> fromEither(Either<E, T> either) {
        Objects.requireNonNull(either, "either is null");
        return either.isRight() ? Validation.valid(either.get()) : Validation.invalid(either.getLeft());
    }

    public static <T> Validation<Throwable, T> fromTry(Try<? extends T> t) {
        Objects.requireNonNull(t, "t is null");
        return t.isSuccess() ? Validation.valid(t.get()) : Validation.invalid(t.getCause());
    }

    public static <E, T> Validation<Seq<E>, Seq<T>> sequence(Iterable<? extends Validation<? extends Seq<? extends E>, ? extends T>> values2) {
        Objects.requireNonNull(values2, "values is null");
        LinearSeq errors = List.empty();
        LinearSeq list = List.empty();
        for (Validation<Seq<E>, T> value : values2) {
            if (value.isInvalid()) {
                errors = errors.prependAll(value.getError().reverse());
                continue;
            }
            if (!errors.isEmpty()) continue;
            list = list.prepend((Object)value.get());
        }
        return errors.isEmpty() ? Validation.valid(list.reverse()) : Validation.invalid(errors.reverse());
    }

    public static <E, T, U> Validation<Seq<E>, Seq<U>> traverse(Iterable<? extends T> values2, Function<? super T, ? extends Validation<? extends Seq<? extends E>, ? extends U>> mapper) {
        Objects.requireNonNull(values2, "values is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return Validation.sequence(Iterator.ofAll(values2).map(mapper));
    }

    public static <E, T> Validation<E, T> narrow(Validation<? extends E, ? extends T> validation) {
        return validation;
    }

    public static <E, T1, T2> Builder<E, T1, T2> combine(Validation<E, T1> validation1, Validation<E, T2> validation2) {
        Objects.requireNonNull(validation1, "validation1 is null");
        Objects.requireNonNull(validation2, "validation2 is null");
        return new Builder(validation1, validation2);
    }

    public static <E, T1, T2, T3> Builder3<E, T1, T2, T3> combine(Validation<E, T1> validation1, Validation<E, T2> validation2, Validation<E, T3> validation3) {
        Objects.requireNonNull(validation1, "validation1 is null");
        Objects.requireNonNull(validation2, "validation2 is null");
        Objects.requireNonNull(validation3, "validation3 is null");
        return new Builder3(validation1, validation2, validation3);
    }

    public static <E, T1, T2, T3, T4> Builder4<E, T1, T2, T3, T4> combine(Validation<E, T1> validation1, Validation<E, T2> validation2, Validation<E, T3> validation3, Validation<E, T4> validation4) {
        Objects.requireNonNull(validation1, "validation1 is null");
        Objects.requireNonNull(validation2, "validation2 is null");
        Objects.requireNonNull(validation3, "validation3 is null");
        Objects.requireNonNull(validation4, "validation4 is null");
        return new Builder4(validation1, validation2, validation3, validation4);
    }

    public static <E, T1, T2, T3, T4, T5> Builder5<E, T1, T2, T3, T4, T5> combine(Validation<E, T1> validation1, Validation<E, T2> validation2, Validation<E, T3> validation3, Validation<E, T4> validation4, Validation<E, T5> validation5) {
        Objects.requireNonNull(validation1, "validation1 is null");
        Objects.requireNonNull(validation2, "validation2 is null");
        Objects.requireNonNull(validation3, "validation3 is null");
        Objects.requireNonNull(validation4, "validation4 is null");
        Objects.requireNonNull(validation5, "validation5 is null");
        return new Builder5(validation1, validation2, validation3, validation4, validation5);
    }

    public static <E, T1, T2, T3, T4, T5, T6> Builder6<E, T1, T2, T3, T4, T5, T6> combine(Validation<E, T1> validation1, Validation<E, T2> validation2, Validation<E, T3> validation3, Validation<E, T4> validation4, Validation<E, T5> validation5, Validation<E, T6> validation6) {
        Objects.requireNonNull(validation1, "validation1 is null");
        Objects.requireNonNull(validation2, "validation2 is null");
        Objects.requireNonNull(validation3, "validation3 is null");
        Objects.requireNonNull(validation4, "validation4 is null");
        Objects.requireNonNull(validation5, "validation5 is null");
        Objects.requireNonNull(validation6, "validation6 is null");
        return new Builder6(validation1, validation2, validation3, validation4, validation5, validation6);
    }

    public static <E, T1, T2, T3, T4, T5, T6, T7> Builder7<E, T1, T2, T3, T4, T5, T6, T7> combine(Validation<E, T1> validation1, Validation<E, T2> validation2, Validation<E, T3> validation3, Validation<E, T4> validation4, Validation<E, T5> validation5, Validation<E, T6> validation6, Validation<E, T7> validation7) {
        Objects.requireNonNull(validation1, "validation1 is null");
        Objects.requireNonNull(validation2, "validation2 is null");
        Objects.requireNonNull(validation3, "validation3 is null");
        Objects.requireNonNull(validation4, "validation4 is null");
        Objects.requireNonNull(validation5, "validation5 is null");
        Objects.requireNonNull(validation6, "validation6 is null");
        Objects.requireNonNull(validation7, "validation7 is null");
        return new Builder7(validation1, validation2, validation3, validation4, validation5, validation6, validation7);
    }

    public static <E, T1, T2, T3, T4, T5, T6, T7, T8> Builder8<E, T1, T2, T3, T4, T5, T6, T7, T8> combine(Validation<E, T1> validation1, Validation<E, T2> validation2, Validation<E, T3> validation3, Validation<E, T4> validation4, Validation<E, T5> validation5, Validation<E, T6> validation6, Validation<E, T7> validation7, Validation<E, T8> validation8) {
        Objects.requireNonNull(validation1, "validation1 is null");
        Objects.requireNonNull(validation2, "validation2 is null");
        Objects.requireNonNull(validation3, "validation3 is null");
        Objects.requireNonNull(validation4, "validation4 is null");
        Objects.requireNonNull(validation5, "validation5 is null");
        Objects.requireNonNull(validation6, "validation6 is null");
        Objects.requireNonNull(validation7, "validation7 is null");
        Objects.requireNonNull(validation8, "validation8 is null");
        return new Builder8(validation1, validation2, validation3, validation4, validation5, validation6, validation7, validation8);
    }

    public boolean isValid();

    public boolean isInvalid();

    default public Validation<E, T> orElse(Validation<? extends E, ? extends T> other) {
        Objects.requireNonNull(other, "other is null");
        return this.isValid() ? this : other;
    }

    default public Validation<E, T> orElse(Supplier<Validation<? extends E, ? extends T>> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return this.isValid() ? this : supplier.get();
    }

    @Override
    default public boolean isEmpty() {
        return this.isInvalid();
    }

    @Override
    public T get();

    default public T getOrElseGet(Function<? super E, ? extends T> other) {
        Objects.requireNonNull(other, "other is null");
        if (this.isValid()) {
            return this.get();
        }
        return other.apply(this.getError());
    }

    public E getError();

    default public Either<E, T> toEither() {
        return this.isValid() ? Either.right(this.get()) : Either.left(this.getError());
    }

    @Override
    public boolean equals(Object var1);

    @Override
    public int hashCode();

    @Override
    public String toString();

    @Override
    default public void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (this.isValid()) {
            action.accept(this.get());
        }
    }

    default public <U> U fold(Function<? super E, ? extends U> ifInvalid, Function<? super T, ? extends U> ifValid) {
        Objects.requireNonNull(ifInvalid, "ifInvalid is null");
        Objects.requireNonNull(ifValid, "ifValid is null");
        return this.isValid() ? ifValid.apply(this.get()) : ifInvalid.apply(this.getError());
    }

    default public Validation<T, E> swap() {
        if (this.isInvalid()) {
            E error = this.getError();
            return Validation.valid(error);
        }
        T value = this.get();
        return Validation.invalid(value);
    }

    default public <U> Validation<E, U> map(Function<? super T, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        if (this.isInvalid()) {
            return Validation.invalid(this.getError());
        }
        T value = this.get();
        return Validation.valid(f.apply(value));
    }

    default public <E2, T2> Validation<E2, T2> bimap(Function<? super E, ? extends E2> errorMapper, Function<? super T, ? extends T2> valueMapper) {
        Objects.requireNonNull(errorMapper, "errorMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        if (this.isInvalid()) {
            E error = this.getError();
            return Validation.invalid(errorMapper.apply(error));
        }
        T value = this.get();
        return Validation.valid(valueMapper.apply(value));
    }

    default public <U> Validation<U, T> mapError(Function<? super E, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        if (this.isInvalid()) {
            E error = this.getError();
            return Validation.invalid(f.apply(error));
        }
        return Validation.valid(this.get());
    }

    default public <U> Validation<Seq<E>, U> ap(Validation<Seq<E>, ? extends Function<? super T, ? extends U>> validation) {
        Objects.requireNonNull(validation, "validation is null");
        if (this.isValid()) {
            if (validation.isValid()) {
                Function<T, U> f = validation.get();
                U u = f.apply(this.get());
                return Validation.valid(u);
            }
            Seq<E> errors = validation.getError();
            return Validation.invalid(errors);
        }
        if (validation.isValid()) {
            E error = this.getError();
            return Validation.invalid(List.of(error));
        }
        Seq<E> errors = validation.getError();
        E error = this.getError();
        return Validation.invalid(errors.append(error));
    }

    default public <U> Builder<E, T, U> combine(Validation<E, U> validation) {
        return new Builder(this, validation);
    }

    default public Option<Validation<E, T>> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.isInvalid() || predicate.test(this.get()) ? Option.some(this) : Option.none();
    }

    default public <U> Validation<E, U> flatMap(Function<? super T, ? extends Validation<E, ? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.isInvalid() ? this : mapper.apply(this.get());
    }

    default public Validation<E, T> peek(Consumer<? super T> action) {
        if (this.isValid()) {
            action.accept(this.get());
        }
        return this;
    }

    @Override
    default public boolean isAsync() {
        return false;
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
    default public Iterator<T> iterator() {
        return this.isValid() ? Iterator.of(this.get()) : Iterator.empty();
    }

    public static final class Builder8<E, T1, T2, T3, T4, T5, T6, T7, T8> {
        private Validation<E, T1> v1;
        private Validation<E, T2> v2;
        private Validation<E, T3> v3;
        private Validation<E, T4> v4;
        private Validation<E, T5> v5;
        private Validation<E, T6> v6;
        private Validation<E, T7> v7;
        private Validation<E, T8> v8;

        private Builder8(Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3, Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6, Validation<E, T7> v7, Validation<E, T8> v8) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
            this.v7 = v7;
            this.v8 = v8;
        }

        public <R> Validation<Seq<E>, R> ap(Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> f) {
            return this.v8.ap(this.v7.ap(this.v6.ap(this.v5.ap(this.v4.ap(this.v3.ap(this.v2.ap(this.v1.ap(Validation.valid(f.curried())))))))));
        }
    }

    public static final class Builder7<E, T1, T2, T3, T4, T5, T6, T7> {
        private Validation<E, T1> v1;
        private Validation<E, T2> v2;
        private Validation<E, T3> v3;
        private Validation<E, T4> v4;
        private Validation<E, T5> v5;
        private Validation<E, T6> v6;
        private Validation<E, T7> v7;

        private Builder7(Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3, Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6, Validation<E, T7> v7) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
            this.v7 = v7;
        }

        public <R> Validation<Seq<E>, R> ap(Function7<T1, T2, T3, T4, T5, T6, T7, R> f) {
            return this.v7.ap(this.v6.ap(this.v5.ap(this.v4.ap(this.v3.ap(this.v2.ap(this.v1.ap(Validation.valid(f.curried()))))))));
        }

        public <T8> Builder8<E, T1, T2, T3, T4, T5, T6, T7, T8> combine(Validation<E, T8> v8) {
            return new Builder8(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, this.v7, v8);
        }
    }

    public static final class Builder6<E, T1, T2, T3, T4, T5, T6> {
        private Validation<E, T1> v1;
        private Validation<E, T2> v2;
        private Validation<E, T3> v3;
        private Validation<E, T4> v4;
        private Validation<E, T5> v5;
        private Validation<E, T6> v6;

        private Builder6(Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3, Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
        }

        public <R> Validation<Seq<E>, R> ap(Function6<T1, T2, T3, T4, T5, T6, R> f) {
            return this.v6.ap(this.v5.ap(this.v4.ap(this.v3.ap(this.v2.ap(this.v1.ap(Validation.valid(f.curried())))))));
        }

        public <T7> Builder7<E, T1, T2, T3, T4, T5, T6, T7> combine(Validation<E, T7> v7) {
            return new Builder7(this.v1, this.v2, this.v3, this.v4, this.v5, this.v6, v7);
        }
    }

    public static final class Builder5<E, T1, T2, T3, T4, T5> {
        private Validation<E, T1> v1;
        private Validation<E, T2> v2;
        private Validation<E, T3> v3;
        private Validation<E, T4> v4;
        private Validation<E, T5> v5;

        private Builder5(Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3, Validation<E, T4> v4, Validation<E, T5> v5) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
        }

        public <R> Validation<Seq<E>, R> ap(Function5<T1, T2, T3, T4, T5, R> f) {
            return this.v5.ap(this.v4.ap(this.v3.ap(this.v2.ap(this.v1.ap(Validation.valid(f.curried()))))));
        }

        public <T6> Builder6<E, T1, T2, T3, T4, T5, T6> combine(Validation<E, T6> v6) {
            return new Builder6(this.v1, this.v2, this.v3, this.v4, this.v5, v6);
        }
    }

    public static final class Builder4<E, T1, T2, T3, T4> {
        private Validation<E, T1> v1;
        private Validation<E, T2> v2;
        private Validation<E, T3> v3;
        private Validation<E, T4> v4;

        private Builder4(Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3, Validation<E, T4> v4) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
        }

        public <R> Validation<Seq<E>, R> ap(Function4<T1, T2, T3, T4, R> f) {
            return this.v4.ap(this.v3.ap(this.v2.ap(this.v1.ap(Validation.valid(f.curried())))));
        }

        public <T5> Builder5<E, T1, T2, T3, T4, T5> combine(Validation<E, T5> v5) {
            return new Builder5(this.v1, this.v2, this.v3, this.v4, v5);
        }
    }

    public static final class Builder3<E, T1, T2, T3> {
        private Validation<E, T1> v1;
        private Validation<E, T2> v2;
        private Validation<E, T3> v3;

        private Builder3(Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
        }

        public <R> Validation<Seq<E>, R> ap(Function3<T1, T2, T3, R> f) {
            return this.v3.ap(this.v2.ap(this.v1.ap(Validation.valid(f.curried()))));
        }

        public <T4> Builder4<E, T1, T2, T3, T4> combine(Validation<E, T4> v4) {
            return new Builder4(this.v1, this.v2, this.v3, v4);
        }
    }

    public static final class Builder<E, T1, T2> {
        private Validation<E, T1> v1;
        private Validation<E, T2> v2;

        private Builder(Validation<E, T1> v1, Validation<E, T2> v2) {
            this.v1 = v1;
            this.v2 = v2;
        }

        public <R> Validation<Seq<E>, R> ap(Function2<T1, T2, R> f) {
            return this.v2.ap(this.v1.ap(Validation.valid(f.curried())));
        }

        public <T3> Builder3<E, T1, T2, T3> combine(Validation<E, T3> v3) {
            return new Builder3(this.v1, this.v2, v3);
        }
    }

    public static final class Invalid<E, T>
    implements Validation<E, T>,
    Serializable {
        private static final long serialVersionUID = 1L;
        private final E error;

        private Invalid(E error) {
            this.error = error;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public boolean isInvalid() {
            return true;
        }

        @Override
        public T get() throws RuntimeException {
            throw new NoSuchElementException("get of 'invalid' Validation");
        }

        @Override
        public E getError() {
            return this.error;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof Invalid && Objects.equals(this.error, ((Invalid)obj).error);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.error);
        }

        @Override
        public String stringPrefix() {
            return "Invalid";
        }

        @Override
        public String toString() {
            return this.stringPrefix() + "(" + this.error + ")";
        }
    }

    public static final class Valid<E, T>
    implements Validation<E, T>,
    Serializable {
        private static final long serialVersionUID = 1L;
        private final T value;

        private Valid(T value) {
            this.value = value;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean isInvalid() {
            return false;
        }

        @Override
        public T get() {
            return this.value;
        }

        @Override
        public E getError() throws RuntimeException {
            throw new NoSuchElementException("error of 'valid' Validation");
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof Valid && Objects.equals(this.value, ((Valid)obj).value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.value);
        }

        @Override
        public String stringPrefix() {
            return "Valid";
        }

        @Override
        public String toString() {
            return this.stringPrefix() + "(" + this.value + ")";
        }
    }
}

