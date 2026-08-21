/*
 * Decompiled with CFR 0.152.
 */
package astar.datastructures;

import astar.ISearchNode;

public interface IClosedSet {
    public boolean contains(ISearchNode var1);

    public void add(ISearchNode var1);

    public ISearchNode min();

    public void clear();
}

