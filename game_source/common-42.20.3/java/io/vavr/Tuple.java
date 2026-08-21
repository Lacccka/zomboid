/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.Tuple0;
import io.vavr.Tuple1;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import io.vavr.Tuple5;
import io.vavr.Tuple6;
import io.vavr.Tuple7;
import io.vavr.Tuple8;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import java.util.Map;
import java.util.Objects;

public interface Tuple {
    public static final int MAX_ARITY = 8;

    public int arity();

    public Seq<?> toSeq();

    public static Tuple0 empty() {
        return Tuple0.instance();
    }

    public static <T1, T2> Tuple2<T1, T2> fromEntry(Map.Entry<? extends T1, ? extends T2> entry) {
        Objects.requireNonNull(entry, "entry is null");
        return new Tuple2<T1, T2>(entry.getKey(), entry.getValue());
    }

    public static <T1> Tuple1<T1> of(T1 t1) {
        return new Tuple1<T1>(t1);
    }

    public static <T1, T2> Tuple2<T1, T2> of(T1 t1, T2 t2) {
        return new Tuple2<T1, T2>(t1, t2);
    }

    public static <T1, T2, T3> Tuple3<T1, T2, T3> of(T1 t1, T2 t2, T3 t3) {
        return new Tuple3<T1, T2, T3>(t1, t2, t3);
    }

    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> of(T1 t1, T2 t2, T3 t3, T4 t4) {
        return new Tuple4<T1, T2, T3, T4>(t1, t2, t3, t4);
    }

