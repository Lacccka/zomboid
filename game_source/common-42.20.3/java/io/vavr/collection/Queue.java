/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.PartialFunction;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Value;
import io.vavr.collection.AbstractQueue;
import io.vavr.collection.Collections;
import io.vavr.collection.GwtIncompatible;
import io.vavr.collection.Iterator;
import io.vavr.collection.JavaConverters;
import io.vavr.collection.LinearSeq;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public final class Queue<T>
extends AbstractQueue<T, Queue<T>>
implements LinearSeq<T> {
    private static final long serialVersionUID = 1L;
    private static final Queue<?> EMPTY = new Queue(List.empty(), List.empty());
    private final List<T> front;
    private final List<T> rear;

    private Queue(List<T> front, List<T> rear) {
        boolean frontIsEmpty = front.isEmpty();
        this.front = frontIsEmpty ? rear.reverse() : front;
        this.rear = frontIsEmpty ? front : rear;
    }

    public static <T> Collector<T, ArrayList<T>, Queue<T>> collector() {
        Supplier<ArrayList> supplier = ArrayList::new;
        BiConsumer<ArrayList, Object> accumulator = ArrayList::add;
        BinaryOperator combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        Function<ArrayList, Queue> finisher = Queue::ofAll;
        return Collector.of(supplier, accumulator, combiner, finisher, new Collector.Characteristics[0]);
    }

    public static <T> Queue<T> empty() {
        return EMPTY;
    }

    public static <T> Queue<T> narrow(Queue<? extends T> queue) {
        return queue;
    }

    public static <T> Queue<T> of(T element) {
        return Queue.ofAll(List.of(element));
    }

    @SafeVarargs
    public static <T> Queue<T> of(T ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Queue.ofAll(List.of(elements));
    }

    public static <T> Queue<T> ofAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (elements instanceof Queue) {
            return (Queue)elements;
        }
        if (elements instanceof JavaConverters.ListView && ((JavaConverters.ListView)elements).getDelegate() instanceof Queue) {
            return (Queue)((JavaConverters.ListView)elements).getDelegate();
        }
        if (!elements.iterator().hasNext()) {
            return Queue.empty();
        }
        if (elements instanceof List) {
            return new Queue((List)elements, List.empty());
        }
        return new Queue<T>(List.ofAll(elements), List.empty());
    }

    public static <T> Queue<T> ofAll(Stream<? extends T> javaStream) {
        Objects.requireNonNull(javaStream, "javaStream is null");
        return new Queue<T>(List.ofAll(javaStream), List.empty());
    }

    public static Queue<Boolean> ofAll(boolean ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Queue.ofAll(List.ofAll(elements));
    }

    public static Queue<Byte> ofAll(byte ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Queue.ofAll(List.ofAll(elements));
    }

    public static Queue<Character> ofAll(char ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Queue.ofAll(List.ofAll(elements));
    }

    public static Queue<Double> ofAll(double ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Queue.ofAll(List.ofAll(elements));
    }

    public static Queue<Float> ofAll(float ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Queue.ofAll(List.ofAll(elements));
    }

    public static Queue<Integer> ofAll(int ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Queue.ofAll(List.ofAll(elements));
    }

    public static Queue<Long> ofAll(long ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Queue.ofAll(List.ofAll(elements));
    }

    public static Queue<Short> ofAll(short ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Queue.ofAll(List.ofAll(elements));
    }

    public static <T> Queue<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return Collections.tabulate(n, f, Queue.empty(), Queue::of);
    }

    public static <T> Queue<T> fill(int n, Supplier<? extends T> s) {
        Objects.requireNonNull(s, "s is null");
        return Collections.fill(n, s, Queue.empty(), Queue::of);
    }

    public static <T> Queue<T> fill(int n, T element) {
        return Collections.fillObject(n, element, Queue.empty(), Queue::of);
    }

    public static Queue<Character> range(char from, char toExclusive) {
        return Queue.ofAll(Iterator.range(from, toExclusive));
    }

    public static Queue<Character> rangeBy(char from, char toExclusive, int step) {
        return Queue.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    @GwtIncompatible
    public static Queue<Double> rangeBy(double from, double toExclusive, double step) {
        return Queue.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static Queue<Integer> range(int from, int toExclusive) {
        return Queue.ofAll(Iterator.range(from, toExclusive));
    }

    public static Queue<Integer> rangeBy(int from, int toExclusive, int step) {
        return Queue.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static Queue<Long> range(long from, long toExclusive) {
        return Queue.ofAll(Iterator.range(from, toExclusive));
    }

    public static Queue<Long> rangeBy(long from, long toExclusive, long step) {
        return Queue.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static Queue<Character> rangeClosed(char from, char toInclusive) {
        return Queue.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static Queue<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return Queue.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    @GwtIncompatible
    public static Queue<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return Queue.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static Queue<Integer> rangeClosed(int from, int toInclusive) {
        return Queue.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static Queue<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return Queue.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static Queue<Long> rangeClosed(long from, long toInclusive) {
        return Queue.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static <T> Queue<Queue<T>> transpose(Queue<Queue<T>> matrix) {
        return Collections.transpose(matrix, Queue::ofAll, Queue::of);
    }

    public static Queue<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return Queue.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static <T, U> Queue<U> unfoldRight(T seed, Function<? super T, Option<Tuple2<? extends U, ? extends T>>> f) {
        return Iterator.unfoldRight(seed, f).toQueue();
    }

    public static <T, U> Queue<U> unfoldLeft(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends U>>> f) {
        return Iterator.unfoldLeft(seed, f).toQueue();
    }

    public static <T> Queue<T> unfold(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends T>>> f) {
        return Iterator.unfold(seed, f).toQueue();
    }

    @Override
    public Queue<T> enqueue(T element) {
        return new Queue<T>(this.front, this.rear.prepend((Object)element));
    }

    /*
     * Exception decompiling
     */
    @Override
    public Queue<T> enqueueAll(Iterable<? extends T> elements) {
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
    public Queue<T> append(T element) {
        return this.enqueue((Object)element);
    }

    @Override
    public Queue<T> appendAll(Iterable<? extends T> elements) {
        return this.enqueueAll((Iterable)elements);
    }

    @Override
    @GwtIncompatible
    public java.util.List<T> asJava() {
        return JavaConverters.asJava(this, JavaConverters.ChangePolicy.IMMUTABLE);
    }

    @Override
    @GwtIncompatible
    public Queue<T> asJava(Consumer<? super java.util.List<T>> action) {
        return Collections.asJava(this, action, JavaConverters.ChangePolicy.IMMUTABLE);
    }

    @Override
    @GwtIncompatible
    public java.util.List<T> asJavaMutable() {
        return JavaConverters.asJava(this, JavaConverters.ChangePolicy.MUTABLE);
    }

    @Override
    @GwtIncompatible
    public Queue<T> asJavaMutable(Consumer<? super java.util.List<T>> action) {
        return Collections.asJava(this, action, JavaConverters.ChangePolicy.MUTABLE);
    }

    @Override
    public <R> Queue<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        return Queue.ofAll(this.iterator().collect(partialFunction));
    }

    @Override
    public Queue<Queue<T>> combinations() {
        return Queue.ofAll(this.toList().combinations().map(Queue::ofAll));
    }

    @Override
    public Queue<Queue<T>> combinations(int k) {
        return Queue.ofAll(this.toList().combinations(k).map(Queue::ofAll));
    }

    @Override
    public Iterator<Queue<T>> crossProduct(int power) {
        return Collections.crossProduct(Queue.empty(), this, power);
    }

    @Override
    public Queue<T> distinct() {
        return Queue.ofAll(this.toList().distinct());
    }

    @Override
    public Queue<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return Queue.ofAll(this.toList().distinctBy((Comparator)comparator));
    }

    @Override
    public <U> Queue<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        return Queue.ofAll(this.toList().distinctBy((Function)keyExtractor));
    }

    @Override
    public Queue<T> drop(int n) {
        if (n <= 0) {
            return this;
        }
        if (n >= this.length()) {
            return Queue.empty();
        }
        return new Queue<T>(this.front.drop(n), this.rear.dropRight(n - this.front.length()));
    }

    @Override
    public Queue<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        LinearSeq dropped = this.toList().dropWhile((Predicate)predicate);
        return Queue.ofAll(dropped.length() == this.length() ? this : dropped);
    }

    @Override
    public Queue<T> dropRight(int n) {
        if (n <= 0) {
            return this;
        }
        if (n >= this.length()) {
            return Queue.empty();
        }
        return new Queue<T>(this.front.dropRight(n - this.rear.length()), this.rear.drop(n));
    }

    @Override
    public Queue<T> dropRightUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return ((Queue)((AbstractQueue)((Object)this.reverse())).dropUntil((Predicate)predicate)).reverse();
    }

    @Override
    public Queue<T> dropRightWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.dropRightUntil((Predicate)predicate.negate());
    }

    @Override
    public Queue<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        LinearSeq filtered = this.toList().filter((Predicate)predicate);
        if (filtered.isEmpty()) {
            return Queue.empty();
        }
        if (filtered.length() == this.length()) {
            return this;
        }
        return Queue.ofAll(filtered);
    }

    @Override
    public <U> Queue<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (this.isEmpty()) {
            return Queue.empty();
        }
        return new Queue<T>(this.front.flatMap(mapper), this.rear.flatMap(mapper));
    }

    @Override
    public T get(int index) {
        if (this.isEmpty()) {
            throw new IndexOutOfBoundsException("get(" + index + ") on empty Queue");
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("get(" + index + ")");
        }
        int length = this.front.length();
        if (index < length) {
            return this.front.get(index);
        }
        int rearIndex = index - length;
        int rearLength = this.rear.length();
        if (rearIndex < rearLength) {
            int reverseRearIndex = rearLength - rearIndex - 1;
            return this.rear.get(reverseRearIndex);
        }
        throw new IndexOutOfBoundsException("get(" + index + ") on Queue of length " + this.length());
    }

    @Override
    public <C> Map<C, Queue<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return Collections.groupBy(this, classifier, Queue::ofAll);
    }

    @Override
    public Iterator<Queue<T>> grouped(int size) {
        return this.sliding(size, size);
    }

    @Override
    public boolean hasDefiniteSize() {
        return true;
    }

    @Override
    public T head() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("head of empty " + this.stringPrefix());
        }
        return this.front.head();
    }

    @Override
    public int indexOf(T element, int from) {
        int frontIndex = this.front.indexOf(element, from);
        if (frontIndex != -1) {
            return frontIndex;
        }
        int rearIndex = this.rear.reverse().indexOf(element, from - this.front.length());
        return rearIndex == -1 ? -1 : rearIndex + this.front.length();
    }

    @Override
    public Queue<T> init() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("init of empty " + this.stringPrefix());
        }
        if (this.rear.isEmpty()) {
            return new Queue<T>(this.front.init(), this.rear);
        }
        return new Queue<T>(this.front, this.rear.tail());
    }

    @Override
    public Queue<T> insert(int index, T element) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("insert(" + index + ", element)");
        }
        int length = this.front.length();
        if (index <= length) {
            return new Queue<T>(this.front.insert(index, (Object)element), this.rear);
        }
        int rearIndex = index - length;
        int rearLength = this.rear.length();
        if (rearIndex <= rearLength) {
            int reverseRearIndex = rearLength - rearIndex;
            return new Queue<T>(this.front, this.rear.insert(reverseRearIndex, (Object)element));
        }
        throw new IndexOutOfBoundsException("insert(" + index + ", element) on Queue of length " + this.length());
    }

    @Override
    public Queue<T> insertAll(int index, Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (index < 0) {
            throw new IndexOutOfBoundsException("insertAll(" + index + ", elements)");
        }
        int length = this.front.length();
        if (index <= length) {
            if (this.isEmpty() && elements instanceof Queue) {
                return (Queue)elements;
            }
            LinearSeq newFront = this.front.insertAll(index, (Iterable)elements);
            return newFront == this.front ? this : new Queue<T>(newFront, this.rear);
        }
        int rearIndex = index - length;
        int rearLength = this.rear.length();
        if (rearIndex <= rearLength) {
            int reverseRearIndex = rearLength - rearIndex;
            LinearSeq newRear = this.rear.insertAll(reverseRearIndex, (Iterable)List.ofAll(elements).reverse());
            return newRear == this.rear ? this : new Queue<T>(this.front, newRear);
        }
        throw new IndexOutOfBoundsException("insertAll(" + index + ", elements) on Queue of length " + this.length());
    }

    @Override
    public Queue<T> intersperse(T element) {
        if (this.isEmpty()) {
            return this;
        }
        if (this.rear.isEmpty()) {
            return new Queue<T>(this.front.intersperse((Object)element), this.rear);
        }
        return new Queue<T>(this.front.intersperse((Object)element), this.rear.intersperse((Object)element).append((Object)element));
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.front.isEmpty();
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
    public T last() {
        return this.rear.isEmpty() ? this.front.last() : this.rear.head();
    }

    @Override
    public int lastIndexOf(T element, int end) {
        return this.toList().lastIndexOf(element, end);
    }

    @Override
    public int length() {
        return this.front.length() + this.rear.length();
    }

    @Override
    public <U> Queue<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return new Queue<T>(this.front.map(mapper), this.rear.map(mapper));
    }

    @Override
    public Queue<T> orElse(Iterable<? extends T> other) {
        return this.isEmpty() ? Queue.ofAll(other) : this;
    }

    @Override
    public Queue<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return this.isEmpty() ? Queue.ofAll(supplier.get()) : this;
    }

    @Override
    public Queue<T> padTo(int length, T element) {
        int actualLength = this.length();
        if (length <= actualLength) {
            return this;
        }
        return Queue.ofAll(this.toList().padTo(length, (Object)element));
    }

    @Override
    public Queue<T> leftPadTo(int length, T element) {
        int actualLength = this.length();
        if (length <= actualLength) {
            return this;
        }
        return Queue.ofAll(this.toList().leftPadTo(length, (Object)element));
    }

    @Override
    public Queue<T> patch(int from, Iterable<? extends T> that, int replaced) {
        from = from < 0 ? 0 : from;
        replaced = replaced < 0 ? 0 : replaced;
        LinearSeq result = ((Queue)this.take(from)).appendAll((Iterable)that);
        result = ((Queue)result).appendAll((Iterable)this.drop(from += replaced));
        return result;
    }

    @Override
    public Tuple2<Queue<T>, Queue<T>> partition(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.toList().partition(predicate).map(Value::toQueue, Value::toQueue);
    }

    @Override
    public Queue<Queue<T>> permutations() {
        return Queue.ofAll(this.toList().permutations().map(Value::toQueue));
    }

    @Override
    public Queue<T> prepend(T element) {
        return new Queue<T>(this.front.prepend((Object)element), this.rear);
    }

    @Override
    public Queue<T> prependAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (this.isEmpty() && elements instanceof Queue) {
            return (Queue)elements;
        }
        LinearSeq newFront = this.front.prependAll((Iterable)elements);
        return newFront == this.front ? this : new Queue<T>(newFront, this.rear);
    }

    @Override
    public Queue<T> remove(T element) {
        LinearSeq removed = this.toList().remove((Object)element);
        return Queue.ofAll(removed.length() == this.length() ? this : removed);
    }

    @Override
    public Queue<T> removeFirst(Predicate<T> predicate) {
        LinearSeq removed = this.toList().removeFirst((Predicate)predicate);
        return Queue.ofAll(removed.length() == this.length() ? this : removed);
    }

    @Override
    public Queue<T> removeLast(Predicate<T> predicate) {
        LinearSeq removed = this.toList().removeLast((Predicate)predicate);
        return Queue.ofAll(removed.length() == this.length() ? this : removed);
    }

    @Override
    public Queue<T> removeAt(int index) {
        return Queue.ofAll(this.toList().removeAt(index));
    }

    @Override
    public Queue<T> removeAll(T element) {
        return Collections.removeAll(this, element);
    }

    @Override
    public Queue<T> replace(T currentElement, T newElement) {
        LinearSeq newFront = this.front.replace((Object)currentElement, (Object)newElement);
        LinearSeq newRear = this.rear.replace((Object)currentElement, (Object)newElement);
        return newFront.size() + newRear.size() == 0 ? Queue.empty() : (newFront == this.front && newRear == this.rear ? this : new Queue<T>(newFront, newRear));
    }

    @Override
    public Queue<T> replaceAll(T currentElement, T newElement) {
        LinearSeq newFront = this.front.replaceAll((Object)currentElement, (Object)newElement);
        LinearSeq newRear = this.rear.replaceAll((Object)currentElement, (Object)newElement);
        return newFront.size() + newRear.size() == 0 ? Queue.empty() : (newFront == this.front && newRear == this.rear ? this : new Queue<T>(newFront, newRear));
    }

    @Override
    public Queue<T> reverse() {
        return this.isEmpty() ? this : Queue.ofAll(this.toList().reverse());
    }

    @Override
    public Queue<T> rotateLeft(int n) {
        return Collections.rotateLeft(this, n);
    }

    @Override
    public Queue<T> rotateRight(int n) {
        return Collections.rotateRight(this, n);
    }

    @Override
    public Queue<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        return this.scanLeft(zero, operation);
    }

    @Override
    public <U> Queue<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        return Collections.scanLeft(this, zero, operation, Value::toQueue);
    }

    @Override
    public <U> Queue<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        return Collections.scanRight(this, zero, operation, Value::toQueue);
    }

    @Override
    public Queue<T> shuffle() {
        return Collections.shuffle(this, Queue::ofAll);
    }

    @Override
    public Queue<T> slice(int beginIndex, int endIndex) {
        return Queue.ofAll(this.toList().slice(beginIndex, endIndex));
    }

    @Override
    public Iterator<Queue<T>> slideBy(Function<? super T, ?> classifier) {
        return this.iterator().slideBy(classifier).map(Queue::ofAll);
    }

    @Override
    public Iterator<Queue<T>> sliding(int size) {
        return this.sliding(size, 1);
    }

    @Override
    public Iterator<Queue<T>> sliding(int size, int step) {
        return this.iterator().sliding(size, step).map(Queue::ofAll);
    }

    @Override
    public Queue<T> sorted() {
        return Queue.ofAll(this.toList().sorted());
    }

    @Override
    public Queue<T> sorted(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return Queue.ofAll(this.toList().sorted((Comparator)comparator));
    }

    @Override
    public <U extends Comparable<? super U>> Queue<T> sortBy(Function<? super T, ? extends U> mapper) {
        return this.sortBy(Comparable::compareTo, (Function)mapper);
    }

    @Override
    public <U> Queue<T> sortBy(Comparator<? super U> comparator, Function<? super T, ? extends U> mapper) {
        return Collections.sortBy(this, comparator, mapper, Queue.collector());
    }

    @Override
    public Tuple2<Queue<T>, Queue<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.toList().span(predicate).map(Value::toQueue, Value::toQueue);
    }

    @Override
    public Tuple2<Queue<T>, Queue<T>> splitAt(int n) {
        return this.toList().splitAt(n).map(Value::toQueue, Value::toQueue);
    }

    @Override
    public Tuple2<Queue<T>, Queue<T>> splitAt(Predicate<? super T> predicate) {
        return this.toList().splitAt(predicate).map(Value::toQueue, Value::toQueue);
    }

    @Override
    public Tuple2<Queue<T>, Queue<T>> splitAtInclusive(Predicate<? super T> predicate) {
        return this.toList().splitAtInclusive(predicate).map(Value::toQueue, Value::toQueue);
    }

    @Override
    public boolean startsWith(Iterable<? extends T> that, int offset) {
        return this.toList().startsWith(that, offset);
    }

    @Override
    public Queue<T> subSequence(int beginIndex) {
        if (beginIndex < 0 || beginIndex > this.length()) {
            throw new IndexOutOfBoundsException("subSequence(" + beginIndex + ")");
        }
        return this.drop(beginIndex);
    }

    @Override
    public Queue<T> subSequence(int beginIndex, int endIndex) {
        Collections.subSequenceRangeCheck(beginIndex, endIndex, this.length());
        if (beginIndex == endIndex) {
            return Queue.empty();
        }
        if (beginIndex == 0 && endIndex == this.length()) {
            return this;
        }
        return Queue.ofAll(this.toList().subSequence(beginIndex, endIndex));
    }

    @Override
    public Queue<T> tail() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("tail of empty " + this.stringPrefix());
        }
        return new Queue<T>(this.front.tail(), this.rear);
    }

    @Override
    public Queue<T> take(int n) {
        if (n <= 0) {
            return Queue.empty();
        }
        if (n >= this.length()) {
            return this;
        }
        int frontLength = this.front.length();
        if (n < frontLength) {
            return new Queue(this.front.take(n), List.empty());
        }
        if (n == frontLength) {
            return new Queue<T>(this.front, List.empty());
        }
        return new Queue<T>(this.front, this.rear.takeRight(n - frontLength));
    }

    @Override
    public Queue<T> takeUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        LinearSeq taken = this.toList().takeUntil((Predicate)predicate);
        return taken.length() == this.length() ? this : Queue.ofAll(taken);
    }

    @Override
    public Queue<T> takeRight(int n) {
        if (n <= 0) {
            return Queue.empty();
        }
        if (n >= this.length()) {
            return this;
        }
        int rearLength = this.rear.length();
        if (n < rearLength) {
            return new Queue(this.rear.take(n).reverse(), List.empty());
        }
        if (n == rearLength) {
            return new Queue(this.rear.reverse(), List.empty());
        }
        return new Queue<T>(this.front.takeRight(n - rearLength), this.rear);
    }

    @Override
    public Queue<T> takeRightUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        LinearSeq taken = this.toList().takeRightUntil((Predicate)predicate);
        return taken.length() == this.length() ? this : Queue.ofAll(taken);
    }

    @Override
    public Queue<T> takeRightWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.takeRightUntil((Predicate)predicate.negate());
    }

    public <U> U transform(Function<? super Queue<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    public <T1, T2> Tuple2<Queue<T1>, Queue<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        return this.toList().unzip(unzipper).map(Value::toQueue, Value::toQueue);
    }

    @Override
    public <T1, T2, T3> Tuple3<Queue<T1>, Queue<T2>, Queue<T3>> unzip3(Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        return this.toList().unzip3(unzipper).map(Value::toQueue, Value::toQueue, Value::toQueue);
    }

    @Override
    public Queue<T> update(int index, T element) {
        return Queue.ofAll(this.toList().update(index, (Object)element));
    }

    @Override
    public Queue<T> update(int index, Function<? super T, ? extends T> updater) {
        Objects.requireNonNull(updater, "updater is null");
        return this.update(index, (Object)updater.apply(this.get(index)));
    }

    @Override
    public <U> Queue<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        return this.zipWith((Iterable)that, Tuple::of);
    }

    @Override
    public <U, R> Queue<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return Queue.ofAll(this.toList().zipWith((Iterable)that, (BiFunction)mapper));
    }

    @Override
    public <U> Queue<Tuple2<T, U>> zipAll(Iterable<? extends U> that, T thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        return Queue.ofAll(this.toList().zipAll((Iterable)that, (Object)thisElem, (Object)thatElem));
    }

    @Override
    public Queue<Tuple2<T, Integer>> zipWithIndex() {
        return this.zipWithIndex(Tuple::of);
    }

    @Override
    public <U> Queue<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return Queue.ofAll(this.toList().zipWithIndex((BiFunction)mapper));
    }

    private Object readResolve() {
        return this.isEmpty() ? EMPTY : this;
    }

    @Override
    public String stringPrefix() {
        return "Queue";
    }

    @Override
    public boolean equals(Object o) {
        return Collections.equals(this, o);
    }

    @Override
    public int hashCode() {
        return Collections.hashOrdered(this);
    }
}

