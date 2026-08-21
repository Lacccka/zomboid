/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx.util.glu.tessellation;

import org.lwjglx.util.glu.tessellation.GLUhalfEdge;

class GLUvertex {
    public GLUvertex next;
    public GLUvertex prev;
    public GLUhalfEdge anEdge;
    public Object data;
    public double[] coords = new double[3];
    public double s;
    public double t;
    public int pqHandle;

    GLUvertex() {
    }
}

