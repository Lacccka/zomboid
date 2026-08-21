/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.RedBlackTree;
import io.vavr.control.Option;
import java.io.Serializable;
import java.util.Comparator;
import java.util.NoSuchElementException;

interface RedBlackTreeModule {

    public static final class Empty<T>
    implements RedBlackTree<T>,
    Serializable {
        private static final long serialVersionUID = 1L;
        final Comparator<T> comparator;

        Empty(Comparator<? super T> comparator) {
            this.comparator = comparator;
        }

        @Override
        public RedBlackTree.Color color() {
            return RedBlackTree.Color.BLACK;
        }

        @Override
        public Comparator<T> comparator() {
            return this.comparator;
        }

        @Override
        public boolean contains(T value) {
            return false;
        }

        @Override
        public Empty<T> emptyInstance() {
            return this;
        }

        @Override
        public Option<T> find(T value) {
            return Option.none();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public RedBlackTree<T> left() {
            throw new UnsupportedOperationException("left on empty");
        }

        @Override
        public RedBlackTree<T> right() {
            throw new UnsupportedOperationException("right on empty");
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public T value() {
            throw new NoSuchElementException("value on empty");
        }

        @Override
        public String toString() {
            return "()";
        }
    }

    public static final class Node<T>
    implements RedBlackTree<T>,
    Serializable {
        private static final long serialVersionUID = 1L;
        final RedBlackTree.Color color;
        final int blackHeight;
        final RedBlackTree<T> left;
        final T value;
        final RedBlackTree<T> right;
        final Empty<T> empty;
        final int size;

        Node(RedBlackTree.Color color, int blackHeight, RedBlackTree<T> left, T value, RedBlackTree<T> right, Empty<T> empty) {
            this.color = color;
            this.blackHeight = blackHeight;
            this.left = left;
            this.value = value;
            this.right = right;
            this.empty = empty;
            this.size = left.size() + right.size() + 1;
        }

        @Override
        public RedBlackTree.Color color() {
            return this.color;
        }

        @Override
        public Comparator<T> comparator() {
            return this.empty.comparator;
        }

        @Override
        public boolean contains(T value) {
            int result = this.empty.comparator.compare(value, this.value);
            if (result < 0) {
                return this.left.contains(value);
            }
            if (result > 0) {
                return this.right.contains(value);
            }
            return true;
        }

        @Override
        public Empty<T> emptyInstance() {
            return this.empty;
        }

        @Override
        public Option<T> find(T value) {
            int result = this.empty.comparator.compare(value, this.value);
            if (result < 0) {
                return this.left.find(value);
            }
            if (result > 0) {
                return this.right.find(value);
            }
            return Option.some(this.value);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public RedBlackTree<T> left() {
            return this.left;
        }

        @Override
        public RedBlackTree<T> right() {
            return this.right;
        }

        @Override
        public int size() {
            return this.size;
        }

        @Override
        public T value() {
            return this.value;
        }

        @Override
        public String toString() {
            return this.isLeaf() ? "(" + (Object)((Object)this.color) + ":" + this.value + ")" : Node.toLispString(this);
        }

        private static String toLispString(RedBlackTree<?> tree) {
            if (tree.isEmpty()) {
                return "";
            }
            Node node = (Node)tree;
            String value = (Object)((Object)node.color) + ":" + node.value;
            if (node.isLeaf()) {
                return value;
            }
            String left = node.left.isEmpty() ? "" : " " + Node.toLispString(node.left);
            String right = node.right.isEmpty() ? "" : " " + Node.toLispString(node.right);
            return "(" + value + left + right + ")";
        }

        private boolean isLeaf() {
            return this.left.isEmpty() && this.right.isEmpty();
        }

        Node<T> color(RedBlackTree.Color color) {
            return this.color == color ? this : new Node<T>(color, this.blackHeight, this.left, this.value, this.right, this.empty);
        }

        static <T> RedBlackTree<T> color(RedBlackTree<T> tree, RedBlackTree.Color color) {
            return tree.isEmpty() ? tree : ((Node)tree).color(color);
        }

        private static <T> Node<T> balanceLeft(RedBlackTree.Color color, int blackHeight, RedBlackTree<T> left, T value, RedBlackTree<T> right, Empty<T> empty) {
            if (color == RedBlackTree.Color.BLACK && !left.isEmpty()) {
                Node ln = (Node)left;
                if (ln.color == RedBlackTree.Color.RED) {
                    if (!ln.left.isEmpty()) {
                        Node lln = (Node)ln.left;
                        if (lln.color == RedBlackTree.Color.RED) {
                            Node<T> newLeft = new Node<T>(RedBlackTree.Color.BLACK, blackHeight, lln.left, lln.value, lln.right, empty);
                            Node<T> newRight = new Node<T>(RedBlackTree.Color.BLACK, blackHeight, ln.right, value, right, empty);
                            return new Node<T>(RedBlackTree.Color.RED, blackHeight + 1, newLeft, ln.value, newRight, empty);
                        }
                    }
                    if (!ln.right.isEmpty()) {
                        Node lrn = (Node)ln.right;
                        if (lrn.color == RedBlackTree.Color.RED) {
                            Node<T> newLeft = new Node<T>(RedBlackTree.Color.BLACK, blackHeight, ln.left, ln.value, lrn.left, empty);
                            Node<T> newRight = new Node<T>(RedBlackTree.Color.BLACK, blackHeight, lrn.right, value, right, empty);
                            return new Node<T>(RedBlackTree.Color.RED, blackHeight + 1, newLeft, lrn.value, newRight, empty);
                        }
                    }
                }
            }
            return new Node<T>(color, blackHeight, left, value, right, empty);
        }

        private static <T> Node<T> balanceRight(RedBlackTree.Color color, int blackHeight, RedBlackTree<T> left, T value, RedBlackTree<T> right, Empty<T> empty) {
            if (color == RedBlackTree.Color.BLACK && !right.isEmpty()) {
                Node rn = (Node)right;
                if (rn.color == RedBlackTree.Color.RED) {
                    if (!rn.right.isEmpty()) {
                        Node rrn = (Node)rn.right;
                        if (rrn.color == RedBlackTree.Color.RED) {
                            Node<T> newLeft = new Node<T>(RedBlackTree.Color.BLACK, blackHeight, left, value, rn.left, empty);
                            Node<T> newRight = new Node<T>(RedBlackTree.Color.BLACK, blackHeight, rrn.left, rrn.value, rrn.right, empty);
                            return new Node<T>(RedBlackTree.Color.RED, blackHeight + 1, newLeft, rn.value, newRight, empty);
                        }
                    }
                    if (!rn.left.isEmpty()) {
                        Node rln = (Node)rn.left;
                        if (rln.color == RedBlackTree.Color.RED) {
                            Node<T> newLeft = new Node<T>(RedBlackTree.Color.BLACK, blackHeight, left, value, rln.left, empty);
                            Node<T> newRight = new Node<T>(RedBlackTree.Color.BLACK, blackHeight, rln.right, rn.value, rn.right, empty);
                            return new Node<T>(RedBlackTree.Color.RED, blackHeight + 1, newLeft, rln.value, newRight, empty);
                        }
                    }
                }
            }
            return new Node<T>(color, blackHeight, left, value, right, empty);
        }

        private static <T> Tuple2<? extends RedBlackTree<T>, Boolean> blackify(RedBlackTree<T> tree) {
            if (tree instanceof Node) {
                Node node = (Node)tree;
                if (node.color == RedBlackTree.Color.RED) {
                    return Tuple.of(node.color(RedBlackTree.Color.BLACK), false);
                }
            }
            return Tuple.of(tree, true);
        }

        static <T> Tuple2<? extends RedBlackTree<T>, Boolean> delete(RedBlackTree<T> tree, T value) {
            if (tree.isEmpty()) {
                return Tuple.of(tree, false);
            }
            Node node = (Node)tree;
            int comparison = node.comparator().compare(value, node.value);
            if (comparison < 0) {
                Tuple2<RedBlackTree<T>, Boolean> deleted = Node.delete(node.left, value);
                RedBlackTree l = (RedBlackTree)deleted._1;
                boolean d = (Boolean)deleted._2;
                if (d) {
                    return Node.unbalancedRight(node.color, node.blackHeight - 1, l, node.value, node.right, node.empty);
                }
                Node<T> newNode = new Node<T>(node.color, node.blackHeight, l, node.value, node.right, node.empty);
                return Tuple.of(newNode, false);
            }
            if (comparison > 0) {
                Tuple2<RedBlackTree<T>, Boolean> deleted = Node.delete(node.right, value);
                RedBlackTree r = (RedBlackTree)deleted._1;
                boolean d = (Boolean)deleted._2;
                if (d) {
                    return Node.unbalancedLeft(node.color, node.blackHeight - 1, node.left, node.value, r, node.empty);
                }
                Node<T> newNode = new Node<T>(node.color, node.blackHeight, node.left, node.value, r, node.empty);
                return Tuple.of(newNode, false);
            }
            if (node.right.isEmpty()) {
                if (node.color == RedBlackTree.Color.BLACK) {
                    return Node.blackify(node.left);
                }
                return Tuple.of(node.left, false);
            }
            Node nodeRight = (Node)node.right;
            Tuple3<RedBlackTree<T>, Boolean, T> newRight = Node.deleteMin(nodeRight);
            RedBlackTree r = (RedBlackTree)newRight._1;
            boolean d = (Boolean)newRight._2;
            Object m = newRight._3;
            if (d) {
                return Node.unbalancedLeft(node.color, node.blackHeight - 1, node.left, m, r, node.empty);
            }
            Node<T> newNode = new Node<T>(node.color, node.blackHeight, node.left, m, r, node.empty);
            return Tuple.of(newNode, false);
        }

        private static <T> Tuple3<? extends RedBlackTree<T>, Boolean, T> deleteMin(Node<T> node) {
            if (node.color() == RedBlackTree.Color.BLACK && node.left().isEmpty() && node.right.isEmpty()) {
                return Tuple.of(node.empty, true, node.value());
            }
            if (node.color() == RedBlackTree.Color.BLACK && node.left().isEmpty() && node.right().color() == RedBlackTree.Color.RED) {
                return Tuple.of(((Node)node.right()).color(RedBlackTree.Color.BLACK), false, node.value());
            }
            if (node.color() == RedBlackTree.Color.RED && node.left().isEmpty()) {
                return Tuple.of(node.right(), false, node.value());
            }
            Node nodeLeft = (Node)node.left;
            Tuple3<RedBlackTree<T>, Boolean, T> newNode = Node.deleteMin(nodeLeft);
            RedBlackTree l = (RedBlackTree)newNode._1;
            boolean deleted = (Boolean)newNode._2;
            Object m = newNode._3;
            if (deleted) {
                Tuple2<Node<T>, Boolean> tD = Node.unbalancedRight(node.color, node.blackHeight - 1, l, node.value, node.right, node.empty);
                return Tuple.of(tD._1, tD._2, m);
            }
            Node<T> tD = new Node<T>(node.color, node.blackHeight, l, node.value, node.right, node.empty);
            return Tuple.of(tD, false, m);
        }

        static <T> Node<T> insert(RedBlackTree<T> tree, T value) {
            if (tree.isEmpty()) {
                Empty empty = (Empty)tree;
                return new Node<T>(RedBlackTree.Color.RED, 1, empty, value, empty, empty);
            }
            Node<T> node = (Node<T>)tree;
            int comparison = node.comparator().compare(value, node.value);
            if (comparison < 0) {
                Node<T> newLeft = Node.insert(node.left, value);
                return newLeft == node.left ? node : Node.balanceLeft(node.color, node.blackHeight, newLeft, node.value, node.right, node.empty);
            }
            if (comparison > 0) {
                Node<T> newRight = Node.insert(node.right, value);
                return newRight == node.right ? node : Node.balanceRight(node.color, node.blackHeight, node.left, node.value, newRight, node.empty);
            }
            return new Node<T>(node.color, node.blackHeight, node.left, value, node.right, node.empty);
        }

        private static boolean isRed(RedBlackTree<?> tree) {
            return !tree.isEmpty() && ((Node)tree).color == RedBlackTree.Color.RED;
        }

        static <T> RedBlackTree<T> join(RedBlackTree<T> t1, T value, RedBlackTree<T> t2) {
            if (t1.isEmpty()) {
                return t2.insert(value);
            }
            if (t2.isEmpty()) {
                return t1.insert(value);
            }
            Node n1 = (Node)t1;
            Node n2 = (Node)t2;
            int comparison = n1.blackHeight - n2.blackHeight;
            if (comparison < 0) {
                return Node.joinLT(n1, value, n2, n1.blackHeight).color(RedBlackTree.Color.BLACK);
            }
            if (comparison > 0) {
                return Node.joinGT(n1, value, n2, n2.blackHeight).color(RedBlackTree.Color.BLACK);
            }
            return new Node<T>(RedBlackTree.Color.BLACK, n1.blackHeight + 1, n1, value, n2, n1.empty);
        }

        private static <T> Node<T> joinGT(Node<T> n1, T value, Node<T> n2, int h2) {
            if (n1.blackHeight == h2) {
                return new Node<T>(RedBlackTree.Color.RED, h2 + 1, n1, value, n2, n1.empty);
            }
            Node<T> node = Node.joinGT((Node)n1.right, value, n2, h2);
            return Node.balanceRight(n1.color, n1.blackHeight, n1.left, n1.value, node, n2.empty);
        }

        private static <T> Node<T> joinLT(Node<T> n1, T value, Node<T> n2, int h1) {
            if (n2.blackHeight == h1) {
                return new Node<T>(RedBlackTree.Color.RED, h1 + 1, n1, value, n2, n1.empty);
            }
            Node<T> node = Node.joinLT(n1, value, (Node)n2.left, h1);
            return Node.balanceLeft(n2.color, n2.blackHeight, node, n2.value, n2.right, n2.empty);
        }

        static <T> RedBlackTree<T> merge(RedBlackTree<T> t1, RedBlackTree<T> t2) {
            if (t1.isEmpty()) {
                return t2;
            }
            if (t2.isEmpty()) {
                return t1;
            }
            Node n1 = (Node)t1;
            Node n2 = (Node)t2;
            int comparison = n1.blackHeight - n2.blackHeight;
            if (comparison < 0) {
                Node<T> node = Node.mergeLT(n1, n2, n1.blackHeight);
                return Node.color(node, RedBlackTree.Color.BLACK);
            }
            if (comparison > 0) {
                Node<T> node = Node.mergeGT(n1, n2, n2.blackHeight);
                return Node.color(node, RedBlackTree.Color.BLACK);
            }
            Node<T> node = Node.mergeEQ(n1, n2);
            return Node.color(node, RedBlackTree.Color.BLACK);
        }

        private static <T> Node<T> mergeEQ(Node<T> n1, Node<T> n2) {
            int h2;
            T m = Node.minimum(n2);
            RedBlackTree t2 = (RedBlackTree)Node.deleteMin(n2)._1;
            int n = h2 = t2.isEmpty() ? 0 : ((Node)t2).blackHeight;
            if (n1.blackHeight == h2) {
                return new Node<T>(RedBlackTree.Color.RED, n1.blackHeight + 1, n1, m, t2, n1.empty);
            }
            if (Node.isRed(n1.left)) {
                Node<T> node = new Node<T>(RedBlackTree.Color.BLACK, n1.blackHeight, n1.right, m, t2, n1.empty);
                return new Node<T>(RedBlackTree.Color.RED, n1.blackHeight + 1, Node.color(n1.left, RedBlackTree.Color.BLACK), n1.value, node, n1.empty);
            }
            if (Node.isRed(n1.right)) {
                RedBlackTree<T> rl = ((Node)n1.right).left;
                T rx = ((Node)n1.right).value;
                RedBlackTree<T> rr = ((Node)n1.right).right;
                Node<T> left = new Node<T>(RedBlackTree.Color.RED, n1.blackHeight, n1.left, n1.value, rl, n1.empty);
                Node<T> right = new Node<T>(RedBlackTree.Color.RED, n1.blackHeight, rr, m, t2, n1.empty);
                return new Node<T>(RedBlackTree.Color.BLACK, n1.blackHeight, left, rx, right, n1.empty);
            }
            return new Node<T>(RedBlackTree.Color.BLACK, n1.blackHeight, n1.color(RedBlackTree.Color.RED), m, t2, n1.empty);
        }

        private static <T> Node<T> mergeGT(Node<T> n1, Node<T> n2, int h2) {
            if (n1.blackHeight == h2) {
                return Node.mergeEQ(n1, n2);
            }
            Node<T> node = Node.mergeGT((Node)n1.right, n2, h2);
            return Node.balanceRight(n1.color, n1.blackHeight, n1.left, n1.value, node, n1.empty);
        }

        private static <T> Node<T> mergeLT(Node<T> n1, Node<T> n2, int h1) {
            if (n2.blackHeight == h1) {
                return Node.mergeEQ(n1, n2);
            }
            Node<T> node = Node.mergeLT(n1, (Node)n2.left, h1);
            return Node.balanceLeft(n2.color, n2.blackHeight, node, n2.value, n2.right, n2.empty);
        }

        static <T> T maximum(Node<T> node) {
            Node curr = node;
            while (!curr.right.isEmpty()) {
                curr = (Node)curr.right;
            }
            return curr.value;
        }

        static <T> T minimum(Node<T> node) {
            Node curr = node;
            while (!curr.left.isEmpty()) {
                curr = (Node)curr.left;
            }
            return curr.value;
        }

        static <T> Tuple2<RedBlackTree<T>, RedBlackTree<T>> split(RedBlackTree<T> tree, T value) {
            if (tree.isEmpty()) {
                return Tuple.of(tree, tree);
            }
            Node node = (Node)tree;
            int comparison = node.comparator().compare(value, node.value);
            if (comparison < 0) {
                Tuple2<RedBlackTree<T>, RedBlackTree<T>> split = Node.split(node.left, value);
                return Tuple.of(split._1, Node.join((RedBlackTree)split._2, node.value, Node.color(node.right, RedBlackTree.Color.BLACK)));
            }
            if (comparison > 0) {
                Tuple2<RedBlackTree<T>, RedBlackTree<T>> split = Node.split(node.right, value);
                return Tuple.of(Node.join(Node.color(node.left, RedBlackTree.Color.BLACK), node.value, (RedBlackTree)split._1), split._2);
            }
            return Tuple.of(Node.color(node.left, RedBlackTree.Color.BLACK), Node.color(node.right, RedBlackTree.Color.BLACK));
        }

        private static <T> Tuple2<Node<T>, Boolean> unbalancedLeft(RedBlackTree.Color color, int blackHeight, RedBlackTree<T> left, T value, RedBlackTree<T> right, Empty<T> empty) {
            if (!left.isEmpty()) {
                Node ln = (Node)left;
                if (ln.color == RedBlackTree.Color.BLACK) {
                    Node<T> newNode = Node.balanceLeft(RedBlackTree.Color.BLACK, blackHeight, ln.color(RedBlackTree.Color.RED), value, right, empty);
                    return Tuple.of(newNode, color == RedBlackTree.Color.BLACK);
                }
                if (color == RedBlackTree.Color.BLACK && !ln.right.isEmpty()) {
                    Node lrn = (Node)ln.right;
                    if (lrn.color == RedBlackTree.Color.BLACK) {
                        Node<T> newRightNode = Node.balanceLeft(RedBlackTree.Color.BLACK, blackHeight, lrn.color(RedBlackTree.Color.RED), value, right, empty);
                        Node<T> newNode = new Node<T>(RedBlackTree.Color.BLACK, ln.blackHeight, ln.left, ln.value, newRightNode, empty);
                        return Tuple.of(newNode, false);
                    }
                }
            }
            throw new IllegalStateException("unbalancedLeft(" + (Object)((Object)color) + ", " + blackHeight + ", " + left + ", " + value + ", " + right + ")");
        }

        private static <T> Tuple2<Node<T>, Boolean> unbalancedRight(RedBlackTree.Color color, int blackHeight, RedBlackTree<T> left, T value, RedBlackTree<T> right, Empty<T> empty) {
            if (!right.isEmpty()) {
                Node rn = (Node)right;
                if (rn.color == RedBlackTree.Color.BLACK) {
                    Node<T> newNode = Node.balanceRight(RedBlackTree.Color.BLACK, blackHeight, left, value, rn.color(RedBlackTree.Color.RED), empty);
                    return Tuple.of(newNode, color == RedBlackTree.Color.BLACK);
                }
                if (color == RedBlackTree.Color.BLACK && !rn.left.isEmpty()) {
                    Node rln = (Node)rn.left;
                    if (rln.color == RedBlackTree.Color.BLACK) {
                        Node<T> newLeftNode = Node.balanceRight(RedBlackTree.Color.BLACK, blackHeight, left, value, rln.color(RedBlackTree.Color.RED), empty);
                        Node<T> newNode = new Node<T>(RedBlackTree.Color.BLACK, rn.blackHeight, newLeftNode, rn.value, rn.right, empty);
                        return Tuple.of(newNode, false);
                    }
                }
            }
            throw new IllegalStateException("unbalancedRight(" + (Object)((Object)color) + ", " + blackHeight + ", " + left + ", " + value + ", " + right + ")");
        }
    }
}

