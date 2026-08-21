/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.AbstractIterator;
import io.vavr.collection.HashArrayMappedTrie;
import io.vavr.collection.Iterator;
import io.vavr.control.Option;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

interface HashArrayMappedTrieModule {

    public static final class ArrayNode<K, V>
    extends AbstractNode<K, V>
    implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Object[] subNodes;
        private final int count;
        private final int size;

        ArrayNode(int count, int size, Object[] subNodes) {
            this.subNodes = subNodes;
            this.count = count;
            this.size = size;
        }

        @Override
        Option<V> lookup(int shift, int keyHash, K key) {
            int frag = ArrayNode.hashFragment(shift, keyHash);
            AbstractNode child = (AbstractNode)this.subNodes[frag];
            return child.lookup(shift + 5, keyHash, key);
        }

        @Override
        V lookup(int shift, int keyHash, K key, V defaultValue) {
            int frag = ArrayNode.hashFragment(shift, keyHash);
            AbstractNode child = (AbstractNode)this.subNodes[frag];
            return child.lookup(shift + 5, keyHash, key, defaultValue);
        }

        @Override
        AbstractNode<K, V> modify(int shift, int keyHash, K key, V value, Action action) {
            int frag = ArrayNode.hashFragment(shift, keyHash);
            AbstractNode child = (AbstractNode)this.subNodes[frag];
            AbstractNode<K, V> newChild = child.modify(shift + 5, keyHash, key, value, action);
            if (child.isEmpty() && !newChild.isEmpty()) {
                return new ArrayNode<K, V>(this.count + 1, this.size + newChild.size(), ArrayNode.update(this.subNodes, frag, newChild));
            }
            if (!child.isEmpty() && newChild.isEmpty()) {
                if (this.count - 1 <= 8) {
                    return this.pack(frag, this.subNodes);
                }
                return new ArrayNode<K, V>(this.count - 1, this.size - child.size(), ArrayNode.update(this.subNodes, frag, EmptyNode.instance()));
            }
            return new ArrayNode<K, V>(this.count, this.size - child.size() + newChild.size(), ArrayNode.update(this.subNodes, frag, newChild));
        }

        private IndexedNode<K, V> pack(int idx, Object[] elements) {
            Object[] arr = new Object[this.count - 1];
            int bitmap = 0;
            int size = 0;
            int ptr = 0;
            for (int i = 0; i < 32; ++i) {
                AbstractNode elem = (AbstractNode)elements[i];
                if (i == idx || elem.isEmpty()) continue;
                size += elem.size();
                arr[ptr++] = elem;
                bitmap |= 1 << i;
            }
            return new IndexedNode(bitmap, size, arr);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public int size() {
            return this.size;
        }
    }

    public static final class IndexedNode<K, V>
    extends AbstractNode<K, V>
    implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int bitmap;
        private final int size;
        private final Object[] subNodes;

        IndexedNode(int bitmap, int size, Object[] subNodes) {
            this.bitmap = bitmap;
            this.size = size;
            this.subNodes = subNodes;
        }

        @Override
        Option<V> lookup(int shift, int keyHash, K key) {
            int frag = IndexedNode.hashFragment(shift, keyHash);
            int bit = IndexedNode.toBitmap(frag);
            if ((this.bitmap & bit) != 0) {
                AbstractNode n = (AbstractNode)this.subNodes[IndexedNode.fromBitmap(this.bitmap, bit)];
                return n.lookup(shift + 5, keyHash, key);
            }
            return Option.none();
        }

        @Override
        V lookup(int shift, int keyHash, K key, V defaultValue) {
            int frag = IndexedNode.hashFragment(shift, keyHash);
            int bit = IndexedNode.toBitmap(frag);
            if ((this.bitmap & bit) != 0) {
                AbstractNode n = (AbstractNode)this.subNodes[IndexedNode.fromBitmap(this.bitmap, bit)];
                return n.lookup(shift + 5, keyHash, key, defaultValue);
            }
            return defaultValue;
        }

