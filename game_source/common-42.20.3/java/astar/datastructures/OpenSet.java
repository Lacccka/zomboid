/*
 * Decompiled with CFR 0.152.
 */
package astar.datastructures;

import astar.ISearchNode;
import astar.datastructures.IOpenSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class OpenSet
implements IOpenSet {
    private final PriorityQueue<ISearchNode> q;
    private final ArrayList<ISearchNode> qa;

    public OpenSet(Comparator<ISearchNode> comp) {
        this.q = new PriorityQueue<ISearchNode>(1000, comp);
        this.qa = new ArrayList(1000);
    }

    @Override
    public void add(ISearchNode node) {
        this.q.add(node);
        this.qa.add(node);
    }

    @Override
    public void remove(ISearchNode node) {
        this.q.remove(node);
        this.qa.remove(node);
    }

    @Override
    public ISearchNode poll() {
        return this.q.poll();
    }

    @Override
    public ISearchNode getNode(ISearchNode node) {
        ArrayList<ISearchNode> qa = this.qa;
        for (int i = 0; i < qa.size(); ++i) {
            ISearchNode openSearchNode = qa.get(i);
            if (!openSearchNode.equals(node)) continue;
            return openSearchNode;
        }
        return null;
    }

    @Override
    public int size() {
        return this.q.size();
    }

    @Override
    public void clear() {
        this.q.clear();
        this.qa.clear();
    }
}

