/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.Tuple;
import io.vavr.Tuple1;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import io.vavr.Tuple5;
import io.vavr.Tuple6;
import io.vavr.Tuple7;
import io.vavr.Tuple8;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class Tuple2<T1, T2>
implements Tuple,
Comparable<Tuple2<T1, T2>>,
Serializable {
    private static final long serialVersionUID = 1L;
    public final T1 _1;
    public final T2 _2;

    public Tuple2(T1 t1, T2 t2) {
        this._1 = t1;
        this._2 = t2;
    }

    public static <T1, T2> Comparator<Tuple2<T1, T2>> comparator(Comparator<? super T1> t1Comp, Comparator<? super T2> t2Comp) {
        return (Comparator & Serializable)(t1, t2) -> {
            int check1 = t1Comp.compare((Object)t1._1, (Object)t2._1);
            if (check1 != 0) {
                return check1;
            }
            int check2 = t2Comp.compare((Object)t1._2, (Object)t2._2);
            if (check2 != 0) {
                return check2;
            }
            return 0;
        };
    }

    private static <U1 extends Comparable<? super U1>, U2 extends Comparable<? super U2>> int compareTo(Tuple2<?, ?> o1, Tuple2<?, ?> o2) {
        Tuple2<?, ?> t1 = o1;
        Tuple2<?, ?> t2 = o2;
        int check1 = ((Comparable)t1._1).compareTo(t2._1);
        if (check1 != 0) {
            return check1;
        }
        int check2 = ((Comparable)t1._2).compareTo(t2._2);
        if (check2 != 0) {
            return check2;
        }
        return 0;
    }

    @Override
    public int arity() {
        return 2;
    }

    @Override
    public int compareTo(Tuple2<T1, T2> that) {
        return Tuple2.compareTo(this, that);
    }

    public T1 _1() {
        return this._1;
    }

    public Tuple2<T1, T2> update1(T1 value) {
        return new Tuple2<T1, T2>(value, this._2);
    }

    public T2 _2() {
        return this._2;
    }

    public Tuple2<T1, T2> update2(T2 value) {
        return new Tuple2<T1, T2>(this._1, value);
    }

    public Tuple2<T2, T1> swap() {
        return Tuple.of(this._2, this._1);
    }

    public Map.Entry<T1, T2> toEntry() {
        return new AbstractMap.SimpleEntry<T1, T2>(this._1, this._2);
    }

    public <U1, U2> Tuple2<U1, U2> map(BiFunction<? super T1, ? super T2, Tuple2<U1, U2>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return mapper.apply(this._1, this._2);
    }

    public <U1, U2> Tuple2<U1, U2> map(Function<? super T1, ? extends U1> f1, Function<? super T2, ? extends U2> f2) {
        Objects.requireNonNull(f1, "f1 is null");
        Objects.requireNonNull(f2, "f2 is null");
        return Tuple.of(f1.apply(this._1), f2.apply(this._2));
    }

    public <U> Tuple2<U, T2> map1(Function<? super T1, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        U u = mapper.apply(this._1);
        return Tuple.of(u, this._2);
    }

    public <U> Tuple2<T1, U> map2(Function<? super T2, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        U u = mapper.apply(this._2);
        return Tuple.of(this._1, u);
    }

    public <U> U apply(BiFunction<? super T1, ? super T2, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this._1, this._2);
    }

    @Override
    public Seq<?> toSeq() {
        return List.of(this._1, this._2);
    }

    public <T3> Tuple3<T1, T2, T3> append(T3 t3) {
        return Tuple.of(this._1, this._2, t3);
    }

    public <T3> Tuple3<T1, T2, T3> concat(Tuple1<T3> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, this._2, tuple._1);
    }

    public <T3, T4> Tuple4<T1, T2, T3, T4> concat(Tuple2<T3, T4> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, this._2, tuple._1, tuple._2);
    }

    public <T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> concat(Tuple3<T3, T4, T5> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, this._2, tuple._1, tuple._2, tuple._3);
    }

    public <T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> concat(Tuple4<T3, T4, T5, T6> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, this._2, tuple._1, tuple._2, tuple._3, tuple._4);
    }

    public <T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> concat(Tuple5<T3, T4, T5, T6, T7> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, this._2, tuple._1, tuple._2, tuple._3, tuple._4, tuple._5);
    }

    public <T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> concat(Tuple6<T3, T4, T5, T6, T7, T8> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, this._2, tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Tuple2)) {
            return false;
        }
        Tuple2 that = (Tuple2)o;
        return Objects.equals(this._1, that._1) && Objects.equals(this._2, that._2);
    }

    public int hashCode() {
        return Tuple.hash(this._1, this._2);
    }

    public String toString() {
        return "(" + this._1 + ", " + this._2 + ")";
    }
}

