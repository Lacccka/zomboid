/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.$;
import io.vavr.API;
import io.vavr.Tuple0;
import io.vavr.Tuple1;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import io.vavr.Tuple5;
import io.vavr.Tuple6;
import io.vavr.Tuple7;
import io.vavr.Tuple8;
import io.vavr.collection.List;
import io.vavr.concurrent.Future;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.control.Validation;

public final class Patterns {
    public static final API.Match.Pattern0<Tuple0> $Tuple0 = API.Match.Pattern0.of(Tuple0.class);

    private Patterns() {
    }

    public static <T1, _1 extends T1> API.Match.Pattern1<Tuple1<T1>, _1> $Tuple1(API.Match.Pattern<_1, ?> p1) {
        return API.Match.Pattern1.of(Tuple1.class, p1, $::Tuple1);
    }

    public static <T1, T2, _1 extends T1, _2 extends T2> API.Match.Pattern2<Tuple2<T1, T2>, _1, _2> $Tuple2(API.Match.Pattern<_1, ?> p1, API.Match.Pattern<_2, ?> p2) {
        return API.Match.Pattern2.of(Tuple2.class, p1, p2, $::Tuple2);
    }

    public static <T1, T2, T3, _1 extends T1, _2 extends T2, _3 extends T3> API.Match.Pattern3<Tuple3<T1, T2, T3>, _1, _2, _3> $Tuple3(API.Match.Pattern<_1, ?> p1, API.Match.Pattern<_2, ?> p2, API.Match.Pattern<_3, ?> p3) {
        return API.Match.Pattern3.of(Tuple3.class, p1, p2, p3, $::Tuple3);
    }

    public static <T1, T2, T3, T4, _1 extends T1, _2 extends T2, _3 extends T3, _4 extends T4> API.Match.Pattern4<Tuple4<T1, T2, T3, T4>, _1, _2, _3, _4> $Tuple4(API.Match.Pattern<_1, ?> p1, API.Match.Pattern<_2, ?> p2, API.Match.Pattern<_3, ?> p3, API.Match.Pattern<_4, ?> p4) {
        return API.Match.Pattern4.of(Tuple4.class, p1, p2, p3, p4, $::Tuple4);
    }

    public static <T1, T2, T3, T4, T5, _1 extends T1, _2 extends T2, _3 extends T3, _4 extends T4, _5 extends T5> API.Match.Pattern5<Tuple5<T1, T2, T3, T4, T5>, _1, _2, _3, _4, _5> $Tuple5(API.Match.Pattern<_1, ?> p1, API.Match.Pattern<_2, ?> p2, API.Match.Pattern<_3, ?> p3, API.Match.Pattern<_4, ?> p4, API.Match.Pattern<_5, ?> p5) {
        return API.Match.Pattern5.of(Tuple5.class, p1, p2, p3, p4, p5, $::Tuple5);
    }