    public static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> of(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {
        return new Tuple5<T1, T2, T3, T4, T5>(t1, t2, t3, t4, t5);
    }

    public static <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> of(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {
        return new Tuple6<T1, T2, T3, T4, T5, T6>(t1, t2, t3, t4, t5, t6);
    }

    public static <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> of(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7) {
        return new Tuple7<T1, T2, T3, T4, T5, T6, T7>(t1, t2, t3, t4, t5, t6, t7);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> of(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8) {
        return new Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    public static int hash(Object o1) {
        return Objects.hashCode(o1);
    }

    public static int hash(Object o1, Object o2) {
        int result = 1;
        result = 31 * result + Tuple.hash(o1);
        result = 31 * result + Tuple.hash(o2);
        return result;
    }

    public static int hash(Object o1, Object o2, Object o3) {
        int result = 1;
        result = 31 * result + Tuple.hash(o1);
        result = 31 * result + Tuple.hash(o2);
        result = 31 * result + Tuple.hash(o3);
        return result;
    }

    public static int hash(Object o1, Object o2, Object o3, Object o4) {
        int result = 1;
        result = 31 * result + Tuple.hash(o1);
        result = 31 * result + Tuple.hash(o2);
        result = 31 * result + Tuple.hash(o3);
        result = 31 * result + Tuple.hash(o4);
        return result;
    }

    public static int hash(Object o1, Object o2, Object o3, Object o4, Object o5) {
        int result = 1;
        result = 31 * result + Tuple.hash(o1);
        result = 31 * result + Tuple.hash(o2);
        result = 31 * result + Tuple.hash(o3);
        result = 31 * result + Tuple.hash(o4);
        result = 31 * result + Tuple.hash(o5);
        return result;
    }

    public static int hash(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6) {
        int result = 1;
        result = 31 * result + Tuple.hash(o1);
        result = 31 * result + Tuple.hash(o2);
        result = 31 * result + Tuple.hash(o3);
        result = 31 * result + Tuple.hash(o4);
        result = 31 * result + Tuple.hash(o5);
        result = 31 * result + Tuple.hash(o6);
        return result;
    }

    public static int hash(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7) {
        int result = 1;
        result = 31 * result + Tuple.hash(o1);
        result = 31 * result + Tuple.hash(o2);
        result = 31 * result + Tuple.hash(o3);
        result = 31 * result + Tuple.hash(o4);
        result = 31 * result + Tuple.hash(o5);
        result = 31 * result + Tuple.hash(o6);
        result = 31 * result + Tuple.hash(o7);
        return result;
    }

    public static int hash(Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8) {
        int result = 1;
        result = 31 * result + Tuple.hash(o1);
        result = 31 * result + Tuple.hash(o2);
        result = 31 * result + Tuple.hash(o3);
        result = 31 * result + Tuple.hash(o4);
        result = 31 * result + Tuple.hash(o5);
        result = 31 * result + Tuple.hash(o6);
        result = 31 * result + Tuple.hash(o7);
        result = 31 * result + Tuple.hash(o8);
        return result;
    }

    public static <T1> Tuple1<T1> narrow(Tuple1<? extends T1> t) {
        return t;
    }

    public static <T1, T2> Tuple2<T1, T2> narrow(Tuple2<? extends T1, ? extends T2> t) {
        return t;
    }

    public static <T1, T2, T3> Tuple3<T1, T2, T3> narrow(Tuple3<? extends T1, ? extends T2, ? extends T3> t) {
        return t;
    }

    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> narrow(Tuple4<? extends T1, ? extends T2, ? extends T3, ? extends T4> t) {
        return t;
    }

    public static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> narrow(Tuple5<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5> t) {
        return t;
    }

    public static <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> narrow(Tuple6<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6> t) {
        return t;
    }

    public static <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> narrow(Tuple7<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7> t) {
        return t;
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> narrow(Tuple8<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8> t) {
        return t;
    }

    public static <T1> Tuple1<Seq<T1>> sequence1(Iterable<? extends Tuple1<? extends T1>> tuples) {
        Objects.requireNonNull(tuples, "tuples is null");
        Stream<Tuple1<T1>> s = Stream.ofAll(tuples);
        return new Tuple1<Seq<T1>>(s.map(Tuple1::_1));
    }

    public static <T1, T2> Tuple2<Seq<T1>, Seq<T2>> sequence2(Iterable<? extends Tuple2<? extends T1, ? extends T2>> tuples) {
        Objects.requireNonNull(tuples, "tuples is null");
        Stream<Tuple2<T1, T2>> s = Stream.ofAll(tuples);
        return new Tuple2<Seq<T1>, Seq<T2>>(s.map(Tuple2::_1), s.map(Tuple2::_2));
    }

    public static <T1, T2, T3> Tuple3<Seq<T1>, Seq<T2>, Seq<T3>> sequence3(Iterable<? extends Tuple3<? extends T1, ? extends T2, ? extends T3>> tuples) {
        Objects.requireNonNull(tuples, "tuples is null");
        Stream<Tuple3<T1, T2, T3>> s = Stream.ofAll(tuples);
        return new Tuple3<Seq<T1>, Seq<T2>, Seq<T3>>(s.map(Tuple3::_1), s.map(Tuple3::_2), s.map(Tuple3::_3));
    }

    public static <T1, T2, T3, T4> Tuple4<Seq<T1>, Seq<T2>, Seq<T3>, Seq<T4>> sequence4(Iterable<? extends Tuple4<? extends T1, ? extends T2, ? extends T3, ? extends T4>> tuples) {
        Objects.requireNonNull(tuples, "tuples is null");
        Stream<Tuple4<T1, T2, T3, T4>> s = Stream.ofAll(tuples);
        return new Tuple4<Seq<T1>, Seq<T2>, Seq<T3>, Seq<T4>>(s.map(Tuple4::_1), s.map(Tuple4::_2), s.map(Tuple4::_3), s.map(Tuple4::_4));
    }

    public static <T1, T2, T3, T4, T5> Tuple5<Seq<T1>, Seq<T2>, Seq<T3>, Seq<T4>, Seq<T5>> sequence5(Iterable<? extends Tuple5<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5>> tuples) {
        Objects.requireNonNull(tuples, "tuples is null");
        Stream<Tuple5<T1, T2, T3, T4, T5>> s = Stream.ofAll(tuples);
        return new Tuple5<Seq<T1>, Seq<T2>, Seq<T3>, Seq<T4>, Seq<T5>>(s.map(Tuple5::_1), s.map(Tuple5::_2), s.map(Tuple5::_3), s.map(Tuple5::_4), s.map(Tuple5::_5));
    }

    public static <T1, T2, T3, T4, T5, T6> Tuple6<Seq<T1>, Seq<T2>, Seq<T3>, Seq<T4>, Seq<T5>, Seq<T6>> sequence6(Iterable<? extends Tuple6<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6>> tuples) {
        Objects.requireNonNull(tuples, "tuples is null");
        Stream<Tuple6<T1, T2, T3, T4, T5, T6>> s = Stream.ofAll(tuples);
        return new Tuple6<Seq<T1>, Seq<T2>, Seq<T3>, Seq<T4>, Seq<T5>, Seq<T6>>(s.map(Tuple6::_1), s.map(Tuple6::_2), s.map(Tuple6::_3), s.map(Tuple6::_4), s.map(Tuple6::_5), s.map(Tuple6::_6));
    }

    public static <T1, T2, T3, T4, T5, T6, T7> Tuple7<Seq<T1>, Seq<T2>, Seq<T3>, Seq<T4>, Seq<T5>, Seq<T6>, Seq<T7>> sequence7(Iterable<? extends Tuple7<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7>> tuples) {
        Objects.requireNonNull(tuples, "tuples is null");
        Stream<Tuple7<T1, T2, T3, T4, T5, T6, T7>> s = Stream.ofAll(tuples);
        return new Tuple7<Seq<T1>, Seq<T2>, Seq<T3>, Seq<T4>, Seq<T5>, Seq<T6>, Seq<T7>>(s.map(Tuple7::_1), s.map(Tuple7::_2), s.map(Tuple7::_3), s.map(Tuple7::_4), s.map(Tuple7::_5), s.map(Tuple7::_6), s.map(Tuple7::_7));
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<Seq<T1>, Seq<T2>, Seq<T3>, Seq<T4>, Seq<T5>, Seq<T6>, Seq<T7>, Seq<T8>> sequence8(Iterable<? extends Tuple8<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8>> tuples) {
        Objects.requireNonNull(tuples, "tuples is null");
        Stream<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> s = Stream.ofAll(tuples);
        return new Tuple8<Seq<T1>, Seq<T2>, Seq<T3>, Seq<T4>, Seq<T5>, Seq<T6>, Seq<T7>, Seq<T8>>(s.map(Tuple8::_1), s.map(Tuple8::_2), s.map(Tuple8::_3), s.map(Tuple8::_4), s.map(Tuple8::_5), s.map(Tuple8::_6), s.map(Tuple8::_7), s.map(Tuple8::_8));
    }
}

