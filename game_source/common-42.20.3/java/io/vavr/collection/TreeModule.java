/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.LinearSeq;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.collection.Traversable;
import io.vavr.collection.Tree;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

interface TreeModule {
    public static <T, U> Tree<U> flatMap(Tree.Node<T> node, Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Tree<U> mapped = Tree.ofAll(mapper.apply(node.getValue()));
        if (mapped.isEmpty()) {
            return Tree.empty();
        }
        List children = (List)node.getChildren().map(child -> TreeModule.flatMap(child, mapper)).filter(Traversable::nonEmpty);
        return Tree.of(mapped.get(), children.prependAll(mapped.getChildren()));
    }

    public static <T, U> Tree.Node<U> map(Tree.Node<T> node, Function<? super T, ? extends U> mapper) {
        U value = mapper.apply(node.getValue());
        LinearSeq children = node.getChildren().map(child -> TreeModule.map(child, mapper));
        return new Tree.Node<U>(value, children);
    }

    public static <T> Tree.Node<T> replace(Tree.Node<T> node, T currentElement, T newElement) {
        if (Objects.equals(node.getValue(), currentElement)) {
            return new Tree.Node<T>(newElement, node.getChildren());
        }
        for (Tree.Node node2 : node.getChildren()) {
            Tree.Node<T> newChild = TreeModule.replace(node2, currentElement, newElement);
            boolean found = newChild != node2;
            if (!found) continue;
            LinearSeq newChildren = node.getChildren().replace((Object)node2, newChild);
            return new Tree.Node<T>(node.getValue(), newChildren);
        }
        return node;
    }

    public static <T> Stream<Tree.Node<T>> traversePreOrder(Tree.Node<T> node) {
        return node.getChildren().foldLeft(Stream.of(node), (acc, child) -> acc.appendAll((Iterable)TreeModule.traversePreOrder(child)));
    }

    public static <T> Stream<Tree.Node<T>> traverseInOrder(Tree.Node<T> node) {
        if (node.isLeaf()) {
            return Stream.of(node);
        }
        List<Tree.Node<T>> children = node.getChildren();
        return children.tail().foldLeft(Stream.empty(), (acc, child) -> acc.appendAll((Iterable)TreeModule.traverseInOrder(child))).prepend(node).prependAll(TreeModule.traverseInOrder((Tree.Node)children.head()));
    }

    public static <T> Stream<Tree.Node<T>> traversePostOrder(Tree.Node<T> node) {
        return node.getChildren().foldLeft(Stream.empty(), (acc, child) -> acc.appendAll((Iterable)TreeModule.traversePostOrder(child))).append(node);
    }

    public static <T> Stream<Tree.Node<T>> traverseLevelOrder(Tree.Node<T> node) {
        LinearSeq result = Stream.empty();
        LinkedList<Tree.Node<T>> queue = new LinkedList<Tree.Node<T>>();
        queue.add(node);
        while (!queue.isEmpty()) {
            Tree.Node next = (Tree.Node)queue.remove();
            result = result.prepend(next);
            queue.addAll(next.getChildren().toJavaList());
        }
        return result.reverse();
    }

    public static <T, T1, T2> Tuple2<Tree.Node<T1>, Tree.Node<T2>> unzip(Tree.Node<T> node, Function<? super T, Tuple2<? extends T1, ? extends T2>> unzipper) {
        Tuple2<? extends T1, ? extends T2> value = unzipper.apply(node.getValue());
        LinearSeq children = node.getChildren().map(child -> TreeModule.unzip(child, unzipper));
        Tree.Node node1 = new Tree.Node(value._1, children.map(t -> (Tree.Node)t._1));
        Tree.Node node2 = new Tree.Node(value._2, children.map(t -> (Tree.Node)t._2));
        return Tuple.of(node1, node2);
    }

    public static <T, T1, T2, T3> Tuple3<Tree.Node<T1>, Tree.Node<T2>, Tree.Node<T3>> unzip3(Tree.Node<T> node, Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> unzipper) {
        Tuple3<? extends T1, ? extends T2, ? extends T3> value = unzipper.apply(node.getValue());
        LinearSeq children = node.getChildren().map(child -> TreeModule.unzip3(child, unzipper));
        Tree.Node node1 = new Tree.Node(value._1, children.map(t -> (Tree.Node)t._1));
        Tree.Node node2 = new Tree.Node(value._2, children.map(t -> (Tree.Node)t._2));
        Tree.Node node3 = new Tree.Node(value._3, children.map(t -> (Tree.Node)t._3));
        return Tuple.of(node1, node2, node3);
    }

    public static <T, U, R> Tree<R> zip(Tree.Node<T> node, Iterator<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        if (!that.hasNext()) {
            return Tree.Empty.instance();
        }
        R value = mapper.apply(node.getValue(), that.next());
        List children = (List)node.getChildren().map(child -> TreeModule.zip(child, that, mapper)).filter(Traversable::nonEmpty);
        return new Tree.Node<R>(value, children);
    }

    public static <T, U> Tree<Tuple2<T, U>> zipAll(Tree.Node<T> node, Iterator<? extends U> that, U thatElem) {
        if (!that.hasNext()) {
            return node.map(value -> Tuple.of(value, thatElem));
        }
        Tuple2<T, U> value2 = Tuple.of(node.getValue(), that.next());
        List children = (List)node.getChildren().map(child -> TreeModule.zipAll(child, that, thatElem)).filter(Traversable::nonEmpty);
        return new Tree.Node<Tuple2<T, U>>(value2, children);
    }
}

