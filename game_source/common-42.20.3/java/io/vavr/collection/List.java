/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.PartialFunction;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Value;
import io.vavr.collection.Collections;
import io.vavr.collection.GwtIncompatible;
import io.vavr.collection.Iterator;
import io.vavr.collection.JavaConverters;
import io.vavr.collection.LinearSeq;
import io.vavr.collection.ListModule;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.NavigableSet;
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

public interface List<T>
extends LinearSeq<T> {
    public static final long serialVersionUID = 1L;

    public static <T> Collector<T, ArrayList<T>, List<T>> collector() {
        Supplier<ArrayList> supplier = ArrayList::new;
        BiConsumer<ArrayList, Object> accumulator = ArrayList::add;
        BinaryOperator combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        Function<ArrayList, List> finisher = List::ofAll;
        return Collector.of(supplier, accumulator, combiner, finisher, new Collector.Characteristics[0]);
    }

    public static <T> List<T> empty() {
        return Nil.instance();
    }

    @Override
    default public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isEmpty();

    @Override
    default public boolean isLazy() {
        return false;
    }

    public static <T> List<T> narrow(List<? extends T> list) {
        return list;
    }

    public static <T> List<T> of(T element) {
        return new Cons(element, Nil.instance());
    }

    @SafeVarargs
    public static <T> List<T> of(T ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        LinearSeq result = Nil.instance();
        for (int i = elements.length - 1; i >= 0; --i) {
            result = result.prepend((Object)elements[i]);
        }
        return result;
    }

    public static <T> List<T> ofAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (elements instanceof List) {
            return (List)elements;
        }
        if (elements instanceof JavaConverters.ListView && ((JavaConverters.ListView)elements).getDelegate() instanceof List) {
            return (List)((JavaConverters.ListView)elements).getDelegate();
        }
        if (elements instanceof java.util.List) {
            LinearSeq result = Nil.instance();
            java.util.List list = (java.util.List)elements;
            ListIterator iterator2 = list.listIterator(list.size());
            while (iterator2.hasPrevious()) {
                result = result.prepend(iterator2.previous());
            }
            return result;
        }
        if (elements instanceof NavigableSet) {
            LinearSeq result = Nil.instance();
            java.util.Iterator iterator3 = ((NavigableSet)elements).descendingIterator();
            while (iterator3.hasNext()) {
                result = result.prepend(iterator3.next());
            }
            return result;
        }
        LinearSeq result = Nil.instance();
        for (T element : elements) {
            result = result.prepend((Object)element);
        }
        return result.reverse();
    }

    public static <T> List<T> ofAll(Stream<? extends T> javaStream) {
        Objects.requireNonNull(javaStream, "javaStream is null");
        java.util.Iterator iterator2 = javaStream.iterator();
        LinearSeq<T> list = List.empty();
        while (iterator2.hasNext()) {
            list = list.prepend(iterator2.next());
        }
        return list.reverse();
    }

    public static List<Boolean> ofAll(boolean ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return List.ofAll(Iterator.ofAll(elements));
    }

    public static List<Byte> ofAll(byte ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return List.ofAll(Iterator.ofAll(elements));
    }

    public static List<Character> ofAll(char ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return List.ofAll(Iterator.ofAll(elements));
    }

    public static List<Double> ofAll(double ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return List.ofAll(Iterator.ofAll(elements));
    }

    public static List<Float> ofAll(float ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return List.ofAll(Iterator.ofAll(elements));
    }

    public static List<Integer> ofAll(int ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return List.ofAll(Iterator.ofAll(elements));
    }

    public static List<Long> ofAll(long ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return List.ofAll(Iterator.ofAll(elements));
    }

    public static List<Short> ofAll(short ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return List.ofAll(Iterator.ofAll(elements));
    }

    public static <T> List<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return Collections.tabulate(n, f, List.empty(), List::of);
    }

    public static <T> List<T> fill(int n, Supplier<? extends T> s) {
        Objects.requireNonNull(s, "s is null");
        return Collections.fill(n, s, List.empty(), List::of);
    }

    public static <T> List<T> fill(int n, T element) {
        return Collections.fillObject(n, element, List.empty(), List::of);
    }

    public static List<Character> range(char from, char toExclusive) {
        return List.ofAll(Iterator.range(from, toExclusive));
    }

    public static List<Character> rangeBy(char from, char toExclusive, int step) {
        return List.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    @GwtIncompatible
    public static List<Double> rangeBy(double from, double toExclusive, double step) {
        return List.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static List<Integer> range(int from, int toExclusive) {
        return List.ofAll(Iterator.range(from, toExclusive));
    }

    public static List<Integer> rangeBy(int from, int toExclusive, int step) {
        return List.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static List<Long> range(long from, long toExclusive) {
        return List.ofAll(Iterator.range(from, toExclusive));
    }

    public static List<Long> rangeBy(long from, long toExclusive, long step) {
        return List.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static List<Character> rangeClosed(char from, char toInclusive) {
        return List.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static List<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return List.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    @GwtIncompatible
    public static List<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return List.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static List<Integer> rangeClosed(int from, int toInclusive) {
        return List.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static List<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return List.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static List<Long> rangeClosed(long from, long toInclusive) {
        return List.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static List<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return List.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static <T> List<List<T>> transpose(List<List<T>> matrix) {
        return Collections.transpose(matrix, List::ofAll, List::of);
    }

    public static <T, U> List<U> unfoldRight(T seed, Function<? super T, Option<Tuple2<? extends U, ? extends T>>> f) {
        return Iterator.unfoldRight(seed, f).toList();
    }

    public static <T, U> List<U> unfoldLeft(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends U>>> f) {
        return Iterator.unfoldLeft(seed, f).toList();
    }

    public static <T> List<T> unfold(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends T>>> f) {
        return Iterator.unfold(seed, f).toList();
    }

    @Override
    default public List<T> append(T element) {
        return this.foldRight(List.of(element), (x, xs) -> xs.prepend(x));
    }

    @Override
    default public List<T> appendAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        return List.ofAll(elements).prependAll((Iterable)this);
    }

    @Override
    @GwtIncompatible
    default public java.util.List<T> asJava() {
        return JavaConverters.asJava(this, JavaConverters.ChangePolicy.IMMUTABLE);
    }

    @Override
    @GwtIncompatible
    default public List<T> asJava(Consumer<? super java.util.List<T>> action) {
        return Collections.asJava(this, action, JavaConverters.ChangePolicy.IMMUTABLE);
    }

    @Override
    @GwtIncompatible
    default public java.util.List<T> asJavaMutable() {
        return JavaConverters.asJava(this, JavaConverters.ChangePolicy.MUTABLE);
    }

    @Override
    @GwtIncompatible
    default public List<T> asJavaMutable(Consumer<? super java.util.List<T>> action) {
        return Collections.asJava(this, action, JavaConverters.ChangePolicy.MUTABLE);
    }

    @Override
    default public <R> List<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        return List.ofAll(this.iterator().collect(partialFunction));
    }

    @Override
    default public List<List<T>> combinations() {
        return List.rangeClosed(0, this.length()).map((T n) -> this.combinations((int)n)).flatMap(Function.identity());
    }

    @Override
    default public List<List<T>> combinations(int k) {
        return ListModule.Combinations.apply(this, Math.max(k, 0));
    }

    @Override
    default public Iterator<List<T>> crossProduct(int power) {
        return Collections.crossProduct(List.empty(), this, power);
    }

    @Override
    default public List<T> distinct() {
        return this.distinctBy(Function.identity());
    }

    @Override
    default public List<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        TreeSet<? super T> seen = new TreeSet<T>(comparator);
        return this.filter(seen::add);
    }

    @Override
    default public <U> List<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        HashSet seen = new HashSet();
        return this.filter((T t) -> seen.add(keyExtractor.apply(t)));
    }

    @Override
    default public List<T> drop(int n) {
        if (n <= 0) {
            return this;
        }
        if (n >= this.size()) {
            return List.empty();
        }
        LinearSeq<T> list = this;
        for (long i = (long)n; i > 0L && !list.isEmpty(); --i) {
            list = list.tail();
        }
        return list;
    }

    @Override
    default public List<T> dropUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.dropWhile((Predicate)predicate.negate());
    }

    @Override
    default public List<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        LinearSeq<T> list = this;
        while (!list.isEmpty() && predicate.test(list.head())) {
            list = list.tail();
        }
        return list;
    }

    @Override
    default public List<T> dropRight(int n) {
        if (n <= 0) {
            return this;
        }
        if (n >= this.length()) {
            return List.empty();
        }
        return List.ofAll(this.iterator().dropRight(n));
    }

    @Override
    default public List<T> dropRightUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.reverse().dropUntil((Predicate)predicate).reverse();
    }

    @Override
    default public List<T> dropRightWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.dropRightUntil((Predicate)predicate.negate());
    }

    @Override
    default public List<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (this.isEmpty()) {
            return this;
        }
        List filtered = this.foldLeft(List.empty(), (xs, x) -> predicate.test(x) ? xs.prepend(x) : xs);
        if (filtered.isEmpty()) {
            return List.empty();
        }
        if (filtered.length() == this.length()) {
            return this;
        }
        return filtered.reverse();
    }

    @Override
    default public List<T> reject(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Collections.reject(this, predicate);
    }

    @Override
    default public <U> List<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        LinearSeq<T> list = List.empty();
        for (Object t : this) {
            for (U u : mapper.apply(t)) {
                list = list.prepend(u);
            }
        }
        return list.reverse();
    }

    @Override
    default public T get(int index) {
        if (this.isEmpty()) {
            throw new IndexOutOfBoundsException("get(" + index + ") on Nil");
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("get(" + index + ")");
        }
        LinearSeq<T> list = this;
        for (int i = index - 1; i >= 0; --i) {
            if (!(list = list.tail()).isEmpty()) continue;
            throw new IndexOutOfBoundsException("get(" + index + ") on List of length " + (index - i));
        }
        return list.head();
    }

    @Override
    default public <C> Map<C, List<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return Collections.groupBy(this, classifier, List::ofAll);
    }

    @Override
    default public Iterator<List<T>> grouped(int size) {
        return this.sliding(size, size);
    }

    @Override
    default public boolean hasDefiniteSize() {
        return true;
    }

    @Override
    default public int indexOf(T element, int from) {
        int index = 0;
        LinearSeq<T> list = this;
        while (!list.isEmpty()) {
            if (index >= from && Objects.equals(list.head(), element)) {
                return index;
            }
            list = list.tail();
            ++index;
        }
        return -1;
    }

    @Override
    default public List<T> init() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("init of empty list");
        }
        return this.dropRight(1);
    }

    @Override
    default public Option<List<T>> initOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.init());
    }

    @Override
    public int length();

    @Override
    default public List<T> insert(int index, T element) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("insert(" + index + ", e)");
        }
        LinearSeq preceding = Nil.instance();
        LinearSeq<T> tail = this;
        int i = index;
        while (i > 0) {
            if (tail.isEmpty()) {
                throw new IndexOutOfBoundsException("insert(" + index + ", e) on List of length " + this.length());
            }
            preceding = preceding.prepend(tail.head());
            --i;
            tail = tail.tail();
        }
        LinearSeq result = tail.prepend((Object)element);
        for (Object next : preceding) {
            result = result.prepend(next);
        }
        return result;
    }

    @Override
    default public List<T> insertAll(int index, Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (index < 0) {
            throw new IndexOutOfBoundsException("insertAll(" + index + ", elements)");
        }
        LinearSeq preceding = Nil.instance();
        LinearSeq<T> tail = this;
        int i = index;
        while (i > 0) {
            if (tail.isEmpty()) {
                throw new IndexOutOfBoundsException("insertAll(" + index + ", elements) on List of length " + this.length());
            }
            preceding = preceding.prepend(tail.head());
            --i;
            tail = tail.tail();
        }
        LinearSeq result = tail.prependAll((Iterable)elements);
        for (Object next : preceding) {
            result = result.prepend(next);
        }
        return result;
    }

    @Override
    default public List<T> intersperse(T element) {
        return List.ofAll(this.iterator().intersperse(element));
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
        LinearSeq<T> list = this;
        for (int index = 0; index <= end && !list.isEmpty(); ++index) {
            if (Objects.equals(list.head(), element)) {
                result = index;
            }
            list = list.tail();
        }
        return result;
    }

    @Override
    default public <U> List<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        LinearSeq<T> list = List.empty();
        for (Object t : this) {
            list = list.prepend(mapper.apply(t));
        }
        return list.reverse();
    }

    @Override
    default public List<T> orElse(Iterable<? extends T> other) {
        return this.isEmpty() ? List.ofAll(other) : this;
    }

    @Override
    default public List<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return this.isEmpty() ? List.ofAll(supplier.get()) : this;
    }

    @Override
    default public List<T> padTo(int length, T element) {
        int actualLength = this.length();
        if (length <= actualLength) {
            return this;
        }
        return this.appendAll((Iterable)Iterator.continually(element).take(length - actualLength));
    }

    @Override
    default public List<T> leftPadTo(int length, T element) {
        int actualLength = this.length();
        if (length <= actualLength) {
            return this;
        }
        return this.prependAll((Iterable)Iterator.continually(element).take(length - actualLength));
    }

    @Override
    default public List<T> patch(int from, Iterable<? extends T> that, int replaced) {
        from = from < 0 ? 0 : from;
        replaced = replaced < 0 ? 0 : replaced;
        LinearSeq result = this.take(from).appendAll((Iterable)that);
        result = result.appendAll((Iterable)this.drop(from += replaced));
        return result;
    }

    @Override
    default public Tuple2<List<T>, List<T>> partition(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        LinearSeq<T> left = List.empty();
        LinearSeq<T> right = List.empty();
        for (Object t : this) {
            if (predicate.test(t)) {
                left = left.prepend(t);
                continue;
            }
            right = right.prepend(t);
        }
        return Tuple.of(left.reverse(), right.reverse());
    }

    default public T peek() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("peek of empty list");
        }
        return this.head();
    }

    default public Option<T> peekOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.head());
    }

    @Override
    default public List<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (!this.isEmpty()) {
            action.accept(this.head());
        }
        return this;
    }

    @Override
    default public List<List<T>> permutations() {
        if (this.isEmpty()) {
            return Nil.instance();
        }
        LinearSeq tail = this.tail();
        if (tail.isEmpty()) {
            return List.of(this);
        }
        Nil zero = Nil.instance();
        return this.distinct().foldLeft(zero, (xs, x) -> {
            Function<List, List> prepend = l -> l.prepend(x);
            return xs.appendAll((Iterable)this.remove(x).permutations().map(prepend));
        });
    }

    default public List<T> pop() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("pop of empty list");
        }
        return this.tail();
    }

    default public Option<List<T>> popOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.pop());
    }

    default public Tuple2<T, List<T>> pop2() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("pop2 of empty list");
        }
        return Tuple.of(this.head(), this.tail());
    }

    default public Option<Tuple2<T, List<T>>> pop2Option() {
        return this.isEmpty() ? Option.none() : Option.some(Tuple.of(this.head(), this.pop()));
    }

    @Override
    default public List<T> prepend(T element) {
        return new Cons(element, this);
    }

    /*
     * Exception decompiling
     */
    @Override
    default public List<T> prependAll(Iterable<? extends T> elements) {
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

    default public List<T> push(T element) {
        return new Cons(element, this);
    }

    default public List<T> push(T ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        LinearSeq<T> result = this;
        for (T element : elements) {
            result = result.prepend((Object)element);
        }
        return result;
    }

    default public List<T> pushAll(Iterable<T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        LinearSeq<T> result = this;
        for (T element : elements) {
            result = result.prepend((Object)element);
        }
        return result;
    }

    @Override
    default public List<T> remove(T element) {
        ArrayDeque preceding = new ArrayDeque(this.size());
        LinearSeq<T> result = this;
        boolean found = false;
        while (!found && !result.isEmpty()) {
            Object head = result.head();
            if (Objects.equals(head, element)) {
                found = true;
            } else {
                preceding.addFirst(head);
            }
            result = result.tail();
        }
        if (!found) {
            return this;
        }
        for (Object next : preceding) {
            result = result.prepend(next);
        }
        return result;
    }

    /*
     * Exception decompiling
     */
    @Override
    default public List<T> removeFirst(Predicate<T> predicate) {
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
    default public List<T> removeLast(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        LinearSeq removedAndReversed = this.reverse().removeFirst((Predicate)predicate);
        return removedAndReversed.length() == this.length() ? this : removedAndReversed.reverse();
    }

    @Override
    default public List<T> removeAt(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("removeAt(" + index + ")");
        }
        if (this.isEmpty()) {
            throw new IndexOutOfBoundsException("removeAt(" + index + ") on Nil");
        }
        LinearSeq init = Nil.instance();
        LinearSeq<T> tail = this;
        while (index > 0 && !tail.isEmpty()) {
            init = init.prepend(tail.head());
            tail = tail.tail();
            --index;
        }
        if (index > 0) {
            throw new IndexOutOfBoundsException("removeAt() on Nil");
        }
        return init.reverse().appendAll((Iterable)tail.tail());
    }

    @Override
    default public List<T> removeAll(T element) {
        return Collections.removeAll(this, element);
    }

    @Override
    default public List<T> removeAll(Iterable<? extends T> elements) {
        return Collections.removeAll(this, elements);
    }

    @Override
    @Deprecated
    default public List<T> removeAll(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.reject((Predicate)predicate);
    }

    @Override
    default public List<T> replace(T currentElement, T newElement) {
        LinearSeq preceding = Nil.instance();
        LinearSeq<T> tail = this;
        while (!tail.isEmpty() && !Objects.equals(tail.head(), currentElement)) {
            preceding = preceding.prepend(tail.head());
            tail = tail.tail();
        }
        if (tail.isEmpty()) {
            return this;
        }
        LinearSeq result = tail.tail().prepend((Object)newElement);
        for (Object next : preceding) {
            result = result.prepend(next);
        }
        return result;
    }

    @Override
    default public List<T> replaceAll(T currentElement, T newElement) {
        LinearSeq result = Nil.instance();
        boolean changed = false;
        LinearSeq<T> list = this;
        while (!list.isEmpty()) {
            Object head = list.head();
            if (Objects.equals(head, currentElement)) {
                result = result.prepend((Object)newElement);
                changed = true;
            } else {
                result = result.prepend(head);
            }
            list = list.tail();
        }
        return changed ? result.reverse() : this;
    }

    @Override
    default public List<T> retainAll(Iterable<? extends T> elements) {
        return Collections.retainAll(this, elements);
    }

    /*
     * Exception decompiling
     */
    @Override
    default public List<T> reverse() {
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
    default public List<T> rotateLeft(int n) {
        return Collections.rotateLeft(this, n);
    }

    @Override
    default public List<T> rotateRight(int n) {
        return Collections.rotateRight(this, n);
    }

    @Override
    default public List<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        return this.scanLeft(zero, operation);
    }

    @Override
    default public <U> List<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        return Collections.scanLeft(this, zero, operation, Value::toList);
    }

    @Override
    default public <U> List<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        return Collections.scanRight(this, zero, operation, Value::toList);
    }

    @Override
    default public List<T> shuffle() {
        return Collections.shuffle(this, List::ofAll);
    }

    @Override
    default public List<T> slice(int beginIndex, int endIndex) {
        if (beginIndex >= endIndex || beginIndex >= this.length() || this.isEmpty()) {
            return List.empty();
        }
        LinearSeq result = Nil.instance();
        LinearSeq<T> list = this;
        long lowerBound = Math.max(beginIndex, 0);
        long upperBound = Math.min(endIndex, this.length());
        int i = 0;
        while ((long)i < upperBound) {
            if ((long)i >= lowerBound) {
                result = result.prepend(list.head());
            }
            list = list.tail();
            ++i;
        }
        return result.reverse();
    }

    @Override
    default public Iterator<List<T>> slideBy(Function<? super T, ?> classifier) {
        return this.iterator().slideBy(classifier).map(List::ofAll);
    }

    @Override
    default public Iterator<List<T>> sliding(int size) {
        return this.sliding(size, 1);
    }

    @Override
    default public Iterator<List<T>> sliding(int size, int step) {
        return this.iterator().sliding(size, step).map(List::ofAll);
    }

    @Override
    default public List<T> sorted() {
        return this.isEmpty() ? this : this.toJavaStream().sorted().collect(List.collector());
    }

    @Override
    default public List<T> sorted(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return this.isEmpty() ? this : this.toJavaStream().sorted(comparator).collect(List.collector());
    }

    @Override
    default public <U extends Comparable<? super U>> List<T> sortBy(Function<? super T, ? extends U> mapper) {
        return this.sortBy(Comparable::compareTo, (Function)mapper);
    }

    @Override
    default public <U> List<T> sortBy(Comparator<? super U> comparator, Function<? super T, ? extends U> mapper) {
        return Collections.sortBy(this, comparator, mapper, List.collector());
    }

    @Override
    default public Tuple2<List<T>, List<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        Tuple2<Iterator<? super T>, Iterator<? super T>> itt = this.iterator().span(predicate);
        return Tuple.of(List.ofAll((Iterable)itt._1), List.ofAll((Iterable)itt._2));
    }

    @Override
    default public Tuple2<List<T>, List<T>> splitAt(int n) {
        if (this.isEmpty()) {
            return Tuple.of(List.empty(), List.empty());
        }
        LinearSeq init = Nil.instance();
        LinearSeq<T> tail = this;
        while (n > 0 && !tail.isEmpty()) {
            init = init.prepend(tail.head());
            tail = tail.tail();
            --n;
        }
        return Tuple.of(init.reverse(), tail);
    }

    @Override
    default public Tuple2<List<T>, List<T>> splitAt(Predicate<? super T> predicate) {
        if (this.isEmpty()) {
            return Tuple.of(List.empty(), List.empty());
        }
        Tuple2<List<? super T>, List<? super T>> t = ListModule.SplitAt.splitByPredicateReversed(this, predicate);
        if (((List)t._2).isEmpty()) {
            return Tuple.of(this, List.empty());
        }
        return Tuple.of(((List)t._1).reverse(), t._2);
    }

    @Override
    default public Tuple2<List<T>, List<T>> splitAtInclusive(Predicate<? super T> predicate) {
        if (this.isEmpty()) {
            return Tuple.of(List.empty(), List.empty());
        }
        Tuple2<List<? super T>, List<? super T>> t = ListModule.SplitAt.splitByPredicateReversed(this, predicate);
        if (((List)t._2).isEmpty() || ((List)t._2).tail().isEmpty()) {
            return Tuple.of(this, List.empty());
        }
        return Tuple.of(((List)t._1).prepend(((List)t._2).head()).reverse(), ((List)t._2).tail());
    }

    @Override
    default public String stringPrefix() {
        return "List";
    }

    @Override
    default public List<T> subSequence(int beginIndex) {
        if (beginIndex < 0 || beginIndex > this.length()) {
            throw new IndexOutOfBoundsException("subSequence(" + beginIndex + ")");
        }
        return this.drop(beginIndex);
    }

    @Override
    default public List<T> subSequence(int beginIndex, int endIndex) {
        Collections.subSequenceRangeCheck(beginIndex, endIndex, this.length());
        if (beginIndex == endIndex) {
            return List.empty();
        }
        if (beginIndex == 0 && endIndex == this.length()) {
            return this;
        }
        LinearSeq result = Nil.instance();
        LinearSeq<T> list = this;
        int i = 0;
        while (i < endIndex) {
            if (i >= beginIndex) {
                result = result.prepend(list.head());
            }
            ++i;
            list = list.tail();
        }
        return result.reverse();
    }

    @Override
    public List<T> tail();

    @Override
    default public Option<List<T>> tailOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.tail());
    }

    @Override
    default public List<T> take(int n) {
        if (n <= 0) {
            return List.empty();
        }
        if (n >= this.length()) {
            return this;
        }
        LinearSeq result = Nil.instance();
        LinearSeq<T> list = this;
        int i = 0;
        while (i < n) {
            result = result.prepend(list.head());
            ++i;
            list = list.tail();
        }
        return result.reverse();
    }

    @Override
    default public List<T> takeUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.takeWhile((Predicate)predicate.negate());
    }

    @Override
    default public List<T> takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        LinearSeq result = Nil.instance();
        LinearSeq<T> list = this;
        while (!list.isEmpty() && predicate.test(list.head())) {
            result = result.prepend(list.head());
            list = list.tail();
        }
        return result.length() == this.length() ? this : result.reverse();
    }

    @Override
    default public List<T> takeRight(int n) {
        if (n <= 0) {
            return List.empty();
        }
        if (n >= this.length()) {
            return this;
        }
        return this.reverse().take(n).reverse();
    }

    @Override
    default public List<T> takeRightUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.takeRightWhile((Predicate)predicate.negate());
    }

    @Override
    default public List<T> takeRightWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.reverse().takeWhile((Predicate)predicate).reverse();
    }

    default public <U> U transform(Function<? super List<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    default public <T1, T2> Tuple2<List<T1>, List<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        LinearSeq xs = Nil.instance();
        LinearSeq ys = Nil.instance();
        for (Object element : this) {
            Tuple2<? extends T1, ? extends T2> t = unzipper.apply(element);
            xs = xs.prepend(t._1);
            ys = ys.prepend(t._2);
        }
        return Tuple.of(xs.reverse(), ys.reverse());
    }

    @Override
    default public <T1, T2, T3> Tuple3<List<T1>, List<T2>, List<T3>> unzip3(Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        LinearSeq xs = Nil.instance();
        LinearSeq ys = Nil.instance();
        LinearSeq zs = Nil.instance();
        for (Object element : this) {
            Tuple3<? extends T1, ? extends T2, ? extends T3> t = unzipper.apply(element);
            xs = xs.prepend(t._1);
            ys = ys.prepend(t._2);
            zs = zs.prepend(t._3);
        }
        return Tuple.of(xs.reverse(), ys.reverse(), zs.reverse());
    }

    @Override
    default public List<T> update(int index, T element) {
        if (this.isEmpty()) {
            throw new IndexOutOfBoundsException("update(" + index + ", e) on Nil");
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("update(" + index + ", e)");
        }
        LinearSeq preceding = Nil.instance();
        LinearSeq<T> tail = this;
        int i = index;
        while (i > 0) {
            if (tail.isEmpty()) {
                throw new IndexOutOfBoundsException("update(" + index + ", e) on List of length " + this.length());
            }
            preceding = preceding.prepend(tail.head());
            --i;
            tail = tail.tail();
        }
        if (tail.isEmpty()) {
            throw new IndexOutOfBoundsException("update(" + index + ", e) on List of length " + this.length());
        }
        LinearSeq result = tail.tail().prepend((Object)element);
        for (Object next : preceding) {
            result = result.prepend(next);
        }
        return result;
    }

    @Override
    default public List<T> update(int index, Function<? super T, ? extends T> updater) {
        Objects.requireNonNull(updater, "updater is null");
        return this.update(index, (Object)updater.apply(this.get(index)));
    }

    @Override
    default public <U> List<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        return this.zipWith((Iterable)that, Tuple::of);
    }

    @Override
    default public <U, R> List<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return List.ofAll(this.iterator().zipWith(that, mapper));
    }

    @Override
    default public <U> List<Tuple2<T, U>> zipAll(Iterable<? extends U> that, T thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        return List.ofAll(this.iterator().zipAll(that, thisElem, thatElem));
    }

    @Override
    default public List<Tuple2<T, Integer>> zipWithIndex() {
        return this.zipWithIndex(Tuple::of);
    }

    @Override
    default public <U> List<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return List.ofAll(this.iterator().zipWithIndex(mapper));
    }

    public static final class Cons<T>
    implements List<T>,
    Serializable {
        private static final long serialVersionUID = 1L;
        private final T head;
        private final List<T> tail;
        private final int length;

        private Cons(T head, List<T> tail) {
            this.head = head;
            this.tail = tail;
            this.length = 1 + tail.length();
        }

        @Override
        public T head() {
            return this.head;
        }

        @Override
        public int length() {
            return this.length;
        }

        @Override
        public List<T> tail() {
            return this.tail;
        }

        @Override
        public boolean isEmpty() {
            return false;
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
            return this.mkString(this.stringPrefix() + "(", ", ", ")");
        }

        @GwtIncompatible(value="The Java serialization protocol is explicitly not supported")
        private Object writeReplace() {
            return new SerializationProxy(this);
        }

        @GwtIncompatible(value="The Java serialization protocol is explicitly not supported")
        private void readObject(ObjectInputStream stream) throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required");
        }

        @GwtIncompatible(value="The Java serialization protocol is explicitly not supported")
        private static final class SerializationProxy<T>
        implements Serializable {
            private static final long serialVersionUID = 1L;
            private transient Cons<T> list;

            SerializationProxy(Cons<T> list) {
                this.list = list;
            }

            private void writeObject(ObjectOutputStream s) throws IOException {
                s.defaultWriteObject();
                s.writeInt(this.list.length());
                LinearSeq<T> l = this.list;
                while (!l.isEmpty()) {
                    s.writeObject(l.head());
                    l = l.tail();
                }
            }

            private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
                s.defaultReadObject();
                int size = s.readInt();
                if (size <= 0) {
                    throw new InvalidObjectException("No elements");
                }
                LinearSeq temp = Nil.instance();
                for (int i = 0; i < size; ++i) {
                    Object element = s.readObject();
                    temp = temp.prepend(element);
                }
                this.list = (Cons)temp.reverse();
            }

            private Object readResolve() {
                return this.list;
            }
        }
    }

    public static final class Nil<T>
    implements List<T>,
    Serializable {
        private static final long serialVersionUID = 1L;
        private static final Nil<?> INSTANCE = new Nil();

        private Nil() {
        }

        public static <T> Nil<T> instance() {
            return INSTANCE;
        }

        @Override
        public T head() {
            throw new NoSuchElementException("head of empty list");
        }

        @Override
        public int length() {
            return 0;
        }

        @Override
        public List<T> tail() {
            throw new UnsupportedOperationException("tail of empty list");
        }

        @Override
        public boolean isEmpty() {
            return true;
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

