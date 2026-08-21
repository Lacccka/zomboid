/*
 * Decompiled with CFR 0.152.
 */
package org.luaj.kahluafork.compiler;

public final class ExpDesc {
    int k;
    int info;
    int aux;
    private double nval;
    private boolean hasNval;
    int t;
    int f;

    public void setNval(double r) {
        this.nval = r;
        this.hasNval = true;
    }

    public double nval() {
        return this.hasNval ? this.nval : (double)this.info;
    }

    void init(int k, int i) {
        this.f = -1;
        this.t = -1;
        this.k = k;
        this.info = i;
    }

    boolean hasjumps() {
        return this.t != this.f;
    }

    boolean isnumeral() {
        return this.k == 5 && this.t == -1 && this.f == -1;
    }

    public void setvalue(ExpDesc other) {
        this.k = other.k;
        this.nval = other.nval;
        this.hasNval = other.hasNval;
        this.info = other.info;
        this.aux = other.aux;
        this.t = other.t;
        this.f = other.f;
    }
}

