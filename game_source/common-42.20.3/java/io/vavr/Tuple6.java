/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.Function6;
import io.vavr.Tuple;
import io.vavr.Tuple1;
import io.vavr.Tuple2;
import io.vavr.Tuple7;
import io.vavr.Tuple8;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

public final class Tuple6<T1, T2, T3, T4, T5, T6>
implements Tuple,
Comparable<Tuple6<T1, T2, T3, T4, T5, T6>>,
Serializable {
    private static final long serialVersionUID = 1L;
    public final T1 _1;
    public final T2 _2;
    public final T3 _3;
    public final T4 _4;
    public final T5 _5;
    public final T6 _6;

    public Tuple6(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {
        this._1 = t1;
        this._2 = t2;
        this._3 = t3;
        this._4 = t4;
        this._5 = t5;
        this._6 = t6;
    }

    public static <T1, T2, T3, T4, T5, T6> Comparator<Tuple6<T1, T2, T3, T4, T5, T6>> comparator(Comparator<? super T1> t1Comp, Comparator<? super T2> t2Comp, Comparator<? super T3> t3Comp, Comparator<? super T4> t4Comp, Comparator<? super T5> t5Comp, Comparator<? super T6> t6Comp) {
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
            int check4 = t4Comp.compare((Object)t1._4, (Object)t2._4);
            if (check4 != 0) {
                return check4;
            }
            int check5 = t5Comp.compare((Object)t1._5, (Object)t2._5);
            if (check5 != 0) {
                return check5;
            }
            int check6 = t6Comp.compare((Object)t1._6, (Object)t2._6);
            if (check6 != 0) {
                return check6;
            }
            return 0;
        };
    }

    private static <U1 extends Comparable<? super U1>, U2 extends Comparable<? super U2>, U3 extends Comparable<? super U3>, U4 extends Comparable<? super U4>, U5 extends Comparable<? super U5>, U6 extends Comparable<? super U6>> int compareTo(Tuple6<?, ?, ?, ?, ?, ?> o1, Tuple6<?, ?, ?, ?, ?, ?> o2) {
        Tuple6<?, ?, ?, ?, ?, ?> t1 = o1;
        Tuple6<?, ?, ?, ?, ?, ?> t2 = o2;
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
        int check4 = ((Comparable)t1._4).compareTo(t2._4);
        if (check4 != 0) {
            return check4;
        }
        int check5 = ((Comparable)t1._5).compareTo(t2._5);
        if (check5 != 0) {
            return check5;
        }
        int check6 = ((Comparable)t1._6).compareTo(t2._6);
        if (check6 != 0) {
            return check6;
        }
        return 0;
    }

    @Override
    public int arity() {
        return 6;
    }

    @Override
    public int compareTo(Tuple6<T1, T2, T3, T4, T5, T6> that) {
        return Tuple6.compareTo(this, that);
    }

    public T1 _1() {
        return this._1;
    }

    public Tuple6<T1, T2, T3, T4, T5, T6> update1(T1 value) {
        return new Tuple6<T1, T2, T3, T4, T5, T6>(value, this._2, this._3, this._4, this._5, this._6);
    }

    public T2 _2() {
        return this._2;
    }

    public Tuple6<T1, T2, T3, T4, T5, T6> update2(T2 value) {
        return new Tuple6<T1, T2, T3, T4, T5, T6>(this._1, value, this._3, this._4, this._5, this._6);
    }

    public T3 _3() {
        return this._3;
    }

    public Tuple6<T1, T2, T3, T4, T5, T6> update3(T3 value) {
        return new Tuple6<T1, T2, T3, T4, T5, T6>(this._1, this._2, value, this._4, this._5, this._6);
    }

    public T4 _4() {
        return this._4;
    }

    public Tuple6<T1, T2, T3, T4, T5, T6> update4(T4 value) {
        return new Tuple6<T1, T2, T3, T4, T5, T6>(this._1, this._2, this._3, value, this._5, this._6);
    }

    public T5 _5() {
        return this._5;
    }

    public Tuple6<T1, T2, T3, T4, T5, T6> update5(T5 value) {
        return new Tuple6<T1, T2, T3, T4, T5, T6>(this._1, this._2, this._3, this._4, value, this._6);
    }

    public T6 _6() {
        return this._6;
    }

    public Tuple6<T1, T2, T3, T4, T5, T6> update6(T6 value) {
        return new Tuple6<T1, T2, T3, T4, T5, T6>(this._1, this._2, this._3, this._4, this._5, value);
    }

    public <U1, U2, U3, U4, U5, U6> Tuple6<U1, U2, U3, U4, U5, U6> map(Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, Tuple6<U1, U2, U3, U4, U5, U6>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return mapper.apply(this._1, this._2, this._3, this._4, this._5, this._6);
    }

    public <U1, U2, U3, U4, U5, U6> Tuple6<U1, U2, U3, U4, U5, U6> map(Function<? super T1, ? extends U1> f1, Function<? super T2, ? extends U2> f2, Function<? super T3, ? extends U3> f3, Function<? super T4, ? extends U4> f4, Function<? super T5, ? extends U5> f5, Function<? super T6, ? extends U6> f6) {
        Objects.requireNonNull(f1, "f1 is null");
        Objects.requireNonNull(f2, "f2 is null");
        Objects.requireNonNull(f3, "f3 is null");
        Objects.requireNonNull(f4, "f4 is null");
        Objects.requireNonNull(f5, "f5 is null");
        Objects.requireNonNull(f6, "f6 is null");
        return Tuple.of(f1.apply(this._1), f2.apply(this._2), f3.apply(this._3), f4.apply(this._4), f5.apply(this._5), f6.apply(this._6));
    }

    public <U> Tuple6<U, T2, T3, T4, T5, T6> map1(Function<? super T1, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        U u = mapper.apply(this._1);
        return Tuple.of(u, this._2, this._3, this._4, this._5, this._6);
    }

    public <U> Tuple6<T1, U, T3, T4, T5, T6> map2(Function<? super T2, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        U u = mapper.apply(this._2);
        return Tuple.of(this._1, u, this._3, this._4, this._5, this._6);
    }

    public <U> Tuple6<T1, T2, U, T4, T5, T6> map3(Function<? super T3, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        U u = mapper.apply(this._3);
        return Tuple.of(this._1, this._2, u, this._4, this._5, this._6);
    }

    public <U> Tuple6<T1, T2, T3, U, T5, T6> map4(Function<? super T4, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        U u = mapper.apply(this._4);
        return Tuple.of(this._1, this._2, this._3, u, this._5, this._6);
    }

    public <U> Tuple6<T1, T2, T3, T4, U, T6> map5(Function<? super T5, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        U u = mapper.apply(this._5);
        return Tuple.of(this._1, this._2, this._3, this._4, u, this._6);
    }

    public <U> Tuple6<T1, T2, T3, T4, T5, U> map6(Function<? super T6, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        U u = mapper.apply(this._6);
        return Tuple.of(this._1, this._2, this._3, this._4, this._5, u);
    }

    public <U> U apply(Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this._1, this._2, this._3, this._4, this._5, this._6);
    }

    @Override
    public Seq<?> toSeq() {
        return List.of(this._1, this._2, this._3, this._4, this._5, this._6);
    }

    public <T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> append(T7 t7) {
        return Tuple.of(this._1, this._2, this._3, this._4, this._5, this._6, t7);
    }

    public <T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> concat(Tuple1<T7> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, this._2, this._3, this._4, this._5, this._6, tuple._1);
    }

    public <T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> concat(Tuple2<T7, T8> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, this._2, this._3, this._4, this._5, this._6, tuple._1, tuple._2);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Tuple6)) {
            return false;
        }
        Tuple6 that = (Tuple6)o;
        return Objects.equals(this._1, that._1) && Objects.equals(this._2, that._2) && Objects.equals(this._3, that._3) && Objects.equals(this._4, that._4) && Objects.equals(this._5, that._5) && Objects.equals(this._6, that._6);
    }

    public int hashCode() {
        return Tuple.hash(this._1, this._2, this._3, this._4, this._5, this._6);
    }

    public String toString() {
        return "(" + this._1 + ", " + this._2 + ", " + this._3 + ", " + this._4 + ", " + this._5 + ", " + this._6 + ")";
    }
}

