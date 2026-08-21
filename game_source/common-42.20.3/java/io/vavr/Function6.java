/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.Function4;
import io.vavr.Function5;
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
public interface Function6<T1, T2, T3, T4, T5, T6, R>
extends Serializable {
    public static final long serialVersionUID = 1L;

    public static <T1, T2, T3, T4, T5, T6, R> Function6<T1, T2, T3, T4, T5, T6, R> constant(R value) {
        return (t1, t2, t3, t4, t5, t6) -> value;
    }

    public static <T1, T2, T3, T4, T5, T6, R> Function6<T1, T2, T3, T4, T5, T6, R> of(Function6<T1, T2, T3, T4, T5, T6, R> methodReference) {
        return methodReference;
    }

    public static <T1, T2, T3, T4, T5, T6, R> Function6<T1, T2, T3, T4, T5, T6, Option<R>> lift(Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> partialFunction) {
        return (t1, t2, t3, t4, t5, t6) -> Try.of(() -> partialFunction.apply(t1, t2, t3, t4, t5, t6)).toOption();
    }

    public static <T1, T2, T3, T4, T5, T6, R> Function6<T1, T2, T3, T4, T5, T6, Try<R>> liftTry(Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> partialFunction) {
        return (t1, t2, t3, t4, t5, t6) -> Try.of(() -> partialFunction.apply(t1, t2, t3, t4, t5, t6));
    }

    public static <T1, T2, T3, T4, T5, T6, R> Function6<T1, T2, T3, T4, T5, T6, R> narrow(Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> f) {
        return f;
    }

    public R apply(T1 var1, T2 var2, T3 var3, T4 var4, T5 var5, T6 var6);

    default public Function5<T2, T3, T4, T5, T6, R> apply(T1 t1) {
        return (t2, t3, t4, t5, t6) -> this.apply(t1, t2, t3, t4, t5, t6);
    }

    default public Function4<T3, T4, T5, T6, R> apply(T1 t1, T2 t2) {
        return (t3, t4, t5, t6) -> this.apply(t1, t2, t3, t4, t5, t6);
    }

    default public Function3<T4, T5, T6, R> apply(T1 t1, T2 t2, T3 t3) {
        return (t4, t5, t6) -> this.apply(t1, t2, t3, t4, t5, t6);
    }

    default public Function2<T5, T6, R> apply(T1 t1, T2 t2, T3 t3, T4 t4) {
        return (t5, t6) -> this.apply(t1, t2, t3, t4, t5, t6);
    }

    default public Function1<T6, R> apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {
        return t6 -> this.apply(t1, t2, t3, t4, t5, t6);
    }

    default public int arity() {
        return 6;
    }

    default public Function1<T1, Function1<T2, Function1<T3, Function1<T4, Function1<T5, Function1<T6, R>>>>>> curried() {
        return t1 -> t2 -> t3 -> t4 -> t5 -> t6 -> this.apply(t1, t2, t3, t4, t5, t6);
    }

    default public Function1<Tuple6<T1, T2, T3, T4, T5, T6>, R> tupled() {
        return t -> this.apply(t._1, t._2, t._3, t._4, t._5, t._6);
    }

    default public Function6<T6, T5, T4, T3, T2, T1, R> reversed() {
        return (t6, t5, t4, t3, t2, t1) -> this.apply(t1, t2, t3, t4, t5, t6);
    }

    default public Function6<T1, T2, T3, T4, T5, T6, R> memoized() {
        if (this.isMemoized()) {
            return this;
        }
        HashMap cache = new HashMap();
        return (Function6<Object, Object, Object, Object, Object, Object, Object> & Memoized)(t1, t2, t3, t4, t5, t6) -> {
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

    default public <V> Function6<T1, T2, T3, T4, T5, T6, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after, "after is null");
        return (t1, t2, t3, t4, t5, t6) -> after.apply((R)this.apply(t1, t2, t3, t4, t5, t6));
    }
}

