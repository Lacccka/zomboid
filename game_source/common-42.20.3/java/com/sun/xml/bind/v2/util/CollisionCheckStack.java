/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.util;

import java.util.AbstractList;
import java.util.Arrays;

public final class CollisionCheckStack<E>
extends AbstractList<E> {
    private Object[] data;
    private int[] next;
    private int size = 0;
    private boolean latestPushResult = false;
    private boolean useIdentity = true;
    private final int[] initialHash = new int[17];

    public CollisionCheckStack() {
        this.data = new Object[16];
        this.next = new int[16];
    }

    public void setUseIdentity(boolean useIdentity) {
        this.useIdentity = useIdentity;
    }

    public boolean getUseIdentity() {
        return this.useIdentity;
    }

    public boolean getLatestPushResult() {
        return this.latestPushResult;
    }

    public boolean push(E o) {
        if (this.data.length == this.size) {
            this.expandCapacity();
        }
        this.data[this.size] = o;
        int hash = this.hash(o);
        boolean r = this.findDuplicate(o, hash);
        this.next[this.size] = this.initialHash[hash];
        this.initialHash[hash] = this.size + 1;
        ++this.size;
        this.latestPushResult = r;
        return this.latestPushResult;
    }

    public void pushNocheck(E o) {
        if (this.data.length == this.size) {
            this.expandCapacity();
        }
        this.data[this.size] = o;
        this.next[this.size] = -1;
        ++this.size;
    }

    public boolean findDuplicate(E o) {
        int hash = this.hash(o);
        return this.findDuplicate(o, hash);
    }

    @Override
    public E get(int index) {
        return (E)this.data[index];
    }

    @Override
    public int size() {
        return this.size;
    }

    private int hash(Object o) {
        return ((this.useIdentity ? System.identityHashCode(o) : o.hashCode()) & Integer.MAX_VALUE) % this.initialHash.length;
    }

    public E pop() {
        --this.size;
        Object o = this.data[this.size];
        this.data[this.size] = null;
        int n = this.next[this.size];
        if (n >= 0) {
            int hash = this.hash(o);
            assert (this.initialHash[hash] == this.size + 1);
            this.initialHash[hash] = n;
        }
        return (E)o;
    }

    public E peek() {
        return (E)this.data[this.size - 1];
    }

    private boolean findDuplicate(E o, int hash) {
        int p = this.initialHash[hash];
        while (p != 0) {
            Object existing = this.data[--p];
            if (this.useIdentity ? existing == o : o.equals(existing)) {
                return true;
            }
            p = this.next[p];
        }
        return false;
    }

    private void expandCapacity() {
        int oldSize = this.data.length;
        int newSize = oldSize * 2;
        Object[] d = new Object[newSize];
        int[] n = new int[newSize];
        System.arraycopy(this.data, 0, d, 0, oldSize);
        System.arraycopy(this.next, 0, n, 0, oldSize);
        this.data = d;
        this.next = n;
    }

    public void reset() {
        if (this.size > 0) {
            this.size = 0;
            Arrays.fill(this.initialHash, 0);
        }
    }

    public String getCycleString() {
        E x;
        StringBuilder sb = new StringBuilder();
        int i = this.size() - 1;
        E obj = this.get(i);
        sb.append(obj);
        do {
            sb.append(" -> ");
            x = this.get(--i);
            sb.append(x);
        } while (obj != x);
        return sb.toString();
    }
}

