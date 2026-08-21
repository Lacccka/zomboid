/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.CheckedFunction1;
import io.vavr.CheckedFunction2Module;
import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Memoized;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction2<T1, T2, R>
extends Serializable {
    public static final long serialVersionUID = 1L;

    public static <T1, T2, R> CheckedFunction2<T1, T2, R> constant(R value) {
        return (t1, t2) -> value;
    }

    public static <T1, T2, R> CheckedFunction2<T1, T2, R> of(CheckedFunction2<T1, T2, R> methodReference) {
        return methodReference;
    }

    public static <T1, T2, R> Function2<T1, T2, Option<R>> lift(CheckedFunction2<? super T1, ? super T2, ? extends R> partialFunction) {
        return (t1, t2) -> Try.of(() -> partialFunction.apply(t1, t2)).toOption();
    }

    public static <T1, T2, R> Function2<T1, T2, Try<R>> liftTry(CheckedFunction2<? super T1, ? super T2, ? extends R> partialFunction) {
        return (t1, t2) -> Try.of(() -> partialFunction.apply(t1, t2));
    }

    public static <T1, T2, R> CheckedFunction2<T1, T2, R> narrow(CheckedFunction2<? super T1, ? super T2, ? extends R> f) {
        return f;
    }

    public R apply(T1 var1, T2 var2) throws Throwable;

    default public CheckedFunction1<T2, R> apply(T1 t1) {
        return t2 -> this.apply(t1, t2);
    }

    default public int arity() {
        return 2;
    }

    default public Function1<T1, CheckedFunction1<T2, R>> curried() {
        return t1 -> t2 -> this.apply(t1, t2);
    }

    default public CheckedFunction1<Tuple2<T1, T2>, R> tupled() {
        return t -> this.apply(t._1, t._2);
    }

    default public CheckedFunction2<T2, T1, R> reversed() {
        return (t2, t1) -> this.apply(t1, t2);
    }

    default public CheckedFunction2<T1, T2, R> memoized() {
        if (this.isMemoized()) {
            return this;
        }
        HashMap cache = new HashMap();
        return (CheckedFunction2<Object, Object, Object> & Memoized)(t1, t2) -> {
            Tuple2<Object, Object> key = Tuple.of(t1, t2);
            Map map = cache;
            synchronized (map) {
                if (cache.containsKey(key)) {
                    return cache.get(key);
                }
                R value = this.tupled().apply(key);
                cache.put(key, value);
                return value;
            }
        };
    }

    default public boolean isMemoized() {
        return this instanceof Memoized;
    }

    default public Function2<T1, T2, R> recover(Function<? super Throwable, ? extends BiFunction<? super T1, ? super T2, ? extends R>> recover) {
        Objects.requireNonNull(recover, "recover is null");
        return (t1, t2) -> {
            try {
                return this.apply(t1, t2);
            }
            catch (Throwable throwable) {
                BiFunction func = (BiFunction)recover.apply(throwable);
                Objects.requireNonNull(func, () -> "recover return null for " + throwable.getClass() + ": " + throwable.getMessage());
                return func.apply(t1, t2);
            }
        };
    }

    default public Function2<T1, T2, R> unchecked() {
        return (t1, t2) -> {
            try {
                return this.apply(t1, t2);
            }
            catch (Throwable t) {
                return CheckedFunction2Module.sneakyThrow(t);
            }
        };
    }

    default public <V> CheckedFunction2<T1, T2, V> andThen(CheckedFunction1<? super R, ? extends V> after) {
        Objects.requireNonNull(after, "after is null");
        return (t1, t2) -> after.apply((R)this.apply(t1, t2));
    }
}

