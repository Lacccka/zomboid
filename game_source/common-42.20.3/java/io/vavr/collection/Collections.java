/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.AbstractIterator;
import io.vavr.collection.ArrayType;
import io.vavr.collection.GwtIncompatible;
import io.vavr.collection.HashSet;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Iterator;
import io.vavr.collection.JavaConverters;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Multimap;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.Traversable;
import io.vavr.control.Option;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

final class Collections {
    Collections() {
    }

    static boolean areEqual(Iterable<?> iterable1, Iterable<?> iterable2) {
        java.util.Iterator<?> iter1 = iterable1.iterator();
        java.util.Iterator<?> iter2 = iterable2.iterator();
        while (iter1.hasNext() && iter2.hasNext()) {
            if (Objects.equals(iter1.next(), iter2.next())) continue;
            return false;
        }
        return iter1.hasNext() == iter2.hasNext();
    }

    @GwtIncompatible
    static <T, C extends Seq<T>> C asJava(C source2, Consumer<? super java.util.List<T>> action, JavaConverters.ChangePolicy changePolicy) {
        Objects.requireNonNull(action, "action is null");
        JavaConverters.ListView view = JavaConverters.asJava(source2, changePolicy);
        action.accept(view);
        return (C)((Seq)view.getDelegate());
    }

    static <T, S extends Seq<T>> Iterator<S> crossProduct(S empty, S seq, int power) {
        if (power < 0) {
            return Iterator.empty();
        }
        return Iterator.range(0, power).foldLeft(Iterator.of(empty), (product, ignored) -> product.flatMap(el -> seq.map(t -> el.append(t))));
    }

    static <T, S extends IndexedSeq<T>> S dropRightUntil(S seq, Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        for (int i = seq.length() - 1; i >= 0; --i) {
            if (!predicate.test(seq.get(i))) continue;
            return (S)seq.take(i + 1);
        }
        return (S)seq.take(0);
    }

    static <T, S extends IndexedSeq<T>> S dropUntil(S seq, Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        for (int i = 0; i < seq.length(); ++i) {
            if (!predicate.test(seq.get(i))) continue;
            return (S)seq.drop(i);
        }
        return (S)seq.take(0);
    }

    static <K, V> boolean equals(Map<K, V> source2, Object object) {
        if (source2 == object) {
            return true;
        }
        if (source2 != null && object instanceof Map) {
            Map map = (Map)object;
            if (source2.size() != map.size()) {
                return false;
            }
            try {
                return source2.forAll(map::contains);
            }
            catch (ClassCastException e) {
                return false;
            }
        }
        return false;
    }

    static <K, V> boolean equals(Multimap<K, V> source2, Object object) {
        if (source2 == object) {
            return true;
        }
        if (source2 != null && object instanceof Multimap) {
            Multimap multimap = (Multimap)object;
            if (source2.size() != multimap.size()) {
                return false;
            }
            try {
                return source2.forAll(multimap::contains);
            }
            catch (ClassCastException e) {
                return false;
            }
        }
        return false;
    }

    static <V> boolean equals(Seq<V> source2, Object object) {
        if (object == source2) {
            return true;
        }
        if (source2 != null && object instanceof Seq) {
            Seq seq = (Seq)object;
            return seq.size() == source2.size() && Collections.areEqual(source2, seq);
        }
        return false;
    }

    static <V> boolean equals(Set<V> source2, Object object) {
        if (source2 == object) {
            return true;
        }
        if (source2 != null && object instanceof Set) {
            Set set = (Set)object;
            if (source2.size() != set.size()) {
                return false;
            }
            try {
                return source2.forAll(set::contains);
            }
            catch (ClassCastException e) {
                return false;
            }
        }
        return false;
    }

    static <T> Iterator<T> fill(int n, Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return Collections.tabulate(n, ignored -> supplier.get());
    }

    static <T> Iterator<T> fillObject(int n, T element) {
        if (n <= 0) {
            return Iterator.empty();
        }
        return Iterator.continually(element).take(n);
    }

    static <C extends Traversable<T>, T> C fill(int n, Supplier<? extends T> s, C empty, Function<T[], C> of) {
        Objects.requireNonNull(s, "s is null");
        Objects.requireNonNull(empty, "empty is null");
        Objects.requireNonNull(of, "of is null");
        return Collections.tabulate(n, anything -> s.get(), empty, of);
    }

