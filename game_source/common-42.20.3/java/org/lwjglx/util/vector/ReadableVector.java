/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx.util.vector;

import java.nio.FloatBuffer;
import org.lwjglx.util.vector.Vector;

public interface ReadableVector {
    public float length();

    public float lengthSquared();

    public Vector store(FloatBuffer var1);
}

