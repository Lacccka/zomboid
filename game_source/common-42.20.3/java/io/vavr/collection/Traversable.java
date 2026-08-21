/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.PartialFunction;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Value;
import io.vavr.collection.AbstractIterator;
import io.vavr.collection.CharSeq;
import io.vavr.collection.Comparators;
import io.vavr.collection.Foldable;
import io.vavr.collection.Iterator;
import io.vavr.collection.Map;
import io.vavr.collection.TraversableModule;
import io.vavr.control.Option;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Traversable<T>
extends Foldable<T>,
Value<T> {
    public static <T> Traversable<T> narrow(Traversable<? extends T> traversable) {
        return traversable;
    }

    default public <K> Option<Map<K, T>> arrangeBy(Function<? super T, ? extends K> getKey) {
        return Option.of(this.groupBy(getKey).mapValues(Traversable::singleOption)).filter(map -> !map.exists(kv -> ((Option)kv._2).isEmpty())).map((T map) -> Map.narrow(map.mapValues(Option::get)));
    }

    default public Option<Double> average() {
        try {
            double[] sum = TraversableModule.neumaierSum(this, t -> ((Number)t).doubleValue());
            double count = sum[1];
            return count == 0.0 ? Option.none() : Option.some(sum[0] / count);
        }
        catch (ClassCastException x) {
            throw new UnsupportedOperationException("not numeric", x);
        }
    }

    public <R> Traversable<R> collect(PartialFunction<? super T, ? extends R> var1);

    default public boolean containsAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        for (T element : elements) {
            if (this.contains(element)) continue;
            return false;
        }
        return true;
    }

    default public int count(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.foldLeft(0, (i, t) -> predicate.test(t) ? i + 1 : i);
    }

    public Traversable<T> distinct();

    public Traversable<T> distinctBy(Comparator<? super T> var1);

    public <U> Traversable<T> distinctBy(Function<? super T, ? extends U> var1);

    public Traversable<T> drop(int var1);

    public Traversable<T> dropRight(int var1);

    public Traversable<T> dropUntil(Predicate<? super T> var1);

    public Traversable<T> dropWhile(Predicate<? super T> var1);

    @Override
    public boolean equals(Object var1);

    default public boolean existsUnique(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        boolean exists = false;
        for (Object t : this) {
            if (!predicate.test(t)) continue;
            if (exists) {
                return false;
            }
            exists = true;
        }
        return exists;
    }

    public Traversable<T> filter(Predicate<? super T> var1);

    default public Traversable<T> reject(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.filter(predicate.negate());
    }

    default public Option<T> find(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        for (Object a : this) {
            if (!predicate.test(a)) continue;
            return Option.some(a);
        }
        return Option.none();
    }

    default public Option<T> findLast(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.iterator().findLast(predicate);
    }

    public <U> Traversable<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> var1);

    @Override
    default public <U> U foldLeft(U zero, BiFunction<? super U, ? super T, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        U xs = zero;
        for (Object x : this) {
            xs = f.apply(xs, x);
        }
        return xs;
    }

    @Override
    public <U> U foldRight(U var1, BiFunction<? super T, ? super U, ? extends U> var2);

    default public void forEachWithIndex(ObjIntConsumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        int index = 0;
        for (Object t : this) {
            action.accept(t, index++);
        }
    }

    @Override
    default public T get() {
        return this.head();
    }

    public <C> Map<C, ? extends Traversable<T>> groupBy(Function<? super T, ? extends C> var1);

    public Iterator<? extends Traversable<T>> grouped(int var1);

    public boolean hasDefiniteSize();

    public T head();

    default public Option<T> headOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.head());
    }

    @Override
    public int hashCode();

    public Traversable<T> init();

    default public Option<? extends Traversable<T>> initOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.init());
    }

    default public boolean isDistinct() {
        return false;
    }

    @Override
    default public boolean isEmpty() {
        return this.length() == 0;
    }

    default public boolean isOrdered() {
        return false;
    }

    default public boolean isSequential() {
        return false;
    }

    @Override
    default public boolean isSingleValued() {
        return false;
    }

    public boolean isTraversableAgain();

    @Override
    default public Iterator<T> iterator() {
        final Traversable that = this;
        return new AbstractIterator<T>(){
            Traversable<T> traversable;
            {
                this.traversable = that;
            }

            @Override
            public boolean hasNext() {
                return !this.traversable.isEmpty();
            }

            @Override
            public T getNext() {
                Object result = this.traversable.head();
                this.traversable = this.traversable.tail();
                return result;
            }
        };
    }

    public T last();

    default public Option<T> lastOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.last());
    }

    public int length();

    @Override
    public <U> Traversable<U> map(Function<? super T, ? extends U> var1);

    default public Option<T> max() {
        return this.maxBy(Comparators.naturalComparator());
    }

    default public Option<T> maxBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        if (this.isEmpty()) {
            return Option.none();
        }
        Object value = this.reduce((t1, t2) -> comparator.compare(t1, t2) >= 0 ? t1 : t2);
        return Option.some(value);
    }

    default public <U extends Comparable<? super U>> Option<T> maxBy(Function<? super T, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        if (this.isEmpty()) {
            return Option.none();
        }
        java.util.Iterator iter = this.iterator();
        Object tm = iter.next();
        Comparable um = (Comparable)f.apply(tm);
        while (iter.hasNext()) {
            Object t = iter.next();
            Comparable u = (Comparable)f.apply(t);
            if (u.compareTo(um) <= 0) continue;
            um = u;
            tm = t;
        }
        return Option.some(tm);
    }

    default public Option<T> min() {
        Object min;
        if (this.isEmpty()) {
            return Option.none();
        }
        T head = this.head();
        if (head instanceof Double) {
            min = this.foldLeft((Double)head, Math::min);
        } else if (head instanceof Float) {
            min = this.foldLeft((Float)head, Math::min);
        } else {
            Comparator comparator = Comparators.naturalComparator();
            min = this.foldLeft(head, (t1, t2) -> comparator.compare(t1, t2) <= 0 ? t1 : t2);
        }
        return Option.some(min);
    }

    default public Option<T> minBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        if (this.isEmpty()) {
            return Option.none();
        }
        Object value = this.reduce((t1, t2) -> comparator.compare(t1, t2) <= 0 ? t1 : t2);
        return Option.some(value);
    }

    default public <U extends Comparable<? super U>> Option<T> minBy(Function<? super T, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        if (this.isEmpty()) {
            return Option.none();
        }
        java.util.Iterator iter = this.iterator();
        Object tm = iter.next();
        Comparable um = (Comparable)f.apply(tm);
        while (iter.hasNext()) {
            Object t = iter.next();
            Comparable u = (Comparable)f.apply(t);
            if (u.compareTo(um) >= 0) continue;
            um = u;
            tm = t;
        }
        return Option.some(tm);
    }

    default public CharSeq mkCharSeq() {
        return this.mkCharSeq("", "", "");
    }

    default public CharSeq mkCharSeq(CharSequence delimiter) {
        return this.mkCharSeq("", delimiter, "");
    }

    default public CharSeq mkCharSeq(CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
        return CharSeq.of(this.mkString(prefix, delimiter, suffix));
    }

    default public String mkString() {
        return this.mkString("", "", "");
    }

    default public String mkString(CharSequence delimiter) {
        return this.mkString("", delimiter, "");
    }

    default public String mkString(CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
        StringBuilder builder = new StringBuilder(prefix);
        this.iterator().map(String::valueOf).intersperse(String.valueOf(delimiter)).forEach(builder::append);
        return builder.append(suffix).toString();
    }

    default public boolean nonEmpty() {
        return !this.isEmpty();
    }

    public Traversable<T> orElse(Iterable<? extends T> var1);

    public Traversable<T> orElse(Supplier<? extends Iterable<? extends T>> var1);

    public Tuple2<? extends Traversable<T>, ? extends Traversable<T>> partition(Predicate<? super T> var1);

    @Override
    public Traversable<T> peek(Consumer<? super T> var1);

    default public Number product() {
        if (this.isEmpty()) {
            return 1;
        }
        try {
            java.util.Iterator iter = this.iterator();
            Object o = iter.next();
            if (o instanceof Integer || o instanceof Long || o instanceof Byte || o instanceof Short) {
                return iter.foldLeft(((Number)o).longValue(), (product, number) -> product * number.longValue());
            }
            if (o instanceof BigInteger) {
                return iter.foldLeft((BigInteger)o, BigInteger::multiply);
            }
            if (o instanceof BigDecimal) {
                return iter.foldLeft((BigDecimal)o, BigDecimal::multiply);
            }
            return iter.toJavaStream().mapToDouble(Number::doubleValue).reduce(((Number)o).doubleValue(), (d1, d2) -> d1 * d2);
        }
        catch (ClassCastException x) {
            throw new UnsupportedOperationException("not numeric", x);
        }
    }

    @Override
    default public T reduceLeft(BiFunction<? super T, ? super T, ? extends T> op) {
        Objects.requireNonNull(op, "op is null");
        return this.iterator().reduceLeft(op);
    }

    @Override
    default public Option<T> reduceLeftOption(BiFunction<? super T, ? super T, ? extends T> op) {
        Objects.requireNonNull(op, "op is null");
        return this.isEmpty() ? Option.none() : Option.some(this.reduceLeft(op));
    }

    @Override
    default public T reduceRight(BiFunction<? super T, ? super T, ? extends T> op) {
        Objects.requireNonNull(op, "op is null");
        if (this.isEmpty()) {
            throw new NoSuchElementException("reduceRight on empty");
        }
        return this.iterator().reduceRight(op);
    }

    @Override
    default public Option<T> reduceRightOption(BiFunction<? super T, ? super T, ? extends T> op) {
        Objects.requireNonNull(op, "op is null");
        return this.isEmpty() ? Option.none() : Option.some(this.reduceRight(op));
    }

    public Traversable<T> replace(T var1, T var2);

    public Traversable<T> replaceAll(T var1, T var2);

    public Traversable<T> retainAll(Iterable<? extends T> var1);

    public Traversable<T> scan(T var1, BiFunction<? super T, ? super T, ? extends T> var2);

    public <U> Traversable<U> scanLeft(U var1, BiFunction<? super U, ? super T, ? extends U> var2);

    public <U> Traversable<U> scanRight(U var1, BiFunction<? super T, ? super U, ? extends U> var2);

    default public T single() {
        return this.singleOption().getOrElseThrow(() -> new NoSuchElementException("Does not contain a single value"));
    }

    default public Option<T> singleOption() {
        java.util.Iterator it = this.iterator();
        if (!it.hasNext()) {
            return Option.none();
        }
        Object first = it.next();
        if (it.hasNext()) {
            return Option.none();
        }
        return Option.some(first);
    }

    default public int size() {
        return this.length();
    }

    public Iterator<? extends Traversable<T>> slideBy(Function<? super T, ?> var1);

    public Iterator<? extends Traversable<T>> sliding(int var1);

    public Iterator<? extends Traversable<T>> sliding(int var1, int var2);

    public Tuple2<? extends Traversable<T>, ? extends Traversable<T>> span(Predicate<? super T> var1);

    @Override
    default public Spliterator<T> spliterator() {
        int characteristics = 1024;
        if (this.isDistinct()) {
            characteristics |= 1;
        }
        if (this.isOrdered()) {
            characteristics |= 0x14;
        }
        if (this.isSequential()) {
            characteristics |= 0x10;
        }
        if (this.hasDefiniteSize()) {
            return Spliterators.spliterator(this.iterator(), (long)this.length(), characteristics |= 0x4040);
        }
        return Spliterators.spliteratorUnknownSize(this.iterator(), characteristics);
    }

    default public Number sum() {
        if (this.isEmpty()) {
            return 0;
        }
        try {
            java.util.Iterator iter = this.iterator();
            Object o = iter.next();
            if (o instanceof Integer || o instanceof Long || o instanceof Byte || o instanceof Short) {
                return iter.foldLeft(((Number)o).longValue(), (sum, number) -> sum + number.longValue());
            }
            if (o instanceof BigInteger) {
                return iter.foldLeft((BigInteger)o, BigInteger::add);
            }
            if (o instanceof BigDecimal) {
                return iter.foldLeft((BigDecimal)o, BigDecimal::add);
            }
            return TraversableModule.neumaierSum(Iterator.of(o).concat(iter), t -> ((Number)t).doubleValue())[0];
        }
        catch (ClassCastException x) {
            throw new UnsupportedOperationException("not numeric", x);
        }
    }

    public Traversable<T> tail();

    public Option<? extends Traversable<T>> tailOption();

    public Traversable<T> take(int var1);

    public Traversable<T> takeRight(int var1);

    public Traversable<T> takeUntil(Predicate<? super T> var1);

    public Traversable<T> takeWhile(Predicate<? super T> var1);

    public <T1, T2> Tuple2<? extends Traversable<T1>, ? extends Traversable<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> var1);

    public <T1, T2, T3> Tuple3<? extends Traversable<T1>, ? extends Traversable<T2>, ? extends Traversable<T3>> unzip3(Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> var1);

    public <U> Traversable<Tuple2<T, U>> zip(Iterable<? extends U> var1);

    public <U> Traversable<Tuple2<T, U>> zipAll(Iterable<? extends U> var1, T var2, U var3);

    public <U, R> Traversable<R> zipWith(Iterable<? extends U> var1, BiFunction<? super T, ? super U, ? extends R> var2);

    public Traversable<Tuple2<T, Integer>> zipWithIndex();

    public <U> Traversable<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> var1);
}