        @Override
        AbstractNode<K, V> modify(int shift, int keyHash, K key, V value, Action action) {
            int newBitmap;
            boolean added;
            int frag = IndexedNode.hashFragment(shift, keyHash);
            int bit = IndexedNode.toBitmap(frag);
            int index = IndexedNode.fromBitmap(this.bitmap, bit);
            int mask = this.bitmap;
            boolean exists = (mask & bit) != 0;
            AbstractNode atIndx = exists ? (AbstractNode)this.subNodes[index] : null;
            AbstractNode<K, V> child = exists ? atIndx.modify(shift + 5, keyHash, key, value, action) : EmptyNode.instance().modify(shift + 5, keyHash, key, value, action);
            boolean removed = exists && child.isEmpty();
            boolean bl = added = !exists && !child.isEmpty();
            int n = removed ? mask & ~bit : (newBitmap = added ? mask | bit : mask);
            if (newBitmap == 0) {
                return EmptyNode.instance();
            }
            if (removed) {
                if (this.subNodes.length <= 2 && this.subNodes[index ^ 1] instanceof LeafNode) {
                    return (AbstractNode)this.subNodes[index ^ 1];
                }
                return new IndexedNode<K, V>(newBitmap, this.size - atIndx.size(), IndexedNode.remove(this.subNodes, index));
            }
            if (added) {
                if (this.subNodes.length >= 16) {
                    return this.expand(frag, child, mask, this.subNodes);
                }
                return new IndexedNode<K, V>(newBitmap, this.size + child.size(), IndexedNode.insert(this.subNodes, index, child));
            }
            if (!exists) {
                return this;
            }
            return new IndexedNode<K, V>(newBitmap, this.size - atIndx.size() + child.size(), IndexedNode.update(this.subNodes, index, child));
        }

        private ArrayNode<K, V> expand(int frag, AbstractNode<K, V> child, int mask, Object[] subNodes) {
            int bit = mask;
            int count = 0;
            int ptr = 0;
            Object[] arr = new Object[32];
            for (int i = 0; i < 32; ++i) {
                if ((bit & 1) != 0) {
                    arr[i] = subNodes[ptr++];
                    ++count;
                } else if (i == frag) {
                    arr[i] = child;
                    ++count;
                } else {
                    arr[i] = EmptyNode.instance();
                }
                bit >>>= 1;
            }
            return new ArrayNode(count, this.size + child.size(), arr);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public int size() {
            return this.size;
        }
    }

    public static final class LeafList<K, V>
    extends LeafNode<K, V>
    implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int hash;
        private final K key;
        private final V value;
        private final int size;
        private final LeafNode<K, V> tail;

