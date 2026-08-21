/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.Tuple2;
import io.vavr.collection.HashArrayMappedTrieModule;
import io.vavr.collection.Iterator;
import io.vavr.control.Option;

interface HashArrayMappedTrie<K, V>
extends Iterable<Tuple2<K, V>> {
    public static <K, V> HashArrayMappedTrie<K, V> empty() {
        return HashArrayMappedTrieModule.EmptyNode.instance();
    }

    public boolean isEmpty();

    public int size();

    public Option<V> get(K var1);

    public V getOrElse(K var1, V var2);

    public boolean containsKey(K var1);

    public HashArrayMappedTrie<K, V> put(K var1, V var2);

    public HashArrayMappedTrie<K, V> remove(K var1);

    @Override
    public Iterator<Tuple2<K, V>> iterator();

    public Iterator<K> keysIterator();

    public Iterator<V> valuesIterator();
}

