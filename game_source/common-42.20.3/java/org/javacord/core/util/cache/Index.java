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

public class Index<K, E> {
    private final Function<E, K> keyMapper;
    private final Map<K, Set<E>> elementsByKey;
    private final Map<E, K> keyByElement;

    public Index(Function<E, K> keyMapper) {
        this(keyMapper, HashMap.empty(), HashMap.empty());
    }

    private Index(Function<E, K> keyMapper, Map<K, Set<E>> elementsByKey, Map<E, K> keyByElement) {
        this.keyMapper = keyMapper;
        this.elementsByKey = elementsByKey;
        this.keyByElement = keyByElement;
    }

    public Function<E, K> getKeyMapper() {
        return this.keyMapper;
    }

    public Index<K, E> addElement(E element) {
        K key = this.keyMapper.apply(element);
        if (key == null) {
            return this;
        }
        Set<E> elements = this.find(key);
        if (elements.contains(element)) {
            return this;
        }
        if (this.keyByElement.containsKey(element)) {
            throw new IllegalStateException("The given element is already in the index with a different key");
        }
        Map<K, Set<E>> newElementsByKey = this.elementsByKey.put(key, elements.add(element));
        Map<E, K> newKeyByElement = this.keyByElement.put(element, key);
        return new Index<K, E>(this.keyMapper, newElementsByKey, newKeyByElement);
    }

    public Index<K, E> removeElement(E element) {
        Object key = this.keyByElement.getOrElse(element, null);
        if (key == null) {
            return this;
        }
        Set<E> elements = this.find(key);
        if (!elements.contains(element)) {
            return this;
        }
        Map<Object, Set<E>> newElementsByKey = this.elementsByKey.put(key, elements.remove(element));
        Map<E, K> newKeyByElement = this.keyByElement.remove(element);
        return new Index<Object, E>(this.keyMapper, newElementsByKey, newKeyByElement);
    }

    public Set<E> find(K key) {
        return this.elementsByKey.getOrElse(key, HashSet.empty());
    }

    public Optional<E> findAny(K key) {
        return this.find(key).headOption().toJavaOptional();
    }
}