    public static <T1, T2, T3, T4, T5, T6, _1 extends T1, _2 extends T2, _3 extends T3, _4 extends T4, _5 extends T5, _6 extends T6> API.Match.Pattern6<Tuple6<T1, T2, T3, T4, T5, T6>, _1, _2, _3, _4, _5, _6> $Tuple6(API.Match.Pattern<_1, ?> p1, API.Match.Pattern<_2, ?> p2, API.Match.Pattern<_3, ?> p3, API.Match.Pattern<_4, ?> p4, API.Match.Pattern<_5, ?> p5, API.Match.Pattern<_6, ?> p6) {
        return API.Match.Pattern6.of(Tuple6.class, p1, p2, p3, p4, p5, p6, $::Tuple6);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, _1 extends T1, _2 extends T2, _3 extends T3, _4 extends T4, _5 extends T5, _6 extends T6, _7 extends T7> API.Match.Pattern7<Tuple7<T1, T2, T3, T4, T5, T6, T7>, _1, _2, _3, _4, _5, _6, _7> $Tuple7(API.Match.Pattern<_1, ?> p1, API.Match.Pattern<_2, ?> p2, API.Match.Pattern<_3, ?> p3, API.Match.Pattern<_4, ?> p4, API.Match.Pattern<_5, ?> p5, API.Match.Pattern<_6, ?> p6, API.Match.Pattern<_7, ?> p7) {
        return API.Match.Pattern7.of(Tuple7.class, p1, p2, p3, p4, p5, p6, p7, $::Tuple7);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, _1 extends T1, _2 extends T2, _3 extends T3, _4 extends T4, _5 extends T5, _6 extends T6, _7 extends T7, _8 extends T8> API.Match.Pattern8<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>, _1, _2, _3, _4, _5, _6, _7, _8> $Tuple8(API.Match.Pattern<_1, ?> p1, API.Match.Pattern<_2, ?> p2, API.Match.Pattern<_3, ?> p3, API.Match.Pattern<_4, ?> p4, API.Match.Pattern<_5, ?> p5, API.Match.Pattern<_6, ?> p6, API.Match.Pattern<_7, ?> p7, API.Match.Pattern<_8, ?> p8) {
        return API.Match.Pattern8.of(Tuple8.class, p1, p2, p3, p4, p5, p6, p7, p8, $::Tuple8);
    }

    public static <T, _1 extends T, _2 extends List<T>> API.Match.Pattern2<List.Cons<T>, _1, _2> $Cons(API.Match.Pattern<_1, ?> p1, API.Match.Pattern<_2, ?> p2) {
        return API.Match.Pattern2.of(List.Cons.class, p1, p2, $::Cons);
    }

    public static <T> API.Match.Pattern0<List.Nil<T>> $Nil() {
        return API.Match.Pattern0.of(List.Nil.class);
    }

    public static <T, _1 extends Option<Try<T>>> API.Match.Pattern1<Future<T>, _1> $Future(API.Match.Pattern<_1, ?> p1) {
        return API.Match.Pattern1.of(Future.class, p1, $::Future);
    }

    public static <L, R, _1 extends R> API.Match.Pattern1<Either.Right<L, R>, _1> $Right(API.Match.Pattern<_1, ?> p1) {
        return API.Match.Pattern1.of(Either.Right.class, p1, $::Right);
    }

    public static <L, R, _1 extends L> API.Match.Pattern1<Either.Left<L, R>, _1> $Left(API.Match.Pattern<_1, ?> p1) {
        return API.Match.Pattern1.of(Either.Left.class, p1, $::Left);
    }

    public static <T, _1 extends T> API.Match.Pattern1<Option.Some<T>, _1> $Some(API.Match.Pattern<_1, ?> p1) {
        return API.Match.Pattern1.of(Option.Some.class, p1, $::Some);
    }

    public static <T> API.Match.Pattern0<Option.None<T>> $None() {
        return API.Match.Pattern0.of(Option.None.class);
    }

    public static <T, _1 extends T> API.Match.Pattern1<Try.Success<T>, _1> $Success(API.Match.Pattern<_1, ?> p1) {
        return API.Match.Pattern1.of(Try.Success.class, p1, $::Success);
    }

    public static <T, _1 extends Throwable> API.Match.Pattern1<Try.Failure<T>, _1> $Failure(API.Match.Pattern<_1, ?> p1) {
        return API.Match.Pattern1.of(Try.Failure.class, p1, $::Failure);
    }

    public static <E, T, _1 extends T> API.Match.Pattern1<Validation.Valid<E, T>, _1> $Valid(API.Match.Pattern<_1, ?> p1) {
        return API.Match.Pattern1.of(Validation.Valid.class, p1, $::Valid);
    }

    public static <E, T, _1 extends E> API.Match.Pattern1<Validation.Invalid<E, T>, _1> $Invalid(API.Match.Pattern<_1, ?> p1) {
        return API.Match.Pattern1.of(Validation.Invalid.class, p1, $::Invalid);
    }
}

