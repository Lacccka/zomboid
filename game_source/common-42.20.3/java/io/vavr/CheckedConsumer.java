/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.CheckedConsumerModule;
import java.util.Objects;
import java.util.function.Consumer;

@FunctionalInterface
public interface CheckedConsumer<T> {
    public static <T> CheckedConsumer<T> of(CheckedConsumer<T> methodReference) {
        return methodReference;
    }

    public void accept(T var1) throws Throwable;

    default public CheckedConsumer<T> andThen(CheckedConsumer<? super T> after) {
        Objects.requireNonNull(after, "after is null");
        return t -> {
            this.accept(t);
            after.accept(t);
        };
    }

    default public Consumer<T> unchecked() {
        return t -> {
            try {
                this.accept(t);
            }
            catch (Throwable x) {
                CheckedConsumerModule.sneakyThrow(x);
            }
        };
    }
}