    static <C extends Traversable<T>, T> C fillObject(int n, T element, C empty, Function<T[], C> of) {
        Objects.requireNonNull(empty, "empty is null");
        Objects.requireNonNull(of, "of is null");
        if (n <= 0) {
            return empty;
        }
        Object[] elements = new Object[n];
        Arrays.fill(elements, element);
        return (C)((Traversable)of.apply((Object[][])elements));
    }

    static <T, C, R extends Iterable<T>> Map<C, R> groupBy(Traversable<T> source2, Function<? super T, ? extends C> classifier, Function<? super Iterable<T>, R> mapper) {
        Objects.requireNonNull(classifier, "classifier is null");
        Objects.requireNonNull(mapper, "mapper is null");
        Map<C, R> results = LinkedHashMap.empty();
        for (Map.Entry<C, Collection<T>> entry : Collections.groupBy(source2, classifier)) {
            results = results.put(entry.getKey(), mapper.apply(entry.getValue()));
        }
        return results;
    }

    private static <T, C> java.util.Set<Map.Entry<C, Collection<T>>> groupBy(Traversable<T> source2, Function<? super T, ? extends C> classifier) {
        java.util.LinkedHashMap<Object, Collection> results = new java.util.LinkedHashMap<Object, Collection>(source2.isTraversableAgain() ? source2.size() : 16);
        for (Object value : source2) {
            C key = classifier.apply(value);
            results.computeIfAbsent(key, k -> new ArrayList()).add(value);
        }
        return results.entrySet();
    }

    static int hashOrdered(Iterable<?> iterable) {
        return Collections.hash(iterable, (acc, hash) -> acc * 31 + hash);
    }

    static int hashUnordered(Iterable<?> iterable) {
        return Collections.hash(iterable, (acc, hash) -> acc + hash);
    }

    private static int hash(Iterable<?> iterable, IntBinaryOperator accumulator) {
        if (iterable == null) {
            return 0;
        }
        int hashCode = 1;
        for (Object o : iterable) {
            hashCode = accumulator.applyAsInt(hashCode, Objects.hashCode(o));
        }
        return hashCode;
    }

    static Option<Integer> indexOption(int index) {
        return Option.when(index >= 0, index);
    }

    static boolean isEmpty(Iterable<?> iterable) {
        return iterable instanceof Traversable && ((Traversable)iterable).isEmpty() || iterable instanceof Collection && ((Collection)iterable).isEmpty() || !iterable.iterator().hasNext();
    }

    static <T> boolean isTraversableAgain(Iterable<? extends T> iterable) {
        return iterable instanceof Collection || iterable instanceof Traversable && ((Traversable)iterable).isTraversableAgain();
    }

    static <T> T last(Traversable<T> source2) {
        if (source2.isEmpty()) {
            throw new NoSuchElementException("last of empty " + source2.stringPrefix());
        }
        java.util.Iterator it = source2.iterator();
        T result = null;
        while (it.hasNext()) {
            result = (T)it.next();
        }
        return result;
    }

