/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.random;

import org.uncommons.maths.random.SeedException;

public interface SeedGenerator {
    public byte[] generateSeed(int var1) throws SeedException;
}

