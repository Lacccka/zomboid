/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.Function1;
import io.vavr.Lazy;
import io.vavr.Memoized;
import io.vavr.Tuple0;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface Function0<R>
extends Serializable,
Supplier<R> {
    public static final long serialVersionUID = 1L;

    public static <R> Function0<R> constant(R value) {
        return () -> value;
    }

    public static <R> Function0<R> of(Function0<R> methodReference) {
        return methodReference;
    }

    public static <R> Function0<Option<R>> lift(Supplier<? extends R> partialFunction) {
        return () -> Try.of(((Supplier)partialFunction)::get).toOption();
    }

    public static <R> Function0<Try<R>> liftTry(Supplier<? extends R> partialFunction) {
        return () -> Try.of(((Supplier)partialFunction)::get);
    }

    public static <R> Function0<R> narrow(Function0<? extends R> f) {
        return f;
    }

    public R apply();

    @Override
    default public R get() {
        return this.apply();
    }

    default public int arity() {
        return 0;
    }

    default public Function0<R> curried() {
        return this;
    }

    default public Function1<Tuple0, R> tupled() {
        return t -> this.apply();
    }

    default public Function0<R> reversed() {
        return this;
    }

    default public Function0<R> memoized() {
        if (this.isMemoized()) {
            return this;
        }
        return Lazy.of(this)::get;
    }

    default public boolean isMemoized() {
        return this instanceof Memoized;
    }

    default public <V> Function0<V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after, "after is null");
        return () -> after.apply((R)this.apply());
    }
}

