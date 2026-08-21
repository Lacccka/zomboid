/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Traversable;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;

final class PriorityQueueBase {
    PriorityQueueBase() {
    }

    static <T> Tuple2<T, Seq<Node<T>>> deleteMin(Comparator<? super T> comparator, Seq<Node<T>> forest) {
        Node<? super T> minTree = PriorityQueueBase.findMin(comparator, forest);
        Seq<Node<T>> forestTail = minTree == forest.head() ? forest.tail() : forest.remove(minTree);
        Seq<Node<T>> newForest = PriorityQueueBase.rebuild(comparator, minTree.children);
        return Tuple.of(minTree.root, PriorityQueueBase.meld(comparator, newForest, forestTail));
    }

    static <T> Seq<Node<T>> insert(Comparator<? super T> comparator, T element, Seq<Node<T>> forest) {
        Node<Object> tree = Node.of(element, 0, List.empty());
        if (forest.size() >= 2) {
            Traversable tail = forest.tail();
            Node t1 = (Node)forest.head();
            Node t2 = (Node)tail.head();
            if (t1.rank == t2.rank) {
                return tree.skewLink(comparator, t1, t2).appendTo((Seq<Node<T>>)tail.tail());
            }
        }
        return tree.appendTo(forest);
    }

    static <T> Seq<Node<T>> meld(Comparator<? super T> comparator, Seq<Node<T>> source2, Seq<Node<T>> target) {
        return PriorityQueueBase.meldUnique(comparator, PriorityQueueBase.uniqify(comparator, source2), PriorityQueueBase.uniqify(comparator, target));
    }

    static <T> Node<T> findMin(Comparator<? super T> comparator, Seq<Node<T>> forest) {
        Iterator iterator2 = forest.iterator();
        Node min = (Node)iterator2.next();
        Iterator iterator3 = iterator2.iterator();
        while (iterator3.hasNext()) {
            Node node = (Node)iterator3.next();
            if (comparator.compare(node.root, min.root) >= 0) continue;
            min = node;
        }
        return min;
    }

    private static <T> Seq<Node<T>> rebuild(Comparator<? super T> comparator, Seq<Node<T>> forest) {
        Seq<Node<T>> nonZeroRank = List.empty();
        Seq<Node<Object>> zeroRank = List.empty();
        while (!forest.isEmpty()) {
            Node initialForestHead = (Node)forest.head();
            if (initialForestHead.rank == 0) {
                zeroRank = PriorityQueueBase.insert(comparator, initialForestHead.root, zeroRank);
            } else {
                nonZeroRank = initialForestHead.appendTo(nonZeroRank);
            }
            forest = forest.tail();
        }
        return PriorityQueueBase.meld(comparator, nonZeroRank, zeroRank);
    }

    private static <T> Seq<Node<T>> uniqify(Comparator<? super T> comparator, Seq<Node<T>> forest) {
        return forest.isEmpty() ? forest : PriorityQueueBase.ins(comparator, (Node)forest.head(), forest.tail());
    }

    private static <T> Seq<Node<T>> ins(Comparator<? super T> comparator, Node<T> tree, Seq<Node<T>> forest) {
        while (!forest.isEmpty() && tree.rank == ((Node)forest.head()).rank) {
            tree = tree.link(comparator, (Node)forest.head());
            forest = forest.tail();
        }
        return tree.appendTo((Seq<Node<Object>>)forest);
    }

    private static <T> Seq<Node<T>> meldUnique(Comparator<? super T> comparator, Seq<Node<T>> forest1, Seq<Node<T>> forest2) {
        if (forest1.isEmpty()) {
            return forest2;
        }
        if (forest2.isEmpty()) {
            return forest1;
        }
        Node tree1 = (Node)forest1.head();
        Node tree2 = (Node)forest2.head();
        if (tree1.rank == tree2.rank) {
            Node<? super T> tree = tree1.link(comparator, tree2);
            Seq<Node<T>> forest = PriorityQueueBase.meldUnique(comparator, forest1.tail(), forest2.tail());
            return PriorityQueueBase.ins(comparator, tree, forest);
        }
        if (tree1.rank < tree2.rank) {
            Seq forest = PriorityQueueBase.meldUnique(comparator, forest1.tail(), forest2);
            return tree1.appendTo(forest);
        }
        Seq forest = PriorityQueueBase.meldUnique(comparator, forest1, forest2.tail());
        return tree2.appendTo(forest);
    }

    static final class Node<T>
    implements Serializable {
        private static final long serialVersionUID = 1L;
        final T root;
        final int rank;
        final Seq<Node<T>> children;

        private Node(T root, int rank, Seq<Node<T>> children) {
            this.root = root;
            this.rank = rank;
            this.children = children;
        }

        static <T> Node<T> of(T value, int rank, Seq<Node<T>> children) {
            return new Node<T>(value, rank, children);
        }

        Node<T> link(Comparator<? super T> comparator, Node<T> tree) {
            return comparator.compare(this.root, tree.root) <= 0 ? Node.of(this.root, this.rank + 1, tree.appendTo(this.children)) : Node.of(tree.root, tree.rank + 1, this.appendTo(tree.children));
        }

        Node<T> skewLink(Comparator<? super T> comparator, Node<T> left, Node<T> right) {
            if (comparator.compare(left.root, this.root) <= 0 && comparator.compare(left.root, right.root) <= 0) {
                return Node.of(left.root, left.rank + 1, this.appendTo(right.appendTo(left.children)));
            }
            if (comparator.compare(right.root, this.root) <= 0) {
                return Node.of(right.root, right.rank + 1, this.appendTo(left.appendTo(right.children)));
            }
            return Node.of(this.root, left.rank + 1, List.of(left, right));
        }

        Seq<Node<T>> appendTo(Seq<Node<T>> forest) {
            return forest.prepend(this);
        }
    }
}

