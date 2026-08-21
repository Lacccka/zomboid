/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.io.IOException;
import java.util.Objects;
import org.apache.commons.io.function.IOFunction;

@FunctionalInterface
public interface IOQuadFunction<T, U, V, W, R> {
    default public <X> IOQuadFunction<T, U, V, W, X> andThen(IOFunction<? super R, ? extends X> after) {
        Objects.requireNonNull(after);
        return (t, u, v, w) -> after.apply((R)this.apply(t, u, v, w));
    }

    public R apply(T var1, U var2, V var3, W var4) throws IOException;
}

