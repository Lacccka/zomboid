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
import io.vavr.collection.LinearSeq;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.collection.Traversable;
import io.vavr.collection.TreeModule;
import io.vavr.control.Option;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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

public interface Tree<T>
extends Traversable<T>,
Serializable {
    public static final long serialVersionUID = 1L;

    public static <T> Collector<T, ArrayList<T>, Tree<T>> collector() {
        Supplier<ArrayList> supplier = ArrayList::new;
        BiConsumer<ArrayList, Object> accumulator = ArrayList::add;
        BinaryOperator combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        Function<ArrayList, Tree> finisher = Tree::ofAll;
        return Collector.of(supplier, accumulator, combiner, finisher, new Collector.Characteristics[0]);
    }

    public static <T> Empty<T> empty() {
        return Empty.instance();
    }

    public static <T> Tree<T> narrow(Tree<? extends T> tree) {
        return tree;
    }

    public static <T> Node<T> of(T value) {
        return new Node<T>(value, List.empty());
    }

    @SafeVarargs
    public static <T> Node<T> of(T value, Node<T> ... children) {
        Objects.requireNonNull(children, "children is null");
        return new Node<T>(value, List.of(children));
    }

    public static <T> Node<T> of(T value, Iterable<Node<T>> children) {
        Objects.requireNonNull(children, "children is null");
        return new Node<T>(value, List.ofAll(children));
    }

    @SafeVarargs
    public static <T> Tree<T> of(T ... values2) {
        Objects.requireNonNull(values2, "values is null");
        List<T> list = List.of(values2);
        return list.isEmpty() ? Empty.instance() : new Node(list.head(), list.tail().map(Tree::of));
    }

    public static <T> Tree<T> ofAll(Iterable<? extends T> iterable) {
        Objects.requireNonNull(iterable, "iterable is null");
        if (iterable instanceof Tree) {
            return (Tree)iterable;
        }
        List<T> list = List.ofAll(iterable);
        return list.isEmpty() ? Empty.instance() : new Node(list.head(), list.tail().map(Tree::of));
    }

    public static <T> Tree<T> ofAll(java.util.stream.Stream<? extends T> javaStream) {
        Objects.requireNonNull(javaStream, "javaStream is null");
        return Tree.ofAll(Iterator.ofAll(javaStream.iterator()));
    }

    public static <T> Tree<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return Collections.tabulate(n, f, Tree.empty(), Tree::of);
    }

    public static <T> Tree<T> fill(int n, Supplier<? extends T> s) {
        Objects.requireNonNull(s, "s is null");
        return Collections.fill(n, s, Tree.empty(), Tree::of);
    }

    public static <T> Tree<T> fill(int n, T element) {
        return Collections.fillObject(n, element, Tree.empty(), Tree::of);
    }

    public static <T> Node<T> recurse(T seed, Function<? super T, ? extends Iterable<? extends T>> descend) {
        Objects.requireNonNull(descend, "descend is null");
        return Tree.of(seed, Stream.of(seed).flatMap(descend).map((T children) -> Tree.recurse(children, descend)));
    }

    public static <T, ID> List<Node<T>> build(Iterable<? extends T> source2, Function<? super T, ? extends ID> idMapper, Function<? super T, ? extends ID> parentMapper) {
        Objects.requireNonNull(source2, "source is null");
        Objects.requireNonNull(source2, "idMapper is null");
        Objects.requireNonNull(source2, "parentMapper is null");
        List<? super T> list = List.ofAll(source2);
        Map<ID, List<T>> byParent = list.groupBy(parentMapper);
        Function descend = idMapper.andThen(byParent::get).andThen(o -> o.getOrElse(List::empty));
        List roots = byParent.get(null).getOrElse(List::empty);
        return roots.map((T v) -> Tree.recurse(v, descend));
    }

    @Override
    default public <R> Tree<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        return Tree.ofAll(this.iterator().collect(partialFunction));
    }

    public T getValue();

    public List<Node<T>> getChildren();

    public boolean isLeaf();

    default public boolean isBranch() {
        return !this.isEmpty() && !this.isLeaf();
    }

    @Override
    default public boolean isAsync() {
        return false;
    }

    @Override
    default public boolean isDistinct() {
        return false;
    }

    @Override
    default public boolean isLazy() {
        return false;
    }

    @Override
    default public boolean isSequential() {
        return true;
    }

    default public Iterator<T> iterator(Order order) {
        return this.values(order).iterator();
    }

    public String toLispString();

    default public <U> U transform(Function<? super Tree<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    default public Seq<Node<T>> traverse() {
        return this.traverse(Order.PRE_ORDER);
    }

    default public Seq<Node<T>> traverse(Order order) {
        Objects.requireNonNull(order, "order is null");
        if (this.isEmpty()) {
            return Stream.empty();
        }
        Node node = (Node)this;
        switch (order) {
            case PRE_ORDER: {
                return TreeModule.traversePreOrder(node);
            }
            case IN_ORDER: {
                return TreeModule.traverseInOrder(node);
            }
            case POST_ORDER: {
                return TreeModule.traversePostOrder(node);
            }
            case LEVEL_ORDER: {
                return TreeModule.traverseLevelOrder(node);
            }
        }
        throw new IllegalStateException("Unknown order: " + order.name());
    }

    default public Seq<T> values() {
        return this.traverse(Order.PRE_ORDER).map(Node::getValue);
    }

    default public Seq<T> values(Order order) {
        return this.traverse(order).map(Node::getValue);
    }

    default public int branchCount() {
        if (this.isEmpty() || this.isLeaf()) {
            return 0;
        }
        return this.getChildren().foldLeft(1, (count, child) -> count + child.branchCount());
    }

    default public int leafCount() {
        if (this.isEmpty()) {
            return 0;
        }
        if (this.isLeaf()) {
            return 1;
        }
        return this.getChildren().foldLeft(0, (count, child) -> count + child.leafCount());
    }

    default public int nodeCount() {
        return this.length();
    }

    @Override
    default public Seq<T> distinct() {
        return this.values().distinct();
    }

    @Override
    default public Seq<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        if (this.isEmpty()) {
            return Stream.empty();
        }
        return this.values().distinctBy((Comparator)comparator);
    }

    @Override
    default public <U> Seq<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        if (this.isEmpty()) {
            return Stream.empty();
        }
        return this.values().distinctBy(keyExtractor);
    }

    @Override
    default public Seq<T> drop(int n) {
        if (n >= this.length()) {
            return Stream.empty();
        }
        return this.values().drop(n);
    }

    @Override
    default public Seq<T> dropRight(int n) {
        if (n >= this.length()) {
            return Stream.empty();
        }
        return this.values().dropRight(n);
    }

    @Override
    default public Seq<T> dropUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.dropWhile((Predicate)predicate.negate());
    }

    @Override
    default public Seq<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (this.isEmpty()) {
            return Stream.empty();
        }
        return this.values().dropWhile((Predicate)predicate);
    }

    @Override
    default public Seq<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (this.isEmpty()) {
            return Stream.empty();
        }
        return this.values().filter((Predicate)predicate);
    }

    @Override
    default public Seq<T> reject(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (this.isEmpty()) {
            return Stream.empty();
        }
        return this.values().reject((Predicate)predicate);
    }

    @Override
    default public <U> Tree<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.isEmpty() ? Empty.instance() : TreeModule.flatMap((Node)this, mapper);
    }

    @Override
    default public <U> U foldRight(U zero, BiFunction<? super T, ? super U, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        if (this.isEmpty()) {
            return zero;
        }
        return this.iterator().foldRight(zero, f);
    }

    @Override
    default public <C> Map<C, Seq<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return Collections.groupBy(this.values(), classifier, Stream::ofAll);
    }

    @Override
    default public Iterator<Seq<T>> grouped(int size) {
        return this.sliding(size, size);
    }

    @Override
    default public boolean hasDefiniteSize() {
        return true;
    }

    @Override
    default public T head() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("head of empty tree");
        }
        return (T)this.iterator().next();
    }

    @Override
    default public Seq<T> init() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("init of empty tree");
        }
        return this.values().init();
    }

    @Override
    default public Option<Seq<T>> initOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.init());
    }

    @Override
    default public boolean isTraversableAgain() {
        return true;
    }

    @Override
    default public Iterator<T> iterator() {
        return this.values().iterator();
    }

    @Override
    default public <U> Tree<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.isEmpty() ? Empty.instance() : TreeModule.map((Node)this, mapper);
    }

    @Override
    default public Tree<T> orElse(Iterable<? extends T> other) {
        return this.isEmpty() ? Tree.ofAll(other) : this;
    }

    @Override
    default public Tree<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return this.isEmpty() ? Tree.ofAll(supplier.get()) : this;
    }

    @Override
    default public Tuple2<Seq<T>, Seq<T>> partition(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (this.isEmpty()) {
            return Tuple.of(Stream.empty(), Stream.empty());
        }
        return this.values().partition(predicate);
    }

    @Override
    default public Tree<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (!this.isEmpty()) {
            action.accept(this.head());
        }
        return this;
    }

    @Override
    default public Tree<T> replace(T currentElement, T newElement) {
        if (this.isEmpty()) {
            return Empty.instance();
        }
        return TreeModule.replace((Node)this, currentElement, newElement);
    }

    @Override
    default public Tree<T> replaceAll(T currentElement, T newElement) {
        return this.map((T t) -> Objects.equals(t, currentElement) ? newElement : t);
    }

    @Override
    default public Seq<T> retainAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        return this.values().retainAll((Iterable)elements);
    }

    @Override
    default public Seq<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        return this.scanLeft(zero, operation);
    }

    @Override
    default public <U> Seq<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        return Collections.scanLeft(this, zero, operation, Value::toStream);
    }

    @Override
    default public <U> Seq<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        return Collections.scanRight(this, zero, operation, Value::toStream);
    }

    @Override
    default public Iterator<Seq<T>> slideBy(Function<? super T, ?> classifier) {
        return this.iterator().slideBy(classifier);
    }

    @Override
    default public Iterator<Seq<T>> sliding(int size) {
        return this.sliding(size, 1);
    }

    @Override
    default public Iterator<Seq<T>> sliding(int size, int step) {
        return this.iterator().sliding(size, step);
    }

    @Override
    default public Tuple2<Seq<T>, Seq<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (this.isEmpty()) {
            return Tuple.of(Stream.empty(), Stream.empty());
        }
        return this.values().span(predicate);
    }

    @Override
    default public String stringPrefix() {
        return "Tree";
    }

    @Override
    default public Seq<T> tail() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("tail of empty tree");
        }
        return this.values().tail();
    }

    @Override
    default public Option<Seq<T>> tailOption() {
        return this.isEmpty() ? Option.none() : Option.some(this.tail());
    }

    @Override
    default public Seq<T> take(int n) {
        if (this.isEmpty()) {
            return Stream.empty();
        }
        return this.values().take(n);
    }

    @Override
    default public Seq<T> takeRight(int n) {
        if (this.isEmpty()) {
            return Stream.empty();
        }
        return this.values().takeRight(n);
    }

    @Override
    default public Seq<T> takeUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.values().takeUntil((Predicate)predicate);
    }

    @Override
    default public Seq<T> takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return this.values().takeWhile((Predicate)predicate);
    }

    @Override
    default public <T1, T2> Tuple2<Tree<T1>, Tree<T2>> unzip(Function<? super T, Tuple2<? extends T1, ? extends T2>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        if (this.isEmpty()) {
            return Tuple.of(Empty.instance(), Empty.instance());
        }
        return TreeModule.unzip((Node)this, unzipper);
    }

    @Override
    default public <T1, T2, T3> Tuple3<Tree<T1>, Tree<T2>, Tree<T3>> unzip3(Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> unzipper) {
        Objects.requireNonNull(unzipper, "unzipper is null");
        if (this.isEmpty()) {
            return Tuple.of(Empty.instance(), Empty.instance(), Empty.instance());
        }
        return TreeModule.unzip3((Node)this, unzipper);
    }

    @Override
    default public <U> Tree<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        return this.zipWith((Iterable)that, Tuple::of);
    }

    @Override
    default public <U, R> Tree<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        if (this.isEmpty()) {
            return Empty.instance();
        }
        return TreeModule.zip((Node)this, that.iterator(), mapper);
    }

    @Override
    default public <U> Tree<Tuple2<T, U>> zipAll(Iterable<? extends U> that, T thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        if (this.isEmpty()) {
            return Iterator.ofAll(that).map((T elem) -> Tuple.of(thisElem, elem)).toTree();
        }
        java.util.Iterator<U> thatIter = that.iterator();
        Tree<Tuple2<T, U>> tree = TreeModule.zipAll((Node)this, thatIter, thatElem);
        if (thatIter.hasNext()) {
            Traversable remainder = Iterator.ofAll(thatIter).map((T elem) -> Tree.of(Tuple.of(thisElem, elem)));
            return new Node<Tuple2<T, U>>(tree.getValue(), tree.getChildren().appendAll((Iterable)remainder));
        }
        return tree;
    }

    @Override
    default public Tree<Tuple2<T, Integer>> zipWithIndex() {
        return this.zipWithIndex(Tuple::of);
    }

    @Override
    default public <U> Tree<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return this.zipWith(Iterator.from(0), mapper);
    }

    @Override
    public boolean equals(Object var1);

    @Override
    public int hashCode();

    @Override
    public String toString();

    public String draw();

    public static enum Order {
        PRE_ORDER,
        IN_ORDER,
        POST_ORDER,
        LEVEL_ORDER;

    }

    public static final class Empty<T>
    implements Tree<T>,
    Serializable {
        private static final long serialVersionUID = 1L;
        private static final Empty<?> INSTANCE = new Empty();

        private Empty() {
        }

        public static <T> Empty<T> instance() {
            return INSTANCE;
        }

        @Override
        public List<Node<T>> getChildren() {
            return List.Nil.instance();
        }

        @Override
        public T getValue() {
            throw new UnsupportedOperationException("getValue of empty Tree");
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public int length() {
            return 0;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public T last() {
            throw new NoSuchElementException("last of empty tree");
        }

        @Override
        public boolean equals(Object o) {
            return o == this;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public String toString() {
            return this.stringPrefix() + "()";
        }

        @Override
        public String toLispString() {
            return "()";
        }

        @Override
        public String draw() {
            return "\u25a3";
        }

        private Object readResolve() {
            return INSTANCE;
        }
    }

    public static final class Node<T>
    implements Tree<T>,
    Serializable {
        private static final long serialVersionUID = 1L;
        private final T value;
        private final List<Node<T>> children;
        private final int size;

        public Node(T value, List<Node<T>> children) {
            Objects.requireNonNull(children, "children is null");
            this.value = value;
            this.children = children;
            this.size = children.foldLeft(1, (acc, child) -> acc + child.size);
        }

        @Override
        public List<Node<T>> getChildren() {
            return this.children;
        }

        @Override
        public T getValue() {
            return this.value;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public int length() {
            return this.size;
        }

        @Override
        public boolean isLeaf() {
            return this.size == 1;
        }

        @Override
        public T last() {
            return this.children.isEmpty() ? this.value : this.children.last().last();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof Node) {
                Node that = (Node)o;
                return Objects.equals(this.getValue(), that.getValue()) && Objects.equals(this.getChildren(), that.getChildren());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Tuple.hash(this.value, this.children);
        }

        @Override
        public String toString() {
            return this.mkString(this.stringPrefix() + "(", ", ", ")");
        }

        @Override
        public String toLispString() {
            return Node.toLispString(this);
        }

        @Override
        public String draw() {
            StringBuilder builder = new StringBuilder();
            this.drawAux("", builder);
            return builder.toString();
        }

        private void drawAux(String indent, StringBuilder builder) {
            builder.append(this.value);
            LinearSeq<Node<T>> it = this.children;
            while (!it.isEmpty()) {
                boolean isLast = it.tail().isEmpty();
                builder.append('\n').append(indent).append(isLast ? "\u2514\u2500\u2500" : "\u251c\u2500\u2500");
                ((Node)it.head()).drawAux(indent + (isLast ? "   " : "\u2502  "), builder);
                it = it.tail();
            }
        }

        private static String toLispString(Tree<?> tree) {
            String value = String.valueOf(tree.getValue());
            if (tree.isLeaf()) {
                return value;
            }
            String children = tree.getChildren().map((T child) -> Node.toLispString(child)).mkString(" ");
            return "(" + value + " " + children + ")";
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
            private transient Node<T> node;

            SerializationProxy(Node<T> node) {
                this.node = node;
            }

            private void writeObject(ObjectOutputStream s) throws IOException {
                s.defaultWriteObject();
                s.writeObject(((Node)this.node).value);
                s.writeObject(((Node)this.node).children);
            }

            private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
                s.defaultReadObject();
                Object value = s.readObject();
                List children = (List)s.readObject();
                this.node = new Node<Object>(value, children);
            }

            private Object readResolve() {
                return this.node;
            }
        }
    }
}

