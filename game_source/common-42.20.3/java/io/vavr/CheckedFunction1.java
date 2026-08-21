/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.CheckedFunction1Module;
import io.vavr.Function1;
import io.vavr.Memoized;
import io.vavr.Tuple1;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction1<T1, R>
extends Serializable {
    public static final long serialVersionUID = 1L;

    public static <T1, R> CheckedFunction1<T1, R> constant(R value) {
        return t1 -> value;
    }

    public static <T1, R> CheckedFunction1<T1, R> of(CheckedFunction1<T1, R> methodReference) {
        return methodReference;
    }

    public static <T1, R> Function1<T1, Option<R>> lift(CheckedFunction1<? super T1, ? extends R> partialFunction) {
        return t1 -> Try.of(() -> partialFunction.apply(t1)).toOption();
    }

    public static <T1, R> Function1<T1, Try<R>> liftTry(CheckedFunction1<? super T1, ? extends R> partialFunction) {
        return t1 -> Try.of(() -> partialFunction.apply(t1));
    }

    public static <T1, R> CheckedFunction1<T1, R> narrow(CheckedFunction1<? super T1, ? extends R> f) {
        return f;
    }

    public static <T> CheckedFunction1<T, T> identity() {
        return t -> t;
    }

    public R apply(T1 var1) throws Throwable;

    default public int arity() {
        return 1;
    }

    default public CheckedFunction1<T1, R> curried() {
        return this;
    }

    default public CheckedFunction1<Tuple1<T1>, R> tupled() {
        return t -> this.apply(t._1);
    }

    default public CheckedFunction1<T1, R> reversed() {
        return this;
    }

    default public CheckedFunction1<T1, R> memoized() {
        if (this.isMemoized()) {
            return this;
        }
        HashMap cache = new HashMap();
        return (CheckedFunction1<Object, Object> & Memoized)t1 -> {
            Map map = cache;
            synchronized (map) {
                if (cache.containsKey(t1)) {
                    return cache.get(t1);
                }
                R value = this.apply(t1);
                cache.put(t1, value);
                return value;
            }
        };
    }

    default public boolean isMemoized() {
        return this instanceof Memoized;
    }

    default public Function1<T1, R> recover(Function<? super Throwable, ? extends Function<? super T1, ? extends R>> recover) {
        Objects.requireNonNull(recover, "recover is null");
        return t1 -> {
            try {
                return this.apply(t1);
            }
            catch (Throwable throwable) {
                Function func = (Function)recover.apply(throwable);
                Objects.requireNonNull(func, () -> "recover return null for " + throwable.getClass() + ": " + throwable.getMessage());
                return func.apply(t1);
            }
        };
    }

    default public Function1<T1, R> unchecked() {
        return t1 -> {
            try {
                return this.apply(t1);
            }
            catch (Throwable t) {
                return CheckedFunction1Module.sneakyThrow(t);
            }
        };
    }

    default public <V> CheckedFunction1<T1, V> andThen(CheckedFunction1<? super R, ? extends V> after) {
        Objects.requireNonNull(after, "after is null");
        return t1 -> after.apply((R)this.apply(t1));
    }

    default public <V> CheckedFunction1<V, R> compose(CheckedFunction1<? super V, ? extends T1> before) {
        Objects.requireNonNull(before, "before is null");
        return v -> this.apply(before.apply((Object)v));
    }
}

