/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.control.Option;
import java.util.Objects;
import java.util.function.BiFunction;

public interface Foldable<T> {
    default public T fold(T zero, BiFunction<? super T, ? super T, ? extends T> combine) {
        Objects.requireNonNull(combine, "combine is null");
        return this.foldLeft(zero, combine);
    }

    public <U> U foldLeft(U var1, BiFunction<? super U, ? super T, ? extends U> var2);

    public <U> U foldRight(U var1, BiFunction<? super T, ? super U, ? extends U> var2);

    default public T reduce(BiFunction<? super T, ? super T, ? extends T> op) {
        Objects.requireNonNull(op, "op is null");
        return this.reduceLeft(op);
    }

    default public Option<T> reduceOption(BiFunction<? super T, ? super T, ? extends T> op) {
        Objects.requireNonNull(op, "op is null");
        return this.reduceLeftOption(op);
    }

    public T reduceLeft(BiFunction<? super T, ? super T, ? extends T> var1);

    public Option<T> reduceLeftOption(BiFunction<? super T, ? super T, ? extends T> var1);

    public T reduceRight(BiFunction<? super T, ? super T, ? extends T> var1);

    public Option<T> reduceRightOption(BiFunction<? super T, ? super T, ? extends T> var1);
}

