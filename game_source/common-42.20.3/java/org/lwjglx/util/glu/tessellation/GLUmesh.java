/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx.util.glu.tessellation;

import org.lwjglx.util.glu.tessellation.GLUface;
import org.lwjglx.util.glu.tessellation.GLUhalfEdge;
import org.lwjglx.util.glu.tessellation.GLUvertex;

class GLUmesh {
    GLUvertex vHead = new GLUvertex();
    GLUface fHead = new GLUface();
    GLUhalfEdge eHead = new GLUhalfEdge(true);
    GLUhalfEdge eHeadSym = new GLUhalfEdge(false);

    GLUmesh() {
    }
}

