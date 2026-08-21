/*
 * Decompiled with CFR 0.152.
 */
package astar.datastructures;

import astar.ISearchNode;

public interface IOpenSet {
    public void add(ISearchNode var1);

    public void remove(ISearchNode var1);

    public ISearchNode poll();

    public ISearchNode getNode(ISearchNode var1);

    public int size();

    public void clear();
}

