/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.pack200;

import org.apache.commons.compress.harmony.pack200.CPConstant;

public class CPFloat
extends CPConstant<CPFloat> {
    private final float theFloat;

    public CPFloat(float theFloat) {
        this.theFloat = theFloat;
    }

    @Override
    public int compareTo(CPFloat obj) {
        return Float.compare(this.theFloat, obj.theFloat);
    }

    public float getFloat() {
        return this.theFloat;
    }
}

