/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx.util.vector;

import java.io.Serializable;
import java.nio.FloatBuffer;
import org.lwjglx.util.vector.ReadableVector;

public abstract class Vector
implements Serializable,
ReadableVector {
    protected Vector() {
    }

    @Override
    public final float length() {
        return (float)Math.sqrt(this.lengthSquared());
    }

    @Override
    public abstract float lengthSquared();

    public abstract Vector load(FloatBuffer var1);

    public abstract Vector negate();

    public final Vector normalise() {
        float len = this.length();
        if (len != 0.0f) {
            float l = 1.0f / len;
            return this.scale(l);
        }
        throw new IllegalStateException("Zero length vector");
    }

    @Override
    public abstract Vector store(FloatBuffer var1);

    public abstract Vector scale(float var1);
}

