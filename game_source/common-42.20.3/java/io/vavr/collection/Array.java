/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.PartialFunction;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.AbstractIterator;
import io.vavr.collection.ArrayModule;
import io.vavr.collection.Collections;
import io.vavr.collection.GwtIncompatible;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Iterator;
import io.vavr.collection.JavaConverters;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
import java.util.stream.Stream;

public final class Array<T>
implements IndexedSeq<T>,
Serializable {
    private static final long serialVersionUID = 1L;
    private static final Array<?> EMPTY = new Array(new Object[0]);
    private final Object[] delegate;

    private Array(Object[] delegate) {
        this.delegate = delegate;
    }

    static <T> Array<T> wrap(Object[] array) {
        return array.length == 0 ? Array.empty() : new Array<T>(array);
    }

    public static <T> Collector<T, ArrayList<T>, Array<T>> collector() {
        Supplier<ArrayList> supplier = ArrayList::new;
        BiConsumer<ArrayList, Object> accumulator = ArrayList::add;
        BinaryOperator combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        Function<ArrayList, Array> finisher = Array::ofAll;
        return Collector.of(supplier, accumulator, combiner, finisher, new Collector.Characteristics[0]);
    }

    public static <T> Array<T> empty() {
        return EMPTY;
    }

    public static <T> Array<T> narrow(Array<? extends T> array) {
        return array;
    }

    public static <T> Array<T> of(T element) {
        return Array.wrap(new Object[]{element});
    }

    @SafeVarargs
    public static <T> Array<T> of(T ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Array.wrap(Arrays.copyOf(elements, elements.length));
    }

    public static <T> Array<T> ofAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (elements instanceof Array) {
            return (Array)elements;
        }
        if (elements instanceof JavaConverters.ListView && ((JavaConverters.ListView)elements).getDelegate() instanceof Array) {
            return (Array)((JavaConverters.ListView)elements).getDelegate();
        }
        return Array.wrap(Array.toArray(elements));
    }

    public static <T> Array<T> ofAll(Stream<? extends T> javaStream) {
        Objects.requireNonNull(javaStream, "javaStream is null");
        return Array.wrap(javaStream.toArray());
    }

    public static Array<Boolean> ofAll(boolean ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Array.ofAll(Iterator.ofAll(elements));
    }

    public static Array<Byte> ofAll(byte ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Array.ofAll(Iterator.ofAll(elements));
    }

    public static Array<Character> ofAll(char ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Array.ofAll(Iterator.ofAll(elements));
    }

    public static Array<Double> ofAll(double ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Array.ofAll(Iterator.ofAll(elements));
    }

    public static Array<Float> ofAll(float ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Array.ofAll(Iterator.ofAll(elements));
    }

    public static Array<Integer> ofAll(int ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Array.ofAll(Iterator.ofAll(elements));
    }

    public static Array<Long> ofAll(long ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Array.ofAll(Iterator.ofAll(elements));
    }

    public static Array<Short> ofAll(short ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Array.ofAll(Iterator.ofAll(elements));
    }

    public static <T> Array<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return Collections.tabulate(n, f, Array.empty(), Array::of);
    }

    public static <T> Array<T> fill(int n, Supplier<? extends T> s) {
        Objects.requireNonNull(s, "s is null");
        return Collections.fill(n, s, Array.empty(), Array::of);
    }

    public static <T> Array<T> fill(int n, T element) {
        return Collections.fillObject(n, element, Array.empty(), Array::of);
    }

    public static Array<Character> range(char from, char toExclusive) {
        return Array.ofAll(Iterator.range(from, toExclusive));
    }

    public static Array<Character> rangeBy(char from, char toExclusive, int step) {
        return Array.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    @GwtIncompatible
    public static Array<Double> rangeBy(double from, double toExclusive, double step) {
        return Array.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static Array<Integer> range(int from, int toExclusive) {
        return Array.ofAll(Iterator.range(from, toExclusive));
    }

    public static Array<Integer> rangeBy(int from, int toExclusive, int step) {
        return Array.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static Array<Long> range(long from, long toExclusive) {
        return Array.ofAll(Iterator.range(from, toExclusive));
    }

    public static Array<Long> rangeBy(long from, long toExclusive, long step) {
        return Array.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static Array<Character> rangeClosed(char from, char toInclusive) {
        return Array.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static Array<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return Array.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    @GwtIncompatible
    public static Array<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return Array.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static Array<Integer> rangeClosed(int from, int toInclusive) {
        return Array.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static Array<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return Array.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static Array<Long> rangeClosed(long from, long toInclusive) {
        return Array.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static Array<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return Array.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    static <T> Array<Array<T>> transpose(Array<Array<T>> matrix) {
        return Collections.transpose(matrix, Array::ofAll, Array::of);
    }

    public static <T, U> Array<U> unfoldRight(T seed, Function<? super T, Option<Tuple2<? extends U, ? extends T>>> f) {
        return Iterator.unfoldRight(seed, f).toArray();
    }

    public static <T, U> Array<U> unfoldLeft(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends U>>> f) {
        return Iterator.unfoldLeft(seed, f).toArray();
    }

    public static <T> Array<T> unfold(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends T>>> f) {
        return Iterator.unfold(seed, f).toArray();
    }

    @Override
    public Array<T> append(T element) {
        Object[] copy = Arrays.copyOf(this.delegate, this.delegate.length + 1);
        copy[this.delegate.length] = element;
        return Array.wrap(copy);
    }

    @Override
    public Array<T> appendAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (this.isEmpty() && elements instanceof Array) {
            Array array = (Array)elements;
            return array;
        }
        Object[] source2 = Array.toArray(elements);
        if (source2.length == 0) {
            return this;
        }
        Object[] arr = Arrays.copyOf(this.delegate, this.delegate.length + source2.length);
        System.arraycopy(source2, 0, arr, this.delegate.length, source2.length);
        return Array.wrap(arr);
    }

    @Override
    @GwtIncompatible
    public List<T> asJava() {
        return JavaConverters.asJava(this, JavaConverters.ChangePolicy.IMMUTABLE);
    }

    @Override
    @GwtIncompatible
    public Array<T> asJava(Consumer<? super List<T>> action) {
        return Collections.asJava(this, action, JavaConverters.ChangePolicy.IMMUTABLE);
    }

    @Override
    @GwtIncompatible
    public List<T> asJavaMutable() {
        return JavaConverters.asJava(this, JavaConverters.ChangePolicy.MUTABLE);
    }

    @Override
    @GwtIncompatible
    public Array<T> asJavaMutable(Consumer<? super List<T>> action) {
        return Collections.asJava(this, action, JavaConverters.ChangePolicy.MUTABLE);
    }

    @Override
    public <R> Array<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        return Array.ofAll(this.iterator().collect(partialFunction));
    }

    @Override
    public boolean hasDefiniteSize() {
        return true;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isLazy() {
        return false;
    }

    @Override
    public boolean isTraversableAgain() {
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return new AbstractIterator<T>(){
            private int index = 0;

            @Override
            public boolean hasNext() {
                return this.index < Array.this.delegate.length;
            }

            @Override
            public T getNext() {
                return Array.this.delegate[this.index++];
            }
        };
    }

    @Override
    public Array<Array<T>> combinations() {
        return ((Array)Array.rangeClosed(0, this.length()).map((T n) -> this.combinations((int)n))).flatMap(Function.identity());
    }

    @Override
    public Array<Array<T>> combinations(int k) {
        return ArrayModule.Combinations.apply(this, Math.max(k, 0));
    }

    @Override
    public Iterator<Array<T>> crossProduct(int power) {
        return Collections.crossProduct(Array.empty(), this, power);
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= this.length()) {
            throw new IndexOutOfBoundsException("get(" + index + ")");
        }
        return (T)this.delegate[index];
    }

    @Override
    public Array<T> distinct() {
        return this.distinctBy(Function.identity());
    }

    @Override
    public Array<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        TreeSet<? super T> seen = new TreeSet<T>(comparator);
        return this.filter(seen::add);
    }

    @Override
    public <U> Array<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        HashSet seen = new HashSet();
        return this.filter((T t) -> seen.add(keyExtractor.apply(t)));
    }

    @Override
    public Array<T> drop(int n) {
        if (n <= 0) {
            return this;
        }
        if (n >= this.length()) {
            return Array.empty();
        }
        Object[] arr = new Object[this.delegate.length - n];
        System.arraycopy(this.delegate, n, arr, 0, arr.length);
        return Array.wrap(arr);
    }

    @Override
    public Array<T> dropUntil(Predicate<? super T> predicate) {
        return Collections.dropUntil(this, predicate);
    }

    @Override
    public Array<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.dropUntil((Predicate)predicate.negate());
    }

    @Override
    public Array<T> dropRight(int n) {
        if (n <= 0) {
            return this;
        }
        if (n >= this.length()) {
            return Array.empty();
        }
        return Array.wrap(Arrays.copyOf(this.delegate, this.delegate.length - n));
    }

    @Override
    public Array<T> dropRightUntil(Predicate<? super T> predicate) {
        return Collections.dropRightUntil(this, predicate);
    }

    @Override
    public Array<T> dropRightWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.dropRightUntil((Predicate)predicate.negate());
    }

    @Override
    public Array<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        ArrayList list = new ArrayList();
        for (Object t : this) {
            if (!predicate.test(t)) continue;
            list.add(t);
        }
        if (list.isEmpty()) {
            return Array.empty();
        }
        if (list.size() == this.size()) {
            return this;
        }
        return Array.wrap(list.toArray());
    }

    @Override
    public Array<T> reject(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Collections.reject(this, predicate);
    }

    @Override
    public <U> Array<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (this.isEmpty()) {
            return Array.empty();
        }
        ArrayList<U> list = new ArrayList<U>();
        for (Object t : this) {
            for (U u : mapper.apply(t)) {
                list.add(u);
            }
        }
        return Array.wrap(list.toArray());
    }

    @Override
    public <C> Map<C, Array<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return Collections.groupBy(this, classifier, Array::ofAll);
    }

    @Override
    public Iterator<Array<T>> grouped(int size) {
        return this.sliding(size, size);
    }

    @Override
    public T head() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("head on empty Array");
        }
        return (T)this.delegate[0];
    }

    @Override
    public int indexOf(T element, int from) {
        for (int i = from; i < this.length(); ++i) {
            if (!Objects.equals(this.get(i), element)) continue;
            return i;
        }
        return -1;
    }

    @Override
    public Array<T> init() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("init of empty Array");
        }
        return this.dropRight(1);
    }

    @Override
    public Option<Array<T>> initOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.init());
    }

    @Override
    public boolean isEmpty() {
        return this.delegate.length == 0;
    }

    private Object readResolve() {
        return this.isEmpty() ? EMPTY : this;
    }

    @Override
    public Array<T> insert(int index, T element) {
        if (index < 0 || index > this.length()) {
            throw new IndexOutOfBoundsException("insert(" + index + ", e) on Array of length " + this.length());
        }
        Object[] arr = new Object[this.delegate.length + 1];
        System.arraycopy(this.delegate, 0, arr, 0, index);
        arr[index] = element;
        System.arraycopy(this.delegate, index, arr, index + 1, this.delegate.length - index);
        return Array.wrap(arr);
    }

    @Override
    public Array<T> insertAll(int index, Iterable<? extends T> elements) {
        if (index < 0 || index > this.length()) {
            throw new IndexOutOfBoundsException("insert(" + index + ", e) on Array of length " + this.length());
        }
        if (this.isEmpty() && elements instanceof Array) {
            Array array = (Array)elements;
            return array;
        }
        Object[] list = Array.toArray(elements);
        if (list.length == 0) {
            return this;
        }
        Object[] arr = new Object[this.delegate.length + list.length];
        System.arraycopy(this.delegate, 0, arr, 0, index);
        System.arraycopy(list, 0, arr, index, list.length);
        System.arraycopy(this.delegate, index, arr, index + list.length, this.delegate.length - index);
        return Array.wrap(arr);
    }

    @Override
    public Array<T> intersperse(T element) {
        if (this.delegate.length <= 1) {
            return this;
        }
        Object[] arr = new Object[this.delegate.length * 2 - 1];
        for (int i = 0; i < this.delegate.length; ++i) {
            arr[i * 2] = this.delegate[i];
            if (i <= 0) continue;
            arr[i * 2 - 1] = element;
        }
        return Array.wrap(arr);
    }

    @Override
    public int lastIndexOf(T element, int end) {
        for (int i = Math.min(end, this.length() - 1); i >= 0; --i) {
            if (!Objects.equals(this.get(i), element)) continue;
            return i;
        }
        return -1;
    }

    @Override
    public int length() {
        return this.delegate.length;
    }

    @Override
    public <U> Array<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        Object[] arr = new Object[this.length()];
        for (int i = 0; i < this.delegate.length; ++i) {
            arr[i] = mapper.apply(this.get(i));
        }
        return Array.wrap(arr);
    }

    @Override
    public Array<T> orElse(Iterable<? extends T> other) {
        return this.isEmpty() ? Array.ofAll(other) : this;
    }

    @Override
    public Array<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return this.isEmpty() ? Array.ofAll(supplier.get()) : this;
    }

    @Override
    public Array<T> padTo(int length, T element) {
        int actualLength = this.length();
        if (length <= actualLength) {
            return this;
        }
        return this.appendAll((Iterable)Iterator.continually(element).take(length - actualLength));
    }

    @Override
    public Array<T> leftPadTo(int length, T element) {
        int actualLength = this.length();
        if (length <= actualLength) {
            return this;
        }
        return this.prependAll((Iterable)Iterator.continually(element).take(length - actualLength));
    }

    @Override
    public Array<T> patch(int from, Iterable<? extends T> that, int replaced) {
        from = from < 0 ? 0 : from;
        replaced = replaced < 0 ? 0 : replaced;
        IndexedSeq result = ((Array)this.take(from)).appendAll((Iterable)that);
        result = ((Array)result).appendAll((Iterable)this.drop(from += replaced));
        return result;
    }

    @Override
    public Tuple2<Array<T>, Array<T>> partition(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        ArrayList left = new ArrayList();
        ArrayList right = new ArrayList();
        for (Object t : this) {
            (predicate.test(t) ? left : right).add(t);
        }
        return Tuple.of(Array.ofAll(left), Array.ofAll(right));
    }

    @Override
    public Array<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (!this.isEmpty()) {
            action.accept(this.head());
        }
        return this;
    }

    @Override
    public Array<Array<T>> permutations() {
        if (this.isEmpty()) {
            return Array.empty();
        }
        if (this.delegate.length == 1) {
            return Array.of(this);
        }
        Array<T> zero = Array.empty();
        return this.distinct().foldLeft(zero, (xs, x) -> {
            Function<Array, Array> prepend = l -> l.prepend(x);
            return xs.appendAll((Iterable)((Array)((Array)this.remove(x)).permutations()).map(prepend));
        });
    }

    @Override
    public Array<T> prepend(T element) {
        return this.insert(0, (Object)element);
    }

    @Override
    public Array<T> prependAll(Iterable<? extends T> elements) {
        return this.insertAll(0, (Iterable)elements);
    }

    @Override
    public Array<T> remove(T element) {
        int index = -1;
        for (int i = 0; i < this.length(); ++i) {
            T value = this.get(i);
            if (!Objects.equals(element, value)) continue;
            index = i;
            break;
        }
        if (index < 0) {
            return this;
        }
        return this.removeAt(index);
    }

    @Override
    public Array<T> removeFirst(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        int found = -1;
        for (int i = 0; i < this.length(); ++i) {
            T value = this.get(i);
            if (!predicate.test(value)) continue;
            found = i;
            break;
        }
        if (found < 0) {
            return this;
        }
        return this.removeAt(found);
    }

    @Override
    public Array<T> removeLast(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        int found = -1;
        for (int i = this.length() - 1; i >= 0; --i) {
            T value = this.get(i);
            if (!predicate.test(value)) continue;
            found = i;
            break;
        }
        if (found < 0) {
            return this;
        }
        return this.removeAt(found);
    }

    @Override
    public Array<T> removeAt(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("removeAt(" + index + ")");
        }
        if (index >= this.length()) {
            throw new IndexOutOfBoundsException("removeAt(" + index + ")");
        }
        Object[] arr = new Object[this.length() - 1];
        System.arraycopy(this.delegate, 0, arr, 0, index);
        System.arraycopy(this.delegate, index + 1, arr, index, this.length() - index - 1);
        return Array.wrap(arr);
    }

    @Override
    public Array<T> removeAll(T element) {
        return Collections.removeAll(this, element);
    }

    @Override
    public Array<T> removeAll(Iterable<? extends T> elements) {
        return Collections.removeAll(this, elements);
    }

    @Override
    @Deprecated
    public Array<T> removeAll(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.reject((Predicate)predicate);
    }

    @Override
    public Array<T> replace(T currentElement, T newElement) {
        Object[] arr = new Object[this.length()];
        boolean found = false;
        for (int i = 0; i < this.length(); ++i) {
            T value = this.get(i);
            if (found) {
                arr[i] = this.delegate[i];
                continue;
            }
            if (Objects.equals(currentElement, value)) {
                arr[i] = newElement;
                found = true;
                continue;
            }
            arr[i] = this.delegate[i];
        }
        return found ? Array.wrap(arr) : this;
    }

    @Override
    public Array<T> replaceAll(T currentElement, T newElement) {
        Object[] arr = new Object[this.length()];
        boolean changed = false;
        for (int i = 0; i < this.length(); ++i) {
            T value = this.get(i);
            if (Objects.equals(currentElement, value)) {
                arr[i] = newElement;
                changed = true;
                continue;
            }
            arr[i] = this.delegate[i];
        }
        return changed ? Array.wrap(arr) : this;
    }

    @Override
    public Array<T> retainAll(Iterable<? extends T> elements) {
        return Collections.retainAll(this, elements);
    }

    @Override
    public Array<T> reverse() {
        if (this.size() <= 1) {
            return this;
        }
        int length = this.delegate.length;
        Object[] arr = new Object[length];
        int i = 0;
        int j = length - 1;
        while (i < length) {
            arr[j] = this.delegate[i];
            ++i;
            --j;
        }
        return Array.wrap(arr);
    }

    @Override
    public Array<T> rotateLeft(int n) {
        return Collections.rotateLeft(this, n);
    }

    @Override
    public Array<T> rotateRight(int n) {
        return Collections.rotateRight(this, n);
    }

    @Override
    public Array<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        return this.scanLeft(zero, operation);
    }

    @Override
    public <U> Array<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        return Collections.scanLeft(this, zero, operation, Array::ofAll);
    }

    @Override
    public <U> Array<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        return Collections.scanRight(this, zero, operation, Array::ofAll);
    }

    @Override
    public Array<T> shuffle() {
        return Collections.shuffle(this, Array::ofAll);
    }

    @Override
    public Array<T> slice(int beginIndex, int endIndex) {
        if (beginIndex >= endIndex || beginIndex >= this.length() || this.isEmpty()) {
            return Array.empty();
        }
        if (beginIndex <= 0 && endIndex >= this.length()) {
            return this;
        }
        int index = Math.max(beginIndex, 0);
        int length = Math.min(endIndex, this.length()) - index;
        Object[] arr = new Object[length];
        System.arraycopy(this.delegate, index, arr, 0, length);
        return Array.wrap(arr);
    }

    @Override
    public Iterator<Array<T>> slideBy(Function<? super T, ?> classifier) {
        return this.iterator().slideBy(classifier).map(Array::ofAll);
    }

    @Override
    public Iterator<Array<T>> sliding(int size) {
        return this.sliding(size, 1);
    }

    @Override
    public Iterator<Array<T>> sliding(int size, int step) {
        return this.iterator().sliding(size, step).map(Array::ofAll);
    }

    @Override
    public Array<T> sorted() {
        Object[] arr = Arrays.copyOf(this.delegate, this.delegate.length);
        Arrays.sort(arr);
        return Array.wrap(arr);
    }

    @Override
    public Array<T> sorted(Comparator<? super T> comparator) {
        Object[] arr = Arrays.copyOf(this.delegate, this.delegate.length);
        Arrays.sort(arr, (o1, o2) -> comparator.compare(o1, o2));
        return Array.wrap(arr);
    }

    @Override
    public <U extends Comparable<? super U>> Array<T> sortBy(Function<? super T, ? extends U> mapper) {
        return this.sortBy(Comparable::compareTo, (Function)mapper);
    }

    @Override
    public <U> Array<T> sortBy(Comparator<? super U> comparator, Function<? super T, ? extends U> mapper) {
        return Collections.sortBy(this, comparator, mapper, Array.collector());
    }

    @Override
    public Tuple2<Array<T>, Array<T>> splitAt(int n) {
        return Tuple.of(this.take(n), this.drop(n));
    }

    @Override
    public Tuple2<Array<T>, Array<T>> splitAt(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        IndexedSeq init = this.takeWhile((Predicate)predicate.negate());
        return Tuple.of(init, this.drop(((Array)init).length()));
    }

    @Override
    public Tuple2<Array<T>, Array<T>> splitAtInclusive(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        for (int i = 0; i < this.delegate.length; ++i) {
            T value = this.get(i);
            if (!predicate.test(value)) continue;
            if (i == this.delegate.length - 1) {
                return Tuple.of(this, Array.empty());
            }
            return Tuple.of(this.take(i + 1), this.drop(i + 1));
        }
        return Tuple.of(this, Array.empty());
    }

    @Override
    public Tuple2<Array<T>, Array<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Tuple.of(this.takeWhile((Predicate)predicate), this.dropWhile((Predicate)predicate));
    }

    @Override
    public Array<T> subSequence(int beginIndex) {
        if (beginIndex < 0 || beginIndex > this.length()) {
            throw new IndexOutOfBoundsException("subSequence(" + beginIndex + ")");
        }
        return this.drop(beginIndex);
    }

    @Override
    public Array<T> subSequence(int beginIndex, int endIndex) {
        Collections.subSequenceRangeCheck(beginIndex, endIndex, this.length());
        if (beginIndex == endIndex) {
            return Array.empty();
        }
        if (beginIndex == 0 && endIndex == this.length()) {
            return this;
        }
        Object[] arr = new Object[endIndex - beginIndex];
        System.arraycopy(this.delegate, beginIndex, arr, 0, arr.length);
        return Array.wrap(arr);
    }

    @Override
    public Array<T> tail() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("tail() on empty Array");
        }
        Object[] arr = new Object[this.delegate.length - 1];
        System.arraycopy(this.delegate, 1, arr, 0, arr.length);
        return Array.wrap(arr);
    }

    @Override
    public Option<Array<T>> tailOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.tail());
    }

    @Override
    public Array<T> take(int n) {
        if (n >= this.length()) {
            return this;
        }
        if (n <= 0) {
            return Array.empty();
        }
        return Array.wrap(Arrays.copyOf(this.delegate, n));
    }

    @Override
    public Array<T> takeUntil(Predicate<? super T> predicate) {
        return Collections.takeUntil(this, predicate);
    }

    @Override
    public Array<T> takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.takeUntil((Predicate)predicate.negate());
    }

    @Override
    public Array<T> takeRight(int n) {
        if (n >= this.length()) {
            return this;
        }
        if (n <= 0) {
            return Array.empty();
        }
        Object[] arr = new Object[n];
        System.arraycopy(this.delegate, this.delegate.length - n, arr, 0, n);
        return Array.wrap(arr);
    }

    @Override
    public Array<T> takeRightUntil(Predicate<? super T> predicate) {
        return Collections.takeRightUntil(this, predicate);
    }

    @Override
    public Array<T> takeRightWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.takeRightUntil((Predicate)predicate.negate());
    }

    public <U> U transform(Function<? super Array<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    public <T1, T2> Tuple2<Array<T1>, Array<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        if (this.isEmpty()) {
            return Tuple.of(Array.empty(), Array.empty());
        }
        Object[] xs = new Object[this.delegate.length];
        Object[] ys = new Object[this.delegate.length];
        for (int i = 0; i < this.delegate.length; ++i) {
            Tuple2<? extends T1, ? extends T2> t = unzipper.apply(this.get(i));
            xs[i] = t._1;
            ys[i] = t._2;
        }
        return Tuple.of(Array.wrap(xs), Array.wrap(ys));
    }

    @Override
    public <T1, T2, T3> Tuple3<Array<T1>, Array<T2>, Array<T3>> unzip3(Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        if (this.isEmpty()) {
            return Tuple.of(Array.empty(), Array.empty(), Array.empty());
        }
        Object[] xs = new Object[this.delegate.length];
        Object[] ys = new Object[this.delegate.length];
        Object[] zs = new Object[this.delegate.length];
        for (int i = 0; i < this.delegate.length; ++i) {
            Tuple3<? extends T1, ? extends T2, ? extends T3> t = unzipper.apply(this.get(i));
            xs[i] = t._1;
            ys[i] = t._2;
            zs[i] = t._3;
        }
        return Tuple.of(Array.wrap(xs), Array.wrap(ys), Array.wrap(zs));
    }

    @Override
    public Array<T> update(int index, T element) {
        if (index < 0 || index >= this.length()) {
            throw new IndexOutOfBoundsException("update(" + index + ")");
        }
        Object[] arr = Arrays.copyOf(this.delegate, this.delegate.length);
        arr[index] = element;
        return Array.wrap(arr);
    }

    @Override
    public Array<T> update(int index, Function<? super T, ? extends T> updater) {
        Objects.requireNonNull(updater, "updater is null");
        return this.update(index, (Object)updater.apply(this.get(index)));
    }

    @Override
    public <U> Array<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        return this.zipWith((Iterable)that, Tuple::of);
    }

    @Override
    public <U, R> Array<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return Array.ofAll(this.iterator().zipWith(that, mapper));
    }

    @Override
    public <U> Array<Tuple2<T, U>> zipAll(Iterable<? extends U> that, T thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        return Array.ofAll(this.iterator().zipAll(that, thisElem, thatElem));
    }

    @Override
    public Array<Tuple2<T, Integer>> zipWithIndex() {
        return Array.ofAll(this.iterator().zipWithIndex());
    }

    @Override
    public <U> Array<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return Array.ofAll(this.iterator().zipWithIndex(mapper));
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
    public String stringPrefix() {
        return "Array";
    }

    @Override
    public String toString() {
        return this.mkString(this.stringPrefix() + "(", ", ", ")");
    }

    private static <T> Object[] toArray(Iterable<T> elements) {
        if (elements instanceof Array) {
            Array array = (Array)elements;
            return array.delegate;
        }
        return Collections.withSize(elements).toArray();
    }
}

