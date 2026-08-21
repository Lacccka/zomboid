/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

import io.vavr.CheckedFunction0;
import io.vavr.CheckedFunction1;
import io.vavr.CheckedFunction2;
import io.vavr.CheckedFunction3;
import io.vavr.CheckedFunction4;
import io.vavr.CheckedFunction5;
import io.vavr.CheckedFunction6;
import io.vavr.CheckedFunction7;
import io.vavr.CheckedFunction8;
import io.vavr.Function0;
import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.Function4;
import io.vavr.Function5;
import io.vavr.Function6;
import io.vavr.Function7;
import io.vavr.Function8;
import io.vavr.GwtIncompatible;
import io.vavr.Lazy;
import io.vavr.MatchError;
import io.vavr.NotImplementedError;
import io.vavr.PartialFunction;
import io.vavr.Tuple;
import io.vavr.Tuple0;
import io.vavr.Tuple1;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import io.vavr.Tuple5;
import io.vavr.Tuple6;
import io.vavr.Tuple7;
import io.vavr.Tuple8;
import io.vavr.collection.Array;
import io.vavr.collection.CharSeq;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Iterator;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.LinkedHashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.PriorityQueue;
import io.vavr.collection.Queue;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.SortedMap;
import io.vavr.collection.SortedSet;
import io.vavr.collection.Stream;
import io.vavr.collection.TreeMap;
import io.vavr.collection.TreeSet;
import io.vavr.collection.Vector;
import io.vavr.concurrent.Future;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class API {
    private API() {
    }

    public static <T> T TODO() {
        throw new NotImplementedError();
    }

    public static <T> T TODO(String msg) {
        throw new NotImplementedError(msg);
    }

    public static void print(Object obj) {
        System.out.print(obj);
    }

    @GwtIncompatible
    public static void printf(String format, Object ... args2) {
        System.out.printf(format, args2);
    }

    public static void println(Object obj) {
        System.out.println(obj);
    }

    public static void println() {
        System.out.println();
    }

    public static <R> Function0<R> Function(Function0<R> methodReference) {
        return Function0.of(methodReference);
    }

    public static <T1, R> Function1<T1, R> Function(Function1<T1, R> methodReference) {
        return Function1.of(methodReference);
    }

    public static <T1, T2, R> Function2<T1, T2, R> Function(Function2<T1, T2, R> methodReference) {
        return Function2.of(methodReference);
    }

    public static <T1, T2, T3, R> Function3<T1, T2, T3, R> Function(Function3<T1, T2, T3, R> methodReference) {
        return Function3.of(methodReference);
    }

    public static <T1, T2, T3, T4, R> Function4<T1, T2, T3, T4, R> Function(Function4<T1, T2, T3, T4, R> methodReference) {
        return Function4.of(methodReference);
    }

    public static <T1, T2, T3, T4, T5, R> Function5<T1, T2, T3, T4, T5, R> Function(Function5<T1, T2, T3, T4, T5, R> methodReference) {
        return Function5.of(methodReference);
    }

    public static <T1, T2, T3, T4, T5, T6, R> Function6<T1, T2, T3, T4, T5, T6, R> Function(Function6<T1, T2, T3, T4, T5, T6, R> methodReference) {
        return Function6.of(methodReference);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, R> Function7<T1, T2, T3, T4, T5, T6, T7, R> Function(Function7<T1, T2, T3, T4, T5, T6, T7, R> methodReference) {
        return Function7.of(methodReference);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> Function(Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> methodReference) {
        return Function8.of(methodReference);
    }

    public static <R> CheckedFunction0<R> CheckedFunction(CheckedFunction0<R> methodReference) {
        return CheckedFunction0.of(methodReference);
    }

    public static <T1, R> CheckedFunction1<T1, R> CheckedFunction(CheckedFunction1<T1, R> methodReference) {
        return CheckedFunction1.of(methodReference);
    }

    public static <T1, T2, R> CheckedFunction2<T1, T2, R> CheckedFunction(CheckedFunction2<T1, T2, R> methodReference) {
        return CheckedFunction2.of(methodReference);
    }

    public static <T1, T2, T3, R> CheckedFunction3<T1, T2, T3, R> CheckedFunction(CheckedFunction3<T1, T2, T3, R> methodReference) {
        return CheckedFunction3.of(methodReference);
    }

    public static <T1, T2, T3, T4, R> CheckedFunction4<T1, T2, T3, T4, R> CheckedFunction(CheckedFunction4<T1, T2, T3, T4, R> methodReference) {
        return CheckedFunction4.of(methodReference);
    }

    public static <T1, T2, T3, T4, T5, R> CheckedFunction5<T1, T2, T3, T4, T5, R> CheckedFunction(CheckedFunction5<T1, T2, T3, T4, T5, R> methodReference) {
        return CheckedFunction5.of(methodReference);
    }

    public static <T1, T2, T3, T4, T5, T6, R> CheckedFunction6<T1, T2, T3, T4, T5, T6, R> CheckedFunction(CheckedFunction6<T1, T2, T3, T4, T5, T6, R> methodReference) {
        return CheckedFunction6.of(methodReference);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, R> CheckedFunction7<T1, T2, T3, T4, T5, T6, T7, R> CheckedFunction(CheckedFunction7<T1, T2, T3, T4, T5, T6, T7, R> methodReference) {
        return CheckedFunction7.of(methodReference);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> CheckedFunction8<T1, T2, T3, T4, T5, T6, T7, T8, R> CheckedFunction(CheckedFunction8<T1, T2, T3, T4, T5, T6, T7, T8, R> methodReference) {
        return CheckedFunction8.of(methodReference);
    }

    public static <R> Function0<R> unchecked(CheckedFunction0<R> f) {
        return f.unchecked();
    }

    public static <T1, R> Function1<T1, R> unchecked(CheckedFunction1<T1, R> f) {
        return f.unchecked();
    }

    public static <T1, T2, R> Function2<T1, T2, R> unchecked(CheckedFunction2<T1, T2, R> f) {
        return f.unchecked();
    }

    public static <T1, T2, T3, R> Function3<T1, T2, T3, R> unchecked(CheckedFunction3<T1, T2, T3, R> f) {
        return f.unchecked();
    }

    public static <T1, T2, T3, T4, R> Function4<T1, T2, T3, T4, R> unchecked(CheckedFunction4<T1, T2, T3, T4, R> f) {
        return f.unchecked();
    }

    public static <T1, T2, T3, T4, T5, R> Function5<T1, T2, T3, T4, T5, R> unchecked(CheckedFunction5<T1, T2, T3, T4, T5, R> f) {
        return f.unchecked();
    }

    public static <T1, T2, T3, T4, T5, T6, R> Function6<T1, T2, T3, T4, T5, T6, R> unchecked(CheckedFunction6<T1, T2, T3, T4, T5, T6, R> f) {
        return f.unchecked();
    }

    public static <T1, T2, T3, T4, T5, T6, T7, R> Function7<T1, T2, T3, T4, T5, T6, T7, R> unchecked(CheckedFunction7<T1, T2, T3, T4, T5, T6, T7, R> f) {
        return f.unchecked();
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> unchecked(CheckedFunction8<T1, T2, T3, T4, T5, T6, T7, T8, R> f) {
        return f.unchecked();
    }

    public static Tuple0 Tuple() {
        return Tuple.empty();
    }

    public static <T1> Tuple1<T1> Tuple(T1 t1) {
        return Tuple.of(t1);
    }

    public static <T1, T2> Tuple2<T1, T2> Tuple(T1 t1, T2 t2) {
        return Tuple.of(t1, t2);
    }

    public static <T1, T2, T3> Tuple3<T1, T2, T3> Tuple(T1 t1, T2 t2, T3 t3) {
        return Tuple.of(t1, t2, t3);
    }

    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> Tuple(T1 t1, T2 t2, T3 t3, T4 t4) {
        return Tuple.of(t1, t2, t3, t4);
    }

    public static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> Tuple(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {
        return Tuple.of(t1, t2, t3, t4, t5);
    }

    public static <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> Tuple(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {
        return Tuple.of(t1, t2, t3, t4, t5, t6);
    }

    public static <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> Tuple(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7) {
        return Tuple.of(t1, t2, t3, t4, t5, t6, t7);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> Tuple(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8) {
        return Tuple.of(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    public static <L, R> Either.Right<L, R> Right(R right) {
        return (Either.Right)Either.right(right);
    }

    public static <L, R> Either.Left<L, R> Left(L left) {
        return (Either.Left)Either.left(left);
    }

    public static <T> Future<T> Future(CheckedFunction0<? extends T> computation) {
        return Future.of(computation);
    }

    public static <T> Future<T> Future(Executor executorService, CheckedFunction0<? extends T> computation) {
        return Future.of(executorService, computation);
    }

    public static <T> Future<T> Future(T result) {
        return Future.successful(result);
    }

    public static <T> Future<T> Future(Executor executorService, T result) {
        return Future.successful(executorService, result);
    }

    public static <T> Lazy<T> Lazy(Supplier<? extends T> supplier) {
        return Lazy.of(supplier);
    }

    public static <T> Option<T> Option(T value) {
        return Option.of(value);
    }

    public static <T> Option.Some<T> Some(T value) {
        return (Option.Some)Option.some(value);
    }

    public static <T> Option.None<T> None() {
        return (Option.None)Option.none();
    }

    public static <T> Try<T> Try(CheckedFunction0<? extends T> supplier) {
        return Try.of(supplier);
    }

    public static <T> Try.Success<T> Success(T value) {
        return (Try.Success)Try.success(value);
    }

    public static <T> Try.Failure<T> Failure(Throwable exception) {
        return (Try.Failure)Try.failure(exception);
    }

    public static <E, T> Validation.Valid<E, T> Valid(T value) {
        return (Validation.Valid)Validation.valid(value);
    }

    public static <E, T> Validation.Invalid<E, T> Invalid(E error) {
        return (Validation.Invalid)Validation.invalid(error);
    }

    public static CharSeq CharSeq(char character) {
        return CharSeq.of(character);
    }

    public static CharSeq CharSeq(char ... characters) {
        return CharSeq.of(characters);
    }

    public static CharSeq CharSeq(CharSequence sequence) {
        return CharSeq.of(sequence);
    }

    public static <T extends Comparable<? super T>> PriorityQueue<T> PriorityQueue() {
        return PriorityQueue.empty();
    }

    public static <T extends Comparable<? super T>> PriorityQueue<T> PriorityQueue(Comparator<? super T> comparator) {
        return PriorityQueue.empty(comparator);
    }

    public static <T extends Comparable<? super T>> PriorityQueue<T> PriorityQueue(T element) {
        return PriorityQueue.of(element);
    }

    public static <T> PriorityQueue<T> PriorityQueue(Comparator<? super T> comparator, T element) {
        return PriorityQueue.of(comparator, element);
    }

    @SafeVarargs
    public static <T extends Comparable<? super T>> PriorityQueue<T> PriorityQueue(T ... elements) {
        return PriorityQueue.of(elements);
    }

    @SafeVarargs
    public static <T> PriorityQueue<T> PriorityQueue(Comparator<? super T> comparator, T ... elements) {
        return PriorityQueue.of(comparator, elements);
    }

    public static <T> Seq<T> Seq() {
        return List.empty();
    }

    public static <T> Seq<T> Seq(T element) {
        return List.of(element);
    }

    @SafeVarargs
    public static <T> Seq<T> Seq(T ... elements) {
        return List.of(elements);
    }

    public static <T> IndexedSeq<T> IndexedSeq() {
        return Vector.empty();
    }

    public static <T> IndexedSeq<T> IndexedSeq(T element) {
        return Vector.of(element);
    }

    @SafeVarargs
    public static <T> IndexedSeq<T> IndexedSeq(T ... elements) {
        return Vector.of(elements);
    }

    public static <T> Array<T> Array() {
        return Array.empty();
    }

    public static <T> Array<T> Array(T element) {
        return Array.of(element);
    }

    @SafeVarargs
    public static <T> Array<T> Array(T ... elements) {
        return Array.of(elements);
    }

    public static <T> List<T> List() {
        return List.empty();
    }

    public static <T> List<T> List(T element) {
        return List.of(element);
    }

    @SafeVarargs
    public static <T> List<T> List(T ... elements) {
        return List.of(elements);
    }

    public static <T> Queue<T> Queue() {
        return Queue.empty();
    }

    public static <T> Queue<T> Queue(T element) {
        return Queue.of(element);
    }

    @SafeVarargs
    public static <T> Queue<T> Queue(T ... elements) {
        return Queue.of(elements);
    }

    public static <T> Stream<T> Stream() {
        return Stream.empty();
    }

    public static <T> Stream<T> Stream(T element) {
        return Stream.of(element);
    }

    @SafeVarargs
    public static <T> Stream<T> Stream(T ... elements) {
        return Stream.of(elements);
    }

    public static <T> Vector<T> Vector() {
        return Vector.empty();
    }

    public static <T> Vector<T> Vector(T element) {
        return Vector.of(element);
    }

    @SafeVarargs
    public static <T> Vector<T> Vector(T ... elements) {
        return Vector.of(elements);
    }

    public static <T> Set<T> Set() {
        return HashSet.empty();
    }

    public static <T> Set<T> Set(T element) {
        return HashSet.of(element);
    }

    @SafeVarargs
    public static <T> Set<T> Set(T ... elements) {
        return HashSet.of(elements);
    }

    public static <T> Set<T> LinkedSet() {
        return LinkedHashSet.empty();
    }

    public static <T> Set<T> LinkedSet(T element) {
        return LinkedHashSet.of(element);
    }

    @SafeVarargs
    public static <T> Set<T> LinkedSet(T ... elements) {
        return LinkedHashSet.of(elements);
    }

    public static <T extends Comparable<? super T>> SortedSet<T> SortedSet() {
        return TreeSet.empty();
    }

    public static <T extends Comparable<? super T>> SortedSet<T> SortedSet(Comparator<? super T> comparator) {
        return TreeSet.empty(comparator);
    }

    public static <T extends Comparable<? super T>> SortedSet<T> SortedSet(T element) {
        return TreeSet.of(element);
    }

    public static <T> SortedSet<T> SortedSet(Comparator<? super T> comparator, T element) {
        return TreeSet.of(comparator, element);
    }

    @SafeVarargs
    public static <T extends Comparable<? super T>> SortedSet<T> SortedSet(T ... elements) {
        return TreeSet.of(elements);
    }

    @SafeVarargs
    public static <T> SortedSet<T> SortedSet(Comparator<? super T> comparator, T ... elements) {
        return TreeSet.of(comparator, elements);
    }

    public static <K, V> Map<K, V> Map() {
        return HashMap.empty();
    }

    @Deprecated
    @SafeVarargs
    public static <K, V> Map<K, V> Map(Tuple2<? extends K, ? extends V> ... entries) {
        return HashMap.ofEntries(entries);
    }

    public static <K, V> Map<K, V> Map(K k1, V v1) {
        return HashMap.of(k1, v1);
    }

    public static <K, V> Map<K, V> Map(K k1, V v1, K k2, V v2) {
        return HashMap.of(k1, v1, k2, v2);
    }

    public static <K, V> Map<K, V> Map(K k1, V v1, K k2, V v2, K k3, V v3) {
        return HashMap.of(k1, v1, k2, v2, k3, v3);
    }

    public static <K, V> Map<K, V> Map(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4);
    }

    public static <K, V> Map<K, V> Map(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }

    public static <K, V> Map<K, V> Map(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        return HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
    }

    public static <K, V> Map<K, V> Map(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        return HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
    }

    public static <K, V> Map<K, V> Map(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        return HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
    }

    public static <K, V> Map<K, V> Map(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        return HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
    }

    public static <K, V> Map<K, V> Map(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        return HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
    }

    public static <K, V> Map<K, V> LinkedMap() {
        return LinkedHashMap.empty();
    }

    @Deprecated
    @SafeVarargs
    public static <K, V> Map<K, V> LinkedMap(Tuple2<? extends K, ? extends V> ... entries) {
        return LinkedHashMap.ofEntries(entries);
    }

    public static <K, V> Map<K, V> LinkedMap(K k1, V v1) {
        return LinkedHashMap.of(k1, v1);
    }

    public static <K, V> Map<K, V> LinkedMap(K k1, V v1, K k2, V v2) {
        return LinkedHashMap.of(k1, v1, k2, v2);
    }

    public static <K, V> Map<K, V> LinkedMap(K k1, V v1, K k2, V v2, K k3, V v3) {
        return LinkedHashMap.of(k1, v1, k2, v2, k3, v3);
    }

    public static <K, V> Map<K, V> LinkedMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return LinkedHashMap.of(k1, v1, k2, v2, k3, v3, k4, v4);
    }

    public static <K, V> Map<K, V> LinkedMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return LinkedHashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }

    public static <K, V> Map<K, V> LinkedMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        return LinkedHashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
    }

    public static <K, V> Map<K, V> LinkedMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        return LinkedHashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
    }

    public static <K, V> Map<K, V> LinkedMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        return LinkedHashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
    }

    public static <K, V> Map<K, V> LinkedMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        return LinkedHashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
    }

    public static <K, V> Map<K, V> LinkedMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        return LinkedHashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
    }

    public static <K extends Comparable<? super K>, V> SortedMap<K, V> SortedMap() {
        return TreeMap.empty();
    }

    public static <K, V> SortedMap<K, V> SortedMap(Comparator<? super K> keyComparator) {
        return TreeMap.empty(keyComparator);
    }

    public static <K, V> SortedMap<K, V> SortedMap(Comparator<? super K> keyComparator, K key, V value) {
        return TreeMap.of(keyComparator, key, value);
    }

    @Deprecated
    @SafeVarargs
    public static <K extends Comparable<? super K>, V> SortedMap<K, V> SortedMap(Tuple2<? extends K, ? extends V> ... entries) {
        return TreeMap.ofEntries(entries);
    }

    @Deprecated
    @SafeVarargs
    public static <K, V> SortedMap<K, V> SortedMap(Comparator<? super K> keyComparator, Tuple2<? extends K, ? extends V> ... entries) {
        return TreeMap.ofEntries(keyComparator, entries);
    }

    @Deprecated
    public static <K extends Comparable<? super K>, V> SortedMap<K, V> SortedMap(java.util.Map<? extends K, ? extends V> map) {
        return TreeMap.ofAll(map);
    }

    public static <K extends Comparable<? super K>, V> SortedMap<K, V> SortedMap(K k1, V v1) {
        return TreeMap.of(k1, v1);
    }

    public static <K extends Comparable<? super K>, V> SortedMap<K, V> SortedMap(K k1, V v1, K k2, V v2) {
        return TreeMap.of(k1, v1, k2, v2);
    }

    public static <K extends Comparable<? super K>, V> SortedMap<K, V> SortedMap(K k1, V v1, K k2, V v2, K k3, V v3) {
        return TreeMap.of(k1, v1, k2, v2, k3, v3);
    }

    public static <K extends Comparable<? super K>, V> SortedMap<K, V> SortedMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return TreeMap.of(k1, v1, k2, v2, k3, v3, k4, v4);
    }

    public static <K extends Comparable<? super K>, V> SortedMap<K, V> SortedMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return TreeMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }

    public static <K extends Comparable<? super K>, V> SortedMap<K, V> SortedMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        return TreeMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
    }

    public static <K extends Comparable<? super K>, V> SortedMap<K, V> SortedMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        return TreeMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
    }

    public static <K extends Comparable<? super K>, V> SortedMap<K, V> SortedMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        return TreeMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
    }

    public static <K extends Comparable<? super K>, V> SortedMap<K, V> SortedMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        return TreeMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
    }

    public static <K extends Comparable<? super K>, V> SortedMap<K, V> SortedMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        return TreeMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
    }

    public static Void run(Runnable unit) {
        unit.run();
        return null;
    }

    public static <T, U> Iterator<U> For(Iterable<T> ts, Function<? super T, ? extends Iterable<U>> f) {
        return Iterator.ofAll(ts).flatMap(f);
    }

    public static <T1> For1<T1> For(Iterable<T1> ts1) {
        Objects.requireNonNull(ts1, "ts1 is null");
        return new For1(ts1);
    }

    public static <T1, T2> For2<T1, T2> For(Iterable<T1> ts1, Iterable<T2> ts2) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        return new For2(ts1, ts2);
    }

    public static <T1, T2, T3> For3<T1, T2, T3> For(Iterable<T1> ts1, Iterable<T2> ts2, Iterable<T3> ts3) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        return new For3(ts1, ts2, ts3);
    }

    public static <T1, T2, T3, T4> For4<T1, T2, T3, T4> For(Iterable<T1> ts1, Iterable<T2> ts2, Iterable<T3> ts3, Iterable<T4> ts4) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        return new For4(ts1, ts2, ts3, ts4);
    }

    public static <T1, T2, T3, T4, T5> For5<T1, T2, T3, T4, T5> For(Iterable<T1> ts1, Iterable<T2> ts2, Iterable<T3> ts3, Iterable<T4> ts4, Iterable<T5> ts5) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        return new For5(ts1, ts2, ts3, ts4, ts5);
    }

    public static <T1, T2, T3, T4, T5, T6> For6<T1, T2, T3, T4, T5, T6> For(Iterable<T1> ts1, Iterable<T2> ts2, Iterable<T3> ts3, Iterable<T4> ts4, Iterable<T5> ts5, Iterable<T6> ts6) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        Objects.requireNonNull(ts6, "ts6 is null");
        return new For6(ts1, ts2, ts3, ts4, ts5, ts6);
    }

    public static <T1, T2, T3, T4, T5, T6, T7> For7<T1, T2, T3, T4, T5, T6, T7> For(Iterable<T1> ts1, Iterable<T2> ts2, Iterable<T3> ts3, Iterable<T4> ts4, Iterable<T5> ts5, Iterable<T6> ts6, Iterable<T7> ts7) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        Objects.requireNonNull(ts6, "ts6 is null");
        Objects.requireNonNull(ts7, "ts7 is null");
        return new For7(ts1, ts2, ts3, ts4, ts5, ts6, ts7);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8> For8<T1, T2, T3, T4, T5, T6, T7, T8> For(Iterable<T1> ts1, Iterable<T2> ts2, Iterable<T3> ts3, Iterable<T4> ts4, Iterable<T5> ts5, Iterable<T6> ts6, Iterable<T7> ts7, Iterable<T8> ts8) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        Objects.requireNonNull(ts6, "ts6 is null");
        Objects.requireNonNull(ts7, "ts7 is null");
        Objects.requireNonNull(ts8, "ts8 is null");
        return new For8(ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8);
    }

    public static <T1> For1Option<T1> For(Option<T1> ts1) {
        Objects.requireNonNull(ts1, "ts1 is null");
        return new For1Option(ts1);
    }

    public static <T1, T2> For2Option<T1, T2> For(Option<T1> ts1, Option<T2> ts2) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        return new For2Option(ts1, ts2);
    }

    public static <T1, T2, T3> For3Option<T1, T2, T3> For(Option<T1> ts1, Option<T2> ts2, Option<T3> ts3) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        return new For3Option(ts1, ts2, ts3);
    }

    public static <T1, T2, T3, T4> For4Option<T1, T2, T3, T4> For(Option<T1> ts1, Option<T2> ts2, Option<T3> ts3, Option<T4> ts4) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        return new For4Option(ts1, ts2, ts3, ts4);
    }

    public static <T1, T2, T3, T4, T5> For5Option<T1, T2, T3, T4, T5> For(Option<T1> ts1, Option<T2> ts2, Option<T3> ts3, Option<T4> ts4, Option<T5> ts5) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        return new For5Option(ts1, ts2, ts3, ts4, ts5);
    }

    public static <T1, T2, T3, T4, T5, T6> For6Option<T1, T2, T3, T4, T5, T6> For(Option<T1> ts1, Option<T2> ts2, Option<T3> ts3, Option<T4> ts4, Option<T5> ts5, Option<T6> ts6) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        Objects.requireNonNull(ts6, "ts6 is null");
        return new For6Option(ts1, ts2, ts3, ts4, ts5, ts6);
    }

    public static <T1, T2, T3, T4, T5, T6, T7> For7Option<T1, T2, T3, T4, T5, T6, T7> For(Option<T1> ts1, Option<T2> ts2, Option<T3> ts3, Option<T4> ts4, Option<T5> ts5, Option<T6> ts6, Option<T7> ts7) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        Objects.requireNonNull(ts6, "ts6 is null");
        Objects.requireNonNull(ts7, "ts7 is null");
        return new For7Option(ts1, ts2, ts3, ts4, ts5, ts6, ts7);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8> For8Option<T1, T2, T3, T4, T5, T6, T7, T8> For(Option<T1> ts1, Option<T2> ts2, Option<T3> ts3, Option<T4> ts4, Option<T5> ts5, Option<T6> ts6, Option<T7> ts7, Option<T8> ts8) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        Objects.requireNonNull(ts6, "ts6 is null");
        Objects.requireNonNull(ts7, "ts7 is null");
        Objects.requireNonNull(ts8, "ts8 is null");
        return new For8Option(ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8);
    }

    public static <T1> For1Future<T1> For(Future<T1> ts1) {
        Objects.requireNonNull(ts1, "ts1 is null");
        return new For1Future(ts1);
    }

    public static <T1, T2> For2Future<T1, T2> For(Future<T1> ts1, Future<T2> ts2) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        return new For2Future(ts1, ts2);
    }

    public static <T1, T2, T3> For3Future<T1, T2, T3> For(Future<T1> ts1, Future<T2> ts2, Future<T3> ts3) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        return new For3Future(ts1, ts2, ts3);
    }

    public static <T1, T2, T3, T4> For4Future<T1, T2, T3, T4> For(Future<T1> ts1, Future<T2> ts2, Future<T3> ts3, Future<T4> ts4) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        return new For4Future(ts1, ts2, ts3, ts4);
    }

    public static <T1, T2, T3, T4, T5> For5Future<T1, T2, T3, T4, T5> For(Future<T1> ts1, Future<T2> ts2, Future<T3> ts3, Future<T4> ts4, Future<T5> ts5) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        return new For5Future(ts1, ts2, ts3, ts4, ts5);
    }

    public static <T1, T2, T3, T4, T5, T6> For6Future<T1, T2, T3, T4, T5, T6> For(Future<T1> ts1, Future<T2> ts2, Future<T3> ts3, Future<T4> ts4, Future<T5> ts5, Future<T6> ts6) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        Objects.requireNonNull(ts6, "ts6 is null");
        return new For6Future(ts1, ts2, ts3, ts4, ts5, ts6);
    }

    public static <T1, T2, T3, T4, T5, T6, T7> For7Future<T1, T2, T3, T4, T5, T6, T7> For(Future<T1> ts1, Future<T2> ts2, Future<T3> ts3, Future<T4> ts4, Future<T5> ts5, Future<T6> ts6, Future<T7> ts7) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        Objects.requireNonNull(ts6, "ts6 is null");
        Objects.requireNonNull(ts7, "ts7 is null");
        return new For7Future(ts1, ts2, ts3, ts4, ts5, ts6, ts7);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8> For8Future<T1, T2, T3, T4, T5, T6, T7, T8> For(Future<T1> ts1, Future<T2> ts2, Future<T3> ts3, Future<T4> ts4, Future<T5> ts5, Future<T6> ts6, Future<T7> ts7, Future<T8> ts8) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        Objects.requireNonNull(ts6, "ts6 is null");
        Objects.requireNonNull(ts7, "ts7 is null");
        Objects.requireNonNull(ts8, "ts8 is null");
        return new For8Future(ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8);
    }

    public static <T1> For1Try<T1> For(Try<T1> ts1) {
        Objects.requireNonNull(ts1, "ts1 is null");
        return new For1Try(ts1);
    }

    public static <T1, T2> For2Try<T1, T2> For(Try<T1> ts1, Try<T2> ts2) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        return new For2Try(ts1, ts2);
    }

    public static <T1, T2, T3> For3Try<T1, T2, T3> For(Try<T1> ts1, Try<T2> ts2, Try<T3> ts3) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        return new For3Try(ts1, ts2, ts3);
    }

    public static <T1, T2, T3, T4> For4Try<T1, T2, T3, T4> For(Try<T1> ts1, Try<T2> ts2, Try<T3> ts3, Try<T4> ts4) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        return new For4Try(ts1, ts2, ts3, ts4);
    }

    public static <T1, T2, T3, T4, T5> For5Try<T1, T2, T3, T4, T5> For(Try<T1> ts1, Try<T2> ts2, Try<T3> ts3, Try<T4> ts4, Try<T5> ts5) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        return new For5Try(ts1, ts2, ts3, ts4, ts5);
    }

    public static <T1, T2, T3, T4, T5, T6> For6Try<T1, T2, T3, T4, T5, T6> For(Try<T1> ts1, Try<T2> ts2, Try<T3> ts3, Try<T4> ts4, Try<T5> ts5, Try<T6> ts6) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        Objects.requireNonNull(ts6, "ts6 is null");
        return new For6Try(ts1, ts2, ts3, ts4, ts5, ts6);
    }

    public static <T1, T2, T3, T4, T5, T6, T7> For7Try<T1, T2, T3, T4, T5, T6, T7> For(Try<T1> ts1, Try<T2> ts2, Try<T3> ts3, Try<T4> ts4, Try<T5> ts5, Try<T6> ts6, Try<T7> ts7) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        Objects.requireNonNull(ts6, "ts6 is null");
        Objects.requireNonNull(ts7, "ts7 is null");
        return new For7Try(ts1, ts2, ts3, ts4, ts5, ts6, ts7);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8> For8Try<T1, T2, T3, T4, T5, T6, T7, T8> For(Try<T1> ts1, Try<T2> ts2, Try<T3> ts3, Try<T4> ts4, Try<T5> ts5, Try<T6> ts6, Try<T7> ts7, Try<T8> ts8) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        Objects.requireNonNull(ts6, "ts6 is null");
        Objects.requireNonNull(ts7, "ts7 is null");
        Objects.requireNonNull(ts8, "ts8 is null");
        return new For8Try(ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8);
    }

    public static <T1> For1List<T1> For(List<T1> ts1) {
        Objects.requireNonNull(ts1, "ts1 is null");
        return new For1List(ts1);
    }

    public static <T1, T2> For2List<T1, T2> For(List<T1> ts1, List<T2> ts2) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        return new For2List(ts1, ts2);
    }

    public static <T1, T2, T3> For3List<T1, T2, T3> For(List<T1> ts1, List<T2> ts2, List<T3> ts3) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        return new For3List(ts1, ts2, ts3);
    }

    public static <T1, T2, T3, T4> For4List<T1, T2, T3, T4> For(List<T1> ts1, List<T2> ts2, List<T3> ts3, List<T4> ts4) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        return new For4List(ts1, ts2, ts3, ts4);
    }

    public static <T1, T2, T3, T4, T5> For5List<T1, T2, T3, T4, T5> For(List<T1> ts1, List<T2> ts2, List<T3> ts3, List<T4> ts4, List<T5> ts5) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        return new For5List(ts1, ts2, ts3, ts4, ts5);
    }

    public static <T1, T2, T3, T4, T5, T6> For6List<T1, T2, T3, T4, T5, T6> For(List<T1> ts1, List<T2> ts2, List<T3> ts3, List<T4> ts4, List<T5> ts5, List<T6> ts6) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        Objects.requireNonNull(ts6, "ts6 is null");
        return new For6List(ts1, ts2, ts3, ts4, ts5, ts6);
    }

    public static <T1, T2, T3, T4, T5, T6, T7> For7List<T1, T2, T3, T4, T5, T6, T7> For(List<T1> ts1, List<T2> ts2, List<T3> ts3, List<T4> ts4, List<T5> ts5, List<T6> ts6, List<T7> ts7) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        Objects.requireNonNull(ts6, "ts6 is null");
        Objects.requireNonNull(ts7, "ts7 is null");
        return new For7List(ts1, ts2, ts3, ts4, ts5, ts6, ts7);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8> For8List<T1, T2, T3, T4, T5, T6, T7, T8> For(List<T1> ts1, List<T2> ts2, List<T3> ts3, List<T4> ts4, List<T5> ts5, List<T6> ts6, List<T7> ts7, List<T8> ts8) {
        Objects.requireNonNull(ts1, "ts1 is null");
        Objects.requireNonNull(ts2, "ts2 is null");
        Objects.requireNonNull(ts3, "ts3 is null");
        Objects.requireNonNull(ts4, "ts4 is null");
        Objects.requireNonNull(ts5, "ts5 is null");
        Objects.requireNonNull(ts6, "ts6 is null");
        Objects.requireNonNull(ts7, "ts7 is null");
        Objects.requireNonNull(ts8, "ts8 is null");
        return new For8List(ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8);
    }

    @GwtIncompatible
    public static <T> Match<T> Match(T value) {
        return new Match(value);
    }

    @GwtIncompatible
    public static <T, R> Match.Case<T, R> Case(Match.Pattern0<T> pattern, Function<? super T, ? extends R> f) {
        Objects.requireNonNull(pattern, "pattern is null");
        Objects.requireNonNull(f, "f is null");
        return new Match.Case0(pattern, f);
    }

    @GwtIncompatible
    public static <T, R> Match.Case<T, R> Case(Match.Pattern0<T> pattern, Supplier<? extends R> supplier) {
        Objects.requireNonNull(pattern, "pattern is null");
        Objects.requireNonNull(supplier, "supplier is null");
        return new Match.Case0(pattern, ignored -> supplier.get());
    }

    @GwtIncompatible
    public static <T, R> Match.Case<T, R> Case(Match.Pattern0<T> pattern, R retVal) {
        Objects.requireNonNull(pattern, "pattern is null");
        return new Match.Case0(pattern, ignored -> retVal);
    }

    @GwtIncompatible
    public static <T, T1, R> Match.Case<T, R> Case(Match.Pattern1<T, T1> pattern, Function<? super T1, ? extends R> f) {
        Objects.requireNonNull(pattern, "pattern is null");
        Objects.requireNonNull(f, "f is null");
        return new Match.Case1(pattern, f);
    }

    @GwtIncompatible
    public static <T, T1, R> Match.Case<T, R> Case(Match.Pattern1<T, T1> pattern, Supplier<? extends R> supplier) {
        Objects.requireNonNull(pattern, "pattern is null");
        Objects.requireNonNull(supplier, "supplier is null");
        return new Match.Case1(pattern, _1 -> supplier.get());
    }

    @GwtIncompatible
    public static <T, T1, R> Match.Case<T, R> Case(Match.Pattern1<T, T1> pattern, R retVal) {
        Objects.requireNonNull(pattern, "pattern is null");
        return new Match.Case1(pattern, _1 -> retVal);
    }

    @GwtIncompatible
    public static <T, T1, T2, R> Match.Case<T, R> Case(Match.Pattern2<T, T1, T2> pattern, BiFunction<? super T1, ? super T2, ? extends R> f) {
        Objects.requireNonNull(pattern, "pattern is null");
        Objects.requireNonNull(f, "f is null");
        return new Match.Case2(pattern, f);
    }

    @GwtIncompatible
    public static <T, T1, T2, R> Match.Case<T, R> Case(Match.Pattern2<T, T1, T2> pattern, Supplier<? extends R> supplier) {
        Objects.requireNonNull(pattern, "pattern is null");
        Objects.requireNonNull(supplier, "supplier is null");
        return new Match.Case2(pattern, (_1, _2) -> supplier.get());
    }

    @GwtIncompatible
    public static <T, T1, T2, R> Match.Case<T, R> Case(Match.Pattern2<T, T1, T2> pattern, R retVal) {
        Objects.requireNonNull(pattern, "pattern is null");
        return new Match.Case2(pattern, (_1, _2) -> retVal);
    }

    @GwtIncompatible
    public static <T, T1, T2, T3, R> Match.Case<T, R> Case(Match.Pattern3<T, T1, T2, T3> pattern, Function3<? super T1, ? super T2, ? super T3, ? extends R> f) {
        Objects.requireNonNull(pattern, "pattern is null");
        Objects.requireNonNull(f, "f is null");
        return new Match.Case3(pattern, f);
    }

    @GwtIncompatible
    public static <T, T1, T2, T3, R> Match.Case<T, R> Case(Match.Pattern3<T, T1, T2, T3> pattern, Supplier<? extends R> supplier) {
        Objects.requireNonNull(pattern, "pattern is null");
        Objects.requireNonNull(supplier, "supplier is null");
        return new Match.Case3(pattern, (_1, _2, _3) -> supplier.get());
    }

    @GwtIncompatible
    public static <T, T1, T2, T3, R> Match.Case<T, R> Case(Match.Pattern3<T, T1, T2, T3> pattern, R retVal) {
        Objects.requireNonNull(pattern, "pattern is null");
        return new Match.Case3(pattern, (_1, _2, _3) -> retVal);
    }

    @GwtIncompatible
    public static <T, T1, T2, T3, T4, R> Match.Case<T, R> Case(Match.Pattern4<T, T1, T2, T3, T4> pattern, Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> f) {
        Objects.requireNonNull(pattern, "pattern is null");
        Objects.requireNonNull(f, "f is null");
        return new Match.Case4(pattern, f);
    }

    @GwtIncompatible
    public static <T, T1, T2, T3, T4, R> Match.Case<T, R> Case(Match.Pattern4<T, T1, T2, T3, T4> pattern, Supplier<? extends R> supplier) {
        Objects.requireNonNull(pattern, "pattern is null");
        Objects.requireNonNull(supplier, "supplier is null");
        return new Match.Case4(pattern, (_1, _2, _3, _4) -> supplier.get());
    }

    @GwtIncompatible
    public static <T, T1, T2, T3, T4, R> Match.Case<T, R> Case(Match.Pattern4<T, T1, T2, T3, T4> pattern, R retVal) {
        Objects.requireNonNull(pattern, "pattern is null");
        return new Match.Case4(pattern, (_1, _2, _3, _4) -> retVal);
    }

    @GwtIncompatible
    public static <T, T1, T2, T3, T4, T5, R> Match.Case<T, R> Case(Match.Pattern5<T, T1, T2, T3, T4, T5> pattern, Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> f) {
        Objects.requireNonNull(pattern, "pattern is null");
        Objects.requireNonNull(f, "f is null");
        return new Match.Case5(pattern, f);
    }

    @GwtIncompatible
    public static <T, T1, T2, T3, T4, T5, R> Match.Case<T, R> Case(Match.Pattern5<T, T1, T2, T3, T4, T5> pattern, Supplier<? extends R> supplier) {
        Objects.requireNonNull(pattern, "pattern is null");
        Objects.requireNonNull(supplier, "supplier is null");
        return new Match.Case5(pattern, (_1, _2, _3, _4, _5) -> supplier.get());
    }

    @GwtIncompatible
    public static <T, T1, T2, T3, T4, T5, R> Match.Case<T, R> Case(Match.Pattern5<T, T1, T2, T3, T4, T5> pattern, R retVal) {
        Objects.requireNonNull(pattern, "pattern is null");
        return new Match.Case5(pattern, (_1, _2, _3, _4, _5) -> retVal);
    }

    @GwtIncompatible
    public static <T, T1, T2, T3, T4, T5, T6, R> Match.Case<T, R> Case(Match.Pattern6<T, T1, T2, T3, T4, T5, T6> pattern, Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> f) {
        Objects.requireNonNull(pattern, "pattern is null");
        Objects.requireNonNull(f, "f is null");
        return new Match.Case6(pattern, f);
    }

    @GwtIncompatible
    public static <T, T1, T2, T3, T4, T5, T6, R> Match.Case<T, R> Case(Match.Pattern6<T, T1, T2, T3, T4, T5, T6> pattern, Supplier<? extends R> supplier) {
        Objects.requireNonNull(pattern, "pattern is null");
        Objects.requireNonNull(supplier, "supplier is null");
        return new Match.Case6(pattern, (_1, _2, _3, _4, _5, _6) -> supplier.get());
    }

    @GwtIncompatible
    public static <T, T1, T2, T3, T4, T5, T6, R> Match.Case<T, R> Case(Match.Pattern6<T, T1, T2, T3, T4, T5, T6> pattern, R retVal) {
        Objects.requireNonNull(pattern, "pattern is null");
        return new Match.Case6(pattern, (_1, _2, _3, _4, _5, _6) -> retVal);
    }

    @GwtIncompatible
    public static <T, T1, T2, T3, T4, T5, T6, T7, R> Match.Case<T, R> Case(Match.Pattern7<T, T1, T2, T3, T4, T5, T6, T7> pattern, Function7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> f) {
        Objects.requireNonNull(pattern, "pattern is null");
        Objects.requireNonNull(f, "f is null");
        return new Match.Case7(pattern, f);
    }

    @GwtIncompatible
    public static <T, T1, T2, T3, T4, T5, T6, T7, R> Match.Case<T, R> Case(Match.Pattern7<T, T1, T2, T3, T4, T5, T6, T7> pattern, Supplier<? extends R> supplier) {
        Objects.requireNonNull(pattern, "pattern is null");
        Objects.requireNonNull(supplier, "supplier is null");
        return new Match.Case7(pattern, (_1, _2, _3, _4, _5, _6, _7) -> supplier.get());
    }

    @GwtIncompatible
    public static <T, T1, T2, T3, T4, T5, T6, T7, R> Match.Case<T, R> Case(Match.Pattern7<T, T1, T2, T3, T4, T5, T6, T7> pattern, R retVal) {
        Objects.requireNonNull(pattern, "pattern is null");
        return new Match.Case7(pattern, (_1, _2, _3, _4, _5, _6, _7) -> retVal);
    }

    @GwtIncompatible
    public static <T, T1, T2, T3, T4, T5, T6, T7, T8, R> Match.Case<T, R> Case(Match.Pattern8<T, T1, T2, T3, T4, T5, T6, T7, T8> pattern, Function8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> f) {
        Objects.requireNonNull(pattern, "pattern is null");
        Objects.requireNonNull(f, "f is null");
        return new Match.Case8(pattern, f);
    }

    @GwtIncompatible
    public static <T, T1, T2, T3, T4, T5, T6, T7, T8, R> Match.Case<T, R> Case(Match.Pattern8<T, T1, T2, T3, T4, T5, T6, T7, T8> pattern, Supplier<? extends R> supplier) {
        Objects.requireNonNull(pattern, "pattern is null");
        Objects.requireNonNull(supplier, "supplier is null");
        return new Match.Case8(pattern, (_1, _2, _3, _4, _5, _6, _7, _8) -> supplier.get());
    }

    @GwtIncompatible
    public static <T, T1, T2, T3, T4, T5, T6, T7, T8, R> Match.Case<T, R> Case(Match.Pattern8<T, T1, T2, T3, T4, T5, T6, T7, T8> pattern, R retVal) {
        Objects.requireNonNull(pattern, "pattern is null");
        return new Match.Case8(pattern, (_1, _2, _3, _4, _5, _6, _7, _8) -> retVal);
    }

    @GwtIncompatible
    public static <T> Match.Pattern0<T> $() {
        return Match.Pattern0.any();
    }

    @GwtIncompatible
    public static <T> Match.Pattern0<T> $(final T prototype) {
        return new Match.Pattern0<T>(){
            private static final long serialVersionUID = 1L;

            @Override
            public T apply(T obj) {
                return obj;
            }

            @Override
            public boolean isDefinedAt(T obj) {
                if (obj == prototype) {
                    return true;
                }
                if (prototype != null && prototype.getClass().isInstance(obj)) {
                    return Objects.equals(obj, prototype);
                }
                return false;
            }
        };
    }

    @GwtIncompatible
    public static <T> Match.Pattern0<T> $(final Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return new Match.Pattern0<T>(){
            private static final long serialVersionUID = 1L;

            @Override
            public T apply(T obj) {
                return obj;
            }

            @Override
            public boolean isDefinedAt(T obj) {
                try {
                    return predicate.test(obj);
                }
                catch (ClassCastException x) {
                    return false;
                }
            }
        };
    }

    @GwtIncompatible
    public static final class Match<T> {
        private final T value;

        private Match(T value) {
            this.value = value;
        }

        @SafeVarargs
        public final <R> R of(Case<? extends T, ? extends R> ... cases) {
            Objects.requireNonNull(cases, "cases is null");
            for (Case<T, R> case_ : cases) {
                Case<T, R> __case = case_;
                if (!__case.isDefinedAt(this.value)) continue;
                return __case.apply(this.value);
            }
            throw new MatchError(this.value);
        }

        @SafeVarargs
        public final <R> Option<R> option(Case<? extends T, ? extends R> ... cases) {
            Objects.requireNonNull(cases, "cases is null");
            for (Case<T, R> case_ : cases) {
                Case<T, R> __case = case_;
                if (!__case.isDefinedAt(this.value)) continue;
                return Option.some(__case.apply(this.value));
            }
            return Option.none();
        }

        public static abstract class Pattern8<T, T1, T2, T3, T4, T5, T6, T7, T8>
        implements Pattern<T, Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> {
            private static final long serialVersionUID = 1L;

            public static <T, T1 extends U1, U1, T2 extends U2, U2, T3 extends U3, U3, T4 extends U4, U4, T5 extends U5, U5, T6 extends U6, U6, T7 extends U7, U7, T8 extends U8, U8> Pattern8<T, T1, T2, T3, T4, T5, T6, T7, T8> of(final Class<? super T> type, final Pattern<T1, ?> p1, final Pattern<T2, ?> p2, final Pattern<T3, ?> p3, final Pattern<T4, ?> p4, final Pattern<T5, ?> p5, final Pattern<T6, ?> p6, final Pattern<T7, ?> p7, final Pattern<T8, ?> p8, final Function<T, Tuple8<U1, U2, U3, U4, U5, U6, U7, U8>> unapply) {
                return new Pattern8<T, T1, T2, T3, T4, T5, T6, T7, T8>(){
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> apply(T obj) {
                        return (Tuple8)unapply.apply(obj);
                    }

                    @Override
                    public boolean isDefinedAt(T obj) {
                        if (type.isInstance(obj)) {
                            Tuple8 u = (Tuple8)unapply.apply(obj);
                            return p1.isDefinedAt(u._1) && p2.isDefinedAt(u._2) && p3.isDefinedAt(u._3) && p4.isDefinedAt(u._4) && p5.isDefinedAt(u._5) && p6.isDefinedAt(u._6) && p7.isDefinedAt(u._7) && p8.isDefinedAt(u._8);
                        }
                        return false;
                    }
                };
            }
        }

        public static abstract class Pattern7<T, T1, T2, T3, T4, T5, T6, T7>
        implements Pattern<T, Tuple7<T1, T2, T3, T4, T5, T6, T7>> {
            private static final long serialVersionUID = 1L;

            public static <T, T1 extends U1, U1, T2 extends U2, U2, T3 extends U3, U3, T4 extends U4, U4, T5 extends U5, U5, T6 extends U6, U6, T7 extends U7, U7> Pattern7<T, T1, T2, T3, T4, T5, T6, T7> of(final Class<? super T> type, final Pattern<T1, ?> p1, final Pattern<T2, ?> p2, final Pattern<T3, ?> p3, final Pattern<T4, ?> p4, final Pattern<T5, ?> p5, final Pattern<T6, ?> p6, final Pattern<T7, ?> p7, final Function<T, Tuple7<U1, U2, U3, U4, U5, U6, U7>> unapply) {
                return new Pattern7<T, T1, T2, T3, T4, T5, T6, T7>(){
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Tuple7<T1, T2, T3, T4, T5, T6, T7> apply(T obj) {
                        return (Tuple7)unapply.apply(obj);
                    }

                    @Override
                    public boolean isDefinedAt(T obj) {
                        if (type.isInstance(obj)) {
                            Tuple7 u = (Tuple7)unapply.apply(obj);
                            return p1.isDefinedAt(u._1) && p2.isDefinedAt(u._2) && p3.isDefinedAt(u._3) && p4.isDefinedAt(u._4) && p5.isDefinedAt(u._5) && p6.isDefinedAt(u._6) && p7.isDefinedAt(u._7);
                        }
                        return false;
                    }
                };
            }
        }

        public static abstract class Pattern6<T, T1, T2, T3, T4, T5, T6>
        implements Pattern<T, Tuple6<T1, T2, T3, T4, T5, T6>> {
            private static final long serialVersionUID = 1L;

            public static <T, T1 extends U1, U1, T2 extends U2, U2, T3 extends U3, U3, T4 extends U4, U4, T5 extends U5, U5, T6 extends U6, U6> Pattern6<T, T1, T2, T3, T4, T5, T6> of(final Class<? super T> type, final Pattern<T1, ?> p1, final Pattern<T2, ?> p2, final Pattern<T3, ?> p3, final Pattern<T4, ?> p4, final Pattern<T5, ?> p5, final Pattern<T6, ?> p6, final Function<T, Tuple6<U1, U2, U3, U4, U5, U6>> unapply) {
                return new Pattern6<T, T1, T2, T3, T4, T5, T6>(){
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Tuple6<T1, T2, T3, T4, T5, T6> apply(T obj) {
                        return (Tuple6)unapply.apply(obj);
                    }

                    @Override
                    public boolean isDefinedAt(T obj) {
                        if (type.isInstance(obj)) {
                            Tuple6 u = (Tuple6)unapply.apply(obj);
                            return p1.isDefinedAt(u._1) && p2.isDefinedAt(u._2) && p3.isDefinedAt(u._3) && p4.isDefinedAt(u._4) && p5.isDefinedAt(u._5) && p6.isDefinedAt(u._6);
                        }
                        return false;
                    }
                };
            }
        }

        public static abstract class Pattern5<T, T1, T2, T3, T4, T5>
        implements Pattern<T, Tuple5<T1, T2, T3, T4, T5>> {
            private static final long serialVersionUID = 1L;

            public static <T, T1 extends U1, U1, T2 extends U2, U2, T3 extends U3, U3, T4 extends U4, U4, T5 extends U5, U5> Pattern5<T, T1, T2, T3, T4, T5> of(final Class<? super T> type, final Pattern<T1, ?> p1, final Pattern<T2, ?> p2, final Pattern<T3, ?> p3, final Pattern<T4, ?> p4, final Pattern<T5, ?> p5, final Function<T, Tuple5<U1, U2, U3, U4, U5>> unapply) {
                return new Pattern5<T, T1, T2, T3, T4, T5>(){
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Tuple5<T1, T2, T3, T4, T5> apply(T obj) {
                        return (Tuple5)unapply.apply(obj);
                    }

                    @Override
                    public boolean isDefinedAt(T obj) {
                        if (type.isInstance(obj)) {
                            Tuple5 u = (Tuple5)unapply.apply(obj);
                            return p1.isDefinedAt(u._1) && p2.isDefinedAt(u._2) && p3.isDefinedAt(u._3) && p4.isDefinedAt(u._4) && p5.isDefinedAt(u._5);
                        }
                        return false;
                    }
                };
            }
        }

        public static abstract class Pattern4<T, T1, T2, T3, T4>
        implements Pattern<T, Tuple4<T1, T2, T3, T4>> {
            private static final long serialVersionUID = 1L;

            public static <T, T1 extends U1, U1, T2 extends U2, U2, T3 extends U3, U3, T4 extends U4, U4> Pattern4<T, T1, T2, T3, T4> of(final Class<? super T> type, final Pattern<T1, ?> p1, final Pattern<T2, ?> p2, final Pattern<T3, ?> p3, final Pattern<T4, ?> p4, final Function<T, Tuple4<U1, U2, U3, U4>> unapply) {
                return new Pattern4<T, T1, T2, T3, T4>(){
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Tuple4<T1, T2, T3, T4> apply(T obj) {
                        return (Tuple4)unapply.apply(obj);
                    }

                    @Override
                    public boolean isDefinedAt(T obj) {
                        if (type.isInstance(obj)) {
                            Tuple4 u = (Tuple4)unapply.apply(obj);
                            return p1.isDefinedAt(u._1) && p2.isDefinedAt(u._2) && p3.isDefinedAt(u._3) && p4.isDefinedAt(u._4);
                        }
                        return false;
                    }
                };
            }
        }

        public static abstract class Pattern3<T, T1, T2, T3>
        implements Pattern<T, Tuple3<T1, T2, T3>> {
            private static final long serialVersionUID = 1L;

            public static <T, T1 extends U1, U1, T2 extends U2, U2, T3 extends U3, U3> Pattern3<T, T1, T2, T3> of(final Class<? super T> type, final Pattern<T1, ?> p1, final Pattern<T2, ?> p2, final Pattern<T3, ?> p3, final Function<T, Tuple3<U1, U2, U3>> unapply) {
                return new Pattern3<T, T1, T2, T3>(){
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Tuple3<T1, T2, T3> apply(T obj) {
                        return (Tuple3)unapply.apply(obj);
                    }

                    @Override
                    public boolean isDefinedAt(T obj) {
                        if (type.isInstance(obj)) {
                            Tuple3 u = (Tuple3)unapply.apply(obj);
                            return p1.isDefinedAt(u._1) && p2.isDefinedAt(u._2) && p3.isDefinedAt(u._3);
                        }
                        return false;
                    }
                };
            }
        }

        public static abstract class Pattern2<T, T1, T2>
        implements Pattern<T, Tuple2<T1, T2>> {
            private static final long serialVersionUID = 1L;

            public static <T, T1 extends U1, U1, T2 extends U2, U2> Pattern2<T, T1, T2> of(final Class<? super T> type, final Pattern<T1, ?> p1, final Pattern<T2, ?> p2, final Function<T, Tuple2<U1, U2>> unapply) {
                return new Pattern2<T, T1, T2>(){
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Tuple2<T1, T2> apply(T obj) {
                        return (Tuple2)unapply.apply(obj);
                    }

                    @Override
                    public boolean isDefinedAt(T obj) {
                        if (type.isInstance(obj)) {
                            Tuple2 u = (Tuple2)unapply.apply(obj);
                            return p1.isDefinedAt(u._1) && p2.isDefinedAt(u._2);
                        }
                        return false;
                    }
                };
            }
        }

        public static abstract class Pattern1<T, T1>
        implements Pattern<T, T1> {
            private static final long serialVersionUID = 1L;

            public static <T, T1 extends U1, U1> Pattern1<T, T1> of(final Class<? super T> type, final Pattern<T1, ?> p1, final Function<T, Tuple1<U1>> unapply) {
                return new Pattern1<T, T1>(){
                    private static final long serialVersionUID = 1L;

                    @Override
                    public T1 apply(T obj) {
                        return ((Tuple1)unapply.apply(obj))._1;
                    }

                    @Override
                    public boolean isDefinedAt(T obj) {
                        if (type.isInstance(obj)) {
                            Tuple1 u = (Tuple1)unapply.apply(obj);
                            return p1.isDefinedAt(u._1);
                        }
                        return false;
                    }
                };
            }
        }

        public static abstract class Pattern0<T>
        implements Pattern<T, T> {
            private static final long serialVersionUID = 1L;
            private static final Pattern0<Object> ANY = new Pattern0<Object>(){
                private static final long serialVersionUID = 1L;

                @Override
                public Object apply(Object obj) {
                    return obj;
                }

                @Override
                public boolean isDefinedAt(Object obj) {
                    return true;
                }
            };

            public static <T> Pattern0<T> any() {
                return ANY;
            }

            public static <T> Pattern0<T> of(final Class<? super T> type) {
                return new Pattern0<T>(){
                    private static final long serialVersionUID = 1L;

                    @Override
                    public T apply(T obj) {
                        return obj;
                    }

                    @Override
                    public boolean isDefinedAt(T obj) {
                        return type.isInstance(obj);
                    }
                };
            }
        }

        public static interface Pattern<T, R>
        extends PartialFunction<T, R> {
        }

        public static final class Case8<T, T1, T2, T3, T4, T5, T6, T7, T8, R>
        implements Case<T, R> {
            private static final long serialVersionUID = 1L;
            private final Pattern8<T, T1, T2, T3, T4, T5, T6, T7, T8> pattern;
            private final Function8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> f;

            private Case8(Pattern8<T, T1, T2, T3, T4, T5, T6, T7, T8> pattern, Function8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> f) {
                this.pattern = pattern;
                this.f = f;
            }

            @Override
            public R apply(T obj) {
                return ((Tuple8)this.pattern.apply(obj)).apply(this.f);
            }

            @Override
            public boolean isDefinedAt(T obj) {
                return this.pattern.isDefinedAt(obj);
            }
        }

        public static final class Case7<T, T1, T2, T3, T4, T5, T6, T7, R>
        implements Case<T, R> {
            private static final long serialVersionUID = 1L;
            private final Pattern7<T, T1, T2, T3, T4, T5, T6, T7> pattern;
            private final Function7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> f;

            private Case7(Pattern7<T, T1, T2, T3, T4, T5, T6, T7> pattern, Function7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> f) {
                this.pattern = pattern;
                this.f = f;
            }

            @Override
            public R apply(T obj) {
                return ((Tuple7)this.pattern.apply(obj)).apply(this.f);
            }

            @Override
            public boolean isDefinedAt(T obj) {
                return this.pattern.isDefinedAt(obj);
            }
        }

        public static final class Case6<T, T1, T2, T3, T4, T5, T6, R>
        implements Case<T, R> {
            private static final long serialVersionUID = 1L;
            private final Pattern6<T, T1, T2, T3, T4, T5, T6> pattern;
            private final Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> f;

            private Case6(Pattern6<T, T1, T2, T3, T4, T5, T6> pattern, Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> f) {
                this.pattern = pattern;
                this.f = f;
            }

            @Override
            public R apply(T obj) {
                return ((Tuple6)this.pattern.apply(obj)).apply(this.f);
            }

            @Override
            public boolean isDefinedAt(T obj) {
                return this.pattern.isDefinedAt(obj);
            }
        }

        public static final class Case5<T, T1, T2, T3, T4, T5, R>
        implements Case<T, R> {
            private static final long serialVersionUID = 1L;
            private final Pattern5<T, T1, T2, T3, T4, T5> pattern;
            private final Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> f;

            private Case5(Pattern5<T, T1, T2, T3, T4, T5> pattern, Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> f) {
                this.pattern = pattern;
                this.f = f;
            }

            @Override
            public R apply(T obj) {
                return ((Tuple5)this.pattern.apply(obj)).apply(this.f);
            }

            @Override
            public boolean isDefinedAt(T obj) {
                return this.pattern.isDefinedAt(obj);
            }
        }

        public static final class Case4<T, T1, T2, T3, T4, R>
        implements Case<T, R> {
            private static final long serialVersionUID = 1L;
            private final Pattern4<T, T1, T2, T3, T4> pattern;
            private final Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> f;

            private Case4(Pattern4<T, T1, T2, T3, T4> pattern, Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> f) {
                this.pattern = pattern;
                this.f = f;
            }

            @Override
            public R apply(T obj) {
                return ((Tuple4)this.pattern.apply(obj)).apply(this.f);
            }

            @Override
            public boolean isDefinedAt(T obj) {
                return this.pattern.isDefinedAt(obj);
            }
        }

        public static final class Case3<T, T1, T2, T3, R>
        implements Case<T, R> {
            private static final long serialVersionUID = 1L;
            private final Pattern3<T, T1, T2, T3> pattern;
            private final Function3<? super T1, ? super T2, ? super T3, ? extends R> f;

            private Case3(Pattern3<T, T1, T2, T3> pattern, Function3<? super T1, ? super T2, ? super T3, ? extends R> f) {
                this.pattern = pattern;
                this.f = f;
            }

            @Override
            public R apply(T obj) {
                return ((Tuple3)this.pattern.apply(obj)).apply(this.f);
            }

            @Override
            public boolean isDefinedAt(T obj) {
                return this.pattern.isDefinedAt(obj);
            }
        }

        public static final class Case2<T, T1, T2, R>
        implements Case<T, R> {
            private static final long serialVersionUID = 1L;
            private final Pattern2<T, T1, T2> pattern;
            private final BiFunction<? super T1, ? super T2, ? extends R> f;

            private Case2(Pattern2<T, T1, T2> pattern, BiFunction<? super T1, ? super T2, ? extends R> f) {
                this.pattern = pattern;
                this.f = f;
            }

            @Override
            public R apply(T obj) {
                return ((Tuple2)this.pattern.apply(obj)).apply(this.f);
            }

            @Override
            public boolean isDefinedAt(T obj) {
                return this.pattern.isDefinedAt(obj);
            }
        }

        public static final class Case1<T, T1, R>
        implements Case<T, R> {
            private static final long serialVersionUID = 1L;
            private final Pattern1<T, T1> pattern;
            private final Function<? super T1, ? extends R> f;

            private Case1(Pattern1<T, T1> pattern, Function<? super T1, ? extends R> f) {
                this.pattern = pattern;
                this.f = f;
            }

            @Override
            public R apply(T obj) {
                return this.f.apply(this.pattern.apply(obj));
            }

            @Override
            public boolean isDefinedAt(T obj) {
                return this.pattern.isDefinedAt(obj);
            }
        }

        public static final class Case0<T, R>
        implements Case<T, R> {
            private static final long serialVersionUID = 1L;
            private final Pattern0<T> pattern;
            private final Function<? super T, ? extends R> f;

            private Case0(Pattern0<T> pattern, Function<? super T, ? extends R> f) {
                this.pattern = pattern;
                this.f = f;
            }

            @Override
            public R apply(T obj) {
                return this.f.apply(this.pattern.apply(obj));
            }

            @Override
            public boolean isDefinedAt(T obj) {
                return this.pattern.isDefinedAt(obj);
            }
        }

        public static interface Case<T, R>
        extends PartialFunction<T, R> {
            public static final long serialVersionUID = 1L;
        }
    }

    public static class For8List<T1, T2, T3, T4, T5, T6, T7, T8> {
        private final List<T1> ts1;
        private final List<T2> ts2;
        private final List<T3> ts3;
        private final List<T4> ts4;
        private final List<T5> ts5;
        private final List<T6> ts6;
        private final List<T7> ts7;
        private final List<T8> ts8;

        private For8List(List<T1> ts1, List<T2> ts2, List<T3> ts3, List<T4> ts4, List<T5> ts5, List<T6> ts6, List<T7> ts7, List<T8> ts8) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
            this.ts6 = ts6;
            this.ts7 = ts7;
            this.ts8 = ts8;
        }

        public <R> List<R> yield(Function8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.flatMap(t4 -> this.ts5.flatMap(t5 -> this.ts6.flatMap(t6 -> this.ts7.flatMap(t7 -> this.ts8.map(t8 -> f.apply(t1, t2, t3, t4, t5, t6, t7, t8)))))))));
        }
    }

    public static class For7List<T1, T2, T3, T4, T5, T6, T7> {
        private final List<T1> ts1;
        private final List<T2> ts2;
        private final List<T3> ts3;
        private final List<T4> ts4;
        private final List<T5> ts5;
        private final List<T6> ts6;
        private final List<T7> ts7;

        private For7List(List<T1> ts1, List<T2> ts2, List<T3> ts3, List<T4> ts4, List<T5> ts5, List<T6> ts6, List<T7> ts7) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
            this.ts6 = ts6;
            this.ts7 = ts7;
        }

        public <R> List<R> yield(Function7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.flatMap(t4 -> this.ts5.flatMap(t5 -> this.ts6.flatMap(t6 -> this.ts7.map(t7 -> f.apply(t1, t2, t3, t4, t5, t6, t7))))))));
        }
    }

    public static class For6List<T1, T2, T3, T4, T5, T6> {
        private final List<T1> ts1;
        private final List<T2> ts2;
        private final List<T3> ts3;
        private final List<T4> ts4;
        private final List<T5> ts5;
        private final List<T6> ts6;

        private For6List(List<T1> ts1, List<T2> ts2, List<T3> ts3, List<T4> ts4, List<T5> ts5, List<T6> ts6) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
            this.ts6 = ts6;
        }

        public <R> List<R> yield(Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.flatMap(t4 -> this.ts5.flatMap(t5 -> this.ts6.map(t6 -> f.apply(t1, t2, t3, t4, t5, t6)))))));
        }
    }

    public static class For5List<T1, T2, T3, T4, T5> {
        private final List<T1> ts1;
        private final List<T2> ts2;
        private final List<T3> ts3;
        private final List<T4> ts4;
        private final List<T5> ts5;

        private For5List(List<T1> ts1, List<T2> ts2, List<T3> ts3, List<T4> ts4, List<T5> ts5) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
        }

        public <R> List<R> yield(Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.flatMap(t4 -> this.ts5.map(t5 -> f.apply(t1, t2, t3, t4, t5))))));
        }
    }

    public static class For4List<T1, T2, T3, T4> {
        private final List<T1> ts1;
        private final List<T2> ts2;
        private final List<T3> ts3;
        private final List<T4> ts4;

        private For4List(List<T1> ts1, List<T2> ts2, List<T3> ts3, List<T4> ts4) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
        }

        public <R> List<R> yield(Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.map(t4 -> f.apply(t1, t2, t3, t4)))));
        }
    }

    public static class For3List<T1, T2, T3> {
        private final List<T1> ts1;
        private final List<T2> ts2;
        private final List<T3> ts3;

        private For3List(List<T1> ts1, List<T2> ts2, List<T3> ts3) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
        }

        public <R> List<R> yield(Function3<? super T1, ? super T2, ? super T3, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.map(t3 -> f.apply(t1, t2, t3))));
        }
    }

    public static class For2List<T1, T2> {
        private final List<T1> ts1;
        private final List<T2> ts2;

        private For2List(List<T1> ts1, List<T2> ts2) {
            this.ts1 = ts1;
            this.ts2 = ts2;
        }

        public <R> List<R> yield(BiFunction<? super T1, ? super T2, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.map(t2 -> f.apply((Object)t1, (Object)t2)));
        }
    }

    public static class For1List<T1> {
        private final List<T1> ts1;

        private For1List(List<T1> ts1) {
            this.ts1 = ts1;
        }

        public <R> List<R> yield(Function<? super T1, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.map(f);
        }

        public List<T1> yield() {
            return this.yield(Function.identity());
        }
    }

    public static class For8Try<T1, T2, T3, T4, T5, T6, T7, T8> {
        private final Try<T1> ts1;
        private final Try<T2> ts2;
        private final Try<T3> ts3;
        private final Try<T4> ts4;
        private final Try<T5> ts5;
        private final Try<T6> ts6;
        private final Try<T7> ts7;
        private final Try<T8> ts8;

        private For8Try(Try<T1> ts1, Try<T2> ts2, Try<T3> ts3, Try<T4> ts4, Try<T5> ts5, Try<T6> ts6, Try<T7> ts7, Try<T8> ts8) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
            this.ts6 = ts6;
            this.ts7 = ts7;
            this.ts8 = ts8;
        }

        public <R> Try<R> yield(Function8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.flatMap(t4 -> this.ts5.flatMap(t5 -> this.ts6.flatMap(t6 -> this.ts7.flatMap(t7 -> this.ts8.map(t8 -> f.apply(t1, t2, t3, t4, t5, t6, t7, t8)))))))));
        }
    }

    public static class For7Try<T1, T2, T3, T4, T5, T6, T7> {
        private final Try<T1> ts1;
        private final Try<T2> ts2;
        private final Try<T3> ts3;
        private final Try<T4> ts4;
        private final Try<T5> ts5;
        private final Try<T6> ts6;
        private final Try<T7> ts7;

        private For7Try(Try<T1> ts1, Try<T2> ts2, Try<T3> ts3, Try<T4> ts4, Try<T5> ts5, Try<T6> ts6, Try<T7> ts7) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
            this.ts6 = ts6;
            this.ts7 = ts7;
        }

        public <R> Try<R> yield(Function7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.flatMap(t4 -> this.ts5.flatMap(t5 -> this.ts6.flatMap(t6 -> this.ts7.map(t7 -> f.apply(t1, t2, t3, t4, t5, t6, t7))))))));
        }
    }

    public static class For6Try<T1, T2, T3, T4, T5, T6> {
        private final Try<T1> ts1;
        private final Try<T2> ts2;
        private final Try<T3> ts3;
        private final Try<T4> ts4;
        private final Try<T5> ts5;
        private final Try<T6> ts6;

        private For6Try(Try<T1> ts1, Try<T2> ts2, Try<T3> ts3, Try<T4> ts4, Try<T5> ts5, Try<T6> ts6) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
            this.ts6 = ts6;
        }

        public <R> Try<R> yield(Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.flatMap(t4 -> this.ts5.flatMap(t5 -> this.ts6.map(t6 -> f.apply(t1, t2, t3, t4, t5, t6)))))));
        }
    }

    public static class For5Try<T1, T2, T3, T4, T5> {
        private final Try<T1> ts1;
        private final Try<T2> ts2;
        private final Try<T3> ts3;
        private final Try<T4> ts4;
        private final Try<T5> ts5;

        private For5Try(Try<T1> ts1, Try<T2> ts2, Try<T3> ts3, Try<T4> ts4, Try<T5> ts5) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
        }

        public <R> Try<R> yield(Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.flatMap(t4 -> this.ts5.map(t5 -> f.apply(t1, t2, t3, t4, t5))))));
        }
    }

    public static class For4Try<T1, T2, T3, T4> {
        private final Try<T1> ts1;
        private final Try<T2> ts2;
        private final Try<T3> ts3;
        private final Try<T4> ts4;

        private For4Try(Try<T1> ts1, Try<T2> ts2, Try<T3> ts3, Try<T4> ts4) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
        }

        public <R> Try<R> yield(Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.map(t4 -> f.apply(t1, t2, t3, t4)))));
        }
    }

    public static class For3Try<T1, T2, T3> {
        private final Try<T1> ts1;
        private final Try<T2> ts2;
        private final Try<T3> ts3;

        private For3Try(Try<T1> ts1, Try<T2> ts2, Try<T3> ts3) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
        }

        public <R> Try<R> yield(Function3<? super T1, ? super T2, ? super T3, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.map(t3 -> f.apply(t1, t2, t3))));
        }
    }

    public static class For2Try<T1, T2> {
        private final Try<T1> ts1;
        private final Try<T2> ts2;

        private For2Try(Try<T1> ts1, Try<T2> ts2) {
            this.ts1 = ts1;
            this.ts2 = ts2;
        }

        public <R> Try<R> yield(BiFunction<? super T1, ? super T2, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.map(t2 -> f.apply((Object)t1, (Object)t2)));
        }
    }

    public static class For1Try<T1> {
        private final Try<T1> ts1;

        private For1Try(Try<T1> ts1) {
            this.ts1 = ts1;
        }

        public <R> Try<R> yield(Function<? super T1, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.map(f);
        }

        public Try<T1> yield() {
            return this.yield(Function.identity());
        }
    }

    public static class For8Future<T1, T2, T3, T4, T5, T6, T7, T8> {
        private final Future<T1> ts1;
        private final Future<T2> ts2;
        private final Future<T3> ts3;
        private final Future<T4> ts4;
        private final Future<T5> ts5;
        private final Future<T6> ts6;
        private final Future<T7> ts7;
        private final Future<T8> ts8;

        private For8Future(Future<T1> ts1, Future<T2> ts2, Future<T3> ts3, Future<T4> ts4, Future<T5> ts5, Future<T6> ts6, Future<T7> ts7, Future<T8> ts8) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
            this.ts6 = ts6;
            this.ts7 = ts7;
            this.ts8 = ts8;
        }

        public <R> Future<R> yield(Function8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.flatMap(t4 -> this.ts5.flatMap(t5 -> this.ts6.flatMap(t6 -> this.ts7.flatMap(t7 -> this.ts8.map(t8 -> f.apply(t1, t2, t3, t4, t5, t6, t7, t8)))))))));
        }
    }

    public static class For7Future<T1, T2, T3, T4, T5, T6, T7> {
        private final Future<T1> ts1;
        private final Future<T2> ts2;
        private final Future<T3> ts3;
        private final Future<T4> ts4;
        private final Future<T5> ts5;
        private final Future<T6> ts6;
        private final Future<T7> ts7;

        private For7Future(Future<T1> ts1, Future<T2> ts2, Future<T3> ts3, Future<T4> ts4, Future<T5> ts5, Future<T6> ts6, Future<T7> ts7) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
            this.ts6 = ts6;
            this.ts7 = ts7;
        }

        public <R> Future<R> yield(Function7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.flatMap(t4 -> this.ts5.flatMap(t5 -> this.ts6.flatMap(t6 -> this.ts7.map(t7 -> f.apply(t1, t2, t3, t4, t5, t6, t7))))))));
        }
    }

    public static class For6Future<T1, T2, T3, T4, T5, T6> {
        private final Future<T1> ts1;
        private final Future<T2> ts2;
        private final Future<T3> ts3;
        private final Future<T4> ts4;
        private final Future<T5> ts5;
        private final Future<T6> ts6;

        private For6Future(Future<T1> ts1, Future<T2> ts2, Future<T3> ts3, Future<T4> ts4, Future<T5> ts5, Future<T6> ts6) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
            this.ts6 = ts6;
        }

        public <R> Future<R> yield(Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.flatMap(t4 -> this.ts5.flatMap(t5 -> this.ts6.map(t6 -> f.apply(t1, t2, t3, t4, t5, t6)))))));
        }
    }

    public static class For5Future<T1, T2, T3, T4, T5> {
        private final Future<T1> ts1;
        private final Future<T2> ts2;
        private final Future<T3> ts3;
        private final Future<T4> ts4;
        private final Future<T5> ts5;

        private For5Future(Future<T1> ts1, Future<T2> ts2, Future<T3> ts3, Future<T4> ts4, Future<T5> ts5) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
        }

        public <R> Future<R> yield(Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.flatMap(t4 -> this.ts5.map(t5 -> f.apply(t1, t2, t3, t4, t5))))));
        }
    }

    public static class For4Future<T1, T2, T3, T4> {
        private final Future<T1> ts1;
        private final Future<T2> ts2;
        private final Future<T3> ts3;
        private final Future<T4> ts4;

        private For4Future(Future<T1> ts1, Future<T2> ts2, Future<T3> ts3, Future<T4> ts4) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
        }

        public <R> Future<R> yield(Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.map(t4 -> f.apply(t1, t2, t3, t4)))));
        }
    }

    public static class For3Future<T1, T2, T3> {
        private final Future<T1> ts1;
        private final Future<T2> ts2;
        private final Future<T3> ts3;

        private For3Future(Future<T1> ts1, Future<T2> ts2, Future<T3> ts3) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
        }

        public <R> Future<R> yield(Function3<? super T1, ? super T2, ? super T3, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.map(t3 -> f.apply(t1, t2, t3))));
        }
    }

    public static class For2Future<T1, T2> {
        private final Future<T1> ts1;
        private final Future<T2> ts2;

        private For2Future(Future<T1> ts1, Future<T2> ts2) {
            this.ts1 = ts1;
            this.ts2 = ts2;
        }

        public <R> Future<R> yield(BiFunction<? super T1, ? super T2, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.map(t2 -> f.apply((Object)t1, (Object)t2)));
        }
    }

    public static class For1Future<T1> {
        private final Future<T1> ts1;

        private For1Future(Future<T1> ts1) {
            this.ts1 = ts1;
        }

        public <R> Future<R> yield(Function<? super T1, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.map(f);
        }

        public Future<T1> yield() {
            return this.yield(Function.identity());
        }
    }

    public static class For8Option<T1, T2, T3, T4, T5, T6, T7, T8> {
        private final Option<T1> ts1;
        private final Option<T2> ts2;
        private final Option<T3> ts3;
        private final Option<T4> ts4;
        private final Option<T5> ts5;
        private final Option<T6> ts6;
        private final Option<T7> ts7;
        private final Option<T8> ts8;

        private For8Option(Option<T1> ts1, Option<T2> ts2, Option<T3> ts3, Option<T4> ts4, Option<T5> ts5, Option<T6> ts6, Option<T7> ts7, Option<T8> ts8) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
            this.ts6 = ts6;
            this.ts7 = ts7;
            this.ts8 = ts8;
        }

        public <R> Option<R> yield(Function8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.flatMap(t4 -> this.ts5.flatMap(t5 -> this.ts6.flatMap(t6 -> this.ts7.flatMap(t7 -> this.ts8.map(t8 -> f.apply(t1, t2, t3, t4, t5, t6, t7, t8)))))))));
        }
    }

    public static class For7Option<T1, T2, T3, T4, T5, T6, T7> {
        private final Option<T1> ts1;
        private final Option<T2> ts2;
        private final Option<T3> ts3;
        private final Option<T4> ts4;
        private final Option<T5> ts5;
        private final Option<T6> ts6;
        private final Option<T7> ts7;

        private For7Option(Option<T1> ts1, Option<T2> ts2, Option<T3> ts3, Option<T4> ts4, Option<T5> ts5, Option<T6> ts6, Option<T7> ts7) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
            this.ts6 = ts6;
            this.ts7 = ts7;
        }

        public <R> Option<R> yield(Function7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.flatMap(t4 -> this.ts5.flatMap(t5 -> this.ts6.flatMap(t6 -> this.ts7.map(t7 -> f.apply(t1, t2, t3, t4, t5, t6, t7))))))));
        }
    }

    public static class For6Option<T1, T2, T3, T4, T5, T6> {
        private final Option<T1> ts1;
        private final Option<T2> ts2;
        private final Option<T3> ts3;
        private final Option<T4> ts4;
        private final Option<T5> ts5;
        private final Option<T6> ts6;

        private For6Option(Option<T1> ts1, Option<T2> ts2, Option<T3> ts3, Option<T4> ts4, Option<T5> ts5, Option<T6> ts6) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
            this.ts6 = ts6;
        }

        public <R> Option<R> yield(Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.flatMap(t4 -> this.ts5.flatMap(t5 -> this.ts6.map(t6 -> f.apply(t1, t2, t3, t4, t5, t6)))))));
        }
    }

    public static class For5Option<T1, T2, T3, T4, T5> {
        private final Option<T1> ts1;
        private final Option<T2> ts2;
        private final Option<T3> ts3;
        private final Option<T4> ts4;
        private final Option<T5> ts5;

        private For5Option(Option<T1> ts1, Option<T2> ts2, Option<T3> ts3, Option<T4> ts4, Option<T5> ts5) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
        }

        public <R> Option<R> yield(Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.flatMap(t4 -> this.ts5.map(t5 -> f.apply(t1, t2, t3, t4, t5))))));
        }
    }

    public static class For4Option<T1, T2, T3, T4> {
        private final Option<T1> ts1;
        private final Option<T2> ts2;
        private final Option<T3> ts3;
        private final Option<T4> ts4;

        private For4Option(Option<T1> ts1, Option<T2> ts2, Option<T3> ts3, Option<T4> ts4) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
        }

        public <R> Option<R> yield(Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.flatMap(t3 -> this.ts4.map(t4 -> f.apply(t1, t2, t3, t4)))));
        }
    }

    public static class For3Option<T1, T2, T3> {
        private final Option<T1> ts1;
        private final Option<T2> ts2;
        private final Option<T3> ts3;

        private For3Option(Option<T1> ts1, Option<T2> ts2, Option<T3> ts3) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
        }

        public <R> Option<R> yield(Function3<? super T1, ? super T2, ? super T3, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.flatMap(t2 -> this.ts3.map(t3 -> f.apply(t1, t2, t3))));
        }
    }

    public static class For2Option<T1, T2> {
        private final Option<T1> ts1;
        private final Option<T2> ts2;

        private For2Option(Option<T1> ts1, Option<T2> ts2) {
            this.ts1 = ts1;
            this.ts2 = ts2;
        }

        public <R> Option<R> yield(BiFunction<? super T1, ? super T2, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.flatMap(t1 -> this.ts2.map(t2 -> f.apply((Object)t1, (Object)t2)));
        }
    }

    public static class For1Option<T1> {
        private final Option<T1> ts1;

        private For1Option(Option<T1> ts1) {
            this.ts1 = ts1;
        }

        public <R> Option<R> yield(Function<? super T1, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return this.ts1.map(f);
        }

        public Option<T1> yield() {
            return this.yield(Function.identity());
        }
    }

    public static class For8<T1, T2, T3, T4, T5, T6, T7, T8> {
        private final Iterable<T1> ts1;
        private final Iterable<T2> ts2;
        private final Iterable<T3> ts3;
        private final Iterable<T4> ts4;
        private final Iterable<T5> ts5;
        private final Iterable<T6> ts6;
        private final Iterable<T7> ts7;
        private final Iterable<T8> ts8;

        private For8(Iterable<T1> ts1, Iterable<T2> ts2, Iterable<T3> ts3, Iterable<T4> ts4, Iterable<T5> ts5, Iterable<T6> ts6, Iterable<T7> ts7, Iterable<T8> ts8) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
            this.ts6 = ts6;
            this.ts7 = ts7;
            this.ts8 = ts8;
        }

        public <R> Iterator<R> yield(Function8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return Iterator.ofAll(this.ts1).flatMap(t1 -> Iterator.ofAll(this.ts2).flatMap(t2 -> Iterator.ofAll(this.ts3).flatMap(t3 -> Iterator.ofAll(this.ts4).flatMap(t4 -> Iterator.ofAll(this.ts5).flatMap(t5 -> Iterator.ofAll(this.ts6).flatMap(t6 -> Iterator.ofAll(this.ts7).flatMap(t7 -> Iterator.ofAll(this.ts8).map(t8 -> f.apply(t1, t2, t3, t4, t5, t6, t7, t8)))))))));
        }
    }

    public static class For7<T1, T2, T3, T4, T5, T6, T7> {
        private final Iterable<T1> ts1;
        private final Iterable<T2> ts2;
        private final Iterable<T3> ts3;
        private final Iterable<T4> ts4;
        private final Iterable<T5> ts5;
        private final Iterable<T6> ts6;
        private final Iterable<T7> ts7;

        private For7(Iterable<T1> ts1, Iterable<T2> ts2, Iterable<T3> ts3, Iterable<T4> ts4, Iterable<T5> ts5, Iterable<T6> ts6, Iterable<T7> ts7) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
            this.ts6 = ts6;
            this.ts7 = ts7;
        }

        public <R> Iterator<R> yield(Function7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return Iterator.ofAll(this.ts1).flatMap(t1 -> Iterator.ofAll(this.ts2).flatMap(t2 -> Iterator.ofAll(this.ts3).flatMap(t3 -> Iterator.ofAll(this.ts4).flatMap(t4 -> Iterator.ofAll(this.ts5).flatMap(t5 -> Iterator.ofAll(this.ts6).flatMap(t6 -> Iterator.ofAll(this.ts7).map(t7 -> f.apply(t1, t2, t3, t4, t5, t6, t7))))))));
        }
    }

    public static class For6<T1, T2, T3, T4, T5, T6> {
        private final Iterable<T1> ts1;
        private final Iterable<T2> ts2;
        private final Iterable<T3> ts3;
        private final Iterable<T4> ts4;
        private final Iterable<T5> ts5;
        private final Iterable<T6> ts6;

        private For6(Iterable<T1> ts1, Iterable<T2> ts2, Iterable<T3> ts3, Iterable<T4> ts4, Iterable<T5> ts5, Iterable<T6> ts6) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
            this.ts6 = ts6;
        }

        public <R> Iterator<R> yield(Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return Iterator.ofAll(this.ts1).flatMap(t1 -> Iterator.ofAll(this.ts2).flatMap(t2 -> Iterator.ofAll(this.ts3).flatMap(t3 -> Iterator.ofAll(this.ts4).flatMap(t4 -> Iterator.ofAll(this.ts5).flatMap(t5 -> Iterator.ofAll(this.ts6).map(t6 -> f.apply(t1, t2, t3, t4, t5, t6)))))));
        }
    }

    public static class For5<T1, T2, T3, T4, T5> {
        private final Iterable<T1> ts1;
        private final Iterable<T2> ts2;
        private final Iterable<T3> ts3;
        private final Iterable<T4> ts4;
        private final Iterable<T5> ts5;

        private For5(Iterable<T1> ts1, Iterable<T2> ts2, Iterable<T3> ts3, Iterable<T4> ts4, Iterable<T5> ts5) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
            this.ts5 = ts5;
        }

        public <R> Iterator<R> yield(Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return Iterator.ofAll(this.ts1).flatMap(t1 -> Iterator.ofAll(this.ts2).flatMap(t2 -> Iterator.ofAll(this.ts3).flatMap(t3 -> Iterator.ofAll(this.ts4).flatMap(t4 -> Iterator.ofAll(this.ts5).map(t5 -> f.apply(t1, t2, t3, t4, t5))))));
        }
    }

    public static class For4<T1, T2, T3, T4> {
        private final Iterable<T1> ts1;
        private final Iterable<T2> ts2;
        private final Iterable<T3> ts3;
        private final Iterable<T4> ts4;

        private For4(Iterable<T1> ts1, Iterable<T2> ts2, Iterable<T3> ts3, Iterable<T4> ts4) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
            this.ts4 = ts4;
        }

        public <R> Iterator<R> yield(Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return Iterator.ofAll(this.ts1).flatMap(t1 -> Iterator.ofAll(this.ts2).flatMap(t2 -> Iterator.ofAll(this.ts3).flatMap(t3 -> Iterator.ofAll(this.ts4).map(t4 -> f.apply(t1, t2, t3, t4)))));
        }
    }

    public static class For3<T1, T2, T3> {
        private final Iterable<T1> ts1;
        private final Iterable<T2> ts2;
        private final Iterable<T3> ts3;

        private For3(Iterable<T1> ts1, Iterable<T2> ts2, Iterable<T3> ts3) {
            this.ts1 = ts1;
            this.ts2 = ts2;
            this.ts3 = ts3;
        }

        public <R> Iterator<R> yield(Function3<? super T1, ? super T2, ? super T3, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return Iterator.ofAll(this.ts1).flatMap(t1 -> Iterator.ofAll(this.ts2).flatMap(t2 -> Iterator.ofAll(this.ts3).map(t3 -> f.apply(t1, t2, t3))));
        }
    }

    public static class For2<T1, T2> {
        private final Iterable<T1> ts1;
        private final Iterable<T2> ts2;

        private For2(Iterable<T1> ts1, Iterable<T2> ts2) {
            this.ts1 = ts1;
            this.ts2 = ts2;
        }

        public <R> Iterator<R> yield(BiFunction<? super T1, ? super T2, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return Iterator.ofAll(this.ts1).flatMap(t1 -> Iterator.ofAll(this.ts2).map(t2 -> f.apply((Object)t1, (Object)t2)));
        }
    }

    public static class For1<T1> {
        private final Iterable<T1> ts1;

        private For1(Iterable<T1> ts1) {
            this.ts1 = ts1;
        }

        public <R> Iterator<R> yield(Function<? super T1, ? extends R> f) {
            Objects.requireNonNull(f, "f is null");
            return Iterator.ofAll(this.ts1).map(f);
        }

        public Iterator<T1> yield() {
            return this.yield(Function.identity());
        }
    }
}

