/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Tuple2;
import io.vavr.collection.AbstractIterator;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.collection.RedBlackTreeModule;
import io.vavr.control.Option;
import java.util.Comparator;
import java.util.Objects;

interface RedBlackTree<T>
extends Iterable<T> {
    public static <T> RedBlackTree<T> empty(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return new RedBlackTreeModule.Empty<T>(comparator);
    }

    public static <T> RedBlackTree<T> of(Comparator<? super T> comparator, T value) {
        Objects.requireNonNull(comparator, "comparator is null");
        RedBlackTreeModule.Empty<? super T> empty = new RedBlackTreeModule.Empty<T>(comparator);
        return new RedBlackTreeModule.Node<T>(Color.BLACK, 1, empty, value, empty, empty);
    }

    @SafeVarargs
    public static <T> RedBlackTree<T> of(Comparator<? super T> comparator, T ... values2) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(values2, "values is null");
        RedBlackTree<T> tree = RedBlackTree.empty(comparator);
        for (T value : values2) {
            tree = tree.insert(value);
        }
        return tree;
    }

    public static <T> RedBlackTree<T> ofAll(Comparator<? super T> comparator, Iterable<? extends T> values2) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(values2, "values is null");
        if (values2 instanceof RedBlackTree && ((RedBlackTree)values2).comparator() == comparator) {
            return (RedBlackTree)values2;
        }
        RedBlackTree<T> tree = RedBlackTree.empty(comparator);
        for (T value : values2) {
            tree = tree.insert(value);
        }
        return tree;
    }

    default public RedBlackTree<T> insert(T value) {
        return RedBlackTreeModule.Node.insert(this, value).color(Color.BLACK);
    }

    public Color color();

    public Comparator<T> comparator();

    public boolean contains(T var1);

    default public RedBlackTree<T> delete(T value) {
        RedBlackTree tree = (RedBlackTree)RedBlackTreeModule.Node.delete(this, value)._1;
        return RedBlackTreeModule.Node.color(tree, Color.BLACK);
    }

    default public RedBlackTree<T> difference(RedBlackTree<T> tree) {
        Objects.requireNonNull(tree, "tree is null");
        if (this.isEmpty() || tree.isEmpty()) {
            return this;
        }
        RedBlackTreeModule.Node that = (RedBlackTreeModule.Node)tree;
        Tuple2 split = RedBlackTreeModule.Node.split(this, that.value);
        return RedBlackTreeModule.Node.merge(((RedBlackTree)split._1).difference(that.left), ((RedBlackTree)split._2).difference(that.right));
    }

    public RedBlackTree<T> emptyInstance();

    public Option<T> find(T var1);

    default public RedBlackTree<T> intersection(RedBlackTree<T> tree) {
        Objects.requireNonNull(tree, "tree is null");
        if (this.isEmpty()) {
            return this;
        }
        if (tree.isEmpty()) {
            return tree;
        }
        RedBlackTreeModule.Node that = (RedBlackTreeModule.Node)tree;
        Tuple2 split = RedBlackTreeModule.Node.split(this, that.value);
        if (this.contains(that.value)) {
            return RedBlackTreeModule.Node.join(((RedBlackTree)split._1).intersection(that.left), that.value, ((RedBlackTree)split._2).intersection(that.right));
        }
        return RedBlackTreeModule.Node.merge(((RedBlackTree)split._1).intersection(that.left), ((RedBlackTree)split._2).intersection(that.right));
    }

    public boolean isEmpty();

    public RedBlackTree<T> left();

    default public Option<T> max() {
        return this.isEmpty() ? Option.none() : Option.some(RedBlackTreeModule.Node.maximum((RedBlackTreeModule.Node)this));
    }

    default public Option<T> min() {
        return this.isEmpty() ? Option.none() : Option.some(RedBlackTreeModule.Node.minimum((RedBlackTreeModule.Node)this));
    }

    public RedBlackTree<T> right();

    public int size();

    default public RedBlackTree<T> union(RedBlackTree<T> tree) {
        Objects.requireNonNull(tree, "tree is null");
        if (tree.isEmpty()) {
            return this;
        }
        RedBlackTreeModule.Node that = (RedBlackTreeModule.Node)tree;
        if (this.isEmpty()) {
            return that.color(Color.BLACK);
        }
        Tuple2 split = RedBlackTreeModule.Node.split(this, that.value);
        return RedBlackTreeModule.Node.join(((RedBlackTree)split._1).union(that.left), that.value, ((RedBlackTree)split._2).union(that.right));
    }

    public T value();

    @Override
    default public Iterator<T> iterator() {
        if (this.isEmpty()) {
            return Iterator.empty();
        }
        final RedBlackTreeModule.Node that = (RedBlackTreeModule.Node)this;
        return new AbstractIterator<T>(){
            List<RedBlackTreeModule.Node<T>> stack;
            {
                this.stack = this.pushLeftChildren(List.empty(), that);
            }

            @Override
            public boolean hasNext() {
                return !this.stack.isEmpty();
            }

            @Override
            public T getNext() {
                Tuple2 result = this.stack.pop2();
                RedBlackTreeModule.Node node = (RedBlackTreeModule.Node)result._1;
                this.stack = node.right.isEmpty() ? (List)result._2 : this.pushLeftChildren((List)result._2, (RedBlackTreeModule.Node)node.right);
                return ((RedBlackTreeModule.Node)result._1).value;
            }

            private List<RedBlackTreeModule.Node<T>> pushLeftChildren(List<RedBlackTreeModule.Node<T>> initialStack, RedBlackTreeModule.Node<T> that2) {
                List stack = initialStack;
                RedBlackTree tree = that2;
                while (!tree.isEmpty()) {
                    RedBlackTreeModule.Node node = tree;
                    stack = stack.push(node);
                    tree = node.left;
                }
                return stack;
            }
        };
    }

    public String toString();

    public static enum Color {
        RED,
        BLACK;


        public String toString() {
            return this == RED ? "R" : "B";
        }
    }
}

