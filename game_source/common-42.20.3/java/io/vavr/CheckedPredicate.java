/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.CheckedPredicateModule;
import java.util.function.Predicate;

@FunctionalInterface
public interface CheckedPredicate<T> {
    public static <T> CheckedPredicate<T> of(CheckedPredicate<T> methodReference) {
        return methodReference;
    }

    public boolean test(T var1) throws Throwable;

    default public CheckedPredicate<T> negate() {
        return t -> !this.test(t);
    }

    default public Predicate<T> unchecked() {
        return t -> {
            try {
                return this.test(t);
            }
            catch (Throwable x) {
                return (Boolean)CheckedPredicateModule.sneakyThrow(x);
            }
        };
    }
}

