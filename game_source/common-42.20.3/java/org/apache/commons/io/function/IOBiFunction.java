/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.io.IOException;
import java.util.Objects;
import java.util.function.BiFunction;
import org.apache.commons.io.function.IOFunction;
import org.apache.commons.io.function.Uncheck;

@FunctionalInterface
public interface IOBiFunction<T, U, R> {
    default public <V> IOBiFunction<T, U, V> andThen(IOFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (t, u) -> after.apply((R)this.apply(t, u));
    }

    public R apply(T var1, U var2) throws IOException;

    default public BiFunction<T, U, R> asBiFunction() {
        return (t, u) -> Uncheck.apply(this, t, u);
    }
}

