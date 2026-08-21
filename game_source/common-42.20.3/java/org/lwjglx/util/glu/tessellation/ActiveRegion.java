/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx.util.glu.tessellation;

import org.lwjglx.util.glu.tessellation.DictNode;
import org.lwjglx.util.glu.tessellation.GLUhalfEdge;

class ActiveRegion {
    GLUhalfEdge eUp;
    DictNode nodeUp;
    int windingNumber;
    boolean inside;
    boolean sentinel;
    boolean dirty;
    boolean fixUpperEdge;

    ActiveRegion() {
    }
}

