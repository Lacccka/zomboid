/*
 * Decompiled with CFR 0.152.
 */
package astar.datastructures;

import astar.ISearchNode;
import astar.datastructures.IClosedSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ClosedSet
implements IClosedSet {
    private final ArrayList<ISearchNode> list = new ArrayList();
    private final Comparator<ISearchNode> comp;

    public ClosedSet(Comparator<ISearchNode> comp) {
        this.comp = comp;
    }

    @Override
    public boolean contains(ISearchNode node) {
        return this.list.contains(node);
    }

    @Override
    public void add(ISearchNode node) {
        this.list.add(node);
    }

    @Override
    public ISearchNode min() {
        return Collections.min(this.list, this.comp);
    }

    @Override
    public void clear() {
        this.list.clear();
    }
}

