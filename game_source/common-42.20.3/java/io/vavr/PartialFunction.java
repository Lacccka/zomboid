/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.Function1;
import io.vavr.Value;
import io.vavr.control.Option;
import java.util.function.Function;

public interface PartialFunction<T, R>
extends Function1<T, R> {
    public static final long serialVersionUID = 1L;

    public static <T, R> PartialFunction<T, R> unlift(final Function<? super T, ? extends Option<? extends R>> totalFunction) {
        return new PartialFunction<T, R>(){
            private static final long serialVersionUID = 1L;

            @Override
            public R apply(T t) {
                return ((Option)totalFunction.apply(t)).get();
            }

            @Override
            public boolean isDefinedAt(T value) {
                return ((Option)totalFunction.apply(value)).isDefined();
            }
        };
    }

    public static <T, V extends Value<T>> PartialFunction<V, T> getIfDefined() {
        return new PartialFunction<V, T>(){
            private static final long serialVersionUID = 1L;

            @Override
            public T apply(V v) {
                return v.get();
            }

            @Override
            public boolean isDefinedAt(V v) {
                return !v.isEmpty();
            }
        };
    }

    @Override
    public R apply(T var1);

    public boolean isDefinedAt(T var1);

    default public Function1<T, Option<R>> lift() {
        return t -> Option.when(this.isDefinedAt(t), () -> this.apply((T)t));
    }
}