        LeafList(int hash, K key, V value, LeafNode<K, V> tail) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.size = 1 + tail.size();
            this.tail = tail;
        }

        @Override
        Option<V> lookup(int shift, int keyHash, K key) {
            if (this.hash != keyHash) {
                return Option.none();
            }
            return this.nodes().find(node -> Objects.equals(node.key(), key)).map(LeafNode::value);
        }

        @Override
        V lookup(int shift, int keyHash, K key, V defaultValue) {
            if (this.hash != keyHash) {
                return defaultValue;
            }
            V result = defaultValue;
            Iterator<LeafNode<K, V>> iterator2 = this.nodes();
            while (iterator2.hasNext()) {
                LeafNode node = (LeafNode)iterator2.next();
                if (!Objects.equals(node.key(), key)) continue;
                result = node.value();
                break;
            }
            return result;
        }

        @Override
        AbstractNode<K, V> modify(int shift, int keyHash, K key, V value, Action action) {
            if (keyHash == this.hash) {
                AbstractNode<K, V> filtered = this.removeElement(key);
                if (action == Action.REMOVE) {
                    return filtered;
                }
                return new LeafList<K, V>(this.hash, key, value, (LeafNode)filtered);
            }
            return action == Action.REMOVE ? this : LeafList.mergeLeaves(shift, this, new LeafSingleton<K, V>(keyHash, key, value));
        }

        private static <K, V> AbstractNode<K, V> mergeNodes(LeafNode<K, V> leaf1, LeafNode<K, V> leaf2) {
            if (leaf2 == null) {
                return leaf1;
            }
            if (leaf1 instanceof LeafSingleton) {
                return new LeafList<K, V>(leaf1.hash(), leaf1.key(), leaf1.value(), leaf2);
            }
            if (leaf2 instanceof LeafSingleton) {
                return new LeafList<K, V>(leaf2.hash(), leaf2.key(), leaf2.value(), leaf1);
            }
            LeafNode<K, V> result = leaf1;
            LeafNode<K, V> tail = leaf2;
            while (tail instanceof LeafList) {
                LeafList list = (LeafList)tail;
                result = new LeafList<K, V>(list.hash, list.key, list.value, result);
                tail = list.tail;
            }
            return new LeafList<K, V>(tail.hash(), tail.key(), tail.value(), result);
        }

        private AbstractNode<K, V> removeElement(K k) {
            if (Objects.equals(k, this.key)) {
                return this.tail;
            }
            LeafNode leaf1 = new LeafSingleton<K, V>(this.hash, this.key, this.value);
            LeafNode<K, V> leaf2 = this.tail;
            boolean found = false;
            while (!found && leaf2 != null) {
                if (Objects.equals(k, leaf2.key())) {
                    found = true;
                } else {
                    leaf1 = new LeafList<K, V>(leaf2.hash(), leaf2.key(), leaf2.value(), leaf1);
                }
                leaf2 = leaf2 instanceof LeafList ? ((LeafList)leaf2).tail : null;
            }
            return LeafList.mergeNodes(leaf1, leaf2);
        }

        @Override
        public int size() {
            return this.size;
        }

        @Override
        public Iterator<LeafNode<K, V>> nodes() {
            return new AbstractIterator<LeafNode<K, V>>(){
                LeafNode<K, V> node;
                {
                    this.node = this;
                }

                @Override
                public boolean hasNext() {
                    return this.node != null;
                }

                @Override
                public LeafNode<K, V> getNext() {
                    LeafNode result = this.node;
                    this.node = this.node instanceof LeafSingleton ? null : ((LeafList)this.node).tail;
                    return result;
                }
            };
        }

        @Override
        int hash() {
            return this.hash;
        }

        @Override
        K key() {
            return this.key;
        }

        @Override
        V value() {
            return this.value;
        }
    }

    public static final class LeafSingleton<K, V>
    extends LeafNode<K, V>
    implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int hash;
        private final K key;
        private final V value;

        LeafSingleton(int hash, K key, V value) {
            this.hash = hash;
            this.key = key;
            this.value = value;
        }

        private boolean equals(int keyHash, K key) {
            return keyHash == this.hash && Objects.equals(key, this.key);
        }

        @Override
        Option<V> lookup(int shift, int keyHash, K key) {
            return Option.when(this.equals(keyHash, key), this.value);
        }

        @Override
        V lookup(int shift, int keyHash, K key, V defaultValue) {
            return this.equals(keyHash, key) ? this.value : defaultValue;
        }

        @Override
        AbstractNode<K, V> modify(int shift, int keyHash, K key, V value, Action action) {
            if (keyHash == this.hash && Objects.equals(key, this.key)) {
                return action == Action.REMOVE ? EmptyNode.instance() : new LeafSingleton<K, V>(this.hash, key, value);
            }
            return action == Action.REMOVE ? this : LeafSingleton.mergeLeaves(shift, this, new LeafSingleton<K, V>(keyHash, key, value));
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Iterator<LeafNode<K, V>> nodes() {
            return Iterator.of(this);
        }

        @Override
        int hash() {
            return this.hash;
        }

        @Override
        K key() {
            return this.key;
        }

        @Override
        V value() {
            return this.value;
        }
    }

    public static abstract class LeafNode<K, V>
    extends AbstractNode<K, V> {
        abstract K key();

        abstract V value();

        abstract int hash();

        static <K, V> AbstractNode<K, V> mergeLeaves(int shift, LeafNode<K, V> leaf1, LeafSingleton<K, V> leaf2) {
            Object[] objectArray;
            int h2;
            int h1 = leaf1.hash();
            if (h1 == (h2 = leaf2.hash())) {
                return new LeafList<K, V>(h1, leaf2.key(), leaf2.value(), leaf1);
            }
            int subH1 = LeafNode.hashFragment(shift, h1);
            int subH2 = LeafNode.hashFragment(shift, h2);
            int newBitmap = LeafNode.toBitmap(subH1) | LeafNode.toBitmap(subH2);
            if (subH1 == subH2) {
                AbstractNode<K, V> newLeaves = LeafNode.mergeLeaves(shift + 5, leaf1, leaf2);
                return new IndexedNode(newBitmap, newLeaves.size(), new Object[]{newLeaves});
            }
            int n = leaf1.size() + leaf2.size();
            if (subH1 < subH2) {
                Object[] objectArray2 = new Object[2];
                objectArray2[0] = leaf1;
                objectArray = objectArray2;
                objectArray2[1] = leaf2;
            } else {
                Object[] objectArray3 = new Object[2];
                objectArray3[0] = leaf2;
                objectArray = objectArray3;
                objectArray3[1] = leaf1;
            }
            return new IndexedNode(newBitmap, n, objectArray);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    public static final class EmptyNode<K, V>
    extends AbstractNode<K, V>
    implements Serializable {
        private static final long serialVersionUID = 1L;
        private static final EmptyNode<?, ?> INSTANCE = new EmptyNode();

        private EmptyNode() {
        }

        static <K, V> EmptyNode<K, V> instance() {
            return INSTANCE;
        }

        @Override
        Option<V> lookup(int shift, int keyHash, K key) {
            return Option.none();
        }

        @Override
        V lookup(int shift, int keyHash, K key, V defaultValue) {
            return defaultValue;
        }

        @Override
        AbstractNode<K, V> modify(int shift, int keyHash, K key, V value, Action action) {
            return action == Action.REMOVE ? this : new LeafSingleton<K, V>(keyHash, key, value);
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Iterator<LeafNode<K, V>> nodes() {
            return Iterator.empty();
        }

        private Object readResolve() {
            return INSTANCE;
        }
    }

    public static abstract class AbstractNode<K, V>
    implements HashArrayMappedTrie<K, V> {
        static final int SIZE = 5;
        static final int BUCKET_SIZE = 32;
        static final int MAX_INDEX_NODE = 16;
        static final int MIN_ARRAY_NODE = 8;

        static int hashFragment(int shift, int hash) {
            return hash >>> shift & 0x1F;
        }

        static int toBitmap(int hash) {
            return 1 << hash;
        }

        static int fromBitmap(int bitmap, int bit) {
            return Integer.bitCount(bitmap & bit - 1);
        }

        static Object[] update(Object[] arr, int index, Object newElement) {
            Object[] newArr = Arrays.copyOf(arr, arr.length);
            newArr[index] = newElement;
            return newArr;
        }

        static Object[] remove(Object[] arr, int index) {
            Object[] newArr = new Object[arr.length - 1];
            System.arraycopy(arr, 0, newArr, 0, index);
            System.arraycopy(arr, index + 1, newArr, index, arr.length - index - 1);
            return newArr;
        }

        static Object[] insert(Object[] arr, int index, Object newElem) {
            Object[] newArr = new Object[arr.length + 1];
            System.arraycopy(arr, 0, newArr, 0, index);
            newArr[index] = newElem;
            System.arraycopy(arr, index, newArr, index + 1, arr.length - index);
            return newArr;
        }

        abstract Option<V> lookup(int var1, int var2, K var3);

        abstract V lookup(int var1, int var2, K var3, V var4);

        abstract AbstractNode<K, V> modify(int var1, int var2, K var3, V var4, Action var5);

        Iterator<LeafNode<K, V>> nodes() {
            return new LeafNodeIterator(this);
        }

        @Override
        public Iterator<Tuple2<K, V>> iterator() {
            return this.nodes().map(node -> Tuple.of(node.key(), node.value()));
        }

        @Override
        public Iterator<K> keysIterator() {
            return this.nodes().map(LeafNode::key);
        }

        @Override
        public Iterator<V> valuesIterator() {
            return this.nodes().map(LeafNode::value);
        }

        @Override
        public Option<V> get(K key) {
            return this.lookup(0, Objects.hashCode(key), key);
        }

        @Override
        public V getOrElse(K key, V defaultValue) {
            return this.lookup(0, Objects.hashCode(key), key, defaultValue);
        }

        @Override
        public boolean containsKey(K key) {
            return this.get(key).isDefined();
        }

        @Override
        public HashArrayMappedTrie<K, V> put(K key, V value) {
            return this.modify(0, Objects.hashCode(key), key, value, Action.PUT);
        }

        @Override
        public HashArrayMappedTrie<K, V> remove(K key) {
            return this.modify(0, Objects.hashCode(key), key, null, Action.REMOVE);
        }

        public final String toString() {
            return this.iterator().map(t -> t._1 + " -> " + t._2).mkString("HashArrayMappedTrie(", ", ", ")");
        }
    }

    public static class LeafNodeIterator<K, V>
    extends AbstractIterator<LeafNode<K, V>> {
        private static final int MAX_LEVELS = 8;
        private final int total;
        private final Object[] nodes = new Object[8];
        private final int[] indexes = new int[8];
        private int level;
        private int ptr = 0;

        LeafNodeIterator(AbstractNode<K, V> root) {
            this.total = root.size();
            this.level = LeafNodeIterator.downstairs(this.nodes, this.indexes, root, 0);
        }

        @Override
        public boolean hasNext() {
            return this.ptr < this.total;
        }

        @Override
        protected LeafNode<K, V> getNext() {
            Object node = this.nodes[this.level];
            while (!(node instanceof LeafNode)) {
                node = this.findNextLeaf();
            }
            ++this.ptr;
            if (node instanceof LeafList) {
                LeafList leaf = (LeafList)node;
                this.nodes[this.level] = leaf.tail;
                return leaf;
            }
            this.nodes[this.level] = EmptyNode.instance();
            return (LeafSingleton)node;
        }

        private Object findNextLeaf() {
            AbstractNode<K, V> node = null;
            while (this.level > 0) {
                int n = --this.level;
                this.indexes[n] = this.indexes[n] + 1;
                node = LeafNodeIterator.getChild((AbstractNode)this.nodes[this.level], this.indexes[this.level]);
                if (node == null) continue;
            }
            this.level = LeafNodeIterator.downstairs(this.nodes, this.indexes, node, this.level + 1);
            return this.nodes[this.level];
        }

        private static <K, V> int downstairs(Object[] nodes, int[] indexes, AbstractNode<K, V> root, int level) {
            while (true) {
                nodes[level] = root;
                indexes[level] = 0;
                if ((root = LeafNodeIterator.getChild(root, 0)) == null) break;
                ++level;
            }
            return level;
        }

        private static <K, V> AbstractNode<K, V> getChild(AbstractNode<K, V> node, int index) {
            if (node instanceof IndexedNode) {
                Object[] subNodes = ((IndexedNode)node).subNodes;
                return index < subNodes.length ? (AbstractNode)subNodes[index] : null;
            }
            if (node instanceof ArrayNode) {
                ArrayNode arrayNode = (ArrayNode)node;
                return index < 32 ? (AbstractNode)arrayNode.subNodes[index] : null;
            }
            return null;
        }
    }

    public static enum Action {
        PUT,
        REMOVE;

    }
}

