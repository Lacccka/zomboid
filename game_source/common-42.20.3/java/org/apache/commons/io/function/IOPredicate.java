/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Predicate;
import org.apache.commons.io.function.Constants;
import org.apache.commons.io.function.Uncheck;

@FunctionalInterface
public interface IOPredicate<T> {
    public static <T> IOPredicate<T> alwaysFalse() {
        return Constants.IO_PREDICATE_FALSE;
    }

    public static <T> IOPredicate<T> alwaysTrue() {
        return Constants.IO_PREDICATE_TRUE;
    }

    public static <T> IOPredicate<T> isEqual(Object target) {
        return null == target ? Objects::isNull : object -> target.equals(object);
    }

    default public IOPredicate<T> and(IOPredicate<? super T> other) {
        Objects.requireNonNull(other);
        return t -> this.test(t) && other.test(t);
    }

    default public Predicate<T> asPredicate() {
        return t -> Uncheck.test(this, t);
    }

    default public IOPredicate<T> negate() {
        return t -> !this.test(t);
    }

    default public IOPredicate<T> or(IOPredicate<? super T> other) {
        Objects.requireNonNull(other);
        return t -> this.test(t) || other.test(t);
    }

    public boolean test(T var1) throws IOException;
}

