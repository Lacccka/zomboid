/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.Function7;
import io.vavr.Tuple;
import io.vavr.Tuple1;
import io.vavr.Tuple8;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

public final class Tuple7<T1, T2, T3, T4, T5, T6, T7>
implements Tuple,
Comparable<Tuple7<T1, T2, T3, T4, T5, T6, T7>>,
Serializable {
    private static final long serialVersionUID = 1L;
    public final T1 _1;
    public final T2 _2;
    public final T3 _3;
    public final T4 _4;
    public final T5 _5;
    public final T6 _6;
    public final T7 _7;

    public Tuple7(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7) {
        this._1 = t1;
        this._2 = t2;
        this._3 = t3;
        this._4 = t4;
        this._5 = t5;
        this._6 = t6;
        this._7 = t7;
    }

    public static <T1, T2, T3, T4, T5, T6, T7> Comparator<Tuple7<T1, T2, T3, T4, T5, T6, T7>> comparator(Comparator<? super T1> t1Comp, Comparator<? super T2> t2Comp, Comparator<? super T3> t3Comp, Comparator<? super T4> t4Comp, Comparator<? super T5> t5Comp, Comparator<? super T6> t6Comp, Comparator<? super T7> t7Comp) {
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
            int check7 = t7Comp.compare((Object)t1._7, (Object)t2._7);
            if (check7 != 0) {
                return check7;
            }
            return 0;
        };
    }

    private static <U1 extends Comparable<? super U1>, U2 extends Comparable<? super U2>, U3 extends Comparable<? super U3>, U4 extends Comparable<? super U4>, U5 extends Comparable<? super U5>, U6 extends Comparable<? super U6>, U7 extends Comparable<? super U7>> int compareTo(Tuple7<?, ?, ?, ?, ?, ?, ?> o1, Tuple7<?, ?, ?, ?, ?, ?, ?> o2) {
        Tuple7<?, ?, ?, ?, ?, ?, ?> t1 = o1;
        Tuple7<?, ?, ?, ?, ?, ?, ?> t2 = o2;
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
        int check7 = ((Comparable)t1._7).compareTo(t2._7);
        if (check7 != 0) {
            return check7;
        }
        return 0;
    }

    @Override
    public int arity() {
        return 7;
    }

    @Override
    public int compareTo(Tuple7<T1, T2, T3, T4, T5, T6, T7> that) {
        return Tuple7.compareTo(this, that);
    }

    public T1 _1() {
        return this._1;
    }

    public Tuple7<T1, T2, T3, T4, T5, T6, T7> update1(T1 value) {
        return new Tuple7<T1, T2, T3, T4, T5, T6, T7>(value, this._2, this._3, this._4, this._5, this._6, this._7);
    }

    public T2 _2() {
        return this._2;
    }

    public Tuple7<T1, T2, T3, T4, T5, T6, T7> update2(T2 value) {
        return new Tuple7<T1, T2, T3, T4, T5, T6, T7>(this._1, value, this._3, this._4, this._5, this._6, this._7);
    }

    public T3 _3() {
        return this._3;
    }

    public Tuple7<T1, T2, T3, T4, T5, T6, T7> update3(T3 value) {
        return new Tuple7<T1, T2, T3, T4, T5, T6, T7>(this._1, this._2, value, this._4, this._5, this._6, this._7);
    }

    public T4 _4() {
        return this._4;
    }

    public Tuple7<T1, T2, T3, T4, T5, T6, T7> update4(T4 value) {
        return new Tuple7<T1, T2, T3, T4, T5, T6, T7>(this._1, this._2, this._3, value, this._5, this._6, this._7);
    }

    public T5 _5() {
        return this._5;
    }

    public Tuple7<T1, T2, T3, T4, T5, T6, T7> update5(T5 value) {
        return new Tuple7<T1, T2, T3, T4, T5, T6, T7>(this._1, this._2, this._3, this._4, value, this._6, this._7);
    }

    public T6 _6() {
        return this._6;
    }

    public Tuple7<T1, T2, T3, T4, T5, T6, T7> update6(T6 value) {
        return new Tuple7<T1, T2, T3, T4, T5, T6, T7>(this._1, this._2, this._3, this._4, this._5, value, this._7);
    }

    public T7 _7() {
        return this._7;
    }

    public Tuple7<T1, T2, T3, T4, T5, T6, T7> update7(T7 value) {
        return new Tuple7<T1, T2, T3, T4, T5, T6, T7>(this._1, this._2, this._3, this._4, this._5, this._6, value);
    }

    public <U1, U2, U3, U4, U5, U6, U7> Tuple7<U1, U2, U3, U4, U5, U6, U7> map(Function7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, Tuple7<U1, U2, U3, U4, U5, U6, U7>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return mapper.apply(this._1, this._2, this._3, this._4, this._5, this._6, this._7);
    }

    public <U1, U2, U3, U4, U5, U6, U7> Tuple7<U1, U2, U3, U4, U5, U6, U7> map(Function<? super T1, ? extends U1> f1, Function<? super T2, ? extends U2> f2, Function<? super T3, ? extends U3> f3, Function<? super T4, ? extends U4> f4, Function<? super T5, ? extends U5> f5, Function<? super T6, ? extends U6> f6, Function<? super T7, ? extends U7> f7) {
        Objects.requireNonNull(f1, "f1 is null");
        Objects.requireNonNull(f2, "f2 is null");
        Objects.requireNonNull(f3, "f3 is null");
        Objects.requireNonNull(f4, "f4 is null");
        Objects.requireNonNull(f5, "f5 is null");
        Objects.requireNonNull(f6, "f6 is null");
        Objects.requireNonNull(f7, "f7 is null");
        return Tuple.of(f1.apply(this._1), f2.apply(this._2), f3.apply(this._3), f4.apply(this._4), f5.apply(this._5), f6.apply(this._6), f7.apply(this._7));
    }

    public <U> Tuple7<U, T2, T3, T4, T5, T6, T7> map1(Function<? super T1, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        U u = mapper.apply(this._1);
        return Tuple.of(u, this._2, this._3, this._4, this._5, this._6, this._7);
    }

    public <U> Tuple7<T1, U, T3, T4, T5, T6, T7> map2(Function<? super T2, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        U u = mapper.apply(this._2);
        return Tuple.of(this._1, u, this._3, this._4, this._5, this._6, this._7);
    }

    public <U> Tuple7<T1, T2, U, T4, T5, T6, T7> map3(Function<? super T3, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        U u = mapper.apply(this._3);
        return Tuple.of(this._1, this._2, u, this._4, this._5, this._6, this._7);
    }

    public <U> Tuple7<T1, T2, T3, U, T5, T6, T7> map4(Function<? super T4, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        U u = mapper.apply(this._4);
        return Tuple.of(this._1, this._2, this._3, u, this._5, this._6, this._7);
    }

    public <U> Tuple7<T1, T2, T3, T4, U, T6, T7> map5(Function<? super T5, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        U u = mapper.apply(this._5);
        return Tuple.of(this._1, this._2, this._3, this._4, u, this._6, this._7);
    }

    public <U> Tuple7<T1, T2, T3, T4, T5, U, T7> map6(Function<? super T6, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        U u = mapper.apply(this._6);
        return Tuple.of(this._1, this._2, this._3, this._4, this._5, u, this._7);
    }

    public <U> Tuple7<T1, T2, T3, T4, T5, T6, U> map7(Function<? super T7, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        U u = mapper.apply(this._7);
        return Tuple.of(this._1, this._2, this._3, this._4, this._5, this._6, u);
    }

    public <U> U apply(Function7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this._1, this._2, this._3, this._4, this._5, this._6, this._7);
    }

    @Override
    public Seq<?> toSeq() {
        return List.of(this._1, this._2, this._3, this._4, this._5, this._6, this._7);
    }

    public <T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> append(T8 t8) {
        return Tuple.of(this._1, this._2, this._3, this._4, this._5, this._6, this._7, t8);
    }

    public <T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> concat(Tuple1<T8> tuple) {
        Objects.requireNonNull(tuple, "tuple is null");
        return Tuple.of(this._1, this._2, this._3, this._4, this._5, this._6, this._7, tuple._1);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Tuple7)) {
            return false;
        }
        Tuple7 that = (Tuple7)o;
        return Objects.equals(this._1, that._1) && Objects.equals(this._2, that._2) && Objects.equals(this._3, that._3) && Objects.equals(this._4, that._4) && Objects.equals(this._5, that._5) && Objects.equals(this._6, that._6) && Objects.equals(this._7, that._7);
    }

    public int hashCode() {
        return Tuple.hash(this._1, this._2, this._3, this._4, this._5, this._6, this._7);
    }

    public String toString() {
        return "(" + this._1 + ", " + this._2 + ", " + this._3 + ", " + this._4 + ", " + this._5 + ", " + this._6 + ", " + this._7 + ")";
    }
}

