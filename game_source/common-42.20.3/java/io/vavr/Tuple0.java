/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.Tuple;
import io.vavr.Tuple1;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import io.vavr.Tuple5;
import io.vavr.Tuple6;
import io.vavr.Tuple7;
import io.vavr.Tuple8;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Supplier;

public final class Tuple0
implements Tuple,
Comparable<Tuple0>,
Serializable {
    private static final long serialVersionUID = 1L;
    private static final Tuple0 INSTANCE = new Tuple0();
    private static final Comparator<Tuple0> COMPARATOR = (Comparator & Serializable)(t1, t2) -> 0;

    private Tuple0() {
    }

    public static Tuple0 instance() {
        return INSTANCE;
    }

    public static Comparator<Tuple0> comparator() {
        return COMPARATOR;
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public int compareTo(Tuple0 that) {
        return 0;
    }

    public <U> U apply(Supplier<? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.get();
    }

    @Override
    public Seq<?> toSeq() {
        return List.empty();
    }

    public <T1> Tuple1<T1> append(T1 t1) {
        return Tuple.of(t1);
    }

    public <T1> Tuple1<T1> concat(Tuple1<T1> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(tuple._1);
    }

    public <T1, T2> Tuple2<T1, T2> concat(Tuple2<T1, T2> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(tuple._1, tuple._2);
    }

    public <T1, T2, T3> Tuple3<T1, T2, T3> concat(Tuple3<T1, T2, T3> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(tuple._1, tuple._2, tuple._3);
    }

    public <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> concat(Tuple4<T1, T2, T3, T4> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(tuple._1, tuple._2, tuple._3, tuple._4);
    }

    public <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> concat(Tuple5<T1, T2, T3, T4, T5> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5);
    }

    public <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> concat(Tuple6<T1, T2, T3, T4, T5, T6> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6);
    }

    public <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> concat(Tuple7<T1, T2, T3, T4, T5, T6, T7> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6, tuple._7);
    }

    public <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> concat(Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6, tuple._7, tuple._8);
    }

    public boolean equals(Object o) {
        return o == this;
    }

    public int hashCode() {
        return 1;
    }

    public String toString() {
        return "()";
    }

    private Object readResolve() {
        return INSTANCE;
    }
}