    static <K, V, K2, U extends Map<K2, V>> U mapKeys(Map<K, V> source2, U zero, Function<? super K, ? extends K2> keyMapper, BiFunction<? super V, ? super V, ? extends V> valueMerge) {
        Objects.requireNonNull(zero, "zero is null");
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMerge, "valueMerge is null");
        return (U)source2.foldLeft(zero, (acc, entry) -> {
            Object k2 = keyMapper.apply((Object)entry._1);
            Object v2 = entry._2;
            Option v1 = acc.get(k2);
            Object v = v1.isDefined() ? valueMerge.apply((Object)v1.get(), (Object)v2) : v2;
            return acc.put(k2, v);
        });
    }

    static <C extends Traversable<T>, T> Tuple2<C, C> partition(C collection, Function<Iterable<T>, C> creator, Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        ArrayList left = new ArrayList();
        ArrayList right = new ArrayList();
        for (Object element : collection) {
            (predicate.test(element) ? left : right).add(element);
        }
        return Tuple.of(creator.apply(left), creator.apply(right));
    }

    static <C extends Traversable<T>, T> C removeAll(C source2, Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (source2.isEmpty()) {
            return source2;
        }
        HashSet removed = HashSet.ofAll(elements);
        return (C)(removed.isEmpty() ? source2 : source2.filter(e -> !removed.contains(e)));
    }

    static <C extends Traversable<T>, T> C reject(C source2, Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (source2.isEmpty()) {
            return source2;
        }
        return (C)source2.filter(predicate.negate());
    }

    static <C extends Traversable<T>, T> C removeAll(C source2, T element) {
        if (source2.isEmpty()) {
            return source2;
        }
        return (C)source2.filter(e -> !Objects.equals(e, element));
    }

    static <C extends Traversable<T>, T> C retainAll(C source2, Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (source2.isEmpty()) {
            return source2;
        }
        HashSet<? extends T> retained = HashSet.ofAll(elements);
        return (C)source2.filter(retained::contains);
    }

    static <T> Iterator<T> reverseIterator(Iterable<T> iterable) {
        if (iterable instanceof java.util.List) {
            return Collections.reverseListIterator((java.util.List)iterable);
        }
        if (iterable instanceof Seq) {
            return ((Seq)iterable).reverseIterator();
        }
        return List.empty().pushAll(iterable).iterator();
    }

    private static <T> Iterator<T> reverseListIterator(final java.util.List<T> list) {
        return new Iterator<T>(){
            private final ListIterator<T> delegate;
            {
                this.delegate = list.listIterator(list.size());
            }

            @Override
            public boolean hasNext() {
                return this.delegate.hasPrevious();
            }

            @Override
            public T next() {
                return this.delegate.previous();
            }
        };
    }

    static <T, C extends Seq<T>> C rotateLeft(C source2, int n) {
        if (source2.isEmpty() || n == 0) {
            return source2;
        }
        if (n < 0) {
            return Collections.rotateRight(source2, -n);
        }
        int len = source2.length();
        int m = n % len;
        if (m == 0) {
            return source2;
        }
        return (C)source2.drop(m).appendAll(source2.take(m));
    }

    static <T, C extends Seq<T>> C rotateRight(C source2, int n) {
        if (source2.isEmpty() || n == 0) {
            return source2;
        }
        if (n < 0) {
            return Collections.rotateLeft(source2, -n);
        }
        int len = source2.length();
        int m = n % len;
        if (m == 0) {
            return source2;
        }
        return (C)source2.takeRight(m).appendAll(source2.dropRight(m));
    }

    static <T, U, R extends Traversable<U>> R scanLeft(Traversable<? extends T> source2, U zero, BiFunction<? super U, ? super T, ? extends U> operation, Function<Iterator<U>, R> finisher) {
        Objects.requireNonNull(operation, "operation is null");
        Traversable iterator2 = source2.iterator().scanLeft(zero, operation);
        return (R)((Traversable)finisher.apply((Iterator<U>)iterator2));
    }

    static <T, U, R extends Traversable<U>> R scanRight(Traversable<? extends T> source2, U zero, BiFunction<? super T, ? super U, ? extends U> operation, Function<Iterator<U>, R> finisher) {
        Objects.requireNonNull(operation, "operation is null");
        Iterator<? extends T> reversedElements = Collections.reverseIterator(source2);
        return (R)Collections.scanLeft(reversedElements, zero, (u, t) -> operation.apply(t, u), us -> (Traversable)finisher.apply(Collections.reverseIterator(us)));
    }

    static <T, U, R extends Seq<T>> R sortBy(Seq<? extends T> source2, Comparator<? super U> comparator, Function<? super T, ? extends U> mapper, Collector<T, ?, R> collector) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return (R)((Seq)source2.toJavaStream().sorted((e1, e2) -> comparator.compare((Object)mapper.apply(e1), (Object)mapper.apply(e2))).collect(collector));
    }

    static <T, S extends Seq<T>> S shuffle(S source2, Function<? super Iterable<T>, S> ofAll) {
        if (source2.length() <= 1) {
            return source2;
        }
        java.util.List list = source2.toJavaList();
        java.util.Collections.shuffle(list);
        return (S)((Seq)ofAll.apply(list));
    }

    static void subSequenceRangeCheck(int beginIndex, int endIndex, int length) {
        if (beginIndex < 0 || endIndex > length) {
            throw new IndexOutOfBoundsException("subSequence(" + beginIndex + ", " + endIndex + "), length = " + length);
        }
        if (beginIndex > endIndex) {
            throw new IllegalArgumentException("subSequence(" + beginIndex + ", " + endIndex + ")");
        }
    }

    static <T> Iterator<T> tabulate(final int n, final Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        if (n <= 0) {
            return Iterator.empty();
        }
        return new AbstractIterator<T>(){
            int i = 0;

            @Override
            public boolean hasNext() {
                return this.i < n;
            }

            @Override
            protected T getNext() {
                return f.apply(this.i++);
            }
        };
    }

    static <C extends Traversable<T>, T> C tabulate(int n, Function<? super Integer, ? extends T> f, C empty, Function<T[], C> of) {
        Objects.requireNonNull(f, "f is null");
        Objects.requireNonNull(empty, "empty is null");
        Objects.requireNonNull(of, "of is null");
        if (n <= 0) {
            return empty;
        }
        Object[] elements = new Object[n];
        for (int i = 0; i < n; ++i) {
            elements[i] = f.apply(i);
        }
        return (C)((Traversable)of.apply((Object[][])elements));
    }

    static <T, S extends IndexedSeq<T>> S takeRightUntil(S seq, Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        for (int i = seq.length() - 1; i >= 0; --i) {
            if (!predicate.test(seq.get(i))) continue;
            return (S)seq.drop(i + 1);
        }
        return seq;
    }

    static <T, S extends IndexedSeq<T>> S takeUntil(S seq, Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        for (int i = 0; i < seq.length(); ++i) {
            if (!predicate.test(seq.get(i))) continue;
            return (S)seq.take(i);
        }
        return seq;
    }

    static <T, U extends Seq<T>, V extends Seq<U>> V transpose(V matrix, Function<Iterable<U>, V> rowFactory, Function<T[], U> columnFactory) {
        Objects.requireNonNull(matrix, "matrix is null");
        if (matrix.isEmpty() || matrix.length() == 1 && ((Seq)matrix.head()).length() <= 1) {
            return matrix;
        }
        return Collections.transposeNonEmptyMatrix(matrix, rowFactory, columnFactory);
    }

    private static <T, U extends Seq<T>, V extends Seq<U>> V transposeNonEmptyMatrix(V matrix, Function<Iterable<U>, V> rowFactory, Function<T[], U> columnFactory) {
        int newHeight = ((Seq)matrix.head()).size();
        int newWidth = matrix.size();
        Object[][] results = new Object[newHeight][newWidth];
        if (matrix.exists(r -> r.size() != newHeight)) {
            throw new IllegalArgumentException("the parameter `matrix` is invalid!");
        }
        int rowIndex = 0;
        for (Seq row : matrix) {
            int columnIndex = 0;
            for (Object element : row) {
                results[columnIndex][rowIndex] = element;
                ++columnIndex;
            }
            ++rowIndex;
        }
        return (V)((Seq)rowFactory.apply(Iterator.of(results).map(columnFactory)));
    }

    static <T> IterableWithSize<T> withSize(Iterable<? extends T> iterable) {
        return Collections.isTraversableAgain(iterable) ? Collections.withSizeTraversable(iterable) : Collections.withSizeTraversable(List.ofAll(iterable));
    }

    private static <T> IterableWithSize<T> withSizeTraversable(Iterable<? extends T> iterable) {
        if (iterable instanceof Collection) {
            return new IterableWithSize<T>(iterable, ((Collection)iterable).size());
        }
        return new IterableWithSize<T>(iterable, ((Traversable)iterable).size());
    }

    static class IterableWithSize<T> {
        private final Iterable<? extends T> iterable;
        private final int size;

        IterableWithSize(Iterable<? extends T> iterable, int size) {
            this.iterable = iterable;
            this.size = size;
        }

        java.util.Iterator<? extends T> iterator() {
            return this.iterable.iterator();
        }

        java.util.Iterator<? extends T> reverseIterator() {
            return Collections.reverseIterator(this.iterable);
        }

        int size() {
            return this.size;
        }

        Object[] toArray() {
            if (this.iterable instanceof Collection) {
                return ((Collection)this.iterable).toArray();
            }
            return ArrayType.asArray(this.iterator(), this.size());
        }
    }
}

