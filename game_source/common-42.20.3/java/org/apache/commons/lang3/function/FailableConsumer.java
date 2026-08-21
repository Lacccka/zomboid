/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.function;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface FailableConsumer<T, E extends Throwable> {
    public static final FailableConsumer NOP = Function.identity()::apply;

    public static <T, E extends Throwable> FailableConsumer<T, E> nop() {
        return NOP;
    }

    public void accept(T var1) throws E;

    default public FailableConsumer<T, E> andThen(FailableConsumer<? super T, E> after) {
        Objects.requireNonNull(after);
        return t -> {
            this.accept(t);
            after.accept(t);
        };
    }
}

