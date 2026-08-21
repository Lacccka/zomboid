/*
 * Decompiled with CFR 0.152.
 */
package astar.datastructures;

import astar.ISearchNode;
import astar.datastructures.HashPriorityQueue;
import astar.datastructures.IOpenSet;
import java.util.Comparator;

public class OpenSetHash
implements IOpenSet {
    private final HashPriorityQueue<Integer, ISearchNode> hashQ;
    private final Comparator<ISearchNode> comp;

    public OpenSetHash(Comparator<ISearchNode> comp) {
        this.hashQ = new HashPriorityQueue(comp);
        this.comp = comp;
    }

    @Override
    public void add(ISearchNode node) {
        this.hashQ.add(node.keyCode(), node);
    }

    @Override
    public void remove(ISearchNode node) {
        this.hashQ.remove(node.keyCode(), node);
    }

    @Override
    public ISearchNode poll() {
        return this.hashQ.poll();
    }

    @Override
    public ISearchNode getNode(ISearchNode node) {
        return this.hashQ.get(node.keyCode());
    }

    @Override
    public int size() {
        return this.hashQ.size();
    }

    public String toString() {
        return this.hashQ.getTreeMap().keySet().toString();
    }

    @Override
    public void clear() {
        this.hashQ.clear();
    }
}

