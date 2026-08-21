/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx.util.glu.tessellation;

import org.lwjglx.util.glu.tessellation.ActiveRegion;
import org.lwjglx.util.glu.tessellation.GLUface;
import org.lwjglx.util.glu.tessellation.GLUvertex;

class GLUhalfEdge {
    public GLUhalfEdge next;
    public GLUhalfEdge sym;
    public GLUhalfEdge oNext;
    public GLUhalfEdge lNext;
    public GLUvertex org;
    public GLUface lFace;
    public ActiveRegion activeRegion;
    public int winding;
    public boolean first;

    GLUhalfEdge(boolean first) {
        this.first = first;
    }
}

