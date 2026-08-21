/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.GwtIncompatible;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class Predicates {
    private Predicates() {
    }

    @SafeVarargs
    public static <T> Predicate<T> allOf(Predicate<T> ... predicates) {
        Objects.requireNonNull(predicates, "predicates is null");
        return t -> List.of(predicates).foldLeft(true, (bool, pred) -> bool != false && pred.test(t));
    }

    @SafeVarargs
    public static <T> Predicate<T> anyOf(Predicate<T> ... predicates) {
        Objects.requireNonNull(predicates, "predicates is null");
        return t -> List.of(predicates).find(pred -> pred.test(t)).isDefined();
    }

    public static <T> Predicate<Iterable<T>> exists(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return iterable -> Iterator.ofAll(iterable).exists(predicate);
    }

    public static <T> Predicate<Iterable<T>> forAll(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return iterable -> Iterator.ofAll(iterable).forAll(predicate);
    }

    @GwtIncompatible
    public static <T> Predicate<T> instanceOf(Class<? extends T> type) {
        Objects.requireNonNull(type, "type is null");
        return obj -> obj != null && type.isAssignableFrom(obj.getClass());
    }

    public static <T> Predicate<T> is(T value) {
        return obj -> Objects.equals(obj, value);
    }

    @SafeVarargs
    public static <T> Predicate<T> isIn(T ... values2) {
        Objects.requireNonNull(values2, "values is null");
        return obj -> List.of(values2).find(value -> Objects.equals(value, obj)).isDefined();
    }

    public static <T> Predicate<T> isNotNull() {
        return Objects::nonNull;
    }

    public static <T> Predicate<T> isNull() {
        return Objects::isNull;
    }

    @SafeVarargs
    public static <T> Predicate<T> noneOf(Predicate<T> ... predicates) {
        Objects.requireNonNull(predicates, "predicates is null");
        return Predicates.anyOf(predicates).negate();
    }

    public static <T> Predicate<T> not(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return predicate.negate();
    }
}

