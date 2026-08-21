/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx.util.glu.tessellation;

import org.lwjglx.util.glu.tessellation.GLUhalfEdge;

class GLUface {
    public GLUface next;
    public GLUface prev;
    public GLUhalfEdge anEdge;
    public Object data;
    public GLUface trail;
    public boolean marked;
    public boolean inside;

    GLUface() {
    }
}

