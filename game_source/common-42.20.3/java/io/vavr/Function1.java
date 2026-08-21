/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.Memoized;
import io.vavr.PartialFunction;
import io.vavr.Tuple1;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@FunctionalInterface
public interface Function1<T1, R>
extends Serializable,
Function<T1, R> {
    public static final long serialVersionUID = 1L;

    public static <T1, R> Function1<T1, R> constant(R value) {
        return t1 -> value;
    }

    public static <T1, R> Function1<T1, R> of(Function1<T1, R> methodReference) {
        return methodReference;
    }

    public static <T1, R> Function1<T1, Option<R>> lift(Function<? super T1, ? extends R> partialFunction) {
        return t1 -> Try.of(() -> partialFunction.apply((Object)t1)).toOption();
    }

    public static <T1, R> Function1<T1, Try<R>> liftTry(Function<? super T1, ? extends R> partialFunction) {
        return t1 -> Try.of(() -> partialFunction.apply((Object)t1));
    }

    public static <T1, R> Function1<T1, R> narrow(Function1<? super T1, ? extends R> f) {
        return f;
    }

    public static <T> Function1<T, T> identity() {
        return t -> t;
    }

    @Override
    public R apply(T1 var1);

    default public int arity() {
        return 1;
    }

    default public Function1<T1, R> curried() {
        return this;
    }

    default public Function1<Tuple1<T1>, R> tupled() {
        return t -> this.apply((T1)t._1);
    }

    default public Function1<T1, R> reversed() {
        return this;
    }

    default public Function1<T1, R> memoized() {
        if (this.isMemoized()) {
            return this;
        }
        HashMap cache = new HashMap();
        return (Function1<Object, Object> & Memoized)t1 -> {
            Map map = cache;
            synchronized (map) {
                if (cache.containsKey(t1)) {
                    return cache.get(t1);
                }
                R value = this.apply((T1)t1);
                cache.put(t1, value);
                return value;
            }
        };
    }

    default public boolean isMemoized() {
        return this instanceof Memoized;
    }

    default public PartialFunction<T1, R> partial(final Predicate<? super T1> isDefinedAt) {
        Objects.requireNonNull(isDefinedAt, "isDefinedAt is null");
        final Function1 self = this;
        return new PartialFunction<T1, R>(){
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isDefinedAt(T1 t1) {
                return isDefinedAt.test(t1);
            }

            @Override
            public R apply(T1 t1) {
                return self.apply(t1);
            }
        };
    }

    @Override
    default public <V> Function1<T1, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after, "after is null");
        return t1 -> after.apply((R)this.apply((T1)t1));
    }

    @Override
    default public <V> Function1<V, R> compose(Function<? super V, ? extends T1> before) {
        Objects.requireNonNull(before, "before is null");
        return v -> this.apply((T1)before.apply((Object)v));
    }
}

