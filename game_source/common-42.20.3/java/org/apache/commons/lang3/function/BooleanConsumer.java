/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.function;

import java.util.Objects;

@FunctionalInterface
public interface BooleanConsumer {
    public static final BooleanConsumer NOP = t -> {};

    public static BooleanConsumer nop() {
        return NOP;
    }

    public void accept(boolean var1);

    default public BooleanConsumer andThen(BooleanConsumer after) {
        Objects.requireNonNull(after);
        return t -> {
            this.accept(t);
            after.accept(t);
        };
    }
}

