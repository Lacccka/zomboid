/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.function;

import java.util.Objects;

@FunctionalInterface
public interface FailableIntPredicate<E extends Throwable> {
    public static final FailableIntPredicate FALSE = t -> false;
    public static final FailableIntPredicate TRUE = t -> true;

    public static <E extends Throwable> FailableIntPredicate<E> falsePredicate() {
        return FALSE;
    }

    public static <E extends Throwable> FailableIntPredicate<E> truePredicate() {
        return TRUE;
    }

    default public FailableIntPredicate<E> and(FailableIntPredicate<E> other) {
        Objects.requireNonNull(other);
        return t -> this.test(t) && other.test(t);
    }

    default public FailableIntPredicate<E> negate() {
        return t -> !this.test(t);
    }

    default public FailableIntPredicate<E> or(FailableIntPredicate<E> other) {
        Objects.requireNonNull(other);
        return t -> this.test(t) || other.test(t);
    }

    public boolean test(int var1) throws E;
}

