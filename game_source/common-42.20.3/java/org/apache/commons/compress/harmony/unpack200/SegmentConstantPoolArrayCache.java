/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;

public class SegmentConstantPoolArrayCache {
    protected IdentityHashMap<String[], CachedArray> knownArrays = new IdentityHashMap(1000);
    protected List<Integer> lastIndexes;
    protected String[] lastArray;
    protected String lastKey;

    protected boolean arrayIsCached(String[] array) {
        CachedArray cachedArray = this.knownArrays.get(array);
        return cachedArray != null && cachedArray.lastKnownSize() == array.length;
    }

    protected void cacheArray(String[] array) {
        if (this.arrayIsCached(array)) {
            throw new IllegalArgumentException("Trying to cache an array that already exists");
        }
        this.knownArrays.put(array, new CachedArray(array));
        this.lastArray = null;
    }

    public List<Integer> indexesForArrayKey(String[] array, String key) {
        if (!this.arrayIsCached(array)) {
            this.cacheArray(array);
        }
        if (this.lastArray == array && this.lastKey == key) {
            return this.lastIndexes;
        }
        this.lastArray = array;
        this.lastKey = key;
        this.lastIndexes = this.knownArrays.get(array).indexesForKey(key);
        return this.lastIndexes;
    }

    protected class CachedArray {
        String[] primaryArray;
        int lastKnownSize;
        HashMap<String, List<Integer>> primaryTable;

        public CachedArray(String[] array) {
            this.primaryArray = array;
            this.lastKnownSize = array.length;
            this.primaryTable = new HashMap(this.lastKnownSize);
            this.cacheIndexes();
        }

        protected void cacheIndexes() {
            for (int index = 0; index < this.primaryArray.length; ++index) {
                String key = this.primaryArray[index];
                this.primaryTable.computeIfAbsent(key, k -> new ArrayList()).add(index);
            }
        }

        public List<Integer> indexesForKey(String key) {
            List<Integer> list = this.primaryTable.get(key);
            return list != null ? list : Collections.emptyList();
        }

        public int lastKnownSize() {
            return this.lastKnownSize;
        }
    }
}

