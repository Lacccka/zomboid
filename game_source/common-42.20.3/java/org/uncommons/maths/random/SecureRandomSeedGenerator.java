/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.random;

import java.security.SecureRandom;
import org.uncommons.maths.random.SeedException;
import org.uncommons.maths.random.SeedGenerator;

public class SecureRandomSeedGenerator
implements SeedGenerator {
    private static final SecureRandom SOURCE = new SecureRandom();

    public byte[] generateSeed(int length) throws SeedException {
        return SOURCE.generateSeed(length);
    }

    public String toString() {
        return "java.security.SecureRandom";
    }
}

