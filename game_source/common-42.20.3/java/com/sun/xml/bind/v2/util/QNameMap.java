/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.util;

import com.sun.xml.bind.v2.runtime.Name;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.xml.namespace.QName;

public final class QNameMap<V> {
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final int MAXIMUM_CAPACITY = 0x40000000;
    transient Entry<V>[] table = new Entry[16];
    transient int size;
    private int threshold = 12;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private Set<Entry<V>> entrySet = null;

    public void put(String namespaceUri, String localname, V value) {
        assert (localname != null);
        assert (namespaceUri != null);
        assert (localname == localname.intern());
        assert (namespaceUri == namespaceUri.intern());
        int hash = QNameMap.hash(localname);
        int i = QNameMap.indexFor(hash, this.table.length);
        Entry<V> e = this.table[i];
        while (e != null) {
            if (e.hash == hash && localname == e.localName && namespaceUri == e.nsUri) {
                e.value = value;
                return;
            }
            e = e.next;
        }
        this.addEntry(hash, namespaceUri, localname, value, i);
    }

    public void put(QName name, V value) {
        this.put(name.getNamespaceURI(), name.getLocalPart(), value);
    }

    public void put(Name name, V value) {
        this.put(name.nsUri, name.localName, value);
    }

    public V get(String nsUri, String localPart) {
        Entry<V> e = this.getEntry(nsUri, localPart);
        if (e == null) {
            return null;
        }
        return e.value;
    }

    public V get(QName name) {
        return this.get(name.getNamespaceURI(), name.getLocalPart());
    }

    public int size() {
        return this.size;
    }

    public QNameMap<V> putAll(QNameMap<? extends V> map) {
        int numKeysToBeAdded = map.size();
        if (numKeysToBeAdded == 0) {
            return this;
        }
        if (numKeysToBeAdded > this.threshold) {
            int newCapacity;
            int targetCapacity = numKeysToBeAdded;
            if (targetCapacity > 0x40000000) {
                targetCapacity = 0x40000000;
            }
            for (newCapacity = this.table.length; newCapacity < targetCapacity; newCapacity <<= 1) {
            }
            if (newCapacity > this.table.length) {
                this.resize(newCapacity);
            }
        }
        for (Entry<V> e : map.entrySet()) {
            this.put(e.nsUri, e.localName, e.getValue());
        }
        return this;
    }

    private static int hash(String x) {
        int h = x.hashCode();
        h += ~(h << 9);
        h ^= h >>> 14;
        h += h << 4;
        h ^= h >>> 10;
        return h;
    }

    private static int indexFor(int h, int length) {
        return h & length - 1;
    }

    private void addEntry(int hash, String nsUri, String localName, V value, int bucketIndex) {
        Entry<V> e = this.table[bucketIndex];
        this.table[bucketIndex] = new Entry<V>(hash, nsUri, localName, value, e);
        if (this.size++ >= this.threshold) {
            this.resize(2 * this.table.length);
        }
    }

