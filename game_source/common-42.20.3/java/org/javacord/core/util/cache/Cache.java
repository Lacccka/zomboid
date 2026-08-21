/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.cache;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import java.util.Optional;
import java.util.function.Function;
import org.javacord.core.util.cache.Index;

public class Cache<T> {
    private final Set<T> elements;
    private final Map<String, Index<Object, T>> indexes;

    private Cache(Set<T> elements, Map<String, Index<Object, T>> indexes) {
        this.elements = elements;
        this.indexes = indexes;
    }

    public static <T> Cache<T> empty() {
        return new Cache(HashSet.empty(), HashMap.empty());
    }

    public Cache<T> addIndex(String indexName, Function<T, Object> mappingFunction) {
        if (this.indexes.containsKey(indexName)) {
            throw new IllegalStateException("The cache already has an index with name " + indexName);
        }
        Index<Object, T> index = new Index<Object, T>(mappingFunction);
        for (Object element : this.elements) {
            index = index.addElement(element);
        }
        Map<String, Index<Object, T>> newIndexes = this.indexes.put(indexName, index);
        return new Cache<T>(this.elements, newIndexes);
    }

    public Cache<T> addElement(T element) {
        Set<T> newElements = this.elements.add(element);
        Map<String, Index<Object, T>> newIndexes = this.indexes.mapValues(index -> index.addElement(element));
        return new Cache<T>(newElements, newIndexes);
    }

    public Cache<T> removeElement(T element) {
        Set<T> newElements = this.elements.remove(element);
        Map<String, Index<Object, T>> newIndexes = this.indexes.mapValues(index -> index.removeElement(element));
        return new Cache<T>(newElements, newIndexes);
    }

    public Cache<T> updateIndexesOfElement(T element) {
        if (!this.elements.contains(element)) {
            return this;
        }
        return this.removeElement(element).addElement(element);
    }

    public Set<T> getAll() {
        return this.elements;
    }

    public Optional<T> findAnyByIndex(String indexName, Object key) {
        Index<Object, T> index = this.indexes.get(indexName).getOrElseThrow(() -> new IllegalArgumentException("No index with given name (" + indexName + ") found"));
        return index.findAny(key);
    }

    public Set<T> findByIndex(String indexName, Object key) {
        Index<Object, T> index = this.indexes.get(indexName).getOrElseThrow(() -> new IllegalArgumentException("No index with given name (" + indexName + ") found"));
        return index.find(key);
    }
}

