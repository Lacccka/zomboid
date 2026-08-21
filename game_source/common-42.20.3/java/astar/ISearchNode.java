/*
 * Decompiled with CFR 0.152.
 */
package astar;

import java.util.ArrayList;

public interface ISearchNode {
    public double f();

    public double g();

    public void setG(double var1);

    public double h();

    public double c(ISearchNode var1);

    public void getSuccessors(ArrayList<ISearchNode> var1);

    public ISearchNode getParent();

    public void setParent(ISearchNode var1);

    public Integer keyCode();

    public boolean equals(Object var1);

    public int hashCode();

    public int getDepth();

    public void setDepth(int var1);
}

