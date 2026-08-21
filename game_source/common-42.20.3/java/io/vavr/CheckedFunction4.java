/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.CheckedFunction1;
import io.vavr.CheckedFunction2;
import io.vavr.CheckedFunction3;
import io.vavr.CheckedFunction4Module;
import io.vavr.Function1;
import io.vavr.Function4;
import io.vavr.Memoized;
import io.vavr.Tuple;
import io.vavr.Tuple4;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction4<T1, T2, T3, T4, R>
extends Serializable {
    public static final long serialVersionUID = 1L;

    public static <T1, T2, T3, T4, R> CheckedFunction4<T1, T2, T3, T4, R> constant(R value) {
        return (t1, t2, t3, t4) -> value;
    }

    public static <T1, T2, T3, T4, R> CheckedFunction4<T1, T2, T3, T4, R> of(CheckedFunction4<T1, T2, T3, T4, R> methodReference) {
        return methodReference;
    }

    public static <T1, T2, T3, T4, R> Function4<T1, T2, T3, T4, Option<R>> lift(CheckedFunction4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> partialFunction) {
        return (t1, t2, t3, t4) -> Try.of(() -> partialFunction.apply(t1, t2, t3, t4)).toOption();
    }

    public static <T1, T2, T3, T4, R> Function4<T1, T2, T3, T4, Try<R>> liftTry(CheckedFunction4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> partialFunction) {
        return (t1, t2, t3, t4) -> Try.of(() -> partialFunction.apply(t1, t2, t3, t4));
    }

    public static <T1, T2, T3, T4, R> CheckedFunction4<T1, T2, T3, T4, R> narrow(CheckedFunction4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> f) {
        return f;
    }

    public R apply(T1 var1, T2 var2, T3 var3, T4 var4) throws Throwable;

    default public CheckedFunction3<T2, T3, T4, R> apply(T1 t1) {
        return (t2, t3, t4) -> this.apply(t1, t2, t3, t4);
    }

    default public CheckedFunction2<T3, T4, R> apply(T1 t1, T2 t2) {
        return (t3, t4) -> this.apply(t1, t2, t3, t4);
    }

    default public CheckedFunction1<T4, R> apply(T1 t1, T2 t2, T3 t3) {
        return t4 -> this.apply(t1, t2, t3, t4);
    }

    default public int arity() {
        return 4;
    }

    default public Function1<T1, Function1<T2, Function1<T3, CheckedFunction1<T4, R>>>> curried() {
        return t1 -> t2 -> t3 -> t4 -> this.apply(t1, t2, t3, t4);
    }

    default public CheckedFunction1<Tuple4<T1, T2, T3, T4>, R> tupled() {
        return t -> this.apply(t._1, t._2, t._3, t._4);
    }

    default public CheckedFunction4<T4, T3, T2, T1, R> reversed() {
        return (t4, t3, t2, t1) -> this.apply(t1, t2, t3, t4);
    }

    default public CheckedFunction4<T1, T2, T3, T4, R> memoized() {
        if (this.isMemoized()) {
            return this;
        }
        HashMap cache = new HashMap();
        return (CheckedFunction4<Object, Object, Object, Object, Object> & Memoized)(t1, t2, t3, t4) -> {
            Tuple4<Object, Object, Object, Object> key = Tuple.of(t1, t2, t3, t4);
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

    default public Function4<T1, T2, T3, T4, R> recover(Function<? super Throwable, ? extends Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R>> recover) {
        Objects.requireNonNull(recover, "recover is null");
        return (t1, t2, t3, t4) -> {
            try {
                return this.apply(t1, t2, t3, t4);
            }
            catch (Throwable throwable) {
                Function4 func = (Function4)recover.apply(throwable);
                Objects.requireNonNull(func, () -> "recover return null for " + throwable.getClass() + ": " + throwable.getMessage());
                return func.apply(t1, t2, t3, t4);
            }
        };
    }

    default public Function4<T1, T2, T3, T4, R> unchecked() {
        return (t1, t2, t3, t4) -> {
            try {
                return this.apply(t1, t2, t3, t4);
            }
            catch (Throwable t) {
                return CheckedFunction4Module.sneakyThrow(t);
            }
        };
    }

    default public <V> CheckedFunction4<T1, T2, T3, T4, V> andThen(CheckedFunction1<? super R, ? extends V> after) {
        Objects.requireNonNull(after, "after is null");
        return (t1, t2, t3, t4) -> after.apply((R)this.apply(t1, t2, t3, t4));
    }
}

