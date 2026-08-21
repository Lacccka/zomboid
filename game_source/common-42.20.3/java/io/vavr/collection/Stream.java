/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Lazy;
import io.vavr.PartialFunction;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Value;
import io.vavr.collection.AbstractIterator;
import io.vavr.collection.Collections;
import io.vavr.collection.GwtIncompatible;
import io.vavr.collection.Iterator;
import io.vavr.collection.JavaConverters;
import io.vavr.collection.LinearSeq;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Queue;
import io.vavr.collection.StreamModule;
import io.vavr.control.Option;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

public interface Stream<T>
extends LinearSeq<T> {
    public static final long serialVersionUID = 1L;

    public static <T> Collector<T, ArrayList<T>, Stream<T>> collector() {
        Supplier<ArrayList> supplier = ArrayList::new;
        BiConsumer<ArrayList, Object> accumulator = ArrayList::add;
        BinaryOperator combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        Function<ArrayList, Stream> finisher = Stream::ofAll;
        return Collector.of(supplier, accumulator, combiner, finisher, new Collector.Characteristics[0]);
    }

    @SafeVarargs
    public static <T> Stream<T> concat(Iterable<? extends T> ... iterables) {
        return Iterator.concat(iterables).toStream();
    }

    public static <T> Stream<T> concat(Iterable<? extends Iterable<? extends T>> iterables) {
        return Iterator.concat(iterables).toStream();
    }

    public static Stream<Integer> from(int value) {
        return Stream.ofAll(Iterator.from(value));
    }

    public static Stream<Integer> from(int value, int step) {
        return Stream.ofAll(Iterator.from(value, step));
    }

    public static Stream<Long> from(long value) {
        return Stream.ofAll(Iterator.from(value));
    }

    public static Stream<Long> from(long value, long step) {
        return Stream.ofAll(Iterator.from(value, step));
    }

    public static <T> Stream<T> continually(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return Stream.ofAll(Iterator.continually(supplier));
    }

    public static <T> Stream<T> iterate(T seed, Function<? super T, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return Stream.ofAll(Iterator.iterate(seed, f));
    }

    public static <T> Stream<T> iterate(Supplier<? extends Option<? extends T>> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return Stream.ofAll(Iterator.iterate(supplier));
    }

    public static <T> Stream<T> cons(T head, Supplier<? extends Stream<? extends T>> tailSupplier) {
        Objects.requireNonNull(tailSupplier, "tailSupplier is null");
        return new StreamModule.ConsImpl<T>(head, tailSupplier);
    }

    public static <T> Stream<T> empty() {
        return Empty.instance();
    }

    public static <T> Stream<T> narrow(Stream<? extends T> stream) {
        return stream;
    }

    public static <T> Stream<T> of(T element) {
        return Stream.cons(element, Empty::instance);
    }

    @SafeVarargs
    public static <T> Stream<T> of(final T ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Stream.ofAll(new Iterator<T>(){
            int i = 0;

            @Override
            public boolean hasNext() {
                return this.i < elements.length;
            }

            @Override
            public T next() {
                return elements[this.i++];
            }
        });
    }

    public static <T> Stream<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return Stream.ofAll(Collections.tabulate(n, f));
    }

    public static <T> Stream<T> fill(int n, Supplier<? extends T> s) {
        Objects.requireNonNull(s, "s is null");
        return Stream.ofAll(Collections.fill(n, s));
    }

    public static <T> Stream<T> fill(int n, T element) {
        return Stream.ofAll(Collections.fillObject(n, element));
    }

    public static <T> Stream<T> ofAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (elements instanceof Stream) {
            return (Stream)elements;
        }
        if (elements instanceof JavaConverters.ListView && ((JavaConverters.ListView)elements).getDelegate() instanceof Stream) {
            return (Stream)((JavaConverters.ListView)elements).getDelegate();
        }
        return StreamModule.StreamFactory.create(elements.iterator());
    }

    public static <T> Stream<T> ofAll(java.util.stream.Stream<? extends T> javaStream) {
        Objects.requireNonNull(javaStream, "javaStream is null");
        return StreamModule.StreamFactory.create(javaStream.iterator());
    }

    public static Stream<Boolean> ofAll(boolean ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Stream.ofAll(Iterator.ofAll(elements));
    }

    public static Stream<Byte> ofAll(byte ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Stream.ofAll(Iterator.ofAll(elements));
    }

    public static Stream<Character> ofAll(char ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Stream.ofAll(Iterator.ofAll(elements));
    }

    public static Stream<Double> ofAll(double ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Stream.ofAll(Iterator.ofAll(elements));
    }

    public static Stream<Float> ofAll(float ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Stream.ofAll(Iterator.ofAll(elements));
    }

    public static Stream<Integer> ofAll(int ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Stream.ofAll(Iterator.ofAll(elements));
    }

    public static Stream<Long> ofAll(long ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Stream.ofAll(Iterator.ofAll(elements));
    }

    public static Stream<Short> ofAll(short ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Stream.ofAll(Iterator.ofAll(elements));
    }

    public static Stream<Character> range(char from, char toExclusive) {
        return Stream.ofAll(Iterator.range(from, toExclusive));
    }

    public static Stream<Character> rangeBy(char from, char toExclusive, int step) {
        return Stream.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    @GwtIncompatible
    public static Stream<Double> rangeBy(double from, double toExclusive, double step) {
        return Stream.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static Stream<Integer> range(int from, int toExclusive) {
        return Stream.ofAll(Iterator.range(from, toExclusive));
    }

    public static Stream<Integer> rangeBy(int from, int toExclusive, int step) {
        return Stream.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static Stream<Long> range(long from, long toExclusive) {
        return Stream.ofAll(Iterator.range(from, toExclusive));
    }

    public static Stream<Long> rangeBy(long from, long toExclusive, long step) {
        return Stream.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static Stream<Character> rangeClosed(char from, char toInclusive) {
        return Stream.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static Stream<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return Stream.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    @GwtIncompatible
    public static Stream<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return Stream.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static Stream<Integer> rangeClosed(int from, int toInclusive) {
        return Stream.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static Stream<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return Stream.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static Stream<Long> rangeClosed(long from, long toInclusive) {
        return Stream.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static Stream<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return Stream.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static <T> Stream<Stream<T>> transpose(Stream<Stream<T>> matrix) {
        return Collections.transpose(matrix, Stream::ofAll, Stream::of);
    }

    public static <T, U> Stream<U> unfoldRight(T seed, Function<? super T, Option<Tuple2<? extends U, ? extends T>>> f) {
        return Iterator.unfoldRight(seed, f).toStream();
    }

    public static <T, U> Stream<U> unfoldLeft(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends U>>> f) {
        return Iterator.unfoldLeft(seed, f).toStream();
    }

    public static <T> Stream<T> unfold(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends T>>> f) {
        return Iterator.unfold(seed, f).toStream();
    }

    public static <T> Stream<T> continually(T t) {
        return Stream.ofAll(Iterator.continually(t));
    }

    @Override
    default public Stream<T> append(T element) {
        return this.isEmpty() ? Stream.of(element) : new StreamModule.AppendElements(this.head(), Queue.of(element), () -> this.tail());
    }

    @Override
    default public Stream<T> appendAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (Collections.isEmpty(elements)) {
            return this;
        }
        if (this.isEmpty()) {
            return Stream.ofAll(elements);
        }
        return Stream.ofAll(Iterator.concat(this, elements));
    }

    default public Stream<T> appendSelf(Function<? super Stream<T>, ? extends Stream<T>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.isEmpty() ? this : new StreamModule.AppendSelf(this, mapper).stream();
    }

    @Override
    @GwtIncompatible
    default public java.util.List<T> asJava() {
        return JavaConverters.asJava(this, JavaConverters.ChangePolicy.IMMUTABLE);
    }

    @Override
    @GwtIncompatible
    default public Stream<T> asJava(Consumer<? super java.util.List<T>> action) {
        return Collections.asJava(this, action, JavaConverters.ChangePolicy.IMMUTABLE);
    }

    @Override
    @GwtIncompatible
    default public java.util.List<T> asJavaMutable() {
        return JavaConverters.asJava(this, JavaConverters.ChangePolicy.MUTABLE);
    }

    @Override
    @GwtIncompatible
    default public Stream<T> asJavaMutable(Consumer<? super java.util.List<T>> action) {
        return Collections.asJava(this, action, JavaConverters.ChangePolicy.MUTABLE);
    }

    @Override
    default public <R> Stream<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        return Stream.ofAll(this.iterator().collect(partialFunction));
    }

    @Override
    default public Stream<Stream<T>> combinations() {
        return Stream.rangeClosed(0, this.length()).map((T n) -> this.combinations((int)n)).flatMap(Function.identity());
    }

    @Override
    default public Stream<Stream<T>> combinations(int k) {
        return StreamModule.Combinations.apply(this, Math.max(k, 0));
    }

    @Override
    default public Iterator<Stream<T>> crossProduct(int power) {
        return Collections.crossProduct(Stream.empty(), this, power);
    }

    default public Stream<T> cycle() {
        return this.isEmpty() ? this : this.appendSelf(Function.identity());
    }

    default public Stream<T> cycle(final int count) {
        if (count <= 0 || this.isEmpty()) {
            return Stream.empty();
        }
        final Stream self = this;
        return Stream.ofAll(new Iterator<T>(){
            Stream<T> stream;
            int i;
            {
                this.stream = self;
                this.i = count - 1;
            }

            @Override
            public boolean hasNext() {
                return !this.stream.isEmpty() || this.i > 0;
            }

            @Override
            public T next() {
                if (this.stream.isEmpty()) {
                    --this.i;
                    this.stream = self;
                }
                Object result = this.stream.head();
                this.stream = this.stream.tail();
                return result;
            }
        });
    }

    @Override
    default public Stream<T> distinct() {
        return this.distinctBy(Function.identity());
    }

    @Override
    default public Stream<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        TreeSet<? super T> seen = new TreeSet<T>(comparator);
        return this.filter(seen::add);
    }

    @Override
    default public <U> Stream<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        HashSet seen = new HashSet();
        return this.filter((T t) -> seen.add(keyExtractor.apply(t)));
    }

    @Override
    default public Stream<T> drop(int n) {
        LinearSeq<T> stream = this;
        while (n-- > 0 && !stream.isEmpty()) {
            stream = stream.tail();
        }
        return stream;
    }

    @Override
    default public Stream<T> dropUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.dropWhile((Predicate)predicate.negate());
    }

    @Override
    default public Stream<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        LinearSeq<T> stream = this;
        while (!stream.isEmpty() && predicate.test(stream.head())) {
            stream = stream.tail();
        }
        return stream;
    }

    @Override
    default public Stream<T> dropRight(int n) {
        if (n <= 0) {
            return this;
        }
        return StreamModule.DropRight.apply(this.take(n).toList(), List.empty(), this.drop(n));
    }

    @Override
    default public Stream<T> dropRightUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.reverse().dropUntil((Predicate)predicate).reverse();
    }

    @Override
    default public Stream<T> dropRightWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.dropRightUntil((Predicate)predicate.negate());
    }

    @Override
    default public Stream<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (this.isEmpty()) {
            return this;
        }
        LinearSeq<T> stream = this;
        while (!stream.isEmpty() && !predicate.test(stream.head())) {
            stream = stream.tail();
        }
        Stream finalStream = stream;
        return stream.isEmpty() ? Stream.empty() : Stream.cons(stream.head(), () -> finalStream.tail().filter(predicate));
    }

    @Override
    default public Stream<T> reject(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Collections.reject(this, predicate);
    }

    @Override
    default public <U> Stream<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.isEmpty() ? Empty.instance() : Stream.ofAll(new StreamModule.FlatMapIterator(this.iterator(), mapper));
    }

    @Override
    default public T get(int index) {
        if (this.isEmpty()) {
            throw new IndexOutOfBoundsException("get(" + index + ") on Nil");
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("get(" + index + ")");
        }
        LinearSeq<T> stream = this;
        for (int i = index - 1; i >= 0; --i) {
            if (!(stream = stream.tail()).isEmpty()) continue;
            throw new IndexOutOfBoundsException("get(" + index + ") on Stream of size " + (index - i));
        }
        return stream.head();
    }

    @Override
    default public <C> Map<C, Stream<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return Collections.groupBy(this, classifier, Stream::ofAll);
    }

    @Override
    default public Iterator<Stream<T>> grouped(int size) {
        return this.sliding(size, size);
    }

    @Override
    default public boolean hasDefiniteSize() {
        return false;
    }

    @Override
    default public int indexOf(T element, int from) {
        int index = 0;
        LinearSeq<T> stream = this;
        while (!stream.isEmpty()) {
            if (index >= from && Objects.equals(stream.head(), element)) {
                return index;
            }
            stream = stream.tail();
            ++index;
        }
        return -1;
    }

    @Override
    default public Stream<T> init() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("init of empty stream");
        }
        LinearSeq tail = this.tail();
        if (tail.isEmpty()) {
            return Empty.instance();
        }
        return Stream.cons(this.head(), () -> this.init());
    }

    @Override
    default public Option<Stream<T>> initOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.init());
    }

    @Override
    default public Stream<T> insert(int index, T element) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("insert(" + index + ", e)");
        }
        if (index == 0) {
            return Stream.cons(element, () -> this);
        }
        if (this.isEmpty()) {
            throw new IndexOutOfBoundsException("insert(" + index + ", e) on Nil");
        }
        return Stream.cons(this.head(), () -> this.tail().insert(index - 1, (Object)element));
    }

    @Override
    default public Stream<T> insertAll(int index, Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (index < 0) {
            throw new IndexOutOfBoundsException("insertAll(" + index + ", elements)");
        }
        if (index == 0) {
            return this.isEmpty() ? Stream.ofAll(elements) : Stream.ofAll(elements).appendAll((Iterable)this);
        }
        if (this.isEmpty()) {
            throw new IndexOutOfBoundsException("insertAll(" + index + ", elements) on Nil");
        }
        return Stream.cons(this.head(), () -> this.tail().insertAll(index - 1, (Iterable)elements));
    }

    @Override
    default public Stream<T> intersperse(T element) {
        if (this.isEmpty()) {
            return this;
        }
        return Stream.cons(this.head(), () -> {
            Stream<Object> tail = this.tail();
            return tail.isEmpty() ? tail : Stream.cons(element, () -> tail.intersperse(element));
        });
    }

    @Override
    default public boolean isAsync() {
        return false;
    }

    @Override
    default public boolean isLazy() {
        return true;
    }

    @Override
    default public boolean isTraversableAgain() {
        return true;
    }

    @Override
    default public T last() {
        return Collections.last(this);
    }

    @Override
    default public int lastIndexOf(T element, int end) {
        int result = -1;
        LinearSeq<T> stream = this;
        for (int index = 0; index <= end && !stream.isEmpty(); ++index) {
            if (Objects.equals(stream.head(), element)) {
                result = index;
            }
            stream = stream.tail();
        }
        return result;
    }

    @Override
    default public int length() {
        return this.foldLeft(0, (n, ignored) -> n + 1);
    }

    @Override
    default public <U> Stream<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (this.isEmpty()) {
            return Empty.instance();
        }
        return Stream.cons(mapper.apply(this.head()), () -> this.tail().map(mapper));
    }

    @Override
    default public Stream<T> padTo(int length, T element) {
        if (length <= 0) {
            return this;
        }
        if (this.isEmpty()) {
            return Stream.continually(element).take(length);
        }
        return Stream.cons(this.head(), () -> this.tail().padTo(length - 1, (Object)element));
    }

    @Override
    default public Stream<T> leftPadTo(int length, T element) {
        int actualLength = this.length();
        if (length <= actualLength) {
            return this;
        }
        return Stream.continually(element).take(length - actualLength).appendAll((Iterable)this);
    }

    @Override
    default public Stream<T> orElse(Iterable<? extends T> other) {
        return this.isEmpty() ? Stream.ofAll(other) : this;
    }

    @Override
    default public Stream<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return this.isEmpty() ? Stream.ofAll(supplier.get()) : this;
    }

    @Override
    default public Stream<T> patch(int from, Iterable<? extends T> that, int replaced) {
        from = from < 0 ? 0 : from;
        replaced = replaced < 0 ? 0 : replaced;
        LinearSeq result = this.take(from).appendAll((Iterable)that);
        result = result.appendAll((Iterable)this.drop(from += replaced));
        return result;
    }

    @Override
    default public Tuple2<Stream<T>, Stream<T>> partition(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Tuple.of(this.filter(predicate), this.filter((Predicate)predicate.negate()));
    }

    @Override
    default public Stream<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (this.isEmpty()) {
            return this;
        }
        Object head = this.head();
        action.accept(head);
        return Stream.cons(head, () -> this.tail().peek(action));
    }

    @Override
    default public Stream<Stream<T>> permutations() {
        if (this.isEmpty()) {
            return Empty.instance();
        }
        LinearSeq tail = this.tail();
        if (tail.isEmpty()) {
            return Stream.of(this);
        }
        Empty zero = Empty.instance();
        return this.distinct().foldLeft(zero, (xs, x) -> {
            Function<Stream, Stream> prepend = l -> l.prepend(x);
            return xs.appendAll((Iterable)this.remove(x).permutations().map(prepend));
        });
    }

    @Override
    default public Stream<T> prepend(T element) {
        return Stream.cons(element, () -> this);
    }

    @Override
    default public Stream<T> prependAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (this.isEmpty()) {
            if (elements instanceof Stream) {
                Stream stream = (Stream)elements;
                return stream;
            }
            return Stream.ofAll(elements);
        }
        return Stream.ofAll(elements).appendAll((Iterable)this);
    }

    @Override
    default public Stream<T> remove(T element) {
        if (this.isEmpty()) {
            return this;
        }
        Object head = this.head();
        return Objects.equals(head, element) ? this.tail() : Stream.cons(head, () -> this.tail().remove((Object)element));
    }

    @Override
    default public Stream<T> removeFirst(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (this.isEmpty()) {
            return this;
        }
        Object head = this.head();
        return predicate.test(head) ? this.tail() : Stream.cons(head, () -> this.tail().removeFirst(predicate));
    }

    @Override
    default public Stream<T> removeLast(Predicate<T> predicate) {
        return this.isEmpty() ? this : this.reverse().removeFirst((Predicate)predicate).reverse();
    }

    @Override
    default public Stream<T> removeAt(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("removeAt(" + index + ")");
        }
        if (index == 0) {
            return this.tail();
        }
        if (this.isEmpty()) {
            throw new IndexOutOfBoundsException("removeAt() on Nil");
        }
        return Stream.cons(this.head(), () -> this.tail().removeAt(index - 1));
    }

    @Override
    default public Stream<T> removeAll(T element) {
        return Collections.removeAll(this, element);
    }

    @Override
    default public Stream<T> removeAll(Iterable<? extends T> elements) {
        return Collections.removeAll(this, elements);
    }

    @Override
    @Deprecated
    default public Stream<T> removeAll(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.reject((Predicate)predicate);
    }

    @Override
    default public Stream<T> replace(T currentElement, T newElement) {
        if (this.isEmpty()) {
            return this;
        }
        Object head = this.head();
        if (Objects.equals(head, currentElement)) {
            return Stream.cons(newElement, () -> this.tail());
        }
        return Stream.cons(head, () -> this.tail().replace((Object)currentElement, (Object)newElement));
    }

    @Override
    default public Stream<T> replaceAll(T currentElement, T newElement) {
        if (this.isEmpty()) {
            return this;
        }
        Object head = this.head();
        T newHead = Objects.equals(head, currentElement) ? newElement : head;
        return Stream.cons(newHead, () -> this.tail().replaceAll((Object)currentElement, (Object)newElement));
    }

    @Override
    default public Stream<T> retainAll(Iterable<? extends T> elements) {
        return Collections.retainAll(this, elements);
    }

    /*
     * Exception decompiling
     */
    @Override
    default public Stream<T> reverse() {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * java.lang.IndexOutOfBoundsException: Index 1 out of bounds for length 1
         *     at java.base/jdk.internal.util.Preconditions.outOfBounds(Unknown Source)
         *     at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Unknown Source)
         *     at java.base/jdk.internal.util.Preconditions.checkIndex(Unknown Source)
         *     at java.base/java.util.Objects.checkIndex(Unknown Source)
         *     at java.base/java.util.ArrayList.get(Unknown Source)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:368)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:167)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteExpression(LambdaRewriter.java:105)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterHelper.applyForwards(ExpressionRewriterHelper.java:12)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMemberFunctionInvokation.applyExpressionRewriterToArgs(AbstractMemberFunctionInvokation.java:101)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMemberFunctionInvokation.applyExpressionRewriter(AbstractMemberFunctionInvokation.java:88)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteExpression(LambdaRewriter.java:103)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.TernaryExpression.applyExpressionRewriter(TernaryExpression.java:106)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteExpression(LambdaRewriter.java:103)
         *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn.rewriteExpressions(StructuredReturn.java:99)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewrite(LambdaRewriter.java:88)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.rewriteLambdas(Op04StructuredStatement.java:1137)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:912)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    @Override
    default public Stream<T> rotateLeft(int n) {
        return Collections.rotateLeft(this, n);
    }

    @Override
    default public Stream<T> rotateRight(int n) {
        return Collections.rotateRight(this, n);
    }

    @Override
    default public Stream<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        return this.scanLeft(zero, operation);
    }

    @Override
    default public <U> Stream<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        return Collections.scanLeft(this, zero, operation, Value::toStream);
    }

    @Override
    default public <U> Stream<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        return Collections.scanRight(this, zero, operation, Value::toStream);
    }

    @Override
    default public Stream<T> shuffle() {
        return Collections.shuffle(this, Stream::ofAll);
    }

    @Override
    default public Stream<T> slice(int beginIndex, int endIndex) {
        if (beginIndex >= endIndex || this.isEmpty()) {
            return Stream.empty();
        }
        int lowerBound = Math.max(beginIndex, 0);
        if (lowerBound == 0) {
            return Stream.cons(this.head(), () -> this.tail().slice(0, endIndex - 1));
        }
        return this.tail().slice(lowerBound - 1, endIndex - 1);
    }

    @Override
    default public Iterator<Stream<T>> slideBy(Function<? super T, ?> classifier) {
        return this.iterator().slideBy(classifier).map(Stream::ofAll);
    }

    @Override
    default public Iterator<Stream<T>> sliding(int size) {
        return this.sliding(size, 1);
    }

    @Override
    default public Iterator<Stream<T>> sliding(int size, int step) {
        return this.iterator().sliding(size, step).map(Stream::ofAll);
    }

    @Override
    default public Stream<T> sorted() {
        return this.isEmpty() ? this : this.toJavaStream().sorted().collect(Stream.collector());
    }

    @Override
    default public Stream<T> sorted(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return this.isEmpty() ? this : this.toJavaStream().sorted(comparator).collect(Stream.collector());
    }

    @Override
    default public <U extends Comparable<? super U>> Stream<T> sortBy(Function<? super T, ? extends U> mapper) {
        return this.sortBy(Comparable::compareTo, (Function)mapper);
    }

    @Override
    default public <U> Stream<T> sortBy(Comparator<? super U> comparator, Function<? super T, ? extends U> mapper) {
        return Collections.sortBy(this, comparator, mapper, Stream.collector());
    }

    @Override
    default public Tuple2<Stream<T>, Stream<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Tuple.of(this.takeWhile((Predicate)predicate), this.dropWhile((Predicate)predicate));
    }

    @Override
    default public Tuple2<Stream<T>, Stream<T>> splitAt(int n) {
        return Tuple.of(this.take(n), this.drop(n));
    }

    @Override
    default public Tuple2<Stream<T>, Stream<T>> splitAt(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Tuple.of(this.takeWhile((Predicate)predicate.negate()), this.dropWhile((Predicate)predicate.negate()));
    }

    @Override
    default public Tuple2<Stream<T>, Stream<T>> splitAtInclusive(Predicate<? super T> predicate) {
        Tuple2<Stream<T>, Stream<T>> split = this.splitAt(predicate);
        if (((Stream)split._2).isEmpty()) {
            return split;
        }
        return Tuple.of(((Stream)split._1).append(((Stream)split._2).head()), ((Stream)split._2).tail());
    }

    @Override
    default public String stringPrefix() {
        return "Stream";
    }

    @Override
    default public Stream<T> subSequence(int beginIndex) {
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException("subSequence(" + beginIndex + ")");
        }
        LinearSeq<T> result = this;
        int i = 0;
        while (i < beginIndex) {
            if (result.isEmpty()) {
                throw new IndexOutOfBoundsException("subSequence(" + beginIndex + ") on Stream of size " + i);
            }
            ++i;
            result = result.tail();
        }
        return result;
    }

    @Override
    default public Stream<T> subSequence(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException("subSequence(" + beginIndex + ", " + endIndex + ")");
        }
        if (beginIndex > endIndex) {
            throw new IllegalArgumentException("subSequence(" + beginIndex + ", " + endIndex + ")");
        }
        if (beginIndex == endIndex) {
            return Empty.instance();
        }
        if (this.isEmpty()) {
            throw new IndexOutOfBoundsException("subSequence of Nil");
        }
        if (beginIndex == 0) {
            return Stream.cons(this.head(), () -> this.tail().subSequence(0, endIndex - 1));
        }
        return this.tail().subSequence(beginIndex - 1, endIndex - 1);
    }

    @Override
    public Stream<T> tail();

    @Override
    default public Option<Stream<T>> tailOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.tail());
    }

    @Override
    default public Stream<T> take(int n) {
        if (n < 1 || this.isEmpty()) {
            return Stream.empty();
        }
        if (n == 1) {
            return Stream.cons(this.head(), Stream::empty);
        }
        return Stream.cons(this.head(), () -> this.tail().take(n - 1));
    }

    @Override
    default public Stream<T> takeUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.takeWhile((Predicate)predicate.negate());
    }

    @Override
    default public Stream<T> takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (this.isEmpty()) {
            return Empty.instance();
        }
        Object head = this.head();
        if (predicate.test(head)) {
            return Stream.cons(head, () -> this.tail().takeWhile(predicate));
        }
        return Empty.instance();
    }

    @Override
    default public Stream<T> takeRight(int n) {
        LinearSeq<T> right = this;
        LinearSeq remaining = this.drop(n);
        while (!remaining.isEmpty()) {
            right = right.tail();
            remaining = remaining.tail();
        }
        return right;
    }

    @Override
    default public Stream<T> takeRightUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.reverse().takeUntil((Predicate)predicate).reverse();
    }

    @Override
    default public Stream<T> takeRightWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.takeRightUntil((Predicate)predicate.negate());
    }

    default public <U> U transform(Function<? super Stream<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    default public <T1, T2> Tuple2<Stream<T1>, Stream<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        LinearSeq stream = this.map(unzipper);
        LinearSeq stream1 = stream.map((T t) -> t._1);
        LinearSeq stream2 = stream.map((T t) -> t._2);
        return Tuple.of(stream1, stream2);
    }

    @Override
    default public <T1, T2, T3> Tuple3<Stream<T1>, Stream<T2>, Stream<T3>> unzip3(Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        LinearSeq stream = this.map(unzipper);
        LinearSeq stream1 = stream.map((T t) -> t._1);
        LinearSeq stream2 = stream.map((T t) -> t._2);
        LinearSeq stream3 = stream.map((T t) -> t._3);
        return Tuple.of(stream1, stream2, stream3);
    }

    @Override
    default public Stream<T> update(int index, T element) {
        if (this.isEmpty()) {
            throw new IndexOutOfBoundsException("update(" + index + ", e) on Nil");
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("update(" + index + ", e)");
        }
        LinearSeq preceding = Empty.instance();
        LinearSeq<T> tail = this;
        int i = index;
        while (i > 0) {
            if (tail.isEmpty()) {
                throw new IndexOutOfBoundsException("update at " + index);
            }
            preceding = preceding.prepend(tail.head());
            --i;
            tail = tail.tail();
        }
        if (tail.isEmpty()) {
            throw new IndexOutOfBoundsException("update at " + index);
        }
        return preceding.reverse().appendAll((Iterable)tail.tail().prepend((Object)element));
    }

    @Override
    default public Stream<T> update(int index, Function<? super T, ? extends T> updater) {
        Objects.requireNonNull(updater, "updater is null");
        return this.update(index, (Object)updater.apply(this.get(index)));
    }

    @Override
    default public <U> Stream<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        return this.zipWith((Iterable)that, Tuple::of);
    }

    @Override
    default public <U, R> Stream<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return Stream.ofAll(this.iterator().zipWith(that, mapper));
    }

    @Override
    default public <U> Stream<Tuple2<T, U>> zipAll(Iterable<? extends U> iterable, T thisElem, U thatElem) {
        Objects.requireNonNull(iterable, "iterable is null");
        return Stream.ofAll(this.iterator().zipAll(iterable, thisElem, thatElem));
    }

    @Override
    default public Stream<Tuple2<T, Integer>> zipWithIndex() {
        return this.zipWithIndex(Tuple::of);
    }

    @Override
    default public <U> Stream<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return Stream.ofAll(this.iterator().zipWithIndex(mapper));
    }

    default public Stream<T> extend(T next) {
        return Stream.ofAll(this.appendAll(Stream.continually(next)));
    }

    default public Stream<T> extend(Supplier<? extends T> nextSupplier) {
        Objects.requireNonNull(nextSupplier, "nextSupplier is null");
        return Stream.ofAll(this.appendAll(Stream.continually(nextSupplier)));
    }

    default public Stream<T> extend(final Function<? super T, ? extends T> nextFunction) {
        Objects.requireNonNull(nextFunction, "nextFunction is null");
        if (this.isEmpty()) {
            return this;
        }
        final Stream that = this;
        return Stream.ofAll(new AbstractIterator<T>(){
            Stream<T> stream;
            T last;
            {
                this.stream = that;
                this.last = null;
            }

            @Override
            protected T getNext() {
                if (this.stream.isEmpty()) {
                    this.stream = Stream.iterate(nextFunction.apply(this.last), nextFunction);
                }
                this.last = this.stream.head();
                this.stream = this.stream.tail();
                return this.last;
            }

            @Override
            public boolean hasNext() {
                return true;
            }
        });
    }

    public static abstract class Cons<T>
    implements Stream<T> {
        private static final long serialVersionUID = 1L;
        final T head;
        final Lazy<Stream<T>> tail;

        Cons(T head, Supplier<Stream<T>> tail) {
            Objects.requireNonNull(tail, "tail is null");
            this.head = head;
            this.tail = Lazy.of(tail);
        }

        @Override
        public T head() {
            return this.head;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Iterator<T> iterator() {
            return new StreamModule.StreamIterator(this);
        }

        @Override
        public boolean equals(Object o) {
            return Collections.equals(this, o);
        }

        @Override
        public int hashCode() {
            return Collections.hashOrdered(this);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(this.stringPrefix()).append("(");
            LinearSeq<T> stream = this;
            while (stream != null && !stream.isEmpty()) {
                Cons cons = stream;
                builder.append(cons.head);
                if (cons.tail.isEvaluated()) {
                    if ((stream = stream.tail()).isEmpty()) continue;
                    builder.append(", ");
                    continue;
                }
                builder.append(", ?");
                stream = null;
            }
            return builder.append(")").toString();
        }
    }

    public static final class Empty<T>
    implements Stream<T>,
    Serializable {
        private static final long serialVersionUID = 1L;
        private static final Empty<?> INSTANCE = new Empty();

        private Empty() {
        }

        public static <T> Empty<T> instance() {
            return INSTANCE;
        }

        @Override
        public T head() {
            throw new NoSuchElementException("head of empty stream");
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Iterator<T> iterator() {
            return Iterator.empty();
        }

        @Override
        public Stream<T> tail() {
            throw new UnsupportedOperationException("tail of empty stream");
        }

        @Override
        public boolean equals(Object o) {
            return Collections.equals(this, o);
        }

        @Override
        public int hashCode() {
            return Collections.hashOrdered(this);
        }

        @Override
        public String toString() {
            return this.stringPrefix() + "()";
        }

        private Object readResolve() {
            return INSTANCE;
        }
    }
}

