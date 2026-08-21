/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.util.function.UnaryOperator;
import org.apache.commons.io.function.IOFunction;
import org.apache.commons.io.function.Uncheck;

@FunctionalInterface
public interface IOUnaryOperator<T>
extends IOFunction<T, T> {
    public static <T> IOUnaryOperator<T> identity() {
        return t -> t;
    }

    default public UnaryOperator<T> asUnaryOperator() {
        return t -> Uncheck.apply(this, t);
    }
}

