/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.opengl;

import org.lwjgl.opengl.GL;

public class NVTextureBarrier {
    protected NVTextureBarrier() {
        throw new UnsupportedOperationException();
    }

    public static native void glTextureBarrierNV();

    static {
        GL.initialize();
    }
}

