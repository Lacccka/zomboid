/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Memoized;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface Function3<T1, T2, T3, R>
extends Serializable {
    public static final long serialVersionUID = 1L;

    public static <T1, T2, T3, R> Function3<T1, T2, T3, R> constant(R value) {
        return (t1, t2, t3) -> value;
    }

    public static <T1, T2, T3, R> Function3<T1, T2, T3, R> of(Function3<T1, T2, T3, R> methodReference) {
        return methodReference;
    }

    public static <T1, T2, T3, R> Function3<T1, T2, T3, Option<R>> lift(Function3<? super T1, ? super T2, ? super T3, ? extends R> partialFunction) {
        return (t1, t2, t3) -> Try.of(() -> partialFunction.apply(t1, t2, t3)).toOption();
    }

    public static <T1, T2, T3, R> Function3<T1, T2, T3, Try<R>> liftTry(Function3<? super T1, ? super T2, ? super T3, ? extends R> partialFunction) {
        return (t1, t2, t3) -> Try.of(() -> partialFunction.apply(t1, t2, t3));
    }

    public static <T1, T2, T3, R> Function3<T1, T2, T3, R> narrow(Function3<? super T1, ? super T2, ? super T3, ? extends R> f) {
        return f;
    }

    public R apply(T1 var1, T2 var2, T3 var3);

    default public Function2<T2, T3, R> apply(T1 t1) {
        return (t2, t3) -> this.apply(t1, t2, t3);
    }

    default public Function1<T3, R> apply(T1 t1, T2 t2) {
        return t3 -> this.apply(t1, t2, t3);
    }

    default public int arity() {
        return 3;
    }

    default public Function1<T1, Function1<T2, Function1<T3, R>>> curried() {
        return t1 -> t2 -> t3 -> this.apply(t1, t2, t3);
    }

    default public Function1<Tuple3<T1, T2, T3>, R> tupled() {
        return t -> this.apply(t._1, t._2, t._3);
    }

    default public Function3<T3, T2, T1, R> reversed() {
        return (t3, t2, t1) -> this.apply(t1, t2, t3);
    }

    default public Function3<T1, T2, T3, R> memoized() {
        if (this.isMemoized()) {
            return this;
        }
        HashMap cache = new HashMap();
        return (Function3<Object, Object, Object, Object> & Memoized)(t1, t2, t3) -> {
            Tuple3<Object, Object, Object> key = Tuple.of(t1, t2, t3);
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

    default public <V> Function3<T1, T2, T3, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after, "after is null");
        return (t1, t2, t3) -> after.apply((R)this.apply(t1, t2, t3));
    }
}

