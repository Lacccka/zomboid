/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.Tuple;
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
import java.util.function.Function;

public final class Tuple1<T1>
implements Tuple,
Comparable<Tuple1<T1>>,
Serializable {
    private static final long serialVersionUID = 1L;
    public final T1 _1;

    public Tuple1(T1 t1) {
        this._1 = t1;
    }

    public static <T1> Comparator<Tuple1<T1>> comparator(Comparator<? super T1> t1Comp) {
        return (Comparator & Serializable)(t1, t2) -> {
            int check1 = t1Comp.compare((Object)t1._1, (Object)t2._1);
            if (check1 != 0) {
                return check1;
            }
            return 0;
        };
    }

    private static <U1 extends Comparable<? super U1>> int compareTo(Tuple1<?> o1, Tuple1<?> o2) {
        Tuple1<?> t1 = o1;
        Tuple1<?> t2 = o2;
        int check1 = ((Comparable)t1._1).compareTo(t2._1);
        if (check1 != 0) {
            return check1;
        }
        return 0;
    }

    @Override
    public int arity() {
        return 1;
    }

    @Override
    public int compareTo(Tuple1<T1> that) {
        return Tuple1.compareTo(this, that);
    }

    public T1 _1() {
        return this._1;
    }

    public Tuple1<T1> update1(T1 value) {
        return new Tuple1<T1>(value);
    }

    public <U1> Tuple1<U1> map(Function<? super T1, ? extends U1> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return Tuple.of(mapper.apply(this._1));
    }

    public <U> U apply(Function<? super T1, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this._1);
    }

    @Override
    public Seq<?> toSeq() {
        return List.of(this._1);
    }

    public <T2> Tuple2<T1, T2> append(T2 t2) {
        return Tuple.of(this._1, t2);
    }

    public <T2> Tuple2<T1, T2> concat(Tuple1<T2> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, tuple._1);
    }

    public <T2, T3> Tuple3<T1, T2, T3> concat(Tuple2<T2, T3> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, tuple._1, tuple._2);
    }

    public <T2, T3, T4> Tuple4<T1, T2, T3, T4> concat(Tuple3<T2, T3, T4> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, tuple._1, tuple._2, tuple._3);
    }

    public <T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> concat(Tuple4<T2, T3, T4, T5> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, tuple._1, tuple._2, tuple._3, tuple._4);
    }

    public <T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> concat(Tuple5<T2, T3, T4, T5, T6> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, tuple._1, tuple._2, tuple._3, tuple._4, tuple._5);
    }

    public <T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> concat(Tuple6<T2, T3, T4, T5, T6, T7> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6);
    }

    public <T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> concat(Tuple7<T2, T3, T4, T5, T6, T7, T8> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6, tuple._7);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Tuple1)) {
            return false;
        }
        Tuple1 that = (Tuple1)o;
        return Objects.equals(this._1, that._1);
    }

    public int hashCode() {
        return Tuple.hash(this._1);
    }

    public String toString() {
        return "(" + this._1 + ")";
    }
}

