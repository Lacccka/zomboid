/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.CheckedFunction1;
import io.vavr.CheckedFunction2;
import io.vavr.CheckedFunction3;
import io.vavr.CheckedFunction4;
import io.vavr.CheckedFunction5;
import io.vavr.CheckedFunction6;
import io.vavr.CheckedFunction7;
import io.vavr.CheckedFunction8Module;
import io.vavr.Function1;
import io.vavr.Function8;
import io.vavr.Memoized;
import io.vavr.Tuple;
import io.vavr.Tuple8;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction8<T1, T2, T3, T4, T5, T6, T7, T8, R>
extends Serializable {
    public static final long serialVersionUID = 1L;

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> CheckedFunction8<T1, T2, T3, T4, T5, T6, T7, T8, R> constant(R value) {
        return (t1, t2, t3, t4, t5, t6, t7, t8) -> value;
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> CheckedFunction8<T1, T2, T3, T4, T5, T6, T7, T8, R> of(CheckedFunction8<T1, T2, T3, T4, T5, T6, T7, T8, R> methodReference) {
        return methodReference;
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Function8<T1, T2, T3, T4, T5, T6, T7, T8, Option<R>> lift(CheckedFunction8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> partialFunction) {
        return (t1, t2, t3, t4, t5, t6, t7, t8) -> Try.of(() -> partialFunction.apply(t1, t2, t3, t4, t5, t6, t7, t8)).toOption();
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Function8<T1, T2, T3, T4, T5, T6, T7, T8, Try<R>> liftTry(CheckedFunction8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> partialFunction) {
        return (t1, t2, t3, t4, t5, t6, t7, t8) -> Try.of(() -> partialFunction.apply(t1, t2, t3, t4, t5, t6, t7, t8));
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> CheckedFunction8<T1, T2, T3, T4, T5, T6, T7, T8, R> narrow(CheckedFunction8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> f) {
        return f;
    }

    public R apply(T1 var1, T2 var2, T3 var3, T4 var4, T5 var5, T6 var6, T7 var7, T8 var8) throws Throwable;

    default public CheckedFunction7<T2, T3, T4, T5, T6, T7, T8, R> apply(T1 t1) {
        return (t2, t3, t4, t5, t6, t7, t8) -> this.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    default public CheckedFunction6<T3, T4, T5, T6, T7, T8, R> apply(T1 t1, T2 t2) {
        return (t3, t4, t5, t6, t7, t8) -> this.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    default public CheckedFunction5<T4, T5, T6, T7, T8, R> apply(T1 t1, T2 t2, T3 t3) {
        return (t4, t5, t6, t7, t8) -> this.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    default public CheckedFunction4<T5, T6, T7, T8, R> apply(T1 t1, T2 t2, T3 t3, T4 t4) {
        return (t5, t6, t7, t8) -> this.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    default public CheckedFunction3<T6, T7, T8, R> apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {
        return (t6, t7, t8) -> this.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    default public CheckedFunction2<T7, T8, R> apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {
        return (t7, t8) -> this.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    default public CheckedFunction1<T8, R> apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7) {
        return t8 -> this.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    default public int arity() {
        return 8;
    }

    default public Function1<T1, Function1<T2, Function1<T3, Function1<T4, Function1<T5, Function1<T6, Function1<T7, CheckedFunction1<T8, R>>>>>>>> curried() {
        return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> t7 -> t8 -> this.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    default public CheckedFunction1<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>, R> tupled() {
        return t -> this.apply(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8);
    }

    default public CheckedFunction8<T8, T7, T6, T5, T4, T3, T2, T1, R> reversed() {
        return (t8, t7, t6, t5, t4, t3, t2, t1) -> this.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    default public CheckedFunction8<T1, T2, T3, T4, T5, T6, T7, T8, R> memoized() {
        if (this.isMemoized()) {
            return this;
        }
        HashMap cache = new HashMap();
        return (CheckedFunction8<Object, Object, Object, Object, Object, Object, Object, Object, Object> & Memoized)(t1, t2, t3, t4, t5, t6, t7, t8) -> {
            Tuple8<Object, Object, Object, Object, Object, Object, Object, Object> key = Tuple.of(t1, t2, t3, t4, t5, t6, t7, t8);
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

    default public Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> recover(Function<? super Throwable, ? extends Function8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R>> recover) {
        Objects.requireNonNull(recover, "recover is null");
        return (t1, t2, t3, t4, t5, t6, t7, t8) -> {
            try {
                return this.apply(t1, t2, t3, t4, t5, t6, t7, t8);
            }
            catch (Throwable throwable) {
                Function8 func = (Function8)recover.apply(throwable);
                Objects.requireNonNull(func, () -> "recover return null for " + throwable.getClass() + ": " + throwable.getMessage());
                return func.apply(t1, t2, t3, t4, t5, t6, t7, t8);
            }
        };
    }

    default public Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> unchecked() {
        return (t1, t2, t3, t4, t5, t6, t7, t8) -> {
            try {
                return this.apply(t1, t2, t3, t4, t5, t6, t7, t8);
            }
            catch (Throwable t) {
                return CheckedFunction8Module.sneakyThrow(t);
            }
        };
    }

    default public <V> CheckedFunction8<T1, T2, T3, T4, T5, T6, T7, T8, V> andThen(CheckedFunction1<? super R, ? extends V> after) {
        Objects.requireNonNull(after, "after is null");
        return (t1, t2, t3, t4, t5, t6, t7, t8) -> after.apply((R)this.apply(t1, t2, t3, t4, t5, t6, t7, t8));
    }
}

