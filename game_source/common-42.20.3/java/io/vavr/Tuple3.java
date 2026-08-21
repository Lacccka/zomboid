/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.Function3;
import io.vavr.Tuple;
import io.vavr.Tuple1;
import io.vavr.Tuple2;
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

public final class Tuple3<T1, T2, T3>
implements Tuple,
Comparable<Tuple3<T1, T2, T3>>,
Serializable {
    private static final long serialVersionUID = 1L;
    public final T1 _1;
    public final T2 _2;
    public final T3 _3;

    public Tuple3(T1 t1, T2 t2, T3 t3) {
        this._1 = t1;
        this._2 = t2;
        this._3 = t3;
    }

    public static <T1, T2, T3> Comparator<Tuple3<T1, T2, T3>> comparator(Comparator<? super T1> t1Comp, Comparator<? super T2> t2Comp, Comparator<? super T3> t3Comp) {
        return (Comparator & Serializable)(t1, t2) -> {
            int check1 = t1Comp.compare((Object)t1._1, (Object)t2._1);
            if (check1 != 0) {
                return check1;
            }
            int check2 = t2Comp.compare((Object)t1._2, (Object)t2._2);
            if (check2 != 0) {
                return check2;
            }
            int check3 = t3Comp.compare((Object)t1._3, (Object)t2._3);
            if (check3 != 0) {
                return check3;
            }
            return 0;
        };
    }

    private static <U1 extends Comparable<? super U1>, U2 extends Comparable<? super U2>, U3 extends Comparable<? super U3>> int compareTo(Tuple3<?, ?, ?> o1, Tuple3<?, ?, ?> o2) {
        Tuple3<?, ?, ?> t1 = o1;
        Tuple3<?, ?, ?> t2 = o2;
        int check1 = ((Comparable)t1._1).compareTo(t2._1);
        if (check1 != 0) {
            return check1;
        }
        int check2 = ((Comparable)t1._2).compareTo(t2._2);
        if (check2 != 0) {
            return check2;
        }
        int check3 = ((Comparable)t1._3).compareTo(t2._3);
        if (check3 != 0) {
            return check3;
        }
        return 0;
    }

    @Override
    public int arity() {
        return 3;
    }

    @Override
    public int compareTo(Tuple3<T1, T2, T3> that) {
        return Tuple3.compareTo(this, that);
    }

    public T1 _1() {
        return this._1;
    }

    public Tuple3<T1, T2, T3> update1(T1 value) {
        return new Tuple3<T1, T2, T3>(value, this._2, this._3);
    }

    public T2 _2() {
        return this._2;
    }

    public Tuple3<T1, T2, T3> update2(T2 value) {
        return new Tuple3<T1, T2, T3>(this._1, value, this._3);
    }

    public T3 _3() {
        return this._3;
    }

    public Tuple3<T1, T2, T3> update3(T3 value) {
        return new Tuple3<T1, T2, T3>(this._1, this._2, value);
    }

    public <U1, U2, U3> Tuple3<U1, U2, U3> map(Function3<? super T1, ? super T2, ? super T3, Tuple3<U1, U2, U3>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return mapper.apply(this._1, this._2, this._3);
    }

    public <U1, U2, U3> Tuple3<U1, U2, U3> map(Function<? super T1, ? extends U1> f1, Function<? super T2, ? extends U2> f2, Function<? super T3, ? extends U3> f3) {
        Objects.requireNonNull(f1, "f1 is null");
        Objects.requireNonNull(f2, "f2 is null");
        Objects.requireNonNull(f3, "f3 is null");
        return Tuple.of(f1.apply(this._1), f2.apply(this._2), f3.apply(this._3));
    }

    public <U> Tuple3<U, T2, T3> map1(Function<? super T1, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        U u = mapper.apply(this._1);
        return Tuple.of(u, this._2, this._3);
    }

    public <U> Tuple3<T1, U, T3> map2(Function<? super T2, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        U u = mapper.apply(this._2);
        return Tuple.of(this._1, u, this._3);
    }

    public <U> Tuple3<T1, T2, U> map3(Function<? super T3, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        U u = mapper.apply(this._3);
        return Tuple.of(this._1, this._2, u);
    }

    public <U> U apply(Function3<? super T1, ? super T2, ? super T3, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this._1, this._2, this._3);
    }

    @Override
    public Seq<?> toSeq() {
        return List.of(this._1, this._2, this._3);
    }

    public <T4> Tuple4<T1, T2, T3, T4> append(T4 t4) {
        return Tuple.of(this._1, this._2, this._3, t4);
    }

    public <T4> Tuple4<T1, T2, T3, T4> concat(Tuple1<T4> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, this._2, this._3, tuple._1);
    }

    public <T4, T5> Tuple5<T1, T2, T3, T4, T5> concat(Tuple2<T4, T5> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, this._2, this._3, tuple._1, tuple._2);
    }

    public <T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> concat(Tuple3<T4, T5, T6> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, this._2, this._3, tuple._1, tuple._2, tuple._3);
    }

    public <T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> concat(Tuple4<T4, T5, T6, T7> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, this._2, this._3, tuple._1, tuple._2, tuple._3, tuple._4);
    }

    public <T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> concat(Tuple5<T4, T5, T6, T7, T8> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, this._2, this._3, tuple._1, tuple._2, tuple._3, tuple._4, tuple._5);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Tuple3)) {
            return false;
        }
        Tuple3 that = (Tuple3)o;
        return Objects.equals(this._1, that._1) && Objects.equals(this._2, that._2) && Objects.equals(this._3, that._3);
    }

    public int hashCode() {
        return Tuple.hash(this._1, this._2, this._3);
    }

    public String toString() {
        return "(" + this._1 + ", " + this._2 + ", " + this._3 + ")";
    }
}

