/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.collection.ArrayType;
import io.vavr.collection.Collections;
import io.vavr.collection.Iterator;
import io.vavr.collection.LeafVisitor;
import io.vavr.collection.NodeModifier;
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

final class BitMappedTrie<T>
implements Serializable {
    static final int BRANCHING_BASE = 5;
    static final int BRANCHING_FACTOR = 32;
    static final int BRANCHING_MASK = 31;
    private static final long serialVersionUID = 1L;
    private static final BitMappedTrie<?> EMPTY = new BitMappedTrie(ArrayType.obj(), ArrayType.obj().empty(), 0, 0, 0);
    final ArrayType<T> type;
    private final Object array;
    private final int offset;
    private final int length;
    private final int depthShift;

    static int firstDigit(int num, int depthShift) {
        return num >> depthShift;
    }

    static int digit(int num, int depthShift) {
        return BitMappedTrie.lastDigit(BitMappedTrie.firstDigit(num, depthShift));
    }

    static int lastDigit(int num) {
        return num & 0x1F;
    }

    static <T> BitMappedTrie<T> empty() {
        return EMPTY;
    }

    private BitMappedTrie(ArrayType<T> type, Object array, int offset, int length, int depthShift) {
        this.type = type;
        this.array = array;
        this.offset = offset;
        this.length = length;
        this.depthShift = depthShift;
    }

    private static int treeSize(int branchCount, int depthShift) {
        int fullBranchSize = 1 << depthShift;
        return branchCount * fullBranchSize;
    }

    static <T> BitMappedTrie<T> ofAll(Object array) {
        ArrayType type = ArrayType.of(array);
        int size = type.lengthOf(array);
        return size == 0 ? BitMappedTrie.empty() : BitMappedTrie.ofAll(array, type, size);
    }

    private static <T> BitMappedTrie<T> ofAll(Object array, ArrayType<T> type, int size) {
        int shift = 0;
        ArrayType<T> t = type;
        while (t.lengthOf(array) > 32) {
            array = t.grouped(array, 32);
            t = ArrayType.obj();
            shift += 5;
        }
        return new BitMappedTrie<T>(type, array, 0, size, shift);
    }

    private BitMappedTrie<T> boxed() {
        return this.map(Function.identity());
    }

    BitMappedTrie<T> prependAll(Iterable<? extends T> iterable) {
        Collections.IterableWithSize<T> iter = Collections.withSize(iterable);
        try {
            return this.prepend(iter.reverseIterator(), iter.size());
        }
        catch (ClassCastException ignored) {
            return super.prepend(iter.reverseIterator(), iter.size());
        }
    }

    private BitMappedTrie<T> prepend(java.util.Iterator<? extends T> iterator2, int size) {
        BitMappedTrie<? extends T> result = this;
        while (size > 0) {
            Object array = result.array;
            int shift = result.depthShift;
            int offset = result.offset;
            if (result.isFullLeft()) {
                array = ArrayType.obj().copyUpdate(ArrayType.obj().empty(), 31, array);
                offset = BitMappedTrie.treeSize(31, shift += 5);
            }
            int index = offset - 1;
            int delta = Math.min(size, BitMappedTrie.lastDigit(index) + 1);
            size -= delta;
            array = result.modify(array, shift, index, NodeModifier.COPY_NODE, this.prependToLeaf(iterator2));
            result = new BitMappedTrie<T>(this.type, array, offset - delta, result.length + delta, shift);
        }
        return result;
    }

    private boolean isFullLeft() {
        return this.offset == 0;
    }

    private NodeModifier prependToLeaf(java.util.Iterator<? extends T> iterator2) {
        return (array, index) -> {
            Object copy = this.type.copy(array, 32);
            while (iterator2.hasNext() && index >= 0) {
                this.type.setAt(copy, index--, iterator2.next());
            }
            return copy;
        };
    }

    BitMappedTrie<T> appendAll(Iterable<? extends T> iterable) {
        Collections.IterableWithSize<T> iter = Collections.withSize(iterable);
        try {
            return this.append(iter.iterator(), iter.size());
        }
        catch (ClassCastException ignored) {
            return super.append(iter.iterator(), iter.size());
        }
    }

    private BitMappedTrie<T> append(java.util.Iterator<? extends T> iterator2, int size) {
        BitMappedTrie<? extends T> result = this;
        while (size > 0) {
            Object array = result.array;
            int shift = result.depthShift;
            if (result.isFullRight()) {
                array = ArrayType.obj().asArray(array);
                shift += 5;
            }
            int index = this.offset + result.length;
            int leafSpace = BitMappedTrie.lastDigit(index);
            int delta = Math.min(size, 32 - leafSpace);
            size -= delta;
            array = result.modify(array, shift, index, NodeModifier.COPY_NODE, this.appendToLeaf(iterator2, leafSpace + delta));
            result = new BitMappedTrie<T>(this.type, array, this.offset, result.length + delta, shift);
        }
        return result;
    }

    private boolean isFullRight() {
        return this.offset + this.length + 1 > BitMappedTrie.treeSize(32, this.depthShift);
    }

    private NodeModifier appendToLeaf(java.util.Iterator<? extends T> iterator2, int leafSize) {
        return (array, index) -> {
            Object copy = this.type.copy(array, leafSize);
            while (iterator2.hasNext() && index < leafSize) {
                this.type.setAt(copy, index++, iterator2.next());
            }
            return copy;
        };
    }

    BitMappedTrie<T> update(int index, T element) {
        try {
            Object root = this.modify(this.array, this.depthShift, this.offset + index, NodeModifier.COPY_NODE, this.updateLeafWith(this.type, element));
            return new BitMappedTrie<T>(this.type, root, this.offset, this.length, this.depthShift);
        }
        catch (ClassCastException ignored) {
            return this.boxed().update(index, element);
        }
    }

    private NodeModifier updateLeafWith(ArrayType<T> type, T element) {
        return (a, i) -> type.copyUpdate(a, i, element);
    }

    BitMappedTrie<T> drop(int n) {
        if (n <= 0) {
            return this;
        }
        if (n >= this.length) {
            return BitMappedTrie.empty();
        }
        int index = this.offset + n;
        Object root = this.arePointingToSameLeaf(0, n) ? this.array : this.modify(this.array, this.depthShift, index, ArrayType.obj()::copyDrop, NodeModifier.IDENTITY);
        return BitMappedTrie.collapsed(this.type, root, index, this.length - n, this.depthShift);
    }

    BitMappedTrie<T> take(int n) {
        if (n >= this.length) {
            return this;
        }
        if (n <= 0) {
            return BitMappedTrie.empty();
        }
        int index = n - 1;
        Object root = this.arePointingToSameLeaf(index, this.length - 1) ? this.array : this.modify(this.array, this.depthShift, this.offset + index, ArrayType.obj()::copyTake, NodeModifier.IDENTITY);
        return BitMappedTrie.collapsed(this.type, root, this.offset, n, this.depthShift);
    }

    private boolean arePointingToSameLeaf(int i, int j) {
        return BitMappedTrie.firstDigit(this.offset + i, 5) == BitMappedTrie.firstDigit(this.offset + j, 5);
    }

    private static <T> BitMappedTrie<T> collapsed(ArrayType<T> type, Object array, int offset, int length, int shift) {
        int skippedElements;
        while (shift > 0 && (skippedElements = ArrayType.obj().lengthOf(array) - 1) == BitMappedTrie.digit(offset, shift)) {
            array = ArrayType.obj().getAt(array, skippedElements);
            offset -= BitMappedTrie.treeSize(skippedElements, shift);
            shift -= 5;
        }
        return new BitMappedTrie<T>(type, array, offset, length, shift);
    }

    private Object modify(Object root, int depthShift, int index, NodeModifier node, NodeModifier leaf) {
        return depthShift == 0 ? leaf.apply(root, index) : this.modifyNonLeaf(root, depthShift, index, node, leaf);
    }

    private Object modifyNonLeaf(Object root, int depthShift, int index, NodeModifier node, NodeModifier leaf) {
        int previousIndex = BitMappedTrie.firstDigit(index, depthShift);
        Object array = root = node.apply(root, previousIndex);
        for (int shift = depthShift - 5; shift >= 5; shift -= 5) {
            int prev = previousIndex;
            previousIndex = BitMappedTrie.digit(index, shift);
            array = this.setNewNode(node, prev, array, previousIndex);
        }
        Object newLeaf = leaf.apply(ArrayType.obj().getAt(array, previousIndex), BitMappedTrie.lastDigit(index));
        ArrayType.obj().setAt(array, previousIndex, newLeaf);
        return root;
    }

    private Object setNewNode(NodeModifier node, int previousIndex, Object array, int offset) {
        Object previous = ArrayType.obj().getAt(array, previousIndex);
        Object newNode = node.apply(previous, offset);
        ArrayType.obj().setAt(array, previousIndex, newNode);
        return newNode;
    }

    T get(int index) {
        Object leaf = this.getLeaf(index);
        int leafIndex = BitMappedTrie.lastDigit(this.offset + index);
        return this.type.getAt(leaf, leafIndex);
    }

    Object getLeaf(int index) {
        if (this.depthShift == 0) {
            return this.array;
        }
        return this.getLeafGeneral(index);
    }

    private Object getLeafGeneral(int index) {
        Object leaf = ArrayType.obj().getAt(this.array, BitMappedTrie.firstDigit(index += this.offset, this.depthShift));
        for (int shift = this.depthShift - 5; shift > 0; shift -= 5) {
            leaf = ArrayType.obj().getAt(leaf, BitMappedTrie.digit(index, shift));
        }
        return leaf;
    }

    Iterator<T> iterator() {
        return new Iterator<T>(){
            private final int globalLength;
            private int globalIndex;
            private int index;
            private Object leaf;
            private int length;
            {
                this.globalLength = BitMappedTrie.this.length;
                this.globalIndex = 0;
                this.index = BitMappedTrie.lastDigit(BitMappedTrie.this.offset);
                this.leaf = BitMappedTrie.this.getLeaf(this.globalIndex);
                this.length = BitMappedTrie.this.type.lengthOf(this.leaf);
            }

            @Override
            public boolean hasNext() {
                return this.globalIndex < this.globalLength;
            }

            @Override
            public T next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException("next() on empty iterator");
                }
                if (this.index == this.length) {
                    this.setCurrentArray();
                }
                Object next = BitMappedTrie.this.type.getAt(this.leaf, this.index);
                ++this.index;
                ++this.globalIndex;
                return next;
            }

            private void setCurrentArray() {
                this.index = 0;
                this.leaf = BitMappedTrie.this.getLeaf(this.globalIndex);
                this.length = BitMappedTrie.this.type.lengthOf(this.leaf);
            }
        };
    }

    <T2> int visit(LeafVisitor<T2> visitor) {
        int end;
        int globalIndex = 0;
        int start = BitMappedTrie.lastDigit(this.offset);
        for (int index = 0; index < this.length; index += end - start) {
            Object leaf = this.getLeaf(index);
            end = this.getMin(start, index, leaf);
            globalIndex = visitor.visit(globalIndex, leaf, start, end);
            start = 0;
        }
        return globalIndex;
    }

    private int getMin(int start, int index, Object leaf) {
        return Math.min(this.type.lengthOf(leaf), start + this.length - index);
    }

    BitMappedTrie<T> filter(Predicate<? super T> predicate) {
        Object results = this.type.newInstance(this.length());
        int length = this.visit((index, leaf, start, end) -> this.filter(predicate, results, index, leaf, start, end));
        return this.length == length ? this : BitMappedTrie.ofAll(this.type.copyRange(results, 0, length));
    }

    private int filter(Predicate<? super T> predicate, Object results, int index, T leaf, int start, int end) {
        for (int i = start; i < end; ++i) {
            T value = this.type.getAt(leaf, i);
            if (!predicate.test(value)) continue;
            this.type.setAt(results, index++, value);
        }
        return index;
    }

    <U> BitMappedTrie<U> map(Function<? super T, ? extends U> mapper) {
        Object results = ArrayType.obj().newInstance(this.length);
        this.visit((index, leaf, start, end) -> this.map(mapper, results, index, leaf, start, end));
        return BitMappedTrie.ofAll(results);
    }

    private <U> int map(Function<? super T, ? extends U> mapper, Object results, int index, T leaf, int start, int end) {
        for (int i = start; i < end; ++i) {
            ArrayType.obj().setAt(results, index++, mapper.apply(this.type.getAt(leaf, i)));
        }
        return index;
    }

    int length() {
        return this.length;
    }
}

