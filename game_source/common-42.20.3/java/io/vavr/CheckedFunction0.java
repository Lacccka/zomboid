/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.CheckedFunction0Module;
import io.vavr.CheckedFunction1;
import io.vavr.Function0;
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
public interface CheckedFunction0<R>
extends Serializable {
    public static final long serialVersionUID = 1L;

    public static <R> CheckedFunction0<R> constant(R value) {
        return () -> value;
    }

    public static <R> CheckedFunction0<R> of(CheckedFunction0<R> methodReference) {
        return methodReference;
    }

    public static <R> Function0<Option<R>> lift(CheckedFunction0<? extends R> partialFunction) {
        return () -> Try.of(partialFunction::apply).toOption();
    }

    public static <R> Function0<Try<R>> liftTry(CheckedFunction0<? extends R> partialFunction) {
        return () -> Try.of(partialFunction::apply);
    }

    public static <R> CheckedFunction0<R> narrow(CheckedFunction0<? extends R> f) {
        return f;
    }

    public R apply() throws Throwable;

    default public int arity() {
        return 0;
    }

    default public CheckedFunction0<R> curried() {
        return this;
    }

    default public CheckedFunction1<Tuple0, R> tupled() {
        return t -> this.apply();
    }

    default public CheckedFunction0<R> reversed() {
        return this;
    }

    default public CheckedFunction0<R> memoized() {
        if (this.isMemoized()) {
            return this;
        }
        Lazy<Object> lazy = Lazy.of(() -> {
            try {
                return this.apply();
            }
            catch (Throwable x) {
                throw new RuntimeException(x);
            }
        });
        return (CheckedFunction0<Object> & Memoized)() -> {
            try {
                return lazy.get();
            }
            catch (RuntimeException x) {
                throw x.getCause();
            }
        };
    }

    default public boolean isMemoized() {
        return this instanceof Memoized;
    }

    default public Function0<R> recover(Function<? super Throwable, ? extends Supplier<? extends R>> recover) {
        Objects.requireNonNull(recover, "recover is null");
        return () -> {
            try {
                return this.apply();
            }
            catch (Throwable throwable) {
                Supplier func = (Supplier)recover.apply(throwable);
                Objects.requireNonNull(func, () -> "recover return null for " + throwable.getClass() + ": " + throwable.getMessage());
                return func.get();
            }
        };
    }

    default public Function0<R> unchecked() {
        return () -> {
            try {
                return this.apply();
            }
            catch (Throwable t) {
                return CheckedFunction0Module.sneakyThrow(t);
            }
        };
    }

    default public <V> CheckedFunction0<V> andThen(CheckedFunction1<? super R, ? extends V> after) {
        Objects.requireNonNull(after, "after is null");
        return () -> after.apply((R)this.apply());
    }
}