    private void resize(int newCapacity) {
        Entry<V>[] oldTable = this.table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == 0x40000000) {
            this.threshold = Integer.MAX_VALUE;
            return;
        }
        Entry[] newTable = new Entry[newCapacity];
        this.transfer(newTable);
        this.table = newTable;
        this.threshold = newCapacity;
    }

    private void transfer(Entry<V>[] newTable) {
        Entry<V>[] src = this.table;
        int newCapacity = newTable.length;
        for (int j = 0; j < src.length; ++j) {
            Entry next;
            Entry<V> e = src[j];
            if (e == null) continue;
            src[j] = null;
            do {
                next = e.next;
                int i = QNameMap.indexFor(e.hash, newCapacity);
                e.next = newTable[i];
                newTable[i] = e;
            } while ((e = next) != null);
        }
    }

    public Entry<V> getOne() {
        for (Entry<V> e : this.table) {
            if (e == null) continue;
            return e;
        }
        return null;
    }

    public Collection<QName> keySet() {
        HashSet<QName> r = new HashSet<QName>();
        for (Entry<V> e : this.entrySet()) {
            r.add(e.createQName());
        }
        return r;
    }

    public boolean containsKey(String nsUri, String localName) {
        return this.getEntry(nsUri, localName) != null;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public Set<Entry<V>> entrySet() {
        EntrySet es = this.entrySet;
        return es != null ? es : (this.entrySet = new EntrySet());
    }

    private Iterator<Entry<V>> newEntryIterator() {
        return new EntryIterator();
    }

    private Entry<V> getEntry(String nsUri, String localName) {
        assert (nsUri == nsUri.intern());
        assert (localName == localName.intern());
        int hash = QNameMap.hash(localName);
        int i = QNameMap.indexFor(hash, this.table.length);
        Entry<V> e = this.table[i];
        while (e != null && (localName != e.localName || nsUri != e.nsUri)) {
            e = e.next;
        }
        return e;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append('{');
        for (Entry<V> e : this.entrySet()) {
            if (buf.length() > 1) {
                buf.append(',');
            }
            buf.append('[');
            buf.append(e);
            buf.append(']');
        }
        buf.append('}');
        return buf.toString();
    }

    private class EntrySet
    extends AbstractSet<Entry<V>> {
        private EntrySet() {
        }

        @Override
        public Iterator<Entry<V>> iterator() {
            return QNameMap.this.newEntryIterator();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry e = (Entry)o;
            Entry candidate = QNameMap.this.getEntry(e.nsUri, e.localName);
            return candidate != null && candidate.equals(e);
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return QNameMap.this.size;
        }
    }

    private class EntryIterator
    extends HashIterator<Entry<V>> {
        private EntryIterator() {
        }

        @Override
        public Entry<V> next() {
            return this.nextEntry();
        }
    }

    public static final class Entry<V> {
        public final String nsUri;
        public final String localName;
        V value;
        final int hash;
        Entry<V> next;

        Entry(int h, String nsUri, String localName, V v, Entry<V> n) {
            this.value = v;
            this.next = n;
            this.nsUri = nsUri;
            this.localName = localName;
            this.hash = h;
        }

        public QName createQName() {
            return new QName(this.nsUri, this.localName);
        }

        public V getValue() {
            return this.value;
        }

        public V setValue(V newValue) {
            V oldValue = this.value;
            this.value = newValue;
            return oldValue;
        }

        public boolean equals(Object o) {
            V v2;
            V v1;
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry e = (Entry)o;
            String k1 = this.nsUri;
            String k2 = e.nsUri;
            String k3 = this.localName;
            String k4 = e.localName;
            return (k1 == k2 || k1 != null && k1.equals(k2) && (k3 == k4 || k3 != null && k3.equals(k4))) && ((v1 = this.getValue()) == (v2 = e.getValue()) || v1 != null && v1.equals(v2));
        }

        public int hashCode() {
            return this.localName.hashCode() ^ (this.value == null ? 0 : this.value.hashCode());
        }

        public String toString() {
            return '\"' + this.nsUri + "\",\"" + this.localName + "\"=" + this.getValue();
        }
    }

    private abstract class HashIterator<E>
    implements Iterator<E> {
        Entry<V> next;
        int index;

        HashIterator() {
            Entry<V>[] t = QNameMap.this.table;
            int i = t.length;
            Entry n = null;
            if (QNameMap.this.size != 0) {
                while (i > 0 && (n = t[--i]) == null) {
                }
            }
            this.next = n;
            this.index = i;
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        Entry<V> nextEntry() {
            Entry e = this.next;
            if (e == null) {
                throw new NoSuchElementException();
            }
            Entry n = e.next;
            Entry<V>[] t = QNameMap.this.table;
            int i = this.index;
            while (n == null && i > 0) {
                n = t[--i];
            }
            this.index = i;
            this.next = n;
            return e;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

