/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.CheckedFunction1;
import io.vavr.CheckedFunction2;
import io.vavr.CheckedFunction3;
import io.vavr.CheckedFunction4;
import io.vavr.CheckedFunction5;
import io.vavr.CheckedFunction6Module;
import io.vavr.Function1;
import io.vavr.Function6;
import io.vavr.Memoized;
import io.vavr.Tuple;
import io.vavr.Tuple6;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction6<T1, T2, T3, T4, T5, T6, R>
extends Serializable {
    public static final long serialVersionUID = 1L;

    public static <T1, T2, T3, T4, T5, T6, R> CheckedFunction6<T1, T2, T3, T4, T5, T6, R> constant(R value) {
        return (t1, t2, t3, t4, t5, t6) -> value;
    }

    public static <T1, T2, T3, T4, T5, T6, R> CheckedFunction6<T1, T2, T3, T4, T5, T6, R> of(CheckedFunction6<T1, T2, T3, T4, T5, T6, R> methodReference) {
        return methodReference;
    }

    public static <T1, T2, T3, T4, T5, T6, R> Function6<T1, T2, T3, T4, T5, T6, Option<R>> lift(CheckedFunction6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> partialFunction) {
        return (t1, t2, t3, t4, t5, t6) -> Try.of(() -> partialFunction.apply(t1, t2, t3, t4, t5, t6)).toOption();
    }

    public static <T1, T2, T3, T4, T5, T6, R> Function6<T1, T2, T3, T4, T5, T6, Try<R>> liftTry(CheckedFunction6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> partialFunction) {
        return (t1, t2, t3, t4, t5, t6) -> Try.of(() -> partialFunction.apply(t1, t2, t3, t4, t5, t6));
    }

    public static <T1, T2, T3, T4, T5, T6, R> CheckedFunction6<T1, T2, T3, T4, T5, T6, R> narrow(CheckedFunction6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> f) {
        return f;
    }

    public R apply(T1 var1, T2 var2, T3 var3, T4 var4, T5 var5, T6 var6) throws Throwable;

    default public CheckedFunction5<T2, T3, T4, T5, T6, R> apply(T1 t1) {
        return (t2, t3, t4, t5, t6) -> this.apply(t1, t2, t3, t4, t5, t6);
    }

    default public CheckedFunction4<T3, T4, T5, T6, R> apply(T1 t1, T2 t2) {
        return (t3, t4, t5, t6) -> this.apply(t1, t2, t3, t4, t5, t6);
    }

    default public CheckedFunction3<T4, T5, T6, R> apply(T1 t1, T2 t2, T3 t3) {
        return (t4, t5, t6) -> this.apply(t1, t2, t3, t4, t5, t6);
    }

    default public CheckedFunction2<T5, T6, R> apply(T1 t1, T2 t2, T3 t3, T4 t4) {
        return (t5, t6) -> this.apply(t1, t2, t3, t4, t5, t6);
    }

    default public CheckedFunction1<T6, R> apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {
        return t6 -> this.apply(t1, t2, t3, t4, t5, t6);
    }

    default public int arity() {
        return 6;
    }

    default public Function1<T1, Function1<T2, Function1<T3, Function1<T4, Function1<T5, CheckedFunction1<T6, R>>>>>> curried() {
        return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> this.apply(t1, t2, t3, t4, t5, t6);
    }

    default public CheckedFunction1<Tuple6<T1, T2, T3, T4, T5, T6>, R> tupled() {
        return t -> this.apply(t._1, t._2, t._3, t._4, t._5, t._6);
    }

    default public CheckedFunction6<T6, T5, T4, T3, T2, T1, R> reversed() {
        return (t6, t5, t4, t3, t2, t1) -> this.apply(t1, t2, t3, t4, t5, t6);
    }

    default public CheckedFunction6<T1, T2, T3, T4, T5, T6, R> memoized() {
        if (this.isMemoized()) {
            return this;
        }
        HashMap cache = new HashMap();
        return (CheckedFunction6<Object, Object, Object, Object, Object, Object, Object> & Memoized)(t1, t2, t3, t4, t5, t6) -> {
            Tuple6<Object, Object, Object, Object, Object, Object> key = Tuple.of(t1, t2, t3, t4, t5, t6);
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

    default public Function6<T1, T2, T3, T4, T5, T6, R> recover(Function<? super Throwable, ? extends Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R>> recover) {
        Objects.requireNonNull(recover, "recover is null");
        return (t1, t2, t3, t4, t5, t6) -> {
            try {
                return this.apply(t1, t2, t3, t4, t5, t6);
            }
            catch (Throwable throwable) {
                Function6 func = (Function6)recover.apply(throwable);
                Objects.requireNonNull(func, () -> "recover return null for " + throwable.getClass() + ": " + throwable.getMessage());
                return func.apply(t1, t2, t3, t4, t5, t6);
            }
        };
    }

    default public Function6<T1, T2, T3, T4, T5, T6, R> unchecked() {
        return (t1, t2, t3, t4, t5, t6) -> {
            try {
                return this.apply(t1, t2, t3, t4, t5, t6);
            }
            catch (Throwable t) {
                return CheckedFunction6Module.sneakyThrow(t);
            }
        };
    }

    default public <V> CheckedFunction6<T1, T2, T3, T4, T5, T6, V> andThen(CheckedFunction1<? super R, ? extends V> after) {
        Objects.requireNonNull(after, "after is null");
        return (t1, t2, t3, t4, t5, t6) -> after.apply((R)this.apply(t1, t2, t3, t4, t5, t6));
    }
}

