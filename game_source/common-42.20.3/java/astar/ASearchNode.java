/*
 * Decompiled with CFR 0.152.
 */
package astar;

import astar.ISearchNode;

public abstract class ASearchNode
implements ISearchNode {
    private double g;
    private int depth;

    @Override
    public double f() {
        return this.g() + this.h();
    }

    @Override
    public double g() {
        return this.g;
    }

    @Override
    public void setG(double g) {
        this.g = g;
    }

    @Override
    public int getDepth() {
        return this.depth;
    }

    @Override
    public void setDepth(int depth) {
        this.depth = depth;
    }
}

